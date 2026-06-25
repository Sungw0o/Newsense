import axiosInstance from './axiosInstance'

export const articleApi = {
  getArticles(params = {}) {
    const queryParams = new URLSearchParams()
    if (params.category) queryParams.append('category', params.category)
    if (params.difficulty) queryParams.append('difficulty', params.difficulty)
    if (params.keyword) queryParams.append('keyword', params.keyword)
    if (params.sort) queryParams.append('sort', params.sort)
    if (params.page !== undefined) queryParams.append('page', params.page)
    if (params.size !== undefined) queryParams.append('size', params.size)
    return axiosInstance.get(`/articles?${queryParams.toString()}`)
  },

  getArticleDetail(articleId) {
    return axiosInstance.get(`/articles/${articleId}`)
  },

  getArticleTerms(articleId) {
    return axiosInstance.get(`/articles/${articleId}/terms`)
  },

  markAsRead(articleId) {
    return axiosInstance.post(`/articles/${articleId}/read`)
  },

  toggleBookmark(articleId) {
    return axiosInstance.post(`/articles/${articleId}/bookmark`)
  }
}

export default articleApi
