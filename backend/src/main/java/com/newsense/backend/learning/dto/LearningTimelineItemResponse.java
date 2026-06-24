package com.newsense.backend.learning.dto;

import com.newsense.backend.learning.domain.LearningHistory;
import com.newsense.backend.review.domain.Review;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
        String reviewSummary,
        String reviewLearned,
        List<String> reviewDifficultTerms,
        LocalDateTime learnedAt
) {
    public static LearningTimelineItemResponse from(LearningHistory history) {
        return from(history, null);
    }

    public static LearningTimelineItemResponse from(LearningHistory history, Review review) {
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
                review == null ? null : review.getSummary(),
                review == null ? null : review.getLearned(),
                review == null ? List.of() : review.getDifficultTerms(),
                history.getLearnedAt()
        );
    }
}
