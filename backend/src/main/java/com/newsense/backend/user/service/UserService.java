package com.newsense.backend.user.service;

import com.newsense.backend.common.exception.CustomException;
import com.newsense.backend.common.exception.ErrorCode;
import com.newsense.backend.user.domain.User;
import com.newsense.backend.user.dto.UserProfileResponse;
import com.newsense.backend.user.dto.UserProfileUpdateRequest;
import com.newsense.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));
        return UserProfileResponse.from(user);
    }

    @Transactional
    public UserProfileResponse updateUserProfile(Long userId, UserProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        if (request.nickname() != null) {
            String newNickname = request.nickname().trim();
            if (!newNickname.equals(user.getNickname()) && userRepository.existsByNickname(newNickname)) {
                throw new CustomException(ErrorCode.NICKNAME_ALREADY_EXISTS);
            }
        }

        user.updateProfile(
                request.nickname() != null ? request.nickname().trim() : null,
                request.interests(),
                request.profileImageUrl(),
                request.level()
        );

        return UserProfileResponse.from(user);
    }
}
