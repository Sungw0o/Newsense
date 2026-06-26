<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import ragApi from '../../api/ragApi'

const router = useRouter()

const recommendations = ref([])
const termsLoading = ref(true)

async function loadRecommendations() {
  try {
    const res = await ragApi.getRecommendations(4)
    const payload = res?.data ?? res ?? {}
    recommendations.value = Array.isArray(payload.recommendations)
      ? payload.recommendations.slice(0, 3)
      : []
  } catch {
    recommendations.value = []
  } finally {
    termsLoading.value = false
  }
}

function getArticle(item) {
  return item?.article ?? item
}

function openArticle(item) {
  const article = getArticle(item)
  const id = article?.articleId ?? article?.id
  if (id) router.push(`/articles/${id}`)
}

onMounted(() => {
  loadRecommendations()
})
</script>

<template>
  <aside class="side-panel">
    <section class="panel-section recommend-section" v-if="!termsLoading && recommendations.length">
      <div class="section-head">
        <h3 class="section-title">AI 맞춤 기사</h3>
      </div>
      <button
        v-for="item in recommendations"
        :key="getArticle(item)?.articleId ?? getArticle(item)?.id ?? getArticle(item)?.title"
        class="recommend-card"
        @click="openArticle(item)"
      >
        <span class="recommend-title">{{ getArticle(item)?.title }}</span>
        <span class="recommend-reason">{{ item?.reason ?? getArticle(item)?.summary ?? '취약 개념과 연결된 기사입니다.' }}</span>
      </button>
    </section>
  </aside>
</template>

<style scoped>
.side-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;
}

.panel-section {
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 14px;
  padding: 18px 18px 16px;
  backdrop-filter: blur(16px);
}

.dark .panel-section {
  background: rgba(20, 24, 34, 0.68);
  border-color: rgba(255, 255, 255, 0.1);
}

.section-head {
  display: flex;
  align-items: center;
  margin-bottom: 14px;
}

.section-title {
  margin: 0;
  font-size: 13.5px;
  font-weight: 800;
  color: var(--ink, #0a0d12);
  flex: 1;
}

.dark .section-title {
  color: #f4f6fa;
}

.recommend-section {
  padding-bottom: 14px;
}

.recommend-card {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 11px 10px;
  margin: 0 0 8px;
  border: 1px solid rgba(0, 132, 255, 0.12);
  border-radius: 10px;
  background: rgba(0, 132, 255, 0.045);
  text-align: left;
  cursor: pointer;
  transition: background .12s, border-color .12s;
}

.recommend-card:last-child {
  margin-bottom: 0;
}

.recommend-card:hover {
  background: rgba(0, 132, 255, 0.08);
  border-color: rgba(0, 132, 255, 0.24);
}

.dark .recommend-card {
  background: rgba(0, 132, 255, 0.09);
  border-color: rgba(79, 179, 255, 0.16);
}

.recommend-title {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  font-size: 13px;
  font-weight: 800;
  line-height: 1.35;
  color: var(--ink, #0a0d12);
}

.dark .recommend-title {
  color: #f4f6fa;
}

.recommend-reason {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  font-size: 11.5px;
  line-height: 1.45;
  color: var(--ink-3, #8a93a3);
}

@media (max-width: 1024px) {
  .side-panel {
    display: grid;
    grid-template-columns: 1fr;
  }
}
</style>
