<template>
  <div class="coach-page">
    <!-- 我的专属教练 -->
    <div class="card-section">
      <div class="section-header">
        <h3 class="section-title">我的专属教练</h3>
        <n-button v-if="myPrivateCoach" type="primary" size="small" class="book-btn" @click="showBookingModal = true">
          预约课程
        </n-button>
      </div>
      
      <!-- 有专属教练的情况 -->
      <div v-if="myPrivateCoach" class="my-coach">
        <div class="coach-avatar-large" :style="getAvatarStyle(myPrivateCoach)">
          <img v-if="myPrivateCoach.personalImageUrl || myPrivateCoach.avatar" :src="myPrivateCoach.personalImageUrl || myPrivateCoach.avatar" class="avatar-img" />
          <span v-else>{{ myPrivateCoach.name ? myPrivateCoach.name[0] : '?' }}</span>
        </div>
        <div class="coach-details">
          <h3>{{ myPrivateCoach.name }}</h3>
          <p class="specialty">{{ formatSpecialty(myPrivateCoach) }}</p>
          <div class="tags" v-if="myPrivateCoach.tags && myPrivateCoach.tags.length > 0">
            <n-tag v-for="(tag, index) in myPrivateCoach.tags.slice(0, 3)" :key="index" size="small" type="success" class="coach-tag">
              {{ tag }}
            </n-tag>
          </div>
          <div class="stats">
            <span v-if="myPrivateCoach.rating" class="rating">★ {{ formatRating(myPrivateCoach.rating) }}分</span>
            <span v-if="myPrivateCoach.studentCount" class="stat-item">学员: {{ myPrivateCoach.studentCount }}人</span>
            <span v-if="myPrivateCoach.workYears" class="stat-item">经验: {{ myPrivateCoach.workYears }}年</span>
          </div>
        </div>
      </div>
      
      <!-- 没有专属教练的空白占位符 -->
      <div v-else class="empty-coach-placeholder">
        <div class="empty-icon">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 12C14.21 12 16 10.21 16 8C16 5.79 14.21 4 12 4C9.79 4 8 5.79 8 8C8 10.21 9.79 12 12 12ZM12 14C9.33 14 4 15.34 4 18V20H20V18C20 15.34 14.67 14 12 14Z" fill="currentColor"/>
          </svg>
        </div>
        <h4 class="empty-title">暂无专属教练</h4>
        <p class="empty-desc">您还没有绑定专属教练，可以从下方推荐教练中选择一位</p>
        <n-button type="primary" size="small" class="choose-btn" @click="scrollToRecommended">
          去选择教练
        </n-button>
      </div>
    </div>

    <!-- 推荐教练 -->
    <div class="card-section" ref="recommendedSection">
      <div class="section-header">
        <h3 class="section-title">推荐教练</h3>
      </div>
      
      <!-- 教练卡片滑动容器 -->
      <div class="coach-slider-container">
        <!-- 左滑动按钮 -->
        <button 
          v-show="canScrollLeft" 
          class="slider-btn slider-btn-left" 
          @click="scrollLeft"
        >
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M15 18L9 12L15 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
        
        <!-- 右滑动按钮 -->
        <button 
          v-show="canScrollRight" 
          class="slider-btn slider-btn-right" 
          @click="scrollRight"
        >
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M9 18L15 12L9 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
        
        <!-- 教练卡片滚动区域 -->
        <div ref="sliderRef" class="coach-slider" @scroll="handleScroll">
          <div 
            v-for="coach in recommendedCoaches" 
            :key="coach.id" 
            class="coach-card-square"
            @click="viewCoachDetail(coach)"
          >
            <div class="card-image-wrapper">
              <img 
                v-if="coach.image" 
                :src="coach.image" 
                class="card-image" 
                :alt="coach.name"
              />
              <div v-else class="card-image-placeholder" :style="{ background: getGradient(coach.id) }">
                <span class="placeholder-text">{{ coach.name ? coach.name[0] : '?' }}</span>
              </div>
              <div class="card-overlay">
                <span class="view-detail">查看详情</span>
              </div>
            </div>
            <div class="card-content">
              <h4 class="card-name">{{ coach.name }}</h4>
              <p class="card-title">{{ coach.title }}</p>
              <div class="card-tags" v-if="coach.tags && coach.tags.length > 0">
                <span v-for="(tag, idx) in coach.tags.slice(0, 2)" :key="idx" class="card-tag">
                  {{ tag }}
                </span>
              </div>
              <div class="card-stats">
                <span v-if="coach.experience" class="stat-badge">{{ coach.experience }}年经验</span>
                <span v-if="coach.rating" class="rating-badge">★ {{ coach.rating }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 训练记录 -->
    <div class="card-section">
      <div class="section-header">
        <h3 class="section-title">训练记录</h3>
      </div>
      
      <!-- 桌面端表格 -->
      <div v-if="!isMobile" class="table-wrapper">
        <n-data-table
          :columns="recordColumns"
          :data="trainingRecords"
          :pagination="false"
          :bordered="false"
          class="record-table"
        />
      </div>
      
      <!-- 移动端训练记录卡片 -->
      <div v-else class="mobile-training-list">
        <div class="training-cards">
          <div 
            v-for="(record, index) in trainingRecords" 
            :key="index"
            class="training-card"
            :style="{ animationDelay: `${index * 100}ms` }"
          >
            <div class="training-header">
              <div class="training-date">
                <span class="day">{{ getDay(record.date) }}</span>
                <span class="month">{{ getMonth(record.date) }}</span>
              </div>
              <n-tag type="success" size="small" round class="status-tag">
                已完成
              </n-tag>
            </div>
            <div class="training-body">
              <h4 class="course-name">{{ record.courseType }}</h4>
              <div class="coach-info">
                <n-icon size="14" :component="PersonOutline" />
                <span>{{ record.coach }}</span>
              </div>
              <div class="training-stats">
                <div class="stat-item">
                  <n-icon size="16" :component="TimeOutline" />
                  <span>{{ record.duration }}</span>
                </div>
                <div class="stat-item highlight">
                  <n-icon size="16" :component="FlameOutline" />
                  <span>{{ record.calories }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 预约课程弹窗 -->
    <n-modal v-model:show="showBookingModal" preset="card" style="width: 500px" title="预约课程">
      <n-form :model="bookingForm" label-placement="left" label-width="80">
        <n-form-item label="课程类型">
          <n-select v-model:value="bookingForm.courseType" :options="courseOptions" />
        </n-form-item>
        <n-form-item label="预约日期">
          <n-date-picker v-model:value="bookingForm.date" type="date" style="width: 100%" />
        </n-form-item>
        <n-form-item label="时间段">
          <n-select v-model:value="bookingForm.timeSlot" :options="timeOptions" />
        </n-form-item>
        <n-form-item label="备注">
          <n-input v-model:value="bookingForm.note" type="textarea" :rows="3" placeholder="请输入备注信息..." />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showBookingModal = false">取消</n-button>
          <n-button type="primary" :loading="bookingLoading" @click="confirmBooking">确认预约</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, h, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { useMessage, NTag, NIcon } from 'naive-ui'
import { PersonOutline, TimeOutline, FlameOutline } from '@vicons/ionicons5'
import { getHomePageCoaches, getMyPrivateCoach } from '@/api/coachDetail'

const message = useMessage()
const recommendedSection = ref(null)
const sliderRef = ref(null)

// 响应式状态
const windowWidth = ref(window.innerWidth)
const isMobile = computed(() => windowWidth.value < 768)

// 监听窗口大小变化
function handleResize() {
  windowWidth.value = window.innerWidth
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

const showBookingModal = ref(false)
const bookingLoading = ref(false)
const myPrivateCoach = ref(null)
const recommendedCoaches = ref([])
const loading = ref(false)

// 滚动状态
const scrollPosition = ref(0)
const maxScroll = ref(0)

const bookingForm = ref({
  courseType: 'muscle',
  date: null,
  timeSlot: '14:00',
  note: ''
})

const courseOptions = [
  { label: '增肌训练', value: 'muscle' },
  { label: '减脂塑形', value: 'fatloss' },
  { label: '体能提升', value: 'fitness' },
  { label: '康复训练', value: 'rehab' }
]

const timeOptions = [
  { label: '09:00 - 10:00', value: '09:00' },
  { label: '10:00 - 11:00', value: '10:00' },
  { label: '14:00 - 15:00', value: '14:00' },
  { label: '15:00 - 16:00', value: '15:00' },
  { label: '18:00 - 19:00', value: '18:00' },
  { label: '19:00 - 20:00', value: '19:00' }
]

// 渐变色数组
const gradients = [
  'linear-gradient(135deg, #2EC4B6, #06D6A0)',
  'linear-gradient(135deg, #FF6B35, #FFD93D)',
  'linear-gradient(135deg, #EF476F, #FFD166)',
  'linear-gradient(135deg, #667eea, #764ba2)',
  'linear-gradient(135deg, #11998e, #38ef7d)',
  'linear-gradient(135deg, #fc4a1a, #f7b733)',
  'linear-gradient(135deg, #7F00FF, #E100FF)',
  'linear-gradient(135deg, #00c6ff, #0072ff)'
]

const recordColumns = [
  { title: '日期', key: 'date' },
  { title: '教练', key: 'coach' },
  { title: '课程类型', key: 'courseType' },
  { title: '时长', key: 'duration' },
  { title: '消耗卡路里', key: 'calories' },
  {
    title: '状态',
    key: 'status',
    render(row) {
      return h(NTag, { type: 'success', size: 'small' }, { default: () => '已完成' })
    }
  }
]

const trainingRecords = [
  { date: '2024-10-15', coach: '张教练', courseType: '增肌训练', duration: '60分钟', calories: '420 kcal', status: 'completed' },
  { date: '2024-10-12', coach: '张教练', courseType: '体能测试', duration: '45分钟', calories: '280 kcal', status: 'completed' },
  { date: '2024-10-08', coach: '张教练', courseType: '核心训练', duration: '50分钟', calories: '350 kcal', status: 'completed' }
]

// 计算属性：是否可以向左滚动
const canScrollLeft = computed(() => scrollPosition.value > 0)

// 计算属性：是否可以向右滚动
const canScrollRight = computed(() => {
  if (!sliderRef.value) return false
  return scrollPosition.value < maxScroll.value - 10
})

// 获取渐变色
function getGradient(id) {
  const index = (id || 0) % gradients.length
  return gradients[index]
}

// 获取头像样式
function getAvatarStyle(coach) {
  if (coach.personalImageUrl || coach.avatar) {
    return { background: 'transparent' }
  }
  return { background: 'linear-gradient(135deg, #2EC4B6, #06D6A0)' }
}

// 格式化专业领域
function formatSpecialty(coach) {
  if (coach.specialties && coach.specialties.length > 0) {
    return '专长: ' + coach.specialties.join('、')
  }
  if (coach.bio) {
    return coach.bio.substring(0, 50) + (coach.bio.length > 50 ? '...' : '')
  }
  return '专业健身教练'
}

// 格式化评分
function formatRating(rating) {
  if (!rating) return '4.9'
  // 如果是百分比格式，转换为分数
  if (rating.includes('%')) {
    return (parseInt(rating) / 20).toFixed(1)
  }
  return rating
}

// 滚动到推荐教练区域
function scrollToRecommended() {
  recommendedSection.value?.scrollIntoView({ behavior: 'smooth' })
}

// 处理滚动事件
function handleScroll() {
  if (!sliderRef.value) return
  scrollPosition.value = sliderRef.value.scrollLeft
  maxScroll.value = sliderRef.value.scrollWidth - sliderRef.value.clientWidth
}

// 向左滚动
function scrollLeft() {
  if (!sliderRef.value) return
  const cardWidth = sliderRef.value.querySelector('.coach-card-square')?.offsetWidth || 280
  const gap = 20
  const scrollAmount = (cardWidth + gap) * 4 // 一次滚动4个卡片
  sliderRef.value.scrollBy({ left: -scrollAmount, behavior: 'smooth' })
}

// 向右滚动
function scrollRight() {
  if (!sliderRef.value) return
  const cardWidth = sliderRef.value.querySelector('.coach-card-square')?.offsetWidth || 280
  const gap = 20
  const scrollAmount = (cardWidth + gap) * 4 // 一次滚动4个卡片
  sliderRef.value.scrollBy({ left: scrollAmount, behavior: 'smooth' })
}

// 查看教练详情
function viewCoachDetail(coach) {
  message.info(`查看 ${coach.name} 的详情功能开发中...`)
}

// 获取推荐教练列表
async function fetchRecommendedCoaches() {
  try {
    // 获取更多教练用于滑动展示
    const data = await getHomePageCoaches(12)
    recommendedCoaches.value = data || []
    
    // 等待DOM更新后计算滚动状态
    nextTick(() => {
      handleScroll()
    })
  } catch (error) {
    console.error('获取推荐教练失败:', error)
    message.error('获取推荐教练失败')
  }
}

// 获取我的专属教练
async function fetchMyPrivateCoach() {
  try {
    const data = await getMyPrivateCoach()
    myPrivateCoach.value = data
  } catch (error) {
    console.error('获取专属教练失败:', error)
    // 如果返回404或null，说明没有专属教练，不显示错误
    myPrivateCoach.value = null
  }
}

function confirmBooking() {
  bookingLoading.value = true
  setTimeout(() => {
    bookingLoading.value = false
    showBookingModal.value = false
    message.success('预约成功！')
    bookingForm.value = { courseType: 'muscle', date: null, timeSlot: '14:00', note: '' }
  }, 1500)
}

// 日期格式化函数
function getDay(dateStr) {
  const date = new Date(dateStr)
  return date.getDate()
}

function getMonth(dateStr) {
  const date = new Date(dateStr)
  const months = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
  return months[date.getMonth()]
}

onMounted(() => {
  fetchRecommendedCoaches()
  fetchMyPrivateCoach()
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleScroll)
})
</script>

<style scoped>
.coach-page {
  max-width: 1400px;
  margin: 0 auto;
}

.card-section {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.06);
  margin-bottom: 24px;
  transition: transform 0.3s, box-shadow 0.3s;
}

.card-section:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(0,0,0,0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #1A1A2E;
  margin: 0;
}

.book-btn {
  padding: 10px 20px;
  font-size: 13px;
  border-radius: 12px;
}

.my-coach {
  display: flex;
  gap: 24px;
  align-items: center;
}

.coach-avatar-large {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: linear-gradient(135deg, #2EC4B6, #06D6A0);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  color: white;
  flex-shrink: 0;
  overflow: hidden;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.coach-details {
  flex: 1;
}

.coach-details h3 {
  font-size: 20px;
  margin-bottom: 8px;
  font-weight: 600;
  color: #1A1A2E;
}

.specialty {
  color: #6B7280;
  margin-bottom: 8px;
  font-size: 14px;
}

.tags {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.coach-tag {
  font-size: 12px;
}

.stats {
  display: flex;
  gap: 16px;
  margin-bottom: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.rating {
  color: #FFD93D;
  font-weight: 600;
}

.stat-item {
  color: #6B7280;
  font-size: 14px;
}

/* 空白占位符样式 */
.empty-coach-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
  text-align: center;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 12px;
  border: 2px dashed #e2e8f0;
}

.empty-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #e0e7ff 0%, #c7d2fe 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.empty-icon svg {
  width: 40px;
  height: 40px;
  color: #6366f1;
}

.empty-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 8px 0;
}

.empty-desc {
  font-size: 14px;
  color: #64748b;
  margin: 0 0 20px 0;
  max-width: 300px;
}

.choose-btn {
  padding: 10px 24px;
  font-size: 14px;
  border-radius: 12px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
}

/* 教练滑动容器 */
.coach-slider-container {
  position: relative;
  padding: 0 20px;
}

/* 滑动按钮 */
.slider-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: white;
  border: none;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  cursor: pointer;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  color: #1A1A2E;
}

.slider-btn:hover {
  background: #f8fafc;
  box-shadow: 0 6px 16px rgba(0,0,0,0.2);
  transform: translateY(-50%) scale(1.05);
}

.slider-btn:active {
  transform: translateY(-50%) scale(0.95);
}

.slider-btn-left {
  left: 0;
}

.slider-btn-right {
  right: 0;
}

.slider-btn svg {
  width: 24px;
  height: 24px;
}

/* 教练滑动区域 */
.coach-slider {
  display: flex;
  gap: 20px;
  overflow-x: auto;
  scroll-behavior: smooth;
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE and Edge */
  padding: 8px 4px;
}

.coach-slider::-webkit-scrollbar {
  display: none; /* Chrome, Safari, Opera */
}

/* 方块教练卡片 */
.coach-card-square {
  flex: 0 0 calc(25% - 15px); /* 4个卡片平均分配，减去gap */
  min-width: 220px;
  max-width: 280px;
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
  transition: all 0.3s ease;
  cursor: pointer;
}

.coach-card-square:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 32px rgba(0,0,0,0.15);
}

.card-image-wrapper {
  position: relative;
  width: 100%;
  aspect-ratio: 1 / 1; /* 正方形 */
  overflow: hidden;
}

.card-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.coach-card-square:hover .card-image {
  transform: scale(1.05);
}

.card-image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder-text {
  font-size: 64px;
  font-weight: 600;
  color: white;
  text-shadow: 0 2px 8px rgba(0,0,0,0.2);
}

.card-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.coach-card-square:hover .card-overlay {
  opacity: 1;
}

.view-detail {
  color: white;
  font-size: 14px;
  font-weight: 500;
  padding: 8px 16px;
  border: 2px solid white;
  border-radius: 20px;
  transition: all 0.3s ease;
}

.view-detail:hover {
  background: white;
  color: #1A1A2E;
}

.card-content {
  padding: 16px;
}

.card-name {
  font-size: 16px;
  font-weight: 600;
  color: #1A1A2E;
  margin: 0 0 6px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-title {
  font-size: 13px;
  color: #6B7280;
  margin: 0 0 10px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-tags {
  display: flex;
  gap: 6px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.card-tag {
  font-size: 11px;
  padding: 4px 10px;
  background: #f3f4f6;
  color: #4b5563;
  border-radius: 12px;
  white-space: nowrap;
}

.card-stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-badge {
  font-size: 12px;
  color: #6B7280;
}

.rating-badge {
  font-size: 13px;
  font-weight: 600;
  color: #FFD93D;
}

.record-table :deep(.n-data-table-th) {
  font-weight: 600;
  color: #6B7280;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding: 14px 16px;
}

.record-table :deep(.n-data-table-td) {
  padding: 14px 16px;
  font-size: 14px;
}

/* 响应式调整 */
@media (max-width: 1200px) {
  .coach-card-square {
    flex: 0 0 calc(33.333% - 14px); /* 3个卡片 */
  }
}

@media (max-width: 900px) {
  .coach-card-square {
    flex: 0 0 calc(50% - 10px); /* 2个卡片 */
  }
}

@media (max-width: 768px) {
  .my-coach {
    flex-direction: column;
    text-align: center;
  }
  
  .coach-avatar-large {
    width: 80px;
    height: 80px;
    font-size: 32px;
  }
  
  .stats {
    justify-content: center;
  }
  
  .tags {
    justify-content: center;
  }
  
  .coach-card-square {
    flex: 0 0 calc(50% - 10px); /* 2个卡片 */
    min-width: 160px;
  }
  
  .placeholder-text {
    font-size: 48px;
  }
}

@media (max-width: 480px) {
  .coach-card-square {
    flex: 0 0 calc(100% - 0px); /* 1个卡片 */
  }
  
  .slider-btn {
    width: 36px;
    height: 36px;
  }
  
  .slider-btn svg {
    width: 20px;
    height: 20px;
  }
}

/* ==================== 移动端训练记录卡片样式 ==================== */
.mobile-training-list {
  padding: 8px 0;
}

.training-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.training-card {
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  border: 1px solid #f1f5f9;
  display: flex;
  gap: 16px;
  align-items: flex-start;
  animation: slideInUp 0.5s ease forwards;
  opacity: 0;
  transition: all 0.3s ease;
}

.training-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.training-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.training-date {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C61 100%);
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.3);
}

.training-date .day {
  font-size: 22px;
  font-weight: 700;
  line-height: 1;
}

.training-date .month {
  font-size: 11px;
  opacity: 0.9;
  margin-top: 2px;
}

.status-tag {
  font-size: 10px;
}

.training-body {
  flex: 1;
  min-width: 0;
}

.course-name {
  font-size: 16px;
  font-weight: 600;
  color: #1A1A2E;
  margin: 0 0 8px 0;
}

.coach-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #6B7280;
  margin-bottom: 12px;
}

.training-stats {
  display: flex;
  gap: 16px;
}

.training-stats .stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #6B7280;
  background: #f1f5f9;
  padding: 6px 12px;
  border-radius: 20px;
}

.training-stats .stat-item.highlight {
  background: linear-gradient(135deg, #fff5f2 0%, #ffe8e0 100%);
  color: #FF6B35;
}

.training-stats .stat-item :deep(.n-icon) {
  color: inherit;
}

/* 响应式调整 */
@media (max-width: 480px) {
  .training-card {
    padding: 16px;
    gap: 12px;
  }
  
  .training-date {
    width: 48px;
    height: 48px;
    border-radius: 14px;
  }
  
  .training-date .day {
    font-size: 18px;
  }
  
  .training-date .month {
    font-size: 10px;
  }
  
  .course-name {
    font-size: 15px;
  }
  
  .training-stats {
    gap: 10px;
  }
  
  .training-stats .stat-item {
    font-size: 12px;
    padding: 5px 10px;
  }
}
</style>
