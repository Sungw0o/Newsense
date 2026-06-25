package com.newsense.backend.learning.dto;

import java.time.LocalDate;
import java.util.List;

public record LearningDailyHistoryResponse(
        LocalDate date,
        long articleReadCount,
        long reviewCount,
        long quizCount,
        List<LearningTimelineItemResponse> timeline
) {
    public static LearningDailyHistoryResponse of(LocalDate date, List<LearningTimelineItemResponse> timeline) {
        return new LearningDailyHistoryResponse(
                date,
                countByType(timeline, "ARTICLE_READ"),
                countByType(timeline, "REVIEW"),
                countByType(timeline, "QUIZ"),
                timeline
        );
    }

    private static long countByType(List<LearningTimelineItemResponse> timeline, String type) {
        return timeline.stream()
                .filter(item -> item.type().equals(type))
                .count();
    }
}
