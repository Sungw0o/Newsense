package com.newsense.backend.community.dto;

import com.newsense.backend.community.domain.Post;

import java.time.LocalDateTime;

public record PostResponse(
        Long id,
        String title,
        String content,
        Long userId,
        String username,
        Long articleMetaId,
        ArticleScrapResponse articleScrap,
        Long scrapSummaryId,
        int likes,
        int dislikes,
        long viewCount,
        long commentCount,
        LocalDateTime createdAt
) {
    public static PostResponse from(Post post, long commentCount) {
        ArticleScrapResponse articleScrap = post.getArticle() == null
                ? null
                : ArticleScrapResponse.from(post.getArticle());
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getUser().getId(),
                post.getUser().getNickname(),
                post.getArticle() == null ? null : post.getArticle().getId(),
                articleScrap,
                post.getScrapSummaryId(),
                post.getLikes(),
                post.getDislikes(),
                post.getViewCount(),
                commentCount,
                post.getCreatedAt()
        );
    }

    public record ArticleScrapResponse(
            Long articleId,
            String title,
            String summary,
            String category,
            String difficulty,
            String source,
            LocalDateTime createdAt
    ) {
        private static ArticleScrapResponse from(com.newsense.backend.article.domain.ArticleMeta article) {
            return new ArticleScrapResponse(
                    article.getId(),
                    article.getTitle(),
                    article.getSummary(),
                    article.getCategory().getDisplayName(),
                    article.getDifficulty().getDisplayName(),
                    article.getSource(),
                    article.getCollectedAt()
            );
        }
    }
}
