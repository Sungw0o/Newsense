import { describe, it, expect, vi, beforeEach } from 'vitest'
import { useCommunityStore } from '../../stores/useCommunityStore'

vi.mock('../../api/communityApi', () => ({
  default: {
    getPosts: vi.fn(),
    getNotices: vi.fn(),
    getPost: vi.fn(),
    createPost: vi.fn(),
    toggleReaction: vi.fn(),
    getComments: vi.fn(),
    createComment: vi.fn(),
  },
}))

import communityApi from '../../api/communityApi'

describe('useCommunityStore — fetchPosts', () => {
  let store

  beforeEach(() => {
    store = useCommunityStore()
    vi.clearAllMocks()
  })

  it('성공: posts 정규화하여 세팅', async () => {
    communityApi.getPosts.mockResolvedValue({
      data: {
        content: [
          { postId: 1, title: '기사 토론', username: '홍길동', likeCount: 5 },
        ],
      },
    })

    await store.fetchPosts()

    expect(store.posts).toHaveLength(1)
    expect(store.posts[0].postId).toBe(1)
    expect(store.isLoading).toBe(false)
  })

  it('실패: posts 빈 배열 (에러 숨김)', async () => {
    communityApi.getPosts.mockRejectedValue(new Error('서버 오류'))

    await store.fetchPosts()

    expect(store.posts).toEqual([])
    expect(store.isLoading).toBe(false)
  })
})

describe('useCommunityStore — fetchPost', () => {
  let store

  beforeEach(() => {
    store = useCommunityStore()
    vi.clearAllMocks()
  })

  it('성공: currentPost 세팅', async () => {
    communityApi.getPost.mockResolvedValue({
      data: { postId: 1, title: '제목', username: '유저' },
    })

    await store.fetchPost(1)

    expect(store.currentPost?.postId).toBe(1)
    expect(store.isPostLoading).toBe(false)
  })

  it('실패: currentPost null (에러 숨김)', async () => {
    communityApi.getPost.mockRejectedValue(new Error('404'))

    await store.fetchPost(999)

    expect(store.currentPost).toBeNull()
  })
})

describe('useCommunityStore — createPost', () => {
  let store

  beforeEach(() => {
    store = useCommunityStore()
    vi.clearAllMocks()
  })

  it('성공: posts 맨 앞에 추가', async () => {
    store.posts = [{ postId: 2, title: '기존 글' }]
    communityApi.createPost.mockResolvedValue({
      data: { postId: 1, title: '새 글', username: '작성자' },
    })

    const result = await store.createPost({ title: '새 글', content: '내용' })

    expect(store.posts[0].postId).toBe(1)
    expect(store.posts).toHaveLength(2)
    expect(result.postId).toBe(1)
  })
})

describe('useCommunityStore — toggleReaction', () => {
  let store

  beforeEach(() => {
    store = useCommunityStore()
    store.posts = [
      { postId: 1, userReaction: null, likeCount: 10, dislikeCount: 2 },
    ]
    vi.clearAllMocks()
  })

  it('좋아요 추가: 낙관적 업데이트 성공', async () => {
    communityApi.toggleReaction.mockResolvedValue({})

    await store.toggleReaction(1, 'like')

    expect(store.posts[0].userReaction).toBe('like')
    expect(store.posts[0].likeCount).toBe(11)
  })

  it('좋아요 취소: 같은 reaction이면 null로 변경', async () => {
    store.posts[0].userReaction = 'like'
    store.posts[0].likeCount = 11
    communityApi.toggleReaction.mockResolvedValue({})

    await store.toggleReaction(1, 'like')

    expect(store.posts[0].userReaction).toBeNull()
    expect(store.posts[0].likeCount).toBe(10)
  })

  it('API 실패: 낙관적 상태 롤백', async () => {
    communityApi.toggleReaction.mockRejectedValue(new Error('오류'))

    await store.toggleReaction(1, 'like')

    expect(store.posts[0].userReaction).toBeNull()
    expect(store.posts[0].likeCount).toBe(10)
  })
})

describe('useCommunityStore — fetchComments / createComment', () => {
  let store

  beforeEach(() => {
    store = useCommunityStore()
    store.currentPost = { postId: 1, commentCount: 2 }
    vi.clearAllMocks()
  })

  it('fetchComments 성공: comments 세팅', async () => {
    communityApi.getComments.mockResolvedValue({
      data: { content: [{ commentId: 1, username: '댓글유저' }] },
    })

    await store.fetchComments(1)

    expect(store.comments).toHaveLength(1)
    expect(store.isCommentLoading).toBe(false)
  })

  it('fetchComments 실패: comments 빈 배열', async () => {
    communityApi.getComments.mockRejectedValue(new Error('오류'))

    await store.fetchComments(1)

    expect(store.comments).toEqual([])
  })

  it('createComment 성공: comments에 추가 및 commentCount 증가', async () => {
    communityApi.createComment.mockResolvedValue({
      data: { commentId: 10, username: '작성자', content: '좋은 글이네요' },
    })

    await store.createComment(1, '좋은 글이네요')

    expect(store.comments).toHaveLength(1)
    expect(store.currentPost.commentCount).toBe(3)
  })
})
