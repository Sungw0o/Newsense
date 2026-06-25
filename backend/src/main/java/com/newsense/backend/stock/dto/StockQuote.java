package com.newsense.backend.stock.dto;

/**
 * 주식 종목 시세 DTO.
 *
 * @param stockCode  종목 코드 (예: "005930")
 * @param stockName  종목명 (예: "삼성전자")
 * @param price      현재가
 * @param change     전일 대비 변화량
 * @param changePct  전일 대비 변화율 (%)
 * @param trend      "UP" | "DOWN" | "FLAT"
 * @param marketCap  시가총액 (원, nullable)
 * @param volume     거래량 (nullable)
 */
public record StockQuote(
        String stockCode,
        String stockName,
        long price,
        long change,
        double changePct,
        String trend,
        Long marketCap,
        Long volume
) {
    public static StockQuote of(String code, String name, long price, long change, double changePct,
                                Long marketCap, Long volume) {
        String trend = change > 0 ? "UP" : change < 0 ? "DOWN" : "FLAT";
        return new StockQuote(code, name, price, change, changePct, trend, marketCap, volume);
    }
}
