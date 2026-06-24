package com.newsense.backend.article.domain;

import java.util.Arrays;

public enum ArticleDifficulty {
    BASIC("초급"),
    INTERMEDIATE("중급"),
    ADVANCED("고급");

    private final String displayName;

    ArticleDifficulty(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ArticleDifficulty from(String value) {
        return Arrays.stream(values())
                .filter(difficulty -> difficulty.name().equalsIgnoreCase(value)
                        || difficulty.displayName.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported article difficulty: " + value));
    }
}
