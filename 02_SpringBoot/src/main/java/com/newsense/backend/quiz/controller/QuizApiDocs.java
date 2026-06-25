package com.newsense.backend.quiz.controller;

import com.newsense.backend.auth.security.UserPrincipal;
import com.newsense.backend.common.response.ApiResponse;
import com.newsense.backend.quiz.dto.QuizAnswerRequest;
import com.newsense.backend.quiz.dto.QuizAnswerResponse;
import com.newsense.backend.quiz.dto.QuizResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Quiz", description = "기사 기반 OX 및 객관식 퀴즈")
public interface QuizApiDocs {

    @Operation(
            summary = "기사 퀴즈 조회",
            description = "저장된 퀴즈가 없으면 기사 본문을 기반으로 생성합니다. 정답과 해설은 노출하지 않습니다."
    )
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/api/v1/articles/{articleId}/quiz")
    ResponseEntity<ApiResponse<List<QuizResponse>>> getArticleQuizzes(
            @PathVariable Long articleId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    );

    @Operation(
            summary = "퀴즈 답변 제출 및 채점",
            description = "사용자 답안을 저장하고 정오답, 실제 정답, 해설을 반환합니다. 오답은 오답노트에 자동 누적됩니다."
    )
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/api/v1/quiz/{quizId}/answer")
    ResponseEntity<ApiResponse<QuizAnswerResponse>> submitAnswer(
            @PathVariable Long quizId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody QuizAnswerRequest request
    );
}
