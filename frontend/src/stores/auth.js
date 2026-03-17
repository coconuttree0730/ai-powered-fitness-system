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
      if (res.data.code === 200) {
        token.value = res.data.data.token
        localStorage.setItem('token', res.data.data.token)
        
        await fetchUserInfo()
        
        const redirect = router.currentRoute.value.query.redirect || '/'
        router.push(redirect)
        return { success: true }
      }
      return { success: false, message: res.data.message }
    } catch (error) {
      return { success: false, message: error.message || '登录失败' }
    }
  }

  async function fetchUserInfo() {
    try {
      const res = await getCurrentUser()
      if (res.data.code === 200) {
        userInfo.value = res.data.data
        localStorage.setItem('userInfo', JSON.stringify(res.data.data))
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
    router.push('/login')
  }

  function hasRole(role) {
    return userRoles.value.includes(role)
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
    hasRole
  }
})
