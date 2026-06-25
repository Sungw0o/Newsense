package com.newsense.backend.rag.controller;

import com.newsense.backend.auth.security.UserPrincipal;
import com.newsense.backend.common.response.ApiResponse;
import com.newsense.backend.rag.dto.RagRecommendationResponse;
import com.newsense.backend.rag.dto.RagSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "RAG", description = "기사 청크 검색 및 취약 개념 기반 추천")
public interface RagApiDocs {

    @Operation(summary = "기사 청크 기반 검색", description = "MongoDB에 저장된 기사 청크를 검색해 관련 기사와 근거 문단을 반환합니다.")
    @GetMapping("/api/v1/rag/search")
    ResponseEntity<ApiResponse<RagSearchResponse>> search(
            @Parameter(description = "검색어 또는 경제 개념")
            @RequestParam @Size(min = 2, max = 100) String query,
            @Parameter(description = "결과 개수 (1-20)")
            @RequestParam(defaultValue = "5") @Min(1) @Max(20) int limit
    );

    @Operation(summary = "내 취약 개념 기반 기사 추천", description = "오답노트에 쌓인 경제 용어를 검색 질의로 사용해 복습용 기사를 추천합니다.")
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/api/v1/rag/recommendations")
    ResponseEntity<ApiResponse<RagRecommendationResponse>> recommend(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "추천 개수 (1-20)")
            @RequestParam(defaultValue = "5") @Min(1) @Max(20) int limit
    );
}
