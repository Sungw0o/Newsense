package com.newsense.backend.rag.dto;

import com.newsense.backend.article.dto.ArticleCardResponse;

import java.util.List;

public record RagArticleResultResponse(
        ArticleCardResponse article,
        List<RagMatchedChunkResponse> matchedChunks,
        List<String> matchedKeywords,
        int score,
        ScoreBreakdown scoreBre