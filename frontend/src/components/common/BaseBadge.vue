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
    '금융': 'bg-primary-500/10 border-primary-500/20 text-primary-400',
    '부동산': 'bg-amber-500/10 border-amber-500/20 text-amber-400',
    '주식': 'bg-brand-500/10 border-brand-500/20 text-brand-400',
    '환율': 'bg-secondary-500/10 border-secondary-500/20 text-secondary-400',
    '거시경제': 'bg-indigo-500/10 border-indigo-500/20 text-indigo-400',
    'default': 'bg-slate-500/10 border-slate-500/20 text-slate-400'
  }

  return `${base} ${themes[props.value] || themes['default']}`
})
</script>

<template>
  <span :class="badgeClasses">
    {{ value }}
  </span>
</template>
