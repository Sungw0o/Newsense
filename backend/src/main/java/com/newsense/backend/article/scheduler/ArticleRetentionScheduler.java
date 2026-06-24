package com.newsense.backend.article.scheduler;

import com.newsense.backend.article.service.ArticleRetentionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "crawler", name = "enabled", havingValue = "true")
public class ArticleRetentionScheduler {

    private final ArticleRetentionService articleRetentionService;

    @Scheduled(cron = "0 0 4 1 * *")
    public void cleanupExpiredInactiveArticles() {
        log.info("Scheduled article retention cleanup started");
        articleRetentionService.deleteExpiredInactiveArticles();
    }
}
