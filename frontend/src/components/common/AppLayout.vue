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
    <AppHeader v-if="userStore.isAuthenticated" />

    <div class="layout-body">
      <main class="layout-main">
        <router-view />
      </main>

      <aside class="sidebar-right">
        <StreakWidget v-if="userStore.isAuthenticated" />
        <IndicatorWidget v-if="userStore.isAuthenticated" />
      </aside>
    </div>

    <AppFooter />
  </div>
</template>

<style scoped>
.layout-body {
  flex: 1;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 220px;
  gap: 28px;
  max-width: 1600px;
  margin: 0 auto;
  padding: 36px clamp(18px, 3vw, 44px) 48px;
  width: 100%;
  align-items: start;
  position: relative;
  z-index: 10;
}

.sidebar-right {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.layout-main {
  min-width: 0;
}

@media (max-width: 1200px) {
  .layout-body {
    grid-template-columns: 1fr;
  }
  .sidebar-right {
    display: none;
