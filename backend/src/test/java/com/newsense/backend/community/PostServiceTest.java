package com.newsense.backend.community;

import com.newsense.backend.article.domain.ArticleMeta;
import com.newsense.backend.article.repository.ArticleMetaRepository;
import com.newsense.backend.common.exception.CustomException;
import com.newsense.backend.common.exception.ErrorCode;
import com.newsense.backend.community.domain.Post;
import com.newsense.backend.community.domain.PostComment;
import com.newsense.backend.community.domain.PostReaction;
import com.newsense.backend.community.domain.PostReactionType;
import com.newsense.backend.community.domain.PostType;
import com.newsense.backend.community.dto.CommentCreateRequest;
import com.newsense.backend.community.dto.CommentResponse;
import com.newsense.backend.community.dto.PostCreateRequest;
import com.newsense.backend.community.dto.PostReactionResponse;
import com.newsense.backend.community.dto.PostResponse;
import com.newsense.backend.community.repository.PostCommentRepository;
import com.newsense.backend.community.repository.PostReactionRepository;
import com.newsense.backend.community.repository.PostReportRepository;
import com.newsense.backend.community.repository.PostRepository;
import com.newsense.backend.community.service.PostService;
import com.newsense.backend.support.TestFixtures;
import com.newsense.backend.user.domain.User;
import com.newsense.backend.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostService unit tests")
class PostServiceTest {

    @InjectMocks PostService postService;

    @Mock PostRepository postRepository;
    @Mock PostReactionRepository postReactionRepository;
    @Mock PostCommentRepository postCommentRepository;
    @Mock PostReportRepository postReportRepository;
    @Mock UserRepository userRepository;
    @Mock ArticleMetaRepository articleMetaRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // 게시글 생성
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createPost")
    class CreatePost {

        @Test
        @DisplayName("일반 사용자가 GENERAL 게시글 생성 성공")
        void createPost_generalUser_success() {
            User user = TestFixtures.user(7L);
            Post post = Post.create("제목", "내용", user, null, null, PostType.GENERAL);
            ReflectionTestUtils.setField(post, "id", 1L);

            given(userRepository.findById(7L)).willReturn(Optional.of(user));
            given(postRepository.save(any(Post.class))).willReturn(post);
            given(postCommentRepository.countByPostId(1L)).willReturn(0L);

            PostResponse response = postService.createPost(7L,
                    new PostCreateRequest("제목", "내용", null, null, PostType.GENERAL));

            assertThat(response.title()).isEqualTo("제목");
            assertThat(response.type()).isEqualTo(PostType.GENERAL);
        }

        @Test
        @DisplayName("일반 사용자가 NOTICE 게시글 생성 시 ACCESS_DENIED")
        void createPost_nonAdminCreatesNotice_throwsAccessDenied() {
            User user = TestFixtures.user(7L);
            given(userRepository.findById(7L)).willReturn(Optional.of(user));

            assertThatThrownBy(() -> postService.createPost(7L,
                    new PostCreateRequest("공지", "내용", null, null, PostType.NOTICE)))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.ACCESS_DENIED));
        }

        @Test
        @DisplayName("존재하지 않는 기사 ID 참조 시 ARTICLE_NOT_FOUND")
        void createPost_articleNotFound_throws() {
            User user = TestFixtures.user(7L);
            given(userRepository.findById(7L)).willReturn(Optional.of(user));
            given(articleMetaRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> postService.createPost(7L,
                    new PostCreateRequest("제목", "내용", 99L, null, null)))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.ARTICLE_NOT_FOUND));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 게시글 조회 (viewCount 증가)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getPost: 조회 시 viewCount 1 증가")
    void getPost_incrementsViewCount() {
        User user = TestFixtures.user(7L);
        Post post = Post.create("제목", "내용", user, null, null, PostType.GENERAL);
        ReflectionTestUtils.setField(post, "id", 1L);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(postCommentRepository.countByPostId(1L)).willReturn(0L);

        postService.getPost(1L);

        assertThat(post.getViewCount()).isEqualTo(1L);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 반응 (좋아요 / 싫어요)
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("toggleLike")
    class ToggleLike {

        @Test
        @DisplayName("처음 좋아요 → likes +1, isLiked=true")
        void toggleLike_firstTime_incrementsLikes() {
            User user = TestFixtures.user(7L);
            Post post = Post.create("제목", "내용", user, null, null, PostType.GENERAL);
            ReflectionTestUtils.setField(post, "id", 1L);
            given(postRepository.findById(1L)).willReturn(Optional.of(post));
            given(userRepository.findById(7L)).willReturn(Optional.of(user));
            given(postReactionRepository.findByPostIdAndUserId(1L, 7L)).willReturn(Optional.empty());
            given(postReactionRepository.save(any(PostReaction.class)))
                    .willAnswer(inv -> inv.getArgument(0));

            PostReactionResponse response = postService.toggleLike(1L, 7L);

            assertThat(response.likes()).isEqualTo(1);
            assertThat(response.liked()).isTrue();
            assertThat(response.disliked()).isFalse();
        }

        @Test
        @DisplayName("좋아요 취소 → likes 0, isLiked=false")
        void toggleLike_cancel_removesLike() {
            User user = TestFixtures.user(7L);
            Post post = Post.create("제목", "내용", user, null, null, PostType.GENERAL);
            ReflectionTestUtils.setField(post, "id", 1L);
            post.addLike();
            PostReaction reaction = PostReaction.create(user, post, PostReactionType.LIKE);

            given(postRepository.findById(1L)).willReturn(Optional.of(post));
            given(userRepository.findById(7L)).willReturn(Optional.of(user));
            given(postReactionRepository.findByPostIdAndUserId(1L, 7L)).willReturn(Optional.of(reaction));

            PostReactionResponse response = postService.toggleLike(1L, 7L);

            assertThat(response.likes()).isEqualTo(0);
            assertThat(response.liked()).isFalse();
            then(postReactionRepository).should().delete(reaction);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 댓글
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("createComment: 댓글 생성 성공")
    void createComment_success() {
        User user = TestFixtures.user(7L);
        Post post = Post.create("제목", "내용", user, null, null, PostType.GENERAL);
        ReflectionTestUtils.setField(post, "id", 1L);
        PostComment comment = PostComment.create(post, user, "댓글 내용");
        ReflectionTestUtils.setField(comment, "id", 10L);

        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(userRepository.findById(7L)).willReturn(Optional.of(user));
        given(postCommentRepository.save(any(PostComment.class))).willReturn(comment);

        CommentResponse response = postService.createComment(1L, 7L, new CommentCreateRequest("댓글 내용"));

        assertThat(response.content()).isEqualTo("댓글 내용");
    }

    @Test
    @DisplayName("deleteComment: 본인 댓글이 아니면 ACCESS_DENIED")
    void deleteComment_notOwner_throwsAccessDenied() {
        User owner = TestFixtures.user(7L);
        User other = TestFixtures.user(8L);
        Post post = Post.create("제목", "내용", owner, null, null, PostType.GENERAL);
        PostComment comment = PostComment.create(post, owner, "내용");
        ReflectionTestUtils.setField(comment, "id", 10L);

        given(postCommentRepository.findById(10L)).willReturn(Optional.of(comment));

        assertThatThrownBy(() -> postService.deleteComment(10L, 8L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(ErrorCode.ACCESS_DENIED));
    }
}
