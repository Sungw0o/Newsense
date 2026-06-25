package com.newsense.backend.admin;

import com.newsense.backend.admin.dto.AdminStatsResponse;
import com.newsense.backend.admin.service.AdminService;
import com.newsense.backend.ai.article.OpenAiArticleClassifierClient;
import com.newsense.backend.article.repository.ArticleContentRepository;
import com.newsense.backend.article.repository.ArticleMetaRepository;
import com.newsense.backend.article.domain.ArticleMeta;
import com.newsense.backend.common.exception.CustomException;
import com.newsense.backend.community.domain.Post;
import com.newsense.backend.community.domain.PostType;
import com.newsense.backend.community.repository.PostReportRepository;
import com.newsense.backend.community.repository.PostRepository;
import com.newsense.backend.support.TestFixtures;
import com.newsense.backend.user.domain.User;
import com.newsense.backend.user.domain.UserRole;
import com.newsense.backend.user.dto.UserProfileResponse;
import com.newsense.backend.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminService unit tests")
class AdminServiceTest {

    @InjectMocks
    AdminService adminService;

    @Mock UserRepository userRepository;
    @Mock PostRepository postRepository;
    @Mock PostReportRepository postReportRepository;
    @Mock ArticleMetaRepository articleMetaRepository;
    @Mock ArticleContentRepository articleContentRepository;
    @Mock OpenAiArticleClassifierClient articleClassifierClient;

    @Test
    void getStats_returnsCountsFromRepositories() {
        given(userRepository.count()).willReturn(10L);
        given(postRepository.count()).willReturn(5L);
        given(articleMetaRepository.count()).willReturn(100L);

        AdminStatsResponse result = adminService.getStats();

        assertThat(result.totalUsers()).isEqualTo(10L);
        assertThat(result.totalPosts()).isEqualTo(5L);
        assertThat(result.totalArticles()).isEqualTo(100L);
    }

    @Test
    void getUsers_mapsUsersToProfileResponse() {
        User user = TestFixtures.user(1L);
        Pageable pageable = PageRequest.of(0, 10);
        given(userRepository.findAll(pageable)).willReturn(new PageImpl<>(List.of(user)));

        var result = adminService.getUsers(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).email()).isEqualTo("user1@example.com");
    }

    @Test
    void changeUserRole_toggesToAdmin_whenCurrentlyUser() {
        User user = TestFixtures.user(1L);
        assertThat(user.getRole()).isEqualTo(UserRole.USER);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        UserProfileResponse result = adminService.changeUserRole(1L);

        assertThat(result.email()).isEqualTo("user1@example.com");
        assertThat(user.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void changeUserRole_throwsEntityNotFound_whenUserMissing() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.changeUserRole(99L))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void deletePost_removesPost_whenExists() {
        User user = TestFixtures.user(1L);
        Post post = Post.create("제목", "내용", user, null, null, PostType.GENERAL);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        adminService.deletePost(1L);

        verify(postRepository).delete(post);
    }

    @Test
    void deletePost_throwsPostNotFound_whenMissing() {
        given(postRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.deletePost(99L))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void getArticles_mapsArticlesToResponse() {
        ArticleMeta article = TestFixtures.article(1L);
        Pageable pageable = PageRequest.of(0, 10);
        given(articleMetaRepository.findAll(pageable)).willReturn(new PageImpl<>(List.of(article)));
        given(articleContentRepository.findById(article.getMongoDocumentId()))
                .willReturn(Optional.of(TestFixtures.content("mongo-1", "본문 텍스트")));

        var result = adminService.getArticles(pageable);

        assertThat(result.getContent()).hasSize(1);
    }
}
