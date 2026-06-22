<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BaseButton from '../components/common/BaseButton.vue'

const route = useRoute()
const router = useRouter()
const articleId = route.params.id

const currentIdx = ref(0)
const selectedAnswers = ref({}) // { quizId: answer }

// 모의 퀴즈 데이터
const quizzes = ref([
  {
    id: 101,
    type: 'OX',
    question: '기준금리가 인하되면 일반적으로 시중 은행의 대출 금리도 하락하는 경향이 있다.',
    options: ['O', 'X']
  },
  {
    id: 102,
    type: 'MULTIPLE',
    question: '이번 본문에서 설명한 한국은행 금리 인하의 직접적인 배경으로 올바르지 않은 것은 무엇인가요?',
    options: [
      '소비자물가 상승률의 안정세',
      '국내 내수 경제의 활성화 필요성',
      '부동산 거래량 폭증을 유도하기 위한 정책적 목적',
      '고금리 기조 유지에 따른 경기 침체 리스크 완화'
    ]
  }
])

const currentQuiz = computed(() => quizzes.value[currentIdx.value] || null)
const progress = computed(() => Math.round(((currentIdx.value + 1) / quizzes.value.length) * 100))

const selectAnswer = (answer) => {
  selectedAnswers.value[currentQuiz.value.id] = answer
}

const nextQuiz = () => {
  if (currentIdx.value < quizzes.value.length - 1) {
    currentIdx.value++
  }
}

const prevQuiz = () => {
  if (currentIdx.value > 0) {
    currentIdx.value--
  }
}

const submitQuiz = () => {
  // 답변 미선택 시 경고
  const unansweredCount = quizzes.value.filter(q => !selectedAnswers.value[q.id]).length
  if (unansweredCount > 0) {
    if (!confirm('아직 풀지 않은 문제가 있습니다. 그래도 제출하시겠습니까?')) {
      return
    }
  }

  // 모의 채점 처리 및 결과 페이지 이동
  router.push(`/quiz/${articleId}/result`)
}
</script>

<template>
  <div class="max-w-3xl mx-auto px-4 py-8">
    <!-- Header -->
    <div class="flex items-center justify-between mb-8">
      <button 
        @click="router.push(`/articles/${articleId}`)" 
        class="text-sm font-semibold text-slate-500 hover:text-primary-600 transition-colors duration-200"
      >
        &larr; 기사 본문으로
      </button>
      <span class="text-xs text-slate-400 font-semibold uppercase tracking-wider">
        QUIZ PROGRESS
      </span>
    </div>

    <!-- Progress Bar -->
    <div class="w-full bg-slate-100 h-2 rounded-full mb-8 relative overflow-hidden">
      <div 
        class="bg-gradient-to-r from-primary-500 to-secondary-500 h-full rounded-full transition-all duration-500"
        :style="{ width: `${progress}%` }"
      ></div>
    </div>

    <!-- Quiz Card -->
    <div v-if="currentQuiz" class="bg-white rounded-3xl border border-slate-200/80 p-8 shadow-premium mb-8 min-h-[300px] flex flex-col justify-between">
      <div>
        <div class="flex items-center gap-2 mb-4">
          <span class="text-xs font-black bg-primary-100 text-primary-700 px-2.5 py-1 rounded-lg">
            문제 {{ currentIdx + 1 }} / {{ quizzes.length }}
          </span>
          <span class="text-xs font-semibold bg-slate-100 text-slate-500 px-2.5 py-1 rounded-lg">
            {{ currentQuiz.type === 'OX' ? 'OX 퀴즈' : '객관식 퀴즈' }}
          </span>
        </div>

        <h2 class="text-xl md:text-2xl font-extrabold text-slate-800 mb-8 leading-snug">
          {{ currentQuiz.question }}
        </h2>

        <!-- Answer Options -->
        <div v-if="currentQuiz.type === 'OX'" class="grid grid-cols-2 gap-4">
          <button 
            v-for="opt in currentQuiz.options" 
            :key="opt"
            @click="selectAnswer(opt)"
            class="py-6 rounded-2xl border-2 text-2xl font-black transition-all duration-300 hover:scale-102 flex items-center justify-center"
            :class="selectedAnswers[currentQuiz.id] === opt 
              ? 'bg-primary-50 border-primary-500 text-primary-700 shadow-md' 
              : 'bg-white border-slate-200 text-slate-400 hover:border-slate-300 hover:text-slate-600'"
          >
            {{ opt }}
          </button>
        </div>

        <div v-else class="space-y-3">
          <button 
            v-for="(opt, idx) in currentQuiz.options" 
            :key="opt"
            @click="selectAnswer(opt)"
            class="w-full text-left p-4 rounded-xl border-2 font-medium text-sm md:text-base transition-all duration-300 hover:scale-102 flex items-center"
            :class="selectedAnswers[currentQuiz.id] === opt 
              ? 'bg-primary-50 border-primary-500 text-primary-700 shadow-sm' 
              : 'bg-white border-slate-200 text-slate-600 hover:border-slate-300'"
          >
            <span 
              class="w-6 h-6 rounded-full border flex items-center justify-center text-xs font-bold mr-3"
              :class="selectedAnswers[currentQuiz.id] === opt 
                ? 'bg-primary-600 border-primary-600 text-white' 
                : 'border-slate-300 text-slate-400 bg-slate-50'"
            >
              {{ idx + 1 }}
            </span>
            {{ opt }}
          </button>
        </div>
      </div>

      <!-- Navigation Buttons -->
      <div class="flex items-center justify-between border-t border-slate-100 pt-6 mt-8">
        <BaseButton 
          variant="outline" 
          @click="prevQuiz" 
          :disabled="currentIdx === 0"
          class="py-2.5 px-4 rounded-xl font-bold text-sm"
        >
          이전 문제
        </BaseButton>

        <BaseButton 
          v-if="currentIdx < quizzes.length - 1"
          variant="outline" 
          @click="nextQuiz" 
          class="py-2.5 px-4 rounded-xl font-bold text-sm"
        >
          다음 문제
        </BaseButton>

        <BaseButton 
          v-else
          variant="primary" 
          @click="submitQuiz" 
          class="py-2.5 px-6 rounded-xl font-bold text-sm bg-primary-600 hover:bg-primary-700 text-white shadow-md shadow-primary-100"
        >
          답안 제출하기
        </BaseButton>
      </div>
    </div>
  </div>
</template>
