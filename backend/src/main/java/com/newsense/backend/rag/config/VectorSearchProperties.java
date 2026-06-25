package com.newsense.backend.rag.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rag.vector-search")
public class VectorSearchProperties {
    private static final double DEFAULT_VECTOR_WEIGHT = 0.45;
    private static final double DEFAULT_KEYWORD_WEIGHT = 0.20;
    private static final double DEFAULT_RECENCY_WEIGHT = 0.10;
    private static final double DEFAULT_CATEGORY_WEIGHT = 0.10;
    private static final double DEFAULT_WEAKNESS_WEIGHT = 0.10;
    private static final double DEFAULT_MARKET_WEIGHT = 0.05;

    private final boolean enabled;
    private final String mode;
    private final String indexName;
    private final int numCandidates;
    private final int dimensions;
    private final double vectorWeight;
    private final double keywordWeight;
    private final double recencyWeight;
    private final double categoryWeight;
    private final double weaknessWeight;
    private final double marketWeight;

    public VectorSearchProperties(
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
        if (mode == null || mode.isBlank()) mode = "local";
        if (indexName == null || indexName.isBlank()) indexName = "article_vector_index";
        if (numCandidates <= 0) numCandidates = 150;
        if (dimensions <= 0) dimensions = 3072;
        this.enabled = enabled;
        this.mode = mode;
        this.indexName = indexName;
        this.numCandidates = numCandidates;
        this.dimensions = dimensions;
        this.vectorWeight = normalizeWeight(vectorWeight, DEFAULT_VECTOR_WEIGHT);
        this.keywordWeight = normalizeWeight(keywordWeight, DEFAULT_KEYWORD_WEIGHT);
        this.recencyWeight = normalizeWeight(recencyWeight, DEFAULT_RECENCY_WEIGHT);
        this.categoryWeight = normalizeWeight(categoryWeight, DEFAULT_CATEGORY_WEIGHT);
        this.weaknessWeight = normalizeWeight(weaknessWeight, DEFAULT_WEAKNESS_WEIGHT);
        this.marketWeight = normalizeWeight(marketWeight, DEFAULT_MARKET_WEIGHT);
    }

    public boolean enabled() {
        return enabled;
    }

    public String mode() {
        return mode;
    }

    public String indexName() {
        return indexName;
    }

    public int numCandidates() {
        return numCandidates;
    }

    public int dimensions() {
        return dimensions;
    }

    public double vectorWeight() {
        return vectorWeight;
    }

    public double keywordWeight() {
        return keywordWeight;
    }

    public double recencyWeight() {
        return recencyWeight;
    }

    public double categoryWeight() {
        return categoryWeight;
    }

    public double weaknessWeight() {
        return weaknessWeight;
    }

    public double marketWeight() {
        return marketWeight;
    }

    public boolean isAtlasMode() {
        return "atlas".equalsIgnoreCase(mode);
    }

    private static double normalizeWeight(double value, double fallback) {
        return value > 0 ? value : fallback;
    }
}
