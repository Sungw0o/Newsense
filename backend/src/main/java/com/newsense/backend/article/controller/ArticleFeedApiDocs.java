package com.newsense.backend.article.controller;

import com.newsense.backend.article.domain.ArticleCategory;
import com.newsense.backend.article.domain.ArticleDifficulty;
import com.newsense.backend.article.dto.ArticleCategoryResponse;
import com.newsense.backend.article.dto.ArticleFeedPageResponse;
import com.newsense.backend.article.dto.ArticleFeedSort;
import com.newsense.backend.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Article Feed", description = "Economic news feed")
public interface ArticleFeedApiDocs {

    @Operation(summary = "Get article feed", description = "Returns articles filtered by category and difficulty.")
    @GetMapping("/api/v1/articles")
    ResponseEntity<ApiResponse<ArticleFeedPageResponse>> getArticles(
            @Parameter(description = "Article category")
            @RequestParam(required = false) ArticleCategory category,
            @Parameter(description = "Article difficulty")
            @RequestParam(required = false) ArticleDifficulty difficulty,
            @Parameter(description = "Zero-based page number")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size (1-100)")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @Parameter(description = "Feed sort order")
            @RequestParam(defaultValue = "LATEST") ArticleFeedSort sort
    );

    @Operation(summary = "Get article categories", description = "Returns categories and article counts.")
    @GetMapping("/api/v1/categories")
    ResponseEntity<ApiResponse<List<ArticleCategoryResponse>>> getCategories();
}
