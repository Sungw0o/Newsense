package com.newsense.backend.learning.dto;

import java.util.List;

/**
 * 사용자 취약 개념 분석 결과.
 *
 * @param topTerms    오답 빈도 상위 경제 용어 (최대 10개)
 * @param topCategories 취약 카테고리 목록 (오답 수 기준 내림차순)
 * @param totalWrongCount 미해결 오답노트 총 수
 * @param avgMistakeCount 항목당 평균 오답 횟수 (반복 실수 지표)
 */
public record WeaknessSummaryResponse(
        List<WeakTerm> topTerms,
        List<WeakCategory> topCategories,
        long totalWrongCount,
        double avgMistakeCount
) {
    public record WeakTerm(String term, int mistakeCount) {}
    public record WeakCategory(String category, long count) {}
}
