package com.newsense.backend.quiz.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record QuizAnswerRequest(
        @NotBlank(message = "답변은 필수입니다.")
        @Size(max = 500, message = "답변은 500자 이하여야 합니다.")
        String answer
) {
}
