package com.newsense.backend.article.service;

import com.newsense.backend.article.domain.ArticleMeta;
import com.newsense.backend.article.repository.ArticleContentRepository;
import com.newsense.backend.article.repository.ArticleMetaRepository;
import com.newsense.backend.article.crawler.config.CrawlerProperties;
import com.newsense.backend.quiz.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleRetentionService {

    private final CrawlerProperties properties;
    private final ArticleMetaRepository articleMetaRepository;
    private final ArticleContentRepository articleContentRepository;
    private final QuizRepository quizRepository;

    @Transactional
    public int deleteExpiredInactiveArticles() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(properties.retentionDays());
        List<ArticleMeta> candidates = articleMetaRepository.findRetentionCandidates(cutoff);
        int deleted = 0;
        for (ArticleMeta article : candidates) {
            quizRepository.deleteByArticleId(article.getId());
            articleMetaRepository.delete(article);
            articleContentRepository.deleteById(article.getMongoDocumentId());
            deleted++;
        }
        log.info(
                "Article retention cleanup completed: cutoff={}, retentionDays={}, deleted={}",
                cutoff,
                properties.retentionDays(),
                deleted
        );
        return deleted;
    }
}
