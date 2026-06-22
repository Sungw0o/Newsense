import { defineStore } from 'pinia'
import { authApi } from '../api/authApi'
import axiosInstance from '../api/axiosInstance'

export const useUserStore = defineStore('user', {
  state: () => ({
    isAuthenticated: false,
    userInfo: null,
  }),
  actions: {
    /**
     * 회원가입 액션
     */
    async register(registerData) {
      return await authApi.register(registerData)
    },

    /**
     * 로그인 액션
     */
    async login(loginData) {
      try {
        const response = await authApi.login(loginData)
        // 백엔드 응답 규격: { success, code, message, data: { accessToken, ... } }
        // axiosInstance 응답 인터셉터가 response.data를 반환하므로 response는 { success, code, message, data } 형태임
        const token = response.data?.accessToken
        
        if (token) {
          localStorage.setItem('accessToken', token)
          this.isAuthenticated = true
          await this.fetchUserProfile()
        } else {
          throw new Error('로그인 응답에 Access Token이 누락되었습니다.')
        }
        return response
      } catch (error) {
        this.clearAuth()
        throw error
      }
    },

    /**
     * 로그아웃 액션
     */
    async logout() {
      try {
        await authApi.logout()
      } catch (error) {
        console.error('Logout error:', error)
      } finally {
        this.clearAuth()
      }
    },

    /**
     * 사용자 정보(프로필) 가져오기
     */
    async fetchUserProfile() {
      try {
        const response = await axiosInstance.get('/users/me')
        // 백엔드 응답 규격 data에 회원 상세 정보가 담겨 있음
        this.userInfo = response.data || response
        this.isAuthenticated = true
      } catch (error) {
        console.error('Fetch profile error:', error)
        this.clearAuth()
        throw error
      }
    },

    /**
     * 인증 상태 초기화 (클라이언트 로그아웃 처리)
     */
    clearAuth() {
      localStorage.removeItem('accessToken')
      delete axiosInstance.defaults.headers.common['Authorization']
      this.isAuthenticated = false
      this.userInfo = null
    },

    /**
     * 앱 구동 시 초기 인증 체크
     */
    async initAuth() {
      const token = localStorage.getItem('accessToken')
      if (token) {
        this.isAuthenticated = true
        try {
          await this.fetchUserProfile()
        } catch (error) {
          console.warn('Initial profile fetch failed, token might be invalid or expired')
          this.clearAuth()
        }
      } else {
        this.clearAuth()
      }
    }
  }
})

export default useUserStore
