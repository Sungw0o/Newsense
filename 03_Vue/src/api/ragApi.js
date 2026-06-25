import axiosInstance from './axiosInstance'

const ragApi = {
  search(query, limit = 5) {
    return axiosInstance.get('/rag/search', { params: { query, limit } })
  },

  getRecommendations(limit = 5) {
    return axiosInstance.get('/rag/recommendations', { params: { limit } })
  },
}

export default ragApi
