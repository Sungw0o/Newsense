package com.newsense.backend.community.dto;

public record PostReactionResponse(
        Long postId,
        int likes,
        int dislikes,
        boolean liked,
        boolean disliked
) {
}
