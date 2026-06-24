<script setup>
import { computed } from 'vue'

const props = defineProps({
  article: { type: Object, required: true },
  featured: { type: Boolean, default: false }
})

const emit = defineEmits(['click'])

const categoryTag = computed(() => {
  const map = {
    '금융': 'eco', '경제': 'eco', '부동산': 'estate',
    '주식': 'stock', '환율': 'fx', '거시경제': 'macro', '정책': 'policy',
  }
  return map[props.article.category] ?? 'default'
})

const difficultyBars = computed(() => {
  const levels = { '초급': 1, '중급': 2, '고급': 3 }
  const count = levels[props.article.difficulty] ?? 1
  return [1, 2, 3].map(i => i <= count)
})

const readTime = computed(() => {
  return props.article.estimatedMinutes ?? props.article.readTime ?? 3
})

const publishedDate = computed(() => {
  const d = props.article.publishedAt ?? props.article.createdAt
  if (!d) return ''
  return new Date(d).toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' })
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
    <!-- Featured ribbon -->
    <span v-if="featured" class="ribbon">
      <span class="r-dot"></span> TODAY
    </span>

    <!-- Card top -->
    <div class="card-top">
      <span class="tag" :class="categoryTag">{{ article.category }}</span>
      <span class="meta-time">{{ readTime }}분 읽기</span>
    </div>

    <!-- Title -->
    <h3>{{ article.title }}</h3>

    <!-- Summary -->
    <p class="summary">{{ article.summary ?? article.preview }}</p>

    <!-- Card footer -->
    <div class="card-foot">
      <span class="study">
        <svg viewBox="0 0 14 14" xmlns="http://www.w3.org/2000/svg">
          <circle cx="7" cy="7" r="5.5"/>
          <path d="M7 4v3.5l2 1.5" stroke-linecap="round"/>
        </svg>
        난이도
        <span class="difficulty">
          <span v-for="(on, i) in difficultyBars" :key="i" class="diff-bar" :class="{ on }"></span>
        </span>
        <b>{{ article.difficulty }}</b>
      </span>

      <span class="quiz-info">
        <span class="q-dot"></span>
        퀴즈 {{ article.quizCount ?? 3 }}문항
        <span style="opacity:0.35; margin:0 2px;">·</span>
        <span>{{ publishedDate }}</span>
      </span>
    </div>
  </article>
</template>

<style scoped>
.card {
  position: relative;
  display: flex;
  flex-direction: column;
  padding: 22px 22px 18px;
  background: #fff;
  border: 1px solid rgba(0, 0, 0, 0.07);
  border-radius: 20px;
  cursor: pointer;
  text-decoration: none;
  color: inherit;
  transition: transform .25s ease, box-shadow .25s ease, border-color .25s ease;
  box-shadow: 0 1px 0 rgba(255, 255, 255, 0.6) inset;
  min-height: 300px;
}
.card:hover {
  transform: translateY(-3px);
  border-color: rgba(0, 132, 255, 0.25);
  box-shadow: 0 24px 50px -22px rgba(0, 80, 200, 0.25);
}

.dark .card {
  background: rgba(20, 24, 34, 0.65);
  border-color: rgba(255, 255, 255, 0.10);
  color: #f4f6fa;
  box-shadow: 0 1px 0 rgba(255, 255, 255, 0.04) inset;
}
.dark .card:hover {
  border-color: rgba(0, 132, 255, 0.55);
  box-shadow: 0 24px 50px -22px rgba(0, 80, 200, 0.45);
}

.card.featured {
  grid-column: span 2;
  background:
    radial-gradient(120% 140% at 0% 0%, rgba(0, 132, 255, 0.08) 0%, rgba(0, 132, 255, 0) 60%),
    #fff;
  min-height: 340px;
}
.dark .card.featured {
  background:
    radial-gradient(120% 140% at 0% 0%, rgba(0, 132, 255, 0.20) 0%, rgba(0, 132, 255, 0) 60%),
    rgba(20, 24, 34, 0.70);
}
.card.featured h3 { font-size: 26px; max-width: 88%; }
.card.featured .summary { max-width: 84%; }

/* Ribbon on featured */
.ribbon {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 9px;
  background: var(--ink, #0a0d12);
  color: #fff;
  border-radius: 6px;
  font-family: 'Nanum Gothic', monospace;
  font-size: 10.5px;
  letter-spacing: 0.8px;
  text-transform: uppercase;
  font-weight: 500;
  position: absolute;
  top: 18px; right: 22px;
  z-index: 3;
}
.r-dot { width: 5px; height: 5px; border-radius: 50%; background: #FF801E; }

/* Card top */
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
  gap: 6px;
  padding: 4px 10px;
  font-size: 11.5px;
  font-weight: 600;
  letter-spacing: 0.3px;
  border-radius: 999px;
}
.tag::before {
  content: "";
  width: 6px; height: 6px;
  border-radius: 50%;
  background: currentColor;
  opacity: 0.7;
}
.tag.eco    { background: #E8F2FF; color: #0a4a99; }
.tag.macro  { background: #F0EBFF; color: #5a3bbf; }
.tag.finance { background: #E8F2FF; color: #0a4a99; }
.tag.estate { background: #FFF1E8; color: #a14a14; }
.tag.stock  { background: #FFF1E8; color: #a14a14; }
.tag.fx     { background: #E8FBF1; color: #1f7a3a; }
.tag.policy { background: #E8FBF1; color: #1f7a3a; }
.tag.default { background: #E8FBF1; color: #1f7a3a; }

.dark .tag.eco    { background: rgba(0,132,255,0.18); color: #9BCBFF; }
.dark .tag.macro  { background: rgba(140,100,255,0.20); color: #C9B6FF; }
.dark .tag.estate,
.dark .tag.stock  { background: rgba(255,128,30,0.18); color: #FFC089; }
.dark .tag.fx,
.dark .tag.policy,
.dark .tag.default { background: rgba(58,208,123,0.18); color: #95EAB8; }
.dark .tag.finance { background: rgba(0,132,255,0.18); color: #9BCBFF; }

.meta-time {
  font-family: 'Nanum Gothic', monospace;
  font-size: 11px;
  color: var(--ink-3, #8a93a3);
  letter-spacing: 0.3px;
  flex-shrink: 0;
}

/* Title */
h3 {
  font-family: 'Fustat', sans-serif;
  font-weight: 700;
  font-size: 19px;
  line-height: 1.3;
  letter-spacing: -0.4px;
  margin: 0 0 12px;
  color: var(--ink, #0a0d12);
}

/* Summary */
.summary {
  font-size: 13.5px;
  line-height: 1.55;
  color: var(--ink-2, #4a5161);
  margin: 0 0 18px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.dark h3 { color: #f4f6fa; }
.dark .summary { color: #a4adbf; }

/* Card footer */
.card-foot {
  margin-top: auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 14px;
  border-top: 1px dashed rgba(0, 0, 0, 0.09);
}
.dark .card-foot { border-color: rgba(255, 255, 255, 0.10); }

.study {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12.5px;
  color: var(--ink-2, #4a5161);
  font-weight: 500;
}
.dark .study { color: #a4adbf; }

.study svg {
  width: 14px; height: 14px;
  stroke: #0084ff;
  fill: none;
  stroke-width: 2;
  flex-shrink: 0;
}

.difficulty {
  display: inline-flex;
  gap: 2px;
  align-items: center;
}
.diff-bar {
  width: 4px; height: 10px;
  border-radius: 2px;
  background: rgba(0, 0, 0, 0.12);
}
.dark .diff-bar { background: rgba(255,255,255,0.15); }
.diff-bar.on { background: #0084ff; }

.study b {
  font-family: 'Nanum Gothic', monospace;
  font-weight: 500;
  color: var(--ink, #0a0d12);
  font-size: 12px;
}
.dark .study b { color: #f4f6fa; }

.quiz-info {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 11.5px;
  color: var(--ink-3, #8a93a3);
  font-family: 'Nanum Gothic', monospace;
}
.q-dot {
  width: 6px; height: 6px;
  border-radius: 50%;
  background: #0084ff;
}

@media (max-width: 640px) {
  .card.featured { grid-column: span 1; }
  .card.featured h3, .card.featured .summary { max-width: 100%; font-size: 18px; }
  .ribbon { top: 14px; right: 14px; }
}
</style>
