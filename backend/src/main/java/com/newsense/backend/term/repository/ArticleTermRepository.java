package com.newsense.backend.term.repository;

import com.newsense.backend.term.domain.ArticleTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ArticleTermRepository extends JpaRepository<ArticleTerm, Long> {

    @Query("select articleTerm from ArticleTerm articleTerm join fetch articleTerm.term where articleTerm.article.id = :articleId order by articleTerm.term.name")
    List<ArticleTerm> findAllByArticleIdWithTerm(@Param("articleId") Long articleId);
}
