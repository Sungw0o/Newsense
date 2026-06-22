package com.newsense.backend.common.health;

import com.newsense.backend.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * 서버 상태 확인용 헬스 체크 컨트롤러.
 * 매핑/문서화 어노테이션은 HealthApiDocs 인터페이스가 들고 있고, 여기엔 로직만 둔다.
 */
@RestController
public class HealthController implements HealthApiDocs {

    @Override
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(ApiResponse.success("OK"));
    }
}
