package com.newsense.backend.stock.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 종목 코드 유효성 검증 서비스.
 *
 * <p>Toss Invest Open API symbols 규격에 맞춰 국내 6자리 코드와 미국 심볼을 허용합니다.
 */
@Service
public class StockValidationService {

    private static final Pattern VALID_CODE_PATTERN = Pattern.compile("^[A-Za-z0-9.\\-]+$");

    /**
     * 단일 종목 코드 유효성 검증.
     */
    public boolean isValid(String stockCode) {
        if (stockCode == null || stockCode.isBlank()) {
            return false;
        }
        return VALID_CODE_PATTERN.matcher(stockCode.trim()).matches();
    }

    /**
     * 유효한 코드만 필터링.
     */
    public List<String> filterValid(List<String> codes) {
        if (codes == null) {
            return List.of();
        }
        return codes.stream()
                .filter(this::isValid)
                .toList();
    }
}
