import axios from 'axios'
import router from '@/router'
import { useAuthStore } from '@/stores/auth'
import { getAccessToken, getRefreshToken, saveAccessToken, removeToken } from '@/utils/auth'
import { ElMessage } from 'element-plus'

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

function syncTokenToStore(token) {
  const authStore = useAuthStore()
  authStore.accessToken = token
}

function clearAllTokens() {
  removeToken()
  localStorage.removeItem('userInfo')
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
  const token = getRefreshToken()
  if (!token) return null

  try {
    const response = await axios.post('/api/v1/auth/refresh', { refreshToken: token })
    if (response.data && response.data.code === 200) {
      const newAccessToken = response.data.data.accessToken
      saveAccessToken(newAccessToken)
      syncTokenToStore(newAccessToken)
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
      ElMessage.error(res.message || '请求失败')
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
          ElMessage.error('登录已过期，请重新登录')
          router.push('/')
          return Promise.reject(error)
        }
      } finally {
        isRefreshing = false
      }
    }

    // 非 401 的 HTTP 错误，显示统一提示
    if (error.response) {
      const status = error.response.status
      const msg = error.response.data?.message
      if (status >= 500) {
        ElMessage.error(msg || '服务器错误，请稍后重试')
      } else if (status === 403) {
        ElMessage.error(msg || '权限不足')
      } else if (status === 404) {
        ElMessage.error(msg || '请求的资源不存在')
      } else if (status >= 400) {
        ElMessage.error(msg || '请求参数错误')
      }
    } else if (error.message && error.message.includes('timeout')) {
      ElMessage.error('请求超时，请检查网络后重试')
    } else if (error.message && error.message.includes('Network Error')) {
      ElMessage.error('网络错误，请检查网络连接')
    }

    return Promise.reject(error)
  }
)

export { getAccessToken, getRefreshToken, saveAccessToken, clearAllTokens, tryRefreshToken }
export default request