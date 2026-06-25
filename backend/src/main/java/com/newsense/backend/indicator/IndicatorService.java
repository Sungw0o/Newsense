package com.newsense.backend.indicator;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndicatorService {

    private static final String REDIS_KEY = "indicator:latest";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);
    private static final double BOK_BASE_RATE = 2.75; // 2025년 기준금리
    private static final String EXCHANGE_RATE_URL = "https://open.er-api.com/v6/latest/USD";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /** 캐시 우선 조회 — 캐시 없으면 실시간 수집 */
    public IndicatorResponse getLatest() {
        String cached = redisTemplate.opsForValue().get(REDIS_KEY);
        if (cached != null) {
            try {
                IndicatorResponse response = objectMapper.readValue(cached, IndicatorResponse.class);
                // 캐시 상태면 status를 STALE로 재표기
                return new IndicatorResponse("STALE", response.insight(), response.items(), response.fetchedAt());
            } catch (JacksonException e) {
                log.warn("Failed to deserialize cached indicator, refreshing: {}", e.getMessage());
            }
        }
        return refresh();
    }

    /** 실시간 수집 후 캐시 갱신 */
    public IndicatorResponse refresh() {
        try {
            RestClient client = RestClient.create();

            YahooQuote kospiQuote = fetchYahooQuote(client, "^KS11");
            YahooQuote kosdaqQuote = fetchYahooQuote(client, "^KQ11");
            Double usdKrw = fetchUsdKrw(client);

            List<IndicatorItem> items = buildItems(usdKrw, kospiQuote, kosdaqQuote);
            String insight = buildInsight(usdKrw, kospiQuote, kosdaqQuote);

            IndicatorResponse response = new IndicatorResponse("OK", insight, items, LocalDateTime.now());
            cacheResponse(response);
            return response;
        } catch (Exception e) {
            log.warn("Financial indicator fetch failed, returning mock: {}", e.getMessage());
            return mockResponse();
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 헬퍼 메서드
    // ──────────────────────────────────────────────────────────────────────────

    private List<IndicatorItem> buildItems(Double usdKrw, YahooQuote kospi, YahooQuote kosdaq) {
        List<IndicatorItem> items = new ArrayList<>();

        items.add(IndicatorItem.of(
                "USD_KRW", "달러/원",
                usdKrw, "원",
                null, null)); // 환율은 전일 대비 없음

        items.add(IndicatorItem.of(
                "KOSPI", "코스피",
                kospi != null ? kospi.price() : null, "pt",
                kospi != null ? kospi.change() : null,
                kospi != null ? kospi.changePct() : null));

        items.add(IndicatorItem.of(
                "KOSDAQ", "코스닥",
                kosdaq != null ? kosdaq.price() : null, "pt",
                kosdaq != null ? kosdaq.change() : null,
                kosdaq != null ? kosdaq.changePct() : null));

        items.add(IndicatorItem.of(
                "BOK_RATE", "기준금리",
                BOK_BASE_RATE, "%",
                null, null));

        return items;
    }

    private String buildInsight(Double usdKrw, Y