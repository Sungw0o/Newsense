package com.newsense.backend.quiz.domain;

import com.newsense.backend.article.domain.ArticleMeta;
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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(
        name = "quiz",
        indexes = @Index(name = "idx_quiz_article_active", columnList = "article_id,is_active")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id", nullable = false)
    private ArticleMeta article;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuizType type;

    @Column(nullable = false, length = 1000)
    private String question;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "quiz_option", joinColumns = @JoinColumn(name = "quiz_id"))
    @OrderColumn(name = "option_order")
    @Column(name = "option_text", nullable = false, length = 500)
    private List<String> options = new ArrayList<>();

    @Column(name = "correct_answer", nullable = false, length = 500)
    private String correctAnswer;

    @Column(nullable = false, length = 2000)
    private String explanation;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    public static Quiz create(
            ArticleMeta article,
            QuizType type,
            String question,
            List<String> options,
            String correctAnswer,
            String explanation,
            int displayOrder
    ) {
        Quiz quiz = new Quiz();
        quiz.article = article;
        quiz.type = type;
        quiz.question = question;
        quiz.options = new ArrayList<>(options);
        quiz.correctAnswer = correctAnswer;
        quiz.explanation = explanation;
        quiz.displayOrder = displayOrder;
        quiz.isActive = true;
        quiz.generatedAt = LocalDateTime.now();
        return quiz;
    }

    public void deactivate() {
        this.isActive = false;
    }
}
