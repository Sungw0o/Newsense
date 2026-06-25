package com.newsense.backend.indicator;

import com.newsense.backend.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/indicators")
@RequiredArgsConstructor
public class IndicatorController implements IndicatorApiDocs {

    private final IndicatorService indicatorService;

    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<IndicatorResponse>> getIndicators() {
        return ResponseEntity.ok(ApiResponse.success(indicatorService.getLatest()));
    }
}
