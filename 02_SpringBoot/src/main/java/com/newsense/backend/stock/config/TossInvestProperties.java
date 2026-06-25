package com.newsense.backend.stock.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "toss.invest")
public record TossInvestProperties(
        boolean enabled,
        String baseUrl,
        String quotePath,
        String secretToken
) {

    public boolean hasCredentials() {
        return secretToken != null && !secretToken.isBlank();
    }
}
