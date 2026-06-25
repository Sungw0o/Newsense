package com.newsense.backend.article.crawler.scheduler;

import com.newsense.backend.article.crawler.service.PublicNewsCrawlerService;
import com.newsense.backend.article.crawler.service.PortalNewsCrawlerService;
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
    private final PortalNewsCrawlerService portalNewsCrawlerService;

    @Scheduled(cron = "${crawler.cron.public-source:0 0 11,17 * * MON-FRI}")
    public void collectPublicNews() {
        log.info("Scheduled public news crawl started");
        crawlerService.collectAll();
    }

    @Scheduled(cron = "${crawler.cron.portal-source:0 0 8,14,20 * * *}")
    public void collectPortalNews() {
        log.info("Scheduled portal news crawl started");
        portalNewsCrawlerService.collectAll();
    }
}
