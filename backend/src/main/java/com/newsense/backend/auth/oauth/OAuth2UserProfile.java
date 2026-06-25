package com.newsense.backend.auth.oauth;

import java.util.Map;

record OAuth2UserProfile(String email, String nickname) {

    static OAuth2UserProfile from(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) {
            case "naver" -> fromNaver(attributes);
            case "kakao" -> fromKakao(attributes);
            default -> fromDefault(attributes);
        };
    }

    private static OAuth2UserProfile fromDefault(Map<String, Object> attributes) {
        return new OAuth2UserProfile(
                asString(attributes.get("email")),
                firstNotBlank(asString(attributes.get("name")), asString(attributes.get("login")))
        );
    }

    @SuppressWarnings("unchecked")
    private static OAuth2UserProfile fromNaver(Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.getOrDefault("response", Map.of());
        return new OAuth2UserProfile(
                asString(response.get("email")),
                firstNotBlank(asString(response.get("nickname")), asString(response.get("name")))
        );
    }

    @SuppressWarnings("unchecked")
    private static OAuth2UserProfile fromKakao(Map<String, Object> attributes) {
        Map<String, Object> account = (Map<String, Object>) attributes.getOrDefault("kakao_account", Map.of());
        Map<String, Object> profile = (Map<String, Object>) account.getOrDefault("profile", Map.of());
        return new OAuth2UserProfile(
                asString(account.get("email")),
                asString(profile.get("nickname"))
        );
    }

    private static String firstNotBlank(String left, String right) {
        return left == null || left.isBlank() ? right : left;
    }

    private static String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
