package com.newsense.backend.stock.service;

import org.springframework.stereotype.Service;

/**
 * 주식 시장 반응 평가 서비스.
 *
 * <p>전일 대비 등락률(%)을 기준으로 다섯 단계로 분류합니다.
 * <ul>
 *   <li>급등 : changePct >= 3.0%</li>
 *   <li>상승 : changePct >= 1.0%</li>
 *   <li>보합 : -1.0% < changePct < 1.0%</li>
 *   <li>하락 : changePct <= -1.0%</li>
 *   <li>급락 : changePct <= -3.0%</li>
 * </ul>
 */
@Service
public class StockMarketReactionService {

    private static final double SURGE_THRESHOLD = 3.0;
    private static final double RISE_THRESHOLD  = 1.0;
    private static final double FALL_THRESHOLD  = -1.0;
    private static final double PLUNGE_THRESHOLD = -3.0;

    /**
     * 등락률로 시장 반응 문자열을 반환합니다.
     *
     * @param changePct 전일 대비 변화율 (%)
     * @return "급등" | "상승" | "보합" | "하락" | "급락"
     */
    public String assess(double changePct) {
        if (changePct >= SURGE_THRESHOLD)  return "급등";
        if (changePct >= RISE_THRESHOLD)   return "상승";
        if (changePct <= PLUNGE_THRESHOLD) return "급락";
        if (changePct <= FALL_THRESHOLD)   return "하락";
        return "보합";
    }
}
