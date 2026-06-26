package com.newsense.backend.admin.service;

import com.newsense.backend.admin.dto.AdminArticleResponse;
import com.newsense.backend.admin.dto.AdminStatsResponse;
import com.newsense.backend.admin.dto.CrawlResultResponse;
import com.newsense.backend.admin.dto.PostReportResponse;
import com.newsense.backend.ai.article.ArticleClassificationResult;
import com.newsense.backend.ai.article.OpenAiArticleClassifierClient;
import com.newsense.backend.article.crawler.model.CrawlRunResult;
import com.newsense.backend.article.crawler.service.PortalNewsCrawlerService;
import com.newsense.backend.article.crawler.service.PublicNewsCrawlerService;
import com.newsense.backend.article.document.ArticleContent;
import com.newsense.backend.article.repository.ArticleMetaRepository;
import com.newsense.backend.article.repository.ArticleContentRepository;
import com.newsense.backend.article.domain.ArticleMeta;
import com.newsense.backend.common.exception.CustomException;
import com.newsense.backend.common.exception.ErrorCode;
import com.newsense.backend.community.domain.Post;
import com.newsense.backend.community.repository.PostReportRepository;
import com.newsense.backend.community.repository.PostCommentRepository;
import com.newsense.backend.community.repository.PostReactionRepository;
import com.newsense.backend.community.repository.PostRepository;
import com.newsense.backend.inquiry.dto.InquiryResponse;
import com.newsense.backend.inquiry.repository.InquiryRepository;
import com.newsense.backend.inquiry.service.InquiryService;
import com.newsense.backend.user.domain.User;
import com.newsense.backend.user.domain.UserRole;
import com.newsense.backend.user.dto.UserProfileResponse;
import com.newsense.backend.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PostCommentRepository postCommentRepository;
    private final PostReactionRepository postReactionRepository;
    private final PostRepository postRepository;
    private final PostReportRepository postReportRepository;
    private final ArticleMetaRepository articleMetaRepository;
    private final ArticleContentRepository articleContentRepository;
    private final OpenAiArticleClassifierClient articleClassifierClient;
    private final PublicNewsCrawlerService publicNewsCrawlerService;
    private final PortalNewsCrawlerService portalNewsCrawlerService;
    private final InquiryService inquiryService;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public AdminStatsResponse getStats() {
        return new AdminStatsResponse(
                userRepository.count(),
                postRepository.count(),
                articleMetaRepository.count()
        );
    }

    @Transactional(readOnly = true)
    public Page<UserProfileResponse> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserProfileResponse::from);
    }

    @Transactional
    public UserProfileResponse changeUserRole(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));
        user.changeRole(user.getRole() == UserRole.ADMIN ? UserRole.USER : UserRole.ADMIN);
        return UserProfileResponse.from(user);
    }

    @Transactional
    public UserProfileResponse deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));
        user.deactivate();
        return UserProfileResponse.from(user);
    }

    @Transactional
    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        postReportRepository.deleteAllByPostId(postId);
        postReactionRepository.deleteAllByPostId(postId);
        postCommentRepository.deleteAllByPostId(postId);
        postRepository.delete(post);
    }

    @Transactional(readOnly = true)
    public Page<PostReportResponse> getReports(Pageable pageable) {
        return postReportRepository.findAllWithDetails(pageable).map(PostReportResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<AdminArticleResponse> getArticles(Pageable pageable) {
        Pageable sorted = org.springframework.data.domain.PageRequest.of(
                pageable.getPageNumber(), pageable.getPageSize(),
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "viewCount"));
        return articleMetaRepository.findAll(sorted)
                .map(article -> AdminArticleResponse.of(article, getContentLength(article)));
    }

    @Transactional
    public AdminArticleResponse refreshArticleSummary(Long articleId) {
        ArticleMeta article = articleMetaRepository.findById(articleId)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTICLE_NOT_FOUND));
        ArticleContent content = articleContentRepository.findById(article.getMongoDocumentId())
                .or(() -> articleContentRepository.findBySourceUrl(article.getSourceUrl()))
                .orElseThrow(() -> new CustomException(ErrorCode.ARTICLE_NOT_FOUND));
        String articleText = content.getCleanText() == null || content.getCleanText().isBlank()
                ? content.getRawText()
                : content.getCleanText();
        ArticleClassificationResult result = articleClassifierClient.classify(article.getTitle(), articleText);
        article.updateSummary(result.summary());
        return AdminArticleResponse.of(article, articleText == null ? 0 : articleText.length());
    }

    @Transactional
    public void deleteArticle(Long articleId) {
        ArticleMeta article = articleMetaRepository.findById(articleId)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTICLE_NOT_FOUND));
        String mongoDocumentId = article.getMongoDocumentId();
        deleteArticleRelations(articleId);
        articleMetaRepository.delete(article);
        if (mongoDocumentId != null && !mongoDocumentId.isBlank()) {
            articleContentRepository.deleteById(mongoDocumentId);
        }
    }

    public CrawlResultResponse triggerCrawl(int maxPerSource) {
        CrawlRunResult publicResult = publicNewsCrawlerService.collectAll(maxPerSource);
        CrawlRunResult portalResult = portalNewsCrawlerService.collectAll();
        int totalDiscovered = publicResult.discovered() + portalResult.discovered();
        int totalSaved = publicResult.saved() + portalResult.saved();
        int totalSkipped = publicResult.skipped() + portalResult.skipped();
        int totalFailed = publicResult.failed() + portalResult.failed();
        return new CrawlResultResponse(totalDiscovered, totalSaved, totalSkipped, totalFailed);
    }

    @Transactional(readOnly = true)
    public Page<InquiryResponse> getInquiries(Pageable pageable) {
        return inquiryService.getAllInquiries(pageable);
    }

    @Transactional
    public InquiryResponse resolveInquiry(Long inquiryId) {
        return inquiryService.resolve(inquiryId);
    }

    private int getContentLength(ArticleMeta article) {
        return articleContentRepository.findById(article.getMongoDocumentId())
                .or(() -> articleContentRepository.findBySourceUrl(article.getSourceUrl()))
                .map(c -> {
                    if (c.getCleanText() != null && !c.getCleanText().isBlank()) {
                        return c.getCleanText().length();
                    }
                    return c.getRawText() != null ? c.getRawText().length() : 0;
                })
                .orElse(0);
    }

    private void deleteArticleRelations(Long articleId) {
        entityManager.createNativeQuery("DELETE FROM wrong_note_term WHERE wrong_note_id IN (SELECT id FROM wrong_note WHERE article_id = :articleId)")
                .setParameter("articleId", articleId)
                .executeUpdate();
        entityManager.createNativeQuery("DELETE FROM wrong_note WHERE article_id = :articleId")
                .setParameter("articleId", articleId)
                .executeUpdate();
        entityManager.createNativeQuery("DELETE FROM quiz_answer WHERE article_id = :articleId")
                .setParameter("articleId", articleId)
                .executeUpdate();
        entityManager.createNativeQuery("DELETE FROM quiz_option WHERE quiz_id IN (SELECT id FROM quiz WHERE article_id = :articleId)")
                .setParameter("articleId", articleId)
                .executeUpdate();
        entityManager.createNativeQuery("DELETE FROM quiz WHERE article_id = :articleId")
                .setParameter("articleId", articleId)
                .executeUpdate();
        entityManager.createNativeQuery("DELETE FROM review_difficult_term WHERE review_id IN (SELECT id FROM review WHERE article_id = :articleId)")
                .setParameter("articleId", articleId)
                .executeUpdate();
        entityManager.createNativeQuery("DELETE FROM review WHERE article_id = :articleId")
                .setParameter("articleId", articleId)
                .executeUpdate();
        entityManager.createNativeQuery("DELETE FROM learning_history WHERE article_id = :articleId")
                .setParameter("articleId", articleId)
                .executeUpdate();
        entityManager.createNativeQuery("DELETE FROM article_read WHERE article_id = :articleId")
                .setParameter("articleId", articleId)
                .executeUpdate();
        entityManager.createNativeQuery("DELETE FROM bookmark WHERE article_id = :articleId")
                .setParameter("articleId", articleId)
                .executeUpdate();
        entityManager.createNativeQuery("DELETE FROM article_related_stock WHERE article_meta_id = :articleId")
                .setParameter("articleId", articleId)
                .executeUpdate();
        entityManager.createNativeQuery("DELETE FROM article_term WHERE article_id = :articleId")
                .setParameter("articleId", articleId)
                .executeUpdate();
        entityManager.createNativeQuery("UPDATE post SET article_meta_id = NULL WHERE article_meta_id = :articleId")
                .setParameter("articleId", articleId)
                .executeUpdate();
    }
}
