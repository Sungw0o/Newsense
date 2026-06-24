package com.newsense.backend.article.crawler;

import com.newsense.backend.ai.article.ArticleImportanceEvaluation;
import com.newsense.backend.ai.article.OpenAiArticleClassifierClient;
import com.newsense.backend.ai.article.OpenAiArticleEvaluatorClient;
import com.newsense.backend.article.crawler.config.CrawlerProperties;
import com.newsense.backend.article.crawler.model.CrawledArticle;
import com.newsense.backend.article.crawler.service.ArticlePersistenceService;
import com.newsense.backend.article.crawler.util.ContentCleaner;
import com.newsense.backend.article.crawler.util.ContentHasher;
import com.newsense.backend.article.crawler.util.SentenceChunker;
import com.newsense.backend.article.domain.ArticleMeta;
import com.newsense.backend.article.repository.ArticleContentRepository;
import com.newsense.backend.article.repository.ArticleMetaRepository;
import com.newsense.backend.article.service.ArticleRetentionService;
import com.newsense.backend.quiz.repository.QuizRepository;
import com.newsense.backend.support.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@DisplayName("Article crawler policy unit tests")
class ArticleCrawlerPolicyTest {

    @Test
    void saveIfNew_skipsArticleWhenImportanceScoreIsBelowThreshold() {
        ArticleMetaRepository articleMetaRepository = mock(ArticleMetaRepository.class);
        ArticleContentRepository articleContentRepository = mock(ArticleContentRepository.class);
        OpenAiArticleEvaluatorClient evaluatorClient = mock(OpenAiArticleEvaluatorClient.class);
        ArticlePersistenceService service = createPersistenceService(
                articleMetaRepository,
                articleContentRepository,
                evaluatorClient,
                properties(70, 30, 180)
        );

        given(articleMetaRepository.countByCollectedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(0L);
        given(evaluatorClient.evaluate("단순 안내", "본문"))
                .willReturn(new ArticleImportanceEvaluation(false, 20, "공지성 게시글"));

        boolean saved = service.saveIfNew(crawledArticle("단순 안내"));

        assertThat(saved).isFalse();
        verify(articleContentRepository, never()).save(any());
    }

    @Test
    void saveIfNew_skipsArticleWhenDailyLimitIsReached() {
        ArticleMetaRepository articleMetaRepository = mock(ArticleMetaRepository.class);
        ArticleContentRepository articleContentRepository = mock(ArticleContentRepository.class);
        OpenAiArticleEvaluatorClient evaluatorClient = mock(OpenAiArticleEvaluatorClient.class);
        ArticlePersistenceService service = createPersistenceService(
                articleMetaRepository,
                articleContentRepository,
                evaluatorClient,
                properties(70, 1, 180)
        );

        given(articleMetaRepository.countByCollectedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(1L);

        boolean saved = service.saveIfNew(crawledArticle("중요 기사"));

        assertThat(saved).isFalse();
        verify(evaluatorClient, never()).evaluate(any(), any());
        verify(articleContentRepository, never()).save(any());
    }

    @Test
    void deleteExpiredInactiveArticles_removesMysqlMongoAndDetachedQuizzes() {
        ArticleMeta article = TestFixtures.article(1L);
        ArticleMetaRepository articleMetaRepository = mock(ArticleMetaRepository.class);
        ArticleContentRepository articleContentRepository = mock(ArticleContentRepository.class);
        QuizRepository quizRepository = mock(QuizRepository.class);
        ArticleRetentionService service = new ArticleRetentionService(
                properties(70, 30, 180),
                articleMetaRepository,
                articleContentRepository,
                quizRepository
        );

        given(articleMetaRepository.findRetentionCandidates(any(LocalDateTime.class))).willReturn(List.of(article));

        int deleted = service.deleteExpiredInactiveArticles();

        assertThat(deleted).isEqualTo(1);
        verify(quizRepository).deleteByArticleId(1L);
        verify(articleMetaRepository).delete(article);
        verify(articleContentRepository).deleteById("mongo-1");
    }

    private ArticlePersistenceService createPersistenceService(
            ArticleMetaRepository articleMetaRepository,
            ArticleContentRepository articleContentRepository,
            OpenAiArticleEvaluatorClient evaluatorClient,
            CrawlerProperties properties
    ) {
        ContentCleaner contentCleaner = mock(ContentCleaner.class);
        ContentHasher contentHasher = mock(ContentHasher.class);
        SentenceChunker sentenceChunker = mock(SentenceChunker.class);
        given(contentCleaner.clean(any())).willReturn("본문");
        given(contentHasher.sha256(any())).willReturn("hash");

        return new ArticlePersistenceService(
                articleMetaRepository,
                articleContentRepository,
                contentCleaner,
                sentenceChunker,
                contentHasher,
                properties,
                mock(ApplicationEventPublisher.class),
                mock(OpenAiArticleClassifierClient.class),
                evaluatorClient
        );
    }

    private CrawledArticle crawledArticle(String title) {
        return new CrawledArticle(
                "source",
                "출처",
                title,
                "https://example.com/" + title,
                LocalDate.of(2026, 6, 24),
                "본문"
        );
    }

    private CrawlerProperties properties(int importanceThreshold, int dailyLimit, int retentionDays) {
        return new CrawlerProperties(
                true,
                false,
                0,
                0,
                0,
                1000,
                1,
                1,
                0,
                0,
                800,
                200,
                importanceThreshold,
                dailyLimit,
                retentionDays,
                "",
                "",
                "",
                "",
                null,
                List.of()
        );
    }
}
