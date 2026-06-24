package com.newsense.backend.admin.dto;

import com.newsense.backend.community.domain.PostReport;

import java.time.LocalDateTime;

public record PostReportResponse(
        Long reportId,
        Long postId,
        String postTitle,
        Long reporterId,
        String reporterNickname,
        String reason,
        LocalDateTime reportedAt
) {
    public static PostReportResponse from(PostReport report) {
        return new PostReportResponse(
                report.getId(),
                report.getPost().getId(),
                report.getPost().getTitle(),
                report.getReporter().getId(),
                report.getReporter().getNickname(),
                report.getReason(),
                report.getReportedAt()
        );
    }
}
