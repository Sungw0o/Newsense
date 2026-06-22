<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useHistoryStore } from '../stores/useHistoryStore'
import BaseBadge from '../components/common/BaseBadge.vue'

const router = useRouter()
const historyStore = useHistoryStore()

// storeToRefs를 사용하여 Pinia 상태의 반응성을 유지하며 추출
const { history, stats, isLoading, error } = storeToRefs(historyStore)

onMounted(async () => {
  try {
    await Promise.all([
      historyStore.fetchHistory(),
      historyStore.fetchStats()
    ])
  } catch (err) {
    console.error('Failed to load history or stats:', err)
  }
})

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

    <!-- Error State -->
    <div v-if="error" class="mb-8 p-4 bg-accent-50 border border-accent-200 rounded-2xl text-accent-700 text-sm">
      ⚠️ {{ error }}
    </div>

    <!-- Stats Dashboard -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-10">
      <!-- Read count card -->
      <div class="bg-white rounded-2xl border border-slate-200 p-6 flex items-center justify-between shadow-sm">
        <div>
          <span class="text-slate-400 text-xs font-semibold uppercase tracking-wider block mb-1">총 읽은 기사</span>
          <span class="text-3xl font-black text-slate-800">
            <template v-if="isLoading">
              <span class="inline-block w-12 h-8 bg-slate-100 animate-pulse rounded"></span>
            </template>
            <template v-else>
              {{ stats?.totalRead || 0 }}
            </template>
            <span class="text-lg font-light text-slate-400 ml-1">개</span>
          </span>
        </div>
        <div class="w-12 h-12 bg-primary-100 rounded-xl flex items-center justify-center text-primary-600 text-xl font-bold">
          📰
        </div>
      </div>

      <!-- Accuracy card -->
      <div class="bg-white rounded-2xl border border-slate-200 p-6 flex items-center justify-between shadow-sm">
        <div>
          <span class="text-slate-400 text-xs font-semibold uppercase tracking-wider block mb-1">퀴즈 평균 정답률</span>
          <span class="text-3xl font-black text-slate-800">
            <template v-if="isLoading">
              <span class="inline-block w-12 h-8 bg-slate-100 animate-pulse rounded"></span>
            </template>
            <template v-else>
              {{ stats?.quizAccuracy || 0 }}
            </template>
            <span class="text-lg font-light text-slate-400 ml-1">%</span>
          </span>
        </div>
        <div class="w-12 h-12 bg-brand-100 rounded-xl flex items-center justify-center text-brand-600 text-xl font-bold">
          🎯
        </div>
      </div>

      <!-- Streak card -->
      <div class="bg-white rounded-2xl border border-slate-200 p-6 flex items-center justify-between shadow-sm">
        <div>
          <span class="text-slate-400 text-xs font-semibold uppercase tracking-wider block mb-1">연속 학습 일수</span>
          <span class="text-3xl font-black text-slate-800">
            <template v-if="isLoading">
              <span class="inline-block w-12 h-8 bg-slate-100 animate-pulse rounded"></span>
            </template>
            <template v-else>
              {{ stats?.streak || 0 }}
            </template>
            <span class="text-lg font-light text-slate-400 ml-1">일째</span>
          </span>
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

      <!-- Loading skeleton -->
      <div v-if="isLoading" class="space-y-4">
        <div v-for="n in 3" :key="n" class="py-5 animate-pulse flex justify-between items-center gap-4">
          <div class="flex-1 space-y-2">
            <div class="h-4 bg-slate-100 rounded w-1/4"></div>
            <div class="h-6 bg-slate-100 rounded w-3/4"></div>
          </div>
          <div class="w-24 h-8 bg-slate-100 rounded"></div>
        </div>
      </div>

      <!-- Empty state -->
      <div v-else-if="!history || history.length === 0" class="text-center py-16">
        <div class="text-4xl mb-4">📚</div>
        <p class="text-slate-400 font-light">아직 학습한 뉴스 이력이 없습니다.</p>
        <p class="text-slate-400 text-sm font-light mt-1">관심 경제 뉴스를 읽고 학습을 시작해 보세요!</p>
      </div>

      <!-- Real Data List -->
      <div v-else class="divide-y divide-slate-100">
        <div 
          v-for="item in history" 
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
