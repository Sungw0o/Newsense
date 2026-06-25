package com.newsense.backend.user;

import com.newsense.backend.article.domain.ArticleDifficulty;
import com.newsense.backend.common.exception.CustomException;
import com.newsense.backend.common.exception.ErrorCode;
import com.newsense.backend.support.TestFixtures;
import com.newsense.backend.user.domain.User;
import com.newsense.backend.user.dto.UserProfileResponse;
import com.newsense.backend.user.dto.UserProfileUpdateRequest;
import com.newsense.backend.user.repository.UserRepository;
import com.newsense.backend.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService unit tests")
class UserServiceTest {

    @InjectMocks
    UserService userService;

    @Mock
    UserRepository userRepository;

    @Test
    void getUserProfile_returnsCurrentProfile() {
        User user = TestFixtures.user(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        UserProfileResponse response = userService.getUserProfile(1L);

        assertThat(response.email()).isEqualTo("user1@example.com");
        assertThat(response.nickname()).isEqualTo("tester1");
    }

    @Test
    void updateUserProfile_trimsNicknameAndUpdatesFields() {
        User user = TestFixtures.user(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userRepository.existsByNickname("newbie")).willReturn(false);

        UserProfileResponse response = userService.updateUserProfile(
                1L,
                new UserProfileUpdateRequest(
                        " newbie ",
                        List.of("금융", "환율"),
                        "https://example.com/profile.png",
                        ArticleDifficulty.INTERMEDIATE
                )
        );

        assertThat(response.nickname()).isEqualTo("newbie");
        assertThat(response.interests()).containsExactly("금융", "환율");
        assertThat(response.level()).isEqualTo(ArticleDifficulty.INTERMEDIATE.name());
    }

    @Test
    void updateUserProfile_rejectsDuplicateNickname() {
        User user = TestFixtures.user(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userRepository.existsByNickname("taken")).willReturn(true);

        assertThatThrownBy(() -> userService.updateUserProfile(
                1L,
                new UserProfileUpdateRequest("taken", null, null, null)
        ))
                .isInstanceOf(CustomException.class)
                .satisfies(error -> assertThat(((CustomException) error).getErrorCode())
                        .isEqualTo(ErrorCode.NICKNAME_ALREADY_EXISTS));
    }

    @Test
    void deleteAccount_deactivatesUser() {
        User user = TestFixtures.user(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        userService.deleteAccount(1L);

        assertThat(user.isActive()).isFalse();
    }
}
