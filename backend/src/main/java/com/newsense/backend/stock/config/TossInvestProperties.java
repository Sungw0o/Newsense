package com.newsense.backend.stock.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "toss.invest")
public record TossInvestProperties(
        boolean enabled,
        String baseUrl,
        String apiKey,
        String secretKey
) {

    public boolean hasCredentials() {
        return apiKey != null && !apiKey.isBlank()
                && secretKey != null && !secretKey.isBlank();
    }
}
