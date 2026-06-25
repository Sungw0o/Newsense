import axiosInstance from './axiosInstance'

const stockApi = {
  getQuotes(codes = []) {
    const params = new URLSearchParams()
    codes.forEach(code => params.append('codes', code))
    return axiosInstance.get(`/stocks?${params.toString()}`)
  }
}

export default stockApi
