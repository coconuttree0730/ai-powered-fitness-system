<template>
  <div class="courses-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-bg"></div>
      <div class="header-content">
        <div class="header-breadcrumb">
          <router-link to="/">首页</router-link>
          <span class="breadcrumb-separator">/</span>
          <span class="current">全部课程</span>
        </div>
        <h1 class="header-title">探索精品课程</h1>
        <p class="header-subtitle">从入门到专业，找到最适合你的健身课程</p>
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
            placeholder="搜索课程名称、教练..."
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

        <!-- 分类筛选 -->
        <div class="filter-group">
          <span class="filter-label">课程分类：</span>
          <div class="filter-tabs">
            <button
              v-for="cat in categoryOptions"
              :key="cat.value"
              :class="['filter-tab', { active: filters.category === cat.value }]"
              @click="selectCategory(cat.value)"
            >
              {{ cat.label }}
            </button>
          </div>
        </div>

        <!-- 难度筛选 -->
        <div class="filter-group">
          <span class="filter-label">难度等级：</span>
          <div class="filter-tabs">
            <button
              v-for="level in levelOptions"
              :key="level.value"
              :class="['filter-tab', { active: filters.level === level.value }]"
              @click="selectLevel(level.value)"
            >
              {{ level.label }}
            </button>
          </div>
        </div>

        <!-- 时长筛选 -->
        <div class="filter-group">
          <span class="filter-label">课程时长：</span>
          <div class="filter-tabs">
            <button
              v-for="dur in durationOptions"
              :key="dur.value"
              :class="['filter-tab', { active: filters.duration === dur.value }]"
              @click="selectDuration(dur.value)"
            >
              {{ dur.label }}
            </button>
          </div>
        </div>

        <!-- 卡路里筛选 -->
        <div class="filter-group">
          <span class="filter-label">消耗热量：</span>
          <div class="filter-tabs">
            <button
              v-for="cal in caloriesOptions"
              :key="cal.value"
              :class="['filter-tab', { active: filters.calories === cal.value }]"
              @click="selectCalories(cal.value)"
            >
              {{ cal.label }}
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

    <!-- 课程列表区域 -->
    <div class="courses-section">
      <div class="courses-container">
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-state">
          <div class="loading-spinner"></div>
          <p>正在加载课程...</p>
        </div>

        <!-- 空状态 -->
        <div v-else-if="courses.length === 0" class="empty-state">
          <div class="empty-icon">
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
              <line x1="9" y1="9" x2="15" y2="15"/>
              <line x1="15" y1="9" x2="9" y2="15"/>
            </svg>
          </div>
          <h3>暂无相关课程</h3>
          <p>试试调整筛选条件或搜索其他关键词</p>
          <button class="btn btn-primary" @click="resetFilters">重置筛选</button>
        </div>

        <!-- 课程网格 -->
        <div v-else class="courses-grid">
          <div
            v-for="course in courses"
            :key="course.id"
            class="course-card"
            @click="goToDetail(course.id)"
          >
            <!-- 课程封面 -->
            <div class="course-image-wrapper">
              <img
                :src="course.imageUrl || defaultCourseImage"
                :alt="course.courseName"
                class="course-image"
                @error="handleImageError"
              />
              <div class="course-badges">
                <span class="badge level-badge" :class="getLevelClass(course.level)">
                  {{ course.level || '初级' }}
                </span>
                <span v-if="course.duration" class="badge duration-badge">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="10"/>
                    <polyline points="12 6 12 12 16 14"/>
                  </svg>
                  {{ course.duration }}分钟
                </span>
              </div>
              <div class="course-overlay">
                <button class="view-btn">查看详情</button>
              </div>
            </div>

            <!-- 课程信息 -->
            <div class="course-info">
              <div class="course-header">
                <h3 class="course-title">{{ course.courseName }}</h3>
                <div class="course-rating" v-if="course.rating">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor" stroke="none">
                    <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                  </svg>
                  <span>{{ course.rating }}</span>
                </div>
              </div>

              <p class="course-description">{{ course.description || '暂无课程描述' }}</p>

              <div class="course-meta">
                <div class="meta-item">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                    <circle cx="12" cy="7" r="4"/>
                  </svg>
                  <span>{{ course.coachName || '专业教练' }}</span>
                </div>
                <div class="meta-item" v-if="course.calories">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M8.5 14.5A2.5 2.5 0 0 0 11 12c0-1.38-.5-2-1-3-1.072-2.143-2.072-2.143-2.072-2.143-1.072 2.143.214 4.286 1.286 6.429 1.072 2.143 2.858 3.214 4.286 3.214 2.858 0 4.286-2.143 4.286-4.286 0-2.858-2.143-5.715-5.715-7.144C9.858 3.214 8.5 5.5 8.5 8.5c0 2.5 1.5 4 2 6-.5 2-2 3-2 3"/>
                  </svg>
                  <span>{{ course.calories }}</span>
                </div>
              </div>

              <div class="course-footer">
                <div class="course-stats">
                  <span class="stat">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                      <circle cx="9" cy="7" r="4"/>
                      <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                      <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
                    </svg>
                    {{ course.studentCount || 0 }}人已学
                  </span>
                </div>
                <div class="course-category-tag" :class="getCategoryClass(course.category)">
                  {{ getCategoryLabel(course.category) }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div v-if="courses.length > 0 && pagination.totalPages > 1" class="pagination-wrapper">
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
            共 {{ pagination.total }} 门课程，第 {{ pagination.page }} / {{ pagination.totalPages }} 页
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
import { getPublicCourses, getCourseCategories } from '@/api/course'
import LoginModal from '@/components/LoginModal.vue'

const router = useRouter()
const route = useRoute()
const message = useMessage()
const authStore = useAuthStore()

// 默认课程图片
const defaultCourseImage = 'https://images.unsplash.com/photo-1571902943202-507ec2618e8f?w=400&h=300&fit=crop'

// 加载状态
const loading = ref(false)

// 登录模态框显示状态
const showLoginModal = ref(false)

// 课程数据
const courses = ref([])

// 筛选条件
const filters = reactive({
  keyword: '',
  category: '',
  level: '',
  duration: '',
  calories: '',
  sortBy: 'newest'
})

// 分页信息
const pagination = reactive({
  page: 1,
  pageSize: 12,
  total: 0,
  totalPages: 0
})

// 分类选项（与数据库中的值保持一致）
const categoryOptions = [
  { label: '全部', value: '' },
  { label: '瑜伽普拉提', value: '瑜伽普拉提' },
  { label: '有氧燃脂', value: '有氧燃脂' },
  { label: '力量训练', value: '力量训练' },
  { label: '舞蹈操课', value: '舞蹈操课' },
  { label: '拳击格斗', value: '拳击格斗' },
  { label: '康复体态', value: '康复体态' }
]

// 难度选项（与数据库中的值保持一致）
const levelOptions = [
  { label: '全部', value: '' },
  { label: '初级', value: '初级' },
  { label: '中级', value: '中级' },
  { label: '高级', value: '高级' }
]

// 时长选项
const durationOptions = [
  { label: '全部', value: '' },
  { label: '30分钟内', value: '0-30' },
  { label: '30-60分钟', value: '30-60' },
  { label: '60分钟以上', value: '60-999' }
]

// 卡路里选项
const caloriesOptions = [
  { label: '全部', value: '' },
  { label: '200以下', value: '0-200' },
  { label: '200-400', value: '200-400' },
  { label: '400-600', value: '400-600' },
  { label: '600以上', value: '600-9999' }
]

// 排序选项
const sortOptions = [
  { label: '最新发布', value: 'newest' },
  { label: '最受欢迎', value: 'popular' },
  { label: '时长最短', value: 'duration_asc' },
  { label: '时长最长', value: 'duration_desc' }
]

// 搜索防抖
let searchTimeout = null
function handleSearchInput() {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    pagination.page = 1
    fetchCourses()
  }, 300)
}

// 清除搜索
function clearSearch() {
  filters.keyword = ''
  pagination.page = 1
  fetchCourses()
}

// 选择分类
function selectCategory(value) {
  filters.category = value
  pagination.page = 1
  fetchCourses()
}

// 选择难度
function selectLevel(value) {
  filters.level = value
  pagination.page = 1
  fetchCourses()
}

// 选择时长
function selectDuration(value) {
  filters.duration = value
  pagination.page = 1
  fetchCourses()
}

// 选择卡路里
function selectCalories(value) {
  filters.calories = value
  pagination.page = 1
  fetchCourses()
}

// 排序变化
function handleSortChange() {
  pagination.page = 1
  fetchCourses()
}

// 重置筛选
function resetFilters() {
  filters.keyword = ''
  filters.category = ''
  filters.level = ''
  filters.duration = ''
  filters.calories = ''
  filters.sortBy = 'newest'
  pagination.page = 1
  fetchCourses()
}

// 切换页面
function changePage(page) {
  if (page < 1 || page > pagination.totalPages) return
  pagination.page = page
  fetchCourses()
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

// 解析范围参数
function parseRange(value) {
  if (!value) return null
  const [min, max] = value.split('-').map(Number)
  return { min, max }
}

// 获取课程列表
async function fetchCourses() {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.page,
      pageSize: pagination.pageSize,
      keyword: filters.keyword || undefined,
      category: filters.category || undefined,
      level: filters.level || undefined,
      sortBy: filters.sortBy
    }

    // 添加时长范围参数
    if (filters.duration) {
      const durationRange = parseRange(filters.duration)
      if (durationRange) {
        params.minDuration = durationRange.min
        params.maxDuration = durationRange.max
      }
    }

    // 添加卡路里范围参数
    if (filters.calories) {
      const caloriesRange = parseRange(filters.calories)
      if (caloriesRange) {
        params.minCalories = caloriesRange.min
        params.maxCalories = caloriesRange.max
      }
    }

    const res = await getPublicCourses(params)

    // 根据响应拦截器，res已经是res.data
    if (res && res.records) {
      courses.value = res.records
      pagination.total = res.total || 0
      pagination.totalPages = res.pages || Math.ceil(res.total / pagination.pageSize)
    } else if (Array.isArray(res)) {
      courses.value = res
      pagination.total = res.length
      pagination.totalPages = 1
    } else {
      courses.value = []
      pagination.total = 0
      pagination.totalPages = 0
    }
  } catch (error) {
    console.error('获取课程列表失败:', error)
    message.error('获取课程列表失败，请稍后重试')
    courses.value = []
  } finally {
    loading.value = false
  }
}

// 获取分类标签
function getCategoryLabel(category) {
  // 数据库中的值已经是中文，直接返回
  return category || '其他'
}

// 获取分类样式类
function getCategoryClass(category) {
  const map = {
    '瑜伽普拉提': 'category-yoga',
    '有氧燃脂': 'category-cardio',
    '力量训练': 'category-strength',
    '舞蹈操课': 'category-dance',
    '拳击格斗': 'category-boxing',
    '康复体态': 'category-pilates'
  }
  return map[category] || 'category-default'
}

// 获取难度样式类
function getLevelClass(level) {
  const map = {
    BEGINNER: 'level-beginner',
    INTERMEDIATE: 'level-intermediate',
    ADVANCED: 'level-advanced',
    初级: 'level-beginner',
    中级: 'level-intermediate',
    高级: 'level-advanced'
  }
  return map[level] || 'level-beginner'
}

// 图片加载失败处理
function handleImageError(event) {
  event.target.src = defaultCourseImage
}

// 跳转到详情页
function goToDetail(id) {
  if (!id) {
    message.error('课程信息无效')
    return
  }

  // 检查用户登录状态
  if (authStore.isLoggedIn) {
    // 已登录用户，跳转到课程详情页
    router.push(`/courses/${id}`)
  } else {
    // 未登录用户，存储课程ID并显示登录模态框
    sessionStorage.setItem('pendingCourseId', id)
    showLoginModal.value = true
  }
}

// 处理登录成功
function handleLoginSuccess() {
  message.success('登录成功')
  // 检查是否有待查看的课程
  const pendingCourseId = sessionStorage.getItem('pendingCourseId')
  if (pendingCourseId) {
    // 清除存储的课程ID
    sessionStorage.removeItem('pendingCourseId')
    // 跳转到课程详情页
    router.push(`/courses/${pendingCourseId}`)
  }
}

// 跳转到注册页面
function goToRegister() {
  showLoginModal.value = false
  router.push('/register')
}

// 监听路由参数变化
watch(() => route.query, (query) => {
  if (query.category) {
    filters.category = query.category
  }
  if (query.keyword) {
    filters.keyword = query.keyword
  }
}, { immediate: true })

// 页面加载
onMounted(() => {
  fetchCourses()
})
</script>

<style scoped>
/* CSS Variables */
.courses-page {
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

/* Courses Section */
.courses-section {
  padding: 40px 0 80px;
}

.courses-container {
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

/* Courses Grid */
.courses-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

/* Course Card */
.course-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-normal);
}

.course-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-lg);
  border-color: rgba(255, 107, 53, 0.2);
}

/* Course Image */
.course-image-wrapper {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.course-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.course-card:hover .course-image {
  transform: scale(1.05);
}

.course-badges {
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

.level-badge {
  background: rgba(0, 0, 0, 0.6);
  color: var(--text-primary);
}

.level-beginner {
  background: rgba(46, 196, 182, 0.9);
  color: #fff;
}

.level-intermediate {
  background: rgba(255, 193, 7, 0.9);
  color: #000;
}

.level-advanced {
  background: rgba(244, 63, 94, 0.9);
  color: #fff;
}

.duration-badge {
  background: rgba(255, 107, 53, 0.9);
  color: #fff;
}

.course-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity var(--transition-fast);
}

.course-card:hover .course-overlay {
  opacity: 1;
}

.view-btn {
  padding: 12px 24px;
  background: var(--primary-gradient);
  border: none;
  border-radius: 100px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  cursor: pointer;
  transform: translateY(10px);
  transition: all var(--transition-fast);
}

.course-card:hover .view-btn {
  transform: translateY(0);
}

/* Course Info */
.course-info {
  padding: 20px;
}

.course-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
}

.course-title {
  font-size: 18px;
  font-weight: 700;
  line-height: 1.4;
  flex: 1;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-rating {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #FFD93D;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}

.course-description {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-muted);
}

.course-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid var(--border-color);
}

.course-stats {
  display: flex;
  gap: 16px;
}

.stat {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--text-muted);
}

.course-category-tag {
  padding: 4px 10px;
  border-radius: 100px;
  font-size: 12px;
  font-weight: 600;
}

.category-yoga {
  background: rgba(168, 85, 247, 0.2);
  color: #A855F7;
}

.category-pilates {
  background: rgba(59, 130, 246, 0.2);
  color: #3B82F6;
}

.category-hiit {
  background: rgba(244, 63, 94, 0.2);
  color: #F43F5E;
}

.category-strength {
  background: rgba(255, 107, 53, 0.2);
  color: #FF6B35;
}

.category-cardio {
  background: rgba(46, 196, 182, 0.2);
  color: #2EC4B6;
}

.category-dance {
  background: rgba(255, 193, 7, 0.2);
  color: #FFC107;
}

.category-boxing {
  background: rgba(239, 68, 68, 0.2);
  color: #EF4444;
}

.category-default {
  background: rgba(255, 255, 255, 0.1);
  color: var(--text-secondary);
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
  .courses-grid {
    grid-template-columns: repeat(3, 1fr);
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
  .courses-grid {
    grid-template-columns: repeat(2, 1fr);
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
  .courses-container {
    padding: 0 12px;
  }

  .header-title {
    font-size: 28px;
  }

  .header-subtitle {
    font-size: 14px;
  }

  .filter-section {
    padding: 16px 0;
  }

  .filter-container {
    gap: 12px;
  }

  .filter-group {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .filter-label {
    font-size: 13px;
    color: var(--text-secondary);
  }

  .filter-tabs {
    width: 100%;
    overflow-x: auto;
    flex-wrap: nowrap;
    padding-bottom: 8px;
    -webkit-overflow-scrolling: touch;
    scrollbar-width: none;
  }

  .filter-tabs::-webkit-scrollbar {
    display: none;
  }

  .filter-tab {
    padding: 8px 14px;
    font-size: 13px;
    white-space: nowrap;
    flex-shrink: 0;
  }

  .search-box {
    padding: 12px 16px;
  }

  .search-input {
    font-size: 15px;
  }

  /* 移动端双排瀑布流布局 */
  .courses-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .course-card {
    border-radius: 16px;
  }

  .course-image-wrapper {
    height: 140px;
  }

  .course-badges {
    top: 8px;
    left: 8px;
    right: 8px;
  }

  .badge {
    padding: 4px 8px;
    font-size: 10px;
  }

  .course-info {
    padding: 12px;
  }

  .course-header {
    margin-bottom: 6px;
  }

  .course-title {
    font-size: 14px;
    font-weight: 600;
  }

  .course-rating {
    font-size: 12px;
    gap: 2px;
  }

  .course-description {
    font-size: 12px;
    line-height: 1.5;
    margin-bottom: 10px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .course-meta {
    gap: 8px;
  }

  .meta-item {
    font-size: 11px;
    gap: 4px;
  }

  .meta-item svg {
    width: 12px;
    height: 12px;
  }

  .courses-section {
    padding: 20px 0 40px;
  }

  /* 加载和空状态 */
  .loading-state {
    padding: 40px 20px;
  }

  .loading-state p {
    font-size: 14px;
  }

  .empty-state {
    padding: 40px 20px;
  }

  .empty-state h3 {
    font-size: 18px;
  }

  .empty-state p {
    font-size: 13px;
  }

  /* 排序和重置 */
  .sort-group {
    flex-direction: row;
    align-items: center;
    gap: 8px;
  }

  .sort-label {
    font-size: 13px;
  }

  .reset-filters-btn {
    padding: 8px 14px;
    font-size: 12px;
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
