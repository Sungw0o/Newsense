package com.newsense.backend.article.repository;

import com.newsense.backend.article.domain.ArticleRelatedStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticleRelatedStockRepository extends JpaRepository<ArticleRelatedStock, Long> {

    List<ArticleRelatedStock> findAllByArticleMetaId(Long articleMetaId);
}
