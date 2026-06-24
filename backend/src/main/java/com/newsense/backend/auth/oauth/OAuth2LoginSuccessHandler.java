package com.newsense.backend.auth.oauth;

import com.newsense.backend.auth.token.JwtTokenProvider;
import com.newsense.backend.auth.token.RefreshTokenCookieManager;
import com.newsense.backend.auth.token.TokenPair;
import com.newsense.backend.auth.token.TokenStore;
import com.newsense.backend.user.domain.User;
import com.newsense.backend.user.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenStore tokenStore;
    private final RefreshTokenCookieManager cookieManager;

    @Value("${app.frontend-url:http://127.0.0.1:5173}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauth2User = token.getPrincipal();
        OAuth2UserProfile profile = OAuth2UserProfile.from(
                token.getAuthorizedClientRegistrationId(),
                oauth2User.getAttributes()
        );
        User user = userRepository.findByEmailAndIsActiveTrue(profile.email().trim().toLowerCase(Locale.ROOT))
                .orElseThrow();
        TokenPair tokenPair = jwtTokenProvider.createTokenPair(user);
        tokenStore.saveRefreshToken(user.getId(), tokenPair.refreshToken(), tokenPair.refreshExpiresAt());
        response.addHeader(HttpHeaders.SET_COOKIE, cookieManager.create(tokenPair.refreshToken()).toString());
        String redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                .path("/oauth/callback")
                .queryParam("accessToken", tokenPair.accessToken())
                .build()
                .toUriString();
        response.sendRedirect(redirectUrl);
    }
}
