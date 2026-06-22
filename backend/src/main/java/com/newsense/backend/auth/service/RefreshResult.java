package com.newsense.backend.auth.service;

import com.newsense.backend.auth.dto.TokenResponse;
import com.newsense.backend.auth.token.TokenPair;

public record RefreshResult(
        TokenResponse response,
        TokenPair tokenPair
) {
}
