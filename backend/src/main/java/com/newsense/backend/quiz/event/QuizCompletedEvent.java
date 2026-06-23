package com.newsense.backend.quiz.event;

import java.time.LocalDateTime;

public record QuizCompletedEvent(
        Long userId,
        Long articleId,
        Long quizId,
        boolean correct,
        LocalDateTime completedAt
) {
}
