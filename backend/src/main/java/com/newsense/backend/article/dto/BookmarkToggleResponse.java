package com.newsense.backend.article.dto;

public record BookmarkToggleResponse(Long articleId, boolean isBookmarked) {
}
