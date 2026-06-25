import { describe, it, expect, vi, beforeEach } from 'vitest'
import { useAdminStore } from '../../stores/useAdminStore'

vi.mock('../../api/adminApi', () => ({
  default: {
    getStats: vi.fn(),
    getUsers: vi.fn(),
    changeUserRole: vi.fn(),
    deletePost: vi.fn(),
    getArticles: vi.fn(),
    refreshArticleSummary: vi.fn(),
  },
}))

import adminApi from '../../api/adminApi'

describe('useAdminStore — fetchStats', () => {
  let store

  beforeEach(() => {
    store = useAdminStore()
    vi.clearAllMocks()
  })

  it('성공: stats 세팅, isLoadingStats=false', async () => {
    adminApi.getStats.mockResolvedValue({ data: { totalUsers: 10, totalPosts: 5 } })

    await store.fetchStats()

    expect(store.stats.totalUsers).toBe(10)
    expect(store.isLoadingStats).toBe(false)
    expect(store.error).toBeNull()
  })

  it('실패: error 세팅', async () => {
    adminApi.getStats.mockRejectedValue({ response: { data: { message: '권한 없음' } } })

    await store.fetchStats()

    expect(store.error).toBe('권한 없음')
    expect(store.isLoadingStats).toBe(false)
  })
})

describe('useAdminStore — fetchUsers', () => {
  let store

  beforeEach(() => {
    store = useAdminStore()
    vi.clearAllMocks()
  })

  it('페이지 응답: content 배열 사용', async () => {
    adminApi.getUsers.mockResolvedValue({
      data: { content: [{ id: 1, email: 'a@a.com' }, { id: 2, email: 'b@b.com' }] },
    })

    await store.fetchUsers()

    expect(store.users).toHaveLength(2)
    expect(store.isLoadingUsers).toBe(false)
  })

  it('배열 응답: 그대로 사용', async () => {
    adminApi.getUsers.mockResolvedValue({ data: [{ id: 1 }] })

    await store.fetchUsers()

    expect(store.users).toHaveLength(1)
  })
})

describe('useAdminStore — changeUserRole', () => {
  let store

  beforeEach(() => {
    store = useAdminStore()
    store.users = [{ id: 1, role: 'USER' }]
    vi.clearAllMocks()
  })

  it('성공: 목록의 유저 업데이트', async () => {
    const updated = { id: 1, role: 'ADMIN' }
    adminApi.changeUserRole.mockResolvedValue({ data: updated })

    const result = await store.changeUserRole(1)

    expect(result.role).toBe('ADMIN')
    expect(store.users[0].role).toBe('ADMIN')
  })

  it('실패: error 세팅 및 throw', async () => {
    adminApi.changeUserRole.mockRejectedValue({ response: { data: { message: '실패' } } })

    await expect(store.changeUserRole(1)).rejects.toBeDefined()
    expect(store.error).toBe('실패')
  })
})

describe('useAdminStore — fetchArticles', () => {
  let store

  beforeEach(() => {
    store = useAdminStore()
    vi.clearAllMocks()
  })

  it('성공: articles 세팅', async () => {
    adminApi.getArticles.mockResolvedValue({
      data: { content: [{ articleId: 1, title: '기사' }] },
    })

    await store.fetchArticles()

    expect(store.articles).toHaveLength(1)
    expect(store.isLoadingArticles).toBe(false)
  })
})

describe('useAdminStore — refreshArticleSummary', () => {
  let store

  beforeEach(() => {
    store = useAdminStore()
    store.articles = [{ articleId: 1, summary: '기존 요약' }]
    vi.clearAllMocks()
  })

  it('성공: 기사 목록 업데이트', async () => {
    const updated = { articleId: 1, summary: '새 요약' }
    adminApi.refreshArticleSummary.mockResolvedValue({ data: updated })

    const result = await store.refreshArticleSummary(1)

    expect(result.summary).toBe('새 요약')
    expect(store.articles[0].summary).toBe('새 요약')
    expect(store.summarizingArticleIds).not.toContain(1)
  })

  it('중복 호출: 이미 요약 중이면 null 반환', async () => {
    store.summarizingArticleIds = [1]

    const result = await store.refreshArticleSummary(1)

    expect(result).toBeNull()
    expect(adminApi.refreshArticleSummary).not.toHaveBeenCalled()
  })
})
