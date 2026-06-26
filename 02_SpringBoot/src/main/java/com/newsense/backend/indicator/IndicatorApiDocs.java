package com.newsense.backend.indicator;

import com.newsense.backend.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "금융 지표", description = "실시간 금융 지표 API")
public interface IndicatorApiDocs {

    @Operation(summary = "최신 금융 지표 조회", description = "달러/원, KOSPI, KOSDAQ 지표를 반환합니다.")
    ResponseEntity<ApiResponse<IndicatorResponse>> getIndicators();
}
