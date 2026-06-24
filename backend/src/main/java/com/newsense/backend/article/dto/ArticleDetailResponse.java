package com.newsense.backend.article.dto;

import com.newsense.backend.article.document.ArticleContent;
import com.newsense.backend.article.domain.ArticleMeta;

import java.time.LocalDate;
import java.util.List;

public record ArticleDetailResponse(
        Long id,
        String title,
        String summary,
        String content,
        String category,
        String difficulty,
        String source,
        String sourceUrl,
        LocalDate publishedAt,
        int estimatedMinutes,
        boolean isRead,
        boolean isBookmarked,
        List<RelatedStockResponse> relatedStocks
) {
    public static ArticleDetailResponse of(
            ArticleMeta article,
            ArticleContent content,
            boolean isRead,
            boolean isBookmarked,
            List<RelatedStockResponse> relatedStocks
    ) {
        return of(article, content.getCleanText(), isRead, isBookmarked, relatedStocks);
    }

    public static ArticleDetailResponse of(
            ArticleMeta article,
            String content,
            boolean isRead,
            boolean isBookmarked,
            List<RelatedStockResponse> relatedStocks
    ) {
        return new ArticleDetailResponse(
                article.getId(),
                article.getTitle(),
                article.getSummary(),
                content,
                article.getCategory().getDisplayName(),
                article.getDifficulty().getDisplayName(),
                article.getSource(),
                article.getSourceUrl(),
                article.getPublishedAt(),
                article.getEstimatedMinutes(),
                isRead,
                isBookmarked,
                relatedStocks
        );
    }
}
