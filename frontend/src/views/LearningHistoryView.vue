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
      <h1 class="text-3xl font-extrabold text-slate-800 dark:text-white mb-2">내 학습 관리</h1>
      <p class="text-slate-500 dark:text-slate-400 font-light">지금까지 뉴스엔스에서 차곡차곡 쌓아올린 나의 경제 근육을 확인해 보세요.</p>
    </header>

    <!-- Error State -->
    <div v-if="error" class="mb-8 p-4 bg-accent-50/50 dark:bg-accent-950/20 border border-accent-200 dark:border-accent-800/30 rounded-2xl text-accent-700 dark:text-accent-400 text-sm">
      ⚠️ {{ error }}
    </div>

    <!-- Stats Dashboard -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-10">
      <!-- Read count card -->
      <div class="glass-panel rounded-2xl p-6 flex items-center justify-between shadow-sm">
        <div>
          <span class="text-slate-400 dark:text-slate-400 text-xs font-semibold uppercase tracking-wider block mb-1">총 읽은 기사</span>
          <span class="text-3xl font-black text-slate-800 dark:text-white">
            <template v-if="isLoading">
              <span class="inline-block w-12 h-8 bg-slate-100 dark:bg-slate-800 animate-pulse rounded"></span>
            </template>
            <template v-else>
              {{ stats?.totalReadArticleCount || 0 }}
            </template>
            <span class="text-lg font-light text-slate-400 dark:text-slate-500 ml-1">개</span>
          </span>
        </div>
        <div class="w-12 h-12 bg-primary-100 dark:bg-primary-950/40 rounded-xl flex items-center justify-center text-primary-600 dark:text-primary-400 text-xl font-bold transition-colors">
          📰
        </div>
      </div>

      <!-- Accuracy card -->
      <div class="glass-panel rounded-2xl p-6 flex items-center justify-between shadow-sm">
        <div>
          <span class="text-slate-400 dark:text-slate-400 text-xs font-semibold uppercase tracking-wider block mb-1">퀴즈 평균 정답률</span>
          <span class="text-3xl font-black text-slate-800 dark:text-white">
            <template v-if="isLoading">
              <span class="inline-block w-12 h-8 bg-slate-100 dark:bg-slate-800 animate-pulse rounded"></span>
            </template>
            <template v-else>
              {{ stats?.quizAccuracyRate || 0 }}
            </template>
            <span class="text-lg font-light text-slate-400 dark:text-slate-500 ml-1">%</span>
          </span>
        </div>
        <div class="w-12 h-12 bg-brand-100 dark:bg-brand-950/40 rounded-xl flex items-center justify-center text-brand-600 dark:text-brand-400 text-xl font-bold transition-colors">
          🎯
        </div>
      </div>

      <!-- Streak card -->
      <div class="glass-panel rounded-2xl p-6 flex items-center justify-between shadow-sm">
        <div>
          <span class="text-slate-400 dark:text-slate-400 text-xs font-semibold uppercase tracking-wider block mb-1">연속 학습 일수</span>
          <span class="text-3xl font-black text-slate-800 dark:text-white">
            <template v-if="isLoading">
              <span class="inline-block w-12 h-8 bg-slate-100 dark:bg-slate-800 animate-pulse rounded"></span>
            </template>
            <template v-else>
              {{ stats?.consecutiveLearningDays || 0 }}
            </template>
            <span class="text-lg font-light text-slate-400 dark:text-slate-500 ml-1">일째</span>
          </span>
        </div>
        <div class="w-12 h-12 bg-accent-100 dark:bg-accent-950/40 rounded-xl flex items-center justify-center text-accent-600 dark:text-accent-400 text-xl font-bold transition-colors">
          🔥
        </div>
      </div>
    </div>

    <!-- History List -->
    <div class="glass-panel rounded-3xl p-8 shadow-premium">
      <h2 class="text-lg font-bold text-slate-800 dark:text-slate-100 mb-6 border-b border-slate-100 dark:border-white/10 pb-4">
        최근 읽은 뉴스 & 퀴즈 이력
      </h2>

      <!-- Loading skeleton -->
      <div v-if="isLoading" class="space-y-4">
        <div v-for="n in 3" :key="n" class="py-5 animate-pulse flex justify-between items-center gap-4">
          <div class="flex-1 space-y-2">
            <div class="h-4 bg-slate-100 dark:bg-slate-800 rounded w-1/4"></div>
            <div class="h-6 bg-slate-100 dark:bg-slate-800 rounded w-3/4"></div>
          </div>
          <div class="w-24 h-8 bg-slate-100 dark:bg-slate-800 rounded"></div>
        </div>
      </div>

      <!-- Empty state -->
      <div v-else-if="!history || !history.days || history.days.length === 0" class="text-center py-16">
        <div class="text-4xl mb-4">📚</div>
        <p class="text-slate-400 dark:text-slate-500 font-light">아직 학습한 뉴스 이력이 없습니다.</p>
        <p class="text-slate-400 dark:text-slate-500 text-sm font-light mt-1">관심 경제 뉴스를 읽고 학습을 시작해 보세요!</p>
      </div>

      <!-- Real Data List -->
      <div v-else class="space-y-8">
        <div 
          v-for="day in history.days" 
          :key="day.date"
          class="space-y-4"
        >
          <!-- Date Header -->
          <div class="flex items-center gap-3">
            <span class="text-sm font-bold text-slate-800 dark:text-slate-200">{{ day.date }}</span>
            <div class="h-px bg-slate-100 dark:bg-white/10 flex-1"></div>
            <span class="text-xs text-slate-400 dark:text-slate-500 font-light">
              기사 {{ day.articleReadCount }} • 퀴즈 {{ day.quizCount }} • 리뷰 {{ day.reviewCount }}
            </span>
          </div>

          <!-- Timeline Items for this Day -->
          <div class="divide-y divide-slate-100 dark:divide-white/10 pl-2">
            <div 
              v-for="item in day.timeline" 
              :key="item.historyId"
              class="py-4 flex flex-col sm:flex-row sm:items-center justify-between gap-4 first:pt-0 last:pb-0"
            >
              <div>
                <div class="flex items-center gap-2 mb-1.5">
                  <span 
                    class="text-xxs px-2 py-0.5 rounded font-bold uppercase tracking-wider"
                    :class="{
                      'bg-primary-100 text-primary-800 dark:bg-primary-950/40 dark:text-primary-400': item.type === 'ARTICLE_READ',
                      'bg-brand-100 text-brand-800 dark:bg-brand-950/40 dark:text-brand-400': item.type === 'QUIZ',
                      'bg-secondary-100 text-secondary-800 dark:bg-secondary-950/40 dark:text-secondary-400': item.type === 'REVIEW'
                    }"
                  >
                    {{ item.typeName }}
                  </span>
                  <BaseBadge :value="item.articleCategory" />
                  <span class="text-xs text-slate-400 font-light">{{ item.learnedAt.substring(11, 16) }}</span>
                </div>
                <h3 
                  @click="navigateToDetail(item.articleId)"
                  class="text-base font-bold text-slate-800 dark:text-slate-200 hover:text-primary-600 dark:hover:text-primary-400 cursor-pointer leading-snug"
                >
                  {{ item.articleTitle }}
                </h3>
              </div>

              <!-- Extra meta / status -->
              <div v-if="item.type === 'QUIZ'" class="flex items-center">
                <span 
                  class="text-xs px-2.5 py-1 rounded-lg border font-semibold"
                  :class="item.quizCorrect 
                    ? 'bg-brand-50 border-brand-200 text-brand-700 dark:bg-brand-950/40 dark:border-brand-800 dark:text-brand-400' 
                    : 'bg-accent-50 border-accent-200 text-accent-700 dark:bg-accent-950/40 dark:border-accent-800 dark:text-accent-400'"
                >
                  {{ item.quizCorrect ? '🎯 정답' : '❌ 오답' }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
