package com.newsense.backend.inquiry.controller;

import com.newsense.backend.auth.security.UserPrincipal;
import com.newsense.backend.common.response.ApiResponse;
import com.newsense.backend.inquiry.dto.InquiryRequest;
import com.newsense.backend.inquiry.dto.InquiryResponse;
import com.newsense.backend.inquiry.service.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class InquiryController implements InquiryApiDocs {

    private final InquiryService inquiryService;

    @Override
    public ResponseEntity<ApiResponse<InquiryResponse>> submit(
            UserPrincipal userPrincipal,
            InquiryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "문의가 접수되었습니다.",
                inquiryService.submit(userPrincipal.getId(), request)));
    }

    @Override
    public ResponseEntity<ApiResponse<Page<InquiryResponse>>> getMyInquiries(
            UserPrincipal userPrincipal,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                "내 문의 목록 조회에 성공했습니다.",
                inquiryService.getMyInquiries(userPrincipal.getId(), pageable)));
    }

    @Override
    public ResponseEntity<ApiResponse<InquiryResponse>> resolve(Long inquiryId) {
        return ResponseEntity.ok(ApiResponse.success(
                "문의가 처리 완료되었습니다.",
                inquiryService.resolve(inquiryId)));
    }
}
