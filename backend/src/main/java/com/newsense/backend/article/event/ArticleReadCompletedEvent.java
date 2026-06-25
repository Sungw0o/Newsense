package com.newsense.backend.article.event;

import java.time.LocalDateTime;

public record ArticleReadCompletedEvent(Long userId, Long articleId, LocalDateTime readAt) {
}
