<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useArticleStore } from '../stores/useArticleStore'
import BaseButton from '../components/common/BaseButton.vue'
import ArticleFeedList from '../components/article/ArticleFeedList.vue'

const router = useRouter()
const articleStore = useArticleStore()
const { articles, isLoading, hasMore, filters } = storeToRefs(articleStore)

const categories = ['전체', '금융', '부동산', '주식', '환율', '거시경제']

// 로컬 폴백 데모용 기사 데이터 (API 오프라인 시 쇼케이스용)
const mockArticles = ref([
  {
    id: 1,
    title: '기준금리 인하가 청년 전세대출에 미치는 영향',
    preview: '한국은행 금융통화위원회가 이번 달 기준금리를 0.25%p 인하했습니다. 이에 따라 시중 은행의 전세자금대출 금리도 하락세를 보일 것으로 예상되는데, 청년들이 주목해야 할 핵심 포인트를 정리했습니다.',
    category: '금융',
    difficulty: '초급',
    createdAt: '2026-06-22',
    readTime: '3분'
  },
  {
    id: 2,
    title: 'LTV와 DSR 규제 완화, 무엇이 달라지나?',
    preview: '정부가 가계부채 관리 방안의 일환으로 주택담보대출 비율(LTV)과 총부채원리금상환비율(DSR)의 한도를 조정했습니다. 부동산 시장과 실수요자들에게 미칠 파급력을 상세히 분석합니다.',
    category: '부동산',
    difficulty: '중급',
    createdAt: '2026-06-21',
    readTime: '5분'
  },
  {
    id: 3,
    title: '미국 연준(Fed)의 테이퍼링 종료와 한국 주식시장',
    preview: '미국 연방준비제도가 테이퍼링 정책 종료 및 금리 인상 사이클 진입을 예고했습니다. 원달러 환율 급변동 상황에서 개인 투자자가 취해야 할 방어적 포트폴리오 전략을 소개합니다.',
    category: '주식',
    difficulty: '고급',
    createdAt: '2026-06-20',
    readTime: '7분'
  }
])

// 스토어 기사가 비어있고 로딩 중이 아닐 때만 데모용 mock 기사 노출 (로컬 폴백)
const displayArticles = computed(() => {
  if (articles.value.length > 0) {
    return articles.value
  }
  // 필터링 적용된 mock 기사 반환
  let res = mockArticles.value
  if (filters.value.category) {
    res = res.filter(a => a.category === filters.value.category)
  }
  if (filters.value.difficulty) {
    res = res.filter(a => a.difficulty === filters.value.difficulty)
  }
  return res
})

const activeCategory = computed(() => {
  return filters.value.category || '전체'
})

onMounted(() => {
  if (articles.value.length === 0) {
    articleStore.fetchArticles()
  }
})

const filterByCategory = async (category) => {
  const categoryParam = category === '전체' ? '' : category
  await articleStore.setCategory(categoryParam)
}

const filterByDifficulty = async (difficulty) => {
  const diffParam = difficulty === '전체' ? '' : difficulty
  await articleStore.setDifficulty(diffParam)
}

const navigateToDetail = (id) => {
  router.push(`/articles/${id}`)
}

const handleLoadMore = () => {
  articleStore.fetchArticles()
}
</script>

<template>
  <div class="relative w-full max-w-6xl mx-auto px-4 py-8">
    <!-- Animated background glowing orbs -->
    <div class="absolute top-10 left-1/4 w-80 h-80 bg-primary-500/10 rounded-full blur-[100px] pointer-events-none animate-pulse-glow"></div>
    <div class="absolute bottom-10 right-1/4 w-80 h-80 bg-brand-500/10 rounded-full blur-[100px] pointer-events-none animate-pulse-glow" style="animation-delay: 2s;"></div>

    <!-- Hero Banner -->
    <header class="relative overflow-hidden mb-12 glass-panel rounded-3xl p-8 md:p-12 text-slate-800 dark:text-white shadow-premium border-primary-500/10">
      <!-- Glow gradient overlay -->
      <div class="absolute inset-0 bg-gradient-to-r from-primary-500/5 to-secondary-500/5 pointer-events-none"></div>
      
      <div class="relative z-10 md:flex md:items-center md:justify-between">
        <div class="md:max-w-xl">
          <h1 class="text-3xl md:text-4xl font-extrabold mb-4 leading-tight tracking-tight select-none text-slate-800 dark:text-white">
            경제 뉴스를 읽고,<br>
            개념 퀴즈로 문해력을 키우세요!
          </h1>
          <p class="text-slate-600 dark:text-slate-300 text-sm md:text-base font-light mb-6 leading-relaxed select-none">
            기획재정부 사전을 기반으로 한 AI 용어 매핑 and 맞춤형 피드백을 통해 경제 지식을 가장 쉽고 체계적으로 학습할 수 있습니다.
          </p>
          <BaseButton 
            variant="primary" 
            class="shadow-glass-glow hover:shadow-glass-glow-hover font-bold px-6 py-3 rounded-full"
            @click="filterByCategory('전체')"
          >
            오늘의 기사 보러가기
          </BaseButton>
        </div>
        
        <!-- Weekly Stats Card -->
        <div class="hidden md:flex mt-8 md:mt-0 w-72 h-44 glass-panel rounded-2xl border-slate-200/50 dark:border-white/5 p-6 flex-col justify-between shadow-premium">
          <span class="text-xs uppercase tracking-widest text-primary-600 dark:text-primary-400 font-bold select-none">Weekly Stats</span>
          <div class="text-3xl font-black text-slate-800 dark:text-white select-none">3,450명</div>
          <div class="text-xs text-slate-500 dark:text-slate-400 leading-normal select-none">이 뉴스엔스에서 경제 근육을 활발히 키우고 있습니다.</div>
        </div>
      </div>
    </header>

    <!-- Categories and Difficulties Filter Tabs -->
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-8 border-b border-slate-200/60 dark:border-white/5 pb-4">
      <!-- Categories -->
      <div class="flex flex-wrap items-center gap-2.5">
        <button 
          v-for="cat in categories" 
          :key="cat"
          @click="filterByCategory(cat)"
          class="px-4 py-2.5 text-xs font-semibold rounded-full border transition-all duration-300 select-none"
          :class="activeCategory === cat 
            ? 'bg-primary-500 border-primary-500/30 text-white shadow-glass-glow' 
            : 'bg-slate-200/40 border-slate-200/50 text-slate-500 hover:bg-slate-200/60 dark:bg-white/5 dark:border-white/5 dark:text-slate-400 dark:hover:bg-white/10 dark:hover:text-slate-200'"
        >
          {{ cat }}
        </button>
      </div>

      <!-- Difficulty Filter -->
      <div class="flex items-center gap-2">
        <span class="text-xs text-slate-400 dark:text-slate-500 font-semibold select-none mr-1">난이도:</span>
        <button 
          v-for="diff in ['전체', '초급', '중급', '고급']" 
          :key="diff"
          @click="filterByDifficulty(diff)"
          class="px-3 py-1.5 text-xs font-semibold rounded-lg border transition-all duration-300 select-none"
          :class="(filters.difficulty || '전체') === diff || (diff === '전체' && !filters.difficulty)
            ? 'bg-secondary-500 border-secondary-500/30 text-white' 
            : 'bg-slate-200/40 border-slate-200/50 text-slate-500 hover:bg-slate-200/60 dark:bg-white/5 dark:border-white/5 dark:text-slate-400 dark:hover:bg-white/10'"
        >
          {{ diff }}
        </button>
      </div>
    </div>

    <!-- Articles Feed List Component -->
    <ArticleFeedList 
      :articles="displayArticles"
      :is-loading="isLoading"
      :has-more="hasMore"
      @load-more="handleLoadMore"
      @card-click="navigateToDetail"
    />
  </div>
</template>
