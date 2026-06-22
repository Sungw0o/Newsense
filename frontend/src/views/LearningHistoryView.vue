<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import BaseBadge from '../components/common/BaseBadge.vue'

const router = useRouter()

// 모의 통계 데이터
const stats = ref({
  totalRead: 12,
  quizAccuracy: 85,
  streak: 5, // 연속 학습 일수
})

// 모의 기사 학습 이력
const historyList = ref([
  {
    id: 1,
    title: '기준금리 인하가 청년 전세대출에 미치는 영향',
    category: '금융',
    difficulty: '초급',
    date: '2026-06-22',
    hasQuiz: true,
    hasReview: true,
  },
  {
    id: 2,
    title: 'LTV와 DSR 규제 완화, 무엇이 달라지나?',
    category: '부동산',
    difficulty: '중급',
    date: '2026-06-20',
    hasQuiz: true,
    hasReview: false,
  },
  {
    id: 4,
    title: '2026 하반기 글로벌 원자재 가격 전망',
    category: '거시경제',
    difficulty: '고급',
    date: '2026-06-18',
    hasQuiz: false,
    hasReview: true,
  }
])
const navigateToDetail = (id) => {
  router.push(`/articles/${id}`)
}
</script>

<template>
  <div class="max-w-6xl mx-auto px-4 py-8">
    <header class="mb-8">
      <h1 class="text-3xl font-extrabold text-slate-900 mb-2">내 학습 관리</h1>
      <p class="text-slate-500 font-light">지금까지 뉴스엔스에서 차곡차곡 쌓아올린 나의 경제 근육을 확인해 보세요.</p>
    </header>

    <!-- Stats Dashboard -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-10">
      <!-- Read count card -->
      <div class="bg-white rounded-2xl border border-slate-200 p-6 flex items-center justify-between shadow-sm">
        <div>
          <span class="text-slate-400 text-xs font-semibold uppercase tracking-wider block mb-1">총 읽은 기사</span>
          <span class="text-3xl font-black text-slate-800">{{ stats.totalRead }} <span class="text-lg font-light text-slate-400">개</span></span>
        </div>
        <div class="w-12 h-12 bg-primary-100 rounded-xl flex items-center justify-center text-primary-600 text-xl font-bold">
          📰
        </div>
      </div>

      <!-- Accuracy card -->
      <div class="bg-white rounded-2xl border border-slate-200 p-6 flex items-center justify-between shadow-sm">
        <div>
          <span class="text-slate-400 text-xs font-semibold uppercase tracking-wider block mb-1">퀴즈 평균 정답률</span>
          <span class="text-3xl font-black text-slate-800">{{ stats.quizAccuracy }} <span class="text-lg font-light text-slate-400">%</span></span>
        </div>
        <div class="w-12 h-12 bg-brand-100 rounded-xl flex items-center justify-center text-brand-600 text-xl font-bold">
          🎯
        </div>
      </div>

      <!-- Streak card -->
      <div class="bg-white rounded-2xl border border-slate-200 p-6 flex items-center justify-between shadow-sm">
        <div>
          <span class="text-slate-400 text-xs font-semibold uppercase tracking-wider block mb-1">연속 학습 일수</span>
          <span class="text-3xl font-black text-slate-800">{{ stats.streak }} <span class="text-lg font-light text-slate-400">일째</span></span>
        </div>
        <div class="w-12 h-12 bg-accent-100 rounded-xl flex items-center justify-center text-accent-600 text-xl font-bold">
          🔥
        </div>
      </div>
    </div>

    <!-- History List -->
    <div class="bg-white rounded-3xl border border-slate-200/80 p-8 shadow-premium">
      <h2 class="text-lg font-bold text-slate-800 mb-6 border-b border-slate-100 pb-4">
        최근 읽은 뉴스 & 퀴즈 이력
      </h2>

      <div class="divide-y divide-slate-100">
        <div 
          v-for="item in historyList" 
          :key="item.id"
          class="py-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4 first:pt-0 last:pb-0"
        >
          <div>
            <div class="flex items-center gap-2 mb-2">
              <BaseBadge :value="item.category" />
              <span class="text-xs text-slate-400">{{ item.date }}</span>
            </div>
            <h3 
              @click="navigateToDetail(item.id)"
              class="text-base font-bold text-slate-800 hover:text-primary-600 cursor-pointer"
            >
              {{ item.title }}
            </h3>
          </div>

          <div class="flex items-center gap-2">
            <!-- Quiz Badge -->
            <span 
              class="text-xs px-2.5 py-1 rounded-lg border font-medium"
              :class="item.hasQuiz 
                ? 'bg-brand-50 border-brand-200 text-brand-700' 
                : 'bg-slate-50 border-slate-200 text-slate-400'"
            >
              🧩 퀴즈 풀기 {{ item.hasQuiz ? '완료' : '미진행' }}
            </span>
            <!-- Review Badge -->
            <span 
              class="text-xs px-2.5 py-1 rounded-lg border font-medium"
              :class="item.hasReview 
                ? 'bg-primary-50 border-primary-200 text-primary-700' 
                : 'bg-slate-50 border-slate-200 text-slate-400'"
            >
              ✏️ 요약 리뷰 {{ item.hasReview ? '완료' : '미작성' }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
