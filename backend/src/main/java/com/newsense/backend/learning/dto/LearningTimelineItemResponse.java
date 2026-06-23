package com.newsense.backend.learning.dto;

import com.newsense.backend.learning.domain.LearningHistory;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record LearningTimelineItemResponse(
        Long historyId,
        String type,
        String typeName,
        Long referenceId,
        Long articleId,
        String articleTitle,
        String articleCategory,
        String articleDifficulty,
        LocalDate articlePublishedAt,
        Boolean quizCorrect,
        LocalDateTime learnedAt
) {
    public static LearningTimelineItemResponse from(LearningHistory history) {
        return new LearningTimelineItemResponse(
                history.getId(),
                history.getType().name(),
                history.getType().getDisplayName(),
                history.getReferenceId(),
                history.getArticle().getId(),
                history.getArticle().getTitle(),
                history.getArticle().getCategory().getDisplayName(),
                history.getArticle().getDifficulty().getDisplayName(),
                history.getArticle().getPublishedAt(),
                history.getQuizCorrect(),
                history.getLearnedAt()
        );
    }
}
