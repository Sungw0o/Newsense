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
    private static