package com.newsense.backend.learning.controller;

import com.newsense.backend.article.dto.ArticleCardResponse;
import com.newsense.backend.auth.security.UserPrincipal;
import com.newsense.backend.common.response.ApiResponse;
import com.newsense.backend.learning.dto.LearningHistoryResponse;
import com.newsense.backend.learning.dto.LearningStatsResponse;
import com.newsense.backend.learning.dto.WeaknessSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Learning", description = "사용자 학습 이력 및 누적 통계")
@SecurityRequirement(name = "BearerAuth")
public interface LearningApiDocs {

    @Operation(summary = "학습 이력 조회", description = "기간 내 기사 읽기, 리뷰 작성, 퀴즈 완료 이력을 날짜별 타임라인으로 조회합니다.")
    @GetMapping("/api/v1/learning/history")
    ResponseEntity<ApiResponse<LearningHistoryResponse>> getHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "조회 시작일. 미입력 시 종료일 기준 최근 30일입니다.")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @Parameter(description = "조회 종료일. 미입력 시 오늘입니다.")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    );

    @Operation(summary = "학습 통계 조회", description = "총 읽은 기사 수, 퀴즈 정답률, 연속 학습일, 주간 학습일수를 조회합니다.")
    @GetMapping("/api/v1/learning/stats")
    ResponseEntity<ApiResponse<LearningStatsResponse>> getStats(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    );

    @Operation(summary = "북마크된 기사 목록 조회", description = "사용자가 북마크한 기사 목록을 최신순으로 반환합니다.")
    @GetMapping("/api/v1/learning/bookmarks")
    ResponseEntity<ApiResponse<List<ArticleCardResponse>>> getBookmarks(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    );

    @Operation(summary = "취약점 요약 조회", description = "오답 노트를 기반으로 사용자의 취약 용어와 카테고리를 요약합니다.")
    @GetMapping("/api/v1/learning/weakness-summary")
    ResponseEntity<ApiResponse<WeaknessSummaryResponse>> getWeaknessSummary(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    );

    @Operation(summary = "취약 개념 분석", description = "오답 노트를 기반으로 취약 용어와 카테고리를 분석합니다.")
    @GetMapping("/api/v1/learning/weakness")
    ResponseEntity<ApiResponse<WeaknessSummaryResponse>> getWeakness(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    );

    @Operation(summary = "학습 이력 삭제", description = "현재 로그인한 사용자의 학습 이력을 삭제합니다.")
    @DeleteMapping("/api/v1/learning/history/{historyId}")
    ResponseEntity<ApiResponse<Void>> deleteHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long historyId
    );
}
