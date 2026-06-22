package com.newsense.backend.article.crawler.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "crawler")
public record CrawlerProperties(
        boolean enabled,
        long initialDelay,
        long fixedDelay,
        int connectionTimeout,
        int maxItemsPerSource,
        int maxRetries,
        long retryDelay,
        long requestDelay,
        int chunkSize,
        List<Source> sources
) {

    public CrawlerProperties {
        sources = sources == null ? List.of() : List.copyOf(sources);
    }

    public record Source(
            String key,
            String name,
            String baseUrl,
            String listUrl,
            String itemSelector,
            String linkSelector,
            String dateSelector,
            String bodySelector,
            String bodyAttribute,
            boolean enabled
    ) {
    }
}
