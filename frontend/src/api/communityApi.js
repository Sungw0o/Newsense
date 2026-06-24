import axiosInstance from './axiosInstance'

const communityApi = {
  getPosts(params = {}) {
    return axiosInstance.get('/posts', { params })
  },
  getNotices() {
    return axiosInstance.get('/posts/notices')
  },
  getPost(postId) {
    return axiosInstance.get(`/posts/${postId}`)
  },
  createPost(data) {
    return axiosInstance.post('/posts', data)
  },
  deletePost(postId) {
    return axiosInstance.delete(`/posts/${postId}`)
  },
  toggleReaction(postId, type) {
    const endpoint = type.toLowerCase() === 'like' ? 'like' : 'dislike'
    return axiosInstance.post(`/posts/${postId}/${endpoint}`)
  },
  getComments(postId) {
    return axiosInstance.get(`/posts/${postId}/comments`)
  },
  createComment(postId, content) {
    return axiosInstance.post(`/posts/${postId}/comments`, { content })
  },
  deleteComment(commentId) {
    return axiosInstance.delete(`/comments/${commentId}`)
  },
}

export default communityApi
