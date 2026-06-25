package com.newsense.backend.admin.dto;

public record CrawlResultResponse(
        int discovered,
        int saved,
        int skipped,
        int failed
) {
}
