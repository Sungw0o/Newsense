<script setup>
import { computed } from 'vue'
import BaseBadge from '../common/BaseBadge.vue'

const props = defineProps({
  article: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['click'])

const difficultyClasses = computed(() => {
  const base = 'text-xs font-semibold px-2 py-0.5 rounded-md border transition-all duration-300'
  const themes = {
    '초급': 'bg-brand-500/10 border-brand-500/20 text-brand-400',
    '중급': 'bg-secondary-500/10 border-secondary-500/20 text-secondary-400',
    '고급': 'bg-accent-500/10 border-accent-500/20 text-accent-400',
    'default': 'bg-slate-500/10 border-slate-500/20 text-slate-400'
  }
  return `${base} ${themes[props.article.difficulty] || themes['default']}`
})

const handleClick = () => {
  emit('click', props.article.id)
}
</script>

<template>
  <div 
    @click="handleClick"
    class="group glass-panel glass-panel-hover rounded-2xl p-6 flex flex-col justify-between cursor-pointer"
  >
    <div>
      <!-- Tags and read time -->
      <div class="flex items-center gap-2 mb-4">
        <BaseBadge :value="article.category" />
        <span class="text-xs text-slate-400 select-none">• {{ article.readTime }} 소요</span>
      </div>

      <!-- Title -->
      <h3 class="text-lg font-bold text-white mb-3 group-hover:text-primary-400 transition-colors duration-300 leading-snug">
        {{ article.title }}
      </h3>

      <!-- Preview Description -->
      <p class="text-slate-400 text-sm font-light mb-6 line-clamp-3 leading-relaxed">
        {{ article.preview }}
      </p>
    </div>

    <!-- Bottom Meta Information -->
    <div class="flex items-center justify-between border-t border-white/5 pt-4 mt-auto">
      <div class="flex items-center gap-1.5">
        <span class="text-xs text-slate-500 select-none">난이도</span>
        <span :class="difficultyClasses">
          {{ article.difficulty }}
        </span>
      </div>
      <span class="text-xs text-slate-500 font-light select-none">{{ article.createdAt }}</span>
    </div>
  </div>
</template>
