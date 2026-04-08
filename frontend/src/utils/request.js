import axios from 'axios'
import { useAuthStore } from '@/stores/auth'
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
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
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
          // 401错误只清除登录状态，不重定向，让页面自己处理
          const authStore = useAuthStore()
          authStore.logout()
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
