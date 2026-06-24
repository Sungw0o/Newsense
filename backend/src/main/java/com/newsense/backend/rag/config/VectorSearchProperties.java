package com.newsense.backend.rag.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rag.vector-search")
public record VectorSearchProperties(
        boolean enabled,
        String mode,
        String indexName,
        int numCandidates,
        int dimensions
) {
    public VectorSearchProperties {
        if (mode == null || mode.isBlank()) mode = "local";
        if (indexName == null || indexName.isBlank()) indexName = "article_vector_index";
        if (numCandidates <= 0) numCandidates = 150;
        if (dimensions <= 0) dimensions = 3072;
    }

    public boolean isAtlasMode() {
        return "atlas".equalsIgnoreCase(mode);
    }
}
