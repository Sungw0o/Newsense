package com.newsense.backend.indicator;

/**
 * 개별 금융 지표 항목.
 *
 * @param key       식별자 (예: "USD_KRW", "KOSPI", "KOSDAQ")
 * @param label     화면 표시 레이블 (예: "달러/원", "코스피")
 * @param value     현재 값
 * @param unit      단위 (예: "원", "pt", "%")
 * @param change    전일 대비 변화량 (없으면 null)
 * @param changePct 전일 대비 변화율 % (없으면 null)
 * @param trend     "UP" | "DOWN" | "FLAT"
 */
public record IndicatorItem(
        String key,
        String label,
        Double value,
        String unit,
        Double change,
        Double changePct,
        String trend
) {

    public static IndicatorItem of(String key, String label, Double value, String unit,
                                   Double change, Double changePct) {
        String trend = "FLAT";
        if (change != null) {
            if (change > 0) trend = "UP";
            else if (change < 0) trend = "DOWN";
        }
        return new IndicatorItem(key, label, value, unit, change, changePct, trend);
    }
}
