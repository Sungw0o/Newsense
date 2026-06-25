package com.newsense.backend.learning.controller;

import com.newsense.backend.article.dto.ArticleCardResponse;
import com.newsense.backend.auth.security.UserPrincipal;
import com.newsense.backend.common.response.ApiResponse;
import com.newsense.backend.learning.dto.LearningHistoryResponse;
import com.newsense.backend.learning.dto.LearningStatsResponse;
import com.newsense.backend.learning.service.LearningHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class LearningController implements LearningApiDocs {

    private final LearningHistoryService learningHistoryService;

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
                "북마크 목록 조회에 성공했습니다.",
                learningHistoryService.getBookmarks(userPrincipal.id())
        ));
    }
}
