<script setup>
import { onMounted } from 'vue'
import AppLayout from './components/common/AppLayout.vue'
import ThemeSwitcher from './components/common/ThemeSwitcher.vue'
import { useThemeStore } from './stores/useThemeStore'

const themeStore = useThemeStore()

onMounted(() => {
  themeStore.initTheme()
})
</script>

<template>
  <div class="relative overflow-x-hidden min-h-screen w-full">
    <!-- Static glow orbs -->
    <div class="glow-orb glow-orb-1"></div>
    <div class="glow-orb glow-orb-2"></div>
    <div class="glow-orb glow-orb-3"></div>

    <div class="relative z-10 min-h-screen flex flex-col w-full">
      <AppLayout />
    </div>

    <ThemeSwitcher />
  </div>
</template>
