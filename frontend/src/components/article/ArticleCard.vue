<script setup>
import { computed } from 'vue'

const props = defineProps({
  article: { type: Object, required: true },
  featured: { type: Boolean, default: false },
})

const emit = defineEmits(['click'])

// 카테고리 → 태그 클래스
const categoryTag = computed(() => {
  const map = {
    '거시경제': 'macro',
    '금융': 'finance',
    '금융/투자': 'finance',
    '부동산': 'realestate',
    '주식': 'stock',
    '환율': 'exchange',
    '정책/제도': 'policy',
    '기업/산업': 'industry',
    '글로벌경제': 'global',
    '통화정책': 'monetary',
  }
  return map[props.article.category] ?? 'default'
})

// 카테고리별 placeholder 아이콘 + 그라디언트
const categoryPlaceholder = computed(() => {
  const map = {
    '거시경제':  { emoji: '📈', from: '#e0f2fe', to: '#bae6fd' },
    '금융':      { emoji: '💰', from: '#e8f2ff', to: '#bfdbfe' },
    '금융/투자': { emoji: '💰', from: '#e8f2ff', to: '#bfdbfe' },
    '부동산':    { emoji: '🏠', from: '#fef3c7', to: '#fde68a' },
    '주식':      { emoji: '📊', from: '#fff4e5', to: '#fed7aa' },
    '환율':      { emoji: '💱', from: '#ecfdf5', to: '#bbf7d0' },
    '정책/제도': { emoji: '⚖️', from: '#f0ebff', to: '#ddd6fe' },
    '기업/산업': { emoji: '🏭', from: '#fef2f2', to: '#fecaca' },
    '글로벌경제':{ emoji: '🌐', from: '#e8fbf1', to: '#bbf7d0' },
    '통화정책':  { emoji: '🏦', from: '#fdf4ff', to: '#f5d0fe' },
  }
  return map[props.article.category] ?? { emoji: '📰', from: '#f1f5f9', to: '#e2e8f0' }
})

const readTime = computed(() => props.article.estimatedMinutes ?? props.article.readTime ?? 3)

const publishedDate = computed(() => {
  const d = props.article.publishedAt ?? props.article.createdAt
  if (!d) return ''
  const date = new Date(d)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}.${month}.${day}`
})

const viewCount = computed(() => {
  const v = props.article.viewCount ?? 0
  return v >= 1000 ? `${(v / 1000).toFixed(1)}k` : String(v)
})
</script>

<template>
  <article
    class="card"
    :class="{ featured }"
    @click="emit('click', article.articleId)"
    role="button"
    tabindex="0"
    @keydown.enter="emit('click', article.articleId)"
  >
    <!-- 카테고리 thumbnail placeholder -->
    <div
      class="card-thumb"
      :style="`background: linear-gradient(135deg, ${categoryPlaceholder.emoji ? categoryPlaceholder.from : '#f1f5f9'} 0%, ${categoryPlaceholder.to} 100%)`"
    >
      <span class="thumb-emoji">{{ categoryPlaceholder.emoji }}</span>
      <span v-if="article.difficulty" class="thumb-difficulty">{{ article.difficulty }}</span>
    </div>

    <div class="card-top">
      <span class="tag" :class="categoryTag">{{ article.category || '경제' }}</span>
      <span class="meta-time">{{ readTime }}분 읽기</span>
    </div>

    <h3>{{ article.title }}</h3>

    <p class="summary">{{ article.summary ?? article.preview }}</p>

    <div class="card-foot">
      <span class="pub-date">{{ publishedDate }}</span>
      <span class="view-count">조회 {{ viewCount }}</span>
    </div>
  </article>
</template>

<style scoped>
.card {
  position: relative;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 0; /* thumbnail이 최상단 차지, 내부 요소에 개별 padding */
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 12px;
  cursor: pointer;
  text-decoration: none;
  color: inherit;
  transition: transform .2s ease, box-shadow .2s ease, border-color .2s ease;
  min-height: 248px;
}

/* 카테고리 placeholder 썸네일 */
.card-thumb {
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 18px;
  flex-shrink: 0;
}

.thumb-emoji {
  font-size: 28px;
  line-height: 1;
  user-select: none;
}

.thumb-difficulty {
  font-size: 11px;
  font-weight: 800;
  padding: 3px 9px;
  border-radius: 999px;
  background: rgba(255,255,255,0.65);
  color: #4a5161;
  backdrop-filter: blur(4px);
}

.card .card-top,
.card h3,
.card .summary,
.card .card-foot {
  padding-left: 18px;
  padding-right: 18px;
}

.card .card-top {
  padding-top: 14px;
  margin-bottom: 10px;
}

.card .summary {
  padding-bottom: 0;
}

.card .card-foot {
  padding-bottom: 16px;
}

.dark .card-thumb {
  filter: brightness(0.75) saturate(0.8);
}

.dark .thumb-difficulty {
  background: rgba(0,0,0,0.35);
  color: #c8d0e0;
}

.card:hover {
  transform: translateY(-2px);
  border-color: rgba(0, 132, 255, 0.28);
  box-shadow: 0 18px 40px -26px rgba(0, 80, 200, 0.36);
}

.dark .card {
  background: rgba(20, 24, 34, 0.72);
  border-color: rgba(255, 255, 255, 0.1);
  color: #f4f6fa;
}

.card.featured {
  grid-column: span 2;
  min-height: 292px;
  background: linear-gradient(135deg, rgba(232, 242, 255, 0.98), rgba(255, 255, 255, 0.94));
}

.dark .card.featured {
  background: linear-gradient(135deg, rgba(10, 74, 153, 0.32), rgba(20, 24, 34, 0.78));
}

.card.featured h3 {
  max-width: 90%;
  font-size: 27px;
}

.card.featured .summary {
  max-width: 86%;
  -webkit-line-clamp: 4;
}

.card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  gap: 10px;
}

.tag {
  display: inline-flex;
  align-items: center;
  padding: 5px 10px;
  font-size: 12px;
  font-weight: 800;
  border-radius: 999px;
}

.tag.finance { background: #E8F2FF; color: #0a4a99; }
.tag.macro { background: #F0EBFF; color: #5a3bbf; }
.tag.stock { background: #FFF4E5; color: #9a4a00; }
.tag.global { background: #E8FBF1; color: #1f7a3a; }
.tag.policy,
.tag.default { background: #EEF2F7; color: #405067; }

.dark .tag.finance { background: rgba(0, 132, 255, 0.18); color: #9BCBFF; }
.dark .tag.macro { background: rgba(140, 100, 255, 0.2); color: #C9B6FF; }
.dark .tag.stock { background: rgba(255, 128, 30, 0.18); color: #FFC089; }
.dark .tag.global { background: rgba(58, 208, 123, 0.18); color: #95EAB8; }
.dark .tag.policy,
.dark .tag.default { background: rgba(148, 163, 184, 0.16); color: #CBD5E1; }

.meta-time {
  font-size: 12px;
  color: var(--ink-3, #8a93a3);
  flex-shrink: 0;
}

h3 {
  font-family: 'Fustat', 'Pretendard', sans-serif;
  font-weight: 800;
  font-size: 19px;
  line-height: 1.36;
  margin: 0 0 12px;
  color: var(--ink, #0a0d12);
}

.summary {
  font-size: 14px;
  line-height: 1.62;
  color: var(--ink-2, #4a5161);
  margin: 0 0 18px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.dark h3 {
  color: #f4f6fa;
}

.dark .summary {
  color: #a4adbf;
}

.card-foot {
  margin-top: auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-top: 14px;
  border-top: 1px solid rgba(0, 0, 0, 0.08);
  color: var(--ink-3, #8a93a3);
  font-size: 12px;
}

.dark .card-foot {