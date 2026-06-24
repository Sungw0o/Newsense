package com.newsense.backend.review;

import com.newsense.backend.article.domain.ArticleMeta;
import com.newsense.backend.article.repository.ArticleMetaRepository;
import com.newsense.backend.common.exception.CustomException;
import com.newsense.backend.common.exception.ErrorCode;
import com.newsense.backend.review.domain.Review;
import com.newsense.backend.review.dto.ReviewCreateRequest;
import com.newsense.backend.review.dto.ReviewResponse;
import com.newsense.backend.review.dto.ReviewUpdateRequest;
import com.newsense.backend.review.repository.ReviewRepository;
import com.newsense.backend.review.service.ReviewService;
import com.newsense.backend.support.TestFixtures;
import com.newsense.backend.user.domain.User;
import com.newsense.backend.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService unit tests")
class ReviewServiceTest {

    @InjectMocks
    ReviewService reviewService;

    @Mock
    ReviewRepository reviewRepository;

    @Mock
    ArticleMetaRepository articleMetaRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @Test
    void createReview_normalizesTermsAndPublishesEvent() {
        ArticleMeta article = TestFixtures.article(1L);
        User user = TestFixtures.user(7L);
        given(articleMetaRepository.findById(1L)).willReturn(Optional.of(article));
        given(userRepository.findById(7L)).willReturn(Optional.of(user));
        given(reviewRepository.findByArticleIdAndUserId(1L, 7L)).willReturn(Optional.empty());
        given(reviewRepository.save(any(Review.class))).willAnswer(invocation -> invocation.getArgument(0));

        ReviewResponse response = reviewService.createReview(
                7L,
                new ReviewCreateRequest(
                        1L,
                        " 요약 ",
                        " 배운 점 ",
                        List.of(" 환율 ", "", "환율", "금리")
                )
        );

        assertThat(response.summary()).isEqualTo("요약");
        assertThat(response.learned()).isEqualTo("배운 점");
        assertThat(response.difficultTerms()).containsExactly("환율", "금리");
        then(eventPublisher).should().publishEvent(any(Object.class));
    }

    @Test
    void createReview_rejectsActiveDuplicate() {
        ArticleMeta article = TestFixtures.article(1L);
        User user = TestFixtures.user(7L);
        Review review = Review.create(user, article, "old", "old", List.of());
        given(articleMetaRepository.findById(1L)).willReturn(Optional.of(article));
        given(userRepository.findById(7L)).willReturn(Optional.of(user));
        given(reviewRepository.findByArticleIdAndUserId(1L, 7L)).willReturn(Optional.of(review));

        assertThatThrownBy(() -> reviewService.createReview(
                7L,
                new ReviewCreateRequest(1L, "new", "new", List.of())
        ))
                .isInstanceOf(CustomException.class)
                .satisfies(error -> assertThat(((CustomException) error).getErrorCode())
                        .isEqualTo(ErrorCode.REVIEW_ALREADY_EXISTS));
    }

    @Test
    void updateAndDeleteReview_modifyOwnedActiveReview() {
        ArticleMeta article = TestFixtures.article(1L);
        User user = TestFixtures.user(7L);
        Review review = Review.create(user, article, "old", "old", List.of("금리"));
        given(reviewRepository.findByIdAndUserIdAndIsActiveTrue(3L, 7L)).willReturn(Optional.of(review));

        ReviewResponse updated = reviewService.updateReview(
                3L,
                7L,
                new ReviewUpdateRequest(" 새 요약 ", " 새 배움 ", List.of("환율"))
        );
        reviewService.deleteReview(3L, 7L);

        assertThat(updated.summary()).isEqualTo("새 요약");
        assertThat(updated.difficultTerms()).containsExactly("환율");
        assertThat(review.isActive()).isFalse();
    }

    @Test
    void getMyReview_throwsWhenMissing() {
        given(reviewRepository.findByArticleIdAndUserIdAndIsActiveTrue(1L, 7L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.getMyReview(1L, 7L))
                .isInstanceOf(CustomException.class)
                .satisfies(error -> assertThat(((CustomException) error).getErrorCode())
                        .isEqualTo(ErrorCode.REVIEW_NOT_FOUND));
    }
}
