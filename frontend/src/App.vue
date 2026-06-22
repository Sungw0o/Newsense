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
    <!-- Liquid Background Elements -->
    <div class="fixed inset-0 z-0 overflow-hidden pointer-events-none">
      <div class="liquid-orb liquid-orb-primary -top-40 -left-40"></div>
      <div class="liquid-orb liquid-orb-secondary top-1/3 -right-60"></div>
      <div class="liquid-orb liquid-orb-accent -bottom-40 left-1/4"></div>
    </div>
    
    <div class="relative z-10 min-h-screen flex flex-col w-full">
      <AppLayout />
    </div>

    <!-- Theme Switcher floating component -->
    <ThemeSwitcher />
  </div>
</template>
