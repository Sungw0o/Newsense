package com.newsense.backend.inquiry.repository;

import com.newsense.backend.inquiry.domain.Inquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    @Query("SELECT i FROM Inquiry i LEFT JOIN FETCH i.user ORDER BY i.createdAt DESC")
    Page<Inquiry> findAllWithUser(Pageable pageable);

    @Query("SELECT i FROM Inquiry i LEFT JOIN FETCH i.user WHERE i.user.id = :userId ORDER BY i.createdAt DESC")
    Page<Inquiry> findByUserId(@Param("userId") Long userId, Pageable pageable);
}
