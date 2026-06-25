package com.newsense.backend.stock.client;

import com.newsense.backend.stock.config.TossInvestProperties;
import com.newsense.backend.stock.dto.StockQuote;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.util.Optional;

/**
 * Toss Invest Open API 클라이언트.
 *
 * <p>인증: 현재가 API는 발급받은 secret token을 Bearer 토큰으로 직접 전달합니다.
 * <p>키 설정: 환경변수 TOSS_INVEST_SECRET_TOKEN 또는 TOSS_INVEST_SECRET_KEY (운영 .env)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TossInvestClient {

    private final RestClient       tossInvestRestClient;
    private final TossInvestProperties properties;

    /**
     * 단일 종목 현재가 조회.
     *
     * @param stockCode 종목 코드 (예: "005930")
     * @return StockQuote Optional — API 비활성화·키 없음·오류 시 empty
     */
    public Optional<StockQuote> getQuote(String stockCode) {
        if (!properties.enabled() || !properties.hasCredentials()) {
            log.debug("[Toss] stock API disabled or credentials missing, skipping quote for {}", stockCode);
            return Optional.empty();
        }

        try {
            JsonNode root = tossInvestRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(properties.quotePath())
                            .queryParam("symbols", stockCode)
                            .build())
                    .header("Authorization", "Bearer " + properties.secretToken())
                    .retrieve()
                    .body(JsonNode.class);

            JsonNode data = extractPayload(root);
            if (data == null || data.isMissingNode() || data.isNull()) {
                log.warn("[Toss] empty response for stockCode={}", stockCode);
                return Optional.empty();
            }

            long  price      = longValue(data, "currentPrice", "current_price", "price", "lastPrice", "close");
            long  change     = longValue(data, "priceChange", "price_change", "change", "changePrice", "signedChangePrice");
            double changePct = doubleValue(data, "priceChangeRate", "price_change_rate", "changeRate", "changePct", "signedChangeRate");
            String name      = textValue(data, stockCode, "name", "stockName", "stock_name", "koreanName", "shortName", "symbol");
            Long  marketCap  = nullableLongValue(data, "marketCap", "market_cap");
            Long  volume     = nullableLongValue(data, "volume", "accTradeVolume", "tradingVolume");

            return Optional.of(StockQuote.of(stockCode, name, price, change, changePct, marketCap, volume));

        } catch (Exception ex) {
            log.warn("[Toss] quote fetch failed stockCode={} reason={}", stockCode, ex.getMessage());
            return Optional.empty();
        }
    }

    private JsonNode extractPayload(JsonNode root) {
        if (root == null) {
            return null;
        }
        if (root.path("result").isArray() && !root.path("result").isEmpty()) {
            return root.path("result").get(0);
        }
        if (!root.path("data").isMissingNode()) {
            return root.path("data");
        }
        if (!root.path("result").isMissingNode()) {
            return root.path("result");
        }
        if (!root.path("body").isMissingNode()) {
            return root.path("body");
        }
        return root;
    }

    private String textValue(JsonNode node, String fallback, String... fields) {
        for (String field : fields) {
            String value = node.path(field).asText("");
            if (!value.isBlank()) {
                return value;
            }
        }
        return fallback;
    }

    private long longValue(JsonNode node, String... fields) {
        Long value = nullableLongValue(node, fields);
        return value == null ? 0L : value;
    }

    private Long nullableLongValue(JsonNode node, String... fields) {
        for (String field : fields) {
            JsonNode value = node.path(field);
            if (!value.isMissingNode() && !value.isNull()) {
                return value.asLong();
            }
        }
        return null;
    }

    private double doubleValue(JsonNode node, String... fields) {
        for (String field : fields) {
            JsonNode value = node.path(field);
            if (!value.isMissingNode() && !value.isNull()) {
                return value.asDouble();
            }
        }
        return 0.0;
    }
}
