package com.newsense.backend.article.crawler.client;

import com.newsense.backend.article.crawler.config.CrawlerProperties;
import com.newsense.backend.article.crawler.model.ArticleCandidate;
import com.newsense.backend.article.crawler.model.CrawledArticle;
import com.newsense.backend.article.crawler.util.PdfOcrExtractor;
import com.newsense.backend.article.crawler.util.PdfTextExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PublicNewsCrawlerClient {

    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("yyyy.MM.dd"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.BASIC_ISO_DATE
    );

    private final CrawlerProperties properties;
    private final PdfTextExtractor pdfTextExtractor;
    private final PdfOcrExtractor pdfOcrExtractor;
    private final CharsetAwareDocumentFetcher documentFetcher;

    public List<ArticleCandidate> fetchCandidates(CrawlerProperties.Source source) {
        Document document = fetchDocument(source.listUrl());
        List<ArticleCandidate> candidates = new ArrayList<>();
        for (Element item : document.select(source.itemSelector())) {
            Element link = item.selectFirst(source.linkSelector());
            if (link == null || link.text().isBlank()) {
                continue;
            }
            String sourceUrl = canonicalUrl(link.absUrl("href"), source.baseUrl());
            if (sourceUrl.isBlank()) {
                continue;
            }
            Element dateElement = item.selectFirst(source.dateSelector());
            LocalDate publishedAt = dateElement == null ? null : parseDate(dateElement.text());
            candidates.add(new ArticleCandidate(link.text().trim(), sourceUrl, publishedAt));
            if (candidates.size() >= properties.maxItemsPerSource()) {
                break;
            }
        }
        if (candidates.isEmpty()) {
            log.warn("No crawl candidates found: source={}, selector={}", source.key(), source.itemSelector());
        }
        return List.copyOf(candidates);
    }

    public CrawledArticle fetchArticle(CrawlerProperties.Source source, ArticleCandidate candidate) {
        Document document = fetchDocument(candidate.sourceUrl());
        String pdfUrl = findPdfUrl(document);
        if (!pdfUrl.isBlank()) {
            String primaryText = pdfTextExtractor.extractText(pdfUrl);
            String fullText = pdfOcrExtractor.extractText(pdfUrl, primaryText);
            if (!fullText.isBlank()) {
                return new CrawledArticle(
                        source.key(),
                        source.name(),
                        candidate.title(),
                        candidate.sourceUrl(),
                        candidate.publishedAt(),
                        fullText
                );
            }
        }
        Element body = document.selectFirst(source.bodySelector());
        if (body == null) {
            throw new IllegalStateException("Article body selector did not match: " + source.bodySelector());
        }
        String rawText = source.bodyAttribute() == null || source.bodyAttribute().isBlank()
                ? body.html()
                : body.attr(source.bodyAttribute());
        return new CrawledArticle(
                source.key(),
                source.name(),
                candidate.title(),
                candidate.sourceUrl(),
                candidate.publishedAt(),
                rawText
        );
    }

    private String findPdfUrl(Document document) {
        Element pdfLink = document.selectFirst("a[href$=.pdf], a[href*=.pdf?], a[href*=atchFileId], a[href*=download]");
        return pdfLink == null ? "" : pdfLink.absUrl("href");
    }

    private Document fetchDocument(String url) {
        RuntimeException lastException = null;
        for (int attempt = 1; attempt <= properties.maxRetries(); attempt++) {
            try {
                return documentFetcher.fetch(url, properties.connectionTimeout(), false);
            } catch (IOException exception) {
                lastException = new IllegalStateException("Failed to fetch " + url, exception);
                log.warn("Crawler request failed: attempt={}/{}, url={}", attempt, properties.maxRetries(), url);
                sleepBeforeRetry(attempt);
            }
        }
        throw lastException == null ? new IllegalStateException("Failed to fetch " + url) : lastException;
    }

    private void sleepBeforeRetry(int attempt) {
        if (attempt >= properties.maxRetries()) {
            return;
        }
        try {
            Thread.sleep(properties.retryDelay() * attempt);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Crawler retry interrupted", exception);
        }
    }

    private LocalDate parseDate(String value) {
        String normalized = value == null ? "" : value.replaceAll("[^0-9.-]", "").trim();
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(normalized, formatter);
            } catch (DateTimeParseException ignored) {
                // Try the next supported source format.
            }
        }
        return null;
    }

    private String canonicalUrl(String candidate, String baseUrl) {
        if (candidate == null || candidate.isBlank()) {
            return "";
        }
        URI uri = URI.create(candidate.startsWith("http") ? candidate : URI.create(baseUrl).resolve(candidate).toString());
        return URI.create(uri.getScheme() + "://" + uri.getAuthority() + uri.getPath()
                + (uri.getQuery() == null ? "" : "?" + uri.getQuery())).normalize().toString();
    }
}
