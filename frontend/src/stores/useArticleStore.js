import { defineStore } from 'pinia'
import axiosInstance from '../api/axiosInstance'

export const useArticleStore = defineStore('article', {
  state: () => ({
    articles: [],
    selectedArticle: null,
    selectedArticleTerms: [],
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
    /**
     * 필터 리셋
     */
    resetFilters() {
      this.filters.category = ''
      this.filters.difficulty = ''
      this.filters.page = 0
      this.articles = []
      this.hasMore = true
    },

    /**
     * 카테고리 필터 설정
     */
    setCategory(category) {
      this.filters.category = category
      this.filters.page = 0
      this.articles = []
      this.hasMore = true
      return this.fetchArticles()
    },

    /**
     * 난이도 필터 설정
     */
    setDifficulty(difficulty) {
      this.filters.difficulty = difficulty
      this.filters.page = 0
      this.articles = []
      this.hasMore = true
      return this.fetchArticles()
    },

    /**
     * 기사 목록 조회 (페이징 / 무한 스크롤)
     */
    async fetchArticles() {
      if (this.isLoading || !this.hasMore) return
      this.isLoading = true

      try {
        const { category, difficulty, page, size } = this.filters
        const queryParams = new URLSearchParams()
        if (category) queryParams.append('category', category)
        if (difficulty) queryParams.append('difficulty', difficulty)
        queryParams.append('page', page)
        queryParams.append('size', size)

        const response = await axiosInstance.get(`/articles?${queryParams.toString()}`)
        // 백엔드 응답 규격 data: { content: [...], last: true/false, ... }
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
        this.isLoading = false
      }
    },

    /**
     * 특정 기사 상세조회
     */
    async fetchArticleDetail(articleId) {
      this.isLoading = true
      try {
        const response = await axiosInstance.get(`/articles/${articleId}`)
        this.selectedArticle = response.data || response
        await this.fetchArticleTerms(articleId)
      } catch (error) {
        console.error('Fetch article detail error:', error)
        throw error
      } finally {
        this.isLoading = false
      }
    },

    /**
     * 기사 내 경제 용어 목록 조회
     */
    async fetchArticleTerms(articleId) {
      try {
        const response = await axiosInstance.get(`/articles/${articleId}/terms`)
        this.selectedArticleTerms = response.data || response
      } catch (error) {
        console.error('Fetch article terms error:', error)
      }
    },

    /**
     * 기사 읽음 처리 완료 API 호출
     */
    async markArticleAsRead(articleId) {
      try {
        return await axiosInstance.post(`/articles/${articleId}/read`)
      } catch (error) {
        console.error('Mark article as read error:', error)
      }
    },

    /**
     * 기사 북마크 토글 API 호출
     */
    async toggleBookmark(articleId) {
      try {
        const response = await axiosInstance.post(`/articles/${articleId}/bookmark`)
        if (this.selectedArticle && this.selectedArticle.id === articleId) {
          // 로컬 상태 동기화
          this.selectedArticle.isBookmarked = !this.selectedArticle.isBookmarked
        }
        return response
      } catch (error) {
        console.error('Toggle bookmark error:', error)
      }
    }
  }
})

export default useArticleStore
