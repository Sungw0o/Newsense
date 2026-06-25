package com.newsense.backend.auth.service;

import com.newsense.backend.auth.dto.LoginRequest;
import com.newsense.backend.auth.dto.LoginResponse;
import com.newsense.backend.auth.dto.SignupRequest;
import com.newsense.backend.auth.dto.SignupResponse;
import com.newsense.backend.auth.dto.TokenResponse;
import com.newsense.backend.auth.dto.UsernameAvailabilityResponse;
import com.newsense.backend.auth.token.JwtTokenProvider;
import com.newsense.backend.auth.token.TokenPair;
import com.newsense.backend.auth.token.TokenStore;
import com.newsense.backend.common.exception.CustomException;
import com.newsense.backend.common.exception.ErrorCode;
import com.newsense.backend.user.domain.User;
import com.newsense.backend.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenStore tokenStore;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        String email = normalizeEmail(request.email());
        String nickname = request.nickname().trim();
        if (userRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (userRepository.existsByNickname(nickname)) {
            throw new CustomException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }
        User user = User.create(
                email,
                passwordEncoder.encode(request.password()),
                nickname
        );
        User savedUser;
        try {
            savedUser = userRepository.save(user);
        } catch (DataIntegrityViolationException exception) {
            if (userRepository.existsByEmail(email)) {
                throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
            }
            if (userRepository.existsByNickname(nickname)) {
                throw new CustomException(ErrorCode.NICKNAME_ALREADY_EXISTS);
            }
            throw exception;
        }
        return new SignupResponse(savedUser.getId(), savedUser.getEmail(), savedUser.getNickname());
    }

    @Transactional(readOnly = true)
    public UsernameAvailabilityResponse checkUsername(String username) {
        return new UsernameAvailabilityResponse(!userRepository.existsByNickname(username));
    }

    @Transactional(readOnly = true)
    public UsernameAvailabilityResponse checkEmail(String email) {
        return new UsernameAvailabilityResponse(!userRepository.existsByEmail(normalizeEmail(email)));
    }

    @Transactional(readOnly = true)
    public UsernameAvailabilityResponse checkNickname(String nickname) {
        return new UsernameAvailabilityResponse(!userRepository.existsByNickname(nickname.trim()));
    }

    @Transactional(readOnly = true)
    public LoginResult login(LoginRequest request) {
        User user = userRepository.findByEmailAndIsActiveTrue(normalizeEmail(request.email()))
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }
        TokenPair tokenPair = jwtTokenProvider.createTokenPair(user);
        tokenStore.saveRefreshToken(user.getId(), tokenPair.refreshToken(), tokenPair.refreshExpiresAt());
        LoginResponse response = new LoginResponse(
                tokenPair.accessToken(),
                "Bearer",
                user.getId(),
                user.getNickname(),
                user.getRole().name()
        );
        return new LoginResult(response, tokenPair);
    }

    @Transactional(readOnly = true)
    public RefreshResult refresh(String refreshToken) {
        Claims claims = jwtTokenProvider.parseRefreshToken(refreshToken);
        Long userId = Long.valueOf(claims.getSubject());
        tokenStore.validateRefreshToken(userId, refreshToken);
        User user = userRepository.findById(userId)
                .filter(User::isActive)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN));
        TokenPair tokenPair = jwtTokenProvider.createTokenPair(user);
        tokenStore.saveRefreshToken(userId, tokenPair.refreshToken(), tokenPair.refreshExpiresAt());
        return new RefreshResult(new TokenResponse(tokenPair.accessToken(), "Bearer"), tokenPair);
    }

    public void logout(String accessToken) {
        Claims claims = jwtTokenProvider.parseAccessToken(accessToken);
        Long userId = Long.valueOf(claims.getSubject());
        tokenStore.blacklistAccessToken(claims.getId(), claims.getExpiration().toInstant());
        tokenStore.deleteRefreshToken(userId);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
