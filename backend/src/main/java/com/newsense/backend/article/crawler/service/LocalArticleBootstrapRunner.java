package com.newsense.backend.article.crawler.service;

import com.newsense.backend.article.crawler.config.CrawlerProperties;
import com.newsense.backend.article.crawler.model.CrawlRunResult;
import com.newsense.backend.article.repository.ArticleMetaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "crawler", name = "bootstrap-enabled", havingValue = "true")
public class LocalArticleBootstrapRunner implements ApplicationRunner {

    private final ArticleMetaRepository articleMetaRepository;
    private final PublicNewsCrawlerService crawlerService;
    private final CrawlerProperties properties;

    @Override
    public void run(ApplicationArguments args) {
        long currentCount = articleMetaRepository.count();
        if (currentCount >= properties.minimumArticles()) {
            log.info(
                    "Local article bootstrap skipped: currentCount={}, minimumArticles={}",
                    currentCount,
                    properties.minimumArticles()
            );
            return;
        }

        log.info(
                "Local article bootstrap started: currentCount={}, minimumArticles={}",
                currentCount,
                properties.minimumArticles()
        );
        CrawlRunResult result = crawlerService.collectAll();
        long updatedCount = articleMetaRepository.count();
        log.info(
                "Local article bootstrap completed: discovered={}, saved={}, skipped={}, failed={}, updatedCount={}",
                result.discovered(),
                result.saved(),
                result.skipped(),
                result.failed(),
                updatedCount
        );

        if (updatedCount < properties.minimumArticles()) {
            log.warn(
                    "Local article count is still below target: updatedCount={}, minimumArticles={}",
                    updatedCount,
                    properties.minimumArticles()
            );
        }
    }
}
