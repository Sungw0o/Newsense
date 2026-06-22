package com.newsense.backend.article.domain;

import com.newsense.backend.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "article_read",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_article_read_user_article",
                columnNames = {"user_id", "article_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArticleRead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id", nullable = false)
    private ArticleMeta article;

    @Column(name = "read_at", nullable = false)
    private LocalDateTime readAt;

    public static ArticleRead create(User user, ArticleMeta article) {
        ArticleRead articleRead = new ArticleRead();
        articleRead.user = user;
        articleRead.article = article;
        articleRead.readAt = LocalDateTime.now();
        return articleRead;
    }
}
