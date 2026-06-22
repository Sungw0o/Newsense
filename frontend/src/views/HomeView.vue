<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import BaseBadge from '../components/common/BaseBadge.vue'
import BaseButton from '../components/common/BaseButton.vue'

const router = useRouter()
const selectedCategory = ref('전체')
const categories = ['전체', '금융', '부동산', '주식', '환율', '거시경제']

// 임시 기사 데이터
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

const filteredArticles = computed(() => {
  if (selectedCategory.value === '전체') {
    return mockArticles.value
  }
  return mockArticles.value.filter(article => article.category === selectedCategory.value)
})

const filterByCategory = (category) => {
  selectedCategory.value = category
}

const navigateToDetail = (id) => {
  router.push(`/articles/${id}`)
}
</script>

<template>
  <div class="max-w-6xl mx-auto px-4 py-8">
    <!-- Hero Banner -->
    <header class="mb-10 text-center md:text-left md:flex md:items-center md:justify-between bg-gradient-to-r from-primary-600 to-secondary-500 rounded-3xl p-8 md:p-12 text-white shadow-premium">
      <div class="md:max-w-xl">
        <h1 class="text-3xl md:text-4xl font-extrabold mb-4 leading-tight">
          경제 뉴스를 읽고,<br>
          개념 퀴즈로 문해력을 키우세요!
        </h1>
        <p class="text-primary-100 text-sm md:text-base font-light mb-6">
          기획재정부 사전을 기반으로 한 AI 용어 매핑과 맞춤형 피드백을 통해 경제 지식을 가장 쉽고 체계적으로 학습할 수 있습니다.
        </p>
        <BaseButton variant="secondary" class="bg-white text-primary-700 hover:bg-slate-50 font-bold px-6 py-3 rounded-full">
          오늘의 기사 보러가기
        </BaseButton>
      </div>
      <div class="hidden md:block w-72 h-48 bg-white/10 backdrop-blur-md rounded-2xl border border-white/20 p-6 flex flex-col justify-between">
        <span class="text-xs uppercase tracking-wider text-primary-200">Weekly Stats</span>
        <div class="text-3xl font-black">3,450명</div>
        <div class="text-sm text-primary-100">이 뉴스엔스에서 경제 근육을 키우고 있습니다.</div>
      </div>
    </header>

    <!-- Categories Filter Tabs -->
    <div class="flex flex-wrap items-center gap-2 mb-8 border-b border-slate-200 pb-4">
      <button 
        v-for="cat in categories" 
        :key="cat"
        @click="filterByCategory(cat)"
        class="px-4 py-2 text-sm font-medium rounded-full transition-all duration-300"
        :class="selectedCategory === cat 
          ? 'bg-primary-600 text-white shadow-md' 
          : 'bg-slate-100 text-slate-600 hover:bg-slate-200'"
      >
        {{ cat }}
      </button>
    </div>

    <!-- Articles Grid List -->
    <div class="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div 
        v-for="article in filteredArticles" 
        :key="article.id"
        @click="navigateToDetail(article.id)"
        class="group bg-white rounded-2xl border border-slate-200/80 p-6 flex flex-col justify-between cursor-pointer transition-all duration-300 hover:shadow-premium-hover hover:border-primary-300 hover:scale-102"
      >
        <div>
          <!-- Tags -->
          <div class="flex items-center gap-2 mb-4">
            <BaseBadge :value="article.category" />
            <span class="text-xs text-slate-400">• {{ article.readTime }}</span>
          </div>
          <!-- Title -->
          <h3 class="text-lg font-bold text-slate-800 mb-3 group-hover:text-primary-600 transition-colors duration-300 leading-snug">
            {{ article.title }}
          </h3>
          <!-- Preview -->
          <p class="text-slate-500 text-sm font-light mb-6 line-clamp-3 leading-relaxed">
            {{ article.preview }}
          </p>
        </div>
        
        <!-- Bottom Section -->
        <div class="flex items-center justify-between border-t border-slate-100 pt-4 mt-auto">
          <div class="flex items-center gap-1">
            <span class="text-xs text-slate-400">난이도</span>
            <span 
              class="text-xs font-semibold px-2 py-0.5 rounded"
              :class="{
                'bg-brand-100 text-brand-700': article.difficulty === '초급',
                'bg-secondary-100 text-secondary-700': article.difficulty === '중급',
                'bg-accent-100 text-accent-700': article.difficulty === '고급'
              }"
            >
              {{ article.difficulty }}
            </span>
          </div>
          <span class="text-xs text-slate-400">{{ article.createdAt }}</span>
        </div>
      </div>
    </div>
  </div>
</template>
