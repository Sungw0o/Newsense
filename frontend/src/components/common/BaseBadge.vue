<script setup>
import { computed } from 'vue'

const props = defineProps({
  /** 배지에 표시할 텍스트 */
  value: { type: String, required: true },
  /**
   * 배지 종류: 'category' | 'difficulty' | 'status'
   * - category: 뉴스 카테고리 (금융, 부동산, 주식, 환율, 거시경제)
   * - difficulty: 난이도 (초급, 중급, 고급)
   * - status: 상태 (해결됨, 미해결)
   */
  variant: { type: String, default: 'category' }
})

const categoryThemes = {
  '거시경제':   'bg-indigo-500/10 border-indigo-500/20 text-indigo-400',
  '금융/투자':  'bg-primary-500/10 border-primary-500/20 text-primary-400',
  '정책/제도':  'bg-amber-500/10 border-amber-500/20 text-amber-400',
  '기업/산업':  'bg-brand-500/10 border-brand-500/20 text-brand-400',
  '글로벌경제': 'bg-secondary-500/10 border-secondary-500/20 text-secondary-400',
}

const difficultyThemes = {
  '초급': 'bg-brand-500/10 border-brand-500/20 text-brand-400',
  '중급': 'bg-amber-500/10 border-amber-500/20 text-amber-400',
  '고급': 'bg-accent-500/10 border-accent-500/20 text-accent-400',
}

const statusThemes = {
  '해결됨': 'bg-brand-500/10 border-brand-500/20 text-brand-400',
  '미해결': 'bg-slate-500/10 border-slate-500/20 text-slate-400',
}

const defaultTheme = 'bg-slate-500/10 border-slate-500/20 text-slate-400'

const badgeClasses = computed(() => {
  const base = 'inline-flex items-center text-xs font-semibold px-2.5 py-1 rounded-lg border transition-colors duration-300'
  let colorClass = defaultTheme

  if (props.variant === 'difficulty') {
    colorClass = difficultyThemes[props.value] ?? defaultTheme
  } else if (props.variant === 'status') {
    colorClass = statusThemes[props.value] ?? defaultTheme
  } else {
    colorClass = categoryThemes[props.value] ?? defaultTheme
  }

  return `${base} ${colorClass}`
})
</script>

<template>
  <span :class="badgeClasses">
    {{ value }}
  </span>
</template>
