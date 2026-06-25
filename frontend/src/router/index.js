import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/useUserStore'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/HomeView.vue'),
    meta: { title: '뉴스 목록 - Newsense', requiresAuth: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginView.vue'),
    meta: { title: '로그인 - Newsense', guestOnly: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/RegisterView.vue'),
    meta: { title: '회원가입 - Newsense', guestOnly: true }
  },
  {
    path: '/oauth/callback',
    name: 'OAuthCallback',
    component: () => import('../views/OAuthCallbackView.vue'),
    meta: { title: '소셜 로그인 - Newsense', guestOnly: true }
  },
  {
    path: '/onboarding',
    name: 'Onboarding',
    component: () => import('../views/OnboardingView.vue'),
    meta: { title: '관심사 설정 - Newsense', requiresAuth: true, isOnboarding: true }
  },
  {
    path: '/articles/:id',
    name: 'ArticleDetail',
    component: () => import('../views/ArticleDetailView.vue'),
    meta: { title: '기사 상세 - Newsense', requiresAuth: true }
  },
  {
    path: '/articles/:id/quiz',
    name: 'Quiz',
    component: () => import('../views/QuizView.vue'),
    meta: { title: '퀴즈 풀기 - Newsense', requiresAuth: true }
  },
  {
    path: '/articles/:id/review',
    name: 'ReviewWrite',
    component: () => import('../views/ReviewWriteView.vue'),
    meta: { title: '리뷰 작성 - Newsense', requiresAuth: true }
  },
  {
    path: '/quiz/:id/result',
    name: 'QuizResult',
    component: () => import('../views/QuizResultView.vue'),
    meta: { title: '퀴즈 결과 - Newsense', requiresAuth: true }
  },
  {
    path: '/history',
    name: 'LearningHistory',
    component: () => import('../views/LearningHistoryView.vue'),
    meta: { title: '학습 이력 - Newsense', requiresAuth: true }
  },
  {
    path: '/wrong-notes',
    name: 'WrongNote',
    component: () => import('../views/WrongNoteView.vue'),
    meta: { title: '오답노트 - Newsense', requiresAuth: true }
  },
  {
    path: '/mypage',
    name: 'MyPage',
    component: () => import('../views/MyPageView.vue'),
    meta: { title: '마이페이지 - Newsense', requiresAuth: true }
  },
  {
    path: '/community',
    name: 'Community',
    component: () => import('../views/CommunityView.vue'),
    meta: { title: '커뮤니티 - Newsense', requiresAuth: true }
  },
  {
    path: '/community/write',
    name: 'CommunityWrite',
    component: () => import('../views/CommunityWriteView.vue'),
    meta: { title: '글쓰기 - Newsense', requiresAuth: true }
  },
  {
    path: '/community/:id',
    name: 'CommunityDetail',
    component: () => import('../views/CommunityDetailView.vue'),
    meta: { title: '게시글 - Newsense', requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('../views/AdminView.vue'),
    meta: { title: '관리자 페이지 - Newsense', requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach(async (to, from, next) => {
  // 1. 브라우저 탭 타이틀 동적 변경
  if (to.meta.title) {
    document.title = to.meta.title
  }

  const userStore = useUserStore()

  // 2. 앱 최초 진입/새로고침 시 토큰이 존재하지만 스토어 정보가 없으면 복구 시도
  if (!userStore.userInfo && localStorage.getItem('accessToken')) {
    try {
      await userStore.initAuth()
    } catch (e) {
      console.warn('Initial verification failed in router beforeEach')
    }
  }

  const isAuthenticated = userStore.isAuthenticated

  // 3. 페이지 보안 접근 제어
  if (to.meta.requiresAuth && !isAuthenticated) {
    return next({ name: 'Login', query: { redirect: to.fullPath } })
  }
  if (to.meta.guestOnly && isAuthenticated) {
    return next({ name: 'Home' })
  }
  if (to.meta.requiresAdmin && userStore.userInfo?.role !== 'ADMIN') {
    alert('관리자 권한이 필요한 페이지입니다.')
    return next({ name: 'Home' })
  }

  // 4. 온보딩 강제: 인증된 유저 중 interests가 비어있으면 /onboarding으로 리다이렉트
  //    isOnboarding 플래그가 있는 페이지(온보딩 자체)는 제외하여 무한루프 방지
  if (
    isAuthenticated &&
    to.meta.requiresAuth &&
    !to.meta.isOnboarding &&
    (userStore.userInfo?.interests?.length ?? 0) === 0
  ) {
    return next({ name: 'Onboarding' })
  }

  next()
})

export default router
