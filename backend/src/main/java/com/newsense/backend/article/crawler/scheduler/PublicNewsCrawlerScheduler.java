package com.newsense.backend.article.crawler.scheduler;

import com.newsense.backend.article.crawler.service.PublicNewsCrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "crawler", name = "enabled", havingValue = "true")
public class PublicNewsCrawlerScheduler {

    private final PublicNewsCrawlerService crawlerService;

    @Scheduled(
            initialDelayString = "${crawler.initial-delay:30000}",
            fixedDelayString = "${crawler.fixed-delay:3600000}"
    )
    public void collectPublicNews() {
        log.info("Scheduled public news crawl started");
        crawlerService.collectAll();
    }
}
