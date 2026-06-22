import axiosInstance from './axiosInstance'

export const authApi = {
  /**
   * 회원가입 API
   * @param {Object} registerData { email, password, nickname }
   */
  register(registerData) {
    return axiosInstance.post('/auth/register', registerData)
  },

  /**
   * 로그인 API
   * @param {Object} loginData { email, password }
   */
  login(loginData) {
    return axiosInstance.post('/auth/login', loginData)
  },

  /**
   * 로그아웃 API
   */
  logout() {
    return axiosInstance.post('/auth/logout')
  },
}

export default authApi
