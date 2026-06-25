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
      keyword: '',
      sort: 'LATEST',
      page: 0,
      size: 20,
    },
    hasMore: true,
    isLoading: false,
    error: null,
  }),
  actions: {
    resetFilters() {
      this.filters.category = ''
      this.filters.difficulty = ''
      this.filters.keyword = ''
      this.filters.sort = 'LATEST'
      this.filters.page = 0
      this.articles = []
      this.hasMore = true
      this.error = null
    },

    setCategory(category) {
      this.filters.category = category
      this.filters.page = 0
      this.articles = []
      this.hasMore = true
      this.error = null
      return this.fetchArticles()
    },

    setDifficulty(difficulty) {
      this.filters.difficulty = difficulty
      this.filters.page = 0
      this.articles = []
      this.hasMore = true
      this.error = null
      return this.fetchArticles()
    },

    setKeyword(keyword) {
      this.filters.keyword = keyword
      this.filters.page = 0
      this.articles = []
      this.hasMore = true
      this.error = null
      return this.fetchArticles()
    },

    setSort(sort) {
      this.filters.sort = sort
      this.filters.page = 0
      this.articles = []
      this.hasMore = true
      this.error = null
      return this.fetchArticles()
    },

    async fetchArticles() {
      if (this.isLoading || !this.hasMore) return
      this.isLoading = true
      this.error = null
      const requestId = ++this.currentRequestId

      try {
        const { category, difficulty, keyword, sort, page, size } = this.filters
        const response = await articleApi.getArticles({
          category,
          difficulty,
          keyword: keyword || undefined,
          sort,
          page,
          size,
        })

        if (requestId !== this.currentRequestId) return

        const data = response.data?.data ?? response.data
        const content = data.content ?? []

        if (page === 0) {
          this.articles = content
        } else {
          this.articles.push(...content)
        }

        this.hasMore = !data.last && content.length > 0
        this.filters.page = page + 1
      } catch (err) {
        if (requestId === this.currentRequestId) {
          this.error = err?.response?.data?.message ?? '기사를 불러오는 데 실패했습니다.'
        }
      } finally {
        if (requestId === this.currentRequestId) {
          this.isLoading = false
        }
      }
    },

    async fetchArticleDetail(articleId) {
      this.isLoading = true
      this.error = null
      try {
        const response = await articleApi.getArticleDetail(articleId)
        const data = response.data?.data ?? response.data
        this.selectedArticle = data
      } catch (err) {
        this.error = err?.response?.data?.message ?? '기사를 불러오는 데 실패했습니다.'
        this.selectedArticle = null
      } finally {
        this.isLoading = false
      }
    },

    async fetchArticleTerms(articleId) {
      try {
        const response = await articleApi.getArticleTerms(articleId)
        this.selectedArticleTerms = response.data?.data ?? response.data ?? []
      } catch {
        this.selectedArticleTerms = []
      }
    },

    async markArticleAsRead(articleId) {
      try {
        await articleApi.markAsRead(articleId)
      } catch {
        // 읽음 처리 실패는 무시
      }
    },

    async toggleBookmark(articleId) {
      try {
        const response = await articleApi.toggleBookmark(articleId)
        const data = response.data?.data ?? response.data
        if (this.selectedArticle) {
          this.selectedArticle.isBookmarked = data.isBookmarked
        }
      } catch (err) {
        throw err
      }
    },
  },
})
