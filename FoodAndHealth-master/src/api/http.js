import axios from 'axios'
import router from '@/router'

const http = axios.create({
  // Prefer env override; default to local backend per API doc.
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 120000
})

// 请求拦截器：自动附加 Token
http.interceptors.request.use(config => {
  const token =localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：统一错误处理
http.interceptors.response.use(
  res => {
    if (res.data.code !== 200) {
      return Promise.reject(res.data)
    }
    return res.data
  },
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      router.push('/login')
    }
    return Promise.reject(err)
  }
)

export default http
