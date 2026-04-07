<template>
  <div :class="['fitness-plan-preview', { 'embedded-mode': isEmbedded }]">
    <!-- 头部 -->
    <div class="plan-header">
      <div class="plan-header-top">
        <div class="plan-title">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M6.5 6.5h11M6.5 17.5h11M3 12h18M6.5 6.5a2.5 2.5 0 1 0-5 0 2.5 2.5 0 1 0 5 0zM17.5 6.5a2.5 2.5 0 1 0-5 0 2.5 2.5 0 1 0 5 0zM6.5 17.5a2.5 2.5 0 1 0-5 0 2.5 2.5 0 1 0 5 0zM17.5 17.5a2.5 2.5 0 1 0-5 0 2.5 2.5 0 1 0 5 0z"/>
          </svg>
          <span>健身训练计划</span>
        </div>
      </div>
      <div class="plan-subtitle">{{ planData.subtitle || 'AI为您量身定制的7天增肌塑形计划' }}</div>
    </div>

    <!-- 用户信息 -->
    <div class="user-info-bar">
      <div class="user-stat">身高: <strong>{{ planData.userInfo?.height || '175cm' }}</strong></div>
      <div class="user-stat">体重: <strong>{{ planData.userInfo?.weight || '70kg' }}</strong></div>
      <div class="user-stat">BMI: <strong>{{ planData.userInfo?.bmi || '22.9' }}</strong></div>
      <div class="user-stat">目标: <strong>{{ planData.userInfo?.goal || '增肌塑形' }}</strong></div>
      <div class="user-stat">强度: <strong>{{ planData.userInfo?.intensity || '中等' }}</strong></div>
    </div>

    <!-- Tab标签栏 -->
    <div class="tabs-container">
      <div class="tabs-wrapper">
        <button
          v-for="(day, index) in planData.weeklyPlan"
          :key="index"
          :class="['tab-item', { active: activeTab === index }]"
          @click="switchTab(index)"
        >
          <span class="tab-day">{{ day.dayName }}</span>
          <span class="tab-focus">{{ day.focus }}</span>
        </button>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="tab-content" ref="tabContentRef">
      <transition name="fade-slide" mode="out-in">
        <div v-if="planData.weeklyPlan && planData.weeklyPlan[activeTab]" :key="activeTab" class="day-section">
          <!-- 推荐课程 - 支持1-3个课程卡片 -->
          <div v-if="getCurrentDay().courses && getCurrentDay().courses.length > 0" class="section-block">
            <div class="section-header">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <polygon points="23 7 16 12 23 17 23 7"/><rect x="1" y="5" width="15" height="14" rx="2" ry="2"/>
              </svg>
              <span>推荐课程</span>
              <span class="course-count">{{ getCurrentDay().courses.length }}个课程</span>
            </div>
            <div class="courses-grid" :class="`courses-count-${getCurrentDay().courses.length}`">
              <div
                v-for="(course, idx) in getCurrentDay().courses"
                :key="idx"
                class="course-card"
                @click="$emit('courseClick', course)"
              >
                <div class="course-image-wrapper">
                  <img
                    :src="getValidImageUrl(course.coverImage, 'course', course.name)"
                    :alt="course.name"
                    class="course-image"
                    loading="lazy"
                  >
                  <div class="course-image-overlay">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <polygon points="5 3 19 12 5 21 5 3"/>
                    </svg>
                  </div>
                </div>
                <div class="course-content">
                  <div class="course-name">{{ course.name }}</div>
                  <div class="course-desc">{{ course.description }}</div>
                  <div class="course-meta">
                    <span class="course-duration">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                        <circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>
                      </svg>
                      {{ course.duration }}分钟
                    </span>
                    <span class="course-link-text">查看详情 →</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <!-- 单个课程兼容旧格式 -->
          <div v-else-if="getCurrentDay().course" class="section-block">
            <div class="section-header">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <polygon points="23 7 16 12 23 17 23 7"/><rect x="1" y="5" width="15" height="14" rx="2" ry="2"/>
              </svg>
              <span>推荐课程</span>
            </div>
            <div class="courses-grid courses-count-1">
              <div class="course-card" @click="$emit('courseClick', getCurrentDay().course)">
                <div class="course-image-wrapper">
                  <img
                    :src="getValidImageUrl(getCurrentDay().course.coverImage, 'course', getCurrentDay().course.name)"
                    :alt="getCurrentDay().course.name"
                    class="course-image"
                    loading="lazy"
                  >
                  <div class="course-image-overlay">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <polygon points="5 3 19 12 5 21 5 3"/>
                    </svg>
                  </div>
                </div>
                <div class="course-content">
                  <div class="course-name">{{ getCurrentDay().course.name }}</div>
                  <div class="course-desc">{{ getCurrentDay().course.description }}</div>
                  <div class="course-meta">
                    <span class="course-duration">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                        <circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>
                      </svg>
                      {{ getCurrentDay().course.duration }}分钟
                    </span>
                    <span class="course-link-text">查看详情 →</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 训练器械 -->
          <div v-if="getCurrentDay().equipment && getCurrentDay().equipment.length > 0" class="section-block">
            <div class="section-header">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/>
              </svg>
              <span>训练器械</span>
            </div>
            <div class="equipment-grid">
              <div
                v-for="(equip, idx) in getCurrentDay().equipment"
                :key="idx"
                class="equipment-item"
              >
                <img
                  :src="getValidImageUrl(equip.image, 'equip', equip.name)"
                  :alt="equip.name"
                  class="equipment-img"
                  loading="lazy"
                >
                <div class="equipment-name">{{ equip.name }}</div>
              </div>
            </div>
          </div>

          <!-- 训练动作 -->
          <div v-if="getCurrentDay().exercises && getCurrentDay().exercises.length > 0" class="section-block">
            <div class="section-header">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M18 8h1a4 4 0 0 1 0 8h-1M2 8h16v9a4 4 0 0 1-4 4H6a4 4 0 0 1-4-4V8zM6 1v3M10 1v3M14 1v3"/>
              </svg>
              <span>训练动作</span>
            </div>
            <div class="exercise-list">
              <div
                v-for="(exercise, idx) in getCurrentDay().exercises"
                :key="idx"
                class="exercise-item"
              >
                <div class="exercise-number">{{ idx + 1 }}</div>
                <div class="exercise-info">
                  <div class="exercise-name">{{ exercise.name }}</div>
                  <div class="exercise-detail">{{ exercise.sets }}组 × {{ exercise.reps }}次 · 组间休息{{ exercise.restSeconds }}秒</div>
                </div>
              </div>
            </div>
          </div>

          <!-- 提示信息 -->
          <div v-if="getCurrentDay().tips && getCurrentDay().tips.length > 0" class="section-block">
            <div class="tips-box">
              <ul>
                <li v-for="(tip, idx) in getCurrentDay().tips" :key="idx">{{ tip }}</li>
              </ul>
            </div>
          </div>
        </div>
      </transition>
    </div>

    <!-- 底部操作按钮 -->
    <div class="plan-actions">
      <button class="btn btn-default" @click="$emit('regenerate')">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="M21.5 2v6h-6M2.5 22v-6h6M2 11.5a10 10 0 0 1 18.8-4.3M22 12.5a10 10 0 0 1-18.8 4.3"/>
        </svg>
        重新生成
      </button>
      <button class="btn btn-primary" @click="$emit('save')">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/><polyline points="17 21 17 13 7 13 7 21"/><polyline points="7 3 7 8 15 8"/>
        </svg>
        保存计划
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  planData: {
    type: Object,
    required: true,
    default: () => ({
      subtitle: '',
      userInfo: {
        height: '',
        weight: '',
        bmi: '',
        goal: '',
        intensity: ''
      },
      weeklyPlan: []
    })
  },
  isEmbedded: {
    type: Boolean,
    default: false
  }
})

defineEmits(['regenerate', 'save', 'courseClick'])

const activeTab = ref(0)
const tabContentRef = ref(null)

function switchTab(index) {
  activeTab.value = index
  if (tabContentRef.value) {
    tabContentRef.value.scrollTop = 0
  }
}

function getCurrentDay() {
  if (!props.planData.weeklyPlan || !props.planData.weeklyPlan[activeTab.value]) {
    return { courses: [], course: null, equipment: [], exercises: [], tips: [] }
  }
  return props.planData.weeklyPlan[activeTab.value]
}

// 图片URL验证和fallback处理
function getValidImageUrl(url, type, name = '') {
  if (!url || url === '' || url === 'undefined' || url === 'null') {
    return getDefaultImage(type, name)
  }

  // 检查是否是有效的URL格式
  try {
    new URL(url)
    // 对于外部URL，直接返回（浏览器会自动处理加载失败）
    return url
  } catch {
    return getDefaultImage(type, name)
  }
}

// 获取默认图片
function getDefaultImage(type, name = '') {
  const imageMap = {
    course: {
      '胸部': 'https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=400&h=250&fit=crop&q=80',
      '背部': 'https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=400&h=250&fit=crop&q=80',
      '腿部': 'https://images.unsplash.com/photo-1434682882158-0d4a9cc89bb3?w=400&h=250&fit=crop&q=80',
      '肩部': 'https://images.unsplash.com/photo-1571902943202-507ec2618e8f?w=400&h=250&fit=crop&q=80',
      '手臂': 'https://images.unsplash.com/photo-1583454110551-21f2fe6907ba?w=400&h=250&fit=crop&q=80',
      '核心': 'https://images.unsplash.com/photo-1518611012118-696072aa579a?w=400&h=250&fit=crop&q=80',
      '臀腿': 'https://images.unsplash.com/photo-1434682882158-0d4a9cc89bb3?w=400&h=250&fit=crop&q=80',
      '有氧': 'https://images.unsplash.com/photo-1476480862126-209bfaa8edc8?w=400&h=250&fit=crop&q=80',
      'HIIT': 'https://images.unsplash.com/photo-1517838277539-f91ace7160b8?w=400&h=250&fit=crop&q=80',
      'default': 'https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=400&h=250&fit=crop&q=80'
    },
    equip: {
      '杠铃': 'https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=100&h=100&fit=crop&q=80',
      '哑铃': 'https://images.unsplash.com/photo-1597452485669-2c7bb5fef90d?w=100&h=100&fit=crop&q=80',
      '卧推凳': 'https://images.unsplash.com/photo-1571902949628-41aaea4ce747?w=100&h=100&fit=crop&q=80',
      '蝴蝶机': 'https://images.unsplash.com/photo-1638536532686-d610adfc8e5c?w=100&h=100&fit=crop&q=80',
      '深蹲架': 'https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=100&h=100&fit=crop&q=80',
      '瑜伽垫': 'https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=100&h=100&fit=crop&q=80',
      '跑步机': 'https://images.unsplash.com/photo-1538805065399-34e299058ebe?w=100&h=100&fit=crop&q=80',
      'default': 'https://images.unsplash.com/photo-1597452485669-2c7bb5fef90d?w=100&h=100&fit=crop&q=80'
    }
  }

  const categoryMap = imageMap[type] || imageMap.course

  // 尝试根据名称匹配
  for (const key of Object.keys(categoryMap)) {
    if (name && (name.includes(key) || key.includes(name))) {
      return categoryMap[key]
    }
  }

  return categoryMap.default || categoryMap['default']
}
</script>

<style scoped>
.fitness-plan-preview {
  width: 100%;
  max-width: 720px;
  margin: 0 auto;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
}

/* 嵌入模式样式调整 */
.fitness-plan-preview.embedded-mode {
  box-shadow: none;
  border-radius: 16px;
  border: 1px solid rgba(229, 231, 235, 0.8);
  background: #ffffff;
  max-width: 100%;
}

/* 头部 */
.plan-header {
  padding: 20px 24px;
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C61 50%, #FF6B35 100%);
  background-size: 200% 200%;
  color: #ffffff;
  box-shadow: 0 4px 20px rgba(255, 107, 53, 0.25);
  animation: gradientShift 8s ease infinite;
}

@keyframes gradientShift {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

.plan-header-top {
  display: flex;
  align-items: center;
  justify-content: center;
}

.plan-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.plan-subtitle {
  font-size: 14px;
  opacity: 0.95;
  text-align: center;
  margin-top: 10px;
  font-weight: 500;
}

/* 用户信息 */
.user-info-bar {
  padding: 14px 24px;
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.06) 0%, rgba(255, 140, 97, 0.04) 100%);
  border-bottom: 1px solid rgba(229, 231, 235, 0.6);
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
  justify-content: center;
}

.user-stat {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #6B7280;
}

.user-stat strong {
  color: #FF6B35;
  font-weight: 700;
}

/* Tab标签栏 */
.tabs-container {
  padding: 0 20px;
  background: #ffffff;
  border-bottom: 2px solid #F3F4F6;
  position: sticky;
  top: 0;
  z-index: 10;
}

.tabs-wrapper {
  display: flex;
  gap: 6px;
  overflow-x: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.tabs-wrapper::-webkit-scrollbar {
  display: none;
}

.tab-item {
  flex: 1;
  min-width: fit-content;
  padding: 14px 18px;
  text-align: center;
  cursor: pointer;
  border: none;
  background: transparent;
  border-bottom: 3px solid transparent;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  white-space: nowrap;
}

.tab-day {
  display: block;
  font-size: 12px;
  color: #9CA3AF;
  margin-bottom: 3px;
  font-weight: 500;
}

.tab-focus {
  display: block;
  font-size: 15px;
  font-weight: 600;
  color: #4B5563;
}

.tab-item:hover {
  background: rgba(255, 107, 53, 0.06);
}

.tab-item.active {
  border-bottom-color: #FF6B35;
  background: rgba(255, 107, 53, 0.08);
}

.tab-item.active .tab-day,
.tab-item.active .tab-focus {
  color: #FF6B35;
}

/* 内容区域 */
.tab-content {
  padding: 20px 24px;
  max-height: 480px;
  overflow-y: auto;
}

.day-section {
  animation: fadeInUp 0.4s ease;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 过渡动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

/* 区块样式 */
.section-block {
  margin-bottom: 20px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
  font-size: 15px;
  font-weight: 600;
  color: #1A1A2E;
}

.section-header svg {
  color: #FF6B35;
}

.course-count {
  margin-left: auto;
  font-size: 12px;
  color: #9CA3AF;
  font-weight: 500;
  background: #F3F4F6;
  padding: 2px 8px;
  border-radius: 10px;
}

/* 推荐课程网格 - 支持1-3个课程 */
.courses-grid {
  display: grid;
  gap: 14px;
}

.courses-grid.courses-count-1 {
  grid-template-columns: 1fr;
}

.courses-grid.courses-count-2 {
  grid-template-columns: repeat(2, 1fr);
}

.courses-grid.courses-count-3 {
  grid-template-columns: repeat(3, 1fr);
}

/* 响应式：小屏幕时2个课程也变成单列 */
@media (max-width: 600px) {
  .courses-grid.courses-count-2,
  .courses-grid.courses-count-3 {
    grid-template-columns: 1fr;
  }
}

/* 课程卡片 */
.course-card {
  background: #ffffff;
  border: 1px solid #E5E7EB;
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  text-decoration: none;
  color: inherit;
  display: flex;
  flex-direction: column;
}

.course-card:hover {
  border-color: #FF6B35;
  box-shadow: 0 12px 40px rgba(255, 107, 53, 0.18);
  transform: translateY(-4px);
}

.course-image-wrapper {
  width: 100%;
  height: 140px;
  overflow: hidden;
  background: linear-gradient(135deg, #F8FAFC 0%, #E5E7EB 100%);
  position: relative;
}

.course-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.course-card:hover .course-image {
  transform: scale(1.08);
}

.course-image-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.35s ease;
}

.course-image-overlay svg {
  color: white;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
  transform: scale(0.8);
  transition: transform 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.course-card:hover .course-image-overlay {
  opacity: 1;
}

.course-card:hover .course-image-overlay svg {
  transform: scale(1);
}

.course-content {
  padding: 14px 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.course-name {
  font-size: 15px;
  font-weight: 700;
  color: #1A1A2E;
  margin-bottom: 8px;
  line-height: 1.4;
}

.course-desc {
  font-size: 13px;
  color: #6B7280;
  line-height: 1.6;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.course-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 12px;
  border-top: 1px solid #F0F2F5;
  margin-top: auto;
}

.course-duration {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #FF6B35;
  font-weight: 600;
}

.course-link-text {
  font-size: 13px;
  color: #FF6B35;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: gap 0.3s ease;
}

.course-card:hover .course-link-text {
  gap: 8px;
}

/* 训练器械网格 */
.equipment-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
  gap: 12px;
}

.equipment-item {
  text-align: center;
  padding: 12px 8px;
  background: linear-gradient(135deg, #F8FAFC 0%, #F3F4F6 100%);
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid transparent;
}

.equipment-item:hover {
  background: rgba(255, 107, 53, 0.08);
  border-color: rgba(255, 107, 53, 0.25);
  transform: translateY(-3px) scale(1.02);
  box-shadow: 0 8px 20px rgba(255, 107, 53, 0.12);
}

.equipment-img {
  width: 52px;
  height: 52px;
  border-radius: 10px;
  object-fit: cover;
  margin: 0 auto 8px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.equipment-name {
  font-size: 11px;
  color: #4B5563;
  font-weight: 600;
  word-break: break-all;
  line-height: 1.3;
}

/* 训练动作列表 */
.exercise-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.exercise-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #F8FAFC 0%, #F3F4F6 100%);
  border-radius: 12px;
  border-left: 4px solid #FF6B35;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.exercise-item:hover {
  background: rgba(255, 107, 53, 0.06);
  border-left-color: #E55A2B;
  transform: translateX(6px);
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.1);
}

.exercise-number {
  width: 28px;
  height: 28px;
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C61 100%);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
  box-shadow: 0 4px 10px rgba(255, 107, 53, 0.3);
}

.exercise-info {
  flex: 1;
}

.exercise-name {
  font-size: 14px;
  font-weight: 700;
  color: #1A1A2E;
  margin-bottom: 3px;
}

.exercise-detail {
  font-size: 12px;
  color: #6B7280;
  font-weight: 500;
}

/* 提示信息 */
.tips-box {
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.08) 0%, rgba(255, 140, 97, 0.04) 100%);
  border: 1px solid rgba(255, 107, 53, 0.2);
  border-radius: 12px;
  padding: 16px 18px;
  font-size: 13px;
  color: #E55A2B;
  line-height: 1.8;
}

.tips-box ul {
  list-style: none;
  padding-left: 0;
  margin: 0;
}

.tips-box li {
  padding: 6px 0;
  padding-left: 22px;
  position: relative;
}

.tips-box li::before {
  content: '✓';
  position: absolute;
  left: 0;
  font-size: 12px;
  color: #FF6B35;
  font-weight: 700;
  width: 18px;
  height: 18px;
  background: rgba(255, 107, 53, 0.15);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 底部操作按钮 */
.plan-actions {
  padding: 16px 24px;
  background: linear-gradient(135deg, #FAFBFC 0%, #F8FAFC 100%);
  border-top: 1px solid #E5E7EB;
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.btn {
  padding: 11px 22px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  background: #ffffff;
  color: #1A1A2E;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.btn-primary {
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C61 100%);
  border: none;
  color: #ffffff;
  box-shadow: 0 6px 20px rgba(255, 107, 53, 0.35);
}

.btn-primary:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 28px rgba(255, 107, 53, 0.45);
  filter: brightness(1.05);
}

.btn-primary:active {
  transform: translateY(-1px);
}

.btn-default {
  background: #F3F4F6;
  color: #4B5563;
  border: 1px solid #E5E7EB;
}

.btn-default:hover {
  background: rgba(255, 107, 53, 0.1);
  border-color: rgba(255, 107, 53, 0.35);
  color: #FF6B35;
  transform: translateY(-2px);
}

/* 滚动条美化 */
.tab-content::-webkit-scrollbar {
  width: 6px;
}

.tab-content::-webkit-scrollbar-track {
  background: #FAFBFC;
  border-radius: 3px;
}

.tab-content::-webkit-scrollbar-thumb {
  background: #E5E7EB;
  border-radius: 3px;
}

.tab-content::-webkit-scrollbar-thumb:hover {
  background: #9CA3AF;
}

/* 嵌入模式下的额外优化 */
.fitness-plan-preview.embedded-mode .plan-header {
  padding: 18px 20px;
}

.fitness-plan-preview.embedded-mode .user-info-bar {
  padding: 12px 20px;
}

.fitness-plan-preview.embedded-mode .tab-content {
  padding: 18px 20px;
}

.fitness-plan-preview.embedded-mode .plan-actions {
  padding: 14px 20px;
}
</style>
