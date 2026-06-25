package com.newsense.backend.article.crawler.model;

import java.time.LocalDate;

public record CrawledArticle(
        String sourceKey,
        String source,
        String title,
        String sourceUrl,
        LocalDate publishedAt,
        String rawText
) {
}
