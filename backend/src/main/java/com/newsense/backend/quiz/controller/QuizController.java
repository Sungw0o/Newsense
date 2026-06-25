package com.newsense.backend.quiz.controller;

import com.newsense.backend.common.response.ApiResponse;
import com.newsense.backend.auth.security.UserPrincipal;
import com.newsense.backend.quiz.dto.QuizAnswerRequest;
import com.newsense.backend.quiz.dto.QuizAnswerResponse;
import com.newsense.backend.quiz.dto.QuizResponse;
import com.newsense.backend.quiz.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class QuizController implements QuizApiDocs {

    private final QuizService quizService;

    @Override
    public ResponseEntity<ApiResponse<List<QuizResponse>>> getArticleQuizzes(Long articleId) {
        return ResponseEntity.ok(ApiResponse.success(
                "기사 퀴즈 조회에 성공했습니다.",
                quizService.getArticleQuizzes(articleId)
        ));
    }

    @Override
    public ResponseEntity<ApiResponse<QuizAnswerResponse>> submitAnswer(
            Long quizId,
            UserPrincipal userPrincipal,
            QuizAnswerRequest request
    ) {
        QuizAnswerResponse response = quizService.submitAnswer(quizId, userPrincipal.id(), request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "퀴즈 답변 제출이 완료되었습니다.", response));
    }
}
