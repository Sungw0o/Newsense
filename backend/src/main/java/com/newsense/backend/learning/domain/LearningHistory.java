package com.newsense.backend.learning.domain;

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
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
        name = "learning_history",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_learning_history_user_type_reference",
                columnNames = {"user_id", "type", "reference_id"}
        ),
        indexes = {
                @Index(name = "idx_learning_history_user_date", columnList = "user_id,learning_date"),
                @Index(name = "idx_learning_history_user_type", columnList = "user_id,type")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LearningHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id", nullable = false)
    private ArticleMeta article;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LearningHistoryType type;

    @Column(name = "reference_id", nullable = false)
    private Long referenceId;

    @Column(name = "quiz_correct")
    private Boolean quizCorrect;

    @Column(name = "learning_date", nullable = false)
    private LocalDate learningDate;

    @Column(name = "learned_at", nullable = false)
    private LocalDateTime learnedAt;

    public static LearningHistory articleRead(User user, ArticleMeta article, Long articleReadReferenceId, LocalDateTime readAt) {
        return create(user, article, LearningHistoryType.ARTICLE_READ, articleReadReferenceId, null, readAt);
    }

    public static LearningHistory review(User user, ArticleMeta article, Long reviewId, LocalDateTime completedAt) {
        return create(user, article, LearningHistoryType.REVIEW, reviewId, null, completedAt);
    }

    public static LearningHistory quiz(User user, ArticleMeta article, Long quizId, boolean correct, LocalDateTime completedAt) {
        return create(user, article, LearningHistoryType.QUIZ, quizId, correct, completedAt);
    }

    private static LearningHistory create(
            User user,
            ArticleMeta article,
            LearningHistoryType type,
            Long referenceId,
            Boolean quizCorrect,
            LocalDateTime learnedAt
    ) {
        LearningHistory history = new LearningHistory();
        history.user = user;
        history.article = article;
        history.type = type;
        history.referenceId = referenceId;
        history.quizCorrect = quizCorrect;
        history.learningDate = learnedAt.toLocalDate();
        history.learnedAt = learnedAt;
        return history;
    }
}
