package com.newsense.backend.learning.event;

import com.newsense.backend.article.event.ArticleReadCompletedEvent;
import com.newsense.backend.learning.service.LearningHistoryService;
import com.newsense.backend.quiz.event.QuizCompletedEvent;
import com.newsense.backend.review.event.ReviewCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class LearningHistoryEventListener {

    private final LearningHistoryService learningHistoryService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleArticleReadCompleted(ArticleReadCompletedEvent event) {
        try {
            learningHistoryService.recordArticleRead(event);
        } catch (RuntimeException exception) {
            log.error(
                    "Failed to record article read learning history. userId={}, articleId={}, readAt={}",
                    event.userId(),
                    event.articleId(),
                    event.readAt(),
                    exception
            );
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReviewCompleted(ReviewCompletedEvent event) {
        try {
            learningHistoryService.recordReview(event);
        } catch (RuntimeException exception) {
            log.error(
                    "Failed to record review learning history. userId={}, articleId={}, reviewId={}, completedAt={}",
                    event.userId(),
                    event.articleId(),
                    event.reviewId(),
                    event.completedAt(),
                    exception
            );
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleQuizCompleted(QuizCompletedEvent event) {
        try {
            learningHistoryService.recordQuiz(event);
        } catch (RuntimeException exception) {
            log.error(
                    "Failed to record quiz learning history. userId={}, articleId={}, quizId={}, completedAt={}",
                    event.userId(),
                    event.articleId(),
                    event.quizId(),
                    event.completedAt(),
                    exception
            );
        }
    }
}
