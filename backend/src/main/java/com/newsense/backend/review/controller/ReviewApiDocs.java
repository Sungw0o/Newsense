package com.newsense.backend.review.controller;

import com.newsense.backend.auth.security.UserPrincipal;
import com.newsense.backend.common.response.ApiResponse;
import com.newsense.backend.review.dto.ReviewCreateRequest;
import com.newsense.backend.review.dto.ReviewResponse;
import com.newsense.backend.review.dto.ReviewUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Review", description = "사용자 학습 리뷰 작성 및 관리")
@SecurityRequirement(name = "BearerAuth")
public interface ReviewApiDocs {

    @Operation(summary = "리뷰 등록")
    @PostMapping("/api/v1/reviews")
    ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ReviewCreateRequest request
    );

    @Operation(summary = "기사별 내 리뷰 조회")
    @GetMapping("/api/v1/articles/{articleId}/review")
    ResponseEntity<ApiResponse<ReviewResponse>> getMyReview(
            @PathVariable Long articleId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    );

    @Operation(summary = "리뷰 수정")
    @PutMapping("/api/v1/reviews/{reviewId}")
    ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ReviewUpdateRequest request
    );

    @Operation(summary = "리뷰 삭제", description = "리뷰를 소프트 딜리트합니다.")
    @DeleteMapping("/api/v1/reviews/{reviewId}")
    ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    );
}
