package com.newsense.backend.indicator;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Financial indicator response DTO.
 *
 * @param status    data freshness: "OK" (live), "STALE" (cache), "MOCK" (fallback)
 * @param insight   one-sentence summary insight
 * @param items     financial indicator items
 * @param fetchedAt time the indicators were fetched
 */
public record IndicatorResponse(
        String status,
        String insight,
        List<IndicatorItem> items,
        LocalDateTime fetchedAt
) {
}
