<template>
  <div class="course-detail-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-bg"></div>
      <div class="header-content">
        <div class="header-breadcrumb">
          <router-link to="/">首页</router-link>
          <span class="breadcrumb-separator">/</span>
          <router-link to="/courses">全部课程</router-link>
          <span class="breadcrumb-separator">/</span>
          <span class="current">{{ course?.courseName || '课程详情' }}</span>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>正在加载课程信息...</p>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="error" class="error-container">
      <div class="error-icon">
        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <circle cx="12" cy="12" r="10"/>
          <line x1="12" y1="8" x2="12" y2="12"/>
          <line x1="12" y1="16" x2="12.01" y2="16"/>
        </svg>
      </div>
      <h3>{{ error }}</h3>
      <button class="btn btn-primary" @click="fetchCourse">重新加载</button>
    </div>

    <!-- 课程详情内容 -->
    <div v-else-if="course" class="detail-container">
      <!-- 课程主图区域 -->
      <div class="course-hero">
        <div class="course-image-section">
          <img
            :src="course.imageUrl || defaultImage"
            :alt="course.courseName"
            class="course-main-image"
            @error="handleImageError"
          />
          <div class="image-overlay">
            <span class="category-badge" :class="getCategoryClass(course.category)">
              {{ getCategoryLabel(course.category) }}
            </span>
            <span v-if="course.difficultyLevel" class="level-badge">
              {{ course.difficultyLevel }}
            </span>
          </div>
        </div>

        <!-- 课程基本信息 -->
        <div class="course-info-section">
          <h1 class="course-title">{{ course.courseName }}</h1>
          <p class="course-subtitle">{{ course.description?.substring(0, 100) || '专业健身课程' }}{{ course.description?.length > 100 ? '...' : '' }}</p>

          <!-- 课程统计 -->
          <div class="course-stats">
            <div class="stat-item">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <polyline points="12 6 12 12 16 14"/>
              </svg>
              <span>{{ course.durationMinutes || 45 }}分钟</span>
            </div>
            <div class="stat-item">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M8.5 14.5A2.5 2.5 0 0 0 11 12c0-1.38-.5-2-1-3-1.072-2.143-2.072-2.143-2.072-2.143-1.072 2.143.214 4.286 1.286 6.429 1.072 2.143 2.858 3.214 4.286 3.214 2.858 0 4.286-2.143 4.286-4.286 0-2.858-2.143-5.715-5.715-7.144C9.858 3.214 8.5 5.5 8.5 8.5c0 2.5 1.5 4 2 6-.5 2-2 3-2 3"/>
              </svg>
              <span>{{ course.caloriesMin || 200 }}-{{ course.caloriesMax || 400 }}卡</span>
            </div>
            <div class="stat-item">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                <circle cx="9" cy="7" r="4"/>
                <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
              </svg>
              <span>{{ course.totalBookingCount || 0 }}人已学</span>
            </div>
          </div>

          <!-- 教练信息 -->
          <div class="coach-info" v-if="course.coachName">
            <div class="coach-avatar">
              <img :src="course.coachAvatar || defaultAvatar" :alt="course.coachName" />
            </div>
            <div class="coach-details">
              <span class="coach-label">授课教练</span>
              <span class="coach-name">{{ course.coachName }}</span>
            </div>
          </div>

          <!-- 预约信息 -->
          <div class="booking-section">
            <div class="booking-info">
              <div class="capacity-info">
                <span class="label">课程容量</span>
                <span class="value">{{ course.capacity || 20 }}人</span>
              </div>
              <div class="remaining-info" :class="{ 'low': course.remainingCount <= 5 }">
                <span class="label">剩余名额</span>
                <span class="value">{{ course.remainingCount || 0 }}人</span>
              </div>
            </div>
            <button
              class="btn btn-primary btn-book"
              :class="{ 'disabled': course.remainingCount <= 0 || bookingLoading }"
              :disabled="course.remainingCount <= 0 || bookingLoading"
              @click="handleBooking"
            >
              <span v-if="bookingLoading" class="btn-spinner"></span>
              <span v-else>{{ course.remainingCount > 0 ? '立即预约' : '已满员' }}</span>
            </button>
          </div>
        </div>
      </div>

      <!-- 课程详情标签页 -->
      <div class="course-tabs">
        <div class="tabs-header">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            :class="['tab-btn', { active: activeTab === tab.key }]"
            @click="activeTab = tab.key"
          >
            {{ tab.label }}
          </button>
        </div>

        <div class="tabs-content">
          <!-- 课程介绍 -->
          <div v-if="activeTab === 'intro'" class="tab-panel">
            <div class="intro-section">
              <h3 class="section-title">课程简介</h3>
              <p class="intro-text">{{ course.description || '暂无课程介绍' }}</p>
            </div>

            <div class="features-section" v-if="course.features?.length">
              <h3 class="section-title">课程特色</h3>
              <ul class="features-list">
                <li v-for="(feature, index) in course.features" :key="index" class="feature-item">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#FF6B35" stroke-width="2">
                    <polyline points="20 6 9 17 4 12"/>
                  </svg>
                  <span>{{ feature }}</span>
                </li>
              </ul>
            </div>
          </div>

          <!-- 课程大纲 -->
          <div v-if="activeTab === 'outline'" class="tab-panel">
            <div class="outline-section">
              <h3 class="section-title">课程内容</h3>
              <div class="outline-list">
                <div
                  v-for="(item, index) in courseOutline"
                  :key="index"
                  class="outline-item"
                >
                  <div class="outline-number">{{ index + 1 }}</div>
                  <div class="outline-content">
                    <h4>{{ item.title }}</h4>
                    <p>{{ item.description }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 教练介绍 -->
          <div v-if="activeTab === 'coach'" class="tab-panel">
            <div class="coach-detail-section">
              <div class="coach-header">
                <img :src="course.coachAvatar || defaultAvatar" :alt="course.coachName" class="coach-large-avatar" />
                <div class="coach-header-info">
                  <h3>{{ course.coachName || '专业教练' }}</h3>
                  <p v-if="course.coachTitle">{{ course.coachTitle }}</p>
                </div>
              </div>
              <div class="coach-bio">
                <p>{{ course.coachBio || '资深健身教练，拥有丰富的教学经验，致力于帮助学员达成健身目标。' }}</p>
              </div>
            </div>
          </div>

          <!-- 注意事项 -->
          <div v-if="activeTab === 'notice'" class="tab-panel">
            <div class="notice-section">
              <h3 class="section-title">上课须知</h3>
              <ul class="notice-list">
                <li v-for="(notice, index) in notices" :key="index" class="notice-item">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#FF6B35" stroke-width="2">
                    <circle cx="12" cy="12" r="10"/>
                    <line x1="12" y1="8" x2="12" y2="12"/>
                    <line x1="12" y1="16" x2="12.01" y2="16"/>
                  </svg>
                  <span>{{ notice }}</span>
                </li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'
import { getCourseDetail, bookCourse } from '@/api/course'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const authStore = useAuthStore()

const loading = ref(false)
const bookingLoading = ref(false)
const error = ref(null)
const course = ref(null)
const activeTab = ref('intro')

const defaultImage = 'https://images.unsplash.com/photo-1571902943202-507ec2618e8f?w=800&h=600&fit=crop'
const defaultAvatar = 'https://images.unsplash.com/photo-1568602471122-7832951cc4c5?w=200&h=200&fit=crop'

const tabs = [
  { key: 'intro', label: '课程介绍' },
  { key: 'outline', label: '课程大纲' },
  { key: 'coach', label: '教练介绍' },
  { key: 'notice', label: '注意事项' }
]

// 课程大纲（模拟数据，实际应从后端获取）
const courseOutline = computed(() => {
  if (course.value?.outline?.length) {
    return course.value.outline
  }
  // 默认大纲
  return [
    { title: '热身准备', description: '5-10分钟动态热身，激活身体肌群' },
    { title: '基础动作教学', description: '学习课程核心动作要领，掌握正确姿势' },
    { title: '正式训练', description: '30-40分钟高强度训练，提升体能' },
    { title: '拉伸放松', description: '5-10分钟静态拉伸，缓解肌肉疲劳' }
  ]
})

// 注意事项
const notices = [
  '请提前10分钟到达，做好课前准备',
  '穿着舒适的运动服装和运动鞋',
  '课前1小时避免大量进食',
  '如有身体不适请提前告知教练',
  '请携带毛巾和饮用水'
]

// 分类映射（与数据库保持一致）
const categoryMap = {
  '瑜伽普拉提': { label: '瑜伽普拉提', class: 'yoga' },
  '有氧燃脂': { label: '有氧燃脂', class: 'cardio' },
  '力量训练': { label: '力量训练', class: 'strength' },
  '舞蹈操课': { label: '舞蹈操课', class: 'dance' },
  '拳击格斗': { label: '拳击格斗', class: 'boxing' },
  '康复体态': { label: '康复体态', class: 'rehab' }
}

onMounted(() => {
  fetchCourse()
})

async function fetchCourse() {
  loading.value = true
  error.value = null
  try {
    const res = await getCourseDetail(route.params.id)
    if (res.data) {
      course.value = res.data
    } else {
      error.value = '课程不存在或已下架'
    }
  } catch (err) {
    error.value = '获取课程详情失败，请稍后重试'
    console.error('获取课程详情失败:', err)
  } finally {
    loading.value = false
  }
}

async function handleBooking() {
  if (!authStore.isLoggedIn) {
    message.info('请先登录后再预约课程')
    // 存储当前课程ID，登录后返回
    sessionStorage.setItem('pendingCourseId', route.params.id)
    // 可以触发全局登录模态框或跳转到登录页
    router.push('/login?redirect=' + encodeURIComponent(`/courses/${route.params.id}`))
    return
  }

  if (course.value.remainingCount <= 0) {
    message.warning('课程已满员')
    return
  }

  bookingLoading.value = true
  try {
    await bookCourse(route.params.id)
    message.success('预约成功！')
    // 刷新课程信息
    fetchCourse()
  } catch (err) {
    message.error(err.message || '预约失败，请稍后重试')
  } finally {
    bookingLoading.value = false
  }
}

function getCategoryClass(category) {
  return categoryMap[category]?.class || 'default'
}

function getCategoryLabel(category) {
  return categoryMap[category]?.label || category
}

function handleImageError(event) {
  event.target.src = defaultImage
}

function goBack() {
  router.back()
}
</script>

<style scoped>
.course-detail-page {
  min-height: 100vh;
  background: #0A0A0F;
  color: #fff;
}

/* 页面头部 */
.page-header {
  position: relative;
  padding: 40px 0 30px;
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.1) 0%, rgba(26, 26, 37, 0.8) 100%);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.header-bg {
  position: absolute;
  inset: 0;
  background: radial-gradient(ellipse at top right, rgba(255, 107, 53, 0.15) 0%, transparent 50%);
}

.header-content {
  position: relative;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.header-breadcrumb {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
}

.header-breadcrumb a {
  color: rgba(255, 255, 255, 0.6);
  text-decoration: none;
  transition: color 0.3s;
}

.header-breadcrumb a:hover {
  color: #FF6B35;
}

.breadcrumb-separator {
  color: rgba(255, 255, 255, 0.3);
}

.header-breadcrumb .current {
  color: #fff;
  font-weight: 500;
}

/* 加载和错误状态 */
.loading-container,
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  gap: 20px;
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 3px solid rgba(255, 107, 53, 0.2);
  border-top-color: #FF6B35;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-icon {
  color: #FF6B35;
}

/* 详情容器 */
.detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

/* 课程主图区域 */
.course-hero {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 40px;
  margin-bottom: 50px;
}

.course-image-section {
  position: relative;
  border-radius: 24px;
  overflow: hidden;
  aspect-ratio: 4/3;
}

.course-main-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-overlay {
  position: absolute;
  top: 20px;
  left: 20px;
  display: flex;
  gap: 10px;
}

.category-badge,
.level-badge {
  padding: 8px 16px;
  border-radius: 100px;
  font-size: 13px;
  font-weight: 600;
}

.category-badge.yoga { background: linear-gradient(135deg, #8B5CF6 0%, #A78BFA 100%); }
.category-badge.cardio { background: linear-gradient(135deg, #EF4444 0%, #F87171 100%); }
.category-badge.strength { background: linear-gradient(135deg, #F59E0B 0%, #FBBF24 100%); }
.category-badge.dance { background: linear-gradient(135deg, #EC4899 0%, #F472B6 100%); }
.category-badge.boxing { background: linear-gradient(135deg, #6366F1 0%, #818CF8 100%); }
.category-badge.rehab { background: linear-gradient(135deg, #10B981 0%, #34D399 100%); }
.category-badge.default { background: linear-gradient(135deg, #FF6B35 0%, #FF8C61 100%); }

.level-badge {
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(10px);
}

/* 课程信息区域 */
.course-info-section {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.course-title {
  font-size: 36px;
  font-weight: 700;
  line-height: 1.3;
  margin: 0;
  background: linear-gradient(135deg, #fff 0%, rgba(255, 255, 255, 0.8) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.course-subtitle {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.6);
  line-height: 1.6;
  margin: 0;
}

.course-stats {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 15px;
}

.stat-item svg {
  color: #FF6B35;
}

/* 教练信息 */
.coach-info {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 16px;
}

.coach-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid rgba(255, 107, 53, 0.3);
}

.coach-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.coach-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.coach-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.5);
}

.coach-name {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
}

/* 预约区域 */
.booking-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 24px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 16px;
}

.booking-info {
  display: flex;
  gap: 30px;
}

.capacity-info,
.remaining-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.capacity-info .label,
.remaining-info .label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.5);
}

.capacity-info .value,
.remaining-info .value {
  font-size: 24px;
  font-weight: 700;
  color: #fff;
}

.remaining-info.low .value {
  color: #EF4444;
}

/* 按钮样式 */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px 32px;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-primary {
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C61 100%);
  color: #fff;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(255, 107, 53, 0.4);
}

.btn-primary:disabled,
.btn-primary.disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-book {
  width: 100%;
  padding: 18px;
  font-size: 18px;
}

.btn-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

/* 标签页 */
.course-tabs {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 24px;
  overflow: hidden;
}

.tabs-header {
  display: flex;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.tab-btn {
  flex: 1;
  padding: 20px;
  background: transparent;
  border: none;
  color: rgba(255, 255, 255, 0.6);
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.tab-btn:hover {
  color: rgba(255, 255, 255, 0.9);
  background: rgba(255, 255, 255, 0.02);
}

.tab-btn.active {
  color: #FF6B35;
}

.tab-btn.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 20%;
  right: 20%;
  height: 3px;
  background: linear-gradient(90deg, transparent, #FF6B35, transparent);
  border-radius: 3px;
}

.tabs-content {
  padding: 40px;
}

.tab-panel {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
  color: #fff;
}

.intro-text {
  font-size: 15px;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 30px;
}

/* 课程特色 */
.features-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  list-style: none;
  padding: 0;
  margin: 0;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.04);
  border-radius: 12px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
}

/* 课程大纲 */
.outline-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.outline-item {
  display: flex;
  gap: 20px;
  padding: 24px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.04);
  border-radius: 16px;
  transition: all 0.3s;
}

.outline-item:hover {
  background: rgba(255, 255, 255, 0.04);
  border-color: rgba(255, 107, 53, 0.2);
}

.outline-number {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C61 100%);
  border-radius: 12px;
  font-weight: 700;
  font-size: 16px;
  flex-shrink: 0;
}

.outline-content h4 {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 8px;
  color: #fff;
}

.outline-content p {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
}

/* 教练详情 */
.coach-detail-section {
  padding: 20px;
}

.coach-header {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 30px;
}

.coach-large-avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid rgba(255, 107, 53, 0.3);
}

.coach-header-info h3 {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 8px;
}

.coach-header-info p {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
}

.coach-bio {
  font-size: 15px;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.7);
}

/* 注意事项 */
.notice-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  list-style: none;
  padding: 0;
  margin: 0;
}

.notice-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.04);
  border-radius: 12px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
}

.notice-item svg {
  flex-shrink: 0;
  margin-top: 2px;
}

/* 响应式 */
@media (max-width: 968px) {
  .course-hero {
    grid-template-columns: 1fr;
  }

  .course-title {
    font-size: 28px;
  }

  .tabs-content {
    padding: 24px;
  }

  .features-list {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .course-stats {
    gap: 16px;
  }

  .booking-info {
    gap: 20px;
  }

  .tabs-header {
    flex-wrap: wrap;
  }

  .tab-btn {
    padding: 16px 12px;
    font-size: 14px;
  }
}
</style>
