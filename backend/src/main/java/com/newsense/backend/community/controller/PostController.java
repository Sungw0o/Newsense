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
import com.newsense.backend.community.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PostController implements PostApiDocs {

    private final PostService postService;

    @Override
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            UserPrincipal userPrincipal,
            PostCreateRequest request
    ) {
        PostResponse response = postService.createPost(userPrincipal.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "게시글이 등록되었습니다.", response));
    }

    @Override
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getPosts(Pageable pageable, PostSort sort, String keyword, PostType type) {
        return ResponseEntity.ok(ApiResponse.success(
                "게시글 목록 조회에 성공했습니다.",
                postService.getPosts(pageable, sort, keyword, type)
        ));
    }

    @Override
    public ResponseEntity<ApiResponse<List<PostResponse>>> getNotices() {
        return ResponseEntity.ok(ApiResponse.success(
                "공지사항 조회에 성공했습니다.",
                postService.getNotices()
        ));
    }

    @Override
    public ResponseEntity<ApiResponse<PostResponse>> getPost(Long postId) {
        return ResponseEntity.ok(ApiResponse.success(
                "게시글 조회에 성공했습니다.",
                postService.getPost(postId)
        ));
    }

    @Override
    public ResponseEntity<ApiResponse<PostReactionResponse>> toggleLike(
            Long postId,
            UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "좋아요 상태가 변경되었습니다.",
                postService.toggleLike(postId, userPrincipal.id())
        ));
    }

    @Override
    public ResponseEntity<ApiResponse<PostReactionResponse>> toggleDislike(
            Long postId,
            UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "싫어요 상태가 변경되었습니다.",
                postService.toggleDislike(postId, userPrincipal.id())
        ));
    }

    @Override
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            Long postId,
            UserPrincipal userPrincipal,
            CommentCreateRequest request
    ) {
        CommentResponse response = postService.createComment(postId, userPrincipal.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "댓글이 등록되었습니다.", response));
    }

    @Override
    public ResponseEntity<ApiResponse<Page<CommentResponse>>> getComments(Long postId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                "댓글 목록 조회에 성공했습니다.",
                postService.getComments(postId, pageable)
        ));
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> deleteComment(Long commentId, UserPrincipal userPrincipal) {
        postService.deleteComment(commentId, userPrincipal.id());
        return ResponseEntity.ok(ApiResponse.success("댓글이 삭제되었습니다.", null));
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> reportPost(Long postId, UserPrincipal userPrincipal, PostReportRequest request) {
        postService.reportPost(postId, userPrincipal.id(), request);
        return ResponseEntity.ok(ApiResponse.success("신고가 접수되었습니다.", null));
    }
}
