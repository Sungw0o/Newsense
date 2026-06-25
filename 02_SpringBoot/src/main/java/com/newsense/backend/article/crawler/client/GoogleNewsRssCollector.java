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
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleNewsRssCollector {

    private static final String SOURCE_KEY = "google-news-rss";
    private static final String SOURCE_NAME = "Google News RSS";

    // 경제/금융 키워드로 검색하되 스포츠 관련 키워드 명시적 제외
    private static final String RSS_URL =
            "https://news.google.com/rss/search?q=%EA%B2%BD%EC%A0%9C+OR+%EA%B8%88%EC%9C%B5+OR+%ED%88%AC%EC%9E%90" +
            "+-%EC%B6%95%EA%B5%AC+-%EC%95%BC%EA%B5%AC+-%EC%8A%A4%ED%8F%AC%EC%B8%A0+-K%EB%A6%AC%EA%B7%B8+-EPL" +
            "&hl=ko&gl=KR&ceid=KR:ko";

    // 제목에 포함되면 비경제 기사로 판단해 걸러낼 키워드
    private static final Set<String> NON_ECONOMIC_TITLE_KEYWORDS = Set.of(
            "축구", "야구", "농구", "배구", "골프", "테니스", "스포츠",
            "K리그", "EPL", "프리미어리그", "분데스리가", "챔피언스리그",
            "월드컵", "올림픽", "패럴림픽", "국가대표", "선수단",
            "감독", "코치", "이적", "드래프트", "FA컵",
            "연예", "드라마", "영화", "아이돌", "콘서트", "공연",
            "맛집", "여행", "날씨", "의료", "건강", "다이어트"
    );

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
                    .filter(candidate -> !isNonEconomicTitle(candidate.title()))
                    .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to fetch Google News RSS", exception);
        }
    }

    private boolean isNonEconomicTitle(String title) {
        String lower = title.toLowerCase();
        for (String keyword : NON_ECONOMIC_TITLE_KEYWORDS) {
            if (lower.contains(keyword.toLowerCase())) {
                log.debug("Non-economic article filtered by title keyword \'{}\': {}", keyword, title);
                return true;
            }
        }
        return false;
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
