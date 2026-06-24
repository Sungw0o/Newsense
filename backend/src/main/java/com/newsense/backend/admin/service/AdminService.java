package com.newsense.backend.admin.service;

import com.newsense.backend.admin.dto.AdminStatsResponse;
import com.newsense.backend.article.repository.ArticleMetaRepository;
import com.newsense.backend.common.exception.CustomException;
import com.newsense.backend.common.exception.ErrorCode;
import com.newsense.backend.community.domain.Post;
import com.newsense.backend.community.repository.PostRepository;
import com.newsense.backend.user.domain.User;
import com.newsense.backend.user.domain.UserRole;
import com.newsense.backend.user.dto.UserProfileResponse;
import com.newsense.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ArticleMetaRepository articleMetaRepository;

    @Transactional(readOnly = true)
    public AdminStatsResponse getStats() {
        return new AdminStatsResponse(
                userRepository.count(),
                postRepository.count(),
                articleMetaRepository.count()
        );
    }

    @Transactional(readOnly = true)
    public Page<UserProfileResponse> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserProfileResponse::from);
    }

    @Transactional
    public UserProfileResponse changeUserRole(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));
        user.changeRole(user.getRole() == UserRole.ADMIN ? UserRole.USER : UserRole.ADMIN);
        return UserProfileResponse.from(user);
    }

    @Transactional
    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        postRepository.delete(post);
    }
}
