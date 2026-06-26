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
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndicatorService {

    private static final String REDIS_KEY  = "indicator:latest";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);
    private static final String EXCHANGE_RATE_URL = "https://open.er-api.com/v6/latest/USD";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public IndicatorResponse getLatest() {
        try {
            String cached = redisTemplate.opsForValue().get(REDIS_KEY);
            if (cached != null) {
                try {
                    IndicatorResponse response = objectMapper.readValue(cached, IndicatorResponse.class);
                    return new IndicatorResponse("STALE", response.insight(), response.items(), response.fetchedAt());
                } catch (JacksonException e) {
                    log.warn("Failed to deserialize cached indicator, refreshing: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("Redis unavailable for indicator cache read, fetching live: {}", e.getMessage());
        }
        return refresh();
    }

    /** Yahoo 시세 (가격 + 변동) */
    record YahooQuote(Double price, Double change, Double changePct) {}

    public IndicatorResponse refresh() {
        try {
            RestClient client = createClient();
            YahooQuote usdKrw = fetchYahooQuote(client, "KRW=X");
            YahooQuote kospi  = fetchYahooQuote(client, "^KS11");
            YahooQuote kosdaq = fetchYahooQuote(client, "^KQ11");
            if (usdKrw == null) {
                Double exchangeRate = fetchUsdKrw(client);
                usdKrw = exchangeRate == null ? null : new YahooQuote(exchangeRate, null, null);
            }
            if (kospi == null && kosdaq == null && usdKrw == null) {
                return mockResponse();
            }

            List<IndicatorItem> items = buildItems(usdKrw, kospi, kosdaq);
            Double usdKrwPrice = usdKrw != null ? usdKrw.price() : null;
            Double kospiPrice  = kospi  != null ? kospi.price()  : null;
            Double kosdaqPrice = kosdaq != null ? kosdaq.price() : null;
            IndicatorResponse response = new IndicatorResponse(
                    "OK", buildInsight(usdKrwPrice, kospiPrice, kosdaqPrice), items, LocalDateTime.now());
            cacheResponse(response);
            return response;
        } catch (Exception e) {
            log.warn("Financial indicator fetch failed, returning mock: {}", e.getMessage());
            return mockResponse();
        }
    }

    RestClient createClient() {
        return RestClient.create();
    }

    private YahooQuote fetchYahooQuote(RestClient client, String symbol) {
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
            if (price <= 0) return null;

            // regularMarketChange가 없으면 chartPreviousClose로 직접 계산
            JsonNode changeNode    = meta.path("regularMarketChange");
            JsonNode changePctNode = meta.path("regularMarketChangePercent");
            Double change    = (!changeNode.isMissingNode()    && !changeNode.isNull())    ? changeNode.asDouble()    : null;
            Double changePct = (!changePctNode.isMissingNode() && !changePctNode.isNull()) ? changePctNode.asDouble() : null;

            if (change == null) {
                JsonNode prevNode = meta.path("chartPreviousClose");
                if (!prevNode.isMissingNode() && !prevNode.isNull()) {
                    double prev = prevNode.asDouble();
                    if (prev > 0) {
                        change    = price - prev;
                        changePct = (change / prev) * 100.0;
                    }
                }
            }

            return new YahooQuote(price, change, changePct);
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
        } catch (Exception e) {
            log.warn("Failed to cache indicator response: {}", e.getMessage());
        }
    }

    private IndicatorResponse mockResponse() {
        List<IndicatorItem> items = buildItems(
                new YahooQuote(1380.0, 2.5, 0.18),
                new YahooQuote(2600.0, 12.5, 0.48),
                new YahooQuote(860.0, -3.2, -0.37)
        );
        return new IndicatorResponse("MOCK", buildInsight(1380.0, 2600.0, 860.0), items, LocalDateTime.now());
    }

    private List<IndicatorItem> buildItems(YahooQuote usdKrw, YahooQuote kospi, YahooQuote kosdaq) {
        return List.of(
                IndicatorItem.of("USD_KRW",  "달러/원",  usdKrw != null ? usdKrw.price() : null, "원",
                        usdKrw != null ? usdKrw.change() : null,
                        usdKrw != null ? usdKrw.changePct() : null),
                IndicatorItem.of("KOSPI",    "코스피",   kospi  != null ? kospi.price()  : null, "pt",
                        kospi  != null ? kospi.change()  : null,
                        kospi  != null ? kospi.changePct() : null),
                IndicatorItem.of("KOSDAQ",   "코스닥",   kosdaq != null ? kosdaq.price() : null, "pt",
                        kosdaq != null ? kosdaq.change() : null,
                        kosdaq != null ? kosdaq.changePct() : null)
        );
    }

    private String buildInsight(Double usdKrw, Double kospi, Double kosdaq) {
        String exchange = usdKrw  == null ? "환율 확인 중"  : "달러/원 "  + Math.round(usdKrw)  + "원";
        String market   = kospi   == null ? "증시 확인 중"  : "코스피 "   + Math.round(kospi)   + "pt";
        String growth   = kosdaq  == null ? "코스닥 확인 중" : "코스닥 "  + Math.round(kosdaq)  + "pt";
        return exchange + " · " + market + " · " + growth;
    }
}
