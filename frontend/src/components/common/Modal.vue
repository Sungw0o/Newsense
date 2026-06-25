<template>
  <div
    v-if="isOpen"
    class="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50 backdrop-blur-sm transition-opacity duration-300"
    @click.self="handleClose"
  >
    <div
      class="bg-white rounded-xl shadow-lg max-w-lg w-full mx-4 p-6 relative animate-[scaleIn_0.2s_ease-out]"
      role="dialog"
      aria-modal="true"
    >
      <button
        @click="handleClose"
        class="absolute top-2 right-2 text-gray-500 hover:text-gray-700"
        aria-label="닫기"
      >
        ×
      </button>
      <slot />
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits, watch, onUnmounted } from 'vue'

const props = defineProps({
  isOpen: { type: Boolean, default: false }
})

const emit = defineEmits(['close'])

function handleClose() {
  emit('close')
}

const handleKeyDown = (e) => {
  if (e.key === 'Escape' && props.isOpen) {
    handleClose()
  }
}

watch(() => props.isOpen, (newVal) => {
  if (newVal) {
    window.addEventListener('keydown', handleKeyDown)
  } else {
    window.removeEventListener('keydown', handleKeyDown)
  }
}, { immediate: true })

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeyDown)
})
</script>

<style scoped>
@keyframes scaleIn {
  from { opacity: 0; transform: scale(0.95); }
  to { opacity: 1; transform: scale(1); }
}
</style>
