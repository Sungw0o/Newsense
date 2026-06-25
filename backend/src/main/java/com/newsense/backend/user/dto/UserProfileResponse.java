package com.newsense.backend.user.dto;

import com.newsense.backend.user.domain.User;
import java.util.List;

public record UserProfileResponse(
        Long id,
        String email,
        String nickname,
        String role,
        String subPlan,
        String profileImageUrl,
        List<String> interests,
        String level,
        boolean active
) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole().name(),
                user.getSubPlan(),
                user.getProfileImageUrl(),
                user.getInterests(),
                user.getLevel().name(),
                user.isActive()
        );
    }
}
