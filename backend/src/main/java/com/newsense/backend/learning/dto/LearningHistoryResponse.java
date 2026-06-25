package com.newsense.backend.learning.dto;

import java.time.LocalDate;
import java.util.List;

public record LearningHistoryResponse(
        LocalDate startDate,
        LocalDate endDate,
        long totalArticleReadCount,
        long totalReviewCount,
        long totalQuizCount,
        List<LearningDailyHistoryResponse> days
) {
    public static LearningHistoryResponse of(
            LocalDate startDate,
            LocalDate endDate,
            List<LearningDailyHistoryResponse> days
    ) {
        return new LearningHistoryResponse(
                startDate,
                endDate,
                days.stream().mapToLong(LearningDailyHistoryResponse::articleReadCount).sum(),
                days.stream().mapToLong(LearningDailyHistoryResponse::reviewCount).sum(),
                days.stream().mapToLong(LearningDailyHistoryResponse::quizCount).sum(),
                days
        );
    }
}
