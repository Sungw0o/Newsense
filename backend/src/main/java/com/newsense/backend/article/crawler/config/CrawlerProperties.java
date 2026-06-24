package com.newsense.backend.article.crawler.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "crawler")
public record CrawlerProperties(
        boolean enabled,
        boolean bootstrapEnabled,
        int minimumArticles,
        long initialDelay,
        long fixedDelay,
        int connectionTimeout,
        int maxItemsPerSource,
        int maxRetries,
        long retryDelay,
        long requestDelay,
        int chunkSize,
        int pdfMinTextLength,
        String tessDataPath,
        String naverClientId,
        String naverClientSecret,
        String newsApiKey,
        Cron cron,
        List<Source> sources
) {

    public CrawlerProperties {
        cron = cron == null ? new Cron(
                "0 0 11,17 * * MON-FRI",
                "0 0 8,14,20 * * *"
        ) : cron;
        sources = sources == null ? List.of() : List.copyOf(sources);
    }

    public record Cron(
            String publicSource,
            String portalSource
    ) {
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
