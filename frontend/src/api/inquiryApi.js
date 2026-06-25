import axiosInstance from './axiosInstance'

const inquiryApi = {
  submit(data) {
    return axiosInstance.post('/inquiries', data)
  },
  getMyInquiries(params = {}) {
    return axiosInstance.get('/inquiries/me', { params })
  },
}

export default inquiryApi
