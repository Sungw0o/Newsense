package com.newsense.backend.wrongnote.domain;

import com.newsense.backend.article.domain.ArticleCategory;
import com.newsense.backend.quiz.domain.Quiz;
import com.newsense.backend.user.domain.User;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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
        name = "wrong_note",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_wrong_note_user_quiz",
                columnNames = {"user_id", "quiz_id"}
        ),
        indexes = @Index(name = "idx_wrong_note_user_active_resolved", columnList = "user_id,is_active,is_resolved")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WrongNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(name = "article_id", nullable = false)
    private Long articleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ArticleCategory category;

    @Column(nullable = false, length = 1000)
    private String question;

    @Column(name = "user_answer", nullable = false, length = 500)
    private String userAnswer;

    @Column(name = "correct_answer", nullable = false, length = 500)
    private String correctAnswer;

    @Column(nullable = false, length = 2000)
    private String explanation;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "wrong_note_term", joinColumns = @JoinColumn(name = "wrong_note_id"))
    @OrderColumn(name = "term_order")
    @Column(name = "term_name", nullable = false, length = 100)
    private List<String> relatedTerms = new ArrayList<>();

    @Column(name = "mistake_count", nullable = false)
    private int mistakeCount;

    @Column(name = "is_resolved", nullable = false)
    private boolean isResolved;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "first_wrong_at", nullable = false, updatable = false)
    private LocalDateTime firstWrongAt;

    @Column(name = "last_wrong_at", nullable = false)
    private LocalDateTime lastWrongAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static WrongNote create(
            User user,
            Quiz quiz,
            String userAnswer,
            List<String> relatedTerms
    ) {
        WrongNote note = new WrongNote();
        note.user = user;
        note.quiz = quiz;
        note.articleId = quiz.getArticle().getId();
        note.category = quiz.getArticle().getCategory();
        note.question = quiz.getQuestion();
        note.userAnswer = userAnswer;
        note.correctAnswer = quiz.getCorrectAnswer();
        note.explanation = quiz.getExplanation();
        note.relatedTerms = new ArrayList<>(relatedTerms);
        note.mistakeCount = 1;
        note.isResolved = false;
        note.isActive = true;
        note.firstWrongAt = LocalDateTime.now();
        note.lastWrongAt = note.firstWrongAt;
        return note;
    }

    public void recordAgain(String userAnswer, List<String> relatedTerms) {
        this.userAnswer = userAnswer;
        this.relatedTerms = new ArrayList<>(relatedTerms);
        this.mistakeCount++;
        this.isResolved = false;
        this.isActive = true;
        this.lastWrongAt = LocalDateTime.now();
        this.resolvedAt = null;
        this.deletedAt = null;
    }

    public void toggleResolved() {
        this.isResolved = !this.isResolved;
        this.resolvedAt = this.isResolved ? LocalDateTime.now() : null;
    }

    public void deactivate() {
        this.isActive = false;
        this.deletedAt = LocalDateTime.now();
    }
}
