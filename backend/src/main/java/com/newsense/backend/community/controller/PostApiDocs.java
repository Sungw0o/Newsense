package com.newsense.backend.community.controller;

import com.newsense.backend.auth.security.UserPrincipal;
import com.newsense.backend.common.response.ApiResponse;
import com.newsense.backend.community.domain.PostType;
import com.newsense.backend.community.dto.CommentCreateRequest;
import com.newsense.backend.community.dto.CommentResponse;
import com.newsense.backend.community.dto.PostCreateRequest;
import com.newsense.backend.community.dto.PostReactionResponse;
import com.newsense.backend.community.dto.PostReportRequest;
import com.newsense.backend.community.dto.PostResponse;
import com.newsense.backend.community.dto.PostSort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Community", description = "커뮤니티 게시글 및 댓글")
public interface PostApiDocs {

    @Operation(summary = "게시글 등록")
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/api/v1/posts")
    ResponseEntity<ApiResponse<PostResponse>> createPost(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody PostCreateRequest request
    );

    @Operation(summary = "전체 게시글 페이징 조회 (키워드 검색 및 타입 필터 포함)")
    @GetMapping("/api/v1/posts")
    ResponseEntity<ApiResponse<Page<PostResponse>>> getPosts(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(defaultValue = "LATEST") PostSort sort,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) PostType type
    );

    @Operation(summary = "공지사항 목록 조회 (최신 10건)")
    @GetMapping("/api/v1/posts/notices")
    ResponseEntity<ApiResponse<List<PostResponse>>> getNotices();

    @Operation(summary = "게시글 단건 조회")
    @GetMapping("/api/v1/posts/{postId}")
    ResponseEntity<ApiResponse<PostResponse>> getPost(@PathVariable Long postId);

    @Operation(summary = "게시글 좋아요 토글")
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/api/v1/posts/{postId}/like")
    ResponseEntity<ApiResponse<PostReactionResponse>> toggleLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    );

    @Operation(summary = "게시글 싫어요 토글")
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/api/v1/posts/{postId}/dislike")
    ResponseEntity<ApiResponse<PostReactionResponse>> toggleDislike(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    );

    @Operation(summary = "댓글 등록")
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/api/v1/posts/{postId}/comments")
    ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CommentCreateRequest request
    );

    @Operation(summary = "댓글 페이징 조회")
    @GetMapping("/api/v1/posts/{postId}/comments")
    ResponseEntity<ApiResponse<Page<CommentResponse>>> getComments(
            @PathVariable Long postId,
            @PageableDefault(size = 20) Pageable pageable
    );

    @Operation(summary = "댓글 삭제")
    @SecurityRequirement(name = "BearerAuth")
    @DeleteMapping("/api/v1/comments/{commentId}")
    ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    );

    @Operation(summary = "게시글 신고")
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/api/v1/posts/{postId}/report")
    ResponseEntity<ApiResponse<Void>> reportPost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody PostReportRequest request
    );
}
