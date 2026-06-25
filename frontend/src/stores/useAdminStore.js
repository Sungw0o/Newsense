import { defineStore } from 'pinia'
import adminApi from '../api/adminApi'

export const useAdminStore = defineStore('admin', {
  state: () => ({
    stats: null,
    users: [],
    articles: [],
    articlePage: {
      number: 0,
      size: 20,
      totalPages: 0,
      totalElements: 0,
      first: true,
      last: true,
    },
    isLoadingStats: false,
    isLoadingUsers: false,
    isLoadingArticles: false,
    summarizingArticleIds: [],
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

    async deactivateUser(userId) {
      try {
        const res = await adminApi.deactivateUser(userId)
        const updated = res?.data ?? res
        const idx = this.users.findIndex(u => u.id === userId)
        if (idx !== -1) this.users[idx] = updated
        return updated
      } catch (e) {
        this.error = e?.response?.data?.message ?? '탈퇴 처리에 실패했습니다.'
        throw e
      }
    },

    async deletePost(postId) {
      await adminApi.deletePost(postId)
    },

    async fetchArticles(params = {}) {
      this.isLoadingArticles = true
      this.error = null
      try {
        const res = await adminApi.getArticles(params)
        const data = res?.data ?? res
        this.articles = Array.isArray(data) ? data : (data?.content ?? [])
        if (!Array.isArray(data)) {
          this.articlePage = {
            number: data?.number ?? 0,
            size: data?.size ?? params.size ?? 20,
            totalPages: data?.totalPages ?? 0,
            totalElements: data?.totalElements ?? this.articles.length,
            first: data?.first ?? true,
            last: data?.last ?? true,
          }
        }
      } catch (e) {
        this.error = e?.response?.data?.message ?? '기사 목록 조회에 실패했습니다.'
      } finally {
        this.isLoadingArticles = false
      }
    },

    async refreshArticleSummary(articleId) {
      if (this.summarizingArticleIds.includes(articleId)) return null
      this.summarizingArticleIds.push(articleId)
      this.error = null
      try {
        const res = await adminApi.refreshArticleSummary(articleId)
        const updated = res?.data ?? res
        const idx = this.articles.findIndex(article => article.articleId === articleId)
        if (idx !== -1) this.articles[idx] = updated
        return updated
      } catch (e) {
        this.error = e?.response?.data?.message ?? 'AI 요약 생성에 실패했습니다.'
        throw e
      } finally {
        this.summarizingArticleIds = this.summarizingArticleIds.filter(id => id !== articleId)
      }
    },

    async deleteArticle(articleId) {
      await adminApi.deleteArticle(articleId)
      this.articles = this.articles.filter(article => article.articleId !== articleId)
      this.articlePage.totalElements = Math.max(0, this.articlePage.totalElements - 1)
    },
  },
})

export default useAdminStore
