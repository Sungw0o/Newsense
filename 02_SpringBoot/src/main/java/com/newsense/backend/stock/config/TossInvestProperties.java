package com.newsense.backend.stock.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "toss.invest")
public record TossInvestProperties(
        boolean enabled,
        String baseUrl,
        String tokenPath,
        String quotePath,
        String grantType,
        String clientId,
        String clientSecret
) {

    public boolean hasCredentials() {
        return clientId != null && !clientId.isBlank()
                && clientSecret != null && !clientSecret.isBlank();
    }
}
