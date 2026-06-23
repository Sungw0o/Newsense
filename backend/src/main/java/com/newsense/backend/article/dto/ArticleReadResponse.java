package com.newsense.backend.article.dto;

import java.time.LocalDateTime;

public record ArticleReadResponse(Long articleId, boolean isRead, LocalDateTime readAt) {
}
