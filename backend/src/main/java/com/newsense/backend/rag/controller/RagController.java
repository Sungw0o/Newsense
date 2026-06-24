package com.newsense.backend.rag.controller;

import com.newsense.backend.auth.security.UserPrincipal;
import com.newsense.backend.common.response.ApiResponse;
import com.newsense.backend.rag.dto.RagRecommendationResponse;
import com.newsense.backend.rag.dto.RagSearchResponse;
import com.newsense.backend.rag.service.RagRetrievalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class RagController implements RagApiDocs {

    private final RagRetrievalService ragRetrievalService;

    @Override
    public ResponseEntity<ApiResponse<RagSearchResponse>> search(String query, int limit) {
        return ResponseEntity.ok(ApiResponse.success(
                "기사 검색에 성공했습니다.",
                ragRetrievalService.search(query, limit)
        ));
    }

    @Override
    public ResponseEntity<ApiResponse<RagRecommendationResponse>> recommend(UserPrincipal userPrincipal, int limit) {
        return ResponseEntity.ok(ApiResponse.success(
                "취약 개념 기반 기사 추천에 성공했습니다.",
                ragRetrievalService.recommendForWeakness(userPrincipal.id(), limit)
        ));
    }
}
