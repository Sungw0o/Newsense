package com.newsense.backend.community.repository;

import com.newsense.backend.community.domain.PostReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PostReportRepository extends JpaRepository<PostReport, Long> {

    boolean existsByPostIdAndReporterId(Long postId, Long reporterId);

    @Query("select r from PostReport r join fetch r.post join fetch r.reporter order by r.reportedAt desc")
    Page<PostReport> findAllWithDetails(Pageable pageable);
}
