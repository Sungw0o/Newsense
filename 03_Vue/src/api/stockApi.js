import axiosInstance from './axiosInstance'

const stockApi = {
  getQuote(code) {
    return axiosInstance.get(`/stocks/${code}`)
  },

  getQuotes(codes = []) {
    const params = new URLSearchParams()
    codes.forEach(code => params.append('codes', code))
    return axiosInstance.get(`/stocks?${params.toString()}`)
  }
}

export default stockApi
