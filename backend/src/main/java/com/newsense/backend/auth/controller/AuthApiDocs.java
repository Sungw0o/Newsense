package com.newsense.backend.auth.controller;

import com.newsense.backend.auth.dto.LoginRequest;
import com.newsense.backend.auth.dto.LoginResponse;
import com.newsense.backend.auth.dto.SignupRequest;
import com.newsense.backend.auth.dto.SignupResponse;
import com.newsense.backend.auth.dto.TokenResponse;
import com.newsense.backend.auth.dto.UsernameAvailabilityResponse;
import com.newsense.backend.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Auth", description = "회원가입 및 JWT 인증")
public interface AuthApiDocs {

    @Operation(summary = "회원가입")
    @PostMapping("/api/v1/auth/signup")
    ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request);

    @Operation(summary = "일반 로그인")
    @PostMapping("/api/v1/auth/login")
    ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request);

    @Operation(summary = "ID 중복 확인")
    @GetMapping("/api/v1/auth/check-username")
    ResponseEntity<ApiResponse<UsernameAvailabilityResponse>> checkUsername(@RequestParam String username);

    @Operation(summary = "이메일 중복 확인")
    @GetMapping("/api/v1/auth/check-email")
    ResponseEntity<ApiResponse<UsernameAvailabilityResponse>> checkEmail(@RequestParam String email);

    @Operation(summary = "닉네임 중복 확인")
    @GetMapping("/api/v1/auth/check-nickname")
    ResponseEntity<ApiResponse<UsernameAvailabilityResponse>> checkNickname(@RequestParam String nickname);

    @Operation(summary = "JWT 토큰 재발급")
    @PostMapping("/api/v1/auth/refresh")
    ResponseEntity<ApiResponse<TokenResponse>> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken
    );

    @Operation(summary = "로그아웃")
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/api/v1/auth/logout")
    ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authorization);
}
