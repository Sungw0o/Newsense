package com.newsense.backend.review.controller;

import com.newsense.backend.auth.security.UserPrincipal;
import com.newsense.backend.common.response.ApiResponse;
import com.newsense.backend.review.dto.ReviewCreateRequest;
import com.newsense.backend.review.dto.ReviewResponse;
import com.newsense.backend.review.dto.ReviewUpdateRequest;
import com.newsense.backend.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReviewController implements ReviewApiDocs {

    private final ReviewService reviewService;

    @Override
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            UserPrincipal userPrincipal,
            ReviewCreateRequest request
    ) {
        ReviewResponse response = reviewService.createReview(userPrincipal.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "리뷰가 등록되었습니다.", response));
    }

    @Override
    public ResponseEntity<ApiResponse<ReviewResponse>> getMyReview(
            Long articleId,
            UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "리뷰 조회에 성공했습니다.",
                reviewService.getMyReview(articleId, userPrincipal.id())
        ));
    }

    @Override
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            Long reviewId,
            UserPrincipal userPrincipal,
            ReviewUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "리뷰가 수정되었습니다.",
                reviewService.updateReview(reviewId, userPrincipal.id(), request)
        ));
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> deleteReview(Long reviewId, UserPrincipal userPrincipal) {
        reviewService.deleteReview(reviewId, userPrincipal.id());
        return ResponseEntity.ok(ApiResponse.success("리뷰가 삭제되었습니다.", null));
    }
}
