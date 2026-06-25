package com.newsense.backend.article.dto;

import com.newsense.backend.article.domain.ArticleRelatedStock;
import com.newsense.backend.stock.dto.StockQuote;

/**
 * 기사 관련 종목 응답 DTO.
 *
 * <p>{@code price} ~ {@code marketReaction} 필드는 Toss Invest API가 활성화된 경우에만
 * 값이 채워지며, 비활성화 시 {@code null}로 반환됩니다.
 */
public record RelatedStockResponse(
        String stockName,
        String stockCode,
        String relationReason,
        Long   price,
        Long   change,
        Double changePct,
        String trend,
        Long   volume,
        String marketReaction
) {
    /** 시세 정보 없이 기본 필드만 채운 응답 */
    public static RelatedStockResponse from(ArticleRelatedStock stock) {
        return new RelatedStockResponse(
                stock.getStockName(),
                stock.getStockCode(),
                stock.getRelationReason(),
                null, null, null, null, null, null
        );
    }

    /** 시세 + 시장반응 포함 응답 */
    public static RelatedStockResponse withQuote(
            ArticleRelatedStock stock,
            StockQuote quote,
            String marketReaction
    ) {
        return new RelatedStockResponse(
                stock.getStockName(),
                stock.getStockCode(),
                stock.getRelationReason(),
                quote.price(),
                quote.change(),
                quote.changePct(),
                quote.trend(),
                quote.volume(),
                marketReaction
        );
    }
}
