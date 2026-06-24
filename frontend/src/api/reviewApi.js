import axiosInstance from './axiosInstance'

export const reviewApi = {
  /**
   * 특정 기사에 대한 내 리뷰 조회
   * @param {number|string} articleId
   */
  getReview(articleId) {
    return axiosInstance.get(`/articles/${articleId}/review`)
  },

  /**
   * 리뷰 등록
   * @param {Object} payload { articleId, summary, learned, difficultTerms: string[] }
   */
  createReview(payload) {
    return axiosInstance.post('/reviews', payload)
  },

  /**
   * 리뷰 수정
   * @param {number|string} reviewId
   * @param {Object} payload { summary, learned, difficultTerms: string[] }
   */
  updateReview(reviewId, payload) {
    return axiosInstance.put(`/reviews/${reviewId}`, payload)
  },

  /**
   * 리뷰 삭제
   * @param {number|string} reviewId
   */
  deleteReview(reviewId) {
    return axiosInstance.delete(`/reviews/${reviewId}`)
  }
}

export default reviewApi
