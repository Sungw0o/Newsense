package com.newsense.backend.review.domain;

import com.newsense.backend.article.domain.ArticleMeta;
import com.newsense.backend.user.domain.User;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(
        name = "review",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_review_user_article",
                columnNames = {"user_id", "article_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id", nullable = false)
    private ArticleMeta article;

    @Column(nullable = false, length = 2000)
    private String summary;

    @Column(nullable = false, length = 2000)
    private String learned;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "review_difficult_term", joinColumns = @JoinColumn(name = "review_id"))
    @OrderColumn(name = "term_order")
    @Column(name = "term_name", nullable = false, length = 100)
    private List<String> difficultTerms = new ArrayList<>();

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static Review create(
            User user,
            ArticleMeta article,
            String summary,
            String learned,
            List<String> difficultTerms
    ) {
        Review review = new Review();
        review.user = user;
        review.article = article;
        review.summary = summary;
        review.learned = learned;
        review.difficultTerms = new ArrayList<>(difficultTerms);
        review.isActive = true;
        review.createdAt = LocalDateTime.now();
        review.updatedAt = review.createdAt;
        return review;
    }

    public void update(String summary, String learned, List<String> difficultTerms) {
        this.summary = summary;
        this.learned = learned;
        this.difficultTerms = new ArrayList<>(difficultTerms);
        this.updatedAt = LocalDateTime.now();
    }

    public void restore(String summary, String learned, List<String> difficultTerms) {
        update(summary, learned, difficultTerms);
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }
}
