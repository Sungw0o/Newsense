package com.newsense.backend.wrongnote.controller;

import com.newsense.backend.article.domain.ArticleCategory;
import com.newsense.backend.auth.security.UserPrincipal;
import com.newsense.backend.common.response.ApiResponse;
import com.newsense.backend.wrongnote.dto.WrongNotePageResponse;
import com.newsense.backend.wrongnote.dto.WrongNoteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Wrong Note", description = "오답노트 조회 및 복습 상태 관리")
@SecurityRequirement(name = "BearerAuth")
public interface WrongNoteApiDocs {

    @Operation(summary = "내 오답노트 목록 조회")
    @GetMapping("/api/v1/wrong-notes")
    ResponseEntity<ApiResponse<WrongNotePageResponse>> getWrongNotes(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) ArticleCategory category,
            @RequestParam(required = false) Boolean isResolved,
            @Parameter(description = "Zero-based page number")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size (1-100)")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    );

    @Operation(summary = "오답 해결 상태 토글")
    @PatchMapping("/api/v1/wrong-notes/{wrongNoteId}/resolve")
    ResponseEntity<ApiResponse<WrongNoteResponse>> toggleResolved(
            @PathVariable Long wrongNoteId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    );

    @Operation(summary = "오답노트 삭제", description = "오답노트를 소프트 딜리트합니다.")
    @DeleteMapping("/api/v1/wrong-notes/{wrongNoteId}")
    ResponseEntity<ApiResponse<Void>> deleteWrongNote(
            @PathVariable Long wrongNoteId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    );
}
