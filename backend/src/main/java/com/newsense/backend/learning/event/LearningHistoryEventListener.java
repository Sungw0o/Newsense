package com.newsense.backend.learning.event;

import com.newsense.backend.article.event.ArticleReadCompletedEvent;
import com.newsense.backend.learning.service.LearningHistoryService;
import com.newsense.backend.quiz.event.QuizCompletedEvent;
import com.newsense.backend.review.event.ReviewCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class LearningHistoryEventListener {

    private final LearningHistoryService learningHistoryService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleArticleReadCompleted(ArticleReadCompletedEvent event) {
        learningHistoryService.recordArticleRead(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReviewCompleted(ReviewCompletedEvent event) {
        learningHistoryService.recordReview(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleQuizCompleted(QuizCompletedEvent event) {
        learningHistoryService.recordQuiz(event);
    }
}
