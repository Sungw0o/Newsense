package com.newsense.backend.review.repository;

import com.newsense.backend.review.domain.Review;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @EntityGraph(attributePaths = "difficultTerms")
    Optional<Review> findByArticleIdAndUserId(Long articleId, Long userId);

    @EntityGraph(attributePaths = "difficultTerms")
    Optional<Review> findByArticleIdAndUserIdAndIsActiveTrue(Long articleId, Long userId);

    @EntityGraph(attributePaths = "difficultTerms")
    Optional<Review> findByIdAndUserIdAndIsActiveTrue(Long reviewId, Long userId);
}
