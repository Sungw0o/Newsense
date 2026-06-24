import { defineStore } from 'pinia'
import { learningApi } from '../api/learningApi'
import ragApi from '../api/ragApi'

export const useHistoryStore = defineStore('history', {
  state: () => ({
    history: null,
    stats: null,
    bookmarks: [],
    recommendations: null,
    isLoading: false,
    error: null
  }),
  actions: {
    async fetchHistory(params = {}) {
      this.isLoading = true
      this.error = null
      try {
        const response = await learningApi.getHistory(params)
        this.history = response.data || response
      } catch (err) {
        this.error = err.message || '학습 이력을 불러오지 못했습니다.'
        throw err
      } finally {
        this.isLoading = false
      }
    },

    async fetchStats() {
      this.isLoading = true
      this.error = null
      try {
        const response = await learningApi.getStats()
        this.stats = response.data || response
      } catch (err) {
        this.error = err.message || '학습 통계를 불러오지 못했습니다.'
        throw err
      } finally {
        this.isLoading = false
      }
    },

    async fetchBookmarks() {
      try {
        const response = await learningApi.getBookmarks()
        this.bookmarks = response.data || response || []
      } catch {
        this.bookmarks = []
      }
    },

    async fetchRecommendations() {
      try {
        const response = await ragApi.getRecommendations(6)
        this.recommendations = response.data || response
      } catch {
        this.recommendations = null
      }
    }
  }
})

export default useHistoryStore
