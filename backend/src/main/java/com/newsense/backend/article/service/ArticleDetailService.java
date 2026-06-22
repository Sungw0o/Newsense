package com.newsense.backend.article.service;

import com.newsense.backend.article.document.ArticleContent;
import com.newsense.backend.article.domain.ArticleMeta;
import com.newsense.backend.article.domain.ArticleRead;
import com.newsense.backend.article.dto.ArticleDetailResponse;
import com.newsense.backend.article.dto.ArticleReadResponse;
import com.newsense.backend.article.dto.ArticleTermResponse;
import com.newsense.backend.article.dto.BookmarkToggleResponse;
import com.newsense.backend.article.event.ArticleReadCompletedEvent;
import com.newsense.backend.article.repository.ArticleContentRepository;
import com.newsense.backend.article.repository.ArticleMetaRepository;
import com.newsense.backend.article.repository.ArticleReadRepository;
import com.newsense.backend.bookmark.domain.Bookmark;
import com.newsense.backend.bookmark.repository.BookmarkRepository;
import com.newsense.backend.common.exception.CustomException;
import com.newsense.backend.common.exception.ErrorCode;
import com.newsense.backend.term.domain.ArticleTerm;
import com.newsense.backend.term.repository.ArticleTermRepository;
import com.newsense.backend.term.repository.TermRepository;
import com.newsense.backend.user.domain.User;
import com.newsense.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleDetailService {

    private final ArticleMetaRepository articleMetaRepository;
    private final ArticleContentRepository articleContentRepository;
    private final ArticleTermRepository articleTermRepository;
    private final TermRepository termRepository;
    private final ArticleReadRepository articleReadRepository;
    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public ArticleDetailResponse getArticleDetail(Long articleId, Long userId) {
        ArticleMeta article = getArticle(articleId);
        ArticleContent content = articleContentRepository.findById(article.getMongoDocumentId())
                .orElseThrow(() -> new CustomException(ErrorCode.ARTICLE_CONTENT_NOT_FOUND));

        boolean isRead = userId != null && articleReadRepository.existsByUserIdAndArticleId(userId, articleId);
        boolean isBookmarked = userId != null && bookmarkRepository.existsByUserIdAndArticleId(userId, articleId);

        return ArticleDetailResponse.of(article, content, isRead, isBookmarked);
    }

    @Transactional
    public List<ArticleTermResponse> getArticleTerms(Long articleId) {
        ArticleMeta article = getArticle(articleId);
        List<ArticleTerm> articleTerms = articleTermRepository.findAllByArticleIdWithTerm(articleId);

        if (articleTerms.isEmpty()) {
            ArticleContent content = articleContentRepository.findById(article.getMongoDocumentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.ARTICLE_CONTENT_NOT_FOUND));
            String articleText = content.getCleanText();

            articleTerms = termRepository.findAll().stream()
                    .filter(term -> articleText.contains(term.getName()))
                    .map(term -> ArticleTerm.create(article, term))
                    .toList();
            articleTermRepository.saveAll(articleTerms);
        }

        return articleTerms.stream()
                .sorted((left, right) -> left.getTerm().getName().compareTo(right.getTerm().getName()))
                .map(articleTerm -> ArticleTermResponse.from(articleTerm.getTerm()))
                .toList();
    }

    @Transactional
    public ArticleReadResponse markAsRead(Long articleId, Long userId) {
        ArticleMeta article = getArticle(articleId);
        User user = getUser(userId);

        ArticleRead existingRead = articleReadRepository.findByUserIdAndArticleId(userId, articleId)
                .orElse(null);
        if (existingRead != null) {
            return new ArticleReadResponse(articleId, true, existingRead.getReadAt());
        }

        ArticleRead articleRead = articleReadRepository.save(ArticleRead.create(user, article));
        eventPublisher.publishEvent(new ArticleReadCompletedEvent(userId, articleId, articleRead.getReadAt()));
        return new ArticleReadResponse(articleId, true, articleRead.getReadAt());
    }

    @Transactional
    public BookmarkToggleResponse toggleBookmark(Long articleId, Long userId) {
        ArticleMeta article = getArticle(articleId);
        User user = getUser(userId);

        return bookmarkRepository.findByUserIdAndArticleId(userId, articleId)
                .map(bookmark -> {
                    bookmarkRepository.delete(bookmark);
                    return new BookmarkToggleResponse(articleId, false);
                })
                .orElseGet(() -> {
                    bookmarkRepository.save(Bookmark.create(user, article));
                    return new BookmarkToggleResponse(articleId, true);
                });
    }

    private ArticleMeta getArticle(Long articleId) {
        return articleMetaRepository.findById(articleId)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTICLE_NOT_FOUND));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));
    }
}
