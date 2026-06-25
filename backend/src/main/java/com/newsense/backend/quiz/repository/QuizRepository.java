package com.newsense.backend.quiz.repository;

import com.newsense.backend.article.domain.ArticleDifficulty;
import com.newsense.backend.quiz.domain.Quiz;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    @EntityGraph(attributePaths = "options")
    List<Quiz> findByArticleIdAndIsActiveTrueOrderByDisplayOrder(Long articleId);

    @EntityGraph(attributePaths = "options")
    List<Qu