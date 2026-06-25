package com.newsense.backend.wrongnote.service;

import com.newsense.backend.quiz.domain.Quiz;

import java.util.List;

public interface WrongNoteRecorder {

    void record(Long userId, Quiz quiz, String userAnswer, List<String> relatedTerms);
}
