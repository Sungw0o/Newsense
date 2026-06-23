package com.newsense.backend.rag.dto;

import java.util.List;

public record RagRecommendationResponse(
        List<String> weaknessTerms,
        List<RagArticleResultResponse> recommendations
) {
}
