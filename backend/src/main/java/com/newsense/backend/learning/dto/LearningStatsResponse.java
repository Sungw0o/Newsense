package com.newsense.backend.learning.dto;

public record LearningStatsResponse(
        long totalReadArticleCount,
        long totalReviewCount,
        long totalQuizCount,
        long correctQuizCount,
        double quizAccuracyRate,
        int consecutiveLearningDays,
        long weeklyLearningDays
) {
}
