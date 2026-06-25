package com.newsense.backend.inquiry.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InquiryRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank String content
) {}
