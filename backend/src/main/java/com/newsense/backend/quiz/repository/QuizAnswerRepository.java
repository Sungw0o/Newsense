package com.newsense.backend.quiz.repository;

import com.newsense.backend.quiz.domain.QuizAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Long> {
}
