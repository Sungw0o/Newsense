import { defineStore } from 'pinia'

export const useThemeStore = defineStore('theme', {
  state: () => ({
    theme: 'dark' // Default is dark
  }),
  actions: {
    initTheme() {
      const savedTheme = localStorage.getItem('theme')
      this.theme = savedTheme ?? 'dark'
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
