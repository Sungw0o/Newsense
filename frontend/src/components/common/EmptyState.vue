<script setup>
defineProps({
  message: {
    type: String,
    default: '데이터가 존재하지 않습니다.'
  }
})
</script>

<template>
  <div class="flex flex-col items-center justify-center py-16 px-4 text-center border-2 border-dashed border-white/10 rounded-3xl bg-white/5 backdrop-blur-md">
    <div class="text-5xl mb-4 select-none opacity-60">📭</div>
    <h3 class="text-sm font-bold text-white mb-1">데이터 비어있음</h3>
    <p class="text-xs text-slate-400 font-light max-w-xs leading-relaxed">{{ message }}</p>
  </div>
</template>
