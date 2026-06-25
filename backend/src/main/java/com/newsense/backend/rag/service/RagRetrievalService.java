package com.newsense.backend.rag.service;

import com.newsense.backend.ai.embedding.OpenAiEmbeddingClient;
import com.newsense.backend.ai.quiz.EconomicTermContext;
import com.newsense.backend.article.document.ArticleContent;
import com.newsense.backend.article.domain.ArticleMeta;
import com.newsense.backend.article.dto.ArticleCardResponse;
import com.newsense.backend.article.repository.ArticleContentRepository;
import com.newsense.backend.article.repository.ArticleMetaRepository;
import com.newsense.backend.common.exception.CustomException;
import com.newsense.backend.common.exception.ErrorCode;
import com.newsense.backend.rag.config.VectorSearchProperties;
import com.newsense.backend.rag.dto.RagArticleResultResponse;
import com.newsense.backend.rag.dto.RagMatchedChunkResponse;
import com.newsense.backend.rag.dto.RagRecommendationResponse;
import com.newsense.backend.rag.dto.RagSearchResponse;
import com.newsense.backend.rag.dto.ScoreBreakdown;
import com.newsense.backend.rag.service.RagVectorSearchService.VectorResult;
import com.newsense.backend.wrongnote.repository.WrongNoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagRetrievalService {

    // ─── 상수 ────────────────────────────────────────────────────────────────
    private static final int MAX_CANDIDATE_ARTICLES     = 120;
    private static final int MAX_RESULT_LIMIT           = 20;
    private static final int DEFAULT_RESULT_LIMIT       = 5;
    private static final int MAX_CHUNKS_PER_ARTICLE     = 3;
    private static final int QUIZ_EVIDENCE_CHUNK_LIMIT  = 5;
    private static final int SNIPPET_RADIUS             = 120;
    private static final int MIN_QUERY_LENGTH           = 2;

    /** 벡터 후보 배수: 최종 limit보다 더 많이 가져와 키워드 re-scoring */
    private static final int VECTOR_CANDIDATE_MULTIPLIER = 4;
    private static final double DEFAULT_VECTOR_WEIGHT = 0.45;
    private static final double DEFAULT_KEYWORD_WEIGHT = 0.20;
    private static final double DEFAULT_RECENCY_WEIGHT = 0.10;
    private static final double DEFAULT_CATEGORY_WEIGHT = 0.10;
    private static final double DEFAULT_WEAKNESS_WEIGHT = 0.10;

    private static final Set<String> STOP_WORDS = Set.of(
            "그리고", "그러나", "하지만", "관련", "기사", "뉴스", "내용", "대한", "대해",
            "으로", "에서", "이다", "있는", "하는", "했다", "한다", "이번", "최근");

    // ─── 의존성 ──────────────────────────────────────────────────────────────
    private final ArticleMetaRepository    articleMetaRepository;
    private final ArticleContentRepository articleContentRepository;
    private final WrongNoteRepository      wrongNoteRepository;
    private final OpenAiEmbeddingClient    embeddingClient;
    private final RagVectorSearchService   vectorSearchService;
    private final VectorSearchProperties   vectorSearchProperties;

    // ─────────────────────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public RagSearchResponse search(String query, int limit) {
        List<String> keywords = extractKeywords(query);
        if (keywords.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        int normalizedLimit = normalizeLimit(limit);
        String reason = "검색어와 기사 청크가 매칭되었습니다.";
        RecommendationContext context = RecommendationContext.empty();

        List<RagArticleResultResponse> results = vectorSearchProperties.enabled()
                ? hybridSearch(query, keywords, normalizedLimit, reason, context)
                : retrieveByKeywords(keywords, normalizedLimit, reason, context);

        return new RagSearchResponse(query.trim(), keywords, results);
    }

    @Transactional(readOnly = true)
    public RagRecommendationResponse recommendForWeakness(Long userId, int limit) {
        List<com.newsense.backend.wrongnote.domain.WrongNote> notes = wrongNoteRepository.findWeaknessSignals(userId);
        RecommendationContext context = RecommendationContext.from(notes);
        List<String> weaknessTerms = context.weaknessTerms().stream().limit(10).toList();

        if (weaknessTerms.isEmpty()) {
            log.info("RAG weakness recommendation fallback: no weakness signals for userId={}", userId);
            return new RagRecommendationResponse(List.of(), latestFallback(normalizeLimit(limit)));
        }

        List<String> keywords = toDistinctList(
                weaknessTerms.stream()
                        .flatMap(term -> extractKeywords(term).stream()));
        List<String> effectiveKeywords = keywords.isEmpty() ? weaknessTerms : keywords;

        String reason = "오답노트의 취약 경제 용어와 관련된 기사입니다.";
        int    lim    = normalizeLimit(limit);
        String query  = String.join(" ", weaknessTerms);

        List<RagArticleResultResponse> results = vectorSearchProperties.enabled()
                ? hybridSearch(query, effectiveKeywords, lim, reason, context)
                : retrieveByKeywords(effectiveKeywords, lim, reason, context);

        if (results.isEmpty()) {
            log.info("RAG weakness recommendation fallback: no matched articles for userId={}, terms={}", userId, weaknessTerms);
            results = latestFallback(lim);
        }

        return new RagRecommendationResponse(weaknessTerms, results);
    }

    public List<String> retrieveQuizEvidence(
            String title,
            ArticleContent content,
            List<EconomicTermContext> economicTerms) {
        List<String> keywords = new ArrayList<>(extractKeywords(title));
        if (economicTerms != null) {
            economicTerms.stream()
                    .map(EconomicTermContext::name)
                    .flatMap(term -> extractKeywords(term).stream())
                    .forEach(keywords::add);
        }
        List<String> distinctKeywords = toDistinctList(keywords.stream());
        if (distinctKeywords.isEmpty()) {
            return safeChunks(content).stream()
                    .limit(QUIZ_EVIDENCE_CHUNK_LIMIT)
                    .toList();
        }
        return scoreChunks(safeChunks(content), distinctKeywords).stream()
                .filter(chunk -> chunk.score() > 0)
                .sorted(Comparator.comparingInt(ScoredChunk::score).reversed())
                .limit(QUIZ_EVIDENCE_CHUNK_LIMIT)
                .map(ScoredChunk::text)
                .toList();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Hybrid search (vector 70% + keyword 30%)
    // ─────────────────────────────────────────────────────────────────────────

    private List<RagArticleResultResponse> hybridSearch(
            String query,
            List<String> keywords,
            int limit,
            String reason,
            RecommendationContext context) {

        // 1. 쿼리 임베딩 생성
        List<Double> queryEmbedding = embeddingClient.embed(query);
        if (queryEmbedding.isEmpty()) {
            log.warn("RAG fallback: query embedding empty, keywords={}", keywords);
            return retrieveByKeywords(keywords, limit, reason, context);
        }

        // 2. 벡터 후보 검색 (배수만큼 더 가져옴)
        int candidateLimit = Math.min(limit * VECTOR_CANDIDATE_MULTIPLIER, MAX_CANDIDATE_ARTICLES);
        List<VectorResult> vectorResults = vectorSearchService.search(queryEmbedding, candidateLimit);
        if (vectorResults.isEmpty()) {
            log.info("RAG fallback: vector search returned no results, keywords={}", keywords);
            return retrieveByKeywords(keywords, limit, reason, context);
        }

        // 3. 벡터 점수 정규화 (max → 1.0)
        double maxVecScore = vectorResults.stream()
                .mapToDouble(VectorResult::score)
                .max()
                .orElse(1.0);
        Map<String, Double> normalizedVecScores = vectorResults.stream()
                .collect(Collectors.toMap(
                        VectorResult::contentId,
                        r -> maxVecScore > 0 ? r.score() / maxVecScore : 0.0));

        // 4. 후보 ArticleMeta 조회
        List<String> contentIds = vectorResults.stream().map(VectorResult::contentId).toList();
        Map<String, ArticleMeta> metaByContentId = articleMetaRepository
                .findAllByMongoDocumentIdIn(contentIds)
                .stream()
                .collect(Collectors.toMap(ArticleMeta::getMongoDocumentId, Function.identity()));

        // 5. 키워드 점수 계산 및 최대값 파악
        record Entry(String contentId, ArticleMeta meta, ArticleContent content, int kScore) {}
        List<Entry> entries = new ArrayList<>();
        int maxKwScore = 1;

        for (String cid : contentIds) {
            ArticleMeta meta = metaByContentId.get(cid);
            if (meta == null) continue;
            ArticleContent content = articleContentRepository.findById(cid).orElse(null);
            int kScore = computeKeywordScore(meta, content, keywords);
            if (kScore > maxKwScore) maxKwScore = kScore;
            entries.add(new Entry(cid, meta, content, kScore));
        }

        // 6. 하이브리드 점수 계산 후 정렬·제한
        final int maxKw = maxKwScore;
        return entries.stream()
                .map(e -> {
                    double vecScore      = normalizedVecScores.getOrDefault(e.contentId(), 0.0);
                    double kwScore       = (double) e.kScore() / maxKw;
                    double recencyScore  = computeRecencyScore(e.meta());
                    double categoryScore = context.categoryScore(e.meta());
                    double weaknessScore = context.weaknessScore(e.meta(), e.content());
                    double hybridScore = vectorWeight() * vecScore
                            + keywordWeight() * kwScore
                            + recencyWeight() * recencyScore
                            + categoryWeight() * categoryScore
                            + weaknessWeight() * weaknessScore;
                    // DTO가 int score를 사용하므로 0~100 스케일로 변환
                    int intScore = (int) Math.round(hybridScore * 100);
                    ScoreBreakdown breakdown = new ScoreBreakdown(
                            vecScore,
                            kwScore,
                            recencyScore,
                            categoryScore,
                            weaknessScore,
                            0.0,
                            hybridScore
                    );
                    return buildResult(e.meta(), e.content(), keywords, intScore, breakdown, context.reasonFor(e.meta(), reason));
                })
                .sorted(Comparator.comparingInt(RagArticleResultResponse::score).reversed())
                .limit(limit)
                .toList();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Keyword-only search (기존 방식 / fallback)
    // ─────────────────────────────────────────────────────────────────────────

    private List<RagArticleResultResponse> retrieveByKeywords(
            List<String> keywords, int limit, String reason, RecommendationContext context) {
        PageRequest pageRequest = PageRequest.of(
                0, MAX_CANDIDATE_ARTICLES,
                Sort.by(
                        Sort.Order.desc("publishedAt").nullsLast(),
                        Sort.Order.desc("collectedAt"),
                        Sort.Order.desc("id")));

        return articleMetaRepository.findAll(pageRequest).stream()
                .map(article -> scoreArticle(article, keywords, reason, context))
                .filter(this::hasRecommendationSignal)
                .sorted(Comparator.comparingInt(RagArticleResultResponse::score).reversed())
                .limit(limit)
                .toList();
    }

    private boolean hasRecommendationSignal(RagArticleResultResponse result) {
        ScoreBreakdown breakdown = result.scoreBreakdown();
        return breakdown.keywordScore() > 0
                || breakdown.categoryScore() > 0
                || breakdown.weaknessScore() > 0;
    }

    private RagArticleResultResponse scoreArticle(
            ArticleMeta article,
            List<String> keywords,
            String reason,
            RecommendationContext context
    ) {
        ArticleContent content = articleContentRepository.findById(article.getMongoDocumentId()).orElse(null);
        int totalScore = computeKeywordScore(article, content, keywords);
        double recencyScore = computeRecencyScore(article);
        double categoryScore = context.categoryScore(article);
        double weaknessScore = context.weaknessScore(article, content);
        double finalScore = totalScore
                + recencyWeight() * recencyScore * 100
                + categoryWeight() * categoryScore * 100
                + weaknessWeight() * weaknessScore * 100;
        return buildResult(
                article,
                content,
                keywords,
                (int) Math.round(finalScore),
                new ScoreBreakdown(0.0, totalScore, recencyScore, categoryScore, weaknessScore, 0.0, finalScore),
                context.reasonFor(article, reason)
        );
    }

    private List<RagArticleResultResponse> latestFallback(int limit) {
        PageRequest pageRequest = PageRequest.of(
                0,
                limit,
                Sort.by(Sort.Order.desc("publishedAt").nullsLast(), Sort.Order.desc("collectedAt"), Sort.Order.desc("id"))
        );
        return articleMetaRepository.findAll(pageRequest).stream()
                .map(article -> {
                    ArticleContent content = articleContentRepository.findById(article.getMongoDocumentId()).orElse(null);
                    double recencyScore = computeRecencyScore(article);
                    return buildResult(
                            article,
                            content,
                            List.of(),
                            (int) Math.round(recencyScore * 100),
                            new ScoreBreakdown(0.0, 0.0, recencyScore, 0.0, 0.0, 0.0, recencyScore),
                            "개인화 데이터가 부족해 최신 기사로 추천했습니다."
                    );
                })
                .toList();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Shared helpers
    // ─────────────────────────────────────────────────────────────────────────

    private int computeKeywordScore(ArticleMeta meta, ArticleContent content, List<String> keywords) {
        int metaScore = scoreText(meta.getTitle(), keywords) * 4
                + scoreText(meta.getSummary(), keywords) * 2;
        if (content == null) return metaScore;
        int chunkScore = scoreChunks(safeChunks(content), keywords).stream()
                .filter(c -> c.score() > 0)
                .mapToInt(ScoredChunk::score)
                .sum();
        return metaScore + chunkScore;
    }

    private RagArticleResultResponse buildResult(
            ArticleMeta meta,
            ArticleContent content,
            List<String> keywords,
            int score,
            ScoreBreakdown scoreBreakdown,
            String reason) {

        List<ScoredChunk> matchedChunks = content == null ? List.of()
                : scoreChunks(safeChunks(content), keywords).stream()
                        .filter(c -> c.score() > 0)
                        .sorted(Comparator.comparingInt(ScoredChunk::score).reversed())
                        .limit(MAX_CHUNKS_PER_ARTICLE)
                        .toList();

        List<String> matchedKeywords = keywords.stream()
                .filter(keyword -> containsKeyword(meta.getTitle(), keyword)
                        || containsKeyword(meta.getSummary(), keyword)
                        || matchedChunks.stream().anyMatch(chunk -> containsKeyword(chunk.text(), keyword)))
                .distinct()
                .toList();

        return new RagArticleResultResponse(
                ArticleCardResponse.from(meta),
                matchedChunks.stream()
                        .map(chunk -> new RagMatchedChunkResponse(
                                chunk.index(),
                                createSnippet(chunk.text(), keywords),
                                chunk.score()))
                        .toList(),
                matchedKeywords,
                score,
                scoreBreakdown,
                reason);
    }

    private double computeRecencyScore(ArticleMeta meta) {
        if (meta.getPublishedAt() == null) {
            return 0.4;
        }
        long days = Math.max(0, ChronoUnit.DAYS.between(meta.getPublishedAt(), LocalDate.now()));
        return Math.max(0.0, 1.0 - Math.min(days, 30) / 30.0);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Text scoring utilities
    // ─────────────────────────────────────────────────────────────────────────

    private List<ScoredChunk> scoreChunks(List<String> chunks, List<String> keywords) {
        List<ScoredChunk> scored = new ArrayList<>();
        for (int index = 0; index < chunks.size(); index++) {
            String chunk = chunks.get(index);
            int score = scoreText(chunk, keywords);
            scored.add(new ScoredChunk(index, chunk, score));
        }
        return scored;
    }

    private int scoreText(String text, List<String> keywords) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        String normalizedText = normalize(text);
        int score = 0;
        for (String keyword : keywords) {
            if (keyword.length() < MIN_QUERY_LENGTH) {
                continue;
            }
            int occurrences = countOccurrences(normalizedText, keyword);
            if (occurrences > 0) {
                score += keyword.contains(" ") ? occurrences * 4 : occurrences * 2;
            }
        }
        return score;
    }

    private int countOccurrences(String text, String keyword) {
        int count = 0;
        int fromIndex = 0;
        while (fromIndex < text.length()) {
            int index = text.indexOf(keyword, fromIndex);
            if (index < 0) break;
            count++;
            fromIndex = index + keyword.length();
        }
        return count;
    }

    private boolean containsKeyword(String text, String keyword) {
        return text != null && normalize(text).contains(keyword);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Keyword extraction
    // ─────────────────────────────────────────────────────────────────────────

    private List<String> extractKeywords(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        String normalized = normalize(query).replaceAll("[^0-9a-z가-힣]+", " ");
        String[] tokens = normalized.split("\\s+");
        LinkedHashSet<String> keywords = new LinkedHashSet<>();
        for (String token : tokens) {
            if (token.length() >= MIN_QUERY_LENGTH && !STOP_WORDS.contains(token)) {
                keywords.add(token);
            }
        }
        String phrase = normalized.trim();
        if (phrase.length() >= MIN_QUERY_LENGTH && phrase.contains(" ")) {
            keywords.add(phrase);
        }
        return List.copyOf(keywords);
    }

    private List<String> toDistinctList(java.util.stream.Stream<String> stream) {
        return stream.collect(
                LinkedHashSet<String>::new,
                LinkedHashSet::add,
                LinkedHashSet::addAll)
                .stream()
                .toList();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Text utilities
    // ─────────────────────────────────────────────────────────────────────────

    private String normalize(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
    }

    private String createSnippet(String text, List<String> keywords) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String normalizedText = normalize(text);
        int firstMatch = keywords.stream()
                .map(normalizedText::indexOf)
                .filter(index -> index >= 0)
                .min(Integer::compareTo)
                .orElse(0);
        int start = Math.max(0, firstMatch - SNIPPET_RADIUS);
        int end = Math.min(text.length(), firstMatch + SNIPPET_RADIUS);
        String prefix = start > 0 ? "..." : "";
        String suffix = end < text.length() ? "..." : "";
        return prefix + text.substring(start, end).trim() + suffix;
    }

    private List<String> safeChunks(ArticleContent content) {
        if (content.getChunks() == null || content.getChunks().isEmpty()) {
            return List.of(content.getCleanText());
        }
        return content.getChunks();
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_RESULT_LIMIT;
        }
        return Math.min(limit, MAX_RESULT_LIMIT);
    }

    private record ScoredChunk(int index, String text, int score) {}

    private record RecommendationContext(
            List<String> weaknessTerms,
            Map<String, Double> termWeights,
            Map<String, Double> categoryWeights
    ) {
        static RecommendationContext empty() {
            return new RecommendationContext(List.of(), Map.of(), Map.of());
        }

        static RecommendationContext from(List<com.newsense.backend.wrongnote.domain.WrongNote> notes) {
            Map<String, Double> termWeights = new HashMap<>();
            Map<String, Double> categoryWeights = new HashMap<>();
            for (com.newsense.backend.wrongnote.domain.WrongNote note : notes) {
                double weight = Math.max(1, note.getMistakeCount());
                if (note.getCategory() != null) {
                    categoryWeights.merge(note.getCategory().name(), weight, Double::sum);
                }
                for (String term : note.getRelatedTerms()) {
                    if (term != null && !term.isBlank()) {
                        termWeights.merge(normalizeStatic(term), weight, Double::sum);
                    }
                }
            }
            double maxTerm = termWeights.values().stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
            double maxCategory = categoryWeights.values().stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
            termWeights.replaceAll((key, value) -> value / maxTerm);
            categoryWeights.replaceAll((key, value) -> value / maxCategory);
            return new RecommendationContext(List.copyOf(termWeights.keySet()), termWeights, categoryWeights);
        }

        double categoryScore(ArticleMeta meta) {
            if (meta.getCategory() == null) return 0.0;
            return categoryWeights.getOrDefault(meta.getCategory().name(), 0.0);
        }

        double weaknessScore(ArticleMeta meta, ArticleContent content) {
            if (termWeights.isEmpty()) return 0.0;
            String text = normalizeStatic(String.join(" ",
                    meta.getTitle() == null ? "" : meta.getTitle(),
                    meta.getSummary() == null ? "" : meta.getSummary(),
                    content == null || content.getCleanText() == null ? "" : content.getCleanText()));
            return termWeights.entrySet().stream()
                    .filter(entry -> text.contains(entry.getKey()))
                    .mapToDouble(Map.Entry::getValue)
                    .max()
                    .orElse(0.0);
        }

        String reasonFor(ArticleMeta meta, String fallback) {
            if (categoryScore(meta) > 0) {
                return "오답노트에서 자주 틀린 " + meta.getCategory().getDisplayName() + " 분야와 관련된 기사입니다.";
            }
            if (!weaknessTerms.isEmpty()) {
                return "오답노트의 취약 용어와 관련된 기사입니다.";
            }
            return fallback;
        }
    }

    private double vectorWeight() {
        return vectorSearchProperties.vectorWeight() > 0 ? vectorSearchProperties.vectorWeight() : DEFAULT_VECTOR_WEIGHT;
    }

    private double keywordWeight() {
        return vectorSearchProperties.keywordWeight() > 0 ? vectorSearchProperties.keywordWeight() : DEFAULT_KEYWORD_WEIGHT;
    }

    private double recencyWeight() {
        return vectorSearchProperties.recencyWeight() > 0 ? vectorSearchProperties.recencyWeight() : DEFAULT_RECENCY_WEIGHT;
    }

    private double categoryWeight() {
        return vectorSearchProperties.categoryWeight() > 0 ? vectorSearchProperties.categoryWeight() : DEFAULT_CATEGORY_WEIGHT;
    }

    private double weaknessWeight() {
        return vectorSearchProperties.weaknessWeight() > 0 ? vectorSearchProperties.weaknessWeight() : DEFAULT_WEAKNESS_WEIGHT;
    }

    private static String normalizeStatic(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
    }
}
