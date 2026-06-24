<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useCommunityStore } from '../stores/useCommunityStore'

const router = useRouter()
const store = useCommunityStore()

const activeFilter = ref('전체')
const filters = ['전체', '기사 스크랩', '자유 토론', '문의']
const activeSort = ref('LATEST')
const sortOptions = [
  { label: '최신순', value: 'LATEST' },
  { label: '좋아요순', value: 'LIKES' },
  { label: '조회순', value: 'VIEWS' },
]
const searchInput = ref('')
const activeKeyword = ref('')
let searchDebounce = null

const typeParam = computed(() => {
  if (activeFilter.value === '기사 스크랩') return null
  if (activeFilter.value === '자유 토론') return null
  if (activeFilter.value === '문의') return 'INQUIRY'
  return null
})

const fetchPosts = () => store.fetchPosts({ sort: activeSort.value, keyword: activeKeyword.value || undefined, type: typeParam.value || undefined })

const changeSort = (sort) => {
  activeSort.value = sort
  fetchPosts()
}

const handleSearch = () => {
  clearTimeout(searchDebounce)
  searchDebounce = setTimeout(() => {
    activeKeyword.value = searchInput.value.trim()
    fetchPosts()
  }, 350)
}

const clearSearch = () => {
  searchInput.value = ''
  activeKeyword.value = ''
  fetchPosts()
}

const displayPosts = computed(() => {
  if (activeFilter.value === '기사 스크랩') return store.posts.filter(p => p.articleScrap)
  if (activeFilter.value === '자유 토론') return store.posts.filter(p => !p.articleScrap && p.type !== 'INQUIRY')
  return store.posts
})

onMounted(async () => {
  await Promise.all([fetchPosts(), store.fetchNotices()])
})

const formatDate = (iso) => {
  const d = new Date(iso)
  return `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const categoryColor = {
  '거시경제': '#6366f1', '금융/투자': '#0084ff', '정책/제도': '#d97706', '기업/산업': '#059669', '글로벌경제': '#0891b2',
}
</script>

<template>
  <div class="comm-shell">
    <!-- Header -->
    <header class="comm-head">
      <div>
        <p class="eyebrow">경제 학습 커뮤니티</p>
        <h1 class="comm-title">커뮤니티</h1>
        <p class="comm-sub">기사를 읽고 생각을 나눠보세요.</p>
      </div>
      <button class="btn-primary write-btn" @click="router.push('/community/write')">
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
          <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
        </svg>
        글쓰기
      </button>
    </header>

    <!-- Search bar -->
    <div class="search-row">
      <div class="search-wrap">
        <span class="search-icon">🔎</span>
        <input
          v-model="searchInput"
          type="text"
          class="search-input"
          placeholder="게시글 제목·내용 검색"
          @input="handleSearch"
          @keydown.enter="handleSearch"
        />
        <button v-if="activeKeyword" class="search-clear" @click="clearSearch" aria-label="검색어 지우기">✕</button>
      </div>
    </div>

    <!-- Filter chips -->
    <div class="filter-row">
      <button
        v-for="f in filters"
        :key="f"
        class="chip"
        :class="{ active: activeFilter === f }"
        @click="activeFilter = f"
      >{{ f }}</button>
    </div>

    <div class="sort-row" aria-label="게시글 정렬">
      <button
        v-for="option in sortOptions"
        :key="option.value"
        class="sort-chip"
        :class="{ active: activeSort === option.value }"
        @click="changeSort(option.value)"
      >
        {{ option.label }}
      </button>
    </div>

    <!-- Pinned notices -->
    <section v-if="store.notices.length > 0 && activeFilter !== '문의'" class="notice-section">
      <div class="notice-header">
        <span class="notice-icon">📢</span>
        <span class="notice-label">공지사항</span>
      </div>
      <div class="notice-list">
        <div
          v-for="notice in store.notices"
          :key="notice.postId"
          class="notice-item"
          tabindex="0"
          role="button"
          :aria-label="notice.title"
          @click="router.push(`/community/${notice.postId}`)"
          @keydown.enter="router.push(`/community/${notice.postId}`)"
          @keydown.space.prevent="router.push(`/community/${notice.postId}`)"
        >
          <span class="notice-pin">📌</span>
          <span class="notice-title">{{ notice.title }}</span>
          <span class="notice-date">{{ formatDate(notice.createdAt) }}</span>
        </div>
      </div>
    </section>

    <!-- Loading -->
    <div v-if="store.isLoading" class="loading-state">
      <div class="spinner"></div>
    </div>

    <!-- Post list -->
    <div v-else class="post-list">
      <article
        v-for="post in displayPosts"
        :key="post.postId"
        class="post-card"
        tabindex="0"
        role="button"
        :aria-label="post.title"
        @click="router.push(`/community/${post.postId}`)"
        @keydown.enter="router.push(`/community/${post.postId}`)"
        @keydown.space.prevent="router.push(`/community/${post.postId}`)"
      >
        <!-- Article scrap badge -->
        <div v-if="post.articleScrap" class="scrap-badge" :style="{ background: (categoryColor[post.articleScrap.category] ?? '#0084ff') + '18', color: categoryColor[post.articleScrap.category] ?? '#0084ff' }">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor"><path d="M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"/></svg>
          {{ post.articleScrap.category }} 기사 스크랩
        </div>

        <h2 class="post-title">{{ post.title }}</h2>
        <p class="post-preview">{{ post.content }}</p>

        <!-- Article scrap preview -->
        <div v-if="post.articleScrap" class="scrap-preview">
          <span class="scrap-icon">📰</span>
          <span class="scrap-text">{{ post.articleScrap.title }}</span>
        </div>

        <footer class="post-foot">
          <div class="post-author">
            <span class="mini-avatar">{{ post.author.avatarInitial }}</span>
            <span class="author-name">{{ post.author.nickname }}</span>
            <span class="post-date">{{ formatDate(post.createdAt) }}</span>
          </div>
          <div class="post-stats">
            <span class="stat">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
              {{ post.viewCount }}
            </span>
            <span class="stat">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3H14z"/><path d="M7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/></svg>
              {{ post.likeCount }}
            </span>
            <span class="stat">
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
              {{ post.commentCount }}
            </span>
          </div>
        </footer>
      </article>

      <div v-if="displayPosts.length === 0" class="empty">
        <p class="eyebrow" style="text-align:center;">게시글이 없습니다</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.comm-shell {
  max-width: 800px;
  margin: 0 auto;
  padding: 48px 0 80px;
}

.eyebrow {
  font-family: 'Nanum Gothic', monospace;
  font-size: 11.5px;
  letter-spacing: 1px;
  color: var(--ink-3, #8a93a3);
  text-transform: uppercase;
  margin: 0 0 10px;
  display: block;
}

.comm-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 28px;
}

.comm-title {
  font-family: 'Fustat', sans-serif;
  font-weight: 800;
  font-size: 44px;
  letter-spacing: -1.5px;
  color: var(--ink, #0a0d12);
  margin: 0 0 6px;
}
.dark .comm-title { color: #f4f6fa; }

.comm-sub {
  font-size: 14px;
  color: var(--ink-2, #4a5161);
  margin: 0;
}
.dark .comm-sub { color: #a4adbf; }

.write-btn {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 11px 20px;
  font-size: 14px;
  flex-shrink: 0;
}

.search-row {
  margin-bottom: 16px;
}

.search-wrap {
  position: relative;
  display: flex;
  align-items: center;
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

.filter-row {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.sort-row {
  display: flex;
  justify-content: flex-end;
  gap: 6px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.chip {
  display: inline-flex;
  align-items: center;
  padding: 7px 14px;
  background: rgba(255,255,255,0.6);
  border: 1px solid rgba(0,0,0,0.08);
  border-radius: 999px;
  font-size: 13px;
  font-weight: 500;
  color: var(--ink-2, #4a5161);
  cursor: pointer;
  transition: all .15s;
}
.chip:hover { color: var(--ink, #0a0d12); border-color: rgba(0,0,0,0.18); }
.chip.active { background: var(--ink, #0a0d12); color: #fff; border-color: var(--ink, #0a0d12); }
.dark .chip { background: rgba(20,24,34,0.55); border-color: rgba(255,255,255,0.12); color: #a4adbf; }
.dark .chip:hover { color: #f4f6fa; }
.dark .chip.active { background: #f4f6fa; color: #07090f; border-color: #f4f6fa; }

.sort-chip {
  display: inline-flex;
  align-items: center;
  padding: 6px 11px;
  background: rgba(255,255,255,0.48);
  border: 1px solid rgba(0,0,0,0.07);
  border-radius: 8px;
  font-size: 12.5px;
  font-weight: 600;
  color: var(--ink-3, #8a93a3);
  cursor: pointer;
  transition: all .15s;
}
.sort-chip:hover { color: #0084ff; border-color: rgba(0,132,255,0.30); }
.sort-chip.active { background: rgba(0,132,255,0.10); color: #0084ff; border-color: rgba(0,132,255,0.35); }
.dark .sort-chip { background: rgba(20,24,34,0.45); border-color: rgba(255,255,255,0.10); color: #a4adbf; }
.dark .sort-chip.active { color: #9BCBFF; border-color: rgba(0,132,255,0.4); background: rgba(0,132,255,0.14); }

.notice-section {
  margin-bottom: 20px;
  border-radius: 14px;
  overflow: hidden;
  border: 1px solid rgba(217, 119, 6, 0.20);
  background: rgba(251, 191, 36, 0.06);
}
.dark .notice-section {
  background: rgba(251, 191, 36, 0.05);
  border-color: rgba(251, 191, 36, 0.15);
}

.notice-header {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 10px 16px;
  border-bottom: 1px solid rgba(217, 119, 6, 0.15);
}
.notice-icon { font-size: 14px; }
.notice-label {
  font-family: 'Nanum Gothic', monospace;
  font-size: 11.5px;
  font-weight: 700;
  letter-spacing: 0.8px;
  color: #d97706;
  text-transform: uppercase;
}
.dark .notice-label { color: #fbbf24; }

.notice-list { display: flex; flex-direction: column; }

.notice-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 16px;
  cursor: pointer;
  transition: background .12s;
  border-bottom: 1px solid rgba(217, 119, 6, 0.08);
}
.notice-item:last-child { border-bottom: none; }
.notice-item:hover { background: rgba(217, 119, 6, 0.06); }
.dark .notice-item:hover { background: rgba(251, 191, 36, 0.07); }
.notice-item:focus-visible { outline: 2px solid #d97706; outline-offset: -2px; }

.notice-pin { font-size: 12px; flex-shrink: 0; }

.notice-title {
  flex: 1;
  font-size: 13.5px;
  font-weight: 600;
  color: var(--ink, #0a0d12);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.dark .notice-title { color: #e0e4ef; }

.notice-date {
  font-family: 'Nanum Gothic', monospace;
  font-size: 11.5px;
  color: var(--ink-3, #8a93a3);
  flex-shrink: 0;
}

.post-list { display: flex; flex-direction: column; gap: 12px; }

.post-card {
  padding: 22px 24px;
  background: rgba(255,255,255,0.65);
  border: 1px solid rgba(0,0,0,0.07);
  border-radius: 18px;
  backdrop-filter: blur(40px) saturate(160%);
  -webkit-backdrop-filter: blur(40px) saturate(160%);
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.85), 0 4px 20px -8px rgba(20,40,80,0.10);
  cursor: pointer;
  transition: transform .15s, box-shadow .15s;
}
.post-card:hover,
.post-card:focus-visible {
  transform: translateY(-2px);
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.85), 0 12px 30px -10px rgba(20,40,80,0.15);
  outline: 2px solid #0084ff;
  outline-offset: 2px;
}
.dark .post-card {
  background: rgba(20,24,34,0.55);
  border-color: rgba(255,255,255,0.10);
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.10), 0 4px 20px -8px rgba(0,0,0,0.4);
}
.dark .post-card:hover { box-shadow: inset 0 1px 0 rgba(255,255,255,0.10), 0 12px 30px -10px rgba(0,0,0,0.5); }

.scrap-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 11.5px;
  font-weight: 600;
  font-family: 'Nanum Gothic', monospace;
  margin-bottom: 10px;
}

.post-title {
  font-family: 'Fustat', sans-serif;
  font-weight: 700;
  font-size: 17px;
  color: var(--ink, #0a0d12);
  margin: 0 0 8px;
  line-height: 1.35;
}
.dark .post-title { color: #f4f6fa; }

.post-preview {
  font-size: 13.5px;
  color: var(--ink-2, #4a5161);
  line-height: 1.6;
  margin: 0 0 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.dark .post-preview { color: #a4adbf; }

.scrap-preview {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  background: rgba(0,132,255,0.06);
  border: 1px solid rgba(0,132,255,0.15);
  border-radius: 10px;
  margin-bottom: 14px;
}
.dark .scrap-preview { background: rgba(0,132,255,0.10); border-color: rgba(0,132,255,0.20); }
.scrap-icon { font-size: 14px; flex-shrink: 0; }
.scrap-text {
  font-size: 12.5px;
  color: #0056cc;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.dark .scrap-text { color: #9BCBFF; }

.post-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 12px;
  border-top: 1px dashed rgba(0,0,0,0.08);
}
.dark .post-foot { border-color: rgba(255,255,255,0.10); }

.post-author { display: flex; align-items: center; gap: 8px; }

.mini-avatar {
  width: 24px; height: 24px;
  border-radius: 50%;
  background: radial-gradient(circle at 30% 30%, #9CCBFF 0%, #0084ff 60%, #0a4a99 100%);
  color: #fff;
  font-family: 'Fustat', sans-serif;
  font-weight: 700;
  font-size: 11px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}

.author-name { font-size: 12.5px; font-weight: 600; color: var(--ink, #0a0d12); }
.dark .author-name { color: #e0e4ef; }

.post-date {
  font-size: 11.5px;
  color: var(--ink-3, #8a93a3);
  font-family: 'Nanum Gothic', monospace;
}

.post-stats { display: flex; gap: 12px; }
.stat {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12.5px;
  color: var(--ink-3, #8a93a3);
  font-family: 'Nanum Gothic', monospace;
}

.loading-state { display: flex; justify-content: center; padding: 80px 0; }
.spinner {
  width: 32px; height: 32px;
  border: 3px solid rgba(0,132,255,0.15);
  border-top-color: #0084ff;
  border-radius: 50%;
  animation: spin .7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.empty { padding: 60px 0; }

@media (max-width: 640px) {
  .comm-shell { padding: 32px 0 60px; }
  .comm-title { font-size: 34px; }
  .comm-head { flex-direction: column; align-items: flex-start; gap: 16px; }
  .write-btn { width: 100%; justify-content: center; }
}
</style>
