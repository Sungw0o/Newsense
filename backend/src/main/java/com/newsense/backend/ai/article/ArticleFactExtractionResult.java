package com.newsense.backend.ai.article;

import java.util.List;

public record ArticleFactExtractionResult(
        List<String> quantitativeFacts,
        List<String> keyTerms,
        List<String> eventFacts
) {
    public static ArticleFactExtractionResult empty() {
        return new ArticleFactExtractionResult(List.of(), List.of(), List.of());
    }

    public boolean isEmpty() {
        return quantitativeFacts.isEmpty() && keyTerms.isEmpty() && eventFacts.isEmpty();
    }
}
