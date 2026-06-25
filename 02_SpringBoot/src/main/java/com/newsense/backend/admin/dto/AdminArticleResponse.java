package com.newsense.backend.admin.dto;

import com.newsense.backend.article.domain.ArticleMeta;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AdminArticleResponse(
        Long articleId,
        String title,
        String source,
        String category,
        LocalDate publishedAt,
        LocalDateTime collectedAt,
        int contentLength,
        boolean hasAiSummary,
        String summary,
        long viewCount
) {
    public static AdminArticleResponse of(ArticleMeta article, int contentLength) {
        String summary = article.getSummary();
        boolean hasAiSummary = summary != null && !summary.isBlank();
        return new AdminArticleResponse(
                article.getId(),
                article.getTitle(),
                article.getSource(),
                article.getCategory().getDisplayName(),
                article.getPublishedAt(),
                article.getCollectedAt(),
                contentLength,
                hasAiSummary,
                summary,
                article.getViewCount()
        );
    }
}
