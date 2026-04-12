import axios from 'axios'
import router from '@/router'

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 60000,
  headers: {
    'Content-Type': 'application/json'
  }
})

request.interceptors.request.use(
  (config) => {
    // 同时检查 localStorage 和 sessionStorage，避免 Pinia store 未初始化的问题
    const token = localStorage.getItem('token') || sessionStorage.getItem('token')
    console.log('[request.js] 请求拦截器 - URL:', config.url, 'Token存在:', !!token)
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
      console.log('[request.js] 已添加 Authorization 头')
    } else {
      console.warn('[request.js] 未找到 Token，请求将不携带 Authorization 头')
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res.data
  },
  (error) => {
    if (error.response) {
      switch (error.response.status) {
        case 401:
          // 401错误清除登录状态（同时清除两种存储）
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          sessionStorage.removeItem('token')
          sessionStorage.removeItem('userInfo')
          // 只在非公开页面时重定向到首页
          const currentPath = router.currentRoute.value.path
          const publicPaths = ['/equipments', '/courses', '/coaches']
          const isPublicPath = publicPaths.some(path => currentPath.startsWith(path))
          if (!isPublicPath && currentPath !== '/') {
            router.push('/')
          }
          break
        case 403:
          router.push('/403')
          break
        case 404:
          router.push('/404')
          break
        default:
          console.error('请求错误:', error.response.data)
      }
    }
    return Promise.reject(error)
  }
)

export default request
