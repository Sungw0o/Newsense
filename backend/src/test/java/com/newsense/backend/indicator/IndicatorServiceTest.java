package com.newsense.backend.indicator;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("IndicatorService unit tests")
class IndicatorServiceTest {

    @Spy
    @InjectMocks
    IndicatorService indicatorService;

    @Mock StringRedisTemplate redisTemplate;
    @Mock ObjectMapper objectMapper;
    @Mock ValueOperations<String, String> valueOps;

    private static IndicatorResponse sampleResponse() {
        List<IndicatorItem> items = List.of(
                IndicatorItem.of("USD_KRW", "달러/원", 1380.0, "원", null, null),
                IndicatorItem.of("KOSPI", "코스피", 2600.0, "pt", 5.0, 0.19),
                IndicatorItem.of("KOSDAQ", "코스닥", 860.0, "pt", -1.0, -0.12),
                IndicatorItem.of("BOK_RATE", "기준금리", 2.75, "%", null, null)
        );
        return new IndicatorResponse("OK", "코스피 2600pt · 달러/원 1380원", items, LocalDateTime.now());
    }

    @Test
    void getLatest_cacheHit_returnsStaleResponse() throws Exception {
        String json = "{\"status\":\"OK\"}";
        IndicatorResponse cached = sampleResponse();
        given(redisTemplate.opsForValue()).willReturn(valueOps);
        given(valueOps.get("indicator:latest")).willReturn(json);
        given(objectMapper.readValue(json, IndicatorResponse.class)).willReturn(cached);

        IndicatorResponse result = indicatorService.getLatest();

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo("STALE");
        assertThat(result.items()).isNotNull();
    }

    @Test
    void getLatest_cacheMiss_callsRefresh() {
        IndicatorResponse mockResp = sampleResponse();
        given(redisTemplate.opsForValue()).willReturn(valueOps);
        given(valueOps.get("indicator:latest")).willReturn(null);
        doReturn(mockResp).when(indicatorService).refresh();

        IndicatorResponse result = indicatorService.getLatest();

        assertThat(result).isEqualTo(mockResp);
        verify(indicatorService).refresh();
    }

    @Test
    void refresh_externalApiFails_returnsMockResponse() {
        IndicatorResponse result = indicatorService.refresh();

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo("MOCK");
        assertThat(result.items()).isNotEmpty();
        assertThat(result.items()).anyMatch(item -> "BOK_RATE".equals(item.key()));
    }

    @Test
    void indicatorItem_trendCalculation() {
        IndicatorItem up   = IndicatorItem.of("KOSPI", "코스피", 2600.0, "pt", 10.0, 0.38);
        IndicatorItem down = IndicatorItem.of("KOSPI", "코스피", 2600.0, "pt", -5.0, -0.19);
        IndicatorItem flat = IndicatorItem.of("BOK_RATE", "기준금리", 2.75, "%", null, null);

        assertThat(up.trend()).isEqualTo("UP");
        assertThat(down.trend()).isEqualTo("DOWN");
        assertThat(flat.trend()).isEqualTo("FLAT");
    }
}
