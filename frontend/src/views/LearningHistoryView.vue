<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useHistoryStore } from '../stores/useHistoryStore'
import { useWrongNoteStore } from '../stores/useWrongNoteStore'
import BaseBadge from '../components/common/BaseBadge.vue'

const router = useRouter()
const historyStore = useHistoryStore()
const wrongNoteStore = useWrongNoteStore()

// storeToRefs를 사용하여 Pinia 상태의 반응성을 유지하며 추출
const { history, stats, isLoading, error } = storeToRefs(historyStore)
const { wrongNotes, isLoading: isWrongNoteLoading } = storeToRefs(wrongNoteStore)

const unresolvedWrongNotes = computed(() => wrongNotes.value.filter(note => !note.isResolved))

onMounted(async () => {
  try {
    await Promise.all([
      historyStore.fetchHistory(),
      historyStore.fetchStats(),
      wrongNoteStore.fetchWrongNotes()
    ])
  } catch (err) {
    console.error('Failed to load history or stats:', err)
  }
})

const navigateToDetail = (id) => {
  router.push(`/articles/${id}`)
}

const navigateToWrongNote = () => {
  router.push('/wrong-notes')
}

const navigateToReview = (id) => {
  router.push(`/articles/${id}/review`)
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
                  <span class="text-xs text-slate-400 font-light">{{ item.learnedAt && typeof item.learnedAt === 'string' && item.learnedAt.length >= 16 ? item.learnedAt.substring(11, 16) : '' }}</span>
                </div>
                <h3 
                  @click="navigateToDetail(item.articleId)"
                  class="text-base font-bold text-slate-800 dark:text-slate-200 hover:text-primary-600 dark:hover:text-primary-400 cursor-pointer leading-snug"
                >
                  {{ item.articleTitle }}
                </h3>
                <div
                  v-if="item.type === 'REVIEW' && (item.reviewSummary || item.reviewLearned || item.reviewDifficultTerms?.length)"
                  class="mt-3 rounded-2xl border border-slate-100 dark:border-white/10 bg-slate-50/80 dark:bg-slate-900/40 p-4 space-y-3"
                >
                  <div v-if="item.reviewSummary">
                    <p class="text-xs font-bold text-slate-400 dark:text-slate-500 mb-1">요약</p>
                    <p class="text-sm text-slate-700 dark:text-slate-300 leading-relaxed">{{ item.reviewSummary }}</p>
                  </div>
                  <div v-if="item.reviewLearned">
                    <p class="text-xs font-bold text-slate-400 dark:text-slate-500 mb-1">배운 점</p>
                    <p class="text-sm text-slate-700 dark:text-slate-300 leading-relaxed">{{ item.reviewLearned }}</p>
                  </div>
                  <div v-if="item.reviewDifficultTerms?.length" class="flex flex-wrap gap-2">
                    <span
                      v-for="term in item.reviewDifficultTerms"
                      :key="term"
                      class="text-xs px-2 py-1 rounded-lg bg-white dark:bg-slate-950 border border-slate-100 dark:border-white/10 text-slate-500 dark:text-slate-400"
                    >
                      {{ term }}
                    </span>
                  </div>
                </div>
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
              <button
                v-else-if="item.type === 'REVIEW'"
                class="shrink-0 px-3 py-1.5 rounded-lg border border-slate-200 dark:border-white/10 text-xs font-bold text-slate-500 dark:text-slate-300 hover:border-primary-300 hover:text-primary-600 dark:hover:text-primary-400 transition-colors"
                @click="navigateToReview(item.articleId)"
              >
                리뷰 보기
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <section class="glass-panel rounded-3xl p-8 shadow-premium mt-8">
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-6 border-b border-slate-100 dark:border-white/10 pb-4">
        <div>
          <h2 class="text-lg font-bold text-slate-800 dark:text-slate-100">오답노트</h2>
          <p class="text-sm text-slate-400 dark:text-slate-500 mt-1">학습 이력에서 바로 복습할 문제를 확인하세요.</p>
        </div>
        <button
          class="px-4 py-2 rounded-xl bg-primary-600 hover:bg-primary-700 text-white text-sm font-bold transition-colors"
          @click="navigateToWrongNote"
        >
          전체 보기
        </button>
      </div>

      <div v-if="isWrongNoteLoading" class="space-y-3">
        <div v-for="n in 3" :key="n" class="h-16 rounded-2xl bg-slate-100 dark:bg-slate-800 animate-pulse"></div>
      </div>

      <div v-else-if="unresolvedWrongNotes.length === 0" class="text-center py-10">
        <p class="text-slate-400 dark:text-slate-500 font-light">아직 복습할 오답이 없습니다.</p>
      </div>

      <div v-else class="divide-y divide-slate-100 dark:divide-white/10">
        <article
          v-for="note in unresolvedWrongNotes.slice(0, 5)"
          :key="note.id"
          class="py-4 first:pt-0 last:pb-0"
        >
          <div class="flex flex-col sm:flex-row sm:items-start justify-between gap-3">
            <div>
              <p class="text-sm font-bold text-slate-800 dark:text-slate-100 leading-snug">
                {{ note.question || note.quizQuestion || '복습 문제' }}
              </p>
              <p class="text-xs text-slate-400 dark:text-slate-500 mt-1">
                {{ note.articleTitle || '연결된 기사' }}
              </p>
            </div>
            <span class="shrink-0 text-xs px-2.5 py-1 rounded-lg bg-accent-50 border border-accent-200 text-accent-700 dark:bg-accent-950/40 dark:border-accent-800 dark:text-accent-400 font-semibold">
              복습 필요
            </span>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>
