import { defineStore } from 'pinia'
import articleApi from '../api/articleApi'

export const useArticleStore = defineStore('article', {
  state: () => ({
    articles: [],
    selectedArticle: null,
    selectedArticleTerms: [],
    currentRequestId: 0,
    filters: {
      category: '',
      difficulty: '',
      page: 0,
      size: 10,
    },
    hasMore: true,
    isLoading: false,
  }),
  actions: {
    resetFilters() {
      this.filters.category = ''
      this.filters.difficulty = ''
      this.filters.page = 0
      this.articles = []
      this.hasMore = true
    },

    setCategory(category) {
      this.filters.category = category
      this.filters.page = 0
      this.articles = []
      this.hasMore = true
      return this.fetchArticles()
    },

    setDifficulty(difficulty) {
      this.filters.difficulty = difficulty
      this.filters.page = 0
      this.articles = []
      this.hasMore = true
      return this.fetchArticles()
    },

    async fetchArticles() {
      if (this.isLoading || !this.hasMore) return
      this.isLoading = true
      const requestId = ++this.currentRequestId

      try {
        const { category, difficulty, page, size } = this.filters
        const response = await articleApi.getArticles({ category, difficulty, page, size })

        if (requestId !== this.currentRequestId) return

        const data = response.data || response
        const content = data.content || []

        if (page === 0) {
          this.articles = content
        } else {
          this.articles.push(...content)
        }

        this.hasMore = !data.last && content.length > 0
        if (this.hasMore) {
          this.filters.page += 1
        }
      } catch (error) {
        console.error('Fetch articles error:', error)
      } finally {
        if (requestId === this.currentRequestId) {
          this.isLoading = false
        }
      }
    },

    async fetchArticleDetail(articleId) {
      this.selectedArticle = null
      this.selectedArticleTerms = []
      this.isLoading = true
      try {
        const response = await articleApi.getArticleDetail(articleId)
        this.selectedArticle = response.data || response
        await this.fetchArticleTerms(articleId)
      } catch (error) {
        console.error('Fetch article detail error:', error)
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async fetchArticleTerms(articleId) {
      try {
        const response = await articleApi.getArticleTerms(articleId)
        this.selectedArticleTerms = response.data || response
      } catch (error) {
        console.error('Fetch article terms error:', error)
        this.selectedArticleTerms = []
        throw error
      }
    },

    async markArticleAsRead(articleId) {
      try {
        return await articleApi.markAsRead(articleId)
      } catch (error) {
        console.error('Mark article as read error:', error)
        throw error
      }
    },

    async toggleBookmark(articleId) {
      try {
        const response = await articleApi.toggleBookmark(articleId)
        const data = response.data || response
        if (this.selectedArticle && String(this.selectedArticle.id) === String(articleId)) {
          this.selectedArticle.isBookmarked = data.isBookmarked
        }
        return response
      } catch (error) {
        console.error('Toggle bookmark error:', error)
        throw error
      }
    }
  }
})

export default useArticleStore
