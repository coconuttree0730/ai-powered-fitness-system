<template>
  <div class="member-layout">
    <!-- 移动端顶部导航栏 -->
    <div class="mobile-header" v-if="isMobile">
      <div class="mobile-logo">
        <div class="logo-icon">💪</div>
        <span>健身会员中心</span>
      </div>
      <div class="mobile-actions">
        <n-tag type="warning" size="small" round class="mobile-points">
          <template #icon>
            <n-icon :component="DiamondOutline" />
          </template>
          580
        </n-tag>
        <n-button text class="menu-toggle" @click="showMobileMenu = !showMobileMenu">
          <n-icon :size="24" :component="showMobileMenu ? CloseOutline : MenuOutline" />
        </n-button>
      </div>
    </div>

    <!-- 移动端菜单抽屉 -->
    <template v-if="isMobile">
      <div class="mobile-menu-overlay" v-if="showMobileMenu" @click="showMobileMenu = false"></div>
      <div class="mobile-menu" :class="{ open: showMobileMenu }">
        <div class="mobile-menu-header">
          <span>菜单</span>
          <n-button text @click="showMobileMenu = false">
            <n-icon :size="24" :component="CloseOutline" />
          </n-button>
        </div>
        <div class="mobile-menu-items">
          <div
            v-for="item in menuOptions"
            :key="item.key"
            :class="['mobile-menu-item', activeKey === item.key ? 'active' : '']"
            @click="handleMobileMenuSelect(item.key)"
          >
            <n-icon :size="20">
              <component :is="item.iconComponent" />
            </n-icon>
            <span>{{ item.label }}</span>
          </div>
        </div>
        <div class="mobile-menu-footer">
          <n-button type="error" block @click="handleLogout">
            <template #icon>
              <n-icon :component="LogOutOutline" />
            </template>
            退出登录
          </n-button>
        </div>
      </div>
    </template>

    <!-- 桌面端布局 -->
    <n-layout v-if="!isMobile" has-sider class="full-height">
      <n-layout-sider
        bordered
        :width="sidebarWidth"
        :native-scrollbar="false"
        show-trigger
        collapse-mode="width"
        :collapsed-width="64"
        v-model:collapsed="collapsed"
        class="sidebar"
      >
        <div class="logo">
          <div class="logo-icon">💪</div>
          <h3 v-show="!collapsed">健身会员中心</h3>
        </div>
        <div class="custom-menu">
          <div
            v-for="item in menuOptions"
            :key="item.key"
            :class="['menu-item', activeKey === item.key ? 'active' : '']"
            @click="handleMenuSelect(item.key)"
          >
            <n-icon :size="20" class="menu-icon">
              <component :is="item.iconComponent" />
            </n-icon>
            <span class="menu-label" v-show="!collapsed">{{ item.label }}</span>
          </div>
        </div>
      </n-layout-sider>
      <n-layout class="main-layout">
        <n-layout-header bordered class="header">
          <div class="header-content">
            <span class="page-title">{{ currentTitle }}</span>
            <div class="header-actions">
              <n-tag type="warning" size="large" round class="points-badge">
                <template #icon>
                  <n-icon :component="DiamondOutline" />
                </template>
                580 积分
              </n-tag>
              <n-dropdown :options="userOptions" @select="handleUserSelect">
                <n-button text class="user-menu">
                  <img
                    v-if="userAvatar"
                    :src="userAvatar"
                    class="user-avatar-img"
                    @error="$event.target.style.display='none'"
                  />
                  <n-avatar
                    v-else
                    round
                    :size="36"
                    class="user-avatar"
                  >{{ usernameInitial }}</n-avatar>
                  <span class="username">{{ username || '用户' }}</span>
                </n-button>
              </n-dropdown>
            </div>
          </div>
        </n-layout-header>
        <n-layout-content class="content">
          <router-view />
        </n-layout-content>
      </n-layout>
    </n-layout>

    <!-- 移动端内容区 -->
    <div v-else class="mobile-content">
      <router-view />
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted, h } from 'vue'
import { useMessage } from 'naive-ui'
import { useRoute, useRouter } from 'vue-router'
import { NIcon, NAvatar } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'
import {
  CalendarOutline,
  DocumentTextOutline,
  ConstructOutline,
  CardOutline,
  PeopleOutline,
  ChatbubbleOutline,
  GiftOutline,
  DiamondOutline,
  MenuOutline,
  CloseOutline,
  LogOutOutline,
  PersonOutline,
  TimeOutline,
  GlobeOutline,
  MoonOutline,
  SunnyOutline
} from '@vicons/ionicons5'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const message = useMessage()
const showMobileMenu = ref(false)
const collapsed = ref(false)
const isMobile = ref(false)
const sidebarWidth = ref(220)

// 使用计算属性获取用户头像和用户名，确保响应式
const userAvatar = computed(() => authStore.userInfo?.avatar || '')
const username = computed(() => authStore.userInfo?.username || '')
const usernameInitial = computed(() => username.value ? username.value.charAt(0) : '用')

// 检测屏幕尺寸
function checkScreenSize() {
  const width = window.innerWidth
  isMobile.value = width < 768
  
  if (width >= 1024) {
    sidebarWidth.value = 220
    collapsed.value = false
  } else if (width >= 900) {
    sidebarWidth.value = 200
    collapsed.value = false
  } else if (width >= 768) {
    sidebarWidth.value = 180
    collapsed.value = false
  }
}

onMounted(() => {
  checkScreenSize()
  window.addEventListener('resize', checkScreenSize)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkScreenSize)
})

const activeKey = computed(() => route.path)
const currentTitle = computed(() => route.meta.title || '会员中心')

const menuOptions = [
  { label: '在线购卡', key: '/member/cards', iconComponent: CardOutline },
  { label: '我的教练', key: '/member/coach', iconComponent: PeopleOutline },
  { label: '健小助', key: '/member/assistant', iconComponent: ChatbubbleOutline },
  { label: '积分商城', key: '/member/store', iconComponent: GiftOutline },
  { label: '我的预约', key: '/member/bookings', iconComponent: CalendarOutline },
  { label: '健身计划', key: '/member/plans', iconComponent: DocumentTextOutline },
  { label: '我的报修', key: '/member/repairs', iconComponent: ConstructOutline }
]

const isDarkMode = ref(false)

const userOptions = computed(() => [
  {
    label: '用户中心',
    key: 'profile',
    icon: () => h(NIcon, null, { default: () => h(PersonOutline) })
  },
  {
    label: '更新日志',
    key: 'changelog',
    icon: () => h(NIcon, null, { default: () => h(TimeOutline) })
  },
  {
    label: '访问官网',
    key: 'website',
    icon: () => h(NIcon, null, { default: () => h(GlobeOutline) })
  },
  {
    label: isDarkMode.value ? '切换为浅色模式' : '切换为深色模式',
    key: 'toggleTheme',
    icon: () => h(NIcon, null, { default: () => h(isDarkMode.value ? SunnyOutline : MoonOutline) })
  },
  {
    type: 'divider',
    key: 'd1'
  },
  {
    label: '退出登录',
    key: 'logout',
    icon: () => h(NIcon, null, { default: () => h(LogOutOutline) })
  }
])

function handleMenuSelect(key) {
  router.push(key)
}

function handleMobileMenuSelect(key) {
  showMobileMenu.value = false
  router.push(key)
}

function handleUserSelect(key) {
  if (key === 'profile') {
    router.push('/member/profile')
  } else if (key === 'changelog') {
    message.info('当前版本: v1.0.0')
  } else if (key === 'website') {
    router.push('/')
  } else if (key === 'toggleTheme') {
    isDarkMode.value = !isDarkMode.value
    message.success(isDarkMode.value ? '已切换到深色模式' : '已切换到浅色模式')
  } else if (key === 'logout') {
    authStore.logout()
  }
}

function handleLogout() {
  authStore.logout()
}
</script>

<style scoped>
/* ==================== 基础布局 ==================== */
.member-layout {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.full-height {
  height: 100%;
  flex: 1;
}

/* ==================== 移动端头部 ==================== */
.mobile-header {
  height: 56px;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  flex-shrink: 0;
  position: relative;
  z-index: 100;
}

.mobile-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  color: white;
  font-weight: 600;
  font-size: 16px;
}

.mobile-logo .logo-icon {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #FF6B35, #FF8C61);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.mobile-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.mobile-points {
  font-weight: 600;
  background: linear-gradient(135deg, #FFD93D, #FFC107) !important;
  color: #1A1A2E !important;
  border: none !important;
}

.menu-toggle {
  color: white;
  padding: 8px;
}

/* ==================== 移动端菜单抽屉 ==================== */
.mobile-menu-overlay {
  position: fixed;
  top: 56px;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 98;
}

.mobile-menu {
  position: fixed;
  top: 56px;
  left: 0;
  right: 0;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  z-index: 99;
  transform: translateY(-100%);
  opacity: 0;
  transition: all 0.3s ease;
  max-height: calc(100vh - 56px);
  overflow-y: auto;
}

.mobile-menu.open {
  transform: translateY(0);
  opacity: 1;
}

.mobile-menu-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
  color: white;
  font-weight: 600;
}

.mobile-menu-header button {
  color: white;
}

.mobile-menu-items {
  padding: 12px 16px;
}

.mobile-menu-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 16px;
  margin: 6px 0;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #FF6B35;
  font-size: 15px;
}

.mobile-menu-item:hover {
  background: rgba(255, 107, 53, 0.1);
  color: #FF8C61;
}

.mobile-menu-item.active {
  background: linear-gradient(90deg, #FF6B35, #E55A2B);
  color: white;
}

.mobile-menu-footer {
  padding: 16px;
  border-top: 1px solid rgba(255,255,255,0.1);
}

/* ==================== 移动端内容区 ==================== */
.mobile-content {
  flex: 1;
  overflow: auto;
  background: #FAFBFC;
  padding: 16px;
}

/* ==================== 桌面端侧边栏 ==================== */
.sidebar {
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  flex-shrink: 0;
}

.sidebar :deep(.n-layout-sider-scroll-container) {
  background: transparent !important;
}

.logo {
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
  padding: 0 20px;
  flex-shrink: 0;
}

.logo-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #FF6B35, #FF8C61);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.logo h3 {
  margin: 0;
  color: white;
  font-size: 16px;
  font-weight: 600;
  white-space: nowrap;
}

/* ==================== 自定义菜单样式 ==================== */
.custom-menu {
  padding: 12px 8px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  margin: 4px 0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #FF6B35;
}

.menu-item:hover {
  background: rgba(255, 107, 53, 0.1);
  color: #FF8C61;
}

.menu-item.active {
  background: linear-gradient(90deg, #FF6B35, #E55A2B);
  color: white;
}

.menu-item.active:hover {
  background: linear-gradient(90deg, #FF6B35, #E55A2B);
  color: white;
}

.menu-icon {
  flex-shrink: 0;
}

.menu-label {
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
}

/* ==================== 主内容区 ==================== */
.main-layout {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 0;
}

.header {
  height: 70px;
  padding: 0 32px;
  display: flex;
  align-items: center;
  background: white;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  flex-shrink: 0;
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1A1A2E;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.points-badge {
  font-weight: 600;
  background: linear-gradient(135deg, #FFD93D, #FFC107) !important;
  color: #1A1A2E !important;
  border: none !important;
}

.user-menu {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 12px;
  border-radius: 10px;
  transition: background 0.3s;
}

.user-menu:hover {
  background: #f5f5f5;
}

.user-avatar {
  background: linear-gradient(135deg, #2EC4B6, #06D6A0);
  color: white;
  font-weight: 600;
}

.user-avatar-img {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
}

.username {
  font-weight: 500;
  color: #1A1A2E;
}

.content {
  padding: 24px 32px;
  background: #FAFBFC;
  flex: 1;
  overflow: auto;
  min-height: 0;
}

/* ==================== 响应式适配 ==================== */

/* 平板端 (768px - 1024px) */
@media (max-width: 1024px) {
  .content {
    padding: 20px 24px;
  }
  
  .page-title {
    font-size: 22px;
  }
  
  .header {
    padding: 0 24px;
  }
}

/* 小平板端 (768px - 900px) */
@media (max-width: 900px) {
  .logo h3 {
    font-size: 14px;
  }
  
  .menu-label {
    font-size: 13px;
  }
  
  .menu-item {
    padding: 10px 12px;
  }
}

/* 大屏手机 (480px - 767px) */
@media (min-width: 480px) and (max-width: 767px) {
  .mobile-menu-item {
    font-size: 16px;
    padding: 16px 20px;
  }
  
  .mobile-content {
    padding: 20px;
  }
}

/* 小屏手机 (< 480px) */
@media (max-width: 479px) {
  .mobile-logo span {
    font-size: 14px;
  }
  
  .mobile-menu-item {
    font-size: 14px;
    padding: 12px 14px;
  }
  
  .mobile-content {
    padding: 12px;
  }
}

/* 超小屏手机 (< 360px) */
@media (max-width: 360px) {
  .mobile-header {
    padding: 0 12px;
  }
  
  .mobile-logo span {
    display: none;
  }
}
</style>
