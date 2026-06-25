package com.newsense.backend.quiz.repository;

import com.newsense.backend.quiz.domain.QuizAnswer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Long> {

    @EntityGraph(attributePaths = {"article", "quiz"})
    List<QuizAnswer> findAllByUserId(Long userId);
}
