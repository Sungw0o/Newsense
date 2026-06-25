package com.newsense.backend.stock.service;

import com.newsense.backend.stock.client.TossInvestClient;
import com.newsense.backend.stock.dto.StockQuote;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 주가 조회 서비스 — Toss Invest API + Redis 캐시.
 *
 * <p>캐시 TTL: 10분. 장 마감 시간대에도 마지막 체결가가 유지됩니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockQuoteService {

    private static final Duration CACHE_TTL         = Duration.ofMinutes(10);
    private static final String   REDIS_KEY_PREFIX   = "stock:quote:";
    private static final Map<String, StockQuote> DEMO_QUOTES = Map.of(
            "005930", StockQuote.of("005930", "삼성전자", 89300, 4100, 4.81, 527_300_000_000_000L, 28_542_110L),
            "000660", StockQuote.of("000660", "SK하이닉스", 512000, 18500, 3.75, 372_740_000_000_000L, 9_834_221L),
            "005380", StockQuote.of("005380", "현대차", 284000, 4500, 1.61, 59_480_000_000_000L, 1_902_441L),
            "105560", StockQuote.of("105560", "KB금융", 126400, -1800, -1.40, 49_720_000_000_000L, 1_324_990L),
            "035720", StockQuote.of("035720", "카카오", 53400, -700, -1.29, 23_650_000_000_000L, 3_418_221L)
    );

    private final TossInvestClient     tossInvestClient;
    private final StringRedisTemplate  redisTemplate;
    private final ObjectMapper         objectMapper;

    /**
     * 단일 종목 시세 조회 (캐시 우선).
     */
    public Optional<StockQuote> getQuote(String stockCode) {
        String key = REDIS_KEY_PREFIX + stockCode;
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            try {
                return Optional.of(objectMapper.readValue(cached, StockQuote.class));
            } catch (JacksonException e) {
                log.warn("[Stock] cache deserialize failed key={}", key);
            }
        }

        Optional<StockQuote> quote = tossInvestClient.getQuote(stockCode)
                .or(() -> fallbackQuote(stockCode));
        quote.ifPresent(q -> cache(key, q));
        return quote;
    }

    /**
     * 복수 종목 일괄 조회.
     */
    public List<StockQuote> getQuotes(List<String> stockCodes) {
        return stockCodes.stream()
                .map(this::getQuote)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private void cache(String key, StockQuote quote) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(quote), CACHE_TTL);
        } catch (JacksonException e) {
            log.warn("[Stock] cache write failed key={}", key);
        }
    }

    private Optional<StockQuote> fallbackQuote(String stockCode) {
        String normalizedCode = stockCode == null ? "" : stockCode.trim();
        StockQuote quote = DEMO_QUOTES.get(normalizedCode);
        if (quote != null) {
            log.warn("[Stock] Toss quote unavailable, using demo fallback for {}", normalizedCode);
            return Optional.of(quote);
        }
        return Optional.empty();
    }
}
