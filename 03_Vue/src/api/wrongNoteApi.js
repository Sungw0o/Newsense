import axiosInstance from './axiosInstance'

export const wrongNoteApi = {
  /**
   * 오답노트 목록 조회
   * @param {Object} params { category, isResolved }
   */
  getWrongNotes(params = {}) {
    const query = new URLSearchParams()
    if (params.category) query.append('category', params.category)
    if (params.isResolved !== undefined) query.append('isResolved', params.isResolved)
    return axiosInstance.get(`/wrong-notes?${query.toString()}`)
  },

  /**
   * 오답 해결 상태 토글
   * @param {number|string} wrongNoteId
   */
  toggleResolve(wrongNoteId) {
    return axiosInstance.patch(`/wrong-notes/${wrongNoteId}/resolve`)
  },

  /**
   * 오답노트 항목 삭제
   * @param {number|string} wrongNoteId
   */
  deleteWrongNote(wrongNoteId) {
    return axiosInstance.delete(`/wrong-notes/${wrongNoteId}`)
  }
}

export default wrongNoteApi
