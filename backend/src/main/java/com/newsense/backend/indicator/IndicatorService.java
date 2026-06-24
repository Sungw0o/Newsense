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

@Slf4j
@Service
@RequiredArgsConstructor
public class IndicatorService {

    private static final String REDIS_KEY = "indicator:latest";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);
    private static final double BOK_BASE_RATE = 3.5;
    private static final String EXCHANGE_RATE_URL = "https://open.er-api.com/v6/latest/USD";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public IndicatorResponse getLatest() {
        String cached = redisTemplate.opsForValue().get(REDIS_KEY);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, IndicatorResponse.class);
            } catch (JacksonException e) {
                log.warn("Failed to deserialize cached indicator, refreshing: {}", e.getMessage());
            }
        }
        return refresh();
    }

    public IndicatorResponse refresh() {
        try {
            RestClient client = RestClient.create();
            Double kospi = fetchYahooPrice(client, "^KS11");
            Double kosdaq = fetchYahooPrice(client, "^KQ11");
            Double usdKrw = fetchUsdKrw(client);

            IndicatorResponse response = new IndicatorResponse(usdKrw, BOK_BASE_RATE, kospi, kosdaq, LocalDateTime.now());
            cacheResponse(response);
            return response;
        } catch (Exception e) {
            log.warn("Financial indicator fetch failed, returning mock: {}", e.getMessage());
            return mockResponse();
        }
    }

    private Double fetchYahooPrice(RestClient client, String symbol) {
        try {
            String url = "https://query1.finance.yahoo.com/v8/finance/chart/"
                    + java.net.URLEncoder.encode(symbol, java.nio.charset.StandardCharsets.UTF_8)
                    + "?range=1d&interval=1d";
            JsonNode root = client.get()
                    .uri(java.net.URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .retrieve()
                    .body(JsonNode.class);

            if (root == null) return null;
            JsonNode meta = root.path("chart").path("result").path(0).path("meta");
            double price = meta.path("regularMarketPrice").asDouble(0.0);
            return price > 0 ? price : null;
        } catch (RestClientException e) {
            log.debug("Yahoo Finance fetch failed for {}: {}", symbol, e.getMessage());
            return null;
        }
    }

    private Double fetchUsdKrw(RestClient client) {
        try {
            JsonNode root = client.get()
                    .uri(EXCHANGE_RATE_URL)
                    .retrieve()
                    .body(JsonNode.class);

            if (root == null) return null;
            double krw = root.path("rates").path("KRW").asDouble(0.0);
            return krw > 0 ? krw : null;
        } catch (RestClientException e) {
            log.debug("Exchange rate fetch failed: {}", e.getMessage());
            return null;
        }
    }

    private void cacheResponse(IndicatorResponse response) {
        try {
            String json = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(REDIS_KEY, json, CACHE_TTL);
        } catch (JacksonException e) {
            log.warn("Failed to cache indicator response: {}", e.getMessage());
        }
    }

    private IndicatorResponse mockResponse() {
        return new IndicatorResponse(1380.0, BOK_BASE_RATE, 2600.0, 860.0, LocalDateTime.now());
    }
}
