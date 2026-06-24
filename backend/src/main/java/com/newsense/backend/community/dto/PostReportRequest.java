package com.newsense.backend.community.dto;

import jakarta.validation.constraints.Size;

public record PostReportRequest(
        @Size(max = 500) String reason
) {
}
