package com.newsense.backend.auth.oauth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OAuth2UserProfile unit tests")
class OAuth2UserProfileTest {

    @Test
    void from_google_extractsEmailAndName() {
        Map<String, Object> attrs = Map.of(
                "email", "user@gmail.com",
                "name", "홍길동"
        );

        OAuth2UserProfile profile = OAuth2UserProfile.from("google", attrs);

        assertThat(profile.email()).isEqualTo("user@gmail.com");
        assertThat(profile.nickname()).isEqualTo("홍길동");
    }

    @Test
    void from_google_usesLoginWhenNameAbsent() {
        Map<String, Object> attrs = Map.of(
                "email", "user@github.com",
                "login", "githubuser"
        );

        OAuth2UserProfile profile = OAuth2UserProfile.from("github", attrs);

        assertThat(profile.email()).isEqualTo("user@github.com");
        assertThat(profile.nickname()).isEqualTo("githubuser");
    }

    @Test
    void from_naver_extractsEmailAndNickname() {
        Map<String, Object> response = Map.of(
                "email", "naver@naver.com",
                "nickname", "네이버유저"
        );
        Map<String, Object> attrs = Map.of("response", response);

        OAuth2UserProfile profile = OAuth2UserProfile.from("naver", attrs);

        assertThat(profile.email()).isEqualTo("naver@naver.com");
        assertThat(profile.nickname()).isEqualTo("네이버유저");
    }

    @Test
    void from_naver_fallsBackToNameWhenNicknameBlank() {
        Map<String, Object> response = Map.of(
                "email", "naver@naver.com",
                "nickname", "",
                "name", "실제이름"
        );
        Map<String, Object> attrs = Map.of("response", response);

        OAuth2UserProfile profile = OAuth2UserProfile.from("naver", attrs);

        assertThat(profile.nickname()).isEqualTo("실제이름");
    }

    @Test
    void from_kakao_extractsEmailAndNickname() {
        Map<String, Object> kakaoProfile = Map.of("nickname", "카카오닉네임");
        Map<String, Object> kakaoAccount = Map.of(
                "email", "kakao@kakao.com",
                "profile", kakaoProfile
        );
        Map<String, Object> attrs = Map.of("kakao_account", kakaoAccount);

        OAuth2UserProfile profile = OAuth2UserProfile.from("kakao", attrs);

        assertThat(profile.email()).isEqualTo("kakao@kakao.com");
        assertThat(profile.nickname()).isEqualTo("카카오닉네임");
    }

    @Test
    void from_kakao_emptyAttributesReturnsBlankFields() {
        OAuth2UserProfile profile = OAuth2UserProfile.from("kakao", Map.of());

        assertThat(profile.email()).isEqualTo("");
        assertThat(profile.nickname()).isEqualTo("");
    }
}
