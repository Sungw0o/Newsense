<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  quiz: {
    type: Object,
    required: true,
    // { quizId, type: 'OX'|'MULTIPLE', question, options: [] }
  },
  /** 제출 중 여부 */
  loading: { type: Boolean, default: false }
})

const emit = defineEmits(['submit'])

const selectedAnswer = ref(null)

const isOX = computed(() => props.quiz.type === 'OX')

function selectAnswer(answer) {
  if (!props.loading) selectedAnswer.value = answer
}

function handleSubmit() {
  if (!selectedAnswer.value) return
  emit('submit', { quizId: props.quiz.quizId, userAns: selectedAnswer.value })
}
</script>

<template>
  <div class="glass-panel rounded-2xl p-6 space-y-5">
    <!-- 문제 -->
    <div class="space-y-2">
      <span class="text-xs font-semibold text-primary-400 uppercase tracking-wide">
        {{ isOX ? 'OX 퀴즈' : '객관식' }}
      </span>
      <p class="text-base font-semibold text-white leading-relaxed">
        {{ quiz.question }}
      </p>
    </div>

    <!-- OX 선택지 -->
    <div v-if="isOX" class="grid grid-cols-2 gap-3">
      <button
        v-for="opt in ['O', 'X']"
        :key="opt"
        type="button"
        :class="[
          'py-6 rounded-xl text-3xl font-black border-2 transition-all duration-200',
          selectedAnswer === opt
            ? opt === 'O'
              ? 'bg-brand-500/20 border-brand-400 text-brand-300 scale-105'
              : 'bg-accent-500/20 border-accent-400 text-accent-300 scale-105'
            : 'bg-white/5 border-white/10 text-slate-300 hover:border-white/30 hover:scale-102',
          loading ? 'cursor-not-allowed opacity-60' : 'cursor-pointer'
        ]"
        :aria-pressed="selectedAnswer === opt"
        :disabled="loading"
        @click="selectAnswer(opt)"
      >
        {{ opt }}
      </button>
    </div>

    <!-- 객관식 선택지 -->
    <div v-else class="space-y-2">
      <button
        v-for="(option, idx) in quiz.options"
        :key="idx"
        type="button"
        :class="[
          'w-full text-left px-4 py-3 rounded-xl border-2 text-sm transition-all duration-200',
          selectedAnswer === option
            ? 'bg-primary-500/20 border-primary-400 text-primary-200'
            : 'bg-white/5 border-white/10 text-slate-300 hover:border-white/30',
          loading ? 'cursor-not-allowed opacity-60' : 'cursor-pointer'
        ]"
        :aria-pressed="selectedAnswer === option"
        :disabled="loading"
        @click="selectAnswer(option)"
      >
        <span class="font-bold text-slate-400 mr-2">{{ idx + 1 }}.</span>
        {{ option }}
      </button>
    </div>

    <!-- 제출 버튼 -->
    <button
      type="button"
      :disabled="!selectedAnswer || loading"
      :class="[
        'w-full py-3 rounded-xl text-sm font-bold transition-all duration-200',
        selectedAnswer && !loading
          ? 'bg-primary-600 hover:bg-primary-700 text-white'
          : 'bg-slate-700 text-slate-500 cursor-not-allowed'
      ]"
      @click="handleSubmit"
    >
      <span v-if="loading" class="inline-block w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin mr-2" />
      {{ loading ? '채점 중...' : '답변 제출' }}
    </button>
  </div>
</template>
