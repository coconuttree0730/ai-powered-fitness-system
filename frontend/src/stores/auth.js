import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, getCurrentUser } from '@/api/auth'
import router from '@/router'

export const useAuthStore = defineStore('auth', () => {
  // 初始化时从 localStorage 或 sessionStorage 读取 token
  const token = ref(
    localStorage.getItem('token') ||
    sessionStorage.getItem('token') ||
    ''
  )
  const userInfo = ref(
    JSON.parse(
      localStorage.getItem('userInfo') ||
      sessionStorage.getItem('userInfo') ||
      'null'
    )
  )

  const isLoggedIn = computed(() => !!token.value)
  const userRoles = computed(() => userInfo.value?.roles || [])
  const isAdmin = computed(() => userRoles.value.includes('ADMIN'))
  const isCoach = computed(() => userRoles.value.includes('COACH'))
  const isMember = computed(() => userRoles.value.includes('MEMBER'))

  async function login(credentials, rememberMe = false) {
    try {
      const res = await loginApi(credentials)
      console.log('[auth.js] loginApi 返回数据:', res)
      console.log('[auth.js] res.token:', res?.token)
      console.log('[auth.js] res.userInfo:', res?.userInfo)
      
      if (res && res.token) {
        // 先清除所有旧数据，避免 localStorage 和 sessionStorage 数据不一致
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        sessionStorage.removeItem('token')
        sessionStorage.removeItem('userInfo')

        token.value = res.token

        if (rememberMe) {
          // 长期保存到 localStorage
          localStorage.setItem('token', res.token)
          console.log('[auth.js] Token 存储到 localStorage')
        } else {
          // 会话级保存到 sessionStorage
          sessionStorage.setItem('token', res.token)
          console.log('[auth.js] Token 存储到 sessionStorage')
        }

        if (res.userInfo) {
          userInfo.value = res.userInfo
          if (rememberMe) {
            localStorage.setItem('userInfo', JSON.stringify(res.userInfo))
          } else {
            sessionStorage.setItem('userInfo', JSON.stringify(res.userInfo))
          }
        }

        const roles = res.userInfo?.roles || []
        console.log('[auth.js] 用户角色:', roles)
        const redirect = router.currentRoute.value.query.redirect || getDashboardPathByRoles(roles)
        console.log('[auth.js] 跳转到:', redirect)
        router.push(redirect)
        return { success: true }
      }
      return { success: false, message: '登录失败' }
    } catch (error) {
      console.error('[auth.js] 登录错误:', error)
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
        // 同时更新两种存储中的用户信息
        localStorage.setItem('userInfo', JSON.stringify(res))
        sessionStorage.setItem('userInfo', JSON.stringify(res))
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    // 清除 localStorage
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    // 清除 sessionStorage
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('userInfo')
    router.push('/')
  }

  function hasRole(role) {
    return userRoles.value.includes(role)
  }

  // 设置登录状态（用于短信登录等直接返回token的场景）
  async function setLoginState(data, rememberMe = false) {
    if (data && data.token) {
      // 先清除所有旧数据，避免 localStorage 和 sessionStorage 数据不一致
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      sessionStorage.removeItem('token')
      sessionStorage.removeItem('userInfo')

      token.value = data.token

      if (rememberMe) {
        localStorage.setItem('token', data.token)
      } else {
        sessionStorage.setItem('token', data.token)
      }

      if (data.userInfo) {
        userInfo.value = data.userInfo
        if (rememberMe) {
          localStorage.setItem('userInfo', JSON.stringify(data.userInfo))
        } else {
          sessionStorage.setItem('userInfo', JSON.stringify(data.userInfo))
        }
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
