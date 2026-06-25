package com.newsense.backend.bookmark.repository;

import com.newsense.backend.bookmark.domain.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    boolean existsByUserIdAndArticleId(Long userId, Long articleId);

    Optional<Bookmark> findByUserIdAndArticleId(Long userId, Long articleId);

    @Query("select b from Bookmark b join fetch b.article where b.user.id = :userId order by b.createdAt desc")
    List<Bookmark> findAllByUserIdWithArticle(Long userId);
}
