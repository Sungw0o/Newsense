package com.newsense.backend.quiz.service;

import com.newsense.backend.article.event.ArticleStoredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuizGenerationListener {

    private final QuizService quizService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void generateAfterArticleStored(ArticleStoredEvent event) {
        try {
            quizService.generateIfAbsent(event.articleId());
        } catch (RuntimeException exception) {
            log.warn("Background quiz generation failed for article {}: {}",
                    event.articleId(), exception.getMessage());
        }
    }
}
