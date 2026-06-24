<script setup>
import { onMounted } from 'vue'
import AppHeader from './AppHeader.vue'
import AppFooter from './AppFooter.vue'
import StreakWidget from './StreakWidget.vue'
import IndicatorWidget from './IndicatorWidget.vue'
import { useHistoryStore } from '../../stores/useHistoryStore'
import { useUserStore } from '../../stores/useUserStore'

const userStore = useUserStore()
const historyStore = useHistoryStore()

onMounted(async () => {
  if (userStore.isAuthenticated) {
    historyStore.fetchStats().catch(() => {})
  }
})
</script>

<template>
  <div class="min-h-screen flex flex-col w-full">
    <AppHeader />

    <div class="layout-body">
      <aside class="sidebar-left">
        <StreakWidget v-if="userStore.isAuthenticated" />
      </aside>

      <main class="layout-main">
        <router-view />
      </main>

      <aside class="sidebar-right">
        <IndicatorWidget />
      </aside>
    </div>

    <AppFooter />
  </div>
</template>

<style scoped>
.layout-body {
  flex: 1;
  display: grid;
  grid-template-columns: 220px 1fr 220px;
  gap: 24px;
  max-width: 1400px;
  margin: 0 auto;
  padding: 40px 16px 48px;
  width: 100%;
  align-items: start;
  position: relative;
  z-index: 10;
}

.sidebar-left,
.sidebar-right {
  display: block;
}

.layout-main {
  min-width: 0;
}

@media (max-width: 1200px) {
  .layout-body {
    grid-template-columns: 1fr;
  }
  .sidebar-left,
  .sidebar-right {
    display: none;
  }
}
</style>
