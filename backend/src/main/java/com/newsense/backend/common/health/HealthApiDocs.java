package com.newsense.backend.common.health;

import com.newsense.backend.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Health 체크 API 문서 정의.
 *
 * 컨벤션: Swagger(@Operation, @Tag 등)와 매핑(@GetMapping 등) 어노테이션은
 * 컨트롤러가 아니라 이 *ApiDocs 인터페이스에 작성한다.
 * 컨트롤러는 implements 후 @Override 메서드에 순수 로직만 담아 깔끔하게 유지한다.
 * 새 도메인을 추가할 때도 {Domain}Controller + {Domain}ApiDocs 형태를 그대로 따른다.
 */
@Tag(name = "Health", description = "서버 상태 확인")
public interface HealthApiDocs {

    @Operation(summary = "헬스 체크", description = "서버가 정상적으로 기동되어 있는지 확인한다.")
    @GetMapping("/api/v1/health")
    ResponseEntity<ApiResponse<String>> healthCheck();
}
