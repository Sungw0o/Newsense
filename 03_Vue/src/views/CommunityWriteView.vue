<script setup>
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useCommunityStore } from '../stores/useCommunityStore'
import { useArticleStore } from '../stores/useArticleStore'

const router = useRouter()
const communityStore = useCommunityStore()
const articleStore = useArticleStore()

const title = ref('')
const content = ref('')
const selectedArticle = ref(null)
const showArticlePicker = ref(false)
const isSubmitting = ref(false)
const errorMsg = ref('')
const articleSearch = ref('')
const articleCategory = ref('')
const searchTimer = ref(null)

const categoryOptions = ['거시경제', '금융/투자', '정책/제도', '기업/산업', '글로벌경제']
const pickerArticles = computed(() => articleStore.articles)

const loadPickerArticles = async () => {
  articleStore.filters.keyword = articleSearch.value.trim()
  articleStore.filters.category = articleCategory.value
  articleStore.filters.difficulty = ''
  articleStore.filters.page = 0
  articleStore.filters.size = 12
  articleStore.articles = []
  articleStore.hasMore = true
  await articleStore.fetchArticles()
}

const openArticlePicker = () => {
  showArticlePicker.value = true
  loadPickerArticles()
}

watch([articleSearch, articleCategory], () => {
  if (!showArticlePicker.value) return
  window.clearTimeout(searchTimer.value)
  searchTimer.value = window.setTimeout(loadPickerArticles, 300)
})

const selectArticle = (article) => {
  selectedArticle.value = article
  showArticlePicker.value = false
}

const removeScrap = () => { selectedArticle.value = null }

const handleSubmit = async () => {
  if (!title.value.trim()) { errorMsg.value = '제목을 입력해 주세요.'; return }
  if (!content.value.trim()) { errorMsg.value = '내용을 입력해 주세요.'; return }
  errorMsg.value = ''
  isSubmitting.value = true
  try {
    const payload = {
      title: title.value.trim(),
      content: content.value.trim(),
      articleMetaId: selectedArticle.value?.articleId ?? null,
    }
    const created = await communityStore.createPost(payload)
    router.push(`/community/${created?.postId ?? ''}`)
  } catch {
    errorMsg.value = '게시글 작성에 실패했습니다. 다시 시도해 주세요.'
  } finally {
    isSubmitting.value = false
  }
}

const categoryColor = {
  '거시경제': '#6366f1', '금융/투자': '#0084ff', '정책/제도': '#d97706', '기업/산업': '#059669', '글로벌경제': '#0891b2',
}
</script>

<template>
  <div class="write-shell">
    <!-- Topbar -->
    <nav class="topbar">
      <button class="back-btn" @click="router.push('/community')">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="15 18 9 12 15 6"/>
        </svg>
        커뮤니티
      </button>
    </nav>

    <header class="write-head">
      <p class="eyebrow">새 게시글</p>
      <h1 class="write-title">글쓰기</h1>
    </header>

    <div class="write-card">
      <!-- Article scrap section -->
      <div class="scrap-section">
        <p class="section-label">기사 요약 스크랩 <span class="optional">(선택)</span></p>
        <p class="section-hint">학습한 기사 요약을 첨부하면 다른 사람들이 맥락을 이해하기 쉬워요.</p>

        <div v-if="selectedArticle" class="scrap-attached">
          <div class="scrap-head-row">
            <span class="scrap-cat-badge" :style="{ background: (categoryColor[selectedArticle.category] || '#0084ff') + '18', color: categoryColor[selectedArticle.category] || '#0084ff' }">
              {{ selectedArticle.category }}
            </span>
            <button class="remove-scrap" @click="removeScrap">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
              스크랩 제거
            </button>
          </div>
          <p class="scrap-att-title">{{ selectedArticle.title }}</p>
          <p class="scrap-att-summary">{{ selectedArticle.summary }}</p>
        </div>

        <button v-else class="scrap-trigger" @click="openArticlePicker">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
          기사 요약 첨부하기
        </button>
      </div>

      <!-- Form fields -->
      <div class="form-field">
        <label class="field-label" for="w-title">제목</label>
        <input
          id="w-title"
          v-model="title"
          type="text"
          class="field-input"
          placeholder="게시글 제목을 입력하세요"
          maxlength="100"
        />
      </div>

      <div class="form-field">
        <label class="field-label" for="w-content">내용</label>
        <textarea
          id="w-content"
          v-model="content"
          class="field-input field-textarea"
          placeholder="경제 기사를 읽고 느낀 점, 질문, 정보를 자유롭게 작성하세요."
          rows="10"
        ></textarea>
      </div>

      <div v-if="errorMsg" class="error-msg">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
        {{ errorMsg }}
      </div>

      <div class="write-foot">
        <button class="cancel-btn" @click="router.push('/community')">취소</button>
        <button class="btn-primary submit-btn" :disabled="isSubmitting" @click="handleSubmit">
          <span v-if="isSubmitting" class="spinner-sm"></span>
          {{ isSubmitting ? '게시 중…' : '게시하기' }}
        </button>
      </div>
    </div>

    <!-- Article picker modal -->
    <Teleport to="body">
      <div v-if="showArticlePicker" class="modal-backdrop" @click.self="showArticlePicker = false">
        <div class="picker-box">
          <div class="picker-head">
            <h3 class="picker-title">기사 선택</h3>
            <button class="picker-close" @click="showArticlePicker = false">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            </button>
          </div>
          <p class="picker-hint">검색어와 카테고리로 첨부할 기사를 찾으세요.</p>
          <div class="picker-controls">
            <label class="sr-only" for="article-picker-search">기사 검색어</label>
            <input
              id="article-picker-search"
              v-model="articleSearch"
              class="picker-search"
              type="search"
              placeholder="기사 제목이나 요약 검색"
            />
            <label class="sr-only" for="article-picker-category">기사 카테고리</label>
            <select id="article-picker-category" v-model="articleCategory" class="picker-select">
              <option value="">전체 카테고리</option>
              <option v-for="category in categoryOptions" :key="category" :value="category">
                {{ category }}
              </option>
            </select>
          </div>
          <div v-if="selectedArticle" class="picker-preview">
            <div>
              <span class="pick-cat" :style="{ color: categoryColor[selectedArticle.category] || '#0084ff' }">{{ selectedArticle.category }}</span>
              <p class="preview-title">{{ selectedArticle.title }}</p>
            </div>
            <button class="preview-clear" @click="removeScrap">선택 취소</button>
          </div>
          <div class="picker-list">
            <button
              v-for="article in pickerArticles"
              :key="article.articleId"
              class="picker-item"
              :class="{ selected: selectedArticle?.articleId === article.articleId }"
              @click="selectArticle(article)"
            >
              <span class="pick-cat" :style="{ color: categoryColor[article.category] || '#0084ff' }">{{ article.category }}</span>
              <span class="pick-body">
                <span class="pick-title">{{ article.title }}</span>
                <span class="pick-summary">{{ article.summary }}</span>
              </span>
            </button>
            <p v-if="articleStore.isLoading" class="picker-state">검색 중입니다.</p>
            <p v-else-if="!pickerArticles.length" class="picker-state">검색 결과가 없습니다.</p>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.write-shell {
  max-width: 720px;
  margin: 0 auto;
  padding: 32px 0 80px;
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

.topbar { margin-bottom: 24px; }
.back-btn {
  display: inline-flex; align-items: center; gap: 6px;
  background: none; border: none; color: var(--ink-2, #4a5161);
  font-size: 13.5px; font-weight: 500; cursor: pointer; padding: 6px 0; transition: color .15s;
}
.back-btn:hover { color: var(--ink, #0a0d12); }
.dark .back-btn { color: #a4adbf; }
.dark .back-btn:hover { color: #f4f6fa; }

.write-head { margin-bottom: 24px; }
.write-title {
  font-family: 'Fustat', sans-serif;
  font-weight: 800;
  font-size: 36px;
  letter-spacing: -1px;
  color: var(--ink, #0a0d12);
  margin: 0;
}
.dark .write-title { color: #f4f6fa; }

.write-card {
  padding: 32px;
  background: rgba(255,255,255,0.65);
  border: 1px solid rgba(0,0,0,0.07);
  border-radius: 22px;
  backdrop-filter: blur(40px) saturate(160%);
  -webkit-backdrop-filter: blur(40px) saturate(160%);
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.85), 0 20px 50px -22px rgba(20,40,80,0.15);
}
.dark .write-card {
  background: rgba(20,24,34,0.55);
  border-color: rgba(255,255,255,0.10);
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.10), 0 20px 50px -22px rgba(0,0,0,0.5);
}

/* Scrap section */
.scrap-section { margin-bottom: 28px; padding-bottom: 24px; border-bottom: 1px dashed rgba(0,0,0,0.08); }
.dark .scrap-section { border-color: rgba(255,255,255,0.10); }

.section-label {
  font-size: 13px; font-weight: 600; color: var(--ink, #0a0d12); margin: 0 0 4px;
}
.dark .section-label { color: #f4f6fa; }
.optional { font-weight: 400; color: var(--ink-3, #8a93a3); }
.section-hint { font-size: 12.5px; color: var(--ink-3, #8a93a3); margin: 0 0 14px; }

.scrap-trigger {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: rgba(0,132,255,0.06);
  border: 1.5px dashed rgba(0,132,255,0.30);
  border-radius: 12px;
  font-size: 13.5px;
  font-weight: 500;
  color: #0084ff;
  cursor: pointer;
  transition: all .15s;
}
.scrap-trigger:hover { background: rgba(0,132,255,0.12); border-style: solid; }
.dark .scrap-trigger { background: rgba(0,132,255,0.10); border-color: rgba(0,132,255,0.35); color: #9BCBFF; }

.scrap-attached {
  padding: 16px;
  background: rgba(0,132,255,0.06);
  border: 1px solid rgba(0,132,255,0.18);
  border-radius: 14px;
}
.dark .scrap-attached { background: rgba(0,132,255,0.10); border-color: rgba(0,132,255,0.25); }

.scrap-head-row { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.scrap-cat-badge {
  display: inline-flex; padding: 3px 10px; border-radius: 999px;
  font-size: 11.5px; font-weight: 600; font-family: 'Nanum Gothic', monospace;
}
.remove-scrap {
  display: inline-flex; align-items: center; gap: 5px;
  background: none; border: none; font-size: 12px; color: var(--ink-3, #8a93a3); cursor: pointer;
}
.remove-scrap:hover { color: #b02a2a; }

.scrap-att-title { font-family: 'Fustat', sans-serif; font-weight: 700; font-size: 14.5px; color: #0056cc; margin: 0 0 5px; }
.dark .scrap-att-title { color: #9BCBFF; }
.scrap-att-summary { font-size: 12.5px; color: var(--ink-2, #4a5161); line-height: 1.55; margin: 0; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.dark .scrap-att-summary { color: #a4adbf; }

/* Form */
.form-field { margin-bottom: 20px; }
.field-label { display: block; font-size: 13px; font-weight: 600; color: var(--ink, #0a0d12); margin-bottom: 8px; }
.dark .field-label { color: #f4f6fa; }

.field-input {
  width: 100%;
  padding: 12px 16px;
  background: rgba(255,255,255,0.85);
  border: 1.5px solid rgba(0,0,0,0.08);
  border-radius: 12px;
  font: inherit;
  font-size: 14px;
  color: var(--ink, #0a0d12);
  outline: none;
  transition: all .15s;
  box-sizing: border-box;
}
.field-input:focus { border-color: #0084ff; box-shadow: 0 0 0 4px rgba(0,132,255,0.10); }
.field-input::placeholder { color: var(--ink-3, #8a93a3); }
.dark .field-input { background: rgba(20,24,34,0.70); border-color: rgba(255,255,255,0.12); color: #f4f6fa; }
.dark .field-input:focus { background: rgba(20,24,34,0.90); }
.field-textarea { resize: vertical; min-height: 200px; }

.error-msg {
  display: flex; align-items: center; gap: 8px;
  padding: 12px 14px;
  background: rgba(255,237,237,0.85);
  border: 1px solid rgba(176,42,42,0.25);
  border-radius: 10px;
  font-size: 13.5px; color: #b02a2a;
  margin-bottom: 16px;
}
.dark .error-msg { background: rgba(255,106,106,0.13); border-color: rgba(255,106,106,0.4); color: #ff8a8a; }

.write-foot { display: flex; justify-content: flex-end; gap: 10px; padding-top: 20px; border-top: 1px solid rgba(0,0,0,0.07); }
.dark .write-foot { border-color: rgba(255,255,255,0.10); }

.cancel-btn {
  padding: 11px 20px; background: transparent; border: 1.5px solid rgba(0,0,0,0.10);
  border-radius: 12px; font-size: 14px; font-weight: 500; color: var(--ink-2, #4a5161); cursor: pointer; transition: all .15s;
}
.cancel-btn:hover { border-color: rgba(0,0,0,0.20); color: var(--ink, #0a0d12); }
.dark .cancel-btn { border-color: rgba(255,255,255,0.15); color: #a4adbf; }
.dark .cancel-btn:hover { color: #f4f6fa; }

.submit-btn { display: inline-flex; align-items: center; gap: 8px; padding: 11px 24px; font-size: 14px; }
.submit-btn:disabled { opacity: 0.55; cursor: default; }

/* Article picker modal */
.modal-backdrop {
  position: fixed; inset: 0; background: rgba(0,0,0,0.45); backdrop-filter: blur(6px);
  display: flex; align-items: center; justify-content: center; z-index: 9999;
}
.picker-box {
  width: 540px; max-height: 80vh;
  display: flex; flex-direction: column;
  padding: 28px;
  background: rgba(255,255,255,0.92);
  border: 1px solid rgba(0,0,0,0.07);
  border-radius: 24px;
  backdrop-filter: blur(48px) saturate(180%);
  box-shadow: 0 30px 80px -20px rgba(20,40,80,0.25);
}
.dark .picker-box { background: rgba(20,24,34,0.90); border-color: rgba(255,255,255,0.12); }

.picker-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 6px; }
.picker-title { font-family: 'Fustat', sans-serif; font-weight: 800; font-size: 20px; color: var(--ink, #0a0d12); margin: 0; }
.dark .picker-title { color: #f4f6fa; }
.picker-close { background: none; border: none; cursor: pointer; color: var(--ink-3, #8a93a3); padding: 4px; border-radius: 8px; }
.picker-close:hover { color: var(--ink, #0a0d12); background: rgba(0,0,0,0.06); }
.dark .picker-close:hover { color: #f4f6fa; background: rgba(255,255,255,0.08); }

.picker-hint { font-size: 13px; color: var(--ink-3, #8a93a3); margin: 0 0 16px; }

.picker-controls {
  display: grid;
  grid-template-columns: 1fr 150px;
  gap: 10px;
  margin-bottom: 12px;
}

.picker-search,
.picker-select {
  min-width: 0;
  padding: 11px 12px;
  border: 1px solid rgba(0,0,0,0.10);
  border-radius: 10px;
  background: rgba(255,255,255,0.82);
  color: var(--ink, #0a0d12);
  font: inherit;
  font-size: 13px;
  outline: none;
}
.picker-search:focus,
.picker-select:focus { border-color: #0084ff; box-shadow: 0 0 0 3px rgba(0,132,255,0.12); }
.dark .picker-search,
.dark .picker-select { background: rgba(20,24,34,0.70); border-color: rgba(255,255,255,0.12); color: #f4f6fa; }

.picker-preview {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  padding: 12px;
  margin-bottom: 12px;
  border: 1px solid rgba(0,132,255,0.22);
  border-radius: 12px;
  background: rgba(0,132,255,0.07);
}
.preview-title { margin: 4px 0 0; font-size: 13px; font-weight: 700; color: var(--ink, #0a0d12); }
.dark .preview-title { color: #f4f6fa; }
.preview-clear { border: none; background: transparent; color: #b02a2a; font-size: 12px; cursor: pointer; white-space: nowrap; }

.picker-list { overflow-y: auto; display: flex; flex-direction: column; gap: 8px; }
.picker-item {
  display: flex; align-items: flex-start; gap: 12px;
  padding: 14px 16px;
  background: rgba(255,255,255,0.7);
  border: 1px solid rgba(0,0,0,0.07);
  border-radius: 12px;
  cursor: pointer;
  text-align: left;
  transition: all .15s;
}
.picker-item:hover { border-color: #0084ff; background: rgba(0,132,255,0.05); }
.picker-item.selected { border-color: #0084ff; background: rgba(0,132,255,0.10); }
.dark .picker-item { background: rgba(20,24,34,0.55); border-color: rgba(255,255,255,0.10); }
.dark .picker-item:hover { border-color: #0084ff; background: rgba(0,132,255,0.10); }

.pick-cat { font-family: 'Nanum Gothic', monospace; font-size: 11px; font-weight: 600; flex-shrink: 0; padding-top: 2px; }
.pick-body { min-width: 0; display: flex; flex-direction: column; gap: 4px; }
.pick-title { font-size: 14px; font-weight: 500; color: var(--ink, #0a0d12); line-height: 1.45; }
.dark .pick-title { color: #e0e4ef; }
.pick-summary { font-size: 12px; line-height: 1.45; color: var(--ink-3, #8a93a3); display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.picker-state { margin: 12px 0 0; text-align: center; font-size: 13px; color: var(--ink-3, #8a93a3); }

.spinner-sm {
  width: 14px; height: 14px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin .65s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

@media (max-width: 640px) {
  .write-shell { padding: 24px 0 60px; }
  .write-card { padding: 20px; }
  .picker-box { width: 90vw; }
  .picker-controls { grid-template-columns: 1fr; }
  .picker-preview { align-items: flex-start; flex-direction: column; }
}
</style>
