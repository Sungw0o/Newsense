package com.newsense.backend.rag.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rag.vector-search")
public record VectorSearchProperties(
        boolean enabled,
        String mode,
        String indexName,
        int numCandidates,
        int dimensions,
        double vectorWeight,
        double keywordWeight,
        double recencyWeight,
        double categoryWeight,
        double weaknessWeight,
        double marketWeight
) {
    private static final double DEFAULT_VECTOR_WEIGHT = 0.45;
    private static final double DEFAULT_KEYWORD_WEIGHT = 0.20;
    private static final double DEFAULT_RECENCY_WEIGHT = 0.10;
    private static final double DEFAULT_CATEGORY_WEIGHT = 0.10;
    private static final double DEFAULT_WEAKNESS_WEIGHT = 0.10;
    private static final double DEFAULT_MARKET_WEIGHT = 0.05;

    public VectorSearchProperties {
        if (mode == null || mode.isBlank()) mode = "local";
        if (indexName == null || indexName.isBlank()) indexName = "article_vector_index";
        if (numCandidates <= 0) numCandidates = 150;
        if (dimensions <= 0) dimensions = 3072;
        vectorWeight = normalizeWeight(vectorWeight, DEFAULT_VECTOR_WEIGHT);
        keywordWeight = normalizeWeight(keywordWeight, DEFAULT_KEYWORD_WEIGHT);
        recencyWeight = normalizeWeight(recencyWeight, DEFAULT_RECENCY_WEIGHT);
        categoryWeight = normalizeWeight(categoryWeight, DEFAULT_CATEGORY_WEIGHT);
        weaknessWeight = normalizeWeight(weaknessWeight, DEFAULT_WEAKNESS_WEIGHT);
        marketWeight = normalizeWeight(marketWeight, DEFAULT_MARKET_WEIGHT);
    }

    public boolean isAtlasMode() {
        return "atlas".equalsIgnoreCase(mode);
    }

    private static double normalizeWeight(double value, double fallback) {
        return value > 0 ? value : fallback;
    }
}
