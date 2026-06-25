import axiosInstance from './axiosInstance'

export const articleApi = {
  /**
   * 기사 목록 조회 API (필터 및 페이징)
   * @param {Object} params { category, difficulty, page, size }
   */
  getArticles(params = {}) {
    const queryParams = new URLSearchParams()
    if (params.category) queryParams.append('category', params.category)
    if (params.difficulty) queryParams.append('difficulty', params.difficulty)
    if (params.keyword) queryParams.append('keyword', params.keyword)
    if (params.page !== undefined) queryParams.append('page', params.page)
    if (params.size !== undefined) queryParams.append('size', params.size)
    return axiosInstance.get(`/articles?${queryParams.toString()}`)
  },

  /**
   * 특정 기사 상세조회 API
   * @param {number|string} articleId
   */
  getArticleDetail(articleId) {
    return axiosInstance.get(`/articles/${articleId}`)
  },

  /**
   * 기사용 경제 용어 사전 조회 API
   * @param {number|string} articleId
   */
  getArticleTerms(articleId) {
    return axiosInstance.get(`/articles/${articleId}/terms`)
  },

  /**
   * 기사 읽음 완료 처리 API
   * @param {number|string} articleId
   */
  markAsRead(articleId) {
    return axiosInstance.post(`/articles/${articleId}/read`)
  },

  /**
   * 기사 북마크 토글 API
   * @param {number|string} articleId
   */
  toggleBookmark(articleId) {
    return axiosInstance.post(`/articles/${articleId}/bookmark`)
  }
}

export default articleApi
