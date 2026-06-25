package com.newsense.backend.community.repository;

import com.newsense.backend.community.domain.PostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    void deleteAllByPostId(Long postId);

    Page<PostComment> findByPostId(Long postId, Pageable pageable);

    long countByPostId(Long postId);
}
