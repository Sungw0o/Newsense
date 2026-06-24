import { defineStore } from 'pinia'
import communityApi from '../api/communityApi'

const MOCK_POSTS = [
  {
    postId: 1,
    title: '기준금리 인하 기사 읽고 느낀 점 — 전세대출 어떻게 될까요?',
    content: '오늘 한국은행 기준금리 인하 기사를 읽었는데, 솔직히 전세대출 금리가 바로 내려가는 건 아니더라고요. 코픽스(COFIX) 연동이라 시차가 있다는 게 새로웠어요. 저처럼 전세 계약 갱신 앞둔 분들 어떻게 대응하고 계신가요?',
    author: { nickname: '경제공부중', avatarInitial: '경' },
    viewCount: 214,
    likeCount: 31,
    dislikeCount: 2,
    commentCount: 7,
    createdAt: '2026-06-24T10:30:00',
    articleScrap: {
      articleId: 1,
      title: '기준금리 인하가 청년 전세대출에 미치는 영향',
      summary: '한국은행 금융통화위원회가 이번 달 기준금리를 0.25%p 인하했습니다. 시중 은행의 전세자금대출 금리도 하락세를 보일 것으로 예상됩니다.',
      category: '금융',
    },
    userReaction: null,
  },
  {
    postId: 2,
    title: 'DSR 규제 완화가 실제로 내 대출 한도에 미치는 영향 정리',
    content: 'LTV/DSR 기사 읽고 직접 계산해봤습니다. 연소득 4천만 원 기준으로 DSR 40% 적용 시 원리금 한도가 약 1,333만 원/년인데, 완화되면 이게 어떻게 바뀌는지 계산법 공유합니다.',
    author: { nickname: '부동산알짜', avatarInitial: '부' },
    viewCount: 389,
    likeCount: 58,
    dislikeCount: 5,
    commentCount: 12,
    createdAt: '2026-06-23T15:00:00',
    articleScrap: {
      articleId: 2,
      title: 'LTV와 DSR 규제 완화, 무엇이 달라지나?',
      summary: '정부가 주택담보대출 비율(LTV)과 총부채원리금상환비율(DSR)의 한도를 조정했습니다.',
      category: '부동산',
    },
    userReaction: null,
  },
  {
    postId: 3,
    title: '원달러 1400원 시대, 달러 예금 지금 들어가도 될까요?',
    content: '환율 기사 읽고 달러 예금에 관심이 생겼는데, 환율이 이미 높은 시점에 진입하는 게 맞는지 고민됩니다. 달러 예금 경험 있는 분 계신가요? 환율 피크 판단이 가장 어렵더라고요.',
    author: { nickname: '환율궁금이', avatarInitial: '환' },
    viewCount: 156,
    likeCount: 19,
    dislikeCount: 1,
    commentCount: 4,
    createdAt: '2026-06-23T09:15:00',
    articleScrap: null,
    userReaction: null,
  },
  {
    postId: 4,
    title: 'CPI 상승률 둔화 — 금리 인하 시그널로 봐도 될까',
    content: '소비자물가지수 기사 읽으면서 근원 CPI와 헤드라인 CPI 차이를 처음 제대로 이해했어요. 에너지·식품 제외한 근원 CPI가 더 중요한 지표라는 게 인상깊었습니다. 다들 어떤 지표 중점으로 보시나요?',
    author: { nickname: '거시경제팬', avatarInitial: '거' },
    viewCount: 98,
    likeCount: 14,
    dislikeCount: 0,
    commentCount: 3,
    createdAt: '2026-06-22T18:40:00',
    articleScrap: {
      articleId: 4,
      title: '소비자물가지수(CPI) 상승률 둔화의 의미',
      summary: '최근 발표된 소비자물가지수 상승률이 예상치를 하회하며 인플레이션 압력이 완화되는 신호를 보내고 있습니다.',
      category: '거시경제',
    },
    userReaction: null,
  },
]

const MOCK_COMMENTS = {
  1: [
    { commentId: 1, author: { nickname: '부동산알짜', avatarInitial: '부' }, content: '코픽스 연동이라는 게 핵심이죠. 보통 기준금리 인하 후 2-3개월 시차가 있습니다.', createdAt: '2026-06-24T11:00:00' },
    { commentId: 2, author: { nickname: '뉴스센스유저', avatarInitial: '뉴' }, content: '저도 갱신 앞두고 있는데, 일단 변동형으로 기다려보려고 합니다.', createdAt: '2026-06-24T11:30:00' },
  ],
  2: [
    { commentId: 3, author: { nickname: '거시경제팬', avatarInitial: '거' }, content: '계산 감사합니다! 혼합금리 적용 시 어떻게 바뀌는지도 궁금하네요.', createdAt: '2026-06-23T16:00:00' },
  ],
  3: [],
  4: [
    { commentId: 4, author: { nickname: '경제공부중', avatarInitial: '경' }, content: '근원 CPI가 더 중요하다는 거 저도 이번 기사로 알았어요!', createdAt: '2026-06-22T19:00:00' },
  ],
}

export const useCommunityStore = defineStore('community', {
  state: () => ({
    posts: [],
    currentPost: null,
    comments: [],
    isLoading: false,
    isPostLoading: false,
    isCommentLoading: false,
  }),

  actions: {
    async fetchPosts() {
      this.isLoading = true
      try {
        const res = await communityApi.getPosts()
        this.posts = res?.data ?? res ?? JSON.parse(JSON.stringify(MOCK_POSTS))
      } catch {
        this.posts = JSON.parse(JSON.stringify(MOCK_POSTS))
      } finally {
        this.isLoading = false
      }
    },

    async fetchPost(postId) {
      this.isPostLoading = true
      try {
        const res = await communityApi.getPost(postId)
        this.currentPost = res?.data ?? res
      } catch {
        const found = MOCK_POSTS.find(p => p.postId === Number(postId))
        this.currentPost = found ? JSON.parse(JSON.stringify(found)) : null
      } finally {
        this.isPostLoading = false
      }
    },

    async createPost(data) {
      const res = await communityApi.createPost(data)
      const created = res?.data ?? res
      this.posts.unshift(created)
      return created
    },

    async toggleReaction(postId, type) {
      const post = this.posts.find(p => p.postId === postId) ?? this.currentPost
      if (!post) return

      // 롤백용 스냅샷
      const snapshot = { userReaction: post.userReaction, likeCount: post.likeCount, dislikeCount: post.dislikeCount }

      // 상태 먼저 낙관적으로 변경
      const prev = post.userReaction
      if (prev === type) {
        post.userReaction = null
        if (type === 'like') post.likeCount--
        else post.dislikeCount--
      } else {
        if (prev === 'like') post.likeCount--
        if (prev === 'dislike') post.dislikeCount--
        post.userReaction = type
        if (type === 'like') post.likeCount++
        else post.dislikeCount++
      }

      if (this.currentPost?.postId === postId) {
        Object.assign(this.currentPost, {
          userReaction: post.userReaction,
          likeCount: post.likeCount,
          dislikeCount: post.dislikeCount,
        })
      }

      // API 실패 시 롤백
      try {
        await communityApi.toggleReaction(postId, type)
      } catch {
        Object.assign(post, snapshot)
        if (this.currentPost?.postId === postId) Object.assign(this.currentPost, snapshot)
      }
    },

    async fetchComments(postId) {
      this.isCommentLoading = true
      try {
        const res = await communityApi.getComments(postId)
        this.comments = res?.data ?? res ?? JSON.parse(JSON.stringify(MOCK_COMMENTS[postId] ?? []))
      } catch {
        this.comments = JSON.parse(JSON.stringify(MOCK_COMMENTS[postId] ?? []))
      } finally {
        this.isCommentLoading = false
      }
    },

    async createComment(postId, content) {
      const res = await communityApi.createComment(postId, content)
      const created = res?.data ?? res ?? {
        commentId: Date.now(),
        author: { nickname: '나', avatarInitial: '나' },
        content,
        createdAt: new Date().toISOString(),
      }
      this.comments.push(created)
      if (this.currentPost?.postId === postId) this.currentPost.commentCount++
    },
  },
})

export default useCommunityStore
