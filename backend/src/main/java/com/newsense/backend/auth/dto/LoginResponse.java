package com.newsense.backend.auth.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        Long userId,
        String nickname,
        String role
) {
}
