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
}

export default adminApi
