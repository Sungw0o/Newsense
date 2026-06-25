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
import java.util.List;
import java.util.Objects;
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

        Optional<StockQuote> quote = tossInvestClient.getQuote(stockCode);
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
}
