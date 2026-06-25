package com.newsense.backend.article.repository;

import com.newsense.backend.article.domain.ArticleMeta;
import com.newsense.backend.article.domain.ArticleCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ArticleMetaRepository extends JpaRepository<ArticleMeta, Long>, JpaSpecificationExecutor<ArticleMeta> {

    boolean existsBySourceUrl(String sourceUrl);

    boolean existsByContentHash(String contentHash);

    Optional<ArticleMeta> findByMongoDocumentId(String mongoDocumentId);

    List<ArticleMeta> findAllByMongoDocumentIdIn(List<String> mongoDocumentIds);

    @Query("""
            select article.category as category, count(article) as articleCount
            from ArticleMeta article
            group by article.category
            """)
    List<CategoryCount> countArticlesByCategory();

    interface CategoryCount {
        ArticleCategory getCategory();

        long getArticleCount();
    }
}
