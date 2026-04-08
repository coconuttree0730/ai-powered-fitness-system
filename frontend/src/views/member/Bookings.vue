<template>
  <div class="bookings-page">
    <!-- 预约统计卡片 - 使用 Naive UI 组件 -->
    <n-grid :cols="4" :x-gap="16" class="stats-grid">
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
      <n-grid v-else :cols="3" :x-gap="20" :y-gap="20">
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
      <div class="section-header">
        <h3 class="section-title">预约记录</h3>
        <n-radio-group v-model:value="filterStatus" size="small">
          <n-radio-button value="all">全部</n-radio-button>
          <n-radio-button value="booked">已预约</n-radio-button>
          <n-radio-button value="completed">已完成</n-radio-button>
          <n-radio-button value="cancelled">已取消</n-radio-button>
        </n-radio-group>
      </div>
      <n-data-table 
        :columns="columns" 
        :data="filteredBookings" 
        :loading="loading"
        :pagination="{ pageSize: 10 }"
        :bordered="false"
        class="booking-table"
      />
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
import { ref, computed, h, onMounted } from 'vue'
import { NTag, NButton, useMessage, NPopconfirm, NIcon, NCard, NStatistic } from 'naive-ui'
import {
  CalendarOutline,
  CheckmarkCircleOutline,
  CheckmarkDoneOutline,
  CloseCircleOutline
} from '@vicons/ionicons5'

const message = useMessage()
const loading = ref(false)
const cancelLoading = ref(false)
const showCancelModal = ref(false)
const cancelId = ref(null)
const filterStatus = ref('all')

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
</style>
