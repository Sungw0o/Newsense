package com.newsense.backend.learning.controller;

import com.newsense.backend.article.dto.ArticleCardResponse;
import com.newsense.backend.auth.security.UserPrincipal;
import com.newsense.backend.common.response.ApiResponse;
import com.newsense.backend.learning.dto.LearningHistoryResponse;
import com.newsense.backend.learning.dto.LearningStatsResponse;
import com.newsense.backend.learning.dto.WeaknessSummaryResponse;
import com.newsense.backend.learning.service.LearningHistoryService;
import com.newsense.backend.learning.service.UserWeaknessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class LearningController implements LearningApiDocs {

    private final LearningHistoryService learningHistoryService;
    private final UserWeaknessService userWeaknessService;

    @Override
    public ResponseEntity<ApiResponse<LearningHistoryResponse>> getHistory(
            UserPrincipal userPrincipal,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "학습 이력 조회에 성공했습니다.",
                learningHistoryService.getHistory(userPrincipal.id(), startDate, endDate)
        ));
    }

    @Override
    public ResponseEntity<ApiResponse<LearningStatsResponse>> getStats(UserPrincipal userPrincipal) {
        return ResponseEntity.ok(ApiResponse.success(
                "학습 통계 조회에 성공했습니다.",
                learningHistoryService.getStats(userPrincipal.id())
        ));
    }

    @Override
    public ResponseEntity<ApiResponse<List<ArticleCardResponse>>> getBookmarks(UserPrincipal userPrincipal) {
        return ResponseEntity.ok(ApiResponse.success(
                "북마크된 기사 목록 조회에 성공했습니다.",
                learningHistoryService.getBookmarks(userPrincipal.id())
        ));
    }

    @Override
    public ResponseEntity<ApiResponse<WeaknessSummaryResponse>> getWeaknessSummary(UserPrincipal userPrincipal) {
        return ResponseEntity.ok(ApiResponse.success(
                "취약점 요약 조회에 성공했습니다.",
                userWeaknessService.getWeaknessSummary(userPrincipal.id())
        ));
    }

    @Override
    public ResponseEntity<ApiResponse<WeaknessSummaryResponse>> getWeakness(UserPrincipal userPrincipal) {
        return ResponseEntity.ok(ApiResponse.success(
                "취약 개념 분석 조회에 성공했습니다.",
                userWeaknessService.getWeaknessSummary(userPrincipal.id())
        ));
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> deleteHistory(UserPrincipal userPrincipal, Long historyId) {
        learningHistoryService.deleteHistory(userPrincipal.id(), historyId);
        return ResponseEntity.ok(ApiResponse.success("학습 이력이 삭제되었습니다.", null));
    }
}
