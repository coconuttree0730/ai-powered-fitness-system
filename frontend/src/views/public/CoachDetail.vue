<template>
  <div class="coach-detail-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-bg"></div>
      <div class="header-content">
        <div class="header-breadcrumb">
          <router-link to="/">首页</router-link>
          <span class="breadcrumb-separator">/</span>
          <router-link to="/" @click.prevent="scrollToCoaches">教练团队</router-link>
          <span class="breadcrumb-separator">/</span>
          <span class="current">{{ coach.name || '教练详情' }}</span>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>正在加载教练信息...</p>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="error" class="error-state">
      <div class="error-icon">
        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <circle cx="12" cy="12" r="10"/>
          <line x1="12" y1="8" x2="12" y2="12"/>
          <line x1="12" y1="16" x2="12.01" y2="16"/>
        </svg>
      </div>
      <h3>{{ error }}</h3>
      <button class="btn btn-primary" @click="fetchCoachDetail">重新加载</button>
    </div>

    <!-- 教练详情内容 -->
    <div v-else class="detail-content">
      <div class="detail-container">
        <!-- 左侧：教练基本信息 -->
        <div class="coach-profile">
          <div class="coach-image-wrapper">
            <img
              :src="coach.image || defaultCoachImage"
              :alt="coach.name"
              class="coach-image"
              @error="handleImageError"
            />
            <div class="coach-image-overlay"></div>
          </div>
          <div class="coach-basic-info">
            <h1 class="coach-name">{{ coach.name }}</h1>
            <p class="coach-title">{{ coach.title }}</p>
            <div class="coach-rating">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor" stroke="none">
                <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
              </svg>
              <span>{{ coach.rating || '98%' }} 好评率</span>
            </div>
            <div class="coach-tags">
              <span v-for="(tag, i) in coach.tags" :key="i" class="coach-tag">{{ tag }}</span>
            </div>
            <!-- 未登录提示 -->
            <div v-if="!authStore.isLoggedIn" class="login-hint">
              <p>登录后可预约该教练的课程</p>
              <button class="btn btn-primary" @click="showLoginModal = true">立即登录</button>
            </div>
            <!-- 登录后预约按钮 -->
            <button v-else class="btn btn-primary btn-book" @click="handleBookCoach">
              预约课程
            </button>
          </div>
        </div>

        <!-- 右侧：教练详细信息 -->
        <div class="coach-details">
          <!-- 统计数据 -->
          <div class="stats-grid">
            <div class="stat-card">
              <div class="stat-value">{{ coach.experience || '5+' }}</div>
              <div class="stat-label">年教学经验</div>
            </div>
            <div class="stat-card">
              <div class="stat-value">{{ coach.students || '1000+' }}</div>
              <div class="stat-label">累计学员</div>
            </div>
            <div class="stat-card">
              <div class="stat-value">{{ coach.courses || '50+' }}</div>
              <div class="stat-label">授课节数</div>
            </div>
            <div class="stat-card">
              <div class="stat-value">{{ coach.rating || '98%' }}</div>
              <div class="stat-label">好评率</div>
            </div>
          </div>

          <!-- 个人简介 -->
          <div class="detail-section">
            <h2 class="section-title">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
              个人简介
            </h2>
            <p class="section-content">{{ coach.introduction || '暂无简介' }}</p>
          </div>

          <!-- 专业特长 -->
          <div class="detail-section">
            <h2 class="section-title">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                <polyline points="22 4 12 14.01 9 11.01"/>
              </svg>
              专业特长
            </h2>
            <div class="specialties-grid">
              <div v-for="(specialty, i) in coach.specialties || coach.tags || []" :key="i" class="specialty-item">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#FF6B35" stroke-width="2">
                  <polyline points="20 6 9 17 4 12"/>
                </svg>
                {{ specialty }}
              </div>
            </div>
          </div>

          <!-- 资质认证 -->
          <div class="detail-section">
            <h2 class="section-title">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="8" r="7"/>
                <polyline points="8.21 13.89 7 23 12 20 17 23 15.79 13.88"/>
              </svg>
              资质认证
            </h2>
            <div class="certificates-list">
              <div v-for="(cert, i) in coach.certificates || ['国家职业资格认证', 'ACE认证教练', 'CPR急救认证']" :key="i" class="certificate-item">
                <div class="certificate-icon">🏆</div>
                <span>{{ cert }}</span>
              </div>
            </div>
          </div>

          <!-- 学员评价 -->
          <div class="detail-section">
            <h2 class="section-title">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/>
              </svg>
              学员评价
            </h2>
            <div class="reviews-list">
              <div v-for="(review, i) in coach.reviews || defaultReviews" :key="i" class="review-item">
                <div class="review-header">
                  <div class="reviewer-avatar">{{ review.name[0] }}</div>
                  <div class="reviewer-info">
                    <div class="reviewer-name">{{ review.name }}</div>
                    <div class="review-rating">
                      <span v-for="star in 5" :key="star" class="star" :class="{ filled: star <= review.rating }">★</span>
                    </div>
                  </div>
                  <div class="review-date">{{ review.date }}</div>
                </div>
                <p class="review-content">{{ review.content }}</p>
              </div>
            </div>
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
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'
import { getCoachDetail } from '@/api/coachDetail'
import LoginModal from '@/components/LoginModal.vue'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const authStore = useAuthStore()

// 默认教练图片
const defaultCoachImage = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAwIiBoZWlnaHQ9IjUwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iNDAwIiBoZWlnaHQ9IjUwMCIgZmlsbD0iIzJBMkEzNSIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBmb250LWZhbWlseT0iQXJpYWwiIGZvbnQtc2l6ZT0iMjQiIGZpbGw9IiM2QjcyODAiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGR5PSIuM2VtIj7nlLXlrZDlhYnvvIzlpKnkuK3nvZHnu5zlvaLlj5Y8L3RleHQ+PC9zdmc+'

// 默认评价数据
const defaultReviews = [
  { name: '张小明', rating: 5, date: '2024-10-15', content: '教练非常专业，根据我的身体状况制定了详细的训练计划，效果很明显！' },
  { name: '李华', rating: 5, date: '2024-10-10', content: '上课氛围很好，教练很有耐心，动作指导很到位，推荐！' },
  { name: '王芳', rating: 4, date: '2024-09-28', content: '教练专业知识丰富，课程安排合理，已经坚持训练3个月了。' }
]

// 状态
const loading = ref(false)
const error = ref(null)
const coach = ref({})
const showLoginModal = ref(false)

// 获取教练详情
async function fetchCoachDetail() {
  const coachId = route.params.id
  if (!coachId) {
    error.value = '教练ID无效'
    return
  }

  loading.value = true
  error.value = null

  try {
    const data = await getCoachDetail(coachId)
    if (data) {
      coach.value = data
    } else {
      error.value = '未找到该教练信息'
    }
  } catch (err) {
    console.error('获取教练详情失败:', err)
    error.value = '获取教练详情失败，请稍后重试'
    // 使用模拟数据作为降级
    coach.value = {
      id: coachId,
      name: '示例教练',
      title: '高级私人教练',
      experience: '8+',
      students: '2000+',
      rating: '99%',
      tags: ['增肌塑形', '体态矫正', '运动康复'],
      introduction: '拥有8年健身教学经验，专注于增肌塑形和体态矫正。曾帮助超过2000名学员实现健身目标，深受学员好评。',
      specialties: ['增肌塑形', '体态矫正', '运动康复', '减脂训练'],
      certificates: ['国家职业资格认证', 'ACE认证教练', 'CPR急救认证', '运动营养师认证']
    }
    error.value = null
  } finally {
    loading.value = false
  }
}

// 处理图片加载失败
function handleImageError(event) {
  event.target.src = defaultCoachImage
}

// 滚动到教练团队区域
function scrollToCoaches() {
  router.push('/').then(() => {
    setTimeout(() => {
      const coachesSection = document.getElementById('coaches')
      if (coachesSection) {
        coachesSection.scrollIntoView({ behavior: 'smooth' })
      }
    }, 100)
  })
}

// 处理预约教练
function handleBookCoach() {
  if (!authStore.isLoggedIn) {
    showLoginModal.value = true
    return
  }
  // 跳转到预约页面或显示预约弹窗
  message.success('预约功能开发中，敬请期待！')
}

// 处理登录成功
function handleLoginSuccess() {
  message.success('登录成功')
  // 登录成功后可以刷新数据或执行其他操作
}

// 跳转到注册页面
function goToRegister() {
  showLoginModal.value = false
  router.push('/register')
}

// 页面加载
onMounted(() => {
  fetchCoachDetail()
})
</script>

<style scoped>
/* CSS Variables */
.coach-detail-page {
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
  padding: 100px 0 40px;
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
  font-size: 14px;
}

.header-breadcrumb a {
  color: var(--text-muted);
  text-decoration: none;
  transition: color var(--transition-fast);
  cursor: pointer;
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

/* Error State */
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100px 20px;
  text-align: center;
}

.error-icon {
  color: var(--primary);
  margin-bottom: 24px;
  opacity: 0.8;
}

.error-state h3 {
  font-size: 20px;
  margin-bottom: 24px;
  color: var(--text-secondary);
}

/* Detail Content */
.detail-content {
  padding: 0 0 80px;
}

.detail-container {
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 40px;
  display: grid;
  grid-template-columns: 380px 1fr;
  gap: 40px;
}

/* Coach Profile */
.coach-profile {
  position: sticky;
  top: 100px;
  height: fit-content;
}

.coach-image-wrapper {
  position: relative;
  border-radius: var(--radius-lg);
  overflow: hidden;
  margin-bottom: 24px;
  box-shadow: var(--shadow-lg);
}

.coach-image {
  width: 100%;
  height: 480px;
  object-fit: cover;
}

.coach-image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to top, rgba(10, 10, 15, 0.8) 0%, transparent 50%);
}

.coach-basic-info {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 24px;
}

.coach-name {
  font-size: 28px;
  font-weight: 800;
  margin-bottom: 8px;
  background: var(--primary-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.coach-title {
  font-size: 16px;
  color: var(--text-secondary);
  margin-bottom: 16px;
}

.coach-rating {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #FFD93D;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 16px;
}

.coach-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 24px;
}

.coach-tag {
  background: rgba(255, 107, 53, 0.1);
  border: 1px solid rgba(255, 107, 53, 0.2);
  padding: 6px 12px;
  border-radius: 100px;
  font-size: 13px;
  color: var(--primary);
}

.login-hint {
  padding: 16px;
  background: rgba(255, 107, 53, 0.05);
  border: 1px solid rgba(255, 107, 53, 0.2);
  border-radius: var(--radius-md);
  text-align: center;
}

.login-hint p {
  font-size: 14px;
  color: var(--text-muted);
  margin-bottom: 12px;
}

.btn-book {
  width: 100%;
  padding: 14px 24px;
  font-size: 16px;
}

/* Coach Details */
.coach-details {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

/* Stats Grid */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.stat-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 24px;
  text-align: center;
  transition: all var(--transition-fast);
}

.stat-card:hover {
  background: var(--bg-card-hover);
  border-color: rgba(255, 107, 53, 0.3);
  transform: translateY(-4px);
}

.stat-value {
  font-size: 32px;
  font-weight: 800;
  color: var(--primary);
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: var(--text-muted);
}

/* Detail Section */
.detail-section {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 32px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 20px;
  color: var(--text-primary);
}

.section-title svg {
  color: var(--primary);
}

.section-content {
  font-size: 16px;
  color: var(--text-secondary);
  line-height: 1.8;
}

/* Specialties */
.specialties-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.specialty-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: var(--bg-dark-secondary);
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--text-secondary);
}

/* Certificates */
.certificates-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.certificate-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: var(--bg-dark-secondary);
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--text-secondary);
}

.certificate-icon {
  font-size: 20px;
}

/* Reviews */
.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.review-item {
  padding: 20px;
  background: var(--bg-dark-secondary);
  border-radius: var(--radius-md);
}

.review-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.reviewer-avatar {
  width: 40px;
  height: 40px;
  background: var(--primary-gradient);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 600;
  color: white;
}

.reviewer-info {
  flex: 1;
}

.reviewer-name {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 4px;
}

.review-rating {
  display: flex;
  gap: 2px;
}

.star {
  color: rgba(255, 255, 255, 0.2);
  font-size: 14px;
}

.star.filled {
  color: #FFD93D;
}

.review-date {
  font-size: 12px;
  color: var(--text-muted);
}

.review-content {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  padding-left: 52px;
}

/* Buttons */
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
@media (max-width: 1200px) {
  .detail-container {
    grid-template-columns: 1fr;
  }

  .coach-profile {
    position: relative;
    top: 0;
    display: grid;
    grid-template-columns: 300px 1fr;
    gap: 24px;
  }

  .coach-image-wrapper {
    margin-bottom: 0;
  }

  .coach-image {
    height: 380px;
  }
}

@media (max-width: 768px) {
  .page-header {
    padding: 80px 0 30px;
  }

  .header-content,
  .detail-container {
    padding: 0 20px;
  }

  .coach-profile {
    grid-template-columns: 1fr;
  }

  .coach-image {
    height: 320px;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .specialties-grid {
    grid-template-columns: 1fr;
  }

  .review-content {
    padding-left: 0;
    margin-top: 12px;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .detail-section {
    padding: 20px;
  }
}
</style>
