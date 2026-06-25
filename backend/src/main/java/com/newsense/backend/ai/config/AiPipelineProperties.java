package com.newsense.backend.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.pipeline")
public record AiPipelineProperties(
        Models models,
        int maxQuizRetries
) {
    public AiPipelineProperties {
        if (models == null) {
            models = new Models(null, null, null, null, null);
        }
        if (maxQuizRetries <= 0) {
            maxQuizRetries = 2;
        }
    }

    public String embeddingModel() {
        return models.valueOrDefault(models.embedding(), "text-embedding-3-large");
    }

    public String factExtractorModel() {
        return models.valueOrDefault(models.factExtractor(), "gpt-4o");
    }

    public String articleClassifierModel(String fallback) {
        return models.valueOrDefault(models.articleClassifier(), fallback);
    }

    public String quizGeneratorModel() {
        return models.valueOrDefault(models.quizGenerator(), "gpt-5");
    }

    public String quizCriticModel(String fallback) {
        return models.valueOrDefault(models.quizCritic(), fallback);
    }

    public record Models(
            String embedding,
            String factExtractor,
            String articleClassifier,
            String quizGenerator,
            String quizCritic
    ) {
        private String valueOrDefault(String value, String defaultValue) {
            return value == null || value.isBlank() ? defaultValue : value;
        }
    }
}
