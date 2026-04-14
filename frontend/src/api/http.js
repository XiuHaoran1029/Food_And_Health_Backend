import axios from 'axios'
import router from '@/router'
import { Capacitor } from '@capacitor/core'

const DEFAULT_API_ORIGIN = 'http://localhost:8080'

function resolveBaseURL() {
  const webBaseURL = (import.meta.env.VITE_WEB_API_BASE_URL || '/').trim()
  const nativeBaseURL = (
    import.meta.env.VITE_NATIVE_API_BASE_URL ||
    import.meta.env.VITE_API_BASE_URL ||
    ''
  ).trim()

  if (Capacitor.isNativePlatform()) {
    return nativeBaseURL || DEFAULT_API_ORIGIN
  }

  return webBaseURL || '/'
}

const http = axios.create({
  baseURL: resolveBaseURL(),
  timeout: 120000
})

// 请求拦截器：登录/注册接口 不携带 Token
http.interceptors.request.use(config => {
  const token = localStorage.getItem('token')

  // ✅ 白名单：这些接口不传递 Token
  const noTokenApis = [
    '/api/auth/login',
    '/api/auth/register'
  ]

  // 如果是登录/注册，直接返回，不加请求头
  if (noTokenApis.includes(config.url)) {
    return config
  }

  // 其他接口才加 Token
  if (token && token !== 'null' && token !== 'undefined') {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器
http.interceptors.response.use(
    res => {
      const contentType = String(res.headers?.['content-type'] || '').toLowerCase()
      const body = res.data
      const isHtmlFallback =
        contentType.includes('text/html') ||
        (typeof body === 'string' && body.trim().toLowerCase().startsWith('<!doctype html'))

      if (isHtmlFallback) {
        const error = new Error('API 返回了 HTML 页面，请检查移动端 API 基址配置')
        error.code = 'API_HTML_FALLBACK'
        throw error
      }

      return body
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
