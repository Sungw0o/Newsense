package com.newsense.backend.auth.oauth;

import com.newsense.backend.user.domain.User;
import com.newsense.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserProfile profile = OAuth2UserProfile.from(registrationId, oauth2User.getAttributes());
        String email = profile.email().trim().toLowerCase(Locale.ROOT);
        if (email.isBlank()) {
            throw new OAuth2AuthenticationException("소셜 계정 이메일을 확인할 수 없습니다.");
        }
        userRepository.findByEmail(email)
                .ifPresentOrElse(
                        User::reactivate,
                        () -> userRepository.save(User.create(
                                email,
                                passwordEncoder.encode(UUID.randomUUID().toString()),
                                createUniqueNickname(profile.nickname(), registrationId)
                        ))
                );
        return oauth2User;
    }

    private String createUniqueNickname(String rawNickname, String provider) {
        String base = rawNickname == null || rawNickname.isBlank() ? provider + "회원" : rawNickname.trim();
        base = base.length() > 20 ? base.substring(0, 20) : base;
        String candidate = base;
        int suffix = 1;
        while (userRepository.existsByNickname(candidate)) {
            String tail = String.valueOf(suffix++);
            int maxBaseLength = Math.max(1, 30 - tail.length());
            candidate = b