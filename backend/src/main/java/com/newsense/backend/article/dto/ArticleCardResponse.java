package com.newsense.backend.article.dto;

import com.newsense.backend.article.domain.ArticleMeta;

import java.time.LocalDate;

public record ArticleCardResponse(
        Long articleId,
        String title,
        String summary,
        String category,
        String difficulty,
        String source,
        int estimatedMinutes,
        LocalDate publishedAt,
        long viewCount
) {
    public static ArticleCardResponse from(ArticleMeta article) {
        return new ArticleCardResponse(
                article.getId(),
                article.getTitle(),
                article.getSummary(),
                article.getCategory().getDisplayName(),
                article.getDifficulty().getDisplayName(),
                article.getSource(),
                article.getEstimatedMinutes(),
                article.getPublishedAt(),
                article.getViewCount()
        );
    }
}
