package com.newsense.backend.review.service;

import com.newsense.backend.article.domain.ArticleMeta;
import com.newsense.backend.article.repository.ArticleMetaRepository;
import com.newsense.backend.common.exception.CustomException;
import com.newsense.backend.common.exception.ErrorCode;
import com.newsense.backend.review.domain.Review;
import com.newsense.backend.review.dto.ReviewCreateRequest;
import com.newsense.backend.review.dto.ReviewResponse;
import com.newsense.backend.review.dto.ReviewUpdateRequest;
import com.newsense.backend.review.event.ReviewCompletedEvent;
import com.newsense.backend.review.repository.ReviewRepository;
import com.newsense.backend.user.domain.User;
import com.newsense.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ArticleMetaRepository articleMetaRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ReviewResponse createReview(Long userId, ReviewCreateRequest request) {
        ArticleMeta article = getArticle(request.articleId());
        User user = getUser(userId);
        List<String> difficultTerms = normalizeTerms(request.difficultTerms());

        Review review = reviewRepository.findByArticleIdAndUserId(request.articleId(), userId)
                .map(existing -> restoreReview(existing, request, difficultTerms))
                .orElseGet(() -> Review.create(
                        user,
                        article,
                        request.summary().trim(),
                        request.learned().trim(),
                        difficultTerms
                ));

        Review savedReview = reviewRepository.save(review);
        eventPublisher.publishEvent(new ReviewCompletedEvent(
                userId,
                article.getId(),
                savedReview.getId(),
                LocalDateTime.now()
        ));
        return ReviewResponse.from(savedReview);
    }

    @Transactional(readOnly = true)
    public ReviewResponse getMyReview(Long articleId, Long userId) {
        Review review = reviewRepository.findByArticleIdAndUserIdAndIsActiveTrue(articleId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));
        return ReviewResponse.from(review);
    }

    @Transactional
    public ReviewResponse updateReview(Long reviewId, Long userId, ReviewUpdateRequest request) {
        Review review = getOwnedReview(reviewId, userId);
        review.update(
                request.summary().trim(),
                request.learned().trim(),
                normalizeTerms(request.difficultTerms())
        );
        return ReviewResponse.from(review);
    }

    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        Review review = getOwnedReview(reviewId, userId);
        review.deactivate();
    }

    private Review restoreReview(
            Review review,
            ReviewCreateRequest request,
            List<String> difficultTerms
    ) {
        if (review.isActive()) {
            throw new CustomException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }
        review.restore(request.summary().trim(), request.learned().trim(), difficultTerms);
        return review;
    }

    private Review getOwnedReview(Long reviewId, Long userId) {
        return reviewRepository.findByIdAndUserIdAndIsActiveTrue(reviewId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.REVIEW_NOT_FOUND));
    }

    private ArticleMeta getArticle(Long articleId) {
        return articleMetaRepository.findById(articleId)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTICLE_NOT_FOUND));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));
    }

    private List<String> normalizeTerms(List<String> terms) {
        return terms.stream()
                .map(String::trim)
                .filter(term -> !term.isBlank())
                .distinct()
                .toList();
    }
}
