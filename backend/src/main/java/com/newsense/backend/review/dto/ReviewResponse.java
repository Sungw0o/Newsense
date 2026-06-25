package com.newsense.backend.review.dto;

import com.newsense.backend.review.domain.Review;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewResponse(
        Long id,
        Long articleId,
        String summary,
        String learned,
        List<String> difficultTerms,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getArticle().getId(),
                review.getSummary(),
                review.getLearned(),
                List.copyOf(review.getDifficultTerms()),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
