import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

export function useUserInfo() {
  const router = useRouter()
  const authStore = useAuthStore()

  const userAvatar = computed(() => authStore.userInfo?.avatar || '')
  const username = computed(() => authStore.userInfo?.username || '')
  const usernameInitial = computed(() => username.value ? username.value.charAt(0) : '用')

  function goHome() {
    router.push('/')
  }

  function handleLogout() {
    authStore.logout()
  }

  return {
    userAvatar,
    username,
    usernameInitial,
    goHome,
    handleLogout
  }
}