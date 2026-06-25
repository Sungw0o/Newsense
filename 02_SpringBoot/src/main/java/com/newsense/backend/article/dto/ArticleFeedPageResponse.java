package com.newsense.backend.article.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record ArticleFeedPageResponse(
        List<ArticleCardResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    public static ArticleFeedPageResponse from(Page<ArticleCardResponse> articles) {
        return new ArticleFeedPageResponse(
                articles.getContent(),
                articles.getNumber(),
                articles.getSize(),
                articles.getTotalElements(),
                articles.getTotalPages(),
                articles.isFirst(),
                articles.isLast()
        );
    }
}
