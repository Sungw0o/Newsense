import { defineStore } from 'pinia'

export const useThemeStore = defineStore('theme', {
  state: () => ({
    theme: 'dark' // Default is dark
  }),
  actions: {
    initTheme() {
      const savedTheme = localStorage.getItem('theme')
      if (savedTheme) {
        this.theme = savedTheme
      } else {
        // System preference default check
        const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
        this.theme = prefersDark ? 'dark' : 'light'
      }
      this.applyThemeToDOM()
    },
    toggleTheme() {
      this.theme = this.theme === 'light' ? 'dark' : 'light'
      localStorage.setItem('theme', this.theme)
      this.applyThemeToDOM()
    },
    applyThemeToDOM() {
      if (this.theme === 'dark') {
        document.documentElement.classList.add('dark')
      } else {
        document.documentElement.classList.remove('dark')
      }
    }
  }
})

export default useThemeStore
