package com.newsense.backend.ai.article;

import com.newsense.backend.article.domain.ArticleCategory;
import com.newsense.backend.article.domain.ArticleDifficulty;

import java.util.List;

public record ArticleClassificationResult(
        ArticleCategory category,
        ArticleDifficulty difficulty,
        String summary,
        List<RelatedStockInfo> relatedStocks
) {
    public ArticleClassificationResult(ArticleCategory category, ArticleDifficulty difficulty, String summary) {
        this(category, difficulty, summary, List.of());
    }
}
