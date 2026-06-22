<script setup>
defineProps({
  message: {
    type: String,
    required: true
  }
})
</script>

<template>
  <div 
    class="p-4 bg-accent-50 border border-accent-200 rounded-xl text-xs md:text-sm text-accent-700 flex items-start gap-2.5 animate-[shake_0.4s_ease-in-out]"
    role="alert"
    aria-live="assertive"
  >
    <span class="text-base select-none mt-0.5">⚠️</span>
    <div class="leading-relaxed">
      <strong class="font-bold block mb-0.5">오류가 발생했습니다</strong>
      <span class="font-light">{{ message }}</span>
    </div>
  </div>
</template>

<style scoped>
@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-4px); }
  75% { transform: translateX(4px); }
}
</style>
