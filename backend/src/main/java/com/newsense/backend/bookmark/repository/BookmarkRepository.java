package com.newsense.backend.bookmark.repository;

import com.newsense.backend.bookmark.domain.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    boolean existsByUserIdAndArticleId(Long userId, Long articleId);

    Optional<Bookmark> findByUserIdAndArticleId(Long userId, Long articleId);
}
