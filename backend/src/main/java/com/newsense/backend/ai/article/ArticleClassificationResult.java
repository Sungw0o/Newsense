package com.newsense.backend.ai.article;

import com.newsense.backend.article.domain.ArticleCategory;
import com.newsense.backend.article.domain.ArticleDifficulty;

public record ArticleClassificationResult(
        ArticleCategory category,
        ArticleDifficulty difficulty,
        String summary
) {
}
