<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { indicatorApi } from '../../api/indicatorApi'

const data = ref(null)
const prev = ref(null)
let timer = null

const fmt = (v, decimals = 2) => v != null ? Number(v).toLocaleString('ko-KR', { minimumFractionDigits: decimals, maximumFractionDigits: decimals }) : '—'

const trendClass = (key) => {
  if (!data.value || !prev.value) return ''
  const cur = data.value[key]
  const old = prev.value[key]
  if (cur == null || old == null) return ''
  if (cur > old) return 'up'
  if (cur < old) return 'down'
  return ''
}

const load = async () => {
  try {
    const res = await indicatorApi.getIndicators()
    prev.value = data.value
    data.value = res.data?.data || res.data
  } catch {
    // silent
  }
}

onMounted(() => {
  load()
  timer = setInterval(load, 120_000)
})
onUnmounted(() => clearInterval(timer))
</script>

<template>
  <aside class="indicator-widget">
    <div class="widget-head">
      <span class="widget-icon">📊</span>
      <span class="widget-title">금융 지표</span>
      <span class="live-dot"></span>
    </div>

    <div v-if="!data" class="widget-loading">
      <span class="skel-line"></span>
      <span class="skel-line short"></span>
      <span class="skel-line"></span>
      <span class="skel-line short"></span>
    </div>

    <div v-else class="indicator-list">
      <div class="indicator-row">
        <span class="ind-label">KOSPI</span>
        <span class="ind-value" :class="trendClass('kospi')">
          {{ fmt(data.kospi, 2) }}
          <span class="arrow" v-if="trendClass('kospi') === 'up'">▲</span>
          <span class="arrow" v-else-if="trendClass('kospi') === 'down'">▼</span>
        </span>
      </div>
      <div class="indicator-row">
        <span class="ind-label">KOSDAQ</span>
        <span class="ind-value" :class="trendClass('kosdaq')">
          {{ fmt(data.kosdaq, 2) }}
          <span class="arrow" v-if="trendClass('kosdaq') === 'up'">▲</span>
          <span class="arrow" v-else-if="trendClass('kosdaq') === 'down'">▼</span>
        </span>
      </div>
      <div class="indicator-row">
        <span class="ind-label">원/달러</span>
        <span class="ind-value" :class="trendClass('usdKrwRate')">
          {{ fmt(data.usdKrwRate, 1) }}원
          <span class="arrow" v-if="trendClass('usdKrwRate') === 'up'">▲</span>
          <span class="arrow" v-else-if="trendClass('usdKrwRate') === 'down'">▼</span>
        </span>
      </div>
      <div class="indicator-row">
        <span class="ind-label">기준금리</span>
        <span class="ind-value neutral">{{ fmt(data.bokBaseRate, 2) }}%</span>
      </div>
    </div>

    <p v-if="data" class="widget-updated">2분마다 갱신</p>
  </aside>
</template>

<style scoped>
.indicator-widget {
  position: sticky;
  top: 80px;
  padding: 18px 16px;
  background: rgba(255, 255, 255, 0.65);
  border: 1px solid rgba(0, 0, 0, 0.07);
  border-radius: 20px;
  backdrop-filter: blur(40px) saturate(160%);
  -webkit-backdrop-filter: blur(40px) saturate(160%);
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.85), 0 4px 16px -6px rgba(20,40,80,0.08);
}
.dark .indicator-widget {
  background: rgba(20,24,34,0.60);
  border-color: rgba(255,255,255,0.10);
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.08), 0 4px 16px -6px rgba(0,0,0,0.35);
}

.widget-head {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 14px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--line);
}
.widget-icon { font-size: 15px; }
.widget-title {
  font-size: 13px;
  font-weight: 700;
  color: var(--ink);
  flex: 1;
}
.dark .widget-title { color: #f4f6fa; }

.live-dot {
  width: 7px; height: 7px;
  border-radius: 50%;
  background: #22c55e;
  animation: pulse-dot 2s infinite;
}
@keyframes pulse-dot {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.indicator-list { display: flex; flex-direction: column; gap: 10px; }

.indicator-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.ind-label {
  font-size: 12px;
  color: var(--ink-3);
  font-family: 'Nanum Gothic', monospace;
}

.ind-value {
  font-size: 13px;
  font-weight: 700;
  color: var(--ink);
  display: flex;
  align-items: center;
  gap: 3px;
}
.dark .ind-value { color: #e0e4ef; }
.ind-value.up { color: #ef4444; }
.ind-value.down { color: #3b82f6; }
.ind-value.neutral { color: var(--ink-2); }

.arrow { font-size: 10px; }

.widget-updated {
  margin: 12px 0 0;
  font-size: 10.5px;
  color: var(--ink-3);
  text-align: right;
  font-family: 'Nanum Gothic', monospace;
}

/* Skeleton */
.widget-loading { display: flex; flex-direction: column; gap: 10px; }
.skel-line {
  display: block;
  height: 14px;
  border-radius: 6px;
  background: linear-gradient(90deg, var(--line) 25%, rgba(0,0,0,0.04) 50%, var(--line) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.2s infinite;
}
.dark .skel-line {
  background: linear-gradient(90deg, rgba(255,255,255,0.06) 25%, rgba(255,255,255,0.10) 50%, rgba(255,255,255,0.06) 75%);
  background-size: 200% 100%;
}
.skel-line.short { width: 60%; }
@keyframes shimmer { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }
</style>
