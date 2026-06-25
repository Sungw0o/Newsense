import axiosInstance from './axiosInstance'

export const quizApi = {
  /**
   * 기사 기반 퀴즈 목록 조회 (정답 제외)
   * @param {number|string} articleId
   */
  getQuizList(articleId) {
    return axiosInstance.get(`/articles/${articleId}/quiz`)
  },

  /**
   * 퀴즈 답변 제출 및 채점
   * @param {number|string} quizId
   * @param {Object} payload { answer: string }
   */
  submitAnswer(quizId, payload) {
    return axiosInstance.post(`/quiz/${quizId}/answer`, payload)
  },

  /**
   * 퀴즈 결과 조회
   * @param {number|string} quizId
   */
  getQuizResult(quizId) {
    return axiosInstance.get(`/quiz/${quizId}/result`)
  }
}

export default quizApi
