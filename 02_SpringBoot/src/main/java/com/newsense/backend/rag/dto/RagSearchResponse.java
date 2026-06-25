package com.newsense.backend.rag.dto;

import java.util.List;

public record RagSearchResponse(
        String query,
        List<String> keywords,
        List<RagArticleResultResponse> results
) {
}
