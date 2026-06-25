import { describe, it, expect, vi, beforeEach } from 'vitest'
import { useArticleStore } from '../../stores/useArticleStore'

vi.mock('../../api/articleApi', () => ({
  default: {
    getArticles: vi.fn(),
    getArticleDetail: vi.fn(),
    getArticleTerms: vi.fn(),
    markAsRead: vi.fn(),
    toggleBookmark: vi.fn(),
  },
}))

import articleApi from '../../api/articleApi'

describe('useArticleStore — fetchArticles', () => {
  let store

  beforeEach(() => {
    store = useArticleStore()
    vi.clearAllMocks()
  })

  it('첫 페이지 로드: articles 세팅, hasMore 반영', async () => {
    articleApi.getArticles.mockResolvedValue({
      data: {
        content: [{ id: 1, title: '기준금리 기사' }],
        last: false,
      },
    })

    await store.fetchArticles()

    expect(store.articles).toHaveLength(1)
    expect(store.articles[0].title).toBe('기준금리 기사')
    expect(store.hasMore).toBe(true)
    expect(store.isLoading).toBe(false)
    expect(store.error).toBeNull()
  })

  it('마지막 페이지: hasMore=false', async () => {
    articleApi.getArticles.mockResolvedValue({
      data: { content: [{ id: 2, title: '기사2' }], last: true },
    })

    await store.fetchArticles()

    expect(store.hasMore).toBe(false)
  })

  it('API 오류: error 세팅, isLoading=false', async () => {
    articleApi.getArticles.mockRejectedValue({
      response: { data: { message: '서버 오류' } },
    })

    await store.fetchArticles()

    expect(store.error).toBe('서버 오류')
    expect(store.isLoading).toBe(false)
  })

  it('isLoading=true 일 때 중복 호출 무시', async () => {
    store.isLoading = true

    await store.fetchArticles()

    expect(articleApi.getArticles).not.toHaveBeenCalled()
  })

  it('hasMore=false 일 때 추가 호출 무시', async () => {
    store.hasMore = false

    await store.fetchArticles()

    expect(articleApi.getArticles).not.toHaveBeenCalled()
  })
})

describe('useArticleStore — setCategory / setDifficulty / setKeyword', () => {
  let store

  beforeEach(() => {
    store = useArticleStore()
    store.articles = [{ id: 1 }]
    vi.clearAllMocks()
    articleApi.getArticles.mockResolvedValue({
      data: { content: [], last: true },
    })
  })

  it('setCategory: 필터 변경 후 articles 초기화 및 재조회', async () => {
    await store.setCategory('MACRO_ECONOMY')

    expect(store.filters.category).toBe('MACRO_ECONOMY')
    expect(store.filters.page).toBe(0)
    expect(store.articles).toHaveLength(0)
    expect(articleApi.getArticles).toHaveBeenCalledTimes(1)
  })

  it('setDifficulty: 필터 변경 후 articles 초기화', async () => {
    await store.setDifficulty('BASIC')

    expect(store.filters.difficulty).toBe('BASIC')
    expect(store.articles).toHaveLength(0)
  })

  it('setKeyword: 키워드 변경 후 articles 초기화', async () => {
    await store.setKeyword('금리')

    expect(store.filters.keyword).toBe('금리')
    expect(store.articles).toHaveLength(0)
  })

  it('resetFilters: 모든 필터 초기화', () => {
    store.filters.category = 'MACRO_ECONOMY'
    store.filters.keyword = '금리'
    store.error = '에러'

    store.resetFilters()

    expect(store.filters.category).toBe('')
    expect(store.filters.keyword).toBe('')
    expect(store.error).toBeNull()
    expect(store.filters.page).toBe(0)
  })
})

describe('useArticleStore — fetchArticleDetail', () => {
  let store

  beforeEach(() => {
    store = useArticleStore()
    vi.clearAllMocks()
  })

  it('기사 상세 + 용어 로드 성공', async () => {
    articleApi.getArticleDetail.mockResolvedValue({
      data: { id: 1, title: '기준금리 기사', isBookmarked: false },
    })
    articleApi.getArticleTerms.mockResolvedValue({
      data: [{ name: '기준금리', definition: '정책 금리' }],
    })

    await store.fetchArticleDetail(1)

    expect(store.selectedArticle.title).toBe('기준금리 기사')
    expect(store.selectedArticleTerms).toHaveLength(1)
    expect(store.isLoading).toBe(false)
  })

  it('기사 상세 실패: 에러 throw', async () => {
    articleApi.getArticleDetail.mockRejectedValue(new Error('404'))

    await expect(store.fetchArticleDetail(999)).rejects.toThrow()
    expect(store.isLoading).toBe(false)
  })
})

describe('useArticleStore — toggleBookmark', () => {
  let store

  beforeEach(() => {
    store = useArticleStore()
    store.selectedArticle = { id: 1, title: '기사', isBookmarked: false }
    vi.clearAllMocks()
  })

  it('북마크 토글: selectedArticle.isBookmarked 업데이트', async () => {
    articleApi.toggleBookmark.mockResolvedValue({ data: { isBookmarked: true } })

    await store.toggleBookmark(1)

    expect(store.selectedArticle.isBookmarked).toBe(true)
  })

  it('북마크 실패: 에러 throw', async () => {
    articleApi.toggleBookmark.mockRejectedValue(new Error('서버 오류'))

    await expect(store.toggleBookmark(1)).rejects.toThrow()
  })
})
