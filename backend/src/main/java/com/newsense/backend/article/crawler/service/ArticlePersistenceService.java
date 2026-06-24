package com.newsense.backend.article.crawler.service;

import com.newsense.backend.ai.article.ArticleClassificationResult;
import com.newsense.backend.ai.article.OpenAiArticleClassifierClient;
import com.newsense.backend.article.crawler.config.CrawlerProperties;
import com.newsense.backend.article.crawler.model.CrawledArticle;
import com.newsense.backend.article.crawler.util.ContentCleaner;
import com.newsense.backend.article.crawler.util.ContentHasher;
import com.newsense.backend.article.crawler.util.SentenceChunker;
import com.newsense.backend.article.document.ArticleContent;
import com.newsense.backend.article.domain.ArticleCategory;
import com.newsense.backend.article.domain.ArticleDifficulty;
import com.newsense.backend.article.domain.ArticleMeta;
import com.newsense.backend.article.event.ArticleStoredEvent;
import com.newsense.backend.article.repository.ArticleContentRepository;
import com.newsense.backend.article.repository.ArticleMetaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticlePersistenceService {

    private static final int SUMMARY_MAX_LENGTH = 500;
    private static final int AI_SUMMARY_MAX_LENGTH = 1000;
    private static final int CHARACTERS_PER_MINUTE = 500;

    private final ArticleMetaRepository articleMetaRepository;
    private final ArticleContentRepository articleContentRepository;
    private final ContentCleaner contentCleaner;
    private final SentenceChunker sentenceChunker;
    private final ContentHasher contentHasher;
    private final CrawlerProperties properties;
    private final ApplicationEventPublisher eventPublisher;
    private final OpenAiArticleClassifierClient articleClassifierClient;

    @Transactional
    public boolean saveIfNew(CrawledArticle article) {
        if (articleMetaRepository.existsBySourceUrl(article.sourceUrl())
                || articleContentRepository.existsBySourceUrl(article.sourceUrl())) {
            return false;
        }

        String cleanText = contentCleaner.clean(article.rawText());
        if (cleanText.isBlank()) {
            throw new IllegalStateException("Clean article body is empty: " + article.sourceUrl());
        }
        String contentHash = contentHasher.sha256(cleanText);
        if (articleMetaRepository.existsByContentHash(contentHash)
                || articleContentRepository.existsByContentHash(contentHash)) {
            return false;
        }
        List<String> chunks = sentenceChunker.chunk(cleanText, properties.chunkSize());
        ArticleContent content = ArticleContent.create(
                article.sourceKey(),
                article.source(),
                article.sourceUrl(),
                article.title(),
                article.publishedAt(),
                article.rawText(),
                cleanText,
                chunks,
                contentHash
        );

        ArticleContent savedContent;
        try {
            savedContent = articleContentRepository.save(content);
        } catch (DuplicateKeyException exception) {
            return false;
        }

        try {
            ArticleClassificationResult classification = classify(article.title(), cleanText);
            ArticleMeta meta = ArticleMeta.create(
                    article.title(),
                    normalizeSummary(classification.summary()),
                    article.source(),
                    article.sourceUrl(),
                    classification.category(),
                    classification.difficulty(),
                    article.publishedAt(),
                    estimateMinutes(cleanText),
                    savedContent.getId(),
                    contentHash
            );
            ArticleMeta savedMeta = articleMetaRepository.save(meta);
            eventPublisher.publishEvent(new ArticleStoredEvent(savedMeta.getId()));
            return true;
        } catch (RuntimeException exception) {
            articleContentRepository.deleteById(savedContent.getId());
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public boolean alreadyExists(String sourceUrl) {
        return articleMetaRepository.existsBySourceUrl(sourceUrl)
                || articleContentRepository.existsBySourceUrl(sourceUrl);
    }

    private String summarize(String cleanText) {
        if (cleanText.length() <= SUMMARY_MAX_LENGTH) {
            return cleanText;
        }
        return cleanText.substring(0, SUMMARY_MAX_LENGTH).trim();
    }

    private String normalizeSummary(String summary) {
        if (summary.length() <= AI_SUMMARY_MAX_LENGTH) {
            return summary;
        }
        return summary.substring(0, AI_SUMMARY_MAX_LENGTH).trim();
    }

    private ArticleClassificationResult classify(String title, String cleanText) {
        try {
            return articleClassifierClient.classify(title, cleanText);
        } catch (RuntimeException exception) {
            log.warn("AI article classification failed. Falling back to default metadata: {}",
                    exception.getMessage());
            return new ArticleClassificationResult(
                    ArticleCategory.MACRO_ECONOMY,
                    ArticleDifficulty.BASIC,
                    summarize(cleanText)
            );
        }
    }

    private int estimateMinutes(String cleanText) {
        return Math.max(1, (int) Math.ceil((double) cleanText.length() / CHARACTERS_PER_MINUTE));
    }
}
