package com.newsense.backend.auth.service;

import com.newsense.backend.auth.dto.LoginResponse;
import com.newsense.backend.auth.token.TokenPair;

public record LoginResult(
        LoginResponse response,
        TokenPair tokenPair
) {
}
