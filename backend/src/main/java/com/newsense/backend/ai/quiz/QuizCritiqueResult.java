package com.newsense.backend.ai.quiz;

import java.util.List;

public record QuizCritiqueResult(
        boolean approved,
        List<String> issues
) {
    public static QuizCritiqueResult approvedResult() {
        return new QuizCritiqueResult(true, List.of());
    }

    public String feedback() {
        if (issues == null || issues.isEmpty()) {
            return "구체적인 검증 피드백 없음";
        }
        return String.join("\n", issues);
    }
}
