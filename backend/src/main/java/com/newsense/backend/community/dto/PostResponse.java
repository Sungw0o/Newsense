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
        Long scrapSummaryId,
        int likes,
        int dislikes,
        long viewCount,
        LocalDateTime createdAt
) {
    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getUser().getId(),
                post.getUser().getNickname(),
                post.getArticle() == null ? null : post.getArticle().getId(),
                post.getScrapSummaryId(),
                post.getLikes(),
                post.getDislikes(),
                post.getViewCount(),
                post.getCreatedAt()
        );
    }
}
