<template>
  <div :class="['fitness-plan-preview', { 'embedded-mode': isEmbedded }]">
    <!-- 头部 -->
    <div class="plan-header">
      <div class="plan-header-top">
        <div class="plan-title">
        <!--  符号
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M6.5 6.5h11M6.5 17.5h11M3 12h18M6.5 6.5a2.5 2.5 0 1 0-5 0 2.5 2.5 0 1 0 5 0zM17.5 6.5a2.5 2.5 0 1 0-5 0 2.5 2.5 0 1 0 5 0zM6.5 17.5a2.5 2.5 0 1 0-5 0 2.5 2.5 0 1 0 5 0zM17.5 17.5a2.5 2.5 0 1 0-5 0 2.5 2.5 0 1 0 5 0z"/>
          </svg>        
           -->

          <span>健身训练计划</span>
        </div>
      </div>
      <!-- "||" 符号后的是无数据时的默认值 -->
      <!-- 这个是llm返回的副标题，感觉不好看可以注释掉 -->
      <!--
      <div class="plan-subtitle">{{ planData.subtitle || 'AI为您量身定制的7天增肌塑形计划' }}</div>
      -->
    </div>

    <!-- 用户信息 -->
    <div class="user-info-bar">
      <div class="user-stat">身高: <strong>{{ normalizedPlan.userInfo.height }}</strong></div>
      <div class="user-stat">体重: <strong>{{ normalizedPlan.userInfo.weight }}</strong></div>
      <div class="user-stat">BMI: <strong>{{ normalizedPlan.userInfo.bmi }}</strong></div>
      <div class="user-stat">目标: <strong>{{ normalizedPlan.userInfo.goal }}</strong></div>
      <div class="user-stat">强度: <strong>{{ normalizedPlan.userInfo.intensity }}</strong></div>
    </div>

    <!-- Tab标签栏 -->
    <div class="tabs-container">
      <div class="tabs-wrapper">
        <button
          v-for="(day, index) in normalizedPlan.weeklyPlan"
          :key="index"
          :class="['tab-item', { active: activeTab === index }]"
          @click="switchTab(index)"
        >
          <span class="tab-day">{{ day.dayName }}</span>
        </button>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="tab-content" ref="tabContentRef">
      <transition name="fade-slide" mode="out-in">
        <div v-if="normalizedPlan.weeklyPlan && normalizedPlan.weeklyPlan[activeTab]" :key="activeTab" class="day-section">
          <!-- 今日焦点说明 -->
          <div v-if="getCurrentDay().focus" class="day-focus-banner">
            <div class="focus-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/>
              </svg>
            </div>
            <span class="focus-text">{{ getCurrentDay().focus }}</span>
          </div>



         

          <!-- 推荐课程 - 支持1-3个课程卡片 -->
          <div v-if="getCurrentDay().courses && getCurrentDay().courses.length > 0" class="section-block">
            <div class="section-header">

              <!--
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <polygon points="23 7 16 12 23 17 23 7"/><rect x="1" y="5" width="15" height="14" rx="2" ry="2"/>
              </svg>              
              -->
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
                    :src="getValidImageUrl(course.coverImage, 'course', course.name || course.id)"
                    :alt="course.name || `课程${idx + 1}`"
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
                  <div class="course-name">{{ course.name || `课程${idx + 1}` }}</div>
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
                    :src="getValidImageUrl(getCurrentDay().course.coverImage, 'course', getCurrentDay().course.name || getCurrentDay().course.id)"
                    :alt="getCurrentDay().course.name || '推荐课程'"
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
                  <div class="course-name">{{ getCurrentDay().course.name || '推荐课程' }}</div>
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
 <!-- 训练动作 -->
          <div v-if="getCurrentDay().exercises && getCurrentDay().exercises.length > 0" class="section-block">
            <div class="section-header">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M18 8h1a4 4 0 0 1 0 8h-1M2 8h16v9a4 4 0 0 1-4 4H6a4 4 0 0 1-4-4V8zM6 1v3M10 1v3M14 1v3"/>
              </svg>
              <span>训练动作</span>
              <span class="exercise-total-badge">{{ getCurrentDay().exercises.length }}个动作</span>
            </div>
            <div class="exercise-grid">
              <div
                v-for="(exercise, idx) in getCurrentDay().exercises"
                :key="idx"
                class="exercise-card"
              >
                <div class="exercise-card-left">
                  <div class="exercise-rank">
                    <span class="rank-num">{{ String(idx + 1).padStart(2, '0') }}</span>
                  </div>
                  <div class="exercise-main">
                    <div class="exercise-name">{{ exercise.name }}</div>
                    <div class="exercise-tags">
                      <span class="tag tag-set" title="组数">
                        <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M12 20V10M18 20V4M6 20v-4"/></svg>
                        <span class="tag-label">组数</span>
                        <span class="tag-value">{{ exercise.sets }}</span>
                      </span>
                      <span class="tag tag-rep" title="次数">
                        <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
                        <span class="tag-label">次数</span>
                        <span class="tag-value">{{ exercise.reps }}</span>
                      </span>
                      <span class="tag tag-rest" title="休息">
                        <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                        <span class="tag-label">休息</span>
                        <span class="tag-value">{{ exercise.restSeconds }}s</span>
                      </span>
                    </div>
                  </div>
                </div>
                <div class="exercise-card-right">
                  <div class="exercise-progress-ring">
                    <svg viewBox="0 0 36 36" class="ring-svg">
                      <circle cx="18" cy="18" r="15.5" fill="none" stroke="#E5E7EB" stroke-width="3"/>
                      <circle cx="18" cy="18" r="15.5" fill="none" stroke="url(#exerciseGrad)" stroke-width="3"
                        stroke-dasharray="97.39" :stroke-dashoffset="97.39 - (97.39 * Math.min(exercise.sets, 5) / 5)"
                        stroke-linecap="round" class="ring-progress"/>
                      <defs>
                        <linearGradient id="exerciseGrad" x1="0%" y1="0%" x2="100%" y2="0%">
                          <stop offset="0%" stop-color="#FF6B35"/>
                          <stop offset="100%" stop-color="#FF8C61"/>
                        </linearGradient>
                      </defs>
                    </svg>
                    <span class="ring-label">{{ exercise.sets }}</span>
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


        </div>
      </transition>
    </div>

    <!-- 底部操作按钮 -->
    <div v-if="showActions" class="plan-actions">
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
import { ref, computed } from 'vue'

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
  },
  showActions: {
    type: Boolean,
    default: true
  }
})

defineEmits(['regenerate', 'save', 'courseClick'])

const activeTab = ref(0)
const tabContentRef = ref(null)

// 数据规范化：确保任何LLM输出格式都能安全渲染
const normalizedPlan = computed(() => {
  const raw = props.planData
  if (!raw || typeof raw !== 'object') return { subtitle: '', userInfo: {}, weeklyPlan: [] }

  return {
    subtitle: raw.subtitle || 'AI健身训练计划',
    userInfo: {
      height: raw.userInfo?.height || '--',
      weight: raw.userInfo?.weight || '--',
      bmi: raw.userInfo?.bmi || '--',
      goal: raw.userInfo?.goal || '--',
      intensity: raw.userInfo?.intensity || '--'
    },
    weeklyPlan: Array.isArray(raw.weeklyPlan) ? raw.weeklyPlan.map((day, idx) => ({
      dayName: day.dayName || `第${idx + 1}天`,
      focus: day.focus || '',
      courses: normalizeCourses(day.courses),
      course: normalizeSingleCourse(day.course),
      equipment: Array.isArray(day.equipment) ? day.equipment : [],
      exercises: Array.isArray(day.exercises) ? day.exercises : []
    })) : []
  }
})

function normalizeCourses(courses) {
  if (!Array.isArray(courses) || courses.length === 0) return null
  return courses.map((c, idx) => ({
    name: c.name || `课程${idx + 1}`,
    coverImage: c.coverImage || '',
    description: c.description || '',
    duration: c.duration ?? 0,
    id: c.id ?? null,
    category: c.category || ''
  }))
}

function normalizeSingleCourse(course) {
  if (!course) return null
  return {
    name: course.name || '推荐课程',
    coverImage: course.coverImage || '',
    description: course.description || '',
    duration: course.duration ?? 0,
    id: course.id ?? null
  }
}

function switchTab(index) {
  activeTab.value = index
  if (tabContentRef.value) {
    tabContentRef.value.scrollTop = 0
  }
}

function getCurrentDay() {
  if (!normalizedPlan.value.weeklyPlan || !normalizedPlan.value.weeklyPlan[activeTab.value]) {
    return { courses: null, course: null, equipment: [], exercises: [] }
  }
  return normalizedPlan.value.weeklyPlan[activeTab.value]
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
  /*计划表最大宽度*/
  max-width: 1820px;
  /* min-width: 1200px;*/
  min-width: 1200px;
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
  min-width: 800px;
  max-height: 85vh;
  display: flex;
  flex-direction: column;
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
  flex: 1;
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

/* 今日焦点横幅 ***************** */
.day-focus-banner {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 20px;
  margin-bottom: 20px;
  background: linear-gradient(135deg, #FFF5F0 0%, #FFE8DD 50%, #FFF0E6 100%);
  border-left: 4px solid #FF6B35;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(255, 107, 53, 0.1);
  animation: focusBannerIn 0.4s ease;
}

@keyframes focusBannerIn {
  from { opacity: 0; transform: translateY(-8px); }
  to { opacity: 1; transform: translateY(0); }
}

.focus-icon {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C61 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 3px 10px rgba(255, 107, 53, 0.3);
}

.focus-text {
  font-size: 15px;
  font-weight: 700;
  color: #D94E1A;
  line-height: 1.5;
  letter-spacing: 0.3px;
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
  /*课程卡片宽度*/
  max-width: 330px;
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
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
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
  width: 130px;
  height: 130px;
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

/* 训练动作网格 - 现代卡片式布局 */
.exercise-grid {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.exercise-total-badge {
  margin-left: auto;
  font-size: 11px;
  color: #9CA3AF;
  font-weight: 600;
  background: linear-gradient(135deg, #FFF5F0, #FFEEE6);
  padding: 3px 10px;
  border-radius: 20px;
  border: 1px solid rgba(255, 107, 53, 0.15);
}

.exercise-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px;
  background: #ffffff;
  border-radius: 14px;
  border: 1px solid #E8ECF1;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.exercise-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: linear-gradient(180deg, #FF6B35 0%, #FF8C61 50%, #FFB088 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.exercise-card:hover {
  border-color: rgba(255, 107, 53, 0.25);
  box-shadow: 0 6px 24px rgba(255, 107, 53, 0.1), 0 2px 8px rgba(0, 0, 0, 0.04);
  transform: translateY(-2px);
}

.exercise-card:hover::before {
  opacity: 1;
}

.exercise-card-left {
  display: flex;
  align-items: center;
  gap: 14px;
  flex: 1;
  min-width: 0;
}

.exercise-rank {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C61 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.3);
}

.rank-num {
  font-size: 13px;
  font-weight: 800;
  color: #ffffff;
  letter-spacing: -0.5px;
}

.exercise-main {
  flex: 1;
  min-width: 0;
}

.exercise-name {
  font-size: 15px;
  font-weight: 700;
  color: #1A1A2E;
  margin-bottom: 6px;
  line-height: 1.3;
  letter-spacing: 0.2px;
}

.exercise-tags {
  display: flex;
  align-items: center;
  gap: 7px;
  flex-wrap: wrap;
}

.tag {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  font-weight: 500;
  padding: 4px 10px;
  border-radius: 8px;
  line-height: 1.4;
  white-space: nowrap;
  border: 1px solid transparent;
}

.tag svg {
  flex-shrink: 0;
  opacity: 0.85;
}

.tag-label {
  font-size: 11px;
  font-weight: 400;
  opacity: 0.7;
}

.tag-value {
  font-weight: 700;
}

.tag-set {
  color: #D94E1A;
  background: linear-gradient(135deg, #FFF5F0, #FFEDE6);
  border-color: rgba(255, 107, 53, 0.2);
}

.tag-rep {
  color: #047857;
  background: linear-gradient(135deg, #ECFDF5, #D1FAE5);
  border-color: rgba(5, 150, 105, 0.2);
}

.tag-rest {
  color: #4F46E5;
  background: linear-gradient(135deg, #EEF2FF, #E0E7FF);
  border-color: rgba(99, 102, 241, 0.2);
}

.exercise-card-right {
  flex-shrink: 0;
  margin-left: 12px;
}

.exercise-progress-ring {
  position: relative;
  width: 44px;
  height: 44px;
}

.ring-svg {
  width: 44px;
  height: 44px;
  transform: rotate(-90deg);
}

.ring-progress {
  transition: stroke-dashoffset 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.ring-label {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 800;
  color: #FF6B35;
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
  max-height: none;
  min-height: 0;
}

.fitness-plan-preview.embedded-mode .plan-actions {
  padding: 14px 20px;
}
</style>
