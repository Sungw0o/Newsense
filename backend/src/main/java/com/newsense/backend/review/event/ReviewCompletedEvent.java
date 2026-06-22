package com.newsense.backend.review.event;

import java.time.LocalDateTime;

public record ReviewCompletedEvent(Long userId, Long articleId, Long reviewId, LocalDateTime completedAt) {
}
