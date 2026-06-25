import axiosInstance from './axiosInstance'

const adminApi = {
  getStats() {
    return axiosInstance.get('/admin/stats')
  },
  getUsers(params = {}) {
    return axiosInstance.get('/admin/users', { params })
  },
  changeUserRole(userId) {
    return axiosInstance.patch(`/admin/users/${userId}/role`)
  },
  deletePost(postId) {
    return axiosInstance.delete(`/admin/posts/${postId}`)
  },
  getReports(params = {}) {
    return axiosInstance.get('/admin/reports', { params })
  },
  getArticles(params = {}) {
    return axiosInstance.get('/admin/articles', { params })
  },
  refreshArticleSummary(articleId) {
    return axiosInstance.patch(`/admin/articles/${articleId}/summary`)
  },
  deleteArticle(articleId) {
    return axiosInstance.delete(`/admin/articles/${articleId}`)
  },
  triggerCrawl(maxPerSource = 50) {
    return axiosInstance.post('/admin/crawl', null, { params: { maxPerSource } })
  },
  getInquiries(params = {}) {
    return axiosInstance.get('/admin/inquiries', { params })
  },
  resolveInquiry(inquiryId) {
    return axiosInstance.patch(`/admin/inquiries/${inquiryId}/resolve`)
  },
}

export default adminApi
