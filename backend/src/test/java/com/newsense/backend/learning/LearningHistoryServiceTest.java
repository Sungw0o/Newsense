package com.newsense.backend.learning;

import com.newsense.backend.article.domain.ArticleMeta;
import com.newsense.backend.article.event.ArticleReadCompletedEvent;
import com.newsense.backend.article.repository.ArticleMetaRepository;
import com.newsense.backend.common.exception.CustomException;
import com.newsense.backend.common.exception.ErrorCode;
import com.newsense.backend.learning.domain.LearningHistory;
import com.newsense.backend.learning.domain.LearningHistoryType;
import com.newsense.backend.learning.dto.LearningHistoryResponse;
import com.newsense.backend.learning.dto.LearningStatsResponse;
import com.newsense.backend.learning.repository.LearningHistoryRepository;
import com.newsense.backend.learning.service.LearningHistoryService;
import com.newsense.backend.quiz.event.QuizCompletedEvent;
import com.newsense.backend.review.event.ReviewCompletedEvent;
import com.newsense.backend.support.TestFixtures;
import com.newsense.backend.user.domain.User;
import com.newsense.backend.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("LearningHistoryService unit tests")
class LearningHistoryServiceTest {

    @InjectMocks
    LearningHistoryService learningHistoryService;

    @Mock
    LearningHistoryRepository learningHistoryRepository;

    @Mock
    ArticleMetaRepository articleMetaRepository;

    @Mock
    UserRepository userRepository;

    @Test
    void recordEvents_saveEachLearningTypeWhenAbsent() {
        User user = TestFixtures.user(7L);
        ArticleMeta article = TestFixtures.article(1L);
        given(learningHistoryRepository.existsByUserIdAndTypeAndReferenceId(any(), any(), any()))
                .willReturn(false);
        given(userRepository.findById(7L)).willReturn(Optional.of(user));
        given(articleMetaRepository.findById(1L)).willReturn(Optional.of(article));

        learningHistoryService.recordArticleRead(new ArticleReadCompletedEvent(7L, 1L, LocalDateTime.now()));
        learningHistoryService.recordReview(new ReviewCompletedEvent(7L, 1L, 3L, LocalDateTime.now()));
        learningHistoryService.recordQuiz(new QuizCompletedEvent(7L, 1L, 10L, true, LocalDateTime.now()));

        then(learningHistoryRepository).should(org.mockito.Mockito.times(3)).saveAndFlush(any(LearningHistory.class));
    }

    @Test
    void recordArticleRead_skipsWhenAlreadyExists() {
        given(learningHistoryRepository.existsByUserIdAndTypeAndReferenceId(
                7L,
                LearningHistoryType.ARTICLE_READ,
                1L
        )).willReturn(true);

        learningHistoryService.recordArticleRead(new ArticleReadCompletedEvent(7L, 1L, LocalDateTime.now()));

        then(learningHistoryRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void getHistory_groupsTimelineAndCountsTypes() {
        User user = TestFixtures.user(7L);
        ArticleMeta article = TestFixtures.article(1L);
        LocalDateTime learnedAt = LocalDateTime.of(2026, 6, 24, 10, 0);
        List<LearningHistory> histories = List.of(
                LearningHistory.articleRead(user, article, 1L, learnedAt),
                LearningHistory.review(user, article, 2L, learnedAt.plusMinutes(1)),
                LearningHistory.quiz(user, article, 3L, true, learnedAt.plusMinutes(2))
        );
        given(learningHistoryRepository.findAllByUserIdAndLearningDateBetweenOrderByLearningDateDescLearnedAtDesc(
                7L,
                learnedAt.toLocalDate(),
                learnedAt.toLocalDate()
        )).willReturn(histories);

        LearningHistoryResponse response = learningHistoryService.getHistory(
                7L,
                learnedAt.toLocalDate(),
                learnedAt.toLocalDate()
        );

        assertThat(response.days()).hasSize(1);
        assertThat(response.totalArticleReadCount()).isEqualTo(1);
        assertThat(response.totalReviewCount()).isEqualTo(1);
        assertThat(response.totalQuizCount()).isEqualTo(1);
    }

    @Test
    void getHistory_rejectsInvalidDateRange() {
        assertThatThrownBy(() -> learningHistoryService.getHistory(
                7L,
                LocalDate.of(2026, 6, 25),
                LocalDate.of(2026, 6, 24)
        ))
                .isInstanceOf(CustomException.class)
                .satisfies(error -> assertThat(((CustomException) error).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_INPUT_VALUE));
    }

    @Test
    void getStats_calculatesAccuracyAndStreak() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);
        given(learningHistoryRepository.countDistinctArticleIdByUserIdAndType(7L, LearningHistoryType.ARTICLE_READ))
                .willReturn(3L);
        given(learningHistoryRepository.countByUserIdAndType(7L, LearningHistoryType.REVIEW)).willReturn(2L);
        given(learningHistoryRepository.countByUserIdAndType(7L, LearningHistoryType.QUIZ)).willReturn(4L);
        given(learningHistoryRepository.countByUserIdAndTypeAndQuizCorrectTrue(7L, LearningHistoryType.QUIZ))
                .willReturn(3L);
        given(learningHistoryRepository.countDistinctLearningDateByUserIdAndLearningDateBetween(
                7L,
                weekStart,
                weekEnd
        )).willReturn(5L);
        given(learningHistoryRepository.findDistinctLearningDatesByUserIdOrderByDesc(7L))
                .willReturn(List.of(today, today.minusDays(1), today.minusDays(2), today.minusDays(4)));

        LearningStatsResponse response = learningHistoryService.getStats(7L);

        assertThat(response.totalReadArticleCount()).isEqualTo(3L);
        assertThat(response.quizAccuracyRate()).isEqualTo(75.0);
        assertThat(response.consecutiveLearningDays()).isEqualTo(3);
        assertThat(response.weeklyLearningDays()).isEqualTo(5L);
    }
}
