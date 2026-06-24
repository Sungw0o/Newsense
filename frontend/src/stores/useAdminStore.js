import { defineStore } from 'pinia'
import adminApi from '../api/adminApi'

export const useAdminStore = defineStore('admin', {
  state: () => ({
    stats: null,
    users: [],
    isLoadingStats: false,
    isLoadingUsers: false,
    error: null,
  }),

  actions: {
    async fetchStats() {
      this.isLoadingStats = true
      this.error = null
      try {
        const res = await adminApi.getStats()
        this.stats = res?.data ?? res
      } catch (e) {
        this.error = e?.response?.data?.message ?? '통계 조회에 실패했습니다.'
      } finally {
        this.isLoadingStats = false
      }
    },

    async fetchUsers(params = {}) {
      this.isLoadingUsers = true
      this.error = null
      try {
        const res = await adminApi.getUsers(params)
        const data = res?.data ?? res
        this.users = Array.isArray(data) ? data : (data?.content ?? [])
      } catch (e) {
        this.error = e?.response?.data?.message ?? '사용자 목록 조회에 실패했습니다.'
      } finally {
        this.isLoadingUsers = false
      }
    },

    async changeUserRole(userId) {
      try {
        const res = await adminApi.changeUserRole(userId)
        const updated = res?.data ?? res
        const idx = this.users.findIndex(u => u.id === userId)
        if (idx !== -1) this.users[idx] = updated
        return updated
      } catch (e) {
        this.error = e?.response?.data?.message ?? '역할 변경에 실패했습니다.'
        throw e
      }
    },

    async deletePost(postId) {
      await adminApi.deletePost(postId)
    },
  },
})

export default useAdminStore
