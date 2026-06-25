package com.newsense.backend.article.crawler.model;

public record CrawlRunResult(
        int discovered,
        int saved,
        int skipped,
        int failed
) {

    public CrawlRunResult add(CrawlRunResult other) {
        return new CrawlRunResult(
                discovered + other.discovered,
                saved + other.saved,
                skipped + other.skipped,
                failed + other.failed
        );
    }

    public static CrawlRunResult empty() {
        return new CrawlRunResult(0, 0, 0, 0);
    }
}
