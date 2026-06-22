package com.newsense.backend.article.crawler.service;

import com.newsense.backend.article.crawler.config.CrawlerProperties;
import com.newsense.backend.article.crawler.model.CrawledArticle;
import com.newsense.backend.article.crawler.util.ContentCleaner;
import com.newsense.backend.article.crawler.util.ContentHasher;
import com.newsense.backend.article.crawler.util.SentenceChunker;
import com.newsense.backend.article.document.ArticleContent;
import com.newsense.backend.article.domain.ArticleMeta;
import com.newsense.backend.article.repository.ArticleContentRepository;
import com.newsense.backend.article.repository.ArticleMetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticlePersistenceService {

    private static final int SUMMARY_MAX_LENGTH = 500;

    private final ArticleMetaRepository articleMetaRepository;
    private final ArticleContentRepository articleContentRepository;
    private final ContentCleaner contentCleaner;
    private final SentenceChunker sentenceChunker;
    private final ContentHasher contentHasher;
    private final CrawlerProperties properties;

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
            ArticleMeta meta = ArticleMeta.create(
                    article.title(),
                    summarize(cleanText),
                    article.source(),
                    article.sourceUrl(),
                    article.publishedAt(),
                    savedContent.getId(),
                    contentHash
            );
            articleMetaRepository.save(meta);
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
}
