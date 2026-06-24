import axiosInstance from './axiosInstance'

const communityApi = {
  getPosts(params = {}) {
    return axiosInstance.get('/community/posts', { params })
  },
  getPost(postId) {
    return axiosInstance.get(`/community/posts/${postId}`)
  },
  createPost(data) {
    return axiosInstance.post('/community/posts', data)
  },
  deletePost(postId) {
    return axiosInstance.delete(`/community/posts/${postId}`)
  },
  toggleReaction(postId, type) {
    return axiosInstance.post(`/community/posts/${postId}/reactions`, { type })
  },
  getComments(postId) {
    return axiosInstance.get(`/community/posts/${postId}/comments`)
  },
  createComment(postId, content) {
    return axiosInstance.post(`/community/posts/${postId}/comments`, { content })
  },
  deleteComment(postId, commentId) {
    return axiosInstance.delete(`/community/posts/${postId}/comments/${commentId}`)
  },
}

export default communityApi
