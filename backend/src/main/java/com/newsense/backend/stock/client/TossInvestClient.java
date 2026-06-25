package com.newsense.backend.stock.client;

import com.newsense.backend.stock.config.TossInvestProperties;
import com.newsense.backend.stock.dto.StockQuote;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.util.Base64;
import java.util.Optional;

/**
 * Toss Invest Open API 클라이언트.
 *
 * <p>인증: HTTP Basic Auth — Authorization: Basic Base64(apiKey:secretKey)
 * <p>키 설정: 환경변수 TOSS_INVEST_API_KEY, TOSS_INVEST_SECRET_KEY (운영 .env)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TossInvestClient {

    private static final String QUOTE_PATH = "/v1/stock/price";

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
            String credential = Base64.getEncoder()
                    .encodeToString((properties.apiKey() + ":" + properties.secretKey()).getBytes());

            JsonNode root = tossInvestRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(QUOTE_PATH)
                            .queryParam("code", stockCode)
                            .build())
                    .header("Authorization", "Basic " + credential)
                    .retrieve()
                    .body(JsonNode.class);

            if (root == null || root.path("data").isMissingNode()) {
                log.warn("[Toss] empty response for stockCode={}", stockCode);
                return Optional.empty();
            }

            JsonNode data = root.path("data");
            long  price      = data.path("currentPrice").asLong(0);
            long  change     = data.path("priceChange").asLong(0);
            double changePct = data.path("priceChangeRate").asDouble(0.0);
            String name      = data.path("name").asText(stockCode);
            Long  marketCap  = data.has("marketCap")  ? data.path("marketCap").asLong()  : null;
            Long  volume     = data.has("volume")      ? data.path("volume").asLong()     : null;

            return Optional.of(StockQuote.of(stockCode, name, price, change, changePct, marketCap, volume));

        } catch (Exception ex) {
            log.warn("[Toss] quote fetch failed stockCode={} reason={}", stockCode, ex.getMessage());
            return Optional.empty();
        }
    }
}
