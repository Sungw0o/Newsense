package com.newsense.backend.admin.dto;

public record AdminStatsResponse(
        long totalUsers,
        long totalPosts,
        long totalArticles
) {
}
