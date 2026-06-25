package com.newsense.backend.stock.controller;

import com.newsense.backend.common.exception.CustomException;
import com.newsense.backend.common.exception.ErrorCode;
import com.newsense.backend.common.response.ApiResponse;
import com.newsense.backend.stock.dto.StockQuote;
import com.newsense.backend.stock.service.StockQuoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StockController implements StockApiDocs {

    private static final int MAX_BATCH_SIZE = 10;

    private final StockQuoteService stockQuoteService;

    @Override
    public ResponseEntity<ApiResponse<StockQuote>> getQuote(String stockCode) {
        StockQuote quote = stockQuoteService.getQuote(stockCode)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));
        return ResponseEntity.ok(ApiResponse.success("종목 시세 조회에 성공했습니다.", quote));
    }

    @Override
    public ResponseEntity<ApiResponse<List<StockQuote>>> getQuotes(List<String> codes) {
        if (codes.size() > MAX_BATCH_SIZE) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return ResponseEntity.ok(ApiResponse.success(
                "종목 시세 조회에 성공했습니다.",
                stockQuoteService.getQuotes(codes)
        ));
    }
}
