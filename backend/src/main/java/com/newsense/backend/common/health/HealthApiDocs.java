package com.newsense.backend.common.health;

import com.newsense.backend.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "Health", description = "서버 상태 확인")
public interface HealthApiDocs {

    @Operation(summary = "헬스 체크", description = "서버가 정상적으로 기동되어 있는지 확인합니다.")
    @GetMapping("/api/v1/health")
    ResponseEntity<ApiResponse<String>> healthCheck();
}
