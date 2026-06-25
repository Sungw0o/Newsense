package com.newsense.backend.article.crawler.service;

import com.newsense.backend.article.crawler.client.GoogleNewsRssCollector;
import com.newsense.backend.article.crawler.client.NaverNewsCrawler;
import com.newsense.backend.article.crawler.client.NaverSearchNewsApiCollector;
import com.newsense.backend.article.crawler.client.NewsApiCollector;
import com.newsense.backend.article.crawler.model.CrawlRunResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortalNewsCrawlerService {

    private final NaverNewsCrawler naverNewsCrawler;
    private final NaverSearchNewsApiCollector naverSearchNewsApiCollector;
    private final NewsApiCollector newsApiCollector;
    private final GoogleNewsRssCollector googleNewsRssCollector;

    public CrawlRunResult collectAll() {
        CrawlRunResult total = CrawlRunResult.empty()
                .add(naverNewsCrawler.collect())
                .add(naverSearchNewsApiCollector.collect())
                .add(newsApiCollector.collect())
                .add(googleNewsRssCollector.collect());
        log.info(
                "Portal news crawl completed: discovered={}, saved={}, skipped={}, failed={}",
                total.discovered(),
                total.saved(),
                total.skipped(),
                total.failed()
        );
        return total;
    }
}
