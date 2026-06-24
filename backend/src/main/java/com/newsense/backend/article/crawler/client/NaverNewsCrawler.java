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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverNewsCrawler {

    private static final String SOURCE_KEY = "naver-news";
    private static final String SOURCE_NAME = "네이버 뉴스";
    private static final String ECONOMY_RANKING_URL = "https://news.naver.com/main/ranking/popularDay.naver?mid=etc&sid1=001";
    private static final String ECONOMY_SECTION_URL = "https://news.naver.com/section/101";

    private final CrawlerProperties properties;
    private final HtmlArticleExtractor htmlArticleExtractor;
    private final ArticlePersistenceService persistenceService;

    public CrawlRunResult collect() {
        List<ArticleCandidate> candidates = new ArrayList<>();
        candidates.addAll(fetchCandidates(ECONOMY_SECTION_URL));
        candidates.addAll(fetchCandidates(ECONOMY_RANKING_URL));
        return saveCandidates(candidates.stream().distinct().limit(properties.maxItemsPerSource()).toList());
    }

    private List<ArticleCandidate> fetchCandidates(String listUrl) {
        Document document = htmlArticleExtractor.fetchDocument(listUrl);
        return document.select("a[href*=/article/]").stream()
                .map(link -> new ArticleCandidate(link.text().trim(), link.absUrl("href"), LocalDate.now()))
                .filter(candidate -> !candidate.title().isBlank() && !candidate.sourceUrl().isBlank())
                .toList();
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
                log.warn("Naver news crawl failed: url={}", candidate.sourceUrl(), exception);
            }
        }
        return new CrawlRunResult(candidates.size(), saved, skipped, failed);
    }
}
