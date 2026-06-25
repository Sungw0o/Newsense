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

    /** Default hybrid score weights. Runtime values come from rag.vector-search.* properties. */
    private static final double DEFAULT_VECTOR_WEIGHT = 0.45;
    private static final double DEFAULT_KEYWORD_WEIGHT = 0.20;
    private static final double DEFAULT_RECENCY_WEIGHT = 0.10;
    private static final double DEFAULT_CATEGORY_WEIGHT = 0.10;
    private static final double DEFAULT_WEAKNESS_WEIGHT = 0.10;
    private static final double DEFAULT_MARKET_WEIGHT = 0.05;

    /** 벡터 후보 배수: 최종 limit보다 더 많이 가져와 키워드 re-scoring */
    private static final int VECTOR_CANDIDATE_MULTIPLIER = 4;

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

        List<RagArticleResultResponse> results = vectorSearchProperties.enabled()
                ? hybridSearch(query, keywords, normalizedLimit, reason)
                : retrieveByKeywords(keywords, normalizedLimit, reason);

        return new RagSearchResponse(query.trim(), keywords, results);
    }

    @Transactional(readOnly = true)
    public RagRecommendationResponse recommendForWeakness(Long userId, int limit) {
        List<String> weaknessTerms = toDistinctList(
                wrongNoteRepository
                        .findWeaknessSignals(userId)
                        .stream()
                        .flatMap(note -> note.getRelatedTerms().stream())
                        .map(String::trim)
                        .filter(term -> !term.isBlank()))
                .stream()
                .limit(10)
                .toList();

        if (weaknessTerms.isEmpty()) {
            return new RagRecommendationResponse(List.of(), List.of());
        }

        List<String> keywords = toDistinctList(
                weaknessTerms.stream()
                        .flatMap(term -> extractKeywords(term).stream()));
        List<String> effectiveKeywords = keywords.isEmpty() ? weaknessTerms : keywords;

        String reason = "오답노트의 취약 경제 용어와 관련된 기사입니다.";
        int    lim    = normalizeLimit(limit);
        String query  = String.join(" ", weaknessTerms);

        List<RagArticleResultResponse> results = vectorSearchProperties.enabled()
                ? hybridSearch(query, effectiveKeywords, lim, reason)
                : retrieveByKeywords(effectiveKeywords, lim, reason);

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
            String reason) {

        // 1. 쿼리 임베딩 생성
        List<Double> queryEmbedding = embeddingClient.embed(query);
        if (queryEmbedding.isEmpty()) {
            log.warn("Query embedding empty — falling back to keyword-only search");
            return retrieveByKeywords(keywords, limit, reason);
        }

        // 2. 벡터 후보 검색 (배수만큼 더 가져옴)
        int candidateLimit = Math.min(limit * VECTOR_CANDIDATE_MULTIPLIER, MAX_CANDIDATE_ARTICLES);
        List<VectorResult> vectorResults = vectorSearchService.search(queryEmbedding, candidateLimit);
        if (vectorResults.isEmpty()) {
            log.debug("Vector search returned no results — falling back to keyword-only search");
            return retrieveByKeywords(keywords, limit, reason);
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
                    double vecScore = normalizedVecScores.getOrDefault(e.contentId(), 0.0);
                    double kwScore = (double) e.kScore() / maxKw;
                    ScoreBreakdown breakdown = buildScoreBreakdown(
                            vecScore,
                            kwScore,
                            recencyScore(e.meta()),
                            0.0,
                            0.0,
                            0.0
                    );
                    return buildResult(e.meta(), e.content(), keywords, toIntScore(breakdown.finalScore()), breakdown, reason);
                })
                .sorted(Comparator.comparingInt(RagArticleResultResponse::score).reversed())
                .limit(limit)
                .toList();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Keyword-only search (기존 방식 / fallback)
    // ─────────────────────────────────────────────────────────────────────────

    private List<RagArticleResultResponse> retrieveByKeywords(
            List<String> keywords, int limit, String reason) {
        PageRequest pageRequest = PageRequest.of(
                0, MAX_CANDIDATE_ARTICLES,
                Sort.by(
                        Sort.Order.desc("publishedAt").nullsLast(),
                        Sort.Order.desc("collectedAt"),
                        Sort.Order.desc("id")));

        record Entry(ArticleMeta article, ArticleContent content, int keywordScore) {}

        List<Entry> entries = articleMetaRepository.findAll(pageRequest).stream()
                .map(article -> {
                    ArticleContent content = articleContentRepository.findById(article.getMongoDocumentId()).orElse(null);
                    return new Entry(article, content, computeKeywordScore(article, content, keywords));
                })
                .filter(entry -> entry.keywordScore() > 0)
                .toList();

        int maxKeywordScore = entries.stream()
                .mapToInt(Entry::keywordScore)
                .max()
                .orElse(1);

        return entries.stream()
                .map(entry -> {
                    ScoreBreakdown breakdown = buildScoreBreakdown(
                            0.0,
                            (double) entry.keywordScore() / maxKeywordScore,
                            recencyScore(entry.article()),
                            0.0,
                            0.0,
                            0.0
                    );
                    return buildResult(
                            entry.article(),
                            entry.content(),
                            keywords,
                            toIntScore(breakdown.finalScore()),
                            breakdown,
                            reason
                    );
                })
                .sorted(Comparator.comparingInt(RagArticleResultResponse::score).reversed())
                .limit(limit)
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

    // ─────────────────────────────────────────────────────────────────────────
    // Text scoring utilities
    // ─────────────────────────────────────────────────────────────────────────

    private ScoreBreakdown buildScoreBreakdown(
            double vectorScore,
            double keywordScore,
            double recencyScore,
            double categoryScore,
            double weaknessScore,
            double marketScore) {
        double finalScore = weight(vectorSearchProperties.vectorWeight(), DEFAULT_VECTOR_WEIGHT) * vectorScore
                + weight(vectorSearchProperties.keywordWeight(), DEFAULT_KEYWORD_WEIGHT) * keywordScore
                + weight(vectorSearchProperties.recencyWeight(), DEFAULT_RECENCY_WEIGHT) * recencyScore
                + weight(vectorSearchProperties.categoryWeight(), DEFAULT_CATEGORY_WEIGHT) * categoryScore
                + weight(vectorSearchProperties.weaknessWeight(), DEFAULT_WEAKNESS_WEIGHT) * weaknessScore
                + weight(vectorSearchProperties.marketWeight(), DEFAULT_MARKET_WEIGHT) * marketScore;
        return new ScoreBreakdown(
                clamp01(vectorScore),
                clamp01(keywordScore),
                clamp01(recencyScore),
                clamp01(categoryScore),
                clamp01(weaknessScore),
                clamp01(marketScore),
                clamp01(finalScore)
        );
    }

    private double recencyScore(ArticleMeta article) {
        if (article.getPublishedAt() == null) {
            return 0.0;
        }
        long daysAge = Math.max(0, ChronoUnit.DAYS.between(article.getPublishedAt(), LocalDate.now()));
        return Math.exp(-0.05 * daysAge);
    }

    private int toIntScore(double finalScore) {
        return (int) Math.round(clamp01(finalScore) * 100);
    }

    private double weight(double configured, double fallback) {
        return configured > 0 ? configured : fallback;
    }

    private double clamp01(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, value));
    }

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

    private boolean containsKeyword(String text, String key