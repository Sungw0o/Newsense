package com.newsense.backend.article.repository;

import com.newsense.backend.article.domain.ArticleMeta;
import com.newsense.backend.article.domain.ArticleCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ArticleMetaRepository extends JpaRepository<ArticleMeta, Long>, JpaSpecificationExecutor<ArticleMeta> {

    boolean existsBySourceUrl(String sourceUrl);

    boolean existsByContentHash(String contentHash);

    long countByCollectedAtBetween(LocalDateTime startInclusive, LocalDateTime endExclusive);

    @Query("""
            select article
            from ArticleMeta article
            where article.collectedAt < :cutoff
              and not exists (
                    select 1
                    from Bookmark bookmark
                    where bookmark.article = article
              )
              and not exists (
                    select 1
                    from ArticleRead articleRead
                    where articleRead.article = article
              )
              and not exists (
                    select 1
                    from LearningHistory history
                    where history.article = article
              )
              and not exists (
                    select 1
                    from QuizAnswer answer
                    where answer.article = article
              )
              and not exists (
                    select 1
                    from Review review
                    where review.article = article
              )
              and not exists (
                    select 1
                    from WrongNote note
                    where note.articleId = article.id
              )
            """)
    List<ArticleMeta> findRetentionCandidates(@Param("cutoff") LocalDateTime cutoff);

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
