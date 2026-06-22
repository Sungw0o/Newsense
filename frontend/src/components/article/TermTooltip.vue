<script setup>
defineProps({
  /** 경제 용어명 */
  term: { type: String, required: true },
  /** 용어 설명 */
  definition: { type: String, required: true },
  /** 툴팁 표시 여부 */
  visible: { type: Boolean, default: false }
})

const emit = defineEmits(['close'])
</script>

<template>
  <span class="relative inline-block">
    <!-- 하이라이트된 텍스트 -->
    <slot />

    <!-- 툴팁 말풍선 -->
    <Transition
      enter-active-class="transition-all duration-200 ease-out"
      enter-from-class="opacity-0 scale-95 translate-y-1"
      enter-to-class="opacity-100 scale-100 translate-y-0"
      leave-active-class="transition-all duration-150 ease-in"
      leave-from-class="opacity-100 scale-100 translate-y-0"
      leave-to-class="opacity-0 scale-95 translate-y-1"
    >
      <div
        v-if="visible"
        class="absolute z-50 bottom-full left-1/2 -translate-x-1/2 mb-2 w-64 glass-panel rounded-xl p-3 shadow-premium pointer-events-none"
        role="tooltip"
        :aria-label="`${term} 설명`"
      >
        <!-- 화살표 -->
        <div
          class="absolute top-full left-1/2 -translate-x-1/2 w-0 h-0"
          style="border-left: 6px solid transparent; border-right: 6px solid transparent; border-top: 6px solid rgba(15,23,42,0.9);"
        />
        <p class="text-xs font-bold text-primary-400 mb-1">{{ term }}</p>
        <p class="text-xs text-slate-300 leading-relaxed">{{ definition }}</p>
      </div>
    </Transition>
  </span>
</template>
