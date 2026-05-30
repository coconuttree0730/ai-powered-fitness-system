import axios from 'axios'
import router from '@/router'
import { useAuthStore } from '@/stores/auth'

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 60000,
  headers: {
    'Content-Type': 'application/json'
  }
})

export class ApiError extends Error {
  constructor(code, message, data) {
    super(message)
    this.code = code
    this.data = data
  }
}

let isRefreshing = false
let pendingRequests = []
const MAX_REFRESH_RETRIES = 3

function getAccessToken() {
  return localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken') || ''
}

function getRefreshToken() {
  return localStorage.getItem('refreshToken') || sessionStorage.getItem('refreshToken') || ''
}

function saveAccessToken(token) {
  localStorage.setItem('accessToken', token)
  sessionStorage.setItem('accessToken', token)

  const authStore = useAuthStore()
  authStore.accessToken = token
}

function clearAllTokens() {
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('userInfo')
  sessionStorage.removeItem('accessToken')
  sessionStorage.removeItem('refreshToken')
  sessionStorage.removeItem('userInfo')

  const authStore = useAuthStore()
  authStore.accessToken = ''
  authStore.refreshToken = ''
  authStore.userInfo = null
}

function resolvePendingRequests(token) {
  pendingRequests.forEach(cb => cb(token))
  pendingRequests = []
}

function rejectPendingRequests() {
  const queue = [...pendingRequests]
  pendingRequests = []
  queue.forEach(cb => cb(null))
}

async function tryRefreshToken() {
  const refreshToken = getRefreshToken()
  if (!refreshToken) return null

  try {
    const response = await axios.post('/api/v1/auth/refresh', { refreshToken })
    if (response.data && response.data.code === 200) {
      const newAccessToken = response.data.data.accessToken
      saveAccessToken(newAccessToken)
      return newAccessToken
    }
  } catch (e) {
    console.error('刷新Token失败:', e)
  }
  return null
}

request.interceptors.request.use(
  (config) => {
    const token = getAccessToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
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
      return Promise.reject(new ApiError(res.code, res.message || '请求失败', res.data))
    }
    return res.data
  },
  async (error) => {
    const originalRequest = error.config

    if (error.response && error.response.status === 401 && !originalRequest._retry) {
      originalRequest._retryCount = originalRequest._retryCount || 0
      if (originalRequest._retryCount >= MAX_REFRESH_RETRIES) {
        clearAllTokens()
        router.push('/')
        return Promise.reject(error)
      }
      originalRequest._retryCount += 1

      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          pendingRequests.push((token) => {
            if (token) {
              originalRequest.headers.Authorization = `Bearer ${token}`
              originalRequest._retry = true
              resolve(request(originalRequest))
            } else {
              reject(error)
            }
          })
        })
      }

      originalRequest._retry = true
      isRefreshing = true

      try {
        const newAccessToken = await tryRefreshToken()
        if (newAccessToken) {
          resolvePendingRequests(newAccessToken)
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`
          return request(originalRequest).catch(retryError => {
            // 刷新后重试仍然 401（如被踢人、token在刷新后被删除），清理本地 token
            if (retryError.response && retryError.response.status === 401) {
              clearAllTokens()
            }
            return Promise.reject(retryError)
          })
        } else {
          rejectPendingRequests()
          clearAllTokens()
          return Promise.reject(error)
        }
      } finally {
        isRefreshing = false
      }
    }

    return Promise.reject(error)
  }
)

export { getAccessToken, getRefreshToken, saveAccessToken, clearAllTokens, tryRefreshToken }
export default request