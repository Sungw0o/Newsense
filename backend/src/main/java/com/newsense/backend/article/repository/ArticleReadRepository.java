package com.newsense.backend.article.repository;

import com.newsense.backend.article.domain.ArticleRead;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArticleReadRepository extends JpaRepository<ArticleRead, Long> {

    boolean existsByUserIdAndArticleId(Long userId, Long articleId);

    Optional<ArticleRead> findByUserIdAndArticleId(Long userId, Long articleId);

    @EntityGraph(attributePaths = "article")
    List<ArticleRead> findAllByUserId(Long userId);
}
