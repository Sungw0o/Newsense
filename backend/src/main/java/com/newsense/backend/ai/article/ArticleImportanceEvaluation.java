package com.newsense.backend.ai.article;

public record ArticleImportanceEvaluation(
        boolean important,
        int importanceScore,
        String reason
) {
}
