package com.newsense.backend.quiz.controller;

import com.newsense.backend.common.response.ApiResponse;
import com.newsense.backend.quiz.dto.QuizResponse;
import com.newsense.backend.quiz.service.QuizService;
import lombok.RequiredArgsConstructor;
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
}
