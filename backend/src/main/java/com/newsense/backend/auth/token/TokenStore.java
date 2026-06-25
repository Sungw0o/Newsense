package com.newsense.backend.auth.token;

import com.newsense.backend.common.exception.CustomException;
import com.newsense.backend.common.exception.ErrorCode;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class TokenStore {

    private static final String REFRESH_PREFIX = "auth:refresh:";
    private static final String BLACKLIST_PREFIX = "auth:blacklist:";

    private final StringRedisTemplate redisTemplate;

    public TokenStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveRefreshToken(Long userId, String refreshToken, Instant expiresAt) {
        redisTemplate.opsForValue().set(
                refreshKey(userId),
                hash(refreshToken),
                positiveDurationUntil(expiresAt)
        );
    }

    public void validateRefreshToken(Long userId, String refreshToken) {
        String storedToken = redisTemplate.opsForValue().get(refreshKey(userId));
        if (!hash(refreshToken).equals(storedToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    public void deleteRefreshToken(Long userId) {
        redisTemplate.delete(refreshKey(userId));
    }

    public void blacklistAccessToken(String tokenId, Instant expiresAt) {
        redisTemplate.opsForValue().set(
                blacklistKey(tokenId),
                "revoked",
                positiveDurationUntil(expiresAt)
        );
    }

    public boolean isBlacklisted(String tokenId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey(tokenId)));
    }

    private Duration positiveDurationUntil(Instant expiresAt) {
        Duration duration = Duration.between(Instant.now(), expiresAt);
        if (duration.isNegative() || duration.isZero()) {
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        }
        return duration;
    }

    private String refreshKey(Long userId) {
        return REFRESH_PREFIX + userId;
    }

    private String blacklistKey(String tokenId) {
        return BLACKLIST_PREFIX + tokenId;
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", exception);
        }
    }
}
