package com.newsense.backend.article.crawler.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.newsense.backend.article.crawler.config.CrawlerProperties;
import com.newsense.backend.article.crawler.model.ArticleCandidate;
import com.newsense.backend.article.crawler.model.CrawlRunResult;
import com.newsense.backend.article.crawler.model.CrawledArticle;
import com.newsense.backend.article.crawler.service.ArticlePersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewsApiCollector {

    private static final String SOURCE_KEY = "newsapi";
    private static final String SOURCE_NAME = "NewsAPI";
    private static final String API_URL = "https://newsapi.org/v2/top-headlines?country=kr&category=business&pageSize=";

    private final CrawlerProperties properties;
    private final HtmlArticleExtractor htmlArticleExtractor;
    private final ArticlePersistenceService persistenceService;

    public CrawlRunResult collect() {
        if (properties.newsApiKey().isBlank()) {
            return CrawlRunResult.empty();
        }
        List<ArticleCandidate> candidates = fetchCandidates();
        return saveCandidates(candidates);
    }

    private List<ArticleCandidate> fetchCandidates() {
        JsonNode response = RestClient.create().get()
                .uri(API_URL + properties.maxItemsPerSource())
                .header("X-Api-Key", properties.newsApiKey())
                .header(HttpHeaders.USER_AGENT, "NewsenseCrawler/1.0")
                .retrieve()
                .body(JsonNode.class);
        List<ArticleCandidate> candidates = new ArrayList<>();
        if (response == null) {
            return candidates;
        }
        for (JsonNode article : response.path("articles")) {
            String title = article.path("title").asText();
            String url = article.path("url").asText();
            if (!title.isBlank() && !url.isBlank()) {
                candidates.add(new ArticleCandidate(title, url, LocalDate.now()));
            }
        }
        return candidates;
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
                log.warn("NewsAPI article crawl failed: url={}", candidate.sourceUrl(), exception);
            }
        }
        return new CrawlRunResult(candidates.size(), saved, skipped, failed);
    }
}
