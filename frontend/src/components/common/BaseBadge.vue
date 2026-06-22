<script setup>
import { computed } from 'vue'

const props = defineProps({
  value: {
    type: String,
    required: true
  }
})

const badgeClasses = computed(() => {
  const base = 'inline-flex items-center text-xs font-semibold px-2.5 py-1 rounded-lg border transition-colors duration-300'
  
  const themes = {
    '금융': 'bg-primary-50 border-primary-200 text-primary-700',
    '부동산': 'bg-amber-50 border-amber-200 text-amber-700',
    '주식': 'bg-brand-50 border-brand-200 text-brand-700',
    '환율': 'bg-secondary-50 border-secondary-200 text-secondary-700',
    '거시경제': 'bg-slate-100 border-slate-300 text-slate-700',
    'default': 'bg-slate-50 border-slate-200 text-slate-500'
  }

  return `${base} ${themes[props.value] || themes['default']}`
})
</script>

<template>
  <span :class="badgeClasses">
    {{ value }}
  </span>
</template>
