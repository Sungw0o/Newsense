package com.newsense.backend.learning.service;

import com.newsense.backend.article.domain.ArticleMeta;
import com.newsense.backend.article.domain.ArticleRead;
import com.newsense.backend.article.dto.ArticleCardResponse;
import com.newsense.backend.article.event.ArticleReadCompletedEvent;
import com.newsense.backend.article.repository.ArticleMetaRepository;
import com.newsense.backend.article.repository.ArticleReadRepository;
import com.newsense.backend.bookmark.repository.BookmarkRepository;
import com.newsense.backend.common.exception.CustomException;
import com.newsense.backend.common.exception.ErrorCode;
import com.newsense.backend.learning.domain.LearningHistory;
import com.newsense.backend.learning.domain.LearningHistoryType;
import com.newsense.backend.learning.dto.LearningDailyHistoryResponse;
import com.newsense.backend.learning.dto.LearningHistoryResponse;
import com.newsense.backend.learning.dto.LearningStatsResponse;
import com.newsense.backend.learning.dto.LearningTimelineItemResponse;
import com.newsense.backend.learning.repository.LearningHistoryRepository;
import com.newsense.backend.quiz.domain.QuizAnswer;
import com.newsense.backend.quiz.event.QuizCompletedEvent;
import com.newsense.backend.quiz.repository.QuizAnswerRepository;
import com.newsense.backend.review.domain.Review;
import com.newsense.backend.review.event.ReviewCompletedEvent;
import com.newsense.backend.review.repository.ReviewRepository;
import com.newsense.backend.user.domain.User;
import com.newsense.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class LearningHistoryService {

    private static final int DEFAULT_HISTORY_DAYS = 30;

    private final LearningHistoryRepository learningHistoryRepository;
    private final ArticleMetaRepository articleMetaRepository;
    private final ArticleReadRepository articleReadRepository;
    private final ReviewRepository reviewRepository;
    private final QuizAnswerRepository quizAnswerRepository;
    private final UserRepository userRepository;
    private final BookmarkRepository bookmarkRepository;

    @Transactional
    public void recordArticleRead(ArticleReadCompletedEvent event) {
        saveIfAbsent(
                event.userId(),
                LearningHistoryType.ARTICLE_READ,
                event.articleId(),
                () -> {
                    User user = getUser(event.userId());
                    ArticleMeta article = getArticle(event.articleId());
                    return LearningHistory.articleRead(user, article, event.articleId(), event.readAt());
                }
        );
    }

    @Transactional
    public void recordReview(ReviewCompletedEvent event) {
        saveIfAbsent(
                event.userId(),
                LearningHistoryType.REVIEW,
                event.reviewId(),
                () -> {
                    User user = getUser(event.userId());
                    ArticleMeta article = getArticle(event.articleId());
                    return LearningHistory.review(user, article, event.reviewId(), event.completedAt());
                }
        );
    }

    @Transactional
    public void recordQuiz(QuizCompletedEvent event) {
        saveIfAbsent(
                event.userId(),
                LearningHistoryType.QUIZ,
                event.quizId(),
                () -> {
                    User user = getUser(event.userId());
                    ArticleMeta article = getArticle(event.articleId());
                    return LearningHistory.quiz(
                            user,
                            article,
                            event.quizId(),
                            event.correct(),
                            event.completedAt()
                    );
                }
        );
    }

    @Transactional
    public LearningHistoryResponse getHistory(Long userId, LocalDate startDate, LocalDate endDate) {
        syncMissingHistories(userId);

        LocalDate resolvedEndDate = endDate == null ? LocalDate.now() : endDate;
        LocalDate resolvedStartDate = startDate == null
                ? resolvedEndDate.minusDays(DEFAULT_HISTORY_DAYS - 1L)
                : startDate;
        validateDateRange(resolvedStartDate, resolvedEndDate);

        List<LearningHistory> histories = learningHistoryRepository
                .findAllByUserIdAndLearningDateBetweenOrderByLearningDateDescLearnedAtDesc(
                        userId,
                        resolvedStartDate,
                        resolvedEndDate
                );
        Map<LocalDate, List<LearningTimelineItemResponse>> grouped = new LinkedHashMap<>();
        histories.forEach(history -> grouped
                .computeIfAbsent(history.getLearningDate(), key -> new ArrayList<>())
                .add(LearningTimelineItemResponse.from(history, findReviewForHistory(history, userId))));

        List<LearningDailyHistoryResponse> days = grouped.entrySet().stream()
                .map(entry -> LearningDailyHistoryResponse.of(entry.getKey(), entry.getValue()))
                .toList();

        return LearningHistoryResponse.of(resolvedStartDate, resolvedEndDate, days);
    }

    @Transactional
    public LearningStatsResponse getStats(Long userId) {
        syncMissingHistories(userId);

        long totalReadArticleCount = learningHistoryRepository.countDistinctArticleIdByUserIdAndType(
                userId,
                LearningHistoryType.ARTICLE_READ
        );
        long totalReviewCount = learningHistoryRepository.countByUserIdAndType(userId, LearningHistoryType.REVIEW);
        long totalQuizCount = learningHistoryRepository.countByUserIdAndType(userId, LearningHistoryType.QUIZ);
        long correctQuizCount = learningHistoryRepository.countByUserIdAndTypeAndQuizCorrectTrue(
                userId,
                LearningHistoryType.QUIZ
        );

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);
        long weeklyLearningDays = learningHistoryRepository.countDistinctLearningDateByUserIdAndLearningDateBetween(
                userId,
                weekStart,
                weekEnd
        );

        return new LearningStatsResponse(
                totalReadArticleCount,
                totalReviewCount,
                totalQuizCount,
                correctQuizCount,
                calculateAccuracyRate(totalQuizCount, correctQuizCount),
                calculateConsecutiveLearningDays(userId),
                weeklyLearningDays
        );
    }

    private void syncMissingHistories(Long userId) {
        User user = getUser(userId);
        syncArticleReadHistories(user);
        syncReviewHistories(user);
        syncQuizHistories(user);
    }

    private void syncArticleReadHistories(User user) {
        List<ArticleRead> articleReads = articleReadRepository.findAllByUserId(user.getId());
        articleReads.forEach(articleRead -> saveIfAbsent(
                user.getId(),
                LearningHistoryType.ARTICLE_READ,
                articleRead.getArticle().getId(),
                () -> LearningHistory.articleRead(
                        user,
                        articleRead.getArticle(),
                        articleRead.getArticle().getId(),
                        articleRead.getReadAt()
                )
        ));
    }

    private void syncReviewHistories(User user) {
        List<Review> reviews = reviewRepository.findAllByUserIdAndIsActiveTrue(user.getId());
        reviews.forEach(review -> saveIfAbsent(
                user.getId(),
                LearningHistoryType.REVIEW,
                review.getId(),
                () -> LearningHistory.review(user, review.getArticle(), review.getId(), review.getUpdatedAt())
        ));
    }

    private void syncQuizHistories(User user) {
        List<QuizAnswer> quizAnswers = quizAnswerRepository.findAllByUserId(user.getId());
        quizAnswers.forEach(quizAnswer -> saveIfAbsent(
                user.getId(),
                LearningHistoryType.QUIZ,
                quizAnswer.getQuiz().getId(),
                () -> LearningHistory.quiz(
                        user,
                        quizAnswer.getArticle(),
                        quizAnswer.getQuiz().getId(),
                        quizAnswer.isCorrect(),
                        quizAnswer.getSubmittedAt()
                )
        ));
    }

    private Review findReviewForHistory(LearningHistory history, Long userId) {
        if (history.getType() != LearningHistoryType.REVIEW) {
            return null;
        }
        return reviewRepository.findByIdAndUserIdAndIsActiveTrue(history.getReferenceId(), userId)
                .orElse(null);
    }

    private double calculateAccuracyRate(long totalQuizCount, long correctQuizCount) {
        if (totalQuizCount == 0) {
            return 0.0;
        }
        return BigDecimal.valueOf(correctQuizCount * 100.0 / totalQuizCount)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private int calculateConsecutiveLearningDays(Long userId) {
        List<LocalDate> learningDates = learningHistoryRepository.findDistinctLearningDatesByUserIdOrderByDesc(userId);
        if (learningDates.isEmpty()) {
            return 0;
        }

        int streak = 1;
        LocalDate expectedDate = learningDates.get(0).minusDays(1);
        for (int index = 1; index < learningDates.size(); index++) {
            if (!learningDates.get(index).equals(expectedDate)) {
                break;
            }
            streak++;
            expectedDate = expectedDate.minusDays(1);
        }
        return streak;
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private void saveIfAbsent(
            Long userId,
            LearningHistoryType type,
            Long referenceId,
            Supplier<LearningHistory> historySupplier
    ) {
        if (learningHistoryRepository.existsByUserIdAndTypeAndReferenceId(userId, type, referenceId)) {
            return;
        }

        try {
            learningHistoryRepository.saveAndFlush(historySupplier.get());
        } catch (DataIntegrityViolationException exception) {
            if (learningHistoryRepository.existsByUserIdAndTypeAndReferenceId(userId, type, referenceId)) {
                log.info(
                        "Learning history already exists. userId={}, type={}, referenceId={}",
                        userId,
                        type,
                        referenceId
                );
                return;
            }
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public List<ArticleCardResponse> getBookmarks(Long userId) {
        return bookmarkRepository.findAllByUserIdWithArticle(userId).stream()
                .map(bookmark -> ArticleCardResponse.from(bookmark.getArticle()))
                .toList();
    }

    private ArticleMeta getArticle(Long articleId) {
        return articleMetaRepository.findById(articleId)
                .orElseThrow(() -> new CustomException(ErrorCode.ARTICLE_NOT_FOUND));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));
    }
}
