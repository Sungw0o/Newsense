package com.newsense.backend.rag.service;

import com.newsense.backend.article.document.ArticleContent;
import com.newsense.backend.rag.config.VectorSearchProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 벡터 유사도 검색 서비스.
 *
 * <p>mode=atlas: MongoDB Atlas $vectorSearch 집계 파이프라인 사용 (프로덕션)
 * <p>mode=local: 전체 임베딩 로드 후 Java에서 코사인 유사도 계산 (로컬/개발)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagVectorSearchService {

    private final VectorSearchProperties props;
    private final MongoTemplate mongoTemplate;

    public record VectorResult(String contentId, double score) {}

    /**
     * 쿼리 임베딩 벡터와 가장 유사한 기사 컨텐츠 ID 목록을 반환합니다.
     *
     * @param queryEmbedding 검색 쿼리 임베딩
     * @param limit          반환할 최대 결과 수
     * @return 유사도 내림차순 정렬된 결과
     */
    public List<VectorResult> search(List<Double> queryEmbedding, int limit) {
        if (queryEmbedding == null || queryEmbedding.isEmpty()) {
            return List.of();
        }
        try {
            return props.isAtlasMode()
                    ? atlasVectorSearch(queryEmbedding, limit)
                    : localCosineSearch(queryEmbedding, limit);
        } catch (Exception e) {
            log.warn("Vector search failed (mode={}), returning empty: {}", props.mode(), e.getMessage());
            return List.of();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Atlas $vectorSearch
    // ─────────────────────────────────────────────────────────────────────────

    private List<VectorResult> atlasVectorSearch(List<Double> queryEmbedding, int limit) {
        Document vectorSearchStage = new Document("$vectorSearch", new Document()
                .append("index", props.indexName())
                .append("path", "embedding")
                .append("queryVector", queryEmbedding)
                .append("numCandidates", props.numCandidates())
                .append("limit", limit));

        Document addScoreStage = new Document("$addFields",
                new Document("vectorScore", new Document("$meta", "vectorSearchScore")));

        Document projectStage = new Document("$project",
                new Document("vectorScore", 1));

        List<Document> pipeline = List.of(vectorSearchStage, addScoreStage, projectStage);

        return mongoTemplate.getCollection("article_content")
                .aggregate(pipeline, Document.class)
                .into(new ArrayList<>())
                .stream()
                .map(doc -> new VectorResult(
                        doc.getObjectId("_id").toHexString(),
                        doc.getDouble("vectorScore")))
                .toList();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Local in-memory cosine similarity
    // ─────────────────────────────────────────────────────────────────────────

    private List<VectorResult> localCosineSearch(List<Double> queryEmbedding, int limit) {
        List<ArticleContent> withEmbeddings = mongoTemplate.find(
                Query.query(Criteria.where("embedding").exists(true).ne(null)),
                ArticleContent.class
        );

        if (withEmbeddings.isEmpty()) {
            log.debug("No articles with embeddings found for local vector search");
            return List.of();
        }

        return withEmbeddings.stream()
                .map(content -> new VectorResult(
                        content.getId(),
                        cosineSimilarity(queryEmbedding, content.getEmbedding())))
                .filter(r -> r.score() > 0.0)
                .sorted(Comparator.comparingDouble(VectorResult::score).reversed())
                .limit(limit)
                .toList();
    }

    private double cosineSimilarity(List<Double> a, List<Double> b) {
        if (a == null || b == null || a.size() != b.size() || a.isEmpty()) {
            return 0.0;
        }
        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < a.size(); i++) {
            double ai = a.get(i);
            double bi = b.get(i);
            dot += ai * bi;
            normA += ai * ai;
            normB += bi * bi;
        }
        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
