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
import com.newsense.backend.community.repository.PostRepository;
import com.newsense.backend.user.domain.User;
import com.newsense.backend.user.domain.UserRole;
import com.newsense.backend.user.dto.UserProfileResponse;
import com.newsense.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostReportRepository postReportRepository;
    private final ArticleMetaRepository articleMetaRepository;
    private final ArticleContentRepository articleContentRepository;
    private final OpenAiArticleClassifierClient articleClassifierClient;
    private final PublicNewsCrawlerService publicNewsCrawlerService;
    private final PortalNewsCrawlerService portalNewsCrawlerService;

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
    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        postRepository.delete(post);
    }

    @Transactional(readOnly = true)
    public Page<PostReportResponse> getReports(Pageable pageable) {
        return postReportRepository.findAllWithDetails(pageable).map(PostReportResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<AdminArticleResponse> getArticles(Pageable pageable) {
        return articleMetaRepository.findAll(pageable)
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

    public CrawlResultResponse triggerCrawl() {
        CrawlRunResult publ