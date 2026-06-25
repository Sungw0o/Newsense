<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useArticleStore } from '../stores/useArticleStore'
import ArticleCard from '../components/article/ArticleCard.vue'

const router = useRouter()
const articleStore = useArticleStore()
const { articles, isLoading, hasMore, filters, error } = storeToRefs(articleStore)

const categories = ['전체', '거시경제', '금융/투자', '정책/제도', '기업/산업', '글로벌경제']
const sortOptions = [
  { value: 'LATEST', label: '최신순' },
  { value: 'MOST_VIEWED', label: '조회순' },
]
const searchInput = ref('')
let searchDebounce = null

const displayArticles = computed(() => articles.value)
const activeCategory = computed(() => filters.value.category || '전체')
const activeSort = computed(() => filters.value.sort || 'LATEST')
const hasKeyword = computed(() => !!filters.value.keyword)

const todayLabel = computed(() => {
  const d = new Date()
  return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, '0')}.${String(d.getDate()).padStart(2, '0')}`
})

onMounted(() => {
  if (articles.value.length === 0) articleStore.fetchArticles()
})

const filterByCategory = async (category) => {
  await articleStore.setCategory(category === '전체' ? '' : category)
}

const changeSort = async (sort) => {
  if (activeSort.value === sort) return
  await articleStore.setSort(sort)
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
    <header class="feed-header">
      <p class="eyebrow-text">오늘의 경제 뉴스</p>
      <p class="feed-meta">
        {{ todayLabel }} 업데이트 <strong>{{ displayArticles.length }}</strong>건
      </p>
    </header>

    <div class="feed-tools">
      <div class="search-wrap">
        <span class="search-icon" aria-hidden="true">⌕</span>
        <input
          v-model="searchInput"
          type="text"
          class="search-input"
          placeholder="기사 제목이나 요약 검색"
          @input="handleSearch"
          @keydown.enter="handleSearch"
        />
        <button v-if="hasKeyword" class="search-clear" @click="clearSearch" aria-label="검색어 지우기">×</button>
      </div>

      <div class="chips" aria-label="기사 카테고리">
        <button
          v-for="cat in categories"
          :key="cat"
          class="chip"
          :class="{ active: activeCategory === cat }"
          @click="filterByCategory(cat)"
        >
          {{ cat }}
        </button>
      </div>
    </div>

    <div class="sort-row">
      <button
        v-for="opt in sortOptions"
        :key="opt.value"
        class="sort-btn"
        :class="{ active: activeSort === opt.value }"
        @click="changeSort(opt.value)"
      >
        {{ opt.label }}
      </button>
    </div>

    <div class="feed-body">
      <div class="feed-main">
        <div v-if="isLoading && displayArticles.length === 0" class="loading-state">
          <div class="spinner"></div>
          <p class="loading-label">기사를 불러오는 중입니다.</p>
        </div>

        <div v-else-if="error" class="empty-state">
          <p class="empty-title">기사를 불러오지 못했습니다.</p>
          <p class="empty-desc">{{ error }}</p>
          <button class="btn-primary" @click="articleStore.fetchArticles()">다시 시도</button>
        </div>

        <div v-else-if="!isLoading && displayArticles.length === 0" class="empty-state">
          <p class="empty-title">표시할 기사가 없습니다.</p>
          <p class="empty-desc">다른 카테고리를 선택하거나 잠시 후 다시 확인해 주세요.</p>
        </div>

        <div v-else class="feed-grid">
          <ArticleCard
            v-for="(article, index) in displayArticles"
            :key="article.articleId"
            :article="article"
            :featured="index === 0"
            @click="navigateToDetail"
          />
        </div>

        <div v-if="hasMore && !isLoading" class="load-more">
          <button class="btn-primary" @click="handleLoadMore">더 보기</button>
        </div>

        <div v-if="isLoading && displayArticles.length > 0" class="load-more">
          <div class="spinner"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.feed-shell {
  max-width: 1360px;
  margin: 0 auto;
  padding: 20px 0 80px;
}

.feed-header {
  display: flex;
  align-items: end;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 24px;
}

.eyebrow-text {
  margin: 0 0 8px;
  font-size: 12px;
  font-weight: 800;
  color: #006fd6;
}

.feed-meta {
  margin: 0;
  font-size: 13px;
  color: var(--ink-3, #8a93a3);
  white-space: nowrap;
}

.feed-meta strong {
  color: #0084ff;
}

.feed-tools {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 14px;
}

.search-wrap {
  position: relative;
  display: flex;
  align-items: center;
  flex: 0 0 min(420px, 100%);
}

.search-icon {
  position: absolute;
  left: 14px;
  font-size: 18px;
  color: var(--ink-3, #8a93a3);
  pointer-events: none;
  line-height: 1;
}

.search-input {
  width: 100%;
  padding: 11px 40px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 10px;
  font-size: 14px;
  color: var(--ink);
  outline: none;
  transition: border-color .15s, box-shadow .15s;
}

.search-input:focus {
  border-color: #0084ff;
  box-shadow: 0 0 0 3px rgba(0, 132, 255, 0.14);
}

.dark .search-input {
  background: rgba(20, 24, 34, 0.62);
  border-color: rgba(255, 255, 255, 0.12);
  color: #f4f6fa;
}

.search-clear {
  position: absolute;
  right: 10px;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 18px;
  color: var(--ink-3);
  padding: 2px 6px;
  border-radius: 6px;
}

.chips {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  flex-wrap: wrap;
}

.chip {
  display: inline-flex;
  align-items: center;
  padding: 8px 13px;
  background: rgba(255, 255, 255, 0.76);
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 999px;
  font-size: 13px;
  font-weight: 700;
  color: var(--ink-2, #4a5161);
  cursor: pointer;
  transition: all .15s;
}

.chip:hover {
  color: var(--ink, #0a0d12);
  border-color: rgba(0, 0, 0, 0.18);
}

.chip.active {
  background: var(--ink, #0a0d12);
  color: #fff;
  border-color: var(--ink, #0a0d12);
}

.dark .chip {
  background: rgba(20, 24, 34, 0.62);
  border-color: rgba(255, 255, 255, 0.12);
  color: #a4adbf;
}

.dark .chip.active {
  background: #f4f6fa;
  color: #07090f;
  border-color: #f4f6fa;
}

/* 정렬 버튼 */
.sort-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 20px;
}

.sort-btn {
  padding: 5px 14px;
  background: none;
  border: 1px solid rgba(0, 0, 0, 0.10);
  border-radius: 999px;
  font-size: 12.5px;
  font-weight: 600;
  color: var(--ink-3, #8a93a3);
  cursor: pointer;
  transition: all .13s;
}

.sort-btn:hover {
  color: var(--ink, #0a0d12);
  border-color: rgba(0, 0, 0, 0.22);
}

.sort-btn.active {
  background: #0084ff;
  color: #fff;
  border-color: #0084ff;
}

.dark .sort-btn {
  border-color: rgba(255, 255, 255, 0.12);
  color: #8a93a3;
}

.dark .sort-btn.active {
  background: #0084ff;
  color: #fff;
  border-color: #0084ff;
}

.feed-body {
  display: block;
}

.feed-main {
  min-width: 0;
}

.feed-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.load-more {
  margin-top: 36px;
  display: flex;
  justify-content: center;
}

.empty-state,
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 84px 0;
  text-align: center;
}

.empty-title {
  font-family: 'Fustat', sans-serif;
  font-size: 18px;
  font-weight: 800;
  color: var(--ink, #0a0d12);
  margin: 0;
}

.dark .empty-title {
  color: #f4f6fa;
}

.empty-desc,
.loading-label {
  font-size: 13.5px;
  color: var(--ink-3, #8a93a3);
  margin: 0;
}

.spinner {
  width: 32px;
  height: 32px;
  border: 3px solid rgba(0, 132, 255, 0.15);
  border-top-color: #0084ff;
  border-radius: 50%;
  animation: spin .7s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@media (max-width: 1024px) {
  .feed-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .feed-header,
  .feed-tools {
    flex-direction: column;
    align-items: stretch;
  }

  .feed-meta {
    white-space: normal;
  }

  .search-wrap {
    flex-basis: auto;
  }

  .chips {
    justify-content: flex-start;
  }

  .feed-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .feed-shell {
    padding: 12px 0 60px;
  }

  .feed-grid {
    grid-template-columns: 1fr;
  }
}
</style>
