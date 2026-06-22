<script setup>
import { ref, onMounted } from 'vue'

const props = defineProps({
  id: { type: [String, Number], required: true },
  message: { type: String, required: true },
  type: {
    type: String,
    default: 'info', // info | success | error | warning
    validator: (v) => ['info', 'success', 'error', 'warning'].includes(v)
  },
  duration: { type: Number, default: 3000 }
})

const emit = defineEmits(['remove'])

const visible = ref(false)

const typeStyles = {
  info: 'border-primary-500/40 bg-dark-800/90 text-primary-300',
  success: 'border-brand-500/40 bg-dark-800/90 text-brand-300',
  error: 'border-accent-500/40 bg-dark-800/90 text-accent-300',
  warning: 'border-yellow-500/40 bg-dark-800/90 text-yellow-300'
}

const typeIcons = {
  info: 'ℹ️',
  success: '✅',
  error: '❌',
  warning: '⚠️'
}

onMounted(() => {
  // 마운트 후 애니메이션 진입
  requestAnimationFrame(() => { visible.value = true })
  setTimeout(() => {
    visible.value = false
    setTimeout(() => emit('remove', props.id), 300)
  }, props.duration)
})
</script>

<template>
  <div
    role="alert"
    aria-live="polite"
    :class="[
      'flex items-center gap-3 px-4 py-3 rounded-xl border backdrop-blur-md shadow-premium',
      'transition-all duration-300',
      typeStyles[type],
      visible ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-2'
    ]"
  >
    <span class="text-base leading-none" aria-hidden="true">{{ typeIcons[type] }}</span>
    <p class="text-sm font-medium flex-1">{{ message }}</p>
    <button
      class="ml-auto text-slate-400 hover:text-slate-200 transition-colors"
      aria-label="알림 닫기"
      @click="emit('remove', id)"
    >
      ✕
    </button>
  </div>
</template>
