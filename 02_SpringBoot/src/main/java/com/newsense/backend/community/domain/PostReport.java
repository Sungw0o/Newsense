package com.newsense.backend.community.domain;

import com.newsense.backend.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "post_report",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_post_report_post_reporter",
                columnNames = {"post_id", "reporter_id"}
        ),
        indexes = @Index(name = "idx_post_report_post_id", columnList = "post_id")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @Column(length = 500)
    private String reason;

    @Column(name = "reported_at", nullable = false)
    private LocalDateTime reportedAt;

    public static PostReport create(Post post, User reporter, String reason) {
        PostReport report = new PostReport();
        report.post = post;
        report.reporter = reporter;
        report.reason = reason;
        report.reportedAt = LocalDateTime.now();
        return report;
    }
}
