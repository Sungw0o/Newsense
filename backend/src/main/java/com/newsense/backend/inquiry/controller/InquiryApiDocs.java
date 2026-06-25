package com.newsense.backend.inquiry.controller;

import com.newsense.backend.auth.security.UserPrincipal;
import com.newsense.backend.common.response.ApiResponse;
import com.newsense.backend.inquiry.dto.InquiryRequest;
import com.newsense.backend.inquiry.dto.InquiryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Inquiry", description = "사용자 문의")
@SecurityRequirement(name = "BearerAuth")
public interface InquiryApiDocs {

    @Operation(summary = "문의 제출")
    @PostMapping("/api/v1/inquiries")
    ResponseEntity<ApiResponse<InquiryResponse>> submit(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody InquiryRequest request
    );

    @Operation(summary = "내 문의 목록 조회")
    @GetMapping("/api/v1/inquiries/me")
    ResponseEntity<ApiResponse<Page<InquiryResponse>>> getMyInquiries(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PageableDefault(size = 10) Pageable pageable
    );

    @Operation(summary = "문의 처리 완료 (관리자)")
    @PatchMapping("/api/v1/admin/inquiries/{inquiryId}/resolve")
    ResponseEntity<ApiResponse<InquiryResponse>> resolve(
            @PathVariable Long inquiryId
    );
}
