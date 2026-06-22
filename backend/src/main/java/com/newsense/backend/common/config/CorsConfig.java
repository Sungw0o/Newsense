package com.newsense.backend.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 허용할 Origin: 로컬 개발 + 추후 프로덕션 도메인 추가
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:5173",  // Vite dev server
                "http://localhost:3000",  // 혹시 다른 포트 사용 시
                "https://*.newsense.com"  // 추후 프로덕션
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With",
                "Cache-Control"
        ));

        // HttpOnly 쿠키(Refresh Token) 수신을 위해 반드시 true
        config.setAllowCredentials(true);

        // Preflight 캐시 시간 (1시간)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
