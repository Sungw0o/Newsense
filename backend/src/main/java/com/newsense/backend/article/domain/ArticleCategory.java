package com.newsense.backend.article.domain;

import java.util.Arrays;

public enum ArticleCategory {
    ECONOMY("거시경제"),
    MONETARY_POLICY("통화정책"),
    FINANCE("금융"),
    EXCHANGE_RATE("환율"),
    REAL_ESTATE("부동산"),
    STOCK("주식");

    private final String displayName;

    ArticleCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ArticleCategory from(String value) {
        return Arrays.stream(values())
                .filter(category -> category.name().equalsIgnoreCase(value)
                        || category.displayName.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported article category: " + value));
    }
}
