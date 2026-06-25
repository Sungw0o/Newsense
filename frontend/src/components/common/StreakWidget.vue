<script setup>
import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useHistoryStore } from '../../stores/useHistoryStore'

const historyStore = useHistoryStore()
const { stats } = storeToRefs(historyStore)

const streak = computed(() => stats.value?.consecutiveLearningDays ?? 0)
const weeklyDays = computed(() => stats.value?.weeklyLearningDays ?? 0)

const fireDots = computed(() => {
  return Array.from({ length: 7 }, (_, i) => i < weeklyDays.value)
})
</script>

<template>
  <aside class="streak-widget">
    <div class="widget-head">
      <span class="widget-icon">🔥</span>
      <span class="widget-title">학습 스트릭</span>
    </div>

    <div class="streak-number">
      <span class="streak-count">{{ streak }}</span>
      <span class="streak-unit">일 연속</span>
    </div>

    <div class="week-row">
      <span class="week-label">이번 주</span>
      <div class="fire-dots">
        <span
          v-for="(active, i) in fireDots"
          :key="i"
          class="fire-dot"
          :class="{ active }"
        >{{ active ? '🔥' : '·' }}</span>
      </div>
    </div>

    <p class="streak-tip">매일 기사를 읽고<br>스트릭을 이어가세요!</p>
  </aside>
</template>

<style scoped>
.streak-widget {
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
.dark .streak-widget {
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
}
.dark .widget-title { color: #f4f6fa; }

.streak-number {
  display: flex;
  align-items: baseline;
  gap: 5px;
  margin-bottom: 16px;
}
.streak-count {
  font-family: 'Fustat', sans-serif;
  font-weight: 800;
  font-size: 40px;
  color: #FF801E;
  line-height: 1;
}
.streak-unit {
  font-size: 14px;
  color: var(--ink-2);
  font-weight: 500;
}
.dark .streak-unit { color: #a4adbf; }

.week-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
}
.week-label {
  font-size: 11px;
  color: var(--ink-3);
  font-family: 'Nanum Gothic', monospace;
  white-space: nowrap;
}
.fire-dots { display: flex; gap: 4px; }
.fire-dot {
  font-size: 14px;
  line-height: 1;
  color: var(--ink-3);
  transition: transform 0.2s;
}
.fire-dot.active { transform: scale(1.2); }

.streak-tip {
  font-size: 11.5px;
  color: var(--ink-3);
  line-height: 1.6;
  margin: 0;
  text-align: center;
}
</style>
