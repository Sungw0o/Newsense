package com.newsense.backend.indicator;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 금융 지표 응답 DTO.
 *
 * @param status    데이터 신선도 — "OK" (실시간), "STALE" (캐시), "MOCK" (폴백)
 * @param insight   한 문�