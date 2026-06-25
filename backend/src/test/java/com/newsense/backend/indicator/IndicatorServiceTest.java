package com.newsense.backend.indicator;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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

    @BeforeEach
    void setUp() {
        given(redisTemplate.opsForValue()).willReturn(valueOps);
    }

    @Test
    void getLatest_cacheHit_returnsDeserializedResponse() throws Exception {
        String json = "{\"bokBaseRate\":3.5}";
        IndicatorResponse expected = new IndicatorResponse(1380.0, 3.5, 2600.0, 860.0, LocalDateTime.now());
        given(valueOps.get("indicator:latest")).willReturn(json);
        given(objectMapper.readValue(json, IndicatorResponse.class)).willReturn(expected);

        IndicatorResponse result = indicatorService.getLatest();

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getLatest_cacheMiss_callsRefresh() {
        IndicatorResponse mockResp = new IndicatorResponse(1380.0, 3.5, 2600.0, 860.0, LocalDateTime.now());
        given(valueOps.get("indicator:latest")).willReturn(null);
        doReturn(mockResp).when(indicatorService).refresh();

        IndicatorResponse result = indicatorService.getLatest();

        assertThat(result).isEqualTo(mockResp);
        verify(indicatorService).refresh();
    }

    @Test
    void refresh_externalApiFails_returnsMockResponse() {
        // HTTP calls will fail in test environment (no network) — caught internally
        IndicatorResponse result = indicatorService.refresh();

        assertThat(result).isNotNull();
        assertThat(result.bokBaseRate()).isEqualTo(3.5);
    }
}
