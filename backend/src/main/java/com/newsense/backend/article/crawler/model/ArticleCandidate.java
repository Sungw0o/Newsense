package com.newsense.backend.article.crawler.model;

import java.time.LocalDate;

public record ArticleCandidate(
        String title,
        String sourceUrl,
        LocalDate publishedAt
) {
}
