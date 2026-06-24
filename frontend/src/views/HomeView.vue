<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useArticleStore } from '../stores/useArticleStore'
import ArticleCard from '../components/article/ArticleCard.vue'

const router = useRouter()
const articleStore = useArticleStore()
const { articles, isLoading, hasMore, filters } = storeToRefs(articleStore)

const categories = ['전체', '금융', '부동산', '주식', '환율', '거시경제']

const mockArticles = ref([
  {
    articleId: 1,
    title: '기준금리 인하가 청년 전세대출에 미치는 영향',
    summary: '한국은행 금융통화위원회가 이번 달 기준금리를 0.25%p 인하했습니다. 시중 은행의 전세자금대출 금리도 하락세를 보일 것으로 예상되는데, 청년들이 주목해야 할 핵심 포인트를 정리했습니다.',
    category: '금융',
    difficulty: '초급',
    publishedAt: '2026-06-22',
    estimatedMinutes: 3,
    quizCount: 3,
  },
  {
    articleId: 2,
    title: 'LTV와 DSR 규제 완화, 무엇이 달라지나?',
    summary: '정부가 주택담보대출 비율(LTV)과 총부채원리금상환비율(DSR)의 한도를 조정했습니다. 부동산 시장과 실수요자들에게 미칠 파급력을 상세히 분석합니다.',
    category: '부동산',
    difficulty: '중급',
    publishedAt: '2026-06-21',
    estimatedMinutes: 5,
    quizCount: 4,
  },
  {
    articleId: 3,
    title: '미국 연준(Fed)의 테이퍼링 종료와 한국 주식시장',
    summary: '미국 연방준비제도가 테이퍼링 정책 종료 및 금리 인상 사이클 진입을 예고했습니다. 원달러 환율 급변동 상황에서 개인 투자자가 취해야 할 방어적 포트폴리오 전략을 소개합니다.',
    category: '주식',
    difficulty: '고급',
    publishedAt: '2026-06-20',
    estimatedMinutes: 7,
    quizCount: 5,
  },
  {
    articleId: 4,
    title: '소비자물가지수(CPI) 상승률 둔화의 의미',
    summary: '최근 발표된 소비자물가지수 상승률이 예상치를 하회하며 인플레이션 압력이 완화되는 신호를 보내고 있습니다. 금리 정책에 미치는 영향을 살펴봅니다.',
    category: '거시경제',
    difficulty: '중급',
    publishedAt: '2026-06-19',
    estimatedMinutes: 4,
    quizCount: 3,
  },
  {
    articleId: 5,
    title: '원달러 환율 1,400원 돌파 — 수출입 기업 영향',
    summary: '원달러 환율이 주요 지지선인 1,400원을 넘어서면서 수출 기업과 수입 의존 업종에 미치는 영향이 엇갈리고 있습니다.',
    category: '환율',
    difficulty: '중급',
    publishedAt: '2026-06-18',
    estimatedMinutes: 4,
    quizCount: 3,
  },
])

const displayArticles = computed(() => {
  if (articles.value.length > 0) return articles.value
  let res = mockArticles.value
  if (filters.value.category) res = res.filter(a => a.category === filters.value.category)
  if (filters.value.difficulty) res = res.filter(a => a.difficulty === filters.value.difficulty)
  return res
})

const activeCategory = computed(() => filters.value.category || '전체')

onMounted(() => {
  if (articles.value.length === 0) articleStore.fetchArticles()
})

const filterByCategory = async (category) => {
  await articleStore.setCategory(category === '전체' ? '' : category)
}

const navigateToDetail = (id) => router.push(`/articles/${id}`)

const handleLoadMore = () => articleStore.fetchArticles()
</script>

<template>
  <div class="feed-shell">
    <!-- Page header -->
    <header class="feed-head">
      <div>
        <p class="eyebrow">
          <span class="live-dot"></span>
          오늘의 브리프
        </p>
        <h1 class="feed-title">
          경제를 읽고,<br>
          <span class="accent">문해력</span>을 키우세요
        </h1>
      </div>
      <div class="feed-meta">
        <b>2026년 6월 24일</b><br>
        뉴스 <span>{{ displayArticles.length }}</span>건 업데이트됨
      </div>
    </header>

    <!-- Category filter chips -->
    <div class="filters-row">
      <div class="chips">
        <button
          v-for="cat in categories"
          :key="cat"
          class="chip"
          :class="{ active: activeCategory === cat }"
          @click="filterByCategory(cat)"
        >
          {{ cat }}
          <span class="chip-count">
            {{ cat === '전체' ? displayArticles.length : displayArticles.filter(a => a.category === cat).length || displayArticles.length }}
          </span>
        </button>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="isLoading && displayArticles.length === 0" class="loading-state">
      <div class="spinner"></div>
      <p class="eyebrow" style="margin-top:16px;">기사를 불러오는 중</p>
    </div>

    <!-- Feed grid -->
    <div v-else class="feed-grid">
      <ArticleCard
        v-for="(article, index) in displayArticles"
        :key="article.articleId"
        :article="article"
        :featured="index === 0"
        @click="navigateToDetail"
      />
    </div>

    <!-- Load more -->
    <div v-if="hasMore && !isLoading" class="load-more">
      <button class="btn-primary" @click="handleLoadMore">
        더 보기
        <span class="load-arrow">↓</span>
      </button>
    </div>

    <div v-if="isLoading && displayArticles.length > 0" class="load-more">
      <div class="spinner"></div>
    </div>
  </div>
</template>

<style scoped>
.feed-shell {
  max-width: 1200px;
  margin: 0 auto;
  padding: 48px 0 80px;
}

/* Header */
.feed-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 28px;
}

.eyebrow {
  font-family: 'Nanum Gothic', monospace;
  font-size: 11.5px;
  letter-spacing: 1px;
  color: var(--ink-3, #8a93a3);
  text-transform: uppercase;
  margin: 0 0 10px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.live-dot {
  width: 7px; height: 7px;
  border-radius: 50%;
  background: #FF801E;
  box-shadow: 0 0 0 4px rgba(255, 128, 30, 0.18);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { box-shadow: 0 0 0 4px rgba(255, 128, 30, 0.18); }
  50%       { box-shadow: 0 0 0 8px rgba(255, 128, 30, 0.06); }
}

.feed-title {
  font-family: 'Fustat', sans-serif;
  font-weight: 800;
  font-size: 52px;
  line-height: 1.05;
  letter-spacing: -1.5px;
  margin: 0;
  color: var(--ink, #0a0d12);
}

.dark .feed-title { color: #f4f6fa; }

.accent {
  background: linear-gradient(95deg, #0084ff 0%, #4FB3FF 60%, #0a4a99 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.dark .accent {
  background: linear-gradient(95deg, #6CB8FF 0%, #B8DAFF 60%, #FFFFFF 100%);
  -webkit-background-clip: text;
  background-clip: text;
}

.feed-meta {
  text-align: right;
  font-size: 13px;
  color: var(--ink-2, #4a5161);
  line-height: 1.65;
  flex-shrink: 0;
}
.feed-meta b { color: var(--ink, #0a0d12); font-weight: 600; }
.dark .feed-meta { color: #a4adbf; }
.dark .feed-meta b { color: #f4f6fa; }
.feed-meta span { color: #0084ff; font-weight: 600; }

/* Filters */
.filters-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.chips {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.chip {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 8px 14px;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 999px;
  font-size: 13px;
  font-weight: 500;
  color: var(--ink-2, #4a5161);
  cursor: pointer;
  transition: all .15s;
  line-height: 1;
}
.chip:hover { color: var(--ink, #0a0d12); border-color: rgba(0,0,0,0.18); }
.chip.active {
  background: var(--ink, #0a0d12);
  color: #fff;
  border-color: var(--ink, #0a0d12);
}

.dark .chip {
  background: rgba(20, 24, 34, 0.55);
  border-color: rgba(255,255,255,0.12);
  color: #a4adbf;
  box-shadow: inset 0 1px 0 0 rgba(255,255,255,0.08);
}
.dark .chip:hover { color: #f4f6fa; border-color: rgba(255,255,255,0.25); }
.dark .chip.active { background: #f4f6fa; color: #07090f; border-color: #f4f6fa; }

.chip-count {
  font-family: 'Nanum Gothic', monospace;
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 5px;
  background: rgba(0, 0, 0, 0.06);
  color: var(--ink-3, #8a93a3);
}
.chip.active .chip-count {
  background: rgba(255,255,255,0.2);
  color: rgba(255,255,255,0.75);
}
.dark .chip.active .chip-count { background: rgba(0,0,0,0.15); color: rgba(0,0,0,0.6); }

/* Feed grid */
.feed-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

/* Load more */
.load-more {
  margin-top: 36px;
  display: flex;
  justify-content: center;
}

.load-arrow {
  width: 24px; height: 24px;
  border-radius: 50%;
  background: #fff;
  color: #0084ff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 700;
}

/* Spinner */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 0;
}
.spinner {
  width: 32px; height: 32px;
  border: 3px solid rgba(0, 132, 255, 0.15);
  border-top-color: #0084ff;
  border-radius: 50%;
  animation: spin .7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* Responsive */
@media (max-width: 1100px) {
  .feed-grid { grid-template-columns: repeat(2, 1fr); }
  .feed-title { font-size: 42px; }
}
@media (max-width: 640px) {
  .feed-shell { padding: 32px 0 60px; }
  .feed-head { flex-direction: column; align-items: flex-start; }
  .feed-meta { text-align: left; }
  .feed-title { font-size: 34px; letter-spacing: -1px; }
  .feed-grid { grid-template-columns: 1fr; }
}
</style>
