package com.newsense.backend.auth.token;

import java.time.Instant;

public record TokenPair(
        String accessToken,
        String refreshToken,
        Instant accessExpiresAt,
        Instant refreshExpiresAt
) {
}
