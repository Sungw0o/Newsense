package com.newsense.backend.article.controller;

import com.newsense.backend.article.dto.ArticleDetailResponse;
import com.newsense.backend.article.dto.ArticleReadResponse;
import com.newsense.backend.article.dto.ArticleTermResponse;
import com.newsense.backend.article.dto.BookmarkToggleResponse;
import com.newsense.backend.auth.security.UserPrincipal;
import com.newsense.backend.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Tag(name = "Article Detail", description = "기사 상세, 경제 용어, 읽음 및 북마크 관리")
public interface ArticleDetailApiDocs {

    @Operation(summary = "기사 상세 조회", description = "MySQL 기사 메타데이터와 MongoDB 본문을 병합해 반환합니다.")
    @GetMapping("/api/v1/articles/{articleId}")
    ResponseEntity<ApiResponse<ArticleDetailResponse>> getArticleDetail(
            @PathVariable Long articleId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    );

    @Operation(summary = "기사 핵심 경제 용어 조회")
    @GetMapping("/api/v1/articles/{articleId}/terms")
    ResponseEntity<ApiResponse<List<ArticleTermResponse>>> getArticleTerms(@PathVariable Long articleId);

    @Operation(summary = "기사 읽음 처리")
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/api/v1/articles/{articleId}/read")
    ResponseEntity<ApiResponse<ArticleReadResponse>> markAsRead(
            @PathVariable Long articleId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    );

    @Operation(summary = "기사 북마크 토글")
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/api/v1/articles/{articleId}/bookmark")
    ResponseEntity<ApiResponse<BookmarkToggleResponse>> toggleBookmark(
            @PathVariable Long articleId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    );
}
