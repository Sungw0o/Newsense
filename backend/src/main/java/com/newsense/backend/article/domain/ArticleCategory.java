package com.newsense.backend.article.domain;

import java.util.Arrays;
import java.util.List;

public enum ArticleCategory {
    MACRO_ECONOMY("거시경제", List.of("ECONOMY", "MACRO", "거시경제")),
    FINANCE_INVESTMENT("금융/투자", List.of("FINANCE", "INVESTMENT", "STOCK", "금융", "주식", "금융/투자")),
    POLICY_SYSTEM("정책/제도", List.of("POLICY", "MONETARY_POLICY", "부동산", "정책/제도")),
    COMPANY_INDUSTRY("기업/산업", List.of("COMPANY", "INDUSTRY", "기업/산업")),
    GLOBAL_ECONOMY("글로벌경제", List.of("GLOBAL", "환율", "글로벌경제"));

    private final String displayName;
    private final List<String> aliases;

    ArticleCategory(String displayName, List<String> aliases) {
        this.displayName = displayName;
        this.aliases = aliases;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ArticleCategory from(String value) {
        return Arrays.stream(values())
                .filter(category -> category.name().equalsIgnoreCase(value)
                        || category.displayName.equals(value)
                        || category.aliases.stream().anyMatch(alias -> alias.equalsIgnoreCase(value)))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported article category: " + value));
    }
}
