package com.newsense.backend.article.repository;

import com.newsense.backend.article.domain.ArticleMeta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleMetaRepository extends JpaRepository<ArticleMeta, Long> {

    boolean existsBySourceUrl(String sourceUrl);

    boolean existsByContentHash(String contentHash);
}
