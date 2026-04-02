<template>
  <div class="equipments-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-bg"></div>
      <div class="header-content">
        <div class="header-breadcrumb">
          <router-link to="/">首页</router-link>
          <span class="breadcrumb-separator">/</span>
          <span class="current">全部器械</span>
        </div>
        <h1 class="header-title">探索专业器械</h1>
        <p class="header-subtitle">国际顶级健身设备，为您提供专业级的训练体验</p>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-section">
      <div class="filter-container">
        <!-- 搜索框 -->
        <div class="search-box">
          <svg class="search-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/>
            <path d="m21 21-4.35-4.35"/>
          </svg>
          <input
            v-model="filters.keyword"
            type="text"
            placeholder="搜索器械名称..."
            class="search-input"
            @input="handleSearchInput"
          />
          <button v-if="filters.keyword" class="clear-btn" @click="clearSearch">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>

        <!-- 类型筛选 -->
        <div class="filter-group">
          <span class="filter-label">器械类型：</span>
          <div class="filter-tabs">
            <button
              v-for="type in typeOptions"
              :key="type.value"
              :class="['filter-tab', { active: filters.typeCode === type.value }]"
              @click="selectType(type.value)"
            >
              {{ type.label }}
            </button>
          </div>
        </div>

        <!-- 状态筛选 -->
        <div class="filter-group">
          <span class="filter-label">使用状态：</span>
          <div class="filter-tabs">
            <button
              v-for="status in statusOptions"
              :key="status.value"
              :class="['filter-tab', { active: filters.status === status.value }]"
              @click="selectStatus(status.value)"
            >
              {{ status.label }}
            </button>
          </div>
        </div>

        <!-- 排序方式 -->
        <div class="sort-group">
          <span class="sort-label">排序：</span>
          <div class="sort-select-wrapper">
            <select v-model="filters.sortBy" class="sort-select" @change="handleSortChange">
              <option v-for="opt in sortOptions" :key="opt.value" :value="opt.value">
                {{ opt.label }}
              </option>
            </select>
            <svg class="sort-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="6 9 12 15 18 9"/>
            </svg>
          </div>
        </div>

        <!-- 重置筛选按钮 -->
        <button class="reset-filters-btn" @click="resetFilters" title="重置所有筛选条件">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/>
            <path d="M3 3v5h5"/>
          </svg>
          重置
        </button>
      </div>
    </div>

    <!-- 器械列表区域 -->
    <div class="equipments-section">
      <div class="equipments-container">
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-state">
          <div class="loading-spinner"></div>
          <p>正在加载器械...</p>
        </div>

        <!-- 空状态 -->
        <div v-else-if="equipments.length === 0" class="empty-state">
          <div class="empty-icon">
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
              <line x1="9" y1="9" x2="15" y2="15"/>
              <line x1="15" y1="9" x2="9" y2="15"/>
            </svg>
          </div>
          <h3>暂无相关器械</h3>
          <p>试试调整筛选条件或搜索其他关键词</p>
          <button class="btn btn-primary" @click="resetFilters">重置筛选</button>
        </div>

        <!-- 器械网格 - 瀑布流布局 -->
        <div v-else class="equipments-grid">
          <div
            v-for="(equipment, index) in equipments"
            :key="equipment.id"
            class="equipment-card"
            :class="getCardSizeClass(index)"
            @click="goToDetail(equipment.id)"
          >
            <!-- 器械封面 -->
            <div class="equipment-image-wrapper">
              <img
                :src="equipment.imageUrl || defaultEquipmentImage"
                :alt="equipment.equipmentName"
                class="equipment-image"
                @error="handleImageError"
              />
              <div class="equipment-badges">
                <span class="badge type-badge" :class="getTypeClass(equipment.typeCode)">
                  {{ equipment.typeName || '其他' }}
                </span>
                <span v-if="equipment.location" class="badge location-badge">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                    <circle cx="12" cy="10" r="3"/>
                  </svg>
                  {{ equipment.location }}
                </span>
              </div>
              <div class="equipment-overlay">
                <div class="overlay-content">
                  <span class="view-text">{{ authStore.isLoggedIn ? '查看详情' : '登录后查看' }}</span>
                  <svg class="view-icon" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="10"/>
                    <line x1="12" y1="16" x2="12" y2="12"/>
                    <line x1="12" y1="8" x2="12.01" y2="8"/>
                  </svg>
                </div>
              </div>
            </div>

            <!-- 器械信息 -->
            <div class="equipment-info">
              <div class="equipment-header">
                <h3 class="equipment-title">{{ equipment.equipmentName }}</h3>
                <div class="equipment-status" :class="getStatusClass(equipment.status)">
                  <span class="status-dot"></span>
                  {{ getStatusLabel(equipment.status) }}
                </div>
              </div>

              <p class="equipment-description">{{ equipment.description || '暂无器械描述' }}</p>

              <div class="equipment-meta">
                <div class="meta-item" v-if="equipment.equipmentNo">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M4 7V4h3M4 17v3h3M20 7V4h-3M20 17v3h-3M9 9h6v6H9z"/>
                  </svg>
                  <span>编号: {{ equipment.equipmentNo }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div v-if="equipments.length > 0 && pagination.totalPages > 1" class="pagination-wrapper">
          <div class="pagination">
            <button
              :disabled="pagination.page === 1"
              class="page-btn prev"
              @click="changePage(pagination.page - 1)"
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="15 18 9 12 15 6"/>
              </svg>
            </button>

            <div class="page-numbers">
              <button
                v-for="page in visiblePages"
                :key="page"
                :class="['page-number', { active: page === pagination.page, ellipsis: page === '...' }]"
                :disabled="page === '...'"
                @click="page !== '...' && changePage(page)"
              >
                {{ page }}
              </button>
            </div>

            <button
              :disabled="pagination.page === pagination.totalPages"
              class="page-btn next"
              @click="changePage(pagination.page + 1)"
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="9 18 15 12 9 6"/>
              </svg>
            </button>
          </div>

          <div class="pagination-info">
            共 {{ pagination.total }} 件器械，第 {{ pagination.page }} / {{ pagination.totalPages }} 页
          </div>
        </div>
      </div>
    </div>

    <!-- 登录模态框 -->
    <LoginModal
      v-model:visible="showLoginModal"
      @login-success="handleLoginSuccess"
      @go-register="goToRegister"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useMessage } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'
import { getEquipmentList, getEquipmentTypes } from '@/api/equipment'
import LoginModal from '@/components/LoginModal.vue'

const router = useRouter()
const route = useRoute()
const message = useMessage()
const authStore = useAuthStore()

// 默认器械图片
const defaultEquipmentImage = 'https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=400&h=300&fit=crop'

// 加载状态
const loading = ref(false)

// 登录模态框显示状态
const showLoginModal = ref(false)

// 器械数据
const equipments = ref([])

// 器械类型列表
const equipmentTypes = ref([])

// 筛选条件
const filters = reactive({
  keyword: '',
  typeCode: '',
  status: '',
  sortBy: 'newest'
})

// 分页信息
const pagination = reactive({
  page: 1,
  pageSize: 12,
  total: 0,
  totalPages: 0
})

// 类型选项（动态从后端获取）
const typeOptions = computed(() => {
  const options = [{ label: '全部', value: '' }]
  equipmentTypes.value.forEach(type => {
    options.push({
      label: type.typeName,
      value: type.typeCode
    })
  })
  return options
})

// 状态选项
const statusOptions = [
  { label: '全部', value: '' },
  { label: '正常使用', value: 1 },
  { label: '维修中', value: 0 },
  { label: '已报废', value: 2 }
]

// 排序选项
const sortOptions = [
  { label: '最新添加', value: 'newest' },
  { label: '名称排序', value: 'name' }
]

// 获取卡片尺寸类名（实现瀑布流效果）
function getCardSizeClass(index) {
  // 根据索引返回不同的卡片尺寸，创建视觉变化
  const pattern = index % 6
  if (pattern === 0 || pattern === 5) return 'card-large'
  if (pattern === 2 || pattern === 3) return 'card-wide'
  return 'card-normal'
}

// 搜索防抖
let searchTimeout = null
function handleSearchInput() {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    pagination.page = 1
    fetchEquipments()
  }, 300)
}

// 清除搜索
function clearSearch() {
  filters.keyword = ''
  pagination.page = 1
  fetchEquipments()
}

// 选择类型
function selectType(value) {
  filters.typeCode = value
  pagination.page = 1
  fetchEquipments()
}

// 选择状态
function selectStatus(value) {
  filters.status = value
  pagination.page = 1
  fetchEquipments()
}

// 排序变化
function handleSortChange() {
  pagination.page = 1
  fetchEquipments()
}

// 重置筛选
function resetFilters() {
  filters.keyword = ''
  filters.typeCode = ''
  filters.status = ''
  filters.sortBy = 'newest'
  pagination.page = 1
  fetchEquipments()
}

// 切换页面
function changePage(page) {
  if (page < 1 || page > pagination.totalPages) return
  pagination.page = page
  fetchEquipments()
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 计算可见页码
const visiblePages = computed(() => {
  const pages = []
  const total = pagination.totalPages
  const current = pagination.page

  if (total <= 7) {
    for (let i = 1; i <= total; i++) pages.push(i)
  } else {
    if (current <= 3) {
      pages.push(1, 2, 3, 4, '...', total)
    } else if (current >= total - 2) {
      pages.push(1, '...', total - 3, total - 2, total - 1, total)
    } else {
      pages.push(1, '...', current - 1, current, current + 1, '...', total)
    }
  }
  return pages
})

// 获取器械类型列表
async function fetchEquipmentTypes() {
  try {
    const res = await getEquipmentTypes()
    if (Array.isArray(res)) {
      equipmentTypes.value = res
    }
  } catch (error) {
    console.error('获取器械类型失败:', error)
  }
}

// 获取器械列表
async function fetchEquipments() {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.page,
      pageSize: pagination.pageSize,
      keyword: filters.keyword || undefined,
      typeCode: filters.typeCode || undefined,
      status: filters.status !== '' ? filters.status : undefined
    }

    const res = await getEquipmentList(params)

    // 根据响应拦截器，res已经是res.data
    if (res && res.records) {
      equipments.value = res.records
      pagination.total = res.total || 0
      pagination.totalPages = res.pages || Math.ceil(res.total / pagination.pageSize)
    } else if (Array.isArray(res)) {
      equipments.value = res
      pagination.total = res.length
      pagination.totalPages = 1
    } else {
      equipments.value = []
      pagination.total = 0
      pagination.totalPages = 0
    }
  } catch (error) {
    console.error('获取器械列表失败:', error)
    message.error('获取器械列表失败，请稍后重试')
    equipments.value = []
  } finally {
    loading.value = false
  }
}

// 获取类型样式类
function getTypeClass(typeCode) {
  const map = {
    'STRENGTH': 'type-strength',
    'CARDIO': 'type-cardio',
    'FREE_WEIGHT': 'type-freeweight',
    'FUNCTIONAL': 'type-functional',
    'YOGA': 'type-yoga'
  }
  return map[typeCode] || 'type-default'
}

// 获取状态样式类
function getStatusClass(status) {
  const map = {
    0: 'status-repair',
    1: 'status-normal',
    2: 'status-scrapped'
  }
  return map[status] || 'status-normal'
}

// 获取状态标签
function getStatusLabel(status) {
  const map = {
    0: '维修中',
    1: '正常使用',
    2: '已报废'
  }
  return map[status] || '未知'
}

// 图片加载失败处理
function handleImageError(event) {
  event.target.src = defaultEquipmentImage
}

// 跳转到详情页
function goToDetail(id) {
  if (!id) {
    message.error('器械信息无效')
    return
  }

  // 检查用户登录状态
  if (authStore.isLoggedIn) {
    // 已登录用户，跳转到器械详情页
    router.push(`/equipments/${id}`)
  } else {
    // 未登录用户，存储器械ID并显示登录模态框
    sessionStorage.setItem('pendingEquipmentId', id)
    showLoginModal.value = true
  }
}

// 处理登录成功
function handleLoginSuccess() {
  message.success('登录成功')
  // 检查是否有待查看的器械
  const pendingEquipmentId = sessionStorage.getItem('pendingEquipmentId')
  if (pendingEquipmentId) {
    // 清除存储的器械ID
    sessionStorage.removeItem('pendingEquipmentId')
    // 跳转到器械详情页
    router.push(`/equipments/${pendingEquipmentId}`)
  }
}

// 跳转到注册页面
function goToRegister() {
  showLoginModal.value = false
  router.push('/register')
}

// 监听路由参数变化
watch(() => route.query, (query) => {
  if (query.typeCode) {
    filters.typeCode = query.typeCode
  }
  if (query.keyword) {
    filters.keyword = query.keyword
  }
}, { immediate: true })

// 页面加载
onMounted(() => {
  fetchEquipmentTypes()
  fetchEquipments()
})
</script>

<style scoped>
/* CSS Variables */
.equipments-page {
  --primary: #FF6B35;
  --primary-light: #FF8C61;
  --primary-dark: #E55A2B;
  --primary-gradient: linear-gradient(135deg, #FF6B35 0%, #FF8C61 100%);
  --bg-dark: #0A0A0F;
  --bg-dark-secondary: #12121A;
  --bg-dark-tertiary: #1A1A25;
  --bg-card: rgba(255, 255, 255, 0.03);
  --bg-card-hover: rgba(255, 255, 255, 0.06);
  --text-primary: #FFFFFF;
  --text-secondary: rgba(255, 255, 255, 0.75);
  --text-muted: rgba(255, 255, 255, 0.5);
  --border-color: rgba(255, 255, 255, 0.08);
  --shadow-sm: 0 2px 8px rgba(0, 0, 0, 0.2);
  --shadow-md: 0 8px 30px rgba(0, 0, 0, 0.3);
  --shadow-lg: 0 20px 60px rgba(0, 0, 0, 0.4);
  --radius-sm: 8px;
  --radius-md: 16px;
  --radius-lg: 24px;
  --transition-fast: 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  --transition-normal: 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  min-height: 100vh;
  background: var(--bg-dark);
  color: var(--text-primary);
}

/* Page Header */
.page-header {
  position: relative;
  padding: 120px 0 60px;
  overflow: hidden;
}

.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    radial-gradient(ellipse at 20% 80%, rgba(255, 107, 53, 0.15) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 20%, rgba(168, 85, 247, 0.1) 0%, transparent 50%),
    linear-gradient(180deg, var(--bg-dark-secondary) 0%, var(--bg-dark) 100%);
  z-index: 0;
}

.header-content {
  position: relative;
  z-index: 1;
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 40px;
}

.header-breadcrumb {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  font-size: 14px;
}

.header-breadcrumb a {
  color: var(--text-muted);
  text-decoration: none;
  transition: color var(--transition-fast);
}

.header-breadcrumb a:hover {
  color: var(--primary);
}

.breadcrumb-separator {
  color: var(--text-muted);
}

.current {
  color: var(--text-primary);
}

.header-title {
  font-size: 48px;
  font-weight: 900;
  margin-bottom: 16px;
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C61 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.header-subtitle {
  font-size: 18px;
  color: var(--text-secondary);
  max-width: 500px;
}

/* Filter Section */
.filter-section {
  background: var(--bg-dark-secondary);
  border-bottom: 1px solid var(--border-color);
  padding: 24px 0;
  position: sticky;
  top: 0;
  z-index: 100;
  backdrop-filter: blur(20px);
}

.filter-container {
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 40px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 24px;
}

/* Search Box */
.search-box {
  position: relative;
  flex: 1;
  min-width: 280px;
  max-width: 400px;
}

.search-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-muted);
  pointer-events: none;
}

.search-input {
  width: 100%;
  padding: 14px 44px 14px 48px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 100px;
  font-size: 15px;
  color: var(--text-primary);
  outline: none;
  transition: all var(--transition-fast);
}

.search-input::placeholder {
  color: var(--text-muted);
}

.search-input:focus {
  border-color: var(--primary);
  background: var(--bg-card-hover);
  box-shadow: 0 0 0 3px rgba(255, 107, 53, 0.1);
}

.clear-btn {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: 50%;
  color: var(--text-muted);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.clear-btn:hover {
  background: rgba(255, 107, 53, 0.2);
  color: var(--primary);
}

/* Filter Groups */
.filter-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.filter-label {
  font-size: 14px;
  color: var(--text-muted);
  white-space: nowrap;
}

.filter-tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.filter-tab {
  padding: 8px 16px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 100px;
  font-size: 14px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);
  white-space: nowrap;
}

.filter-tab:hover {
  background: var(--bg-card-hover);
  border-color: rgba(255, 107, 53, 0.3);
  color: var(--text-primary);
}

.filter-tab.active {
  background: var(--primary-gradient);
  border-color: transparent;
  color: var(--text-primary);
}

/* Sort Group */
.sort-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.sort-label {
  font-size: 14px;
  color: var(--text-muted);
  font-weight: 500;
}

.sort-select-wrapper {
  position: relative;
  min-width: 150px;
}

.sort-select {
  width: 100%;
  padding: 12px 40px 12px 20px;
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.15) 0%, rgba(255, 140, 97, 0.08) 100%);
  border: 2px solid rgba(255, 107, 53, 0.4);
  border-radius: 100px;
  font-size: 14px;
  font-weight: 600;
  color: var(--primary);
  cursor: pointer;
  outline: none;
  appearance: none;
  transition: all var(--transition-fast);
  box-shadow: 
    0 2px 8px rgba(255, 107, 53, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.sort-select:hover {
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.25) 0%, rgba(255, 140, 97, 0.15) 100%);
  border-color: var(--primary);
  box-shadow: 
    0 4px 20px rgba(255, 107, 53, 0.35),
    inset 0 1px 0 rgba(255, 255, 255, 0.15);
  transform: translateY(-2px);
}

.sort-select:focus {
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.3) 0%, rgba(255, 140, 97, 0.2) 100%);
  border-color: var(--primary);
  box-shadow: 
    0 0 0 4px rgba(255, 107, 53, 0.15),
    0 4px 20px rgba(255, 107, 53, 0.3);
}

/* 下拉选项样式 */
.sort-select option {
  background: var(--bg-dark-secondary);
  color: var(--text-primary);
  padding: 14px 18px;
  font-size: 14px;
  font-weight: 500;
}

.sort-select option:hover,
.sort-select option:focus,
.sort-select option:checked {
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C61 100%) !important;
  color: white;
}

.sort-icon {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--primary);
  pointer-events: none;
  transition: all var(--transition-fast);
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.2));
}

.sort-select-wrapper:hover .sort-icon {
  transform: translateY(-50%) rotate(180deg);
  color: var(--primary-light);
}

/* 重置筛选按钮 */
.reset-filters-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
  background: transparent;
  border: 1px solid var(--border-color);
  border-radius: 100px;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-muted);
  cursor: pointer;
  transition: all var(--transition-fast);
  margin-left: auto;
}

.reset-filters-btn:hover {
  background: rgba(255, 107, 53, 0.1);
  border-color: rgba(255, 107, 53, 0.4);
  color: var(--primary);
  transform: translateY(-1px);
}

.reset-filters-btn svg {
  transition: transform var(--transition-fast);
}

.reset-filters-btn:hover svg {
  transform: rotate(-180deg);
}

/* Equipments Section */
.equipments-section {
  padding: 40px 0 80px;
}

.equipments-container {
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 40px;
}

/* Loading State */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100px 20px;
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 3px solid rgba(255, 107, 53, 0.2);
  border-top-color: var(--primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-state p {
  color: var(--text-secondary);
  font-size: 16px;
}

/* Empty State */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100px 20px;
  text-align: center;
}

.empty-icon {
  color: var(--text-muted);
  margin-bottom: 24px;
  opacity: 0.5;
}

.empty-state h3 {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 8px;
}

.empty-state p {
  color: var(--text-secondary);
  margin-bottom: 24px;
}

/* Equipments Grid - 瀑布流布局 */
.equipments-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  grid-auto-rows: 280px;
  gap: 24px;
}

/* Equipment Card */
.equipment-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-normal);
  display: flex;
  flex-direction: column;
}

.equipment-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-lg);
  border-color: rgba(255, 107, 53, 0.2);
}

/* 卡片尺寸变体 */
.equipment-card.card-large {
  grid-row: span 2;
}

.equipment-card.card-wide {
  grid-column: span 2;
}

/* Equipment Image */
.equipment-image-wrapper {
  position: relative;
  flex: 1;
  overflow: hidden;
}

.equipment-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.equipment-card:hover .equipment-image {
  transform: scale(1.05);
}

.equipment-badges {
  position: absolute;
  top: 12px;
  left: 12px;
  right: 12px;
  display: flex;
  justify-content: space-between;
  gap: 8px;
}

.badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border-radius: 100px;
  font-size: 12px;
  font-weight: 600;
  backdrop-filter: blur(10px);
}

.type-badge {
  background: rgba(0, 0, 0, 0.6);
  color: var(--text-primary);
}

.type-strength {
  background: rgba(255, 107, 53, 0.9);
  color: #fff;
}

.type-cardio {
  background: rgba(46, 196, 182, 0.9);
  color: #fff;
}

.type-freeweight {
  background: rgba(168, 85, 247, 0.9);
  color: #fff;
}

.type-functional {
  background: rgba(255, 193, 7, 0.9);
  color: #000;
}

.type-yoga {
  background: rgba(59, 130, 246, 0.9);
  color: #fff;
}

.type-default {
  background: rgba(107, 114, 128, 0.9);
  color: #fff;
}

.location-badge {
  background: rgba(255, 107, 53, 0.9);
  color: #fff;
}

.equipment-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity var(--transition-fast);
}

.equipment-card:hover .equipment-overlay {
  opacity: 1;
}

.overlay-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 24px;
  background: var(--primary-gradient);
  border-radius: 100px;
  transform: translateY(10px);
  transition: all var(--transition-fast);
}

.equipment-card:hover .overlay-content {
  transform: translateY(0);
}

.view-text {
  font-size: 16px;
  font-weight: 600;
  color: white;
}

.view-icon {
  color: white;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.8;
    transform: scale(1.1);
  }
}

/* Equipment Info */
.equipment-info {
  padding: 20px;
  background: var(--bg-card);
}

.equipment-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
}

.equipment-title {
  font-size: 18px;
  font-weight: 700;
  line-height: 1.4;
  flex: 1;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.equipment-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
  padding: 4px 10px;
  border-radius: 100px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-normal {
  background: rgba(34, 197, 94, 0.2);
  color: #22C55E;
}

.status-normal .status-dot {
  background: #22C55E;
}

.status-repair {
  background: rgba(255, 193, 7, 0.2);
  color: #FFC107;
}

.status-repair .status-dot {
  background: #FFC107;
}

.status-scrapped {
  background: rgba(239, 68, 68, 0.2);
  color: #EF4444;
}

.status-scrapped .status-dot {
  background: #EF4444;
}

.equipment-description {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.equipment-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-muted);
}

/* Pagination */
.pagination-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  margin-top: 48px;
}

.pagination {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.page-btn:hover:not(:disabled) {
  background: var(--bg-card-hover);
  border-color: var(--primary);
  color: var(--primary);
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-numbers {
  display: flex;
  gap: 8px;
}

.page-number {
  min-width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.page-number:hover:not(:disabled):not(.active):not(.ellipsis) {
  background: var(--bg-card-hover);
  border-color: var(--primary);
  color: var(--primary);
}

.page-number.active {
  background: var(--primary-gradient);
  border-color: transparent;
  color: var(--text-primary);
}

.page-number.ellipsis {
  background: transparent;
  border: none;
  cursor: default;
}

.pagination-info {
  font-size: 14px;
  color: var(--text-muted);
}

/* Button */
.btn {
  padding: 12px 24px;
  border-radius: var(--radius-sm);
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
  transition: all var(--transition-fast);
  cursor: pointer;
  border: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.btn-primary {
  background: var(--primary-gradient);
  color: var(--text-primary);
  box-shadow: 0 4px 15px rgba(255, 107, 53, 0.3);
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(255, 107, 53, 0.4);
}

/* Responsive */
@media (max-width: 1400px) {
  .equipments-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  
  .equipment-card.card-wide {
    grid-column: span 2;
  }
}

@media (max-width: 1200px) {
  .filter-container {
    flex-direction: column;
    align-items: stretch;
  }

  .search-box {
    max-width: 100%;
  }

  .sort-group {
    margin-left: 0;
  }
}

@media (max-width: 992px) {
  .equipments-grid {
    grid-template-columns: repeat(2, 1fr);
    grid-auto-rows: 250px;
  }
  
  .equipment-card.card-wide {
    grid-column: span 2;
  }

  .header-title {
    font-size: 36px;
  }
}

@media (max-width: 768px) {
  .page-header {
    padding: 100px 0 40px;
  }

  .header-content,
  .filter-container,
  .equipments-container {
    padding: 0 20px;
  }

  .header-title {
    font-size: 28px;
  }

  .filter-group {
    flex-direction: column;
    align-items: flex-start;
  }

  .filter-tabs {
    width: 100%;
    overflow-x: auto;
    flex-wrap: nowrap;
    padding-bottom: 8px;
    -webkit-overflow-scrolling: touch;
  }

  .equipments-grid {
    grid-template-columns: 1fr;
    grid-auto-rows: auto;
  }
  
  .equipment-card.card-large,
  .equipment-card.card-wide {
    grid-row: span 1;
    grid-column: span 1;
  }

  .equipment-image-wrapper {
    height: 200px;
  }
}

@media (max-width: 480px) {
  .header-title {
    font-size: 24px;
  }

  .pagination {
    flex-wrap: wrap;
    justify-content: center;
  }
}
</style>
