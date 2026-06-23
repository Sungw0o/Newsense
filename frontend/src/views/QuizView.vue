<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useQuizStore } from '../stores/useQuizStore'
import BaseButton from '../components/common/BaseButton.vue'
import LoadingSpinner from '../components/common/LoadingSpinner.vue'

const route = useRoute()
const router = useRouter()
const articleId = route.params.id

const quizStore = useQuizStore()
const { quizzes, currentQuizIndex, answers, isLoading } = storeToRefs(quizStore)

const currentQuiz = computed(() => quizStore.currentQuiz)
const progress = computed(() => quizStore.progressPercentage)

const selectAnswer = (answer) => {
  if (currentQuiz.value) {
    quizStore.saveAnswer(currentQuiz.value.id, answer)
  }
}

const nextQuiz = () => {
  quizStore.nextQuiz()
}

const prevQuiz = () => {
  quizStore.prevQuiz()
}

const submitQuiz = async () => {
  // 답변 미선택 시 경고
  const unansweredCount = quizzes.value.filter(q => !answers.value[q.id]).length
  if (unansweredCount > 0) {
    if (!confirm('아직 풀지 않은 문제가 있습니다. 그래도 제출하시겠습니까?')) {
      return
    }
  }

  try {
    await quizStore.submitAnswers()
    // 채점 처리 후 결과 페이지 이동
    router.push(`/quiz/${articleId}/result`)
  } catch (err) {
    alert('퀴즈 정답 제출에 실패했습니다. 다시 시도해 주세요.')
  }
}

onMounted(async () => {
  try {
    await quizStore.fetchQuizzes(articleId)
  } catch (err) {
    console.error('Failed to fetch quizzes:', err)
  }
})
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

    <!-- Loading State -->
    <div v-if="isLoading" class="flex flex-col items-center justify-center py-20 gap-4">
      <LoadingSpinner />
      <p class="text-sm text-slate-400 font-light select-none">AI가 퀴즈를 출제하고 있습니다...</p>
    </div>

    <!-- Empty State -->
    <div v-else-if="quizzes.length === 0" class="text-center py-20 text-slate-400">
      ⚠️ 이 기사에는 퀴즈가 준비되어 있지 않습니다.
    </div>

    <!-- Main Content -->
    <div v-else>
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
              문제 {{ currentQuizIndex + 1 }} / {{ quizzes.length }}
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
              :class="answers[currentQuiz.id] === opt 
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
              :class="answers[currentQuiz.id] === opt 
                ? 'bg-primary-50 border-primary-500 text-primary-700 shadow-sm' 
                : 'bg-white border-slate-200 text-slate-600 hover:border-slate-300'"
            >
              <span 
                class="w-6 h-6 rounded-full border flex items-center justify-center text-xs font-bold mr-3"
                :class="answers[currentQuiz.id] === opt 
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
            :disabled="currentQuizIndex === 0"
            class="py-2.5 px-4 rounded-xl font-bold text-sm"
          >
            이전 문제
          </BaseButton>

          <BaseButton 
            v-if="currentQuizIndex < quizzes.length - 1"
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
  </div>
</template>

