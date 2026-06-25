package com.newsense.backend.indicator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class IndicatorScheduler {

    private final IndicatorService indicatorService;

    @Scheduled(fixedDelay = 300_000, initialDelay = 10_000)
    public void refreshIndicators() {
        log.debug("Refreshing financial indicators");
        indicatorService.refresh();
    }
}
