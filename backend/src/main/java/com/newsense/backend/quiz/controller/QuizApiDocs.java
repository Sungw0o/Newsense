package com.newsense.backend.quiz.controller;

import com.newsense.backend.common.response.ApiResponse;
import com.newsense.backend.quiz.dto.QuizResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Tag(name = "Quiz", description = "기사 기반 OX 및 객관식 퀴즈")
public interface QuizApiDocs {

    @Operation(
            summary = "기사 퀴즈 조회",
            description = "저장된 퀴즈가 없으면 기사 본문을 기반으로 생성합니다. 정답과 해설은 노출하지 않습니다."
    )
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/api/v1/articles/{articleId}/quiz")
    ResponseEntity<ApiResponse<List<QuizResponse>>> getArticleQuizzes(@PathVariable Long articleId);
}
