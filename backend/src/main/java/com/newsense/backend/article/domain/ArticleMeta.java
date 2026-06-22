package com.newsense.backend.article.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "article_meta",
        uniqueConstraints = @UniqueConstraint(name = "uk_article_meta_source_url", columnNames = "source_url"),
        indexes = {
                @Index(name = "idx_article_meta_published_at", columnList = "published_at"),
                @Index(name = "idx_article_meta_category_difficulty", columnList = "category,difficulty")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArticleMeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, length = 1000)
    private String summary;

    @Column(nullable = false, length = 100)
    private String source;

    @Column(name = "source_url", nullable = false, length = 1000)
    private String sourceUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ArticleCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ArticleDifficulty difficulty;

    @Column(name = "published_at")
    private LocalDate publishedAt;

    @Column(name = "mongo_document_id", nullable = false, length = 100)
    private String mongoDocumentId;

    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;

    @Column(name = "collected_at", nullable = false)
    private LocalDateTime collectedAt;

    public static ArticleMeta create(
            String title,
            String summary,
            String source,
            String sourceUrl,
            LocalDate publishedAt,
            String mongoDocumentId,
            String contentHash
    ) {
        ArticleMeta article = new ArticleMeta();
        article.title = title;
        article.summary = summary;
        article.source = source;
        article.sourceUrl = sourceUrl;
        article.category = ArticleCategory.ECONOMY;
        article.difficulty = ArticleDifficulty.BASIC;
        article.publishedAt = publishedAt;
        article.mongoDocumentId = mongoDocumentId;
        article.contentHash = contentHash;
        article.collectedAt = LocalDateTime.now();
        return article;
    }
}
