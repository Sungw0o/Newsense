package com.newsense.backend.article.service;

import com.newsense.backend.article.document.ArticleContent;
import com.newsense.backend.article.domain.ArticleMeta;
import com.newsense.backend.article.domain.ArticleRead;
import com.newsense.backend.article.dto.ArticleDetailResponse;
import com.newsense.backend.article.dto.ArticleReadResponse;
import com.newsense.backend.article.dto.ArticleTermResponse;
import com.newsense.backend.article.dto.BookmarkToggleResponse;
import com.newsense.backend.article.dto.RelatedStockResponse;
import com.newsense.backend.article.repository.ArticleRelatedStockRepository;
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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private final MongoTemplate mongoTemplate;
    private final ArticleRelatedStockRepository articleRelatedStockRepository;

    @Transactional(readOnly = true)
    public ArticleDetailResponse getArticleDetail(Long articleId, Long userId) {
        ArticleMeta article = getArticle(articleId);
        String content = getArticleContent(article)
                .map(ArticleContent::getCleanText)
                .orElse(article.getSummary());

        boolean isRead = userId != null && articleReadRepository.existsByUserIdAndArticleId(userId, articleId);
        boolean isBookmarked = userId != null && bookmarkRepository.existsByUserIdAndArticleId(userId, articleId);

        List<RelatedStockResponse> relatedStocks = articleRelatedStockRepository.findAllByArticleMetaId(articleId)
                .stream()
                .map(RelatedStockResponse::from)
                .collect(Collectors.toList());

        return ArticleDetailResponse.of(article, content, isRead, isBookmarked, relatedStocks);
    }

    @Transactional
    public List<ArticleTermResponse> getArticleTerms(Long articleId) {
        ArticleMeta article = getArticle(articleId);
        List<ArticleTerm> articleTerms = articleTermRepository.findAllByArticleIdWithTerm(articleId);

        if (articleTerms.isEmpty()) {
            String articleText = getArticleContent(article)
                    .map(ArticleContent::getCleanText)
                    .orElse(article.getSummary());

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

    private Optional<ArticleContent> getArticleContent(ArticleMeta article) {
        return articleContentRepository.findById(article.getMongoDocumentId())
                .or(() -> articleContentRepository.findBySourceUrl(article.getSourceUrl()))
                .or(() -> findArticleContentWithMongoTemplate(article));
    }

    private Optional<ArticleContent> findArticleContentWithMongoTemplate(ArticleMeta article) {
        ArticleContent byId = mongoTemplate.findById(article.getMongoDocumentId(), ArticleContent.class);
        if (byId != null) {
            return Optional.of(byId);
        }
        Query query = Query.query(Criteria.where("sourceUrl").is(article.getSourceUrl()));
        return Optional.ofNullable(mongoTemplate.findOne(query, ArticleContent.class));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));
    }
}
