package com.newsense.backend.community.dto;

import com.newsense.backend.community.domain.PostComment;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        Long postId,
        Long userId,
        String username,
        String content,
        LocalDateTime createdAt
) {
    public static CommentResponse from(PostComment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getPost().getId(),
                comment.getUser().getId(),
                comment.getUser().getNickname(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}
