package com.newsense.backend.auth;

import com.newsense.backend.auth.dto.LoginRequest;
import com.newsense.backend.auth.dto.SignupRequest;
import com.newsense.backend.auth.dto.SignupResponse;
import com.newsense.backend.auth.service.RefreshResult;
import com.newsense.backend.auth.service.AuthService;
import com.newsense.backend.auth.service.LoginResult;
import com.newsense.backend.auth.token.JwtTokenProvider;
import com.newsense.backend.auth.token.TokenPair;
import com.newsense.backend.auth.token.TokenStore;
import com.newsense.backend.common.exception.CustomException;
import com.newsense.backend.common.exception.ErrorCode;
import com.newsense.backend.user.domain.User;
import com.newsense.backend.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 단위 테스트")
class AuthServiceTest {

    @InjectMocks
    AuthService authService;

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    JwtTokenProvider jwtTokenProvider;

    @Mock
    TokenStore tokenStore;

    // ─────────────────────────────────────────────
    //  회원가입
    // ─────────────────────────────────────────────

    @Nested
    @DisplayName("회원가입 (signup)")
    class Signup {

        @Test
        @DisplayName("신규 이메일/닉네임 → 유저 저장 후 SignupResponse 반환")
        void signup_success() {
            SignupRequest request = new SignupRequest("new@example.com", "Password1!", "newbie");
            User savedUser = User.create("new@example.com", "encoded", "newbie");

            given(userRepository.existsByEmail("new@example.com")).willReturn(false);
            given(userRepository.existsByNickname("newbie")).willReturn(false);
            given(passwordEncoder.encode("Password1!")).willReturn("encoded");
            given(userRepository.save(any(User.class))).willReturn(savedUser);

            SignupResponse response = authService.signup(request);

            assertThat(response.email()).isEqualTo("new@example.com");
            assertThat(response.nickname()).isEqualTo("newbie");
            then(userRepository).should().save(any(User.class));
        }

        @Test
        @DisplayName("이메일 대소문자 → 소문자로 정규화 후 중복 체크")
        void signup_emailNormalized() {
            SignupRequest request = new SignupRequest("  Test@EXAMPLE.COM  ", "Password1!", "user");
            User savedUser = User.create("test@example.com", "encoded", "user");

            given(userRepository.existsByEmail("test@example.com")).willReturn(false);
            given(userRepository.existsByNickname("user")).willReturn(false);
            given(passwordEncoder.encode(any())).willReturn("encoded");
            given(userRepository.save(any())).willReturn(savedUser);

            authService.signup(request);

            then(userRepository).should().existsByEmail("test@example.com");
        }

        @Test
        @DisplayName("중복 이메일 → EMAIL_ALREADY_EXISTS 예외")
        void signup_duplicateEmail_throwsException() {
            SignupRequest request = new SignupRequest("dup@example.com", "Password1!", "user");
            given(userRepository.existsByEmail("dup@example.com")).willReturn(true);

            assertThatThrownBy(() -> authService.signup(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS));
        }

        @Test
        @DisplayName("중복 닉네임 → NICKNAME_ALREADY_EXISTS 예외")
        void signup_duplicateNickname_throwsException() {
            SignupRequest request = new SignupRequest("ok@example.com", "Password1!", "taken");

            given(userRepository.existsByEmail("ok@example.com")).willReturn(false);
            given(userRepository.existsByNickname("taken")).willReturn(true);

            assertThatThrownBy(() -> authService.signup(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.NICKNAME_ALREADY_EXISTS));
        }
    }

    // ─────────────────────────────────────────────
    //  로그인
    // ─────────────────────────────────────────────

    @Nested
    @DisplayName("중복 확인")
    class Availability {

        @Test
        @DisplayName("이메일은 trim 및 소문자 정규화 후 중복 여부를 확인한다")
        void checkEmail_normalizesEmail() {
            given(userRepository.existsByEmail("test@example.com")).willReturn(false);

            var response = authService.checkEmail("  Test@EXAMPLE.COM  ");

            assertThat(response.available()).isTrue();
            then(userRepository).should().existsByEmail("test@example.com");
        }

        @Test
        @DisplayName("닉네임은 trim 후 중복 여부를 확인한다")
        void checkNickname_trimsNickname() {
            given(userRepository.existsByNickname("tester")).willReturn(true);

            var response = authService.checkNickname("  tester  ");

            assertThat(response.available()).isFalse();
            then(userRepository).should().existsByNickname("tester");
        }
    }
    @Nested
    @DisplayName("로그인 (login)")
    class Login {

        @Test
        @DisplayName("올바른 이메일/비밀번호 → JWT 발급 + Refresh Token Redis 저장")
        void login_success() {
            LoginRequest request = new LoginRequest("user@example.com", "Password1!");
            User user = User.create("user@example.com", "encoded", "tester");
            TokenPair tokenPair = new TokenPair(
                    "access.token", "refresh.token",
                    Instant.now().plusSeconds(1800),
                    Instant.now().plusSeconds(1209600)
            );

            given(userRepository.findByEmailAndIsActiveTrue("user@example.com"))
                    .willReturn(Optional.of(user));
            given(passwordEncoder.matches("Password1!", "encoded")).willReturn(true);
            given(jwtTokenProvider.createTokenPair(user)).willReturn(tokenPair);

            LoginResult result = authService.login(request);

            assertThat(result.response().accessToken()).isEqualTo("access.token");
            assertThat(result.response().nickname()).isEqualTo("tester");
            assertThat(result.response().tokenType()).isEqualTo("Bearer");
            then(tokenStore).should().saveRefreshToken(any(), anyString(), any());
        }

        @Test
        @DisplayName("존재하지 않는 이메일 → INVALID_CREDENTIALS 예외")
        void login_userNotFound() {
            LoginRequest request = new LoginRequest("none@example.com", "Password1!");
            given(userRepository.findByEmailAndIsActiveTrue("none@example.com"))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_CREDENTIALS));
        }

        @Test
        @DisplayName("비밀번호 불일치 → INVALID_CREDENTIALS 예외")
        void login_wrongPassword() {
            LoginRequest request = new LoginRequest("user@example.com", "WrongPass!");
            User user = User.create("user@example.com", "encoded", "tester");

            given(userRepository.findByEmailAndIsActiveTrue("user@example.com"))
                    .willReturn(Optional.of(user));
            given(passwordEncoder.matches("WrongPass!", "encoded")).willReturn(false);

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_CREDENTIALS));
        }

        @Test
        @DisplayName("이메일 대소문자 혼합 → 소문자 정규화 후 조회")
        void login_emailNormalized() {
            LoginRequest request = new LoginRequest("  USER@EXAMPLE.COM  ", "Password1!");
            User user = User.create("user@example.com", "encoded", "tester");
            TokenPair tokenPair = new TokenPair(
                    "access.token", "refresh.token",
                    Instant.now().plusSeconds(1800),
                    Instant.now().plusSeconds(1209600)
            );

            given(userRepository.findByEmailAndIsActiveTrue("user@example.com"))
                    .willReturn(Optional.of(user));
            given(passwordEncoder.matches("Password1!", "encoded")).willReturn(true);
            given(jwtTokenProvider.createTokenPair(user)).willReturn(tokenPair);

            authService.login(request);

            then(userRepository).should().findByEmailAndIsActiveTrue("user@example.com");
        }
    }

    @Nested
    @DisplayName("refresh/logout")
    class RefreshAndLogout {

        @Test
        @DisplayName("valid refresh token rotates token pair")
        void refresh_success() {
            Claims claims = mock(Claims.class);
            User user = User.create("user@example.com", "encoded", "tester");
            TokenPair tokenPair = new TokenPair(
                    "new.access",
                    "new.refresh",
                    Instant.now().plusSeconds(1800),
                    Instant.now().plusSeconds(1209600)
            );

            given(claims.getSubject()).willReturn("1");
            given(jwtTokenProvider.parseRefreshToken("old.refresh")).willReturn(claims);
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(jwtTokenProvider.createTokenPair(user)).willReturn(tokenPair);

            RefreshResult result = authService.refresh("old.refresh");

            assertThat(result.response().accessToken()).isEqualTo("new.access");
            then(tokenStore).should().validateRefreshToken(1L, "old.refresh");
            then(tokenStore).should().saveRefreshToken(1L, "new.refresh", tokenPair.refreshExpiresAt());
        }

        @Test
        @DisplayName("inactive user cannot refresh")
        void refresh_inactiveUser_throwsException() {
            Claims claims = mock(Claims.class);
            User user = User.create("user@example.com", "encoded", "tester");
            user.deactivate();

            given(claims.getSubject()).willReturn("1");
            given(jwtTokenProvider.parseRefreshToken("old.refresh")).willReturn(claims);
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            assertThatThrownBy(() -> authService.refresh("old.refresh"))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_TOKEN));
        }

        @Test
        @DisplayName("logout blacklists access token and deletes refresh token")
        void logout_success() {
            Claims claims = mock(Claims.class);
            Date expiresAt = Date.from(Instant.now().plusSeconds(1800));

            given(claims.getSubject()).willReturn("1");
            given(claims.getId()).willReturn("jwt-id");
            given(claims.getExpiration()).willReturn(expiresAt);
            given(jwtTokenProvider.parseAccessToken("access.token")).willReturn(claims);

            authService.logout("access.token");

            then(tokenStore).should().blacklistAccessToken("jwt-id", expiresAt.toInstant());
            then(tokenStore).should().deleteRefreshToken(1L);
        }
    }
}
