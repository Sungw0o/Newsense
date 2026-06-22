package com.newsense.backend.auth.controller;

import com.newsense.backend.auth.dto.LoginRequest;
import com.newsense.backend.auth.dto.LoginResponse;
import com.newsense.backend.auth.dto.SignupRequest;
import com.newsense.backend.auth.dto.SignupResponse;
import com.newsense.backend.auth.dto.TokenResponse;
import com.newsense.backend.auth.service.AuthService;
import com.newsense.backend.auth.service.LoginResult;
import com.newsense.backend.auth.service.RefreshResult;
import com.newsense.backend.auth.token.RefreshTokenCookieManager;
import com.newsense.backend.common.exception.CustomException;
import com.newsense.backend.common.exception.ErrorCode;
import com.newsense.backend.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApiDocs {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthService authService;
    private final RefreshTokenCookieManager cookieManager;

    @Override
    public ResponseEntity<ApiResponse<SignupResponse>> signup(SignupRequest request) {
        SignupResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "회원가입이 완료되었습니다.", response));
    }

    @Override
    public ResponseEntity<ApiResponse<LoginResponse>> login(LoginRequest request) {
        LoginResult result = authService.login(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieManager.create(result.tokenPair().refreshToken()).toString())
                .body(ApiResponse.success("로그인이 완료되었습니다.", result.response()));
    }

    @Override
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(String refreshToken) {
        RefreshResult result = authService.refresh(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieManager.create(result.tokenPair().refreshToken()).toString())
                .body(ApiResponse.success("토큰이 재발급되었습니다.", result.response()));
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> logout(String authorization) {
        authService.logout(extractAccessToken(authorization));
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieManager.delete().toString())
                .body(ApiResponse.success("로그아웃이 완료되었습니다.", null));
    }

    private String extractAccessToken(String authorization) {
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        return authorization.substring(BEARER_PREFIX.length());
    }
}
