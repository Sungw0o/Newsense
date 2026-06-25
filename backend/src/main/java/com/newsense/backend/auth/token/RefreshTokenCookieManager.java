package com.newsense.backend.auth.token;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RefreshTokenCookieManager {

    public static final String COOKIE_NAME = "refreshToken";

    private final JwtProperties properties;

    public RefreshTokenCookieManager(JwtProperties properties) {
        this.properties = properties;
    }

    public ResponseCookie create(String refreshToken) {
        return ResponseCookie.from(COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(properties.secureCookie())
                .sameSite("Lax")
                .path("/api/v1/auth")
                .maxAge(Duration.ofMillis(properties.refreshExpiration()))
                .build();
    }

    public ResponseCookie delete() {
        return ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(properties.secureCookie())
                .sameSite("Lax")
                .path("/api/v1/auth")
                .maxAge(Duration.ZERO)
                .build();
    }
}
