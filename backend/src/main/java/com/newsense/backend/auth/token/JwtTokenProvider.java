package com.newsense.backend.auth.token;

import com.newsense.backend.common.exception.CustomException;
import com.newsense.backend.common.exception.ErrorCode;
import com.newsense.backend.user.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private static final String TOKEN_TYPE = "tokenType";
    private static final String ACCESS = "access";
    private static final String REFRESH = "refresh";

    private final JwtProperties properties;
    private final SecretKey secretKey;

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
        this.secretKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public TokenPair createTokenPair(User user) {
        Instant now = Instant.now();
        Instant accessExpiresAt = now.plusMillis(properties.accessExpiration());
        Instant refreshExpiresAt = now.plusMillis(properties.refreshExpiration());
        return new TokenPair(
                createToken(user, ACCESS, now, accessExpiresAt),
                createToken(user, REFRESH, now, refreshExpiresAt),
                accessExpiresAt,
                refreshExpiresAt
        );
    }

    public String createAccessToken(User user) {
        Instant now = Instant.now();
        return createToken(user, ACCESS, now, now.plusMillis(properties.accessExpiration()));
    }

    public Claims parseAccessToken(String token) {
        Claims claims = parse(token);
        if (!ACCESS.equals(claims.get(TOKEN_TYPE, String.class))) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        return claims;
    }

    public Claims parseRefreshToken(String token) {
        Claims claims = parse(token);
        if (!REFRESH.equals(claims.get(TOKEN_TYPE, String.class))) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        return claims;
    }

    public long getAccessExpirationMillis() {
        return properties.accessExpiration();
    }

    public long getRefreshExpirationMillis() {
        return properties.refreshExpiration();
    }

    private String createToken(User user, String tokenType, Instant issuedAt, Instant expiresAt) {
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .claim(TOKEN_TYPE, tokenType)
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    private Claims parse(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException exception) {
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }
}
