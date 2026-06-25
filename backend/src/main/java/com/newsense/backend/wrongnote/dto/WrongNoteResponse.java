package com.newsense.backend.wrongnote.dto;

import com.newsense.backend.wrongnote.domain.WrongNote;

import java.time.LocalDate;
import java.util.List;

public record WrongNoteResponse(
        Long id,
        Long quizId,
        Long articleId,
        String category,
        LocalDate date,
        String question,
        String userAns,
        String correctAns,
        String explanation,
        boolean isResolved,
        int mistakeCount,
        List<String> relatedTerms
) {
    public static WrongNoteResponse from(WrongNote note) {
        return new WrongNoteResponse(
                note.getId(),
                note.getQuiz().getId(),
                note.getArticleId(),
                note.getCategory().getDisplayName(),
                note.getLastWrongAt().toLocalDate(),
                note.getQuestion(),
                note.getUserAnswer(),
                note.getCorrectAnswer(),
                note.getExplanation(),
                note.isResolved(),
                note.getMistakeCount(),
                List.copyOf(note.getRelatedTerms())
        );
    }
}
