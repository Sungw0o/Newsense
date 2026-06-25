package com.newsense.backend.community.domain;

import com.newsense.backend.article.domain.ArticleMeta;
import com.newsense.backend.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "post")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_meta_id")
    private ArticleMeta article;

    @Column(name = "scrap_summary_id")
    private Long scrapSummaryId;

    @Column(nullable = false)
    private int likes;

    @Column(nullable = false)
    private int dislikes;

    @Column(name = "view_count", nullable = false)
    private long viewCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostType type;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static Post create(
            String title,
            String content,
            User user,
            ArticleMeta article,
            Long scrapSummaryId,
            PostType type
    ) {
        Post post = new Post();
        post.title = title;
        post.content = content;
        post.user = user;
        post.article = article;
        post.scrapSummaryId = scrapSummaryId;
        post.type = type == null ? PostType.GENERAL : type;
        post.likes = 0;
        post.dislikes = 0;
        post.viewCount = 0;
        post.createdAt = LocalDateTime.now();
        return post;
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void addLike() {
        this.likes++;
    }

    public void removeLike() {
        this.likes = Math.max(0, this.likes - 1);
    }

    public void addDislike() {
        this.dislikes++;
    }

    public void removeDislike() {
        this.dislikes = Math.max(0, this.dislikes - 1);
    }
}
