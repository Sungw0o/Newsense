package com.newsense.backend.quiz.repository;

import com.newsense.backend.quiz.domain.Quiz;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    @EntityGraph(attributePaths = "options")
    List<Quiz> findByArticleIdAndIsActiveTrueOrderByDisplayOrder(Long articleId);
}
