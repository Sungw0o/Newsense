package com.newsense.backend.rag.runner;

import com.newsense.backend.article.document.ArticleContent;
import com.newsense.backend.article.repository.ArticleContentRepository;
import com.newsense.backend.rag.config.VectorSearchProperties;
import com.newsense.backend.rag.service.ArticleEmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 애플리케이션 기동 시 임베딩이 없는 기존 기사에 대해 임베딩을 백필합니다.
 *
 * <p>활성화 조건: {@code rag.embedding.backfill.enabled=true}
 * <p>각 기사마다 {@link ArticleEmbeddingService#generateAndStoreAsync}를 호출하므로
 * 비동기(@Async) 스레드 풀에서 순차 처리됩니다.
 * 대량 기사가 있을 경우 애플리케이션 기동 직후 일시적으로 OpenAI API 호출이 집중될 수 있습니다.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "rag.embedding.backfill", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class EmbeddingBackfillRunner implements ApplicationRunner {

    private final ArticleContentRepository articleContentRepository;
    private final ArticleEmbeddingService  articleEmbeddingService;
    private final VectorSearchProperties   vectorSearchProperties;

    @Override
    public void run(ApplicationArguments args) {
        if (!vectorSearchProperties.enabled()) {
            log.info("Vector search disabled — skipping embedding backfill");
            return;
        }

        List<ArticleContent> missing = articleContentRepository.findAllWithoutEmbedding();
        if (missing.isEmpty()) {
            log.info("Embedding backfill: all articles already have embeddings");
            return;
        }

        log.info("Embedding backfill: {} articles without embedding — starting async generation", missing.size());
        for (ArticleContent content : missing) {
            if (content.getCleanText() == null || content.getCleanText().isBlank()) {
                log.debug("Skipping backfill for contentId={} — no cleanText", content.getId());
                continue;
            }
            articleEmbeddingService.generateAndStoreAsync(
                    content.getId(),
                    content.getTitle() != null ? content.getTitle() : "",
                    content.getCleanText());
        }
        log.info("Embedding backfill: all {} tasks submitted to async pool", missing.size());
    }
}
