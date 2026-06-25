<template>
  <div 
    class="flex flex-col items-center justify-center py-12"
    role="status"
    aria-live="polite"
    aria-busy="true"
  >
    <!-- Spinning rings -->
    <div class="relative w-12 h-12">
      <div class="absolute w-full h-full border-4 border-slate-100 rounded-full"></div>
      <div class="absolute w-full h-full border-4 border-primary-600 border-t-transparent rounded-full animate-spin"></div>
    </div>
    <span class="text-xs text-slate-400 font-medium mt-4">데이터를 불러오는 중입니다...</span>
  </div>
</template>
