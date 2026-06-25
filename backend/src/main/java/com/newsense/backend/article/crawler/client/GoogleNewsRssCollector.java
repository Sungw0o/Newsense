package com.newsense.backend.article.crawler.client;

import com.newsense.backend.article.crawler.config.CrawlerProperties;
import com.newsense.backend.article.crawler.model.ArticleCandidate;
import com.newsense.backend.article.crawler.model.CrawlRunResult;
import com.newsense.backend.article.crawler.model.CrawledArticle;
import com.newsense.backend.article.crawler.service.ArticlePersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleNewsRssCollector {

    private static final String SOURCE_KEY = "google-news-rss";
    private static final String SOURCE_NAME = "Google News RSS";
    private static final String RSS_URL = "https://news.google.com/rss/search?q=%EA%B2%BD%EC%A0%9C+OR+%EA%B8%88%EC%9C%B5+OR+%ED%88%AC%EC%9E%90&hl=ko&gl=KR&ceid=KR:ko";

    private final CrawlerProperties properties;
    private final HtmlArticleExtractor htmlArticleExtractor;
    private final ArticlePersistenceService persistenceService;
    private final CharsetAwareDocumentFetcher documentFetcher;

    public CrawlRunResult collect() {
        List<ArticleCandidate> candidates = fetchCandidates();
        return saveCandidates(candidates);
    }

    private List<ArticleCandidate> fetchCandidates() {
        try {
            Document document = documentFetcher.fetch(RSS_URL, properties.connectionTimeout(), true);
            return document.select("item").stream()
                    .limit(properties.maxItemsPerSource())
                    .map(item -> new ArticleCandidate(
                            text(item, "title"),
                            text(item, "link"),
                            LocalDate.now()
                    ))
                    .filter(candidate -> !candidate.title().isBlank() && !candidate.sourceUrl().isBlank())
                    .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to fetch Google News RSS", exception);
        }
    }

    private CrawlRunResult saveCandidates(List<ArticleCandidate> candidates) {
        int saved = 0;
        int skipped = 0;
        int failed = 0;
        for (ArticleCandidate candidate : candidates) {
            try {
                if (persistenceService.alreadyExists(candidate.sourceUrl())) {
                    skipped++;
                    continue;
                }
                Document document = htmlArticleExtractor.fetchDocument(candidate.sourceUrl());
                String title = htmlArticleExtractor.extractTitle(document);
                String body = htmlArticleExtractor.extractBody(document);
                if (title.isBlank() || body.isBlank()) {
                    skipped++;
                    continue;
                }
                if (persistenceService.saveIfNew(new CrawledArticle(
                        SOURCE_KEY,
                        SOURCE_NAME,
                        title,
                        candidate.sourceUrl(),
                        candidate.publishedAt(),
                        body
                ))) {
                    saved++;
                } else {
                    skipped++;
                }
            } catch (RuntimeException exception) {
                failed++;
                log.warn("Google News RSS article crawl failed: url={}", candidate.sourceUrl(), exception);
            }
        }
        return new CrawlRunResult(candidates.size(), saved, skipped, failed);
    }

    private String text(Element element, String selector) {
        Element selected = element.selectFirst(selector);
        return selected == null ? "" : selected.text().trim();
    }
}
