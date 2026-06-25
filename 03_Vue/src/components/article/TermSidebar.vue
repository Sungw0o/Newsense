<script setup>
import { ref } from 'vue'

defineProps({
  /** 기사 내 핵심 경제 용어 목록 */
  terms: {
    type: Array,
    default: () => [],
    // [{ term: String, definition: String }]
  }
})

const openTermId = ref(null)

function toggleTerm(termName) {
  openTermId.value = openTermId.value === termName ? null : termName
}
</script>

<template>
  <aside
    v-if="terms.length > 0"
    class="glass-panel rounded-2xl p-4"
    aria-label="기사 핵심 경제 용어"
  >
    <h3 class="text-sm font-bold text-white mb-3 flex items-center gap-2">
      <span aria-hidden="true">📚</span> 핵심 경제 용어
    </h3>

    <ul class="space-y-2">
      <li
        v-for="item in terms"
        :key="item.term"
        class="rounded-xl overflow-hidden border border-white/5"
      >
        <!-- 용어 헤더 버튼 -->
        <button
          type="button"
          :id="`term-btn-${item.term}`"
          :aria-expanded="openTermId === item.term"
          :aria-controls="`term-def-${item.term}`"
          class="w-full flex items-center justify-between px-3 py-2 text-left text-xs font-semibold text-primary-300 hover:bg-primary-500/10 transition-colors duration-200"
          @click="toggleTerm(item.term)"
        >
          {{ item.term }}
          <span
            class="transition-transform duration-200 text-slate-400"
            :class="openTermId === item.term ? 'rotate-180' : ''"
            aria-hidden="true"
          >▾</span>
        </button>

        <!-- 용어 설명 (아코디언) -->
        <Transition
          enter-active-class="transition-all duration-200 ease-out"
          enter-from-class="max-h-0 opacity-0"
          enter-to-class="max-h-40 opacity-100"
          leave-active-class="transition-all duration-150 ease-in"
          leave-from-class="max-h-40 opacity-100"
          leave-to-class="max-h-0 opacity-0"
        >
          <div
            v-if="openTermId === item.term"
            :id="`term-def-${item.term}`"
            :aria-labelledby="`term-btn-${item.term}`"
            class="px-3 pb-3 text-xs text-slate-400 leading-relaxed bg-white/3"
          >
            {{ item.definition }}
          </div>
        </Transition>
      </li>
    </ul>
  </aside>
</template>
