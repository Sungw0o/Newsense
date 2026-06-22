package com.newsense.backend.wrongnote.controller;

import com.newsense.backend.article.domain.ArticleCategory;
import com.newsense.backend.auth.security.UserPrincipal;
import com.newsense.backend.common.response.ApiResponse;
import com.newsense.backend.wrongnote.dto.WrongNotePageResponse;
import com.newsense.backend.wrongnote.dto.WrongNoteResponse;
import com.newsense.backend.wrongnote.service.WrongNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WrongNoteController implements WrongNoteApiDocs {

    private final WrongNoteService wrongNoteService;

    @Override
    public ResponseEntity<ApiResponse<WrongNotePageResponse>> getWrongNotes(
            UserPrincipal userPrincipal,
            ArticleCategory category,
            Boolean isResolved,
            int page,
            int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "오답노트 조회에 성공했습니다.",
                wrongNoteService.getWrongNotes(userPrincipal.id(), category, isResolved, page, size)
        ));
    }

    @Override
    public ResponseEntity<ApiResponse<WrongNoteResponse>> toggleResolved(
            Long wrongNoteId,
            UserPrincipal userPrincipal
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "오답 해결 상태가 변경되었습니다.",
                wrongNoteService.toggleResolved(wrongNoteId, userPrincipal.id())
        ));
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> deleteWrongNote(Long wrongNoteId, UserPrincipal userPrincipal) {
        wrongNoteService.deleteWrongNote(wrongNoteId, userPrincipal.id());
        return ResponseEntity.ok(ApiResponse.success("오답노트가 삭제되었습니다.", null));
    }
}
