<template>
  <el-container class="admin-layout">
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <h2>智能健身房</h2>
        <span>管理后台</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#001529"
        text-color="#fff"
        active-text-color="#1890ff"
      >
        <el-menu-item index="/admin">
          <el-icon><DataAnalysis /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-sub-menu index="/admin/courses">
          <template #title>
            <el-icon><Calendar /></el-icon>
            <span>课程管理</span>
          </template>
          <el-menu-item index="/admin/courses">
            <el-icon><Reading /></el-icon>
            <span>公开课管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/video-courses">
            <el-icon><VideoCamera /></el-icon>
            <span>视频课程管理</span>
          </el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/admin/equipment">
          <el-icon><Box /></el-icon>
          <span>器材管理</span>
        </el-menu-item>
        <el-sub-menu index="/admin/business">
          <template #title>
            <el-icon><ShoppingBag /></el-icon>
            <span>业务管理</span>
          </template>
          <el-menu-item index="/admin/membership-cards">
            <el-icon><CreditCard /></el-icon>
            <span>会员卡管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/products">
            <el-icon><Goods /></el-icon>
            <span>商品管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/order-management">
            <el-icon><ShoppingCart /></el-icon>
            <span>订单管理</span>
          </el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/admin/content-management">
          <el-icon><Document /></el-icon>
          <span>内容管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/analysis">
          <el-icon><TrendCharts /></el-icon>
          <span>数据分析</span>
        </el-menu-item>
        <el-sub-menu index="/admin/knowledge">
          <template #title>
            <el-icon><Collection /></el-icon>
            <span>知识库管理</span>
          </template>
          <el-menu-item index="/admin/knowledge-base">
            <el-icon><Document /></el-icon>
            <span>文档管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/knowledge-categories">
            <el-icon><Folder /></el-icon>
            <span>分类管理</span>
          </el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/admin/repairs">
          <el-icon><Tools /></el-icon>
          <span>报修管理</span>
        </el-menu-item>
        <el-sub-menu index="/admin/system">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统设置</span>
          </template>
          <el-menu-item index="/admin/dict-management">
            <el-icon><CollectionTag /></el-icon>
            <span>数据字典</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/admin' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown>
            <span class="user-info">
              <img
                v-if="userAvatar"
                :src="userAvatar"
                class="user-avatar-img"
                @error="$event.target.style.display='none'"
              />
              <el-avatar
                v-else
                :size="32"
              >{{ usernameInitial }}</el-avatar>
              <span>{{ username }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="goHome">返回首页</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserInfo } from '@/composables/useUserInfo'
import {
  DataAnalysis, User, Calendar, Box, Tools, TrendCharts,
  CreditCard, Document, ShoppingCart, ShoppingBag, Goods, Collection, Folder,
  Setting, CollectionTag, VideoCamera, Reading
} from '@element-plus/icons-vue'

const route = useRoute()
const { userAvatar, username, usernameInitial, goHome, handleLogout } = useUserInfo()

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta.title || '仪表盘')
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

.sidebar {
  background: #001529;
}

.logo {
  height: 64px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo h2 {
  margin: 0;
  font-size: 18px;
}

.logo span {
  font-size: 12px;
  opacity: 0.7;
}

.el-menu {
  border-right: none;
}

.header {
  background: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.user-avatar-img {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}

.main {
  background: #f0f2f5;
  padding: 20px;
}
</style>
