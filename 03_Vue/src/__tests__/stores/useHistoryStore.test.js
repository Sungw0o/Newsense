import { describe, it, expect, vi, beforeEach } from 'vitest'
import { useHistoryStore } from '../../stores/useHistoryStore'

vi.mock('../../api/learningApi', () => ({
  learningApi: {
    getHistory: vi.fn(),
    getStats: vi.fn(),
    getBookmarks: vi.fn(),
  },
}))

vi.mock('../../api/ragApi', () => ({
  default: {
    getRecommendations: vi.fn(),
  },
}))

import { learningApi } from '../../api/learningApi'
import ragApi from '../../api/ragApi'

describe('useHistoryStore — fetchHistory', () => {
  let store

  beforeEach(() => {
    store = useHistoryStore()
    vi.clearAllMocks()
  })

  it('성공: history 세팅, isLoading=false', async () => {
    learningApi.getHistory.mockResolvedValue({
      data: { timeline: [], stats: {} },
    })

    await store.fetchHistory()

    expect(store.history).toBeTruthy()
    expect(store.isLoading).toBe(false)
    expect(store.error).toBeNull()
  })

  it('실패: error 세팅, 에러 throw', async () => {
    learningApi.getHistory.mockRejectedValue(new Error('네트워크 오류'))

    await expect(store.fetchHistory()).rejects.toThrow('네트워크 오류')
    expect(store.error).toBe('네트워크 오류')
    expect(store.isLoading).toBe(false)
  })

  it('파라미터 전달: learningApi.getHistory 호출 시 params 전달', async () => {
    learningApi.getHistory.mockResolvedValue({ data: {} })

    await store.fetchHistory({ days: 7 })

    expect(learningApi.getHistory).toHaveBeenCalledWith({ days: 7 })
  })
})

describe('useHistoryStore — fetchStats', () => {
  let store

  beforeEach(() => {
    store = useHistoryStore()
    vi.clearAllMocks()
  })

  it('성공: stats 세팅', async () => {
    learningApi.getStats.mockResolvedValue({
      data: { totalArticlesRead: 10, quizAccuracy: 80 },
    })

    await store.fetchStats()

    expect(store.stats.totalArticlesRead).toBe(10)
    expect(store.isLoading).toBe(false)
  })

  it('실패: error 세팅', async () => {
    learningApi.getStats.mockRejectedValue(new Error('통계 로드 실패'))

    await expect(store.fetchStats()).rejects.toThrow()
    expect(store.error).toBe('통계 로드 실패')
  })
})

describe('useHistoryStore — fetchBookmarks', () => {
  let store

  beforeEach(() => {
    store = useHistoryStore()
    vi.clearAllMocks()
  })

  it('성공: bookmarks 배열 세팅', async () => {
    learningApi.getBookmarks.mockResolvedValue({
      data: [{ articleId: 1 }, { articleId: 2 }],
    })

    await store.fetchBookmarks()

    expect(store.bookmarks).toHaveLength(2)
  })

  it('실패: bookmarks 빈 배열로 초기화 (에러 숨김)', async () => {
    learningApi.getBookmarks.mockRejectedValue(new Error('오류'))

    await store.fetchBookmarks()

    expect(store.bookmarks).toEqual([])
  })
})

describe('useHistoryStore — fetchRecommendations', () => {
  let store

  beforeEach(() => {
    store = useHistoryStore()
    vi.clearAllMocks()
  })

  it('성공: recommendations 세팅', async () => {
    ragApi.getRecommendations.mockResolvedValue({
      data: { weaknessTerms: ['금리'], recommendations: [] },
    })

    await store.fetchRecommendations()

    expect(store.recommendations).toBeTruthy()
    expect(ragApi.getRecommendations).toHaveBeenCalledWith(6)
  })

  it('실패: recommendations null 유지 (에러 숨김)', async () => {
    ragApi.getRecommendations.mockRejectedValue(new Error('오류'))

    await store.fetchRecommendations()

    expect(store.recommendations).toBeNull()
  })
})
