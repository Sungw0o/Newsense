package com.newsense.backend.rag.service;

import com.newsense.backend.ai.quiz.EconomicTermContext;
import com.newsense.backend.article.document.ArticleContent;
import com.newsense.backend.article.domain.ArticleMeta;
import com.newsense.backend.article.dto.ArticleCardResponse;
import com.newsense.backend.article.repository.ArticleContentRepository;
import com.newsense.backend.article.repository.ArticleMetaRepository;
import com.newsense.backend.common.exception.CustomException;
import com.newsense.backend.common.exception.ErrorCode;
import com.newsense.backend.rag.dto.RagArticleResultResponse;
import com.newsense.backend.rag.dto.RagMatchedChunkResponse;
import com.newsense.backend.rag.dto.RagRecommendationResponse;
import com.newsense.backend.rag.dto.RagSearchResponse;
import com.newsense.backend.wrongnote.domain.WrongNote;
import com.newsense.backend.wrongnote.repository.WrongNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RagRetrievalService {

    private static final int MAX_CANDIDATE_ARTICLES = 120;
    private static final int MAX_RESULT_LIMIT = 20;
    private static final int DEFAULT_RESULT_LIMIT = 5;
    private static final int MAX_CHUNKS_PER_ARTICLE = 3;
    private static final int QUIZ_EVIDENCE_CHUNK_LIMIT = 5;
    private static final int SNIPPET_RADIUS = 120;
    private static final int MIN_QUERY_LENGTH = 2;
    private static final Set<String> STOP_WORDS = Set.of(
            "그리고", "그러나", "하지만", "관련", "기사", "뉴스", "내용", "대한", "대해",
            "으로", "에서", "이다", "있는", "하는", "했다", "한다", "이번", "최근");

    private final ArticleMetaRepository articleMetaRepository;
    private final ArticleContentRepository articleContentRepository;
    private final WrongNoteRepository wrongNoteRepository;

    @Transactional(readOnly = true)
    public RagSearchResponse search(String query, int limit) {
        List<String> keywords = extractKeywords(query);
        if (keywords.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return new RagSearchResponse(
                query.trim(),
                keywords,
                retrieveByKeywords(keywords, normalizeLimit(limit), "검색어와 기사 청크가 매칭되었습니다."));
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

        return new RagRecommendationResponse(
                weaknessTerms,
                retrieveByKeywords(
                        keywords.isEmpty() ? weaknessTerms : keywords,
                        normalizeLimit(limit),
                        "오답노트의 취약 경제 용어와 관련된 기사입니다."));
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

    private List<RagArticleResultResponse> retrieveByKeywords(List<String> keywords, int limit, String reason) {
        PageRequest pageRequest = PageRequest.of(
                0,
                MAX_CANDIDATE_ARTICLES,
                Sort.by(
                        Sort.Order.desc("publishedAt").nullsLast(),
                        Sort.Order.desc("collectedAt"),
                        Sort.Order.desc("id")));

        return articleMetaRepository.findAll(pageRequest).stream()
                .map(article -> scoreArticle(article, keywords, reason))
                .filter(result -> result.score() > 0)
                .sorted(Comparator.comparingInt(RagArticleResultResponse::score).reversed())
                .limit(limit)
                .toList();
    }

    private RagArticleResultResponse scoreArticle(ArticleMeta article, List<String> keywords, String reason) {
        ArticleContent content = articleContentRepository.findById(article.getMongoDocumentId()).orElse(null);
        List<ScoredChunk> chunks = content == null ? List.of() : scoreChunks(safeChunks(content), keywords);
        int metadataScore = scoreText(article.getTitle(), keywords) * 4
                + scoreText(article.getSummary(), keywords) * 2;

        List<ScoredChunk> matchedChunks = chunks.stream()
                .filter(chunk -> chunk.score() > 0)
                .sorted(Comparator.comparingInt(ScoredChunk::score).reversed())
                .limit(MAX_CHUNKS_PER_ARTICLE)
                .toList();

        int chunkScore = matchedChunks.stream().mapToInt(ScoredChunk::score).sum();
        int totalScore = metadataScore + chunkScore;
        List<String> matchedKeywords = keywords.stream()
                .filter(keyword -> containsKeyword(article.getTitle(), keyword)
                        || containsKeyword(article.getSummary(), keyword)
                        || matchedChunks.stream().anyMatch(chunk -> containsKeyword(chunk.text(), keyword)))
                .distinct()
                .toList();

        return new RagArticleResultResponse(
                ArticleCardResponse.from(article),
                matchedChunks.stream()
                        .map(chunk -> new RagMatchedChunkResponse(
                                chunk.index(),
                                createSnippet(chunk.text(), keywords),
                                chunk.score()))
                        .toList(),
                matchedKeywords,
                totalScore,
                reason);
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
            if (index < 0) {
                break;
            }
            count++;
            fromIndex = index + keyword.length();
        }
        return count;
    }

    private boolean containsKeyword(String text, String keyword) {
        return text != null && normalize(text).contains(keyword);
    }

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

    private String normalize(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT);
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

    private record ScoredChunk(int index, String text, int score) {
    }
}
