<script setup>
import { computed } from 'vue'
import { useThemeStore } from '../../stores/useThemeStore'

const themeStore = useThemeStore()
const isDark = computed(() => themeStore.theme === 'dark')

const toggle = () => {
  themeStore.toggleTheme()
}
</script>

<template>
  <button 
    @click="toggle"
    class="fixed bottom-6 right-6 z-50 p-3.5 rounded-full glass-panel shadow-glass-glow hover:shadow-glass-glow-hover hover:scale-110 active:scale-95 transition-all duration-300 border border-primary-500/20 group"
    aria-label="Toggle Theme"
  >
    <!-- Sun Icon (Visible when Dark Mode is active) -->
    <svg 
      v-if="isDark"
      class="w-6 h-6 text-amber-400 group-hover:rotate-45 transition-transform duration-500" 
      fill="none" 
      stroke="currentColor" 
      stroke-width="2" 
      viewBox="0 0 24 24"
    >
      <circle cx="12" cy="12" r="5"></circle>
      <path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42"></path>
    </svg>

    <!-- Moon Icon (Visible when Light Mode is active) -->
    <svg 
      v-else
      class="w-6 h-6 text-slate-700 group-hover:-rotate-12 transition-transform duration-500" 
      fill="none" 
      stroke="currentColor" 
      stroke-width="2" 
      viewBox="0 0 24 24"
    >
      <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path>
    </svg>
  </button>
</template>
