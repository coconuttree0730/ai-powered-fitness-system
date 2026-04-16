<template>
  <div class="bookings-page">
    <!-- 预约统计卡片 - 使用 Naive UI 组件 -->
    <n-grid :cols="isMobile ? 2 : 4" :x-gap="isMobile ? 8 : 16" :y-gap="isMobile ? 8 : 0" class="stats-grid">
      <n-grid-item>
        <n-card class="stat-card" :bordered="false" size="small">
          <n-statistic :value="stats.total">
            <template #prefix>
              <n-icon :component="CalendarOutline" class="stat-icon stat-icon-total" />
            </template>
            <template #label>
              <span class="stat-label">总预约</span>
            </template>
          </n-statistic>
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card class="stat-card" :bordered="false" size="small">
          <n-statistic :value="stats.upcoming">
            <template #prefix>
              <n-icon :component="CheckmarkCircleOutline" class="stat-icon stat-icon-upcoming" />
            </template>
            <template #label>
              <span class="stat-label">即将开始</span>
            </template>
          </n-statistic>
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card class="stat-card" :bordered="false" size="small">
          <n-statistic :value="stats.completed">
            <template #prefix>
              <n-icon :component="CheckmarkDoneOutline" class="stat-icon stat-icon-completed" />
            </template>
            <template #label>
              <span class="stat-label">已完成</span>
            </template>
          </n-statistic>
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card class="stat-card" :bordered="false" size="small">
          <n-statistic :value="stats.cancelled">
            <template #prefix>
              <n-icon :component="CloseCircleOutline" class="stat-icon stat-icon-cancelled" />
            </template>
            <template #label>
              <span class="stat-label">已取消</span>
            </template>
          </n-statistic>
        </n-card>
      </n-grid-item>
    </n-grid>

    <!-- 即将开始的课程 -->
    <div class="card-section">
      <div class="section-header">
        <h3 class="section-title">即将开始的课程</h3>
      </div>
      <div v-if="upcomingBookings.length === 0" class="empty-state">
        <n-empty description="暂无即将开始的课程" />
      </div>
      <n-grid v-else :cols="isMobile ? 1 : isTablet ? 2 : 3" :x-gap="16" :y-gap="16">
        <n-grid-item v-for="booking in upcomingBookings" :key="booking.id">
          <div class="booking-card">
            <div class="booking-header">
              <n-tag :type="getStatusType(booking.status)" size="small" round>
                {{ getStatusText(booking.status) }}
              </n-tag>
              <span class="booking-time">{{ formatDate(booking.startTime) }}</span>
            </div>
            <div class="booking-body">
              <h4 class="course-name">{{ booking.courseName }}</h4>
              <div class="coach-info">
                <div class="coach-avatar-small">{{ booking.coachName?.[0] || '教' }}</div>
                <div class="coach-detail">
                  <span class="coach-name">{{ booking.coachName }}</span>
                  <span class="course-time">{{ formatTimeRange(booking.startTime, booking.endTime) }}</span>
                </div>
              </div>
            </div>
            <div class="booking-footer">
              <n-button 
                v-if="booking.status === 'booked'" 
                type="error" 
                size="small" 
                @click="handleCancel(booking.id)"
              >
                取消预约
              </n-button>
              <n-button v-else size="small" disabled>已取消</n-button>
            </div>
          </div>
        </n-grid-item>
      </n-grid>
    </div>

    <!-- 预约记录表 -->
    <div class="card-section">
      <div class="section-header" :class="{ 'mobile-header': isMobile }">
        <h3 class="section-title">预约记录</h3>
        <n-radio-group v-model:value="filterStatus" size="small">
          <n-radio-button value="all">全部</n-radio-button>
          <n-radio-button value="booked">已预约</n-radio-button>
          <n-radio-button value="completed">已完成</n-radio-button>
          <n-radio-button value="cancelled">已取消</n-radio-button>
        </n-radio-group>
      </div>
      
      <!-- 桌面端表格 -->
      <div v-if="!isMobile" class="table-wrapper">
        <n-data-table 
          :columns="columns" 
          :data="filteredBookings" 
          :loading="loading"
          :pagination="{ pageSize: 10 }"
          :bordered="false"
          class="booking-table"
        />
      </div>
      
      <!-- 移动端预约记录卡片 -->
      <div v-else class="mobile-booking-list">
        <n-spin :show="loading">
          <n-empty v-if="filteredBookings.length === 0" description="暂无预约记录" />
          <div v-else class="booking-cards">
            <div 
              v-for="(booking, index) in filteredBookings" 
              :key="booking.id"
              class="booking-record-card"
              :class="booking.status"
              :style="{ animationDelay: `${index * 80}ms` }"
            >
              <div class="booking-record-header">
                <div class="booking-date-badge">
                  <span class="day">{{ getDay(booking.startTime) }}</span>
                  <span class="month">{{ getMonth(booking.startTime) }}</span>
                </div>
                <div class="booking-status-wrapper">
                  <n-tag :type="getStatusType(booking.status)" size="small" round>
                    {{ getStatusText(booking.status) }}
                  </n-tag>
                </div>
              </div>
              <div class="booking-record-body">
                <h4 class="course-title">{{ booking.courseName }}</h4>
                <div class="booking-meta">
                  <div class="meta-item">
                    <n-icon size="14" :component="PersonOutline" />
                    <span>{{ booking.coachName }}</span>
                  </div>
                  <div class="meta-item">
                    <n-icon size="14" :component="TimeOutline" />
                    <span>{{ formatTimeRange(booking.startTime, booking.endTime) }}</span>
                  </div>
                </div>
              </div>
              <div class="booking-record-footer" v-if="booking.status === 'booked'">
                <n-button type="error" size="small" @click="handleCancel(booking.id)">
                  取消预约
                </n-button>
              </div>
            </div>
          </div>
        </n-spin>
      </div>
    </div>

    <!-- 取消预约确认弹窗 -->
    <n-modal v-model:show="showCancelModal" preset="dialog" title="确认取消预约" type="warning">
      <p>确定要取消这门课程的预约吗？取消后将无法恢复。</p>
      <template #action>
        <n-button @click="showCancelModal = false">再想想</n-button>
        <n-button type="error" :loading="cancelLoading" @click="confirmCancel">确认取消</n-button>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, computed, h, onMounted, onUnmounted } from 'vue'
import { NTag, NButton, useMessage, NPopconfirm, NIcon, NCard, NStatistic, NSpin, NEmpty } from 'naive-ui'
import {
  CalendarOutline,
  CheckmarkCircleOutline,
  CheckmarkDoneOutline,
  CloseCircleOutline,
  PersonOutline,
  TimeOutline
} from '@vicons/ionicons5'

const message = useMessage()
const loading = ref(false)
const cancelLoading = ref(false)
const showCancelModal = ref(false)
const cancelId = ref(null)
const filterStatus = ref('all')

// 响应式状态
const windowWidth = ref(window.innerWidth)
const isMobile = computed(() => windowWidth.value < 768)
const isTablet = computed(() => windowWidth.value >= 768 && windowWidth.value < 1024)

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

// 模拟数据
const bookings = ref([
  { id: 1, courseName: '增肌训练', coachName: '张教练', startTime: '2024-10-20T14:00:00', endTime: '2024-10-20T15:00:00', status: 'booked' },
  { id: 2, courseName: '瑜伽基础', coachName: '李教练', startTime: '2024-10-18T10:00:00', endTime: '2024-10-18T11:00:00', status: 'completed' },
  { id: 3, courseName: 'HIIT燃脂', coachName: '王教练', startTime: '2024-10-15T18:00:00', endTime: '2024-10-15T19:00:00', status: 'cancelled' },
  { id: 4, courseName: '核心训练', coachName: '张教练', startTime: '2024-10-22T16:00:00', endTime: '2024-10-22T17:00:00', status: 'booked' },
  { id: 5, courseName: '普拉提', coachName: '李教练', startTime: '2024-10-12T09:00:00', endTime: '2024-10-12T10:00:00', status: 'completed' },
])

const stats = computed(() => {
  return {
    total: bookings.value.length,
    upcoming: bookings.value.filter(b => b.status === 'booked').length,
    completed: bookings.value.filter(b => b.status === 'completed').length,
    cancelled: bookings.value.filter(b => b.status === 'cancelled').length
  }
})

const upcomingBookings = computed(() => {
  return bookings.value.filter(b => b.status === 'booked').slice(0, 3)
})

const filteredBookings = computed(() => {
  if (filterStatus.value === 'all') return bookings.value
  return bookings.value.filter(b => b.status === filterStatus.value)
})

const columns = [
  { title: '课程名称', key: 'courseName' },
  { title: '教练', key: 'coachName' },
  { 
    title: '预约日期', 
    key: 'startTime',
    render: (row) => formatDate(row.startTime)
  },
  { 
    title: '时间段', 
    key: 'timeRange',
    render: (row) => formatTimeRange(row.startTime, row.endTime)
  },
  {
    title: '状态',
    key: 'status',
    render: (row) => h(NTag, { type: getStatusType(row.status), size: 'small' }, () => getStatusText(row.status))
  },
  {
    title: '操作',
    key: 'actions',
    render: (row) => {
      if (row.status === 'booked') {
        return h(NButton, { 
          type: 'error', 
          size: 'small',
          onClick: () => handleCancel(row.id)
        }, () => '取消预约')
      }
      return h('span', { style: { color: '#999' } }, '-')
    }
  }
]

// 日期格式化函数
function getDay(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.getDate()
}

function getMonth(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const months = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
  return months[date.getMonth()]
}

function getStatusType(status) {
  const map = { booked: 'success', completed: 'default', cancelled: 'error' }
  return map[status] || 'default'
}

function getStatusText(status) {
  const map = { booked: '已预约', completed: '已完成', cancelled: '已取消' }
  return map[status] || status
}

function formatDate(time) {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

function formatTimeRange(start, end) {
  if (!start || !end) return ''
  const s = new Date(start)
  const e = new Date(end)
  return `${String(s.getHours()).padStart(2, '0')}:${String(s.getMinutes()).padStart(2, '0')} - ${String(e.getHours()).padStart(2, '0')}:${String(e.getMinutes()).padStart(2, '0')}`
}

function handleCancel(id) {
  cancelId.value = id
  showCancelModal.value = true
}

function confirmCancel() {
  cancelLoading.value = true
  setTimeout(() => {
    const booking = bookings.value.find(b => b.id === cancelId.value)
    if (booking) {
      booking.status = 'cancelled'
      message.success('预约已取消')
    }
    cancelLoading.value = false
    showCancelModal.value = false
    cancelId.value = null
  }, 800)
}

onMounted(() => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 500)
})
</script>

<style scoped>
.bookings-page {
  max-width: 1400px;
  margin: 0 auto;
}

.stats-grid {
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 12px;
  transition: all 0.3s;
  cursor: pointer;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.08);
}

.stat-card :deep(.n-statistic) {
  display: flex;
  flex-direction: row-reverse;
  align-items: center;
  justify-content: space-between;
}

.stat-card :deep(.n-statistic__label) {
  margin-bottom: 0;
  margin-top: 4px;
}

.stat-card :deep(.n-statistic-value) {
  font-size: 32px;
  font-weight: 700;
  color: #1A1A2E;
}

.stat-icon {
  font-size: 24px;
}

.stat-icon-total {
  color: #FF6B35;
}

.stat-icon-upcoming {
  color: #06D6A0;
}

.stat-icon-completed {
  color: #667eea;
}

.stat-icon-cancelled {
  color: #EF476F;
}

.stat-label {
  font-size: 14px;
  color: #6B7280;
}

.card-section {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.06);
  margin-bottom: 24px;
  transition: all 0.3s;
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

.empty-state {
  padding: 60px 0;
}

.booking-card {
  background: #F8FAFC;
  border-radius: 16px;
  padding: 20px;
  transition: all 0.3s;
  border: 1px solid transparent;
}

.booking-card:hover {
  background: white;
  border-color: #E5E7EB;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
}

.booking-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.booking-time {
  font-size: 13px;
  color: #6B7280;
}

.booking-body {
  margin-bottom: 16px;
}

.course-name {
  font-size: 18px;
  font-weight: 600;
  color: #1A1A2E;
  margin: 0 0 12px 0;
}

.coach-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.coach-avatar-small {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #2EC4B6, #06D6A0);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 16px;
  font-weight: 600;
}

.coach-detail {
  display: flex;
  flex-direction: column;
}

.coach-name {
  font-size: 14px;
  font-weight: 600;
  color: #1A1A2E;
}

.course-time {
  font-size: 13px;
  color: #6B7280;
}

.booking-footer {
  display: flex;
  justify-content: flex-end;
}

.booking-table :deep(.n-data-table-th) {
  font-weight: 600;
  color: #6B7280;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding: 14px 16px;
}

.booking-table :deep(.n-data-table-td) {
  padding: 14px 16px;
  font-size: 14px;
}

/* ==================== 响应式适配 ==================== */
@media (max-width: 1024px) {
  .stats-grid {
    margin-bottom: 20px;
  }
  
  .stat-card :deep(.n-statistic-value) {
    font-size: 28px;
  }
  
  .card-section {
    padding: 20px;
    border-radius: 14px;
  }
  
  .section-title {
    font-size: 16px;
  }
}

@media (max-width: 768px) {
  .bookings-page {
    padding: 0;
  }
  
  .stats-grid {
    margin-bottom: 16px;
  }
  
  .stat-card :deep(.n-statistic) {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .stat-card :deep(.n-statistic-value) {
    font-size: 24px;
  }
  
  .stat-icon {
    font-size: 20px;
    margin-bottom: 8px;
  }
  
  .stat-label {
    font-size: 12px;
  }
  
  .card-section {
    padding: 16px;
    border-radius: 12px;
    margin-bottom: 16px;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
    margin-bottom: 16px;
  }
  
  .section-header.mobile-header {
    flex-direction: row;
    flex-wrap: wrap;
    align-items: center;
  }
  
  .section-title {
    font-size: 16px;
  }
  
  .booking-card {
    padding: 16px;
  }
  
  .course-name {
    font-size: 16px;
  }
  
  .coach-avatar-small {
    width: 36px;
    height: 36px;
    font-size: 14px;
  }
  
  .empty-state {
    padding: 40px 0;
  }
  
  .table-responsive {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
    margin: 0 -16px;
    padding: 0 16px;
  }
  
  .booking-table {
    min-width: 100%;
  }
}

@media (max-width: 480px) {
  .stat-card :deep(.n-statistic-value) {
    font-size: 20px;
  }
  
  .stat-icon {
    font-size: 18px;
  }
  
  .section-title {
    font-size: 15px;
  }
  
  .booking-card {
    padding: 14px;
  }
  
  .course-name {
    font-size: 15px;
  }
}

/* ==================== 移动端预约记录卡片样式 ==================== */
.mobile-booking-list {
  padding: 8px 0;
}

.booking-cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.booking-record-card {
  background: white;
  border-radius: 20px;
  padding: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  border: 1px solid #f3f4f6;
  display: flex;
  gap: 14px;
  align-items: flex-start;
  animation: slideInUp 0.4s ease forwards;
  opacity: 0;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.booking-record-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: #e5e7eb;
}

.booking-record-card.booked::before {
  background: linear-gradient(180deg, #FF6B35 0%, #FF8C61 100%);
}

.booking-record-card.completed::before {
  background: linear-gradient(180deg, #06D6A0 0%, #2EC4B6 100%);
}

.booking-record-card.cancelled::before {
  background: #9ca3af;
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

.booking-record-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.booking-record-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.booking-date-badge {
  width: 52px;
  height: 52px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 14px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 2px solid #e2e8f0;
}

.booking-date-badge .day {
  font-size: 20px;
  font-weight: 700;
  color: #1a1a2e;
  line-height: 1;
}

.booking-date-badge .month {
  font-size: 11px;
  color: #6b7280;
  margin-top: 2px;
}

.booking-record-body {
  flex: 1;
  min-width: 0;
}

.course-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 10px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.booking-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #6b7280;
}

.meta-item :deep(.n-icon) {
  color: #9ca3af;
}

.booking-record-footer {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #e5e7eb;
  display: flex;
  justify-content: flex-end;
}

/* 响应式调整 */
@media (max-width: 480px) {
  .booking-record-card {
    padding: 14px;
    gap: 12px;
  }
  
  .booking-date-badge {
    width: 48px;
    height: 48px;
    border-radius: 12px;
  }
  
  .booking-date-badge .day {
    font-size: 18px;
  }
  
  .course-title {
    font-size: 14px;
  }
  
  .meta-item {
    font-size: 12px;
  }
}
</style>
