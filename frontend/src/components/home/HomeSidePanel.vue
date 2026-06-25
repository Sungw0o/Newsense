<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { indicatorApi } from '../../api/indicatorApi'
import { learningApi } from '../../api/learningApi'
import ragApi from '../../api/ragApi'

const router = useRouter()

// ── 경제 지표 ──────────────────────────────────────────────
const indicators = ref(null)
const indicatorLoading = ref(true)

async function loadIndicators() {
  try {
    const res = await indicatorApi.getIndicators()
    indicators.value = res.data?.data ?? res.data
  } catch {
    // 실패 시 null 유지 (UI에서 스켈레톤 표시)
  } finally {
    indicatorLoading.value = false
  }
}

function formatValue(item) {
  if (item.value == null) return '-'
  if (item.key === 'BOK_RATE') return `${item.value.toFixed(2)}%`
  if (item.key === 'USD_KRW') return `${Math.round(item.value).toLocaleString()}원`
  return item.value.toLocaleString(undefined, { maximumFractionDigits: 2 })
}

function formatChange(item) {
  if (item.change == null) return null
  const sign = item.change >= 0 ? '+' : ''
  return `${sign}${item.change.toFixed(2)}`
}

// ── 학습 현황 ──────────────────────────────────────────────
const stats = ref(null)
const statsLoading = ref(true)

async function loadStats() {
  try {
    const res = await learningApi.getStats()
    stats.value = res.data?.data ?? res.data
  } catch {
    // 비로그인 포함 — 무시
  } finally {
    statsLoading.value = false
  }
}

// ── 추천 용어 ──────────────────────────────────────────────
const terms = ref([])
const termsLoading = ref(true)

async function loadTerms() {
  try {
    const res = await ragApi.getRecommendations(4)
    terms.value = (res.data?.data ?? res.data ?? []).slice(0, 4)
  } catch {
    // 무시
  } finally {
    termsLoading.value = false
  }
}

onMounted(() => {
  loadIndicators()
  loadStats()
  loadTerms()
})
</script>

<template>
  <aside class="side-panel">
    <!-- 1. 경제 지표 ────────────────────────────────────── -->
    <section class="panel-section">
      <div class="section-head">
        <span class="section-icon">📊</span>
        <h3 class="section-title">실시간 경제 지표</h3>
        <span v-if="indicators?.status === 'STALE'" class="stale-badge">캐시</span>
        <span v-else-if="indicators?.status === 'MOCK'" class="stale-badge mock">준비 중</span>
      </div>

      <div v-if="indicatorLoading" class="skeleton-list">
        <div v-for="n in 4" :key="n" class="skeleton-row"></div>
      </div>

      <template v-else-if="indicators?.items?.length">
        <p v-if="indicators.insight" class="indicator-insight">{{ indicators.insight }}</p>
        <ul class="indicator-list">
          <li v-for="item in indicators.items" :key="item.key" class="indicator-row">
            <span class="ind-label">{{ item.label }}</span>
            <span class="ind-right">
              <span class="ind-value">{{ formatValue(item) }}</span>
              <span
                v-if="formatChange(item)"
                class="ind-change"
                :class="item.trend === 'UP' ? 'up' : item.trend === 'DOWN' ? 'down' : ''"
              >
                {{ item.trend === 'UP' ? '▲' : item.trend === 'DOWN' ? '▼' : '' }}
                {{ Math.abs(item.change).toFixed(2) }}
              </span>
            </span>
          </li>
        </ul>
      </template>
      <p v-else class="empty-hint">지표를 불러올 수 없습니다.</p>
    </section>

    <!-- 2. 추천 용어 ────────────────────────────────────── -->
    <section class="panel-section" v-if="!termsLoading && terms.length">
      <div class="section-head">
        <span class="section-icon">💡</span>
        <h3 class="section-title">오늘의 경제 용어</h3>
      </div>
      <ul class="term-list">
        <li
          v-for="term in terms"
          :key="term.name ?? term.term"
          class="term-row"
          @click="router.push('/')"
        >
          <span class="term-name">{{ term.name ?? term.term }}</span>
          <span class="term-arrow">→</span>
        </li>
      </ul>
    </section>

    <!-- 3. 학습 현황 ────────────────────────────────────── -->
    <section class="panel-section" v-if="stats && !statsLoading">
      <div class="section-head">
        <span class="section-icon">🎯</span>
        <h3 class="section-title">나의 학습 현황</h3>
      </div>
      <div class="stats-grid">
        <div class="stat-card">
          <span class="stat-value">{{ stats.totalReadArticles ?? 0 }}</span>
          <span class="stat-label">읽은 기사</span>
        </div>
        <div class="stat-card">
          <span class="stat-value">{{ stats.correctRate != null ? `${Math.round(stats.correctRate)}%` : '-' }}</span>
          <span class="stat-label">퀴즈 정답률</span>
        </div>
        <div class="stat-card">
          <span class="stat-value">{{ stats.streakDays ?? 0 }}일</span>
          <span class="stat-label">연속 학습</span>
        </div>
        <div class="stat-card">
          <span class="stat-value">{{ stats.totalQuizCount ?? 0 }}</span>
          <span class="stat-label">푼 퀴즈</span>
        </div>
      </div>
    </section>
  </aside>
</template>

<style scoped>
.side-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;
}

/* 각 섹션 카드 */
.panel-section {
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 14px;
  padding: 18px 18px 16px;
  backdrop-filter: blur(16px);
}

.dark .panel-section {
  background: rgba(20, 24, 34, 0.68);
  border-color: rgba(255, 255, 255, 0.1);
}

/* 섹션 헤더 */
.section-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
}

.section-icon {
  font-size: 16px;
  line-height: 1;
}

.section-title {
  margin: 0;
  font-size: 13.5px;
  font-weight: 800;
  color: var(--ink, #0a0d12);
  flex: 1;
}

.dark .section-title {
  color: #f4f6fa;
}

.stale-badge {
  font-size: 10px;
  font-weight: 700;
  padding: 2px 7px;
  border-radius: 999px;
  background: #e8f2ff;
  color: #0a4a99;
}

.stale-badge.mock {
  background: #f0ebff;
  color: #5a3bbf;
}

/* 경제 지표 리스트 */
.indicator-insight {
  font-size: 12px;
  color: var(--ink-2, #4a5161);
  line-height: 1.5;
  margin: 0 0 12px;
  padding: 8px 10px;
  background: rgba(0, 132, 255, 0.06);
  border-radius: 8px;
  border-left: 3px solid #0084ff;
}

.dark .indicator-insight {
  background: rgba(0, 132, 255, 0.12);
  color: #a4adbf;
}

.indicator-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.indicator-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 9px 10px;
  border-radius: 8px;
  transition: background .12s;
}

.indicator-row:hover {
  background: rgba(0, 0, 0, 0.03);
}

.dark .indicator-row:hover {
  background: rgba(255, 255, 255, 0.05);
}

.ind-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--ink-2, #4a5161);
}

.dark .ind-label {
  color: #a4adbf;
}

.ind-right {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.ind-value {
  font-size: 14px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  color: var(--ink, #0a0d12);
}

.dark .ind-value {
  color: #f4f6fa;
}

.ind-change {
  font-size: 11px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  color: var(--ink-3);
}

.ind-change.up   { color: #e03b3b; }
.ind-change.down { color: #1d7fd4; }

/* 추천 용어 */
.term-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.term-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 9px 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: background .12s;
}

.term-row:hover {
  background: rgba(0, 132, 255, 0.06);
}

.dark .term-row:hover {
  background: rgba(0, 132, 255, 0.12);
}

.term-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--ink, #0a0d12);
}

.dark .term-name {
  color: #e2e8f0;
}

.term-arrow {
  font-size: 12px;
  color: var(--ink-3);
}

/* 학습 현황 */
.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.stat-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
  padding: 12px 8px;
  background: rgba(0, 0, 0, 0.03);
  border-radius: 10px;
}

.dark .stat-card {
  background: rgba(255, 255, 255, 0.05);
}

.stat-value {
  font-size: 20px;
  font-weight: 800;
  color: var(--ink, #0a0d12);
  font-variant-numeric: tabular-nums;
}

.dark .stat-value {
  color: #f4f6fa;
}

.stat-label {
  font-size: 11px;
  font-weight: 600;
  color: var(--ink-3, #8a93a3);
}

/* 스켈레톤 */
.skeleton-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.skeleton-row {
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(90deg, rgba(0,0,0,0.05) 25%, rgba(0,0,0,0.09) 50%, rgba(0,0,0,0.05) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
}

@keyframes shimmer {
  to { background-position: -200% 0; }
}

.empty-hint {
  font-size: 12px;
  color: var(--ink-3);
  text-align: center;
  padding: 8px 0;
  margin: 0;
}
</style>
