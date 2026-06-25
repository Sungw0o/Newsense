package com.newsense.backend.article.controller;

import com.newsense.backend.article.dto.ArticleCardResponse;
import com.newsense.backend.article.service.ArticleRecommendationService;
import com.newsense.backend.auth.security.UserPrincipal;
import com.newsense.backend.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ArticleRecommendationController implements ArticleRecommendationApiDocs {

    private final ArticleRecommendationService recommendationService;

    @Override
    public ResponseEntity<ApiResponse<List<ArticleCardResponse>>> recommend(
            UserPrincipal userPrincipal, int limit) {
        return ResponseEntity.ok(ApiResponse.success(
                "기사 추천 목록 조회에 성공했습니다.",
                recommendationService.recommend(userPrincipal.id(), limit)
        ));
    }
}
