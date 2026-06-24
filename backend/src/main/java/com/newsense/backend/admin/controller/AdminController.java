package com.newsense.backend.admin.controller;

import com.newsense.backend.admin.dto.AdminStatsResponse;
import com.newsense.backend.admin.dto.PostReportResponse;
import com.newsense.backend.admin.service.AdminService;
import com.newsense.backend.common.response.ApiResponse;
import com.newsense.backend.user.dto.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminController implements AdminApiDocs {

    private final AdminService adminService;

    @Override
    public ResponseEntity<ApiResponse<AdminStatsResponse>> getStats() {
        return ResponseEntity.ok(ApiResponse.success("통계 조회에 성공했습니다.", adminService.getStats()));
    }

    @Override
    public ResponseEntity<ApiResponse<Page<UserProfileResponse>>> getUsers(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("사용자 목록 조회에 성공했습니다.", adminService.getUsers(pageable)));
    }

    @Override
    public ResponseEntity<ApiResponse<UserProfileResponse>> changeUserRole(Long userId) {
        return ResponseEntity.ok(ApiResponse.success("역할이 변경되었습니다.", adminService.changeUserRole(userId)));
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> deletePost(Long postId) {
        adminService.deletePost(postId);
        return ResponseEntity.ok(ApiResponse.success("게시글이 삭제되었습니다.", null));
    }

    @Override
    @GetMapping("/api/v1/admin/reports")
    public ResponseEntity<ApiResponse<Page<PostReportResponse>>> getReports(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("신고 목록 조회에 성공했습니다.", adminService.getReports(pageable)));
    }
}
