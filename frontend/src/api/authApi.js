import axiosInstance from './axiosInstance'

export const authApi = {
  /**
   * 회원가입 API
   * @param {Object} registerData { email, password, nickname }
   */
  register(registerData) {
    return axiosInstance.post('/auth/signup', registerData)
  },

  /**
   * 로그인 API
   * @param {Object} loginData { email, password }
   */
  login(loginData) {
    return axiosInstance.post('/auth/login', loginData)
  },

  /**
   * Access Token 재발급 API (Refresh Token은 HttpOnly Cookie로 전송)
   */
  refresh() {
    return axiosInstance.post('/auth/refresh')
  },

  /**
   * 로그아웃 API
   */
  logout() {
    return axiosInstance.post('/auth/logout')
  },

  /**
   * 이메일(username) 중복 확인 API
   * @param {string} username
   */
  checkUsername(username) {
    return axiosInstance.get('/auth/check-username', { params: { username } })
  },

  checkEmail(email) {
    return 