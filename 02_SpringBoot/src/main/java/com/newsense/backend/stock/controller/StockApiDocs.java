package com.newsense.backend.stock.controller;

import com.newsense.backend.common.response.ApiResponse;
import com.newsense.backend.stock.dto.StockQuote;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Stock", description = "주식 종목 시세 (Toss Invest 연동)")
public interface StockApiDocs {

    @Operation(summary = "단일 종목 시세 조회", description = "종목 코드로 현재가·변동률을 조회합니다.")
    @GetMapping("/api/v1/stocks/{code}")
    ResponseEntity<ApiResponse<StockQuote>> getQuote(
            @Parameter(description = "종목 코드 (예: 005930)") @PathVariable("code") String stockCode
    );

    @Operation(summary = "복수 종목 시세 조회", description = "쉼표로 구분된 종목 코드 목록의 시세를 일괄 조회합니다. (최대 10개)")
    @GetMapping("/api/v1/stocks")
    ResponseEntity<ApiResponse<List<StockQuote>>> getQuotes(
            @Parameter(description = "종목 코드 목록 (쉼표 구분, 최대 10개)")
            @RequestParam List<String> codes
    );
}
