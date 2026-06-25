<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import ragApi from '../../api/ragApi'

const router = useRouter()

const terms = ref([])
const termsLoading = ref(true)

async function loadTerms() {
  try {
    const res = await ragApi.getRecommendations(4)
    const payload = res?.data ?? res ?? {}
    terms.value = Array.isArray(payload.weaknessTerms) ? payload.weaknessTerms.slice(0, 4) : []
  } catch {
    terms.value = []
  } finally {
    termsLoading.value = false
  }
}

onMounted(() => {
  loadTerms()
})
</script>

<template>
  <aside class="side-panel">
    <section class="panel-section" v-if="!termsLoading && terms.length">
      <div class="section-head">
        <span class="section-icon">💡</span>
        <h3 class="section-title">오늘의 경제 용어</h3>
      </div>
      <ul class="term-list">
        <li
          v-for="term in terms"
          :key="term.name ?? term.term ?? term"
          class="term-row"
          @click="router.push('/')"
        >
          <span class="term-name">{{ term.name ?? term.term ?? term }}</span>
          <span class="term-arrow">→</span>
        </li>
      </ul>
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
  gap: 8px;
  margin-bottom: 14px;
}

.section-icon {
  font-size: 16px;
  line-height: 1;
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

.term-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.term-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 9px 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: background .12s;
}

.term-row:hover {
  background: rgba(0, 132, 255, 0.06);
}

.dark .term-row:hover {
  background: rgba(0, 132, 255, 0.12);
}

.term-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--ink, #0a0d12);
}

.dark .term-name {
  color: #e2e8f0;
}

.term-arrow {
  font-size: 12px;
  color: var(--ink-3);
}

@media (max-width: 1024px) {
  .side-panel {
    display: grid;
    grid-template-columns: 1fr;
  }
}
</style>
