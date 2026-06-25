package com.newsense.backend.auth.dto;

public record TokenResponse(
        String accessToken,
        String tokenType
) {
}
