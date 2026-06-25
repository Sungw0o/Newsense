package com.newsense.backend.auth;

import com.newsense.backend.auth.controller.AuthController;
import com.newsense.backend.auth.dto.LoginRequest;
import com.newsense.backend.auth.dto.LoginResponse;
import com.newsense.backend.auth.dto.SignupRequest;
import com.newsense.backend.auth.dto.SignupResponse;
import com.newsense.backend.auth.service.AuthService;
import com.newsense.backend.auth.service.LoginResult;
import com.newsense.backend.auth.token.RefreshTokenCookieManager;
import com.newsense.backend.auth.token.TokenPair;
import com.newsense.backend.common.exception.CustomException;
import com.newsense.backend.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController 단위 테스트")
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private RefreshTokenCookieManager cookieManager;

    @InjectMocks
    private AuthController authController;

    @Nested
    @DisplayName("회원가입")
    class Signup {
        @Test
        @DisplayName("정상 입력 → 201 Created 반환")
        void signup_success() {
            SignupRequest request = new SignupRequest("test@example.com", "Password1!", "tester");
            SignupResponse response = new SignupResponse(1L, "test@example.com", "tester");

            given(authService.signup(any())).willReturn(response);

            ResponseEntity<?> result = authController.signup(request);

            assertEquals(HttpStatus.CREATED, result.getStatusCode());
        }
    }

    @Nested
    @DisplayName("로그인")
    class Login {
        @Test
        @DisplayName("정상 자격증명 → 200 OK 반환")
        void login_success() {
            LoginRequest request = new LoginRequest("user@example.com", "Password1!");
            LoginResponse loginResponse = new LoginResponse("fake.access.token", "Bearer", 1L, "tester", "USER");
            TokenPair tokenPair = new TokenPair("fake.access.token", "fake.refresh.token", Instant.now(), Instant.now());
            LoginResult loginResult = new LoginResult(loginResponse, tokenPair);

            given(authService.login(any())).willReturn(loginResult);
            given(cookieManager.create(any())).willReturn(ResponseCookie.from("refresh_token", "fake").build());

            ResponseEntity<?> result = authController.login(request);

            assertEquals(HttpStatus.OK, result.getStatusCode());
        }
    }

    @Nested
    @DisplayName("로그아웃")
    class Logout {
        @Test
        @DisplayName("로그아웃 → 200 OK 반환")
        void logout_success() {
            given(cookieManager.delete()).willReturn(ResponseCookie.from("refresh_token", "").build());

            ResponseEntity<?> result = authController.logout("Bearer fake.access.token");

            assertEquals(HttpStatus.OK, result.getStatusCode());
            verify(authService).logout("fake.access.token");
        }
    }
}
