package com.newsense.backend.indicator;

import java.time.LocalDateTime;

public record IndicatorResponse(
        Double usdKrwRate,
        Double bokBaseRate,
        Double kospi,
        Double kosdaq,
        LocalDateTime fetchedAt
) {
}
