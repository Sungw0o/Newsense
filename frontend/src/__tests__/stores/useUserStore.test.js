import { describe, it, expect, vi, beforeEach } from 'vitest'
import { useUserStore } from '../../stores/useUserStore'

// authApi 전체를 모킹
vi.mock('../../api/authApi', () => ({
  authApi: {
    login: vi.fn(),
    logout: vi.fn(),
    register: vi.fn(),
    refresh: vi.fn(),
  },
  default: {
    login: vi.fn(),
    logout: vi.fn(),
    register: vi.fn(),
    refresh: vi.fn(),
  },
}))

// axiosInstance 모킹 (fetchUserProfile 내부에서 /users/me 호출)
vi.mock('../../api/axiosInstance', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    interceptors: {
      request: { use: vi.fn() },
      response: { use: vi.fn() },
    },
    defaults: { headers: { common: {} } },
  },
}))

import { authApi } from '../../api/authApi'
import axiosInstance from '../../api/axiosInstance'

// ─────────────────────────────────────────────────────────────────────────────
//  useUserStore — 로그인 관련 액션 테스트
// ─────────────────────────────────────────────────────────────────────────────

describe('useUserStore — 로그인 (login 액션)', () => {
  let store

  beforeEach(() => {
    store = useUserStore()
    localStorage.clear()
    vi.clearAllMocks()
  })

  // ── 성공 케이스 ──────────────────────────────────────────────────────────

  it('로그인 성공: accessToken 저장, isAuthenticated=true, userInfo 세팅', async () => {
    const mockLoginData = { email: 'user@example.com', password: 'Password1!' }

    // axiosInstance 응답 인터셉터가 response.data를 반환하는 구조 시뮬레이션
    authApi.login.mockResolvedValue({
      success: true,
      code: 200,
      message: '로그인이 완료되었습니다.',
      data: {
        accessToken: 'mock.access.token',
        tokenType: 'Bearer',
        userId: 1,
        nickname: 'tester',
        role: 'USER',
      },
    })

    axiosInstance.get.mockResolvedValue({
      success: true,
      data: { id: 1, nickname: 'tester', email: 'user@example.com' },
    })

    await store.login(mockLoginData)

    expect(localStorage.getItem('accessToken')).toBe('mock.access.token')
    expect(store.isAuthenticated).toBe(true)
    expect(store.userInfo).toBeTruthy()
    expect(store.userInfo.nickname).toBe('tester')
  })

  it('로그인 성공: authApi.login 호출 시 올바른 이메일/비밀번호 전달', async () => {
    const loginData = { email: 'check@example.com', password: 'Correct1!' }

    authApi.login.mockResolvedValue({
      data: { accessToken: 'token' },
    })
    axiosInstance.get.mockResolvedValue({ data: { nickname: 'user' } })

    await store.login(loginData)

    expect(authApi.login).toHaveBeenCalledWith(loginData)
    expect(authApi.login).toHaveBeenCalledTimes(1)
  })

  // ── 실패 케이스 ──────────────────────────────────────────────────────────

  it('로그인 실패: API 에러 시 isAuthenticated=false 유지, accessToken 없음', async () => {
    authApi.login.mockRejectedValue(new Error('401 Unauthorized'))

    await expect(store.login({ email: 'bad@example.com', password: 'wrong' }))
      .rejects.toThrow()

    expect(localStorage.getItem('accessToken')).toBeNull()
    expect(store.isAuthenticated).toBe(false)
    expect(store.userInfo).toBeNull()
  })

  it('로그인 실패: 응답에 accessToken 없으면 에러 throw', async () => {
    authApi.login.mockResolvedValue({
      success: false,
      data: null,
    })

    await expect(store.login({ email: 'user@example.com', password: 'pass' }))
      .rejects.toThrow('로그인 응답에 Access Token이 누락되었습니다.')

    expect(store.isAuthenticated).toBe(false)
  })

  it('fetchUserProfile 실패 시: clearAuth 호출 → isAuthenticated=false, localStorage 비워짐', async () => {
    authApi.login.mockResolvedValue({
      data: { accessToken: 'valid.token' },
    })
    // /users/me 호출 실패
    axiosInstance.get.mockRejectedValue(new Error('Network Error'))

    await expect(store.login({ email: 'user@example.com', password: 'Password1!' }))
      .rejects.toThrow()

    expect(store.isAuthenticated).toBe(false)
    expect(localStorage.getItem('accessToken')).toBeNull()
  })
})

// ─────────────────────────────────────────────────────────────────────────────
//  useUserStore — 로그아웃 (logout 액션)
// ─────────────────────────────────────────────────────────────────────────────

describe('useUserStore — 로그아웃 (logout 액션)', () => {
  let store

  beforeEach(() => {
    store = useUserStore()
    localStorage.clear()
    vi.clearAllMocks()
  })

  it('로그아웃 성공: isAuthenticated=false, userInfo=null, localStorage 비워짐', async () => {
    // 로그인 상태 세팅
    localStorage.setItem('accessToken', 'some.token')
    store.isAuthenticated = true
    store.userInfo = { id: 1, nickname: 'tester' }

    authApi.logout.mockResolvedValue({})

    await store.logout()

    expect(store.isAuthenticated).toBe(false)
    expect(store.userInfo).toBeNull()
    expect(localStorage.getItem('accessToken')).toBeNull()
  })

  it('로그아웃 API 실패해도 클라이언트 인증 상태는 초기화됨', async () => {
    localStorage.setItem('accessToken', 'some.token')
    store.isAuthenticated = true

    authApi.logout.mockRejectedValue(new Error('Network Error'))

    await store.logout() // 에러가 throw되지 않아야 함

    expect(store.isAuthenticated).toBe(false)
    expect(localStorage.getItem('accessToken')).toBeNull()
  })
})

// ─────────────────────────────────────────────────────────────────────────────
//  useUserStore — initAuth (앱 최초 진입 인증 복구)
// ─────────────────────────────────────────────────────────────────────────────

describe('useUserStore — initAuth (초기 인증 상태 복구)', () => {
  let store

  beforeEach(() => {
    store = useUserStore()
    localStorage.clear()
    vi.clearAllMocks()
  })

  it('localStorage에 토큰 있으면 fetchUserProfile 호출 → isAuthenticated=true', async () => {
    localStorage.setItem('accessToken', 'existing.token')
    axiosInstance.get.mockResolvedValue({
      data: { id: 1, nickname: 'tester' },
    })

    await store.initAuth()

    expect(store.isAuthenticated).toBe(true)
    expect(store.userInfo).toBeTruthy()
    expect(axiosInstance.get).toHaveBeenCalledWith('/users/me')
  })

  it('localStorage에 토큰 없으면 clearAuth → isAuthenticated=false', async () => {
    await store.initAuth()

    expect(store.isAuthenticated).toBe(false)
    expect(store.userInfo).toBeNull()
    expect(axiosInstance.get).not.toHaveBeenCalled()
  })

  it('fetchUserProfile 실패 시 토큰 만료 처리 → clearAuth', async () => {
    localStorage.setItem('accessToken', 'expired.token')
    axiosInstance.get.mockRejectedValue(new Error('401'))

    await store.initAuth()

    expect(store.isAuthenticated).toBe(false)
    expect(localStorage.getItem('accessToken')).toBeNull()
  })
})
