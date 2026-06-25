package com.newsense.backend.article.controller;

import com.newsense.backend.auth.security.UserPrincipal;
import com.newsense.backend.article.dto.ArticleCardResponse;
import com.newsense.backend.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Article Recommendation", description = "개인화 기사 추천")
@SecurityRequirement(name = "BearerAuth")
public interface ArticleRecommendationApiDocs {

    @Operation(
            summary = "개인화 기사 추천",
            description = "관심 카테고리 + 취약 개념 기반으로 아직 읽지 않은 기사를 추천합니다. (최대 20개)"
    )
    @GetMapping("/api/v1/articles/recommendations")
    ResponseEntity<ApiResponse<List<ArticleCardResponse>>> recommend(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "10") int limit
    );
}
