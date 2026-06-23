package com.newsense.backend.rag.dto;

public record RagMatchedChunkResponse(
        int chunkIndex,
        String snippet,
        int score
) {
}
