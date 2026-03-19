<template>
  <div class="coach-layout">
    <n-layout has-sider>
      <n-layout-sider
        bordered
        :width="200"
        :native-scrollbar="false"
        show-trigger
        collapse-mode="width"
        :collapsed-width="64"
      >
        <div class="logo">
          <h3>教练中心</h3>
        </div>
        <n-menu
          :options="menuOptions"
          :value="activeKey"
          @update:value="handleMenuSelect"
        />
      </n-layout-sider>
      <n-layout>
        <n-layout-header bordered class="header">
          <div class="header-content">
            <span>{{ currentTitle }}</span>
            <n-dropdown :options="userOptions" @select="handleUserSelect">
              <n-button text>
                <n-avatar round :size="32">{{ authStore.userInfo?.username?.charAt(0) }}</n-avatar>
                <span style="margin-left: 8px">{{ authStore.userInfo?.username }}</span>
              </n-button>
            </n-dropdown>
          </div>
        </n-layout-header>
        <n-layout-content class="content">
          <router-view />
        </n-layout-content>
      </n-layout>
    </n-layout>
  </div>
</template>

<script setup>
import { computed, h } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NIcon } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'
import {
  HomeOutline,
  PersonOutline,
  CalendarOutline,
  PeopleOutline,
  TimeOutline
} from '@vicons/ionicons5'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const activeKey = computed(() => route.path)
const currentTitle = computed(() => route.meta.title || '教练中心')

function renderIcon(icon) {
  return () => h(NIcon, null, { default: () => h(icon) })
}

const menuOptions = [
  { label: '工作台', key: '/coach', icon: renderIcon(HomeOutline) },
  { label: '个人信息', key: '/coach/profile', icon: renderIcon(PersonOutline) },
  { label: '我的课程', key: '/coach/courses', icon: renderIcon(CalendarOutline) },
  { label: '课程日程', key: '/coach/schedule', icon: renderIcon(TimeOutline) },
  { label: '我的学员', key: '/coach/students', icon: renderIcon(PeopleOutline) }
]

const userOptions = [
  { label: '返回首页', key: 'home' },
  { label: '退出登录', key: 'logout' }
]

function handleMenuSelect(key) {
  router.push(key)
}

function handleUserSelect(key) {
  if (key === 'home') {
    router.push('/')
  } else if (key === 'logout') {
    authStore.logout()
  }
}
</script>

<style scoped>
.coach-layout {
  height: 100vh;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #e8e8e8;
}

.logo h3 {
  margin: 0;
  color: #52c41a;
}

.header {
  height: 64px;
  padding: 0 24px;
  display: flex;
  align-items: center;
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.content {
  padding: 24px;
  background: #f5f5f5;
  min-height: calc(100vh - 64px);
}
</style>
