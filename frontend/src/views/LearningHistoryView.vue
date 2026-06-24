<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useHistoryStore } from '../stores/useHistoryStore'
import { useWrongNoteStore } from '../stores/useWrongNoteStore'

const router = useRouter()
const historyStore = useHistoryStore()
const wrongNoteStore = useWrongNoteStore()

const { history, stats, bookmarks, recommendations, isLoading, error } = storeToRefs(historyStore)
const { wrongNotes, isLoading: isWrongNoteLoading } = storeToRefs(wrongNoteStore)

const unresolvedWrongNotes = computed(() => wrongNotes.value.filter(note => !note.isResolved))

onMounted(async () => {
  await Promise.all([
    historyStore.fetchHistory(),
    historyStore.fetchStats(),
    historyStore.fetchBookmarks(),
    historyStore.fetchRecommendations(),
    wrongNoteStore.fetchWrongNotes()
  ]).catch(() => {})
})

const typeLabel = { ARTICLE_READ: '기사 읽기', QUIZ: '퀴즈', REVIEW: '리뷰' }
const typeColor = { ARTICLE_READ: '#0084ff', QUIZ: '#d97706', REVIEW: '#059669' }

const formatTime = (str) => (str && str.length >= 16) ? str.substring(11, 16) : ''

const navigate = (path) => router.push(path)

// Group timeline items by articleId within each day
const groupedDays = computed(() => {
  if (!history.value?.days) return []
  return history.value.days.map(day => {
    const groups = []
    const seen = new Map()
    for (const item of day.timeline) {
      const key = item.articleId ?? item.historyId
      if (!seen.has(key)) {
        seen.set(key, { articleId: item.articleId, articleTitle: item.articleTitle, articleCategory: item.articleCategory, items: [] })
        groups.push(seen.get(key))
      }
      seen.get(key).items.push(item)
    }
    return { ...day, groups }
  })
})

// Tracks which article groups are expanded (when >1 event)
const expanded = ref(new Set())
const toggleGroup = (dayDate, articleId) => {
  const key = `${dayDate}__${articleId}`
  if (expanded.value.has(key)) {
    expanded.value.delete(key)
  } else {
    expanded.value.add(key)
  }
  // trigger reactivity
  expanded.value = new Set(expanded.value)
}
const isExpanded = (dayDate, articleId) => expanded.value.has(`${dayDate}__${articleId}`)

const openSections = ref(new Set(['timeline', 'recommendations', 'bookmarks', 'wrongNotes']))
const toggleSection = (key) => {
  if (openSections.value.has(key)) {
    openSections.value.delete(key)
  } else {
    openSections.value.add(key)
  }
  openSections.value = new Set(openSections.value)
}
const isSectionOpen = (key) => openSections.value.has(key)
</script>

<template>
  <div class="history-shell">
    <!-- Page header -->
    <header class="history-head">
      <div>
        <p class="eyebrow">내 학습 관리</p>
        <h1 class="history-title">학습 이력</h1>
        <p class="history-sub">뉴스센스에서 차곡차곡 쌓아온 경제 지식을 확인하세요.</p>
      </div>
    </header>

    <!-- Error banner -->
    <div v-if="error" class="error-banner">⚠️ {{ error }}</div>

    <!-- Streak banner -->
    <div class="streak-banner">
      <div class="streak-left">
        <span class="streak-flame">🔥</span>
        <div>
          <p class="streak-num">{{ stats?.consecutiveLearningDays ?? 0 }}일 연속 학습 중</p>
          <p class="streak-sub">매일 뉴스를 읽고 경제 감각을 키워보세요!</p>
        </div>
      </div>
      <div class="streak-dots">
        <div v-for="(_, i) in 7" :key="i" class="streak-dot" :class="{ active: i < (stats?.weeklyLearningDays ?? 0) }">
          <span v-if="i < (stats?.weeklyLearningDays ?? 0)" class="dot-fire">🔥</span>
          <span v-else class="dot-empty">○</span>
          <span class="dot-day">{{ ['월','화','수','목','금','토','일'][i] }}</span>
        </div>
      </div>
    </div>

    <!-- Stats grid (4 cards) -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon" style="background: rgba(0,132,255,0.10); color:#0084ff;">📰</div>
        <div>
          <p class="stat-label">총 읽은 기사</p>
          <p class="stat-value">
            <template v-if="isLoading"><span class="skel"></span></template>
            <template v-else>{{ stats?.totalReadArticleCount ?? 0 }}<span class="stat-unit">개</span></template>
          </p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: rgba(217,119,6,0.10); color:#d97706;">🎯</div>
        <div>
          <p class="stat-label">퀴즈 정답률</p>
          <p class="stat-value">
            <template v-if="isLoading"><span class="skel"></span></template>
            <template v-else>{{ stats?.quizAccuracyRate ?? 0 }}<span class="stat-unit">%</span></template>
          </p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: rgba(255,128,30,0.10); color:#FF801E;">🔥</div>
        <div>
          <p class="stat-label">연속 학습 일수</p>
          <p class="stat-value">
            <template v-if="isLoading"><span class="skel"></span></template>
            <template v-else>{{ stats?.consecutiveLearningDays ?? 0 }}<span class="stat-unit">일째</span></template>
          </p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: rgba(5,150,105,0.10); color:#059669;">📅</div>
        <div>
          <p class="stat-label">이번 주 학습일</p>
          <p class="stat-value">
            <template v-if="isLoading"><span class="skel"></span></template>
            <template v-else>{{ stats?.weeklyLearningDays ?? 0 }}<span class="stat-unit">일</span></template>
          </p>
        </div>
      </div>
    </div>

    <!-- History timeline -->
    <section class="section-card">
      <button class="section-toggle" @click="toggleSection('timeline')">
        <span class="section-title">최근 학습 타임라인</span>
        <span class="toggle-arrow" :class="{ open: isSectionOpen('timeline') }">▾</span>
      </button>

      <div v-if="isSectionOpen('timeline') && isLoading" class="timeline-skel">
        <div v-for="n in 3" :key="n" class="skel-row">
          <span class="skel" style="width:80px;height:14px;"></span>
          <span class="skel" style="width:60%;height:18px;margin-top:6px;"></span>
        </div>
      </div>

      <div v-else-if="isSectionOpen('timeline') && !history?.days?.length" class="empty-state">
        <p class="empty-icon">📚</p>
        <p class="empty-title">아직 학습 이력이 없습니다</p>
        <p class="empty-desc">관심 경제 뉴스를 읽고 학습을 시작해 보세요!</p>
        <button class="btn-primary" style="margin-top:16px;" @click="navigate('/')">뉴스 피드 보기</button>
      </div>

      <div v-else-if="isSectionOpen('timeline')" class="timeline">
        <div v-for="day in groupedDays" :key="day.date" class="day-group">
          <div class="day-header">
            <span class="day-label">{{ day.date }}</span>
            <div class="day-divider"></div>
            <span class="day-counts">기사 {{ day.articleReadCount }} · 퀴즈 {{ day.quizCount }} · 리뷰 {{ day.reviewCount }}</span>
          </div>

          <div class="timeline-items">
            <!-- Grouped by article — all groups are collapsible -->
            <div v-for="group in day.groups" :key="group.articleId ?? group.items[0].historyId" class="article-group">
              <div class="group-toggle">
                <div
                  class="group-toggle-header"
                  @click="toggleGroup(day.date, group.articleId ?? group.items[0].historyId)"
                >
                  <div class="item-left">
                    <div class="type-badges">
                      <span
                        v-for="item in group.items"
                        :key="item.historyId"
                        class="type-badge"
                        :style="{ background: (typeColor[item.type] ?? '#0084ff') + '18', color: typeColor[item.type] ?? '#0084ff' }"
                      >{{ typeLabel[item.type] ?? item.type }}</span>
                      <span v-if="group.articleCategory" class="category-badge">{{ group.articleCategory }}</span>
                      <span v-if="group.items.length === 1" class="time-badge">{{ formatTime(group.items[0].learnedAt) }}</span>
                    </div>
                    <p class="item-title" @click.stop="navigate(`/articles/${group.articleId}`)">{{ group.articleTitle }}</p>
                  </div>
                  <div class="item-right">
                    <span class="toggle-arrow" :class="{ open: isExpanded(day.date, group.articleId ?? group.items[0].historyId) }">▾</span>
                  </div>
                </div>

                <!-- Expanded sub-items -->
                <div v-if="isExpanded(day.date, group.articleId ?? group.items[0].historyId)" class="group-sub-items">
                  <div v-for="item in group.items" :key="item.historyId" class="sub-item">
                    <div class="sub-item-left">
                      <div class="type-badges">
                        <span class="type-badge" :style="{ background: (typeColor[item.type] ?? '#0084ff') + '18', color: typeColor[item.type] ?? '#0084ff' }">
                          {{ typeLabel[item.type] ?? item.type }}
                        </span>
                        <span class="time-badge">{{ formatTime(item.learnedAt) }}</span>
                      </div>
                      <div v-if="item.type === 'REVIEW' && (item.reviewSummary || item.reviewLearned)" class="review-card">
                        <div v-if="item.reviewSummary">
                          <p class="review-label">요약</p>
                          <p class="review-text">{{ item.reviewSummary }}</p>
                        </div>
                        <div v-if="item.reviewLearned">
                          <p class="review-label">배운 점</p>
                          <p class="review-text">{{ item.reviewLearned }}</p>
                        </div>
                      </div>
                    </div>
                    <div class="item-right">
                      <span v-if="item.type === 'QUIZ'" class="quiz-result" :class="item.quizCorrect ? 'correct' : 'wrong'">
                        {{ item.quizCorrect ? '🎯 정답' : '❌ 오답' }}
                      </span>
                      <button v-else-if="item.type === 'REVIEW'" class="btn-ghost" @click="navigate(`/articles/${group.articleId}/review`)">
                        리뷰 보기
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- AI-based personalized recommendations -->
    <section v-if="recommendations && (recommendations.articles?.length || recommendations.results?.length)" class="section-card ai-rec">
      <div class="section-head-row">
        <div>
          <h2 class="section-title">🤖 AI 맞춤 추천 기사</h2>
          <p class="section-sub">오답노트의 취약 개념을 바탕으로 복습에 도움이 될 기사를 추천합니다.</p>
        </div>
        <button class="section-mini-toggle" @click="toggleSection('recommendations')">
          <span class="toggle-arrow" :class="{ open: isSectionOpen('recommendations') }">▾</span>
        </button>
      </div>

      <div v-if="isSectionOpen('recommendations') && recommendations.weaknessTerms?.length" class="weakness-tags">
        <span class="tag-label">분석된 취약 개념</span>
        <span v-for="term in recommendations.weaknessTerms.slice(0, 6)" :key="term" class="weakness-tag">{{ term }}</span>
      </div>

      <div v-if="isSectionOpen('recommendations')" class="rec-list">
        <div
          v-for="item in (recommendations.articles || recommendations.results || []).slice(0, 6)"
          :key="item.article?.articleId || item.articleId"
          class="rec-item"
          @click="navigate(`/articles/${item.article?.articleId || item.articleId}`)"
        >
          <div class="rec-meta">
            <span class="bookmark-category">{{ item.article?.category || item.category }}</span>
            <span class="bookmark-source">{{ item.article?.source || item.source }}</span>
          </div>
          <p class="bookmark-title">{{ item.article?.title || item.title }}</p>
          <div v-if="item.matchedKeywords?.length" class="matched-keywords">
            <span v-for="kw in item.matchedKeywords.slice(0, 3)" :key="kw" class="matched-kw">{{ kw }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- Bookmarked articles -->
    <section class="section-card">
      <div class="section-head-row">
        <div>
          <h2 class="section-title">북마크한 기사</h2>
          <p class="section-sub">저장해 둔 기사를 빠르게 다시 읽어보세요.</p>
        </div>
        <button class="section-mini-toggle" @click="toggleSection('bookmarks')">
          <span class="toggle-arrow" :class="{ open: isSectionOpen('bookmarks') }">▾</span>
        </button>
      </div>

      <div v-if="isSectionOpen('bookmarks') && !bookmarks.length" class="empty-state small">
        <p class="empty-icon">🔖</p>
        <p class="empty-desc">북마크한 기사가 없습니다.</p>
      </div>

      <div v-else-if="isSectionOpen('bookmarks')" class="bookmark-list">
        <div
          v-for="article in bookmarks"
          :key="article.articleId"
          class="bookmark-item"
          @click="navigate(`/articles/${article.articleId}`)"
        >
          <div class="bookmark-meta">
            <span class="bookmark-category">{{ article.category }}</span>
            <span class="bookmark-source">{{ article.source }}</span>
          </div>
          <p class="bookmark-title">{{ article.title }}</p>
          <p class="bookmark-summary">{{ article.summary }}</p>
        </div>
      </div>
    </section>

    <!-- Wrong notes preview -->
    <section class="section-card">
      <div class="section-head-row">
        <div>
          <h2 class="section-title">오답 노트</h2>
          <p class="section-sub">학습 이력에서 복습할 문제를 확인하세요.</p>
        </div>
        <div class="section-actions">
          <button class="section-mini-toggle" @click="toggleSection('wrongNotes')">
            <span class="toggle-arrow" :class="{ open: isSectionOpen('wrongNotes') }">▾</span>
          </button>
          <button class="btn-primary" @click="navigate('/wrong-notes')">전체 보기</button>
        </div>
      </div>

      <div v-if="isSectionOpen('wrongNotes') && isWrongNoteLoading" class="timeline-skel">
        <div v-for="n in 3" :key="n" class="skel-row">
          <span class="skel" style="width:75%;height:16px;"></span>
          <span class="skel" style="width:50%;height:12px;margin-top:6px;"></span>
        </div>
      </div>

      <div v-else-if="isSectionOpen('wrongNotes') && unresolvedWrongNotes.length === 0" class="empty-state small">
        <p class="empty-icon">✅</p>
        <p class="empty-desc">복습할 오답이 없습니다.</p>
      </div>

      <div v-else-if="isSectionOpen('wrongNotes')" class="wrong-list">
        <div v-for="note in unresolvedWrongNotes.slice(0, 5)" :key="note.id" class="wrong-item">
          <div class="wrong-content">
            <p class="wrong-question">{{ note.question || note.quizQuestion || '복습 문제' }}</p>
            <p class="wrong-article">{{ note.articleTitle || '연결된 기사' }}</p>
          </div>
          <span class="wrong-badge">복습 필요</span>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.history-shell {
  max-width: 900px;
  margin: 0 auto;
  padding: 48px 0 80px;
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.eyebrow {
  font-family: 'Nanum Gothic', monospace;
  font-size: 11.5px;
  letter-spacing: 1px;
  color: var(--ink-3);
  text-transform: uppercase;
  margin: 0 0 10px;
}

.history-head { margin-bottom: 4px; }

.history-title {
  font-family: 'Fustat', sans-serif;
  font-weight: 800;
  font-size: 44px;
  letter-spacing: -1.5px;
  color: var(--ink);
  margin: 0 0 6px;
}
.dark .history-title { color: #f4f6fa; }

.history-sub { font-size: 14px; color: var(--ink-2); margin: 0; }
.dark .history-sub { color: #a4adbf; }

.error-banner {
  padding: 12px 16px;
  border-radius: 12px;
  background: var(--no-bg);
  border: 1px solid var(--no-border);
  color: var(--no);
  font-size: 13.5px;
}

/* Stats */
/* Streak banner */
.streak-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 22px 28px;
  background: linear-gradient(135deg, rgba(255,128,30,0.12) 0%, rgba(255,180,50,0.08) 100%);
  border: 1px solid rgba(255,128,30,0.22);
  border-radius: 22px;
  backdrop-filter: blur(40px) saturate(160%);
  -webkit-backdrop-filter: blur(40px) saturate(160%);
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.85), 0 4px 16px -6px rgba(255,128,30,0.15);
  flex-wrap: wrap;
}
.dark .streak-banner {
  background: linear-gradient(135deg, rgba(255,128,30,0.15) 0%, rgba(255,180,50,0.08) 100%);
  border-color: rgba(255,128,30,0.30);
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.06), 0 4px 16px -6px rgba(0,0,0,0.3);
}

.streak-left {
  display: flex;
  align-items: center;
  gap: 14px;
}
.streak-flame { font-size: 32px; filter: drop-shadow(0 2px 4px rgba(255,128,30,0.4)); }
.streak-num {
  font-family: 'Fustat', sans-serif;
  font-weight: 800;
  font-size: 20px;
  color: #FF801E;
  margin: 0 0 3px;
}
.streak-sub { font-size: 12.5px; color: var(--ink-3); margin: 0; }
.dark .streak-sub { color: #a4adbf; }

.streak-dots {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
}
.streak-dot {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}
.dot-fire { font-size: 18px; filter: drop-shadow(0 1px 3px rgba(255,128,30,0.5)); }
.dot-empty { font-size: 16px; color: var(--line); line-height: 1; }
.dot-day { font-size: 10.5px; font-family: 'Nanum Gothic', monospace; color: var(--ink-3); }

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.65);
  border: 1px solid rgba(0, 0, 0, 0.07);
  border-radius: 18px;
  backdrop-filter: blur(40px) saturate(160%);
  -webkit-backdrop-filter: blur(40px) saturate(160%);
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.85), 0 4px 16px -6px rgba(20,40,80,0.08);
}
.dark .stat-card {
  background: rgba(20,24,34,0.55);
  border-color: rgba(255,255,255,0.10);
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.08), 0 4px 16px -6px rgba(0,0,0,0.3);
}

.stat-icon {
  width: 42px; height: 42px;
  border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.stat-label {
  font-size: 11.5px;
  color: var(--ink-3);
  font-family: 'Nanum Gothic', monospace;
  letter-spacing: 0.5px;
  text-transform: uppercase;
  margin: 0 0 4px;
}

.stat-value {
  font-family: 'Fustat', sans-serif;
  font-weight: 800;
  font-size: 28px;
  color: var(--ink);
  margin: 0;
  line-height: 1;
}
.dark .stat-value { color: #f4f6fa; }

.stat-unit {
  font-size: 14px;
  font-weight: 400;
  color: var(--ink-3);
  margin-left: 3px;
}

/* Section card */
.section-card {
  padding: 28px;
  background: rgba(255, 255, 255, 0.65);
  border: 1px solid rgba(0, 0, 0, 0.07);
  border-radius: 24px;
  backdrop-filter: blur(40px) saturate(160%);
  -webkit-backdrop-filter: blur(40px) saturate(160%);
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.85), 0 4px 20px -8px rgba(20,40,80,0.10);
}
.dark .section-card {
  background: rgba(20,24,34,0.55);
  border-color: rgba(255,255,255,0.10);
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.08), 0 4px 20px -8px rgba(0,0,0,0.35);
}

.section-head-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--line);
}

.section-toggle {
  width: 100%;
  padding: 0 0 16px;
  margin: 0 0 20px;
  border: none;
  border-bottom: 1px solid var(--line);
  background: none;
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
}

.section-mini-toggle {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  border: 1px solid var(--line);
  background: var(--bg-soft);
  color: var(--ink-2);
  cursor: pointer;
  transition: all .2s;
}
.section-mini-toggle:hover {
  border-color: #0084ff;
  color: #0084ff;
}

.section-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-title {
  font-family: 'Fustat', sans-serif;
  font-weight: 700;
  font-size: 18px;
  color: var(--ink);
  margin: 0 0 4px;
}
.dark .section-title { color: #f4f6fa; }

.section-sub { font-size: 13px; color: var(--ink-3); margin: 0; }

/* Timeline */
.timeline { display: flex; flex-direction: column; gap: 24px; }
.day-group {}

/* Article group container */
.article-group {
  border-bottom: 1px solid var(--line);
}
.article-group:last-child { border-bottom: none; }

/* Collapsible toggle header */
.group-toggle {}
.group-toggle-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 0;
  cursor: pointer;
  user-select: none;
}
.group-toggle-header:hover .item-title { color: #0084ff; }

.toggle-arrow {
  font-size: 16px;
  color: var(--ink-3);
  transition: transform 0.2s;
  flex-shrink: 0;
  margin-top: 2px;
}
.toggle-arrow.open { transform: rotate(180deg); }

/* Expanded sub-items */
.group-sub-items {
  padding: 0 0 10px 16px;
  border-left: 2px solid var(--line);
  margin-left: 8px;
  display: flex;
  flex-direction: column;
  gap: 0;
}
.sub-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 10px 0;
  border-bottom: 1px dashed var(--line);
}
.sub-item:last-child { border-bottom: none; }
.sub-item-left { flex: 1; min-width: 0; }

.day-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.day-label {
  font-family: 'Fustat', sans-serif;
  font-size: 13px;
  font-weight: 700;
  color: var(--ink);
  white-space: nowrap;
}
.dark .day-label { color: #f4f6fa; }

.day-divider { flex: 1; height: 1px; background: var(--line); }

.day-counts {
  font-size: 11.5px;
  color: var(--ink-3);
  white-space: nowrap;
  font-family: 'Nanum Gothic', monospace;
}

.timeline-items { display: flex; flex-direction: column; }

.timeline-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 0;
}

.item-left { flex: 1; min-width: 0; }

.type-badges { display: flex; align-items: center; gap: 7px; margin-bottom: 6px; flex-wrap: wrap; }

.type-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 5px;
  font-size: 11px;
  font-weight: 700;
  font-family: 'Nanum Gothic', monospace;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.category-badge {
  font-size: 11.5px;
  color: var(--ink-3);
  background: var(--bg-soft);
  padding: 2px 8px;
  border-radius: 5px;
  font-family: 'Nanum Gothic', monospace;
}

.time-badge { font-size: 11.5px; color: var(--ink-3); font-family: 'Nanum Gothic', monospace; }

.item-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--ink);
  margin: 0;
  cursor: pointer;
  transition: color .15s;
  line-height: 1.4;
}
.item-title:hover { color: #0084ff; }
.dark .item-title { color: #e0e4ef; }

.review-card {
  margin-top: 10px;
  padding: 12px 14px;
  background: var(--bg-soft);
  border: 1px solid var(--line);
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.review-label {
  font-size: 11px;
  font-weight: 700;
  color: var(--ink-3);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin: 0 0 3px;
}

.review-text {
  font-size: 13.5px;
  color: var(--ink-2);
  line-height: 1.6;
  margin: 0;
}
.dark .review-text { color: #a4adbf; }

.item-right { flex-shrink: 0; }

.quiz-result {
  display: inline-block;
  font-size: 12px;
  font-weight: 700;
  padding: 5px 12px;
  border-radius: 8px;
}
.quiz-result.correct { background: var(--ok-bg); color: var(--ok); border: 1px solid var(--ok-border); }
.quiz-result.wrong { background: var(--no-bg); color: var(--no); border: 1px solid var(--no-border); }

.btn-ghost {
  padding: 6px 14px;
  border-radius: 9px;
  border: 1px solid rgba(0,0,0,0.10);
  background: none;
  font-size: 12.5px;
  font-weight: 600;
  color: var(--ink-2);
  cursor: pointer;
  transition: border-color .12s, color .12s;
}
.btn-ghost:hover { border-color: #0084ff; color: #0084ff; }
.dark .btn-ghost { border-color: rgba(255,255,255,0.12); color: #a4adbf; }
.dark .btn-ghost:hover { border-color: #4FB3FF; color: #4FB3FF; }

/* AI Recommendations */
.ai-rec { border-color: rgba(0,132,255,0.20); }
.dark .ai-rec { border-color: rgba(0,132,255,0.25); }

.weakness-tags {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
  padding: 12px 14px;
  background: rgba(0,132,255,0.05);
  border: 1px solid rgba(0,132,255,0.12);
  border-radius: 12px;
}

.tag-label {
  font-size: 11.5px;
  font-weight: 600;
  color: var(--ink-3);
  font-family: 'Nanum Gothic', monospace;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  white-space: nowrap;
}

.weakness-tag {
  font-size: 12px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 8px;
  background: rgba(0,132,255,0.12);
  color: #0084ff;
  font-family: 'Nanum Gothic', monospace;
}
.dark .weakness-tag { color: #4FB3FF; background: rgba(0,132,255,0.18); }

.rec-list { display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px; }

.rec-item {
  padding: 14px 16px;
  border: 1px solid var(--line);
  border-radius: 14px;
  cursor: pointer;
  transition: border-color .15s, transform .15s;
}
.rec-item:hover { border-color: #0084ff; transform: translateY(-1px); }

.matched-keywords {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  margin-top: 8px;
}

.matched-kw {
  font-size: 11px;
  padding: 2px 7px;
  border-radius: 5px;
  background: rgba(0,132,255,0.08);
  color: #0084ff;
  font-family: 'Nanum Gothic', monospace;
}
.dark .matched-kw { color: #4FB3FF; background: rgba(0,132,255,0.15); }

/* Bookmarks */
.bookmark-list { display: flex; flex-direction: column; gap: 10px; }

.bookmark-item {
  padding: 14px 16px;
  border: 1px solid var(--line);
  border-radius: 14px;
  cursor: pointer;
  transition: border-color .15s, transform .15s;
}
.bookmark-item:hover { border-color: #0084ff; transform: translateY(-1px); }

.bookmark-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.bookmark-category {
  font-size: 11.5px;
  font-weight: 600;
  color: #0084ff;
  background: rgba(0,132,255,0.10);
  padding: 2px 8px;
  border-radius: 5px;
  font-family: 'Nanum Gothic', monospace;
}

.bookmark-source { font-size: 11.5px; color: var(--ink-3); }

.bookmark-title {
  font-size: 14.5px;
  font-weight: 700;
  color: var(--ink);
  margin: 0 0 4px;
  line-height: 1.4;
}
.dark .bookmark-title { color: #f4f6fa; }

.bookmark-summary {
  font-size: 12.5px;
  color: var(--ink-3);
  margin: 0;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.5;
}

/* Wrong notes */
.wrong-list { display: flex; flex-direction: column; }

.wrong-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 0;
  border-bottom: 1px solid var(--line);
}
.wrong-item:last-child { border-bottom: none; }

.wrong-question {
  font-size: 14px;
  font-weight: 600;
  color: var(--ink);
  margin: 0 0 4px;
  line-height: 1.4;
}
.dark .wrong-question { color: #f4f6fa; }

.wrong-article { font-size: 12px; color: var(--ink-3); margin: 0; }

.wrong-badge {
  flex-shrink: 0;
  font-size: 11.5px;
  font-weight: 700;
  padding: 4px 10px;
  border-radius: 8px;
  background: var(--no-bg);
  border: 1px solid var(--no-border);
  color: var(--no);
}

/* Skeleton */
.skel {
  display: inline-block;
  background: linear-gradient(90deg, var(--line) 25%, rgba(0,0,0,0.04) 50%, var(--line) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.2s infinite;
  border-radius: 6px;
  height: 22px;
  width: 120px;
}
.dark .skel {
  background: linear-gradient(90deg, rgba(255,255,255,0.06) 25%, rgba(255,255,255,0.10) 50%, rgba(255,255,255,0.06) 75%);
  background-size: 200% 100%;
}
@keyframes shimmer { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }

.timeline-skel { display: flex; flex-direction: column; gap: 20px; }
.skel-row { display: flex; flex-direction: column; gap: 0; }

/* Empty */
.empty-state {
  text-align: center;
  padding: 48px 24px;
}
.empty-state.small { padding: 24px; }
.empty-icon { font-size: 36px; margin: 0 0 12px; }
.empty-title { font-size: 16px; font-weight: 700; color: var(--ink); margin: 0 0 6px; }
.empty-desc { font-size: 13.5px; color: var(--ink-3); margin: 0; }
.dark .empty-title { color: #f4f6fa; }

/* History section title (no section-head-row) */
@media (max-width: 768px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .history-title { font-size: 34px; }
  .rec-list { grid-template-columns: 1fr; }
}

@media (max-width: 480px) {
  .stats-grid { grid-template-columns: 1fr 1fr; gap: 10px; }
  .stat-card { padding: 14px; gap: 10px; }
  .stat-value { font-size: 24px; }
}
</style>
