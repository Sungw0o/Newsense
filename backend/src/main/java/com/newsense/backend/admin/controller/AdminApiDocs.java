package com.newsense.backend.admin.controller;

import com.newsense.backend.admin.dto.AdminStatsResponse;
import com.newsense.backend.admin.dto.AdminArticleResponse;
import com.newsense.backend.admin.dto.PostReportResponse;
import com.newsense.backend.common.response.ApiResponse;
import com.newsense.backend.user.dto.UserProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Admin", description = "관리자 전용 API")
@SecurityRequirement(name = "BearerAuth")
public interface AdminApiDocs {

    @Operation(summary = "대시보드 통계 조회")
    @GetMapping("/api/v1/admin/stats")
    ResponseEntity<ApiResponse<AdminStatsResponse>> getStats();

    @Operation(summary = "전체 사용자 목록 조회 (페이징)")
    @GetMapping("/api/v1/admin/users")
    ResponseEntity<ApiResponse<Page<UserProfileResponse>>> getUsers(
            @PageableDefault(size = 20) Pageable pageable
    );

    @Operation(summary = "사용자 역할 토글 (USER ↔ ADMIN)")
    @PatchMapping("/api/v1/admin/users/{userId}/role")
    ResponseEntity<ApiResponse<UserProfileResponse>> changeUserRole(
            @PathVariable Long userId
    );

    @Operation(summary = "게시글 강제 삭제 (관리자)")
    @DeleteMapping("/api/v1/admin/posts/{postId}")
    ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long postId
    );

    @Operation(summary = "신고 목록 조회 (관리자)")
    @GetMapping("/api/v1/admin/reports")
    ResponseEntity<ApiResponse<Page<PostReportResponse>>> getReports(
            @PageableDefault(size = 20) Pageable pageable
    );

    @Operation(summary = "수집 기사 목록 조회 (관리자)")
    @GetMapping("/api/v1/admin/articles")
    ResponseEntity<ApiResponse<Page<AdminArticleResponse>>> getArticles(
            @PageableDefault(size = 20) Pageable pageable
    );

    @Operation(summary = "AI 요약 강제 생성/갱신 (관리자)")
    @PatchMapping("/api/v1/admin/articles/{articleId}/summary")
    ResponseEntity<ApiResponse<AdminArticleResponse>> refreshArticleSummary(
            @PathVariable Long articleId
    );
}
