package com.newsense.backend.quiz.dto;

import com.newsense.backend.quiz.domain.Quiz;
import com.newsense.backend.quiz.domain.QuizAnswer;

import java.time.LocalDateTime;

public record QuizAnswerResponse(
        Long answerId,
        Long quizId,
        Long articleId,
        String userAnswer,
        String correctAnswer,
        boolean correct,
        String explanation,
        boolean wrongNoteRecorded,
        LocalDateTime submittedAt
) {
    public static QuizAnswerResponse of(QuizAnswer answer, Quiz quiz, boolean wrongNoteRecorded) {
        return new QuizAnswerResponse(
                answer.getId(),
                quiz.getId(),
                quiz.getArticle().getId(),
                answer.getUserAnswer(),
                quiz.getCorrectAnswer(),
                answer.isCorrect(),
                quiz.getExplanation(),
                wrongNoteRecorded,
                answer.getSubmittedAt()
        );
    }
}
