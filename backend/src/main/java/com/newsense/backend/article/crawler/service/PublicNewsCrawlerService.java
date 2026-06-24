package com.newsense.backend.article.crawler.service;

import com.newsense.backend.article.crawler.client.PublicNewsCrawlerClient;
import com.newsense.backend.article.crawler.config.CrawlerProperties;
import com.newsense.backend.article.crawler.model.ArticleCandidate;
import com.newsense.backend.article.crawler.model.CrawlRunResult;
import com.newsense.backend.article.crawler.model.CrawledArticle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublicNewsCrawlerService {

    private final CrawlerProperties properties;
    private final PublicNewsCrawlerClient crawlerClient;
    private final ArticlePersistenceService persistenceService;

    public CrawlRunResult collectAll() {
        CrawlRunResult total = CrawlRunResult.empty();
        for (CrawlerProperties.Source source : properties.sources()) {
            if (!source.enabled()) {
                continue;
            }
            total = total.add(collectSource(source));
        }
        log.info(
                "News crawl completed: discovered={}, saved={}, skipped={}, failed={}",
                total.discovered(),
                total.saved(),
                total.skipped(),
                total.failed()
        );
        return total;
    }

    private CrawlRunResult collectSource(CrawlerProperties.Source source) {
        try {
            List<ArticleCandidate> candidates = crawlerClient.fetchCandidates(source);
            int saved = 0;
            int skipped = 0;
            int failed = 0;
            for (ArticleCandidate candidate : candidates) {
                try {
                    if (persistenceService.alreadyExists(candidate.sourceUrl())) {
                        skipped++;
                        continue;
                    }
                    CrawledArticle article = crawlerClient.fetchArticle(source, candidate);
                    if (persistenceService.saveIfNew(article)) {
                        saved++;
                    } else {
                        skipped++;
                    }
                } catch (RuntimeException exception) {
                    failed++;
                    log.error(
                            "Article crawl failed: source={}, url={}",
                            source.key(),
                            candidate.sourceUrl(),
                            exception
                    );
                } finally {
                    pauseBetweenRequests();
                }
            }
            return new CrawlRunResult(candidates.size(), saved, skipped, failed);
        } catch (RuntimeException exception) {
            log.error("Source crawl failed: source={}, listUrl={}", source.key(), source.listUrl(), exception);
            return new CrawlRunResult(0, 0, 0, 1);
        }
    }

    private void pauseBetweenRequests() {
        if (properties.requestDelay() <= 0) {
            return;
        }
        try {
            Thread.sleep(properties.requestDelay());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Crawler request delay interrupted", exception);
        }
    }
}
