import axiosInstance from './axiosInstance'

export const indicatorApi = {
  getIndicators() {
    return axiosInstance.get('/indicators')
  }
}
