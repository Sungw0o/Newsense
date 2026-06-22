package com.newsense.backend.quiz.dto;

import com.newsense.backend.quiz.domain.Quiz;

import java.util.List;

public record QuizResponse(Long id, String type, String question, List<String> options) {

    public static QuizResponse from(Quiz quiz) {
        return new QuizResponse(
                quiz.getId(),
                quiz.getType().name(),
                quiz.getQuestion(),
                List.copyOf(quiz.getOptions())
        );
    }
}
