package com.newsense.backend.user.controller;

import com.newsense.backend.auth.security.UserPrincipal;
import com.newsense.backend.common.response.ApiResponse;
import com.newsense.backend.user.dto.UserProfileResponse;
import com.newsense.backend.user.dto.UserProfileUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "User", description = "회원 프로필 및 마이페이지 설정 관리")
public interface UserApiDocs {

    @Operation(summary = "내 프로필 조회")
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/api/v1/users/me")
    ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    );

    @Operation(summary = "내 프로필 수정")
    @SecurityRequirement(name = "BearerAuth")
    @PutMapping("/api/v1/users/me")
    ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody UserProfileUpdateRequest request
    );

    @Operation(summary = "회원 탈퇴", description = "계정을 비활성화(soft delete)합니다. 모든 데이터는 보존되며 재활성화 시 복구 가능합니다.")
    @SecurityRequirement(name = "BearerAuth")
    @DeleteMapping("/api/v1/users/me")
    ResponseEntity<ApiResponse<Void>> deleteAccount(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    );
}
