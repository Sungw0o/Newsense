<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useArticleStore } from '../stores/useArticleStore'
import ArticleCard from '../components/article/ArticleCard.vue'

const router = useRouter()
const articleStore = useArticleStore()
const { articles, isLoading, hasMore, filters, error } = storeToRefs(articleStore)

const categories = ['전체', '거시경제', '금융/투자', '정책/제도', '기업/산업', '글로벌경제']
const searchInput = ref('')
let searchDebounce = null

const displayArticles = computed(() => articles.value)
const activeCategory = computed(() => filters.value.category || '전체')
const hasKeyword = computed(() => !!filters.value.keyword)

onMounted(() => {
  if (articles.value.length === 0) articleStore.fetchArticles()
})

const filterByCategory = async (category) => {
  await articleStore.setCategory(category === '전체' ? '' : category)
}

const navigateToDetail = (id) => router.push(`/articles/${id}`)

const handleLoadMore = () => articleStore.fetchArticles()

const handleSearch = () => {
  clearTimeout(searchDebounce)
  searchDebounce = setTimeout(() => {
    articleStore.setKeyword(searchInput.value.trim())
  }, 350)
}

const clearSearch = () => {
  searchInput.value = ''
  articleStore.setKeyword('')
}
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

    <!-- Search bar -->
    <div class="search-row">
      <div class="search-wrap">
        <span class="search-icon">🔎</span>
        <input
          v-model="searchInput"
          type="text"
          class="search-input"
          placeholder="기사 제목·요약 검색"
          @input="handleSearch"
          @keydown.enter="handleSearch"
        />
        <button v-if="hasKeyword" class="search-clear" @click="clearSearch" aria-label="검색어 지우기">✕</button>
      </div>
    </div>

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

    <!-- Error -->
    <div v-else-if="error" class="empty-state">
      <p class="empty-icon">⚠️</p>
      <p class="empty-title">기사를 불러오지 못했습니다</p>
      <p class="empty-desc">{{ error }}</p>
      <button class="btn-primary" style="margin-top:16px;" @click="articleStore.fetchArticles()">다시 시도</button>
    </div>

    <!-- Empty -->
    <div v-else-if="!isLoading && displayArticles.length === 0" class="empty-state">
      <p class="empty-icon">📭</p>
      <p class="empty-title">표시할 기사가 없습니다</p>
      <p class="empty-desc">다른 카테고리를 선택하거나 잠시 후 다시 확인해 주세요.</p>
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

/* Search */
.search-row {
  margin-bottom: 16px;
}

.search-wrap {
  position: relative;
  display: flex;
  align-items: center;
  max-width: 520px;
}

.search-icon {
  position: absolute;
  left: 14px;
  font-size: 15px;
  pointer-events: none;
  line-height: 1;
}

.search-input {
  width: 100%;
  padding: 10px 40px 10px 40px;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(0, 0, 0, 0.10);
  border-radius: 12px;
  font-size: 14px;
  color: var(--ink);
  outline: none;
  transition: border-color .15s, box-shadow .15s;
}
.search-input::placeholder { color: var(--ink-3); }
.search-input:focus {
  border-color: #0084ff;
  box-shadow: 0 0 0 3px rgba(0, 132, 255, 0.14);
}

.dark .search-input {
  background: rgba(20, 24, 34, 0.55);
  border-color: rgba(255,255,255,0.12);
  color: #f4f6fa;
}
.dark .search-input:focus {
  border-color: #4FB3FF;
  box-shadow: 0 0 0 3px rgba(79, 179, 255, 0.14);
}

.search-clear {
  position: absolute;
  right: 10px;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 12px;
  color: var(--ink-3);
  padding: 4px 6px;
  border-radius: 6px;
  transition: background .12s, color .12s;
}
.search-clear:hover { background: rgba(0,0,0,0.06); color: var(--ink); }
.dark .search-clear:hover { background: rgba(255,255,255,0.08); color: #f4f6fa; }

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

/* Empty / Error */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 0;
  text-align: center;
}
.empty-icon { font-size: 40px; margin: 0 0 16px; }
.empty-title {
  font-family: 'Fustat', sans-serif;
  font-size: 18px;
  font-weight: 700;
  color: var(--ink, #0a0d12);
  margin: 0 0 8px;
}
.dark .empty-title { color: #f4f6fa; }
.empty-desc {
  font-size: 13.5px;
  color: var(--ink-3, #8a93a3);
  margin: 0;
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
