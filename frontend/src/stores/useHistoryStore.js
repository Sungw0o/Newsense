import { defineStore } from 'pinia'
import { learningApi } from '../api/learningApi'

export const useHistoryStore = defineStore('history', {
  state: () => ({
    /** 학습 이력 목록 (날짜별 그룹) */
    history: [],
    /** 종합 통계 */
    stats: null,
    isLoading: false,
    error: null
  }),
  actions: {
    /**
     * 날짜 범위 학습 이력 로드
     */
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

    /**
     * 종합 학습 통계 로드
     */
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
    }
  }
})

export default useHistoryStore
