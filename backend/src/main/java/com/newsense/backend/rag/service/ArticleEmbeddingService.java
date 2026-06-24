package com.newsense.backend.rag.service;

import com.newsense.backend.ai.embedding.OpenAiEmbeddingClient;
import com.newsense.backend.article.document.ArticleContent;
import com.newsense.backend.rag.config.VectorSearchProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleEmbeddingService {

    private static final int EMBEDDING_TEXT_MAX_CHARS = 8_000;

    private final OpenAiEmbeddingClient embeddingClient;
    private final MongoTemplate mongoTemplate;
    private final VectorSearchProperties vectorSearchProperties;

    /**
     * 기사 본문 임베딩을 비동기로 생성하여 MongoDB에 저장합니다.
     * 임베딩 생성 실패 시 기사 저장에는 영향을 주지 않습니다.
     *
     * @param contentId MongoDB 문서 ID
     * @param title     기사 제목
     * @param cleanText 정제된 본문
     */
    @Async
    public void generateAndStoreAsync(String contentId, String title, String cleanText) {
        if (!vectorSearchProperties.enabled()) {
            return;
        }
        try {
            String embeddingInput = buildEmbeddingInput(title, cleanText);
            List<Double> embedding = embeddingClient.embed(embeddingInput);

            if (embedding.isEmpty()) {
                log.debug("Empty embedding returned for contentId={}, skipping store", contentId);
                return;
            }

            mongoTemplate.updateFirst(
                    Query.query(Criteria.where("_id").is(contentId)),
                    new Update().set("embedding", embedding),
                    ArticleContent.class
            );
            log.debug("Embedding stored: contentId={}, dimensions={}", contentId, embedding.size());
        } catch (Exception e) {
            log.warn("Embedding generation failed for contentId={}: {}", contentId, e.getMessage());
        }
    }

    /**
     * 임베딩 입력 텍스트 구성: 제목 + 본문 앞부분
     */
    private String buildEmbeddingInput(String title, String cleanText) {
        String body = cleanText.length() > EMBEDDING_TEXT_MAX_CHARS
                ? cleanText.substring(0, EMBEDDING_TEXT_MAX_CHARS)
                : cleanText;
        return title + "\n\n" + body;
    }
}
