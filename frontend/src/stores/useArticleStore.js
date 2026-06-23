import { defineStore } from 'pinia'
import articleApi from '../api/articleApi'

export const useArticleStore = defineStore('article', {
  state: () => ({
    articles: [],
    selectedArticle: null,
    selectedArticleTerms: [],
    currentRequestId: 0, // 레이스 컨디션 방지용 요청 ID
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
      const requestId = ++this.currentRequestId // 요청 번호 생성

      try {
        const { category, difficulty, page, size } = this.filters
        const response = await articleApi.getArticles({ category, difficulty, page, size })
        
        // 최신 요청이 아닌 경우 무시 (Race Condition 방지)
        if (requestId !== this.currentRequestId) return

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
        if (requestId === this.currentRequestId) {
          this.isLoading = false
        }
      }
    },

    /**
     * 특정 기사 상세조회
     */
    async fetchArticleDetail(articleId) {
      this.selectedArticle = null
      this.selectedArticleTerms = [] // 상세조회 시작 시 이전 데이터 클리어
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

    /**
     * 기사 내 경제 용어 목록 조회
     */
    async fetchArticleTerms(articleId) {
      try {
        const response = await articleApi.getArticleTerms(articleId)
        this.selectedArticleTerms = response.data || response
      } catch (error) {
        console.error('Fetch article terms error:', error)
        this.selectedArticleTerms = [] // 에러 발생 시 클리어
        throw error
      }
    },

    /**
     * 기사 읽음 처리 완료 API 호출
     */
    async markArticleAsRead(articleId) {
      try {
        return await articleApi.markAsRead(articleId)
      } catch (error) {
        console.error('Mark article as read error:', error)
        throw error // 에러 전파
      }
    },

    /**
     * 기사 북마크 토글 API 호출
     */
    async toggleBookmark(articleId) {
      try {
        const response = await articleApi.toggleBookmark(articleId)
        if (this.selectedArticle && this.selectedArticle.id === articleId) {
          // 로컬 상태 동기화
          this.selectedArticle.isBookmarked = !this.selectedArticle.isBookmarked
        }
        return response
      } catch (error) {
        console.error('Toggle bookmark error:', error)
        throw error // 에러 전파
      }
    }
  }
})

export default useArticleStore
