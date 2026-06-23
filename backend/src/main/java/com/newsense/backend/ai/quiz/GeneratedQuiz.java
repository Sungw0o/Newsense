package com.newsense.backend.ai.quiz;

import com.newsense.backend.quiz.domain.QuizType;

import java.util.List;

public record GeneratedQuiz(
        QuizType type,
        String question,
        List<String> options,
        String correctAnswer,
        String explanation
) {
}
