<script setup>
import { computed, ref, onMounted } from 'vue'
import { indicatorApi } from '../../api/indicatorApi'

const items = ref([])
const status = ref(null)
const loading = ref(true)
const refreshing = ref(false)
const updatedAt = ref(null)

const fmt = (item) => {
  if (item.value == null) return '—'
  if (item.key === 'BOK_RATE') return `${item.value.toFixed(2)}%`
  if (item.key === 'USD_KRW') return `${Math.round(item.value).toLocaleString()}원`
  return item.value.toLocaleString('ko-KR', { maximumFractionDigits: 2 })
}

const fmtChange = (item) => {
  if (item.change == null) return null
  const sign = item.change > 0 ? '+' : item.change < 0 ? '-' : ''
  if (item.key === 'BOK_RATE') return `${sign}${item.change.toFixed(2)}%`
  if (item.key === 'USD_KRW') {
    const rate = item.changePct == null ? '' : ` (${sign}${Math.abs(item.changePct).toFixed(2)}%)`
    return `${sign}${Math.abs(item.change).toFixed(2)}원${rate}`
  }
  const rate = item.changePct == null ? '' : ` (${sign}${Math.abs(item.changePct).toFixed(2)}%)`
  return `${sign}${Math.abs(item.change).toFixed(2)}${rate}`
}

const load = async (manual = false) => {
  if (manual) refreshing.value = true
  try {
    const res = await indicatorApi.getIndicators()
    const raw = res?.data ?? res
    items.value = raw?.items ?? []
    status.value = raw?.status ?? null
    updatedAt.value = new Date()
  } catch {
    // silent
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

const footerText = computed(() => {
  const date = updatedAt.value ?? new Date()
  return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')} 기준`
})

onMounted(() => {
  load()
})
</script>

<template>
  <aside class="indicator-widget">
    <div class="widget-head">
      <span class="widget-title">금융 지표</span>
      <span v-if="status === 'MOCK'" class="badge mock">준비 중</span>
      <button
        class="refresh-btn"
        :class="{ spinning: refreshing }"
        :disabled="refreshing"
        @click="load(true)"
        title="새로고침"
        aria-label="지표 새로고침"
      >↻</button>
    </div>

    <div v-if="loading" class="skel-wrap">
      <div v-for="n in 3" :key="n" class="skel-row"></div>
    </div>

    <table v-else-if="items.length" class="ind-table">
      <thead>
        <tr>
          <th>지표</th>
          <th class="num">현재</th>
          <th class="num">변동</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in items" :key="item.key">
          <td class="label">{{ item.label }}</td>
          <td class="num val" :class="item.trend === 'UP' ? 'up' : item.trend === 'DOWN' ? 'down' : ''">{{ fmt(item) }}</td>
          <td
            class="num chg"
            :class="item.trend === 'UP' ? 'up' : item.trend === 'DOWN' ? 'down' : ''"
          >
            <template v-if="fmtChange(item)">
              {{ item.trend === 'UP' ? '▲' : item.trend === 'DOWN' ? '▼' : '' }}
              {{ fmtChange(item) }}
            </template>
            <span v-else class="flat">—</span>
          </td>
        </tr>
      </tbody>
    </table>

    <p v-else class="empty">지표를 불러올 수 없습니다.</p>

    <p class="widget-footer">
      {{ footerText }}
    </p>
  </aside>
</template>

<style scoped>
.indicator-widget {
  position: sticky;
  top: 80px;
  padding: 14px 14px 12px;
  background: rgba(255,255,255,0.70);
  border: 1px solid rgba(0,0,0,0.07);
  border-radius: 16px;
  backdrop-filter: blur(32px) saturate(160%);
  -webkit-backdrop-filter: blur(32px) saturate(160%);
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.85), 0 4px 14px -6px rgba(20,40,80,0.08);
}
.dark .indicator-widget {
  background: rgba(20,24,34,0.65);
  border-color: rgba(255,255,255,0.10);
}
.widget-head {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid rgba(0,0,0,0.07);
}
.dark .widget-head { border-color: rgba(255,255,255,0.09); }
.widget-title {
  font-size: 12.5px;
  font-weight: 800;
  color: var(--ink, #0a0d12);
  flex: 1;
}
.dark .widget-title { color: #f4f6fa; }
.badge {
  font-size: 9.5px;
  font-weight: 700;
  padding: 2px 6px;
  border-radius: 999px;
}
.badge.mock  { background: #ede9fe; color: #5b21b6; }
.refresh-btn {
  font-size: 15px;
  line-height: 1;
  background: none;
  border: none;
  color: var(--ink-3, #8a93a3);
  cursor: pointer;
  padding: 2px 3px;
  border-radius: 6px;
  transition: color .15s;
}
.refresh-btn:hover { color: #0084ff; }
.refresh-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.refresh-btn.spinning { animation: spin-once .6s linear; }
@keyframes spin-once { to { transform: rotate(360deg); } }
.ind-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 12.5px;
}
.ind-table thead th {
  padding: 0 4px 6px;
  font-size: 10.5px;
  font-weight: 700;
  color: var(--ink-3, #8a93a3);
  border-bottom: 1px solid rgba(0,0,0,0.07);
  white-space: nowrap;
}
.dark .ind-table thead th { border-color: rgba(255,255,255,0.09); }
.ind-table thead th.num { text-align: right; }
.ind-table tbody tr:hover td { background: rgba(0,0,0,0.025); }
.dark .ind-table tbody tr:hover td { background: rgba(255,255,255,0.04); }
.ind-table tbody td {
  padding: 6px 4px;
  border-bottom: 1px solid rgba(0,0,0,0.04);
  color: var(--ink, #0a0d12);
  vertical-align: middle;
}
.dark .ind-table tbody td { border-color: rgba(255,255,255,0.06); color: #e0e4ef; }
.ind-table tbody tr:last-child td { border-bottom: none; }
.ind-table td.label { font-weight: 600; color: var(--ink-2, #4a5161); }
.dark .ind-table td.label { color: #a4adbf; }
.ind-table td.num { text-align: right; font-variant-numeric: tabular-nums; }
.ind-table td.val { font-weight: 700; }
.ind-table td.val.up   { color: #e03b3b; }
.ind-table td.val.down { color: #1d7fd4; }
.ind-table td.chg {
  font-size: 11px;
  font-weight: 700;
  color: var(--ink-3, #8a93a3);
  white-space: nowrap;
}
.ind-table td.chg.up   { color: #e03b3b; }
.ind-table td.chg.down { color: #1d7fd4; }
.flat { color: var(--ink-3); opacity: .5; }
.skel-wrap { display: flex; flex-direction: column; gap: 7px; padding: 4px 0; }
.skel-row {
  height: 24px;
  border-radius: 6px;
  background: linear-gradient(90deg, rgba(0,0,0,0.05) 25%, rgba(0,0,0,0.09) 50%, rgba(0,0,0,0.05) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.3s infinite;
}
.dark .skel-row {
  background: linear-gradient(90deg, rgba(255,255,255,0.06) 25%, rgba(255,255,255,0.10) 50%, rgba(255,255,255,0.06) 75%);
  background-size: 200% 100%;
}
@keyframes shimmer { to { background-position: -200% 0; } }
.empty { font-size: 11.5px; color: var(--ink-3); text-align: center; padding: 8px 0; margin: 0; }
.widget-footer {
  display: flex;
  justify-content: flex-end;
  margin: 8px 0 0;
  font-size: 10px;
  color: var(--ink-3, #8a93a3);
}
</style>
