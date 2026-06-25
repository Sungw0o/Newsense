package com.newsense.backend.article.dto;

import com.newsense.backend.article.domain.ArticleRelatedStock;

public record RelatedStockResponse(
        String stockName,
        String stockCode,
        String relationReason
) {
    public static RelatedStockResponse from(ArticleRelatedStock stock) {
        return new RelatedStockResponse(stock.getStockName(), stock.getStockCode(), stock.getRelationReason());
    }
}
