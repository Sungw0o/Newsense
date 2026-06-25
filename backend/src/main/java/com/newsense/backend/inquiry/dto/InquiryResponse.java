package com.newsense.backend.inquiry.dto;

import com.newsense.backend.inquiry.domain.Inquiry;

import java.time.LocalDateTime;

public record InquiryResponse(
        Long id,
        String authorName,
        String title,
        String content,
        boolean resolved,
        LocalDateTime createdAt,
        LocalDateTime resolvedAt
) {
    public static InquiryResponse from(Inquiry inquiry) {
        String name = inquiry.getUser() != null ? inquiry.getUser().getNickname() : "탈퇴한 사용자";
        return new InquiryResponse(
                inquiry.getId(),
                name,
                inquiry.getTitle(),
                inquiry.getContent(),
                inquiry.isResolved(),
                inquiry.getCreatedAt(),
                inquiry.getResolvedAt()
        );
    }
}
