package com.newsense.backend.ai.article;

public record RelatedStockInfo(
        String stockName,
        String stockCode,
        String relationReason
) {
}
