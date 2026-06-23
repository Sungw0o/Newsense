package com.newsense.backend.learning.repository;

import com.newsense.backend.learning.domain.LearningHistory;
import com.newsense.backend.learning.domain.LearningHistoryType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LearningHistoryRepository extends JpaRepository<LearningHistory, Long> {

    boolean existsByUserIdAndTypeAndReferenceId(Long userId, LearningHistoryType type, Long referenceId);

    @EntityGraph(attributePaths = "article")
    List<LearningHistory> findAllByUserIdAndLearningDateBetweenOrderByLearningDateDescLearnedAtDesc(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );

    long countDistinctArticleIdByUserIdAndType(Long userId, LearningHistoryType type);

    long countByUserIdAndType(Long userId, LearningHistoryType type);

    long countByUserIdAndTypeAndQuizCorrectTrue(Long userId, LearningHistoryType type);

    long countDistinctLearningDateByUserIdAndLearningDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    @Query("""
            select distinct history.learningDate
            from LearningHistory history
            where history.user.id = :userId
            order by history.learningDate desc
            """)
    List<LocalDate> findDistinctLearningDatesByUserIdOrderByDesc(@Param("userId") Long userId);
}
