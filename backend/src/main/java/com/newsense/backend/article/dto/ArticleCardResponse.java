package com.newsense.backend.article.dto;

import com.newsense.backend.article.domain.ArticleMeta;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ArticleCardResponse(
        Long articleId,
        String title,
        String summary,
        String category,
        String source,
        LocalDate publishedAt,
        LocalDateTime collectedAt,
        long viewCount
) {
    public static ArticleCardResponse from(ArticleMeta article) {
        return new ArticleCardResponse(
                article.getId(),
                article.getTitle(),
                article.getSummary(),
                article.getCategory().getDisplayName(),
                article.getSource(),
                article.getPublishedAt(),
                article.getCollectedAt(),
                article.getViewCount()
        );
    }
}
