package com.newsense.backend.ai.quiz;

import com.newsense.backend.quiz.domain.QuizType;
import com.newsense.backend.quiz.domain.QuizPurpose;

import java.util.List;

public record GeneratedQuiz(
        QuizType type,
        QuizPurpose purpose,
        String question,
        List<String> options,
        String correctAnswer,
        String explanation
) {
}
