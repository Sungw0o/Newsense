<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useQuizStore } from '../stores/useQuizStore'
import BaseButton from '../components/common/BaseButton.vue'

const route = useRoute()
const router = useRouter()
const articleId = route.params.id

const quizStore = useQuizStore()
const { quizResults } = storeToRefs(quizStore)

// 만약 퀴즈 결과가 없으면 (새로고침 등), 기사 본문으로 돌려보냅니다.
if (!quizResults.value) {
  router.replace(`/articles/${articleId}`)
}

const results = computed(() => quizResults.value?.results || [])
const score = computed(() => quizResults.value?.score || 0)
const total = computed(() => quizResults.value?.totalQuestions || 0)
</script>

<template>
  <div class="max-w-3xl mx-auto px-4 py-8">
    <!-- Result Card header -->
    <div class="bg-gradient-to-br from-primary-600 to-secondary-600 rounded-3xl p-8 text-center text-white shadow-premium mb-8">
      <h1 class="text-2xl font-bold mb-2">퀴즈를 모두 풀었습니다!</h1>
      <p class="text-primary-100 text-sm font-light mb-6">수고하셨습니다. 정오답 해설을 통해 지식을 다져보세요.</p>
      
      <div class="inline-flex items-baseline justify-center gap-1 bg-white/10 backdrop-blur-md rounded-2xl px-8 py-4 border border-white/20">
        <span class="text-sm text-primary-200">내 점수:</span>
        <span class="text-4xl font-black text-amber-300">{{ score }}</span>
        <span class="text-lg text-primary-200">/ {{ total }} 개</span>
      </div>
    </div>

    <!-- Review Details -->
    <div class="space-y-6 mb-8">
      <div 
        v-for="(res, idx) in results" 
        :key="res.quizId"
        class="bg-white rounded-2xl border p-6 shadow-sm transition-all duration-300 hover:border-slate-300"
        :class="res.isCorrect ? 'border-brand-200' : 'border-accent-200'"
      >
        <div class="flex items-center gap-2 mb-4">
          <span 
            class="text-xs font-bold px-2.5 py-1 rounded-lg"
            :class="res.isCorrect 
              ? 'bg-brand-100 text-brand-700' 
              : 'bg-accent-100 text-accent-700'"
          >
            Q{{ idx + 1 }}. {{ res.isCorrect ? '정답입니다' : '오답입니다' }}
          </span>
        </div>

        <h3 class="text-base font-bold text-slate-800 mb-4 leading-snug">
          {{ res.question }}
        </h3>

        <!-- Ans Info -->
        <div class="grid sm:grid-cols-2 gap-3 mb-4 text-sm font-light">
          <div class="p-3 bg-slate-50 rounded-xl flex items-center justify-between">
            <span class="text-slate-400">내가 입력한 답:</span>
            <span class="font-bold" :class="res.isCorrect ? 'text-slate-700' : 'text-accent-600'">{{ res.userAns }}</span>
          </div>
          <div class="p-3 bg-slate-50 rounded-xl flex items-center justify-between">
            <span class="text-slate-400">실제 정답:</span>
            <span class="font-bold text-brand-700">{{ res.correctAns }}</span>
          </div>
        </div>

        <!-- Explanation -->
        <div class="bg-amber-50/50 border border-amber-100 rounded-xl p-4 text-xs md:text-sm leading-relaxed text-slate-600">
          <strong class="block text-amber-800 font-bold mb-1">해설:</strong>
          {{ res.explanation }}
        </div>
      </div>
    </div>

    <!-- CTA -->
    <div class="flex flex-col sm:flex-row gap-4 justify-center items-center">
      <BaseButton 
        variant="outline" 
        @click="router.push(`/articles/${articleId}`)"
        class="w-full sm:w-auto py-3 px-6 rounded-xl font-bold"
      >
        &larr; 기사 다시 읽으러 가기
      </BaseButton>
      <BaseButton 
        variant="primary"
        @click="router.push(`/articles/${articleId}/review`)"
        class="w-full sm:w-auto py-3 px-8 rounded-xl font-bold bg-primary-600 hover:bg-primary-700 text-white shadow-md"
      >
        ✏️ 기사 요약 및 리뷰 작성하기
      </BaseButton>
    </div>
  </div>
</template>
