<template>
  <div :class="['fitness-plan-preview', { 'embedded-mode': isEmbedded }]">
    <!-- 用户信息卡片 -->
    <div class="user-info-card">
      <div class="info-item">
        <span class="label">身高</span>
        <span class="value">{{ normalizedPlan.userInfo.height }}</span>
      </div>
      <div class="info-item">
        <span class="label">体重</span>
        <span class="value">{{ normalizedPlan.userInfo.weight }}</span>
      </div>
      <div class="info-item">
        <span class="label">BMI</span>
        <span class="value">{{ normalizedPlan.userInfo.bmi }}</span>
      </div>
      <div class="info-item">
        <span class="label">目标</span>
        <n-tag size="small" type="info">{{ normalizedPlan.userInfo.goal }}</n-tag>
      </div>
      <div class="info-item">
        <span class="label">强度</span>
        <n-tag size="small" :type="getIntensityType(normalizedPlan.userInfo.intensity)">
          {{ normalizedPlan.userInfo.intensity }}
        </n-tag>
      </div>
    </div>

    <!-- 星期标签页 - 使用 Naive UI -->
    <n-tabs v-model:value="activeTab" type="line" animated class="day-tabs">
      <n-tab-pane
        v-for="(day, index) in normalizedPlan.weeklyPlan"
        :key="index"
        :name="index"
        :tab="day.dayName"
      >
        <div class="day-content">
          <!-- 今日焦点 -->
          <div v-if="day.focus" class="focus-banner">
            <n-icon size="18" color="#f97316">
              <InformationCircleOutline />
            </n-icon>
            <span>{{ day.focus }}</span>
          </div>

          <!-- 推荐课程 -->
          <div v-if="day.courses?.length" class="section">
            <div class="section-title">
              <span>推荐课程</span>
              <n-tag size="small" round>{{ day.courses.length }}个课程</n-tag>
            </div>
            <div class="courses-grid">
              <div
                v-for="course in day.courses"
                :key="course.id"
                class="course-card"
                @click="$emit('courseClick', course)"
              >
                <img :src="getValidImageUrl(course.coverImage, 'course', course.name)" :alt="course.name">
                <div class="course-info">
                  <h4>{{ course.name }}</h4>
                  <p class="desc">{{ course.description }}</p>
                  <div class="meta">
                    <n-icon size="14"><TimeOutline /></n-icon>
                    <span>{{ course.duration }}分钟</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 单个课程兼容旧格式 -->
          <div v-else-if="day.course" class="section">
            <div class="section-title">
              <span>推荐课程</span>
              <n-tag size="small" round>1个课程</n-tag>
            </div>
            <div class="courses-grid">
              <div
                class="course-card"
                @click="$emit('courseClick', day.course)"
              >
                <img :src="getValidImageUrl(day.course.coverImage, 'course', day.course.name)" :alt="day.course.name">
                <div class="course-info">
                  <h4>{{ day.course.name }}</h4>
                  <p class="desc">{{ day.course.description }}</p>
                  <div class="meta">
                    <n-icon size="14"><TimeOutline /></n-icon>
                    <span>{{ day.course.duration }}分钟</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 训练动作 -->
          <div v-if="day.exercises?.length" class="section">
            <div class="section-title">
              <span>训练动作</span>
              <n-tag size="small" round type="warning">{{ day.exercises.length }}个动作</n-tag>
            </div>
            <div class="exercises-list">
              <div
                v-for="(exercise, idx) in day.exercises"
                :key="idx"
                class="exercise-item"
              >
                <div class="exercise-number">{{ String(idx + 1).padStart(2, '0') }}</div>
                <div class="exercise-info">
                  <span class="name">{{ exercise.name }}</span>
                  <div class="tags">
                    <n-tag size="small" type="success">{{ exercise.sets }}组</n-tag>
                    <n-tag size="small" type="info">{{ exercise.reps }}次</n-tag>
                    <n-tag size="small" type="warning">休息{{ exercise.restSeconds }}s</n-tag>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 训练器械 -->
          <div v-if="day.equipment?.length" class="section">
            <div class="section-title">
              <span>训练器械</span>
            </div>
            <div class="equipment-grid">
              <div
                v-for="(equip, idx) in day.equipment"
                :key="idx"
                class="equipment-item"
              >
                <img :src="getValidImageUrl(equip.image, 'equip', equip.name)" :alt="equip.name">
                <span>{{ equip.name }}</span>
              </div>
            </div>
          </div>
        </div>
      </n-tab-pane>
    </n-tabs>

    <!-- 底部操作按钮 -->
    <div v-if="showActions" class="plan-actions">
      <n-button @click="$emit('regenerate')">
        <template #icon>
          <n-icon><RefreshOutline /></n-icon>
        </template>
        重新生成
      </n-button>
      <n-button type="primary" @click="$emit('save')">
        <template #icon>
          <n-icon><SaveOutline /></n-icon>
        </template>
        保存计划
      </n-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { NTag, NTabs, NTabPane, NIcon, NButton } from 'naive-ui'
import { InformationCircleOutline, TimeOutline, RefreshOutline, SaveOutline } from '@vicons/ionicons5'

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

function getIntensityType(intensity) {
  if (!intensity || intensity === '--') return 'default'
  if (intensity.includes('高')) return 'error'
  if (intensity.includes('中')) return 'warning'
  return 'success'
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
      '肩部': 'https://images.unsplash.com/photo-1571902949628-41aaea4ce747?w=400&h=250&fit=crop&q=80',
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
  max-width: 1820px;
  min-width: 1200px;
  margin: 0 auto;
  background: #ffffff;
  border-radius: 12px;
  overflow: hidden;
}

/* 嵌入模式样式调整 */
.fitness-plan-preview.embedded-mode {
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  background: #ffffff;
  max-width: 100%;
  min-width: 800px;
  max-height: 85vh;
  display: flex;
  flex-direction: column;
}

/* 用户信息卡片 */
.user-info-card {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  padding: 16px 20px;
  background: linear-gradient(135deg, #fff7ed 0%, #ffedd5 100%);
  border-radius: 12px;
  margin: 16px 20px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-item .label {
  font-size: 13px;
  color: #78716c;
}

.info-item .value {
  font-size: 14px;
  font-weight: 600;
  color: #ea580c;
}

/* 星期标签页 */
.day-tabs :deep(.n-tabs-nav) {
  margin-bottom: 16px;
  padding: 0 20px;
}

.day-content {
  padding: 4px 20px 20px;
}

/* 焦点横幅 */
.focus-banner {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: #fff7ed;
  border-left: 4px solid #f97316;
  border-radius: 8px;
  margin-bottom: 20px;
  font-size: 14px;
  font-weight: 500;
  color: #c2410c;
}

/* 区块样式 */
.section {
  margin-bottom: 24px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 14px;
}

/* 课程网格 */
.courses-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.course-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
}

.course-card:hover {
  border-color: #f97316;
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.15);
  transform: translateY(-2px);
}

.course-card img {
  width: 100%;
  height: 140px;
  object-fit: cover;
}

.course-info {
  padding: 12px 14px;
}

.course-info h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 600;
  color: #1a1a2e;
}

.course-info .desc {
  margin: 0 0 10px 0;
  font-size: 12px;
  color: #6b7280;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-info .meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #f97316;
  font-weight: 500;
}

/* 动作列表 */
.exercises-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.exercise-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  transition: all 0.2s ease;
}

.exercise-item:hover {
  border-color: #f97316;
  background: #fff7ed;
}

.exercise-number {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f97316 0%, #fb923c 100%);
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  border-radius: 8px;
}

.exercise-info {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.exercise-info .name {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a2e;
}

.exercise-info .tags {
  display: flex;
  gap: 8px;
}

/* 器械网格 */
.equipment-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 12px;
}

.equipment-item {
  text-align: center;
  padding: 12px;
  background: #f9fafb;
  border-radius: 10px;
}

.equipment-item img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
  margin-bottom: 8px;
}

.equipment-item span {
  font-size: 12px;
  color: #4b5563;
  font-weight: 500;
}

/* 底部操作按钮 */
.plan-actions {
  padding: 16px 20px;
  background: #fafafa;
  border-top: 1px solid #e5e7eb;
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

/* 滚动条美化 */
:deep(.n-tabs-pane-wrapper) {
  max-height: 60vh;
  overflow-y: auto;
}

:deep(.n-tabs-pane-wrapper)::-webkit-scrollbar {
  width: 6px;
}

:deep(.n-tabs-pane-wrapper)::-webkit-scrollbar-track {
  background: #fafafa;
  border-radius: 3px;
}

:deep(.n-tabs-pane-wrapper)::-webkit-scrollbar-thumb {
  background: #e5e7eb;
  border-radius: 3px;
}

:deep(.n-tabs-pane-wrapper)::-webkit-scrollbar-thumb:hover {
  background: #9ca3af;
}
</style>
