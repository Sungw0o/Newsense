import { describe, it, expect, vi, beforeEach } from 'vitest'
import { useWrongNoteStore } from '../../stores/useWrongNoteStore'

vi.mock('../../api/wrongNoteApi', () => ({
  wrongNoteApi: {
    getWrongNotes: vi.fn(),
    toggleResolve: vi.fn(),
    deleteWrongNote: vi.fn(),
  },
}))

import { wrongNoteApi } from '../../api/wrongNoteApi'

describe('useWrongNoteStore — fetchWrongNotes', () => {
  let store

  beforeEach(() => {
    store = useWrongNoteStore()
    vi.clearAllMocks()
  })

  it('페이지 응답: content 배열을 wrongNotes에 세팅', async () => {
    wrongNoteApi.getWrongNotes.mockResolvedValue({
      data: {
        content: [
          { id: 1, quizId: 10, isResolved: false },
          { id: 2, quizId: 11, isResolved: true },
        ],
      },
    })

    await store.fetchWrongNotes()

    expect(store.wrongNotes).toHaveLength(2)
    expect(store.isLoading).toBe(false)
    expect(store.error).toBeNull()
  })

  it('배열 응답: data가 배열이면 그대로 사용', async () => {
    wrongNoteApi.getWrongNotes.mockResolvedValue({
      data: [{ id: 1, isResolved: false }],
    })

    await store.fetchWrongNotes()

    expect(store.wrongNotes).toHaveLength(1)
  })

  it('실패: error 세팅 및 에러 throw', async () => {
    wrongNoteApi.getWrongNotes.mockRejectedValue(new Error('네트워크 오류'))

    await expect(store.fetchWrongNotes()).rejects.toThrow('네트워크 오류')
    expect(store.error).toBe('네트워크 오류')
    expect(store.isLoading).toBe(false)
  })
})

describe('useWrongNoteStore — toggleResolve', () => {
  let store

  beforeEach(() => {
    store = useWrongNoteStore()
    store.wrongNotes = [
      { id: 1, isResolved: false },
      { id: 2, isResolved: true },
    ]
    vi.clearAllMocks()
  })

  it('해결 상태 반전: isResolved false → true', async () => {
    wrongNoteApi.toggleResolve.mockResolvedValue({})

    await store.toggleResolve(1)

    expect(store.wrongNotes.find(n => n.id === 1).isResolved).toBe(true)
  })

  it('해결 상태 반전: isResolved true → false', async () => {
    wrongNoteApi.toggleResolve.mockResolvedValue({})

    await store.toggleResolve(2)

    expect(store.wrongNotes.find(n => n.id === 2).isResolved).toBe(false)
  })

  it('API 실패: 에러 throw, 로컬 상태 미변경', async () => {
    wrongNoteApi.toggleResolve.mockRejectedValue(new Error('오류'))

    await expect(store.toggleResolve(1)).rejects.toThrow()
    // 상태가 변하지 않아야 함
    expect(store.wrongNotes.find(n => n.id === 1).isResolved).toBe(false)
  })
})

describe('useWrongNoteStore — deleteWrongNote', () => {
  let store

  beforeEach(() => {
    store = useWrongNoteStore()
    store.wrongNotes = [
      { id: 1, isResolved: false },
      { id: 2, isResolved: true },
    ]
    vi.clearAllMocks()
  })

  it('삭제 성공: wrongNotes에서 즉시 제거', async () => {
    wrongNoteApi.deleteWrongNote.mockResolvedValue({})

    await store.deleteWrongNote(1)

    expect(store.wrongNotes).toHaveLength(1)
    expect(store.wrongNotes.find(n => n.id === 1)).toBeUndefined()
  })

  it('삭제 실패: 에러 throw, 목록 그대로 유지', async () => {
    wrongNoteApi.deleteWrongNote.mockRejectedValue(new Error('삭제 실패'))

    await expect(store.deleteWrongNote(1)).rejects.toThrow()
    expect(store.wrongNotes).toHaveLength(2)
  })
})
