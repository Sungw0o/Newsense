package com.newsense.backend.bookmark.domain;

import com.newsense.backend.article.domain.ArticleMeta;
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
        name = "bookmark",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_bookmark_user_article",
                columnNames = {"user_id", "article_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id", nullable = false)
    private ArticleMeta article;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static Bookmark create(User user, ArticleMeta article) {
        Bookmark bookmark = new Bookmark();
        bookmark.user = user;
        bookmark.article = article;
        bookmark.createdAt = LocalDateTime.now();
        return bookmark;
    }
}
