package com.newsense.backend.common.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 모든 API 응답을 감싸는 공통 응답 규격.
 * <pre>
 * {
 *   "success": true,
 *   "code": 200,
 *   "message": "메시지",
 *   "data": { ... }
 * }
 * </pre>
 */
@Getter
@Builder
public class ApiResponse<T> {

    private final boolean success;
    private final int code;
    private final String message;
    private final T data;

    public static <T> ApiResponse<T> success(T data) {
        return success(200, "요청이 성공적으로 처리되었습니다.", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return success(200, message, data);
    }

    public static <T> ApiResponse<T> success(int code, String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .code(code)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .data(null)
                .build();
    }
}
