package com.newsense.backend.quiz.domain;

import com.newsense.backend.article.domain.ArticleMeta;
import com.newsense.backend.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "quiz_answer",
        indexes = @Index(name = "idx_quiz_answer_user_id", columnList = "user_id")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id", nullable = false)
    private ArticleMeta article;

    @Column(name = "user_answer", nullable = false, length = 500)
    private String userAnswer;

    @Column(name = "correct_answer", nullable = false, length = 500)
    private String correctAnswer;

    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    public static QuizAnswer create(User user, Quiz quiz, String userAnswer, boolean correct) {
        QuizAnswer quizAnswer = new QuizAnswer();
        quizAnswer.user = user;
        quizAnswer.quiz = quiz;
        quizAnswer.article = quiz.getArticle();
        quizAnswer.userAnswer = userAnswer;
        quizAnswer.correctAnswer = quiz.getCorrectAnswer();
        quizAnswer.isCorrect = correct;
        quizAnswer.submittedAt = LocalDateTime.now();
        return quizAnswer;
    }
}
