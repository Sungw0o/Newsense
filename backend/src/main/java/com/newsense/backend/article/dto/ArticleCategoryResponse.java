package com.newsense.backend.article.dto;

import com.newsense.backend.article.domain.ArticleCategory;

public record ArticleCategoryResponse(
        ArticleCategory categoryId,
        String name,
        long articleCount
) {
}
