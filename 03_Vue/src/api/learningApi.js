import axiosInstance from './axiosInstance'

export const learningApi = {
  /**
   * 날짜별 학습 이력 조회
   * @param {Object} params { startDate: 'YYYY-MM-DD', endDate: 'YYYY-MM-DD' }
   */
  getHistory(params = {}) {
    const query = new URLSearchParams()
    if (params.startDate) query.append('startDate', params.startDate)
    if (params.endDate) query.append('endDate', params.endDate)
    return axiosInstance.get(`/learning/history?${query.toString()}`)
  },

  /**
   * 종합 학습 통계 조회 (총 읽은 기사, 퀴즈 정답률, 연속 학습일 등)
   */
  getStats() {
    return axiosInstance.get('/learning/stats')
  },

  /**
   * 북마크된 기사 목록 조회
   */
  getBookmarks() {
    return axiosInstance.get('/learning/bookmarks')
  },

  getWeakness() {
    return axiosInstance.get('/learning/weakness')
  },

  getWeaknessSummary() {
    return axiosInstance.get('/learning/weakness-summary')
  },

  deleteHistory(historyId) {
    return axiosInstance.delete(`/learning/history/${historyId}`)
  }
}

export default learningApi
