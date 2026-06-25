<script setup>
defineProps({
  /** 채점 결과 객체 */
  result: {
    type: Object,
    required: true,
    // { question, userAns, correctAnswer, isCorrect, explanation }
  },
  /** 문항 번호 (1-indexed) */
  index: { type: Number, default: 1 }
})
</script>

<template>
  <div
    :class="[
      'glass-panel rounded-2xl p-5 border-l-4',
      result.isCorrect ? 'border-l-brand-400' : 'border-l-accent-400'
    ]"
    role="region"
    :aria-label="`${index}번 문항 결과`"
  >
    <!-- 헤더: 문항 번호 + 정오답 뱃지 -->
    <div class="flex items-center justify-between mb-3">
      <span class="text-xs font-semibold text-slate-400">{{ index }}번 문항</span>
      <span
        :class="[
          'text-xs font-bold px-2.5 py-1 rounded-lg border',
          result.isCorrect
            ? 'bg-brand-500/10 border-brand-500/20 text-brand-400'
            : 'bg-accent-500/10 border-accent-500/20 text-accent-400'
        ]"
      >
        {{ result.isCorrect ? '✅ 정답' : '❌ 오답' }}
      </span>
    </div>

    <!-- 문제 -->
    <p class="text-sm font-semibold text-white mb-3 leading-relaxed">{{ result.question }}</p>

    <!-- 내 답 / 정답 -->
    <div class="grid grid-cols-2 gap-2 mb-3 text-xs">
      <div class="bg-white/5 rounded-lg px-3 py-2">
        <span class="text-slate-500 block mb-0.5">내 답</span>
        <span class="font-bold" :class="result.isCorrect ? 'text-brand-400' : 'text-accent-400'">
          {{ result.userAns }}
        </span>
      </div>
      <div class="bg-white/5 rounded-lg px-3 py-2">
        <span class="text-slate-500 block mb-0.5">정답</span>
        <span class="font-bold text-brand-400">{{ result.correctAnswer }}</span>
      </div>
    </div>

    <!-- 해설 -->
    <div class="bg-primary-500/5 border border-primary-500/10 rounded-xl px-3 py-2.5">
      <p class="text-xs text-slate-400 leading-relaxed">
        <span class="font-semibold text-primary-400 mr-1">💡 해설</span>
        {{ result.explanation }}
      </p>
    </div>
  </div>
</template>
