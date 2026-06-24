package com.newsense.backend.article.controller;

import com.newsense.backend.article.dto.ArticleDetailResponse;
import com.newsense.backend.article.dto.ArticleReadResponse;
import com.newsense.backend.article.dto.ArticleTermResponse;
import com.newsense.backend.article.dto.BookmarkToggleResponse;
import com.newsense.backend.article.service.ArticleDetailService;
import com.newsense.backend.auth.security.UserPrincipal;
import com.newsense.backend.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ArticleDetailController implements ArticleDetailApiDocs {

    private final ArticleDetailService articleDetailService;

    @Override
    public ResponseEntity<ApiResponse<ArticleDetailResponse>> getArticleDetail(
            Long articleId,
            UserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal == null ? null : userPrincipal.id();
        return ResponseEntity.ok(ApiResponse.success(
                "기사 상세 조회에 성공했습니다.",
                articleDetailService.getArticleDetail(articleId, userId)
        ));
    }

    @Override
    public ResponseEntity<ApiResponse<List<ArticleTermResponse>>> getArticleTerms(Long articleId) {
        return ResponseEntity.ok(ApiResponse.success(
                "기사 경제 용어 조회에 성공했습니다.",
                articleDetailService.getArticleTerms(articleId)
        ));
    }

    @Override
    public ResponseEntity<ApiResponse<ArticleReadResponse>> markAsRead(
            Long articleId,
            UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "기사 읽음 처리가 완료되었습니다.",
                articleDetailService.markAsRead(articleId, userPrincipal.id())
        ));
    }

    @Override
    public ResponseEntity<ApiResponse<BookmarkToggleResponse>> toggleBookmark(
            Long articleId,
            UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "북마크 상태가 변경되었습니다.",
                articleDetailService.toggleBookmark(articleId, userPrincipal.id())
        ));
    }
}
