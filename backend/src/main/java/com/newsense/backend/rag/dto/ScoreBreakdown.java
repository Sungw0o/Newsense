package com.newsense.backend.rag.dto;

public record ScoreBreakdown(
        double vectorScore,
        double keywordScore,
        double recencyScore,
        double categoryScore,
        double weaknessScore,
        double marketScore,
        double finalScore
) {
}
