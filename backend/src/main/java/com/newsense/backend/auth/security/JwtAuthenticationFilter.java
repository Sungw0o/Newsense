package com.newsense.backend.auth.security;

import com.newsense.backend.auth.token.JwtTokenProvider;
import com.newsense.backend.auth.token.TokenStore;
import com.newsense.backend.common.exception.CustomException;
import com.newsense.backend.common.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenStore tokenStore;
    private final CustomUserDetailsService userDetailsService;
    private final SecurityErrorWriter errorWriter;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = resolveToken(request);
        try {
            if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                authenticate(token);
            }
            filterChain.doFilter(request, response);
        } catch (CustomException exception) {
            SecurityContextHolder.clearContext();
            errorWriter.write(
                    response,
                    exception.getErrorCode().getStatus().value(),
                    exception.getMessage()
            );
        }
    }

    private void authenticate(String token) {
        Claims claims = jwtTokenProvider.parseAccessToken(token);
        if (tokenStore.isBlacklisted(claims.getId())) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        UserPrincipal principal = (UserPrincipal) userDetailsService.loadUserByUsername(claims.get("email", String.class));
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return authorization.substring(BEARER_PREFIX.length());
    }
}
