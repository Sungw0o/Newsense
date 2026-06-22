import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true, // 필수: HttpOnly 쿠키(Refresh Token) 송수신용
  headers: {
    'Content-Type': 'application/json',
  },
})

// 요청 인터셉터: 로컬 스토리지에서 Access Token을 꺼내 Authorization 헤더에 자동 주입
axiosInstance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response 인터셉터 대기 큐 관리
let isRefreshing = false
let failedQueue = []

const processQueue = (error, token = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error)
    } else {
      prom.resolve(token)
    }
  })
  failedQueue = []
}

// 응답 인터셉터: 공통 응답 규격 처리 및 401 에러 발생 시 토큰 자동 재발급(Refresh)
axiosInstance.interceptors.response.use(
  (response) => {
    // 백엔드 공통 응답 규격 { success, code, message, data }에서 data 바로 반환
    return response.data
  },
  async (error) => {
    const originalRequest = error.config

    // 401 Unauthorized 에러 발생 시 토큰 갱신 시도
    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`
            return axiosInstance(originalRequest)
          })
          .catch((err) => Promise.reject(err))
      }

      originalRequest._retry = true
      isRefreshing = true

      try {
        // Refresh Token은 HttpOnly 쿠키로 오기 때문에 빈 Body로 POST 요청 전송
        const response = await axios.post(`${API_BASE_URL}/auth/refresh`, {}, {
          withCredentials: true,
        })
        
        // 백엔드 응답 규격에 맞춰 새로운 Access Token 추출
        const newAccessToken = response.data?.data?.accessToken || response.data?.accessToken
        
        if (newAccessToken) {
          localStorage.setItem('accessToken', newAccessToken)
          axiosInstance.defaults.headers.common.Authorization = `Bearer ${newAccessToken}`
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`
          
          processQueue(null, newAccessToken)
          return axiosInstance(originalRequest)
        } else {
          throw new Error('Refresh response did not contain a valid token')
        }
      } catch (refreshError) {
        processQueue(refreshError, null)
        // 토큰 갱신 실패 시 로컬 토큰 만료 처리 및 로그인 페이지 리다이렉트
        localStorage.removeItem('accessToken')
        if (window.location.pathname !== '/login' && window.location.pathname !== '/register') {
          window.location.href = '/login'
        }
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    return Promise.reject(error)
  }
)

export default axiosInstance
