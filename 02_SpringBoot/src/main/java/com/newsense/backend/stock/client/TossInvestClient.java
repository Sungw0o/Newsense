package com.newsense.backend.stock.client;

import com.newsense.backend.stock.config.TossInvestProperties;
import com.newsense.backend.stock.dto.StockQuote;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.Optional;

/**
 * Toss Invest Open API 클라이언트.
 *
 * <p>인증: POST /oauth2/token으로 access_token을 발급받고 Bearer 토큰으로 API를 호출합니다.
 * <p>키 설정: 환경변수 TOSS_INVEST_CLIENT_ID, TOSS_INVEST_CLIENT_SECRET (운영 .env)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TossInvestClient {

    private static final long TOKEN_EXPIRY_SAFETY_SECONDS = 60;

    private final RestClient       tossInvestRestClient;
    private final TossInvestProperties properties;

    private volatile String accessToken;
    private volatile Instant accessTokenExpiresAt = Instant.EPOCH;

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
            String token = getAccessToken();

            JsonNode root = tossInvestRestClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder.path(properties.quotePath());
                        if (properties.quotePath().contains("{code}")) {
                            return builder.build(stockCode);
                        }
                        return builder.queryParam("code", stockCode).build();
                    })
                    .header("Authorization", "Bearer " + token)
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
            String name      = textValue(data, stockCode, "name", "stockName", "stock_name", "koreanName", "shortName");
            Long  marketCap  = nullableLongValue(data, "marketCap", "market_cap");
            Long  volume     = nullableLongValue(data, "volume", "accTradeVolume", "tradingVolume");

            return Optional.of(StockQuote.of(stockCode, name, price, change, changePct, marketCap, volume));

        } catch (Exception ex) {
            log.warn("[Toss] quote fetch failed stockCode={} reason={}", stockCode, ex.getMessage());
            return Optional.empty();
        }
    }

    private String getAccessToken() {
        Instant now = Instant.now();
        if (accessToken != null && now.isBefore(accessTokenExpiresAt.minusSeconds(TOKEN_EXPIRY_SAFETY_SECONDS))) {
            return accessToken;
        }
        synchronized (this) {
            now = Instant.now();
            if (accessToken != null && now.isBefore(accessTokenExpiresAt.minusSeconds(TOKEN_EXPIRY_SAFETY_SECONDS))) {
                return accessToken;
            }

            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("grant_type", properties.grantType());
            form.add("client_id", properties.clientId());
            form.add("client_secret", properties.clientSecret());

            JsonNode tokenResponse = tossInvestRestClient.post()
                    .uri(properties.tokenPath())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(JsonNode.class);

            String token = tokenResponse == null ? "" : tokenResponse.path("access_token").asText("");
            if (token.isBlank()) {
                throw new IllegalStateException("Toss token response did not contain access_token");
            }

            long expiresIn = tokenResponse.path("expires_in").asLong(3600);
            accessToken = token;
            accessTokenExpiresAt = Instant.now().plusSeconds(Math.max(120, expiresIn));
            return accessToken;
        }
    }

    private JsonNode extractPayload(JsonNode root) {
        if (root == null) {
            return null;
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
