<template>
  <div class="bookings-page">
    <n-spin :show="loading">
      <!-- 可预约课程 -->
      <section class="card-section">
        <div class="section-header">
          <div>
            <h3 class="section-title">本周可预约课程</h3>
            <p class="section-subtitle">{{ weekDisplay }}</p>
          </div>
          <n-button quaternary @click="loadPageData">刷新</n-button>
        </div>

        <div v-if="availableSessions.length === 0" class="empty-state">
          <n-empty description="本周暂无可预约的公开课" />
        </div>

        <div v-else class="session-grid" :class="{ mobile: isMobile }">
          <article
            v-for="session in availableSessions"
            :key="session.id"
            class="session-card"
            :class="{ disabled: session.isBooked }"
          >
            <div
              class="session-cover"
              :style="session.imageUrl ? { backgroundImage: `url(${session.imageUrl})` } : undefined"
            >
              <div class="session-cover-overlay">
                <n-tag :type="session.isBooked ? 'success' : 'warning'" size="small" round>
                  {{ session.isBooked ? '已预约' : '可预约' }}
                </n-tag>
                <span class="session-date-text">{{ formatMonthDay(session.sessionDate) }}</span>
              </div>
            </div>

            <div class="session-content">
              <h4 class="session-title">{{ session.courseName }}</h4>

              <div class="session-meta">
                <div class="meta-row">
                  <span class="meta-label">上课时间</span>
                  <span>{{ formatTimeRange(session.startTime, session.endTime) }}</span>
                </div>
                <div class="meta-row">
                  <span class="meta-label">已预约人数</span>
                  <span>{{ session.bookedCount ?? 0 }} 人</span>
                </div>
                <div class="meta-row">
                  <span class="meta-label">授课教练</span>
                  <span>{{ session.coachName || '待分配' }}</span>
                </div>
              </div>

              <div class="session-actions">
                <n-button
                  v-if="!session.isBooked"
                  type="primary"
                  size="small"
                  :loading="actionSessionId === session.id"
                  @click="handleBook(session)"
                >
                  立即预约
                </n-button>
              </div>
            </div>
          </article>
        </div>
      </section>

      <!-- 已预约课程 -->
      <section v-if="bookedSessions.length > 0" class="card-section booked-section">
        <div class="section-header">
          <div>
            <h3 class="section-title">已预约课程</h3>
            <p class="section-subtitle">您已预约的本周课程</p>
          </div>
        </div>

        <div class="session-grid" :class="{ mobile: isMobile }">
          <article
            v-for="session in bookedSessions"
            :key="session.id"
            class="session-card booking-record"
          >
            <div
              class="session-cover"
              :style="session.imageUrl ? { backgroundImage: `url(${session.imageUrl})` } : undefined"
            >
              <div class="session-cover-overlay">
                <n-tag type="success" size="small" round>已预约</n-tag>
                <span class="session-date-text">{{ formatMonthDay(session.sessionDate) }}</span>
              </div>
            </div>

            <div class="session-content">
              <h4 class="session-title">{{ session.courseName }}</h4>

              <div class="session-meta">
                <div class="meta-row">
                  <span class="meta-label">上课时间</span>
                  <span>{{ formatTimeRange(session.startTime, session.endTime) }}</span>
                </div>
                <div class="meta-row">
                  <span class="meta-label">已预约人数</span>
                  <span>{{ session.bookedCount ?? 0 }} 人</span>
                </div>
                <div class="meta-row">
                  <span class="meta-label">授课教练</span>
                  <span>{{ session.coachName || '待分配' }}</span>
                </div>
              </div>

              <div class="session-actions">
                <n-button
                  type="error"
                  size="small"
                  :loading="cancelLoading && cancelBookingId === session.bookingId"
                  @click="handleCancel(session.bookingId)"
                >
                  取消预约
                </n-button>
              </div>
            </div>
          </article>
        </div>
      </section>
    </n-spin>

    <n-modal v-model:show="showCancelModal" preset="dialog" title="确认取消预约" type="warning">
      <p>确认取消这次课程预约吗？取消后该课程会重新回到上方可预约列表。</p>
      <template #action>
        <n-button @click="showCancelModal = false">再想想</n-button>
        <n-button type="error" :loading="cancelLoading" @click="confirmCancel">确认取消</n-button>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useMessage } from 'naive-ui'

import { bookSession, cancelBooking as cancelBookingApi, getCourseSessions, getMyBookings } from '@/api/course'
import {
  buildWeekSessionCards,
  getTodayKey,
  getWeekEndKey,
  toDateKey
} from '@/utils/bookings.js'

const message = useMessage()

const loading = ref(false)
const cancelLoading = ref(false)
const showCancelModal = ref(false)
const weekSessions = ref([])
const cancelBookingId = ref(null)
const actionSessionId = ref(null)
const windowWidth = ref(window.innerWidth)

const isMobile = computed(() => windowWidth.value < 768)
const weekStartKey = computed(() => getTodayKey(new Date()))
const weekEndKey = computed(() => getWeekEndKey(new Date()))
const weekDisplay = computed(() => {
  const start = formatMonthDay(weekStartKey.value)
  const end = formatMonthDay(weekEndKey.value)
  return `${start} - ${end}`
})

const availableSessions = computed(() => {
  return weekSessions.value.filter((s) => !s.isBooked)
})

const bookedSessions = computed(() => {
  return weekSessions.value.filter((s) => s.isBooked)
})

function handleResize() {
  windowWidth.value = window.innerWidth
}

function formatMonthDay(value) {
  const dateKey = toDateKey(value)
  if (!dateKey) {
    return '--'
  }
  const date = new Date(`${dateKey}T00:00:00`)
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

function formatTime(value) {
  if (!value) {
    return '--:--'
  }
  return String(value).slice(0, 5)
}

function formatTimeRange(startTime, endTime) {
  return `${formatTime(startTime)} - ${formatTime(endTime)}`
}

async function loadPageData() {
  loading.value = true

  try {
    const [sessionPage, bookingList] = await Promise.all([
      getCourseSessions({
        pageNum: 1,
        pageSize: 50,
        startDate: weekStartKey.value,
        endDate: weekEndKey.value
      }),
      getMyBookings()
    ])

    const normalizedBookings = Array.isArray(bookingList) ? bookingList : []
    const sessionRecords = Array.isArray(sessionPage?.records) ? sessionPage.records : []

    weekSessions.value = buildWeekSessionCards(sessionRecords, normalizedBookings, new Date())
  } catch (error) {
    message.error(error?.message || '加载预约数据失败')
  } finally {
    loading.value = false
  }
}

async function handleBook(session) {
  actionSessionId.value = session.id

  try {
    const bookingId = await bookSession(session.id, session.courseId)
    message.success('预约成功')

    const target = weekSessions.value.find((s) => s.id === session.id)
    if (target) {
      target.isBooked = true
      target.bookingId = bookingId
      target.bookedCount = (target.bookedCount ?? 0) + 1
    }
  } catch (error) {
    message.error(error?.message || '预约失败')
  } finally {
    actionSessionId.value = null
  }
}

function handleCancel(bookingId) {
  cancelBookingId.value = bookingId
  showCancelModal.value = true
}

async function confirmCancel() {
  if (!cancelBookingId.value) {
    return
  }

  cancelLoading.value = true

  try {
    await cancelBookingApi(cancelBookingId.value, {
      cancelReason: '会员取消预约'
    })
    message.success('已取消预约')
    showCancelModal.value = false

    const target = weekSessions.value.find((s) => s.bookingId === cancelBookingId.value)
    if (target) {
      target.isBooked = false
      target.bookingId = null
      target.bookedCount = Math.max((target.bookedCount ?? 1) - 1, 0)
    }
  } catch (error) {
    message.error(error?.message || '取消预约失败')
  } finally {
    cancelLoading.value = false
    cancelBookingId.value = null
  }
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  loadPageData()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.bookings-page {
  max-width: 1400px;
  margin: 0 auto;
}

.card-section {
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.06);
  margin-bottom: 24px;
}

.booked-section {
  background: linear-gradient(180deg, #f0fdf4 0%, #ffffff 100%);
  border: 1px solid #bbf7d0;
}

.section-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.section-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #1a1a2e;
}

.section-subtitle {
  margin: 6px 0 0;
  font-size: 13px;
  color: #6b7280;
}

.empty-state {
  padding: 56px 0;
}

.session-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.session-grid.mobile {
  grid-template-columns: 1fr;
}

.session-card {
  overflow: hidden;
  border-radius: 18px;
  border: 1px solid #eef2f7;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  transition: opacity 0.2s ease;
}

.session-card.disabled {
  opacity: 0.55;
  pointer-events: none;
}

.session-card.booking-record {
  border: 2px solid #06d6a0;
}

.session-cover {
  height: 130px;
  background: linear-gradient(135deg, #1a1a2e 0%, #ff6b35 100%);
  background-size: cover;
  background-position: center;
  position: relative;
}

.session-cover-overlay {
  position: absolute;
  inset: 0;
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.2) 0%, rgba(0, 0, 0, 0.45) 100%);
}

.session-date-text {
  color: #fff;
  font-size: 13px;
  font-weight: 600;
}

.session-content {
  padding: 18px;
}

.session-title {
  margin: 0 0 14px;
  font-size: 20px;
  font-weight: 700;
  color: #111827;
}

.session-meta {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.meta-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  font-size: 14px;
  color: #374151;
}

.meta-label {
  color: #6b7280;
}

.session-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
}

@media (max-width: 1024px) {
  .session-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .bookings-page {
    padding: 0;
  }

  .card-section {
    padding: 18px;
    border-radius: 16px;
  }

  .section-title {
    font-size: 18px;
  }

  .session-cover {
    height: 110px;
  }

  .session-title {
    font-size: 18px;
  }

  .meta-row {
    flex-direction: column;
    gap: 4px;
  }
}
</style>
