package com.newsense.backend.indicator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("IndicatorScheduler unit tests")
class IndicatorSchedulerTest {

    @InjectMocks
    IndicatorScheduler indicatorScheduler;

    @Mock
    IndicatorService indicatorService;

    @Test
    void refreshIndicators_delegatesToIndicatorService() {
        indicatorScheduler.refreshIndicators();

        verify(indicatorService).refresh();
    }
}
