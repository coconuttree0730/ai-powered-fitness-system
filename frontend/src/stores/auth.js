import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, getCurrentUser } from '@/api/auth'
import router from '@/router'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const userRoles = computed(() => userInfo.value?.roles || [])
  const isAdmin = computed(() => userRoles.value.includes('ADMIN'))
  const isCoach = computed(() => userRoles.value.includes('COACH'))
  const isMember = computed(() => userRoles.value.includes('MEMBER'))

  async function login(credentials) {
    try {
      const res = await loginApi(credentials)
      if (res && res.token) {
        token.value = res.token
        localStorage.setItem('token', res.token)
        
        if (res.userInfo) {
          userInfo.value = res.userInfo
          localStorage.setItem('userInfo', JSON.stringify(res.userInfo))
        }
        
        const roles = res.userInfo?.roles || []
        const redirect = router.currentRoute.value.query.redirect || getDashboardPathByRoles(roles)
        router.push(redirect)
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
        localStorage.setItem('userInfo', JSON.stringify(res))
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.push('/')
  }

  function hasRole(role) {
    return userRoles.value.includes(role)
  }

  // 设置登录状态（用于短信登录等直接返回token的场景）
  async function setLoginState(data) {
    if (data && data.token) {
      token.value = data.token
      localStorage.setItem('token', data.token)
      
      if (data.userInfo) {
        userInfo.value = data.userInfo
        localStorage.setItem('userInfo', JSON.stringify(data.userInfo))
      }
      
      const roles = data.userInfo?.roles || []
      const redirect = router.currentRoute.value.query.redirect || getDashboardPathByRoles(roles)
      router.push(redirect)
      return { success: true }
    }
    return { success: false, message: '登录数据无效' }
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    userRoles,
    isAdmin,
    isCoach,
    isMember,
    login,
    fetchUserInfo,
    logout,
    hasRole,
    setLoginState
  }
})
