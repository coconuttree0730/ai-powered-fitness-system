import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, getCurrentUser, logout as logoutApi } from '@/api/auth'
import router from '@/router'

export const useAuthStore = defineStore('auth', () => {
  //获取登录信息：用于登录携带token
  const accessToken = ref(
    localStorage.getItem('accessToken') ||
    sessionStorage.getItem('accessToken') ||
    ''
  )
  //获取登录信息：用于刷新token
  const refreshToken = ref(
    localStorage.getItem('refreshToken') ||
    sessionStorage.getItem('refreshToken') ||
    ''
  )

  const userInfo = ref(
    JSON.parse(
      localStorage.getItem('userInfo') ||
      sessionStorage.getItem('userInfo') ||
      'null'
    )
  )

  const isLoggedIn = computed(() => !!accessToken.value)
  const userRoles = computed(() => userInfo.value?.roles || [])
  const isAdmin = computed(() => userRoles.value.includes('ADMIN'))
  const isCoach = computed(() => userRoles.value.includes('COACH'))
  const isMember = computed(() => userRoles.value.includes('MEMBER'))
// 清除存储，登出时执行
  function _clearStorage() {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userInfo')
    sessionStorage.removeItem('accessToken')
    sessionStorage.removeItem('refreshToken')
    sessionStorage.removeItem('userInfo')
  }

  function _saveCredentials(data, rememberMe) {
    _clearStorage()
    accessToken.value = data.accessToken
    refreshToken.value = data.refreshToken
    userInfo.value = data.userInfo || null

    const storage = rememberMe ? localStorage : sessionStorage
    storage.setItem('accessToken', data.accessToken)
    storage.setItem('refreshToken', data.refreshToken)
    if (data.userInfo) {
      storage.setItem('userInfo', JSON.stringify(data.userInfo))
    }
  }

  function _redirectByRoles(roles) {
    const redirect = router.currentRoute.value.query.redirect || getDashboardPathByRoles(roles)
    router.push(redirect)
  }

  async function login(credentials, rememberMe = false) {
    try {
      const res = await loginApi({ ...credentials, rememberMe })
      if (res && res.accessToken) {
        _saveCredentials(res, rememberMe)
        _redirectByRoles(res.userInfo?.roles || [])
        return { success: true }
      }
      return { success: false, message: '登录失败' }
    } catch (error) {
      return { success: false, message: error.message || '登录失败' }
    }
  }

  function getDashboardPathByRoles(roles) {
    if (roles.includes('ADMIN')) return '/admin'
    if (roles.includes('COACH')) return '/coach'
    if (roles.includes('MEMBER')) return '/member'
    return '/'
  }

  async function fetchUserInfo() {
    try {
      const res = await getCurrentUser()
      if (res) {
        userInfo.value = res
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }

  function updateAccessToken(newAccessToken) {
    accessToken.value = newAccessToken
    if (localStorage.getItem('accessToken')) {
      localStorage.setItem('accessToken', newAccessToken)
    }
    if (sessionStorage.getItem('accessToken')) {
      sessionStorage.setItem('accessToken', newAccessToken)
    }
  }

  function getRefreshToken() {
    return refreshToken.value
  }

  async function logout() {
    const currentRefreshToken = refreshToken.value

    try {
      await logoutApi(currentRefreshToken)
    } catch (e) {
      console.warn('后端登出失败，继续清理本地状态:', e)
    }

    accessToken.value = ''
    refreshToken.value = ''
    userInfo.value = null
    _clearStorage()
    router.push('/')
  }

  async function setLoginState(data, rememberMe = false) {
    if (data && data.accessToken) {
      _saveCredentials(data, rememberMe)
      _redirectByRoles(data.userInfo?.roles || [])
      return { success: true }
    }
    return { success: false, message: '登录数据无效' }
  }

  return {
    accessToken,
    refreshToken,
    userInfo,
    isLoggedIn,
    userRoles,
    isAdmin,
    isCoach,
    isMember,
    login,
    fetchUserInfo,
    logout,
    setLoginState,
    updateAccessToken,
    getRefreshToken
  }
})