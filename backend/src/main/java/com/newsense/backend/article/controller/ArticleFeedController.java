package com.newsense.backend.article.controller;

import com.newsense.backend.article.domain.ArticleCategory;
import com.newsense.backend.article.domain.ArticleDifficulty;
import com.newsense.backend.article.dto.ArticleCategoryResponse;
import com.newsense.backend.article.dto.ArticleFeedPageResponse;
import com.newsense.backend.article.dto.ArticleFeedSort;
import com.newsense.backend.article.service.ArticleFeedService;
import com.newsense.backend.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ArticleFeedController implements ArticleFeedApiDocs {

    private final ArticleFeedService articleFeedService;

    @Override
    public ResponseEntity<ApiResponse<ArticleFeedPageResponse>> getArticles(
            ArticleCategory category,
            ArticleDifficulty difficulty,
            String keyword,
            int page,
            int size,
            ArticleFeedSort sort) {
        return ResponseEntity.ok(ApiResponse.success(
                articleFeedService.getArticles(category, difficulty, keyword, page, size, sort)));
    }

    @Override
    public ResponseEntity<ApiResponse<List<ArticleCategoryResponse>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.success(articleFeedService.getCategories()));
    }
}
