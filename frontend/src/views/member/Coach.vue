<template>
  <div class="coach-page">
    <!-- 我的专属教练 -->
    <div class="card-section">
      <div class="section-header">
        <h3 class="section-title">我的专属教练</h3>
        <n-button v-if="myPrivateCoach" type="primary" size="small" class="book-btn" @click="openBookingModal">
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

    <!-- 预约课程弹窗（日历选择器） -->
    <n-modal v-model:show="showBookingModal" preset="card" style="width: 780px; max-width: 95vw" :title="`预约私教课程 - ${myPrivateCoach?.name || ''}`">
      <div class="booking-calendar-container">
        <div class="booking-calendar-panel">
          <div class="calendar-nav">
            <n-button text @click="calendarPrevMonth">
              <n-icon :component="ChevronBackOutline" size="18" />
            </n-button>
            <span class="calendar-month-label">{{ calendarYearMonth }}</span>
            <n-button text @click="calendarNextMonth">
              <n-icon :component="ChevronForwardOutline" size="18" />
            </n-button>
          </div>
          <div class="calendar-week-header">
            <div v-for="day in weekDayLabels" :key="day" class="week-day-cell">{{ day }}</div>
          </div>
          <div class="calendar-days-grid">
            <div
              v-for="(day, idx) in calendarDays"
              :key="idx"
              class="calendar-day-cell"
              :class="{
                'other-month': !day.isCurrentMonth,
                'is-today': day.isToday,
                'is-selected': selectedDate && day.dateStr === selectedDate,
                'has-bookings': day.bookingCount > 0
              }"
              @click="selectCalendarDate(day)"
            >
              <span class="day-number">{{ day.dayNumber }}</span>
              <span v-if="day.bookingCount > 0" class="day-booking-dot"></span>
            </div>
          </div>
        </div>
        <div class="booking-slots-panel">
          <div class="slots-header">
            <span class="slots-date-label">{{ selectedDateLabel }}</span>
          </div>
          <div class="slots-list" v-if="selectedDateSlots.length > 0">
            <div
              v-for="slot in selectedDateSlots"
              :key="slot.value"
              class="slot-item"
              :class="{
                'slot-free': slot.free,
                'slot-booked': !slot.free,
                'slot-selected': selectedTimeSlot === slot.value && slot.free
              }"
              @click="selectTimeSlot(slot)"
            >
              <div class="slot-time">{{ slot.label }}</div>
              <div class="slot-status">
                <span v-if="!slot.free" class="booked-text">
                  <n-icon :component="PersonOutline" size="12" />
                  {{ slot.bookedBy }}
                </span>
                <span v-else class="free-text">空闲</span>
              </div>
            </div>
          </div>
          <div v-else class="slots-empty">
            <n-empty description="请先选择日期" />
          </div>
        </div>
      </div>
      <div class="booking-note-section">
        <n-input v-model:value="bookingNote" type="textarea" :rows="2" placeholder="备注（可选）" />
      </div>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showBookingModal = false">取消</n-button>
          <n-button type="primary" :loading="bookingLoading" :disabled="!selectedTimeSlot" @click="confirmCalendarBooking">
            确认预约
          </n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 教练详情弹窗 -->
    <n-modal v-model:show="showDetailModal" preset="card" style="width: 640px; max-width: 90vw" title="教练详情">
      <div v-if="coachDetail" class="coach-detail-content">
        <div class="detail-hero">
          <img v-if="coachDetail.personalImageUrl || coachDetail.avatar"
               :src="coachDetail.personalImageUrl || coachDetail.avatar"
               class="detail-hero-img" />
          <div v-else class="detail-hero-placeholder">
            <span>{{ (coachDetail.realName || coachDetail.username || '?')[0] }}</span>
          </div>
        </div>
        <div class="detail-header">
          <h3 class="detail-name">{{ coachDetail.realName || coachDetail.username }}</h3>
          <div class="detail-tags" v-if="coachDetail.tags && coachDetail.tags.length > 0">
            <n-tag v-for="(tag, idx) in coachDetail.tags" :key="idx" size="small" type="success">{{ tag }}</n-tag>
          </div>
          <div class="detail-stats">
            <span v-if="coachDetail.workYears" class="detail-stat">从业 {{ coachDetail.workYears }} 年</span>
            <span v-if="coachDetail.studentCount" class="detail-stat">{{ coachDetail.studentCount }} 名学员</span>
            <span v-if="coachDetail.rating" class="detail-stat">好评率 {{ coachDetail.rating }}</span>
          </div>
        </div>
        <div class="detail-body">
          <div v-if="coachDetail.specialties && coachDetail.specialties.length > 0" class="detail-section">
            <h4 class="detail-section-title">擅长领域</h4>
            <div class="detail-specialties">
              <span v-for="(s, idx) in coachDetail.specialties" :key="idx" class="specialty-tag">{{ s }}</span>
            </div>
          </div>
          <div v-if="coachDetail.teachingStyle" class="detail-section">
            <h4 class="detail-section-title">教学风格</h4>
            <p class="detail-text">{{ coachDetail.teachingStyle }}</p>
          </div>
          <div v-if="coachDetail.bio" class="detail-section">
            <h4 class="detail-section-title">个人简介</h4>
            <p class="detail-text">{{ coachDetail.bio }}</p>
          </div>
          <div v-if="coachDetail.experience" class="detail-section">
            <h4 class="detail-section-title">教学经验</h4>
            <p class="detail-text">{{ coachDetail.experience }}</p>
          </div>
          <div v-if="coachDetail.education" class="detail-section">
            <h4 class="detail-section-title">教育背景</h4>
            <p class="detail-text">{{ coachDetail.education }}</p>
          </div>
          <div v-if="coachDetail.training" class="detail-section">
            <h4 class="detail-section-title">培训经历</h4>
            <p class="detail-text">{{ coachDetail.training }}</p>
          </div>
          <div v-if="coachDetail.honors && coachDetail.honors.length > 0" class="detail-section">
            <h4 class="detail-section-title">荣誉</h4>
            <ul class="detail-list">
              <li v-for="(h, idx) in coachDetail.honors" :key="idx">{{ h }}</li>
            </ul>
          </div>
          <div v-if="coachDetail.languages && coachDetail.languages.length > 0" class="detail-section">
            <h4 class="detail-section-title">语言能力</h4>
            <p class="detail-text">{{ coachDetail.languages.join('、') }}</p>
          </div>
        </div>
      </div>
      <div v-else class="detail-loading">
        <n-spin size="medium" />
        <p>加载教练信息中...</p>
      </div>
      <template #footer>
        <n-space justify="space-between" style="width: 100%">
          <span></span>
          <n-button
            type="primary"
            :disabled="detailBtnDisabled"
            :loading="detailBtnLoading"
            @click="handleBecomeTrainee"
          >
            {{ detailBtnText }}
          </n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 套餐选择弹窗 -->
    <n-modal v-model:show="showPackageModal" preset="card" style="width: 520px; max-width: 90vw" title="选择私教套餐">
      <div class="package-list">
        <div
          v-for="pkg in coachPackages"
          :key="pkg.id"
          class="package-item"
          :class="{ 'package-selected': selectedPackage && selectedPackage.id === pkg.id }"
          @click="handlePackageSelect(pkg)"
        >
          <div class="package-radio">
            <div class="radio-circle" :class="{ 'radio-checked': selectedPackage && selectedPackage.id === pkg.id }">
              <div v-if="selectedPackage && selectedPackage.id === pkg.id" class="radio-dot"></div>
            </div>
          </div>
          <div class="package-info">
            <h4 class="package-name">{{ pkg.name }}</h4>
            <p class="package-desc">{{ pkg.description }}</p>
            <p class="package-price">参考价格: ¥{{ pkg.originalPrice }}</p>
          </div>
        </div>
        <div v-if="coachPackages.length === 0" class="package-empty">
          <p>该教练暂未配置可购买套餐</p>
        </div>
      </div>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showPackageModal = false">取消</n-button>
          <n-button
            type="primary"
            :disabled="!selectedPackage"
            :loading="buyLoading"
            @click="handleConfirmBuy"
          >
            确认购买
          </n-button>
        </n-space>
      </template>
    </n-modal>

  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { useMessage, NTag } from 'naive-ui'
import { PersonOutline, TimeOutline, FlameOutline, ChevronBackOutline, ChevronForwardOutline } from '@vicons/ionicons5'
import { getHomePageCoaches, getMyPrivateCoach, getCoachDetail, getCoachPackages } from '@/api/coachDetail'
import { createCoachPackageOrder, payCoachOrder } from '@/api/coachProduct'
import { createPrivateCoachBooking, getCoachBookingsByRange } from '@/api/privateCoachBooking'
import { getStudentBinding } from '@/api/coach/students'
import { getUnifiedOrderDetail, submitAlipayForm } from '@/api/membership'
import {
  clearPaymentMarker,
  isPaymentFinished,
  markPaymentStarted,
  readPaymentMarker
} from '@/utils/paymentMarker'
import { useAuthStore } from '@/stores/auth'

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

const showDetailModal = ref(false)
const coachDetail = ref(null)
const detailBtnLoading = ref(false)
const selectedCoachId = ref(null)

const showPackageModal = ref(false)
const coachPackages = ref([])
const selectedPackage = ref(null)
const buyLoading = ref(false)

const authStore = useAuthStore()
const currentUserId = computed(() => authStore.userInfo?.id)

// 滚动状态
const scrollPosition = ref(0)
const maxScroll = ref(0)

const bookingNote = ref('')
const calendarMonth = ref(new Date())
const selectedDate = ref(null)
const selectedTimeSlot = ref(null)
const monthBookings = ref([])

const TIME_SLOTS = [
  { label: '09:00 - 10:00', value: '09:00', start: '09:00', end: '10:00' },
  { label: '10:00 - 11:00', value: '10:00', start: '10:00', end: '11:00' },
  { label: '14:00 - 15:00', value: '14:00', start: '14:00', end: '15:00' },
  { label: '15:00 - 16:00', value: '15:00', start: '15:00', end: '16:00' },
  { label: '18:00 - 19:00', value: '18:00', start: '18:00', end: '19:00' },
  { label: '19:00 - 20:00', value: '19:00', start: '19:00', end: '20:00' }
]

const weekDayLabels = ['日', '一', '二', '三', '四', '五', '六']

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

const detailBtnText = computed(() => {
  if (!myPrivateCoach.value || !coachDetail.value) return '成为学员'
  if (myPrivateCoach.value.id === coachDetail.value.userId) return '已是我的教练'
  return '成为学员'
})

const detailBtnDisabled = computed(() => {
  if (!myPrivateCoach.value || !coachDetail.value) return false
  if (myPrivateCoach.value.id === coachDetail.value.userId) return true
  return false
})

// 查看教练详情
async function viewCoachDetail(coach) {
  selectedCoachId.value = coach.id
  coachDetail.value = null
  showDetailModal.value = true
  detailBtnLoading.value = false

  try {
    const data = await getCoachDetail(coach.id)
    coachDetail.value = data
  } catch (error) {
    console.error('获取教练详情失败:', error)
    message.error('获取教练详情失败')
    showDetailModal.value = false
  }
}

async function handleBecomeTrainee() {
  if (myPrivateCoach.value && myPrivateCoach.value.id !== coachDetail.value.userId) {
    message.warning('您已有专属教练，当前版本暂不支持直接切换')
    return
  }

  detailBtnLoading.value = true
  try {
    const packages = await getCoachPackages(selectedCoachId.value)
    coachPackages.value = packages || []
    selectedPackage.value = null
    if (coachPackages.value.length === 0) {
      message.warning('该教练暂未配置可购买套餐')
    } else {
      showPackageModal.value = true
    }
  } catch (error) {
    console.error('获取套餐失败:', error)
    message.error('获取套餐列表失败')
  } finally {
    detailBtnLoading.value = false
  }
}

function handlePackageSelect(pkg) {
  selectedPackage.value = pkg
}

async function handleConfirmBuy() {
  if (!selectedPackage.value) return

  buyLoading.value = true
  try {
    const orderResult = await createCoachPackageOrder(selectedPackage.value.id)

    if (!orderResult || !orderResult.orderNo) {
      message.error('创建订单失败')
      return
    }

    message.info('开始支付，请在支付宝完成付款')
    const payResult = await payCoachOrder(orderResult.orderNo)

    if (!payResult) {
      message.error('支付请求失败')
      return
    }

    if (payResult.payForm) {
      markPaymentStarted({ orderNo: orderResult.orderNo, orderType: 'COACH_PACKAGE' })
      submitAlipayForm(payResult.payForm)
    } else {
      message.error('获取支付表单失败，请稍后重试')
    }
  } catch (error) {
    console.error('支付失败:', error)
    message.error(error.response?.data?.message || error.message || '支付失败，请重试')
  } finally {
    buyLoading.value = false
  }
}

async function refreshAll() {
  await Promise.all([
    fetchMyPrivateCoach(),
    fetchRecommendedCoaches()
  ])
}

async function syncPendingCoachPayment() {
  const marker = readPaymentMarker()
  if (!marker?.orderNo || marker.orderType !== 'COACH_PACKAGE') return

  try {
    const order = await getUnifiedOrderDetail(marker.orderNo)
    if (isPaymentFinished(order)) {
      clearPaymentMarker()
      message.success('支付完成，已为你绑定教练')
      await refreshAll()
    }
  } catch (error) {
    console.error('同步私教套餐支付状态失败:', error)
  }
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

const calendarYearMonth = computed(() => {
  const d = calendarMonth.value
  return `${d.getFullYear()}年${d.getMonth() + 1}月`
})

const calendarDays = computed(() => {
  const year = calendarMonth.value.getFullYear()
  const month = calendarMonth.value.getMonth()
  const firstDay = new Date(year, month, 1)
  const startDayOfWeek = firstDay.getDay()
  const daysInMonth = new Date(year, month + 1, 0).getDate()
  const daysInPrevMonth = new Date(year, month, 0).getDate()
  const today = new Date()
  const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`

  const bookingCountMap = {}
  monthBookings.value.forEach(b => {
    bookingCountMap[b.bookingDate] = (bookingCountMap[b.bookingDate] || 0) + 1
  })

  const days = []

  for (let i = startDayOfWeek - 1; i >= 0; i--) {
    const day = daysInPrevMonth - i
    const dateStr = `${year}-${String(month === 0 ? 12 : month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
    days.push({
      dayNumber: day,
      isCurrentMonth: false,
      isToday: dateStr === todayStr,
      dateStr,
      bookingCount: 0
    })
  }

  for (let i = 1; i <= daysInMonth; i++) {
    const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(i).padStart(2, '0')}`
    days.push({
      dayNumber: i,
      isCurrentMonth: true,
      isToday: dateStr === todayStr,
      dateStr,
      bookingCount: bookingCountMap[dateStr] || 0
    })
  }

  const remainingSlots = 42 - days.length
  for (let i = 1; i <= remainingSlots; i++) {
    days.push({
      dayNumber: i,
      isCurrentMonth: false,
      isToday: false,
      dateStr: '',
      bookingCount: 0
    })
  }

  return days
})

const selectedDateLabel = computed(() => {
  if (!selectedDate.value) return '请选择日期'
  const d = new Date(selectedDate.value + 'T00:00:00')
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const year = d.getFullYear()
  const month = d.getMonth() + 1
  const day = d.getDate()
  return `${year}年${month}月${day}日 ${weekDays[d.getDay()]}`
})

const selectedDateSlots = computed(() => {
  if (!selectedDate.value) return []
  const dateBookings = monthBookings.value.filter(b => b.bookingDate === selectedDate.value)
  return TIME_SLOTS.map(slot => {
    const booked = dateBookings.find(b => {
      const bStart = b.startTime && b.startTime.substring ? b.startTime.substring(0, 5) : ''
      return bStart === slot.value && b.status !== 2
    })
    return {
      value: slot.value,
      label: slot.label,
      start: slot.start,
      end: slot.end,
      free: !booked,
      bookedBy: booked ? booked.userName : null
    }
  })
})

function calendarPrevMonth() {
  const d = new Date(calendarMonth.value)
  d.setMonth(d.getMonth() - 1)
  calendarMonth.value = d
  loadMonthBookings()
}

function calendarNextMonth() {
  const d = new Date(calendarMonth.value)
  d.setMonth(d.getMonth() + 1)
  calendarMonth.value = d
  loadMonthBookings()
}

function selectCalendarDate(day) {
  if (!day.isCurrentMonth) return
  selectedDate.value = day.dateStr
  selectedTimeSlot.value = null
}

function selectTimeSlot(slot) {
  if (!slot.free) return
  selectedTimeSlot.value = slot.value
}

async function loadMonthBookings() {
  const d = calendarMonth.value
  const startDate = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-01`
  const lastDay = new Date(d.getFullYear(), d.getMonth() + 1, 0).getDate()
  const endDate = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(lastDay).padStart(2, '0')}`

  const coachId = myPrivateCoach.value?.id || myPrivateCoach.value?.coachId
  if (!coachId) {
    monthBookings.value = []
    return
  }

  try {
    const data = await getCoachBookingsByRange(coachId, startDate, endDate)
    monthBookings.value = data || []
  } catch {
    monthBookings.value = []
  }
}

async function confirmCalendarBooking() {
  if (!selectedDate.value || !selectedTimeSlot.value) return

  const slot = TIME_SLOTS.find(s => s.value === selectedTimeSlot.value)
  if (!slot) return

  bookingLoading.value = true
  try {
    await createPrivateCoachBooking({
      coachId: myPrivateCoach.value?.id || myPrivateCoach.value?.coachId,
      bookingDate: selectedDate.value,
      startTime: slot.start + ':00',
      endTime: slot.end + ':00',
      note: bookingNote.value || null
    })
    message.success('预约成功！教练确认后即可上课')
    showBookingModal.value = false
    resetBookingState()
  } catch (error) {
    console.error('预约失败:', error)
    const msg = error?.response?.data?.message || error?.message || '预约失败，请稍后重试'
    message.error(msg)
  } finally {
    bookingLoading.value = false
  }
}

function openBookingModal() {
  resetBookingState()
  calendarMonth.value = new Date()
  showBookingModal.value = true
  loadMonthBookings()
}

function resetBookingState() {
  selectedDate.value = null
  selectedTimeSlot.value = null
  bookingNote.value = ''
}

onMounted(() => {
  fetchRecommendedCoaches()
  fetchMyPrivateCoach()
  syncPendingCoachPayment()

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

/* ==================== 教练详情弹窗样式 ==================== */
.coach-detail-content {
  max-height: 60vh;
  overflow-y: auto;
}

.detail-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 0;
  gap: 16px;
  color: #6B7280;
}

.detail-hero {
  width: 100%;
  height: 200px;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 20px;
  background: linear-gradient(135deg, #2EC4B6, #06D6A0);
}

.detail-hero-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-hero-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 72px;
  font-weight: 600;
  color: white;
}

.detail-header {
  margin-bottom: 20px;
}

.detail-name {
  font-size: 22px;
  font-weight: 600;
  color: #1A1A2E;
  margin: 0 0 10px 0;
}

.detail-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.detail-stats {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.detail-stat {
  font-size: 13px;
  color: #6B7280;
  background: #f3f4f6;
  padding: 4px 12px;
  border-radius: 12px;
}

.detail-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-section {
  padding-bottom: 12px;
  border-bottom: 1px solid #f3f4f6;
}

.detail-section:last-child {
  border-bottom: none;
}

.detail-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin: 0 0 8px 0;
}

.detail-text {
  font-size: 14px;
  color: #6B7280;
  line-height: 1.6;
  margin: 0;
}

.detail-list {
  margin: 0;
  padding-left: 20px;
  font-size: 14px;
  color: #6B7280;
  line-height: 1.8;
}

.detail-specialties {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.specialty-tag {
  font-size: 13px;
  padding: 4px 12px;
  background: #ecfdf5;
  color: #059669;
  border-radius: 12px;
}

/* ==================== 套餐选择弹窗样式 ==================== */
.package-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.package-item {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 16px;
  border: 2px solid #f3f4f6;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.package-item:hover {
  border-color: #2EC4B6;
  background: #f0fdfa;
}

.package-selected {
  border-color: #2EC4B6;
  background: #f0fdfa;
}

.package-radio {
  flex-shrink: 0;
  padding-top: 2px;
}

.radio-circle {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid #d1d5db;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.radio-checked {
  border-color: #2EC4B6;
}

.radio-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #2EC4B6;
}

.package-info {
  flex: 1;
  min-width: 0;
}

.package-name {
  font-size: 15px;
  font-weight: 600;
  color: #1A1A2E;
  margin: 0 0 4px 0;
}

.package-desc {
  font-size: 13px;
  color: #6B7280;
  margin: 0 0 6px 0;
  line-height: 1.5;
}

.package-price {
  font-size: 14px;
  font-weight: 600;
  color: #2EC4B6;
  margin: 0;
}

.package-empty {
  text-align: center;
  padding: 32px 0;
  color: #9ca3af;
  font-size: 14px;
}

.booking-calendar-container {
  display: flex;
  gap: 24px;
}

.booking-calendar-panel {
  flex: 0 0 320px;
}

.calendar-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.calendar-month-label {
  font-size: 15px;
  font-weight: 600;
  color: #1A1A2E;
}

.calendar-week-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  margin-bottom: 4px;
}

.week-day-cell {
  text-align: center;
  font-size: 12px;
  font-weight: 600;
  color: #9ca3af;
  padding: 4px 0;
}

.calendar-days-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 2px;
}

.calendar-day-cell {
  position: relative;
  text-align: center;
  padding: 8px 0;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  color: #374151;
  transition: all 0.15s ease;
}

.calendar-day-cell:hover {
  background: #E8F8F5;
}

.calendar-day-cell.other-month {
  color: #d1d5db;
  cursor: default;
}

.calendar-day-cell.is-today {
  font-weight: 700;
  color: #2EC4B6;
}

.calendar-day-cell.is-selected {
  background: #2EC4B6;
  color: #fff;
  font-weight: 600;
}

.calendar-day-cell.is-selected.is-today {
  color: #fff;
}

.day-number {
  display: block;
}

.day-booking-dot {
  display: block;
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #F59E0B;
  margin: 2px auto 0;
}

.calendar-day-cell.is-selected .day-booking-dot {
  background: #fff;
}

.booking-slots-panel {
  flex: 1;
  min-width: 0;
}

.slots-header {
  margin-bottom: 12px;
}

.slots-date-label {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.slots-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.slot-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  cursor: pointer;
  transition: all 0.15s ease;
}

.slot-item.slot-free:hover {
  border-color: #2EC4B6;
  background: #E8F8F5;
}

.slot-item.slot-booked {
  cursor: not-allowed;
  background: #F3F4F6;
  opacity: 0.6;
}

.slot-item.slot-selected {
  border-color: #2EC4B6;
  background: #2EC4B6;
  color: #fff;
}

.slot-time {
  font-size: 14px;
  font-weight: 600;
}

.slot-status {
  font-size: 12px;
}

.free-text {
  color: #10B981;
}

.slot-selected .free-text {
  color: #fff;
}

.booked-text {
  color: #9ca3af;
  display: flex;
  align-items: center;
  gap: 4px;
}

.slots-empty {
  padding: 32px 0;
}

.booking-note-section {
  margin-top: 16px;
}
</style>
