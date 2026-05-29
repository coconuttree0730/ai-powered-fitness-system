<template>
  <div class="coach-courses">
    <div class="action-bar">
      <div class="action-right">
        <n-input-group>
          <n-input
            v-model:value="searchForm.courseName"
            placeholder="搜索课程名称..."
            clearable
            style="width: 220px"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <n-icon :component="SearchOutline" />
            </template>
          </n-input>
          <n-select
            v-model:value="searchForm.dayOfWeek"
            :options="weekOptions"
            placeholder="星期"
            style="width: 110px"
            clearable
            @update:value="handleSearch"
          />
        </n-input-group>
      </div>
    </div>

    <n-grid :cols="4" :x-gap="16" class="stats-grid" style="margin-bottom: 24px;">
      <n-grid-item>
        <div class="mini-stat-card">
          <div class="mini-stat-icon primary">
            <n-icon :component="CalendarOutline" size="24" />
          </div>
          <div class="mini-stat-info">
            <div class="mini-stat-value">{{ totalCount }}</div>
            <div class="mini-stat-label">总排课数</div>
          </div>
        </div>
      </n-grid-item>
      <n-grid-item>
        <div class="mini-stat-card">
          <div class="mini-stat-icon success">
            <n-icon :component="FitnessOutline" size="24" />
          </div>
          <div class="mini-stat-info">
            <div class="mini-stat-value">{{ upcomingCount }}</div>
            <div class="mini-stat-label">待开始</div>
          </div>
        </div>
      </n-grid-item>
      <n-grid-item>
        <div class="mini-stat-card">
          <div class="mini-stat-icon warning">
            <n-icon :component="PeopleOutline" size="24" />
          </div>
          <div class="mini-stat-info">
            <div class="mini-stat-value">{{ totalBookings }}</div>
            <div class="mini-stat-label">总预约数</div>
          </div>
        </div>
      </n-grid-item>
      <n-grid-item>
        <div class="mini-stat-card">
          <div class="mini-stat-icon info">
            <n-icon :component="TrendingUpOutline" size="24" />
          </div>
          <div class="mini-stat-info">
            <div class="mini-stat-value">{{ todayCount }}</div>
            <div class="mini-stat-label">今日课程</div>
          </div>
        </div>
      </n-grid-item>
    </n-grid>

    <div class="table-card">
      <n-data-table
        :columns="columns"
        :data="sessions"
        :loading="loading"
        :pagination="pagination"
        :row-key="row => row.id"
      />
    </div>

    <n-modal
      v-model:show="showDetailModal"
      preset="card"
      title="课程详情"
      style="width: 640px"
      :mask-closable="true"
    >
      <div class="course-detail" v-if="currentSession">
        <div class="detail-cover" v-if="currentSession.imageUrl">
          <n-image
            :src="currentSession.imageUrl"
            style="width: 100%; height: 200px; object-fit: cover; border-radius: 8px;"
            :preview-src-list="[currentSession.imageUrl]"
          />
        </div>
        <div class="detail-no-cover" v-else>
          <n-icon :component="ImageOutline" size="48" />
          <span>暂无课程图片</span>
        </div>

        <div class="detail-header">
          <h2 class="detail-title">{{ currentSession.courseName }}</h2>
          <n-tag :type="getCategoryType(currentSession.category)" size="medium">
            {{ currentSession.category || '未知' }}
          </n-tag>
        </div>

        <div class="detail-meta">
          <div class="meta-item">
            <n-icon :component="CalendarOutline" size="18" />
            <span>{{ currentSession.sessionDate }} {{ formatWeek(currentSession.dayOfWeek) }}</span>
          </div>
          <div class="meta-item">
            <n-icon :component="TimeOutline" size="18" />
            <span>{{ currentSession.startTime || '' }} - {{ currentSession.endTime || '' }}</span>
          </div>
          <div class="meta-item">
            <n-icon :component="PeopleOutline" size="18" />
            <span>预约 {{ currentSession.bookedCount || 0 }} / 容量 {{ currentSession.capacity || 0 }}</span>
          </div>
          <div class="meta-item">
            <n-icon :component="PersonOutline" size="18" />
            <span>教练：{{ currentSession.coachName || '-' }}</span>
          </div>
          <div class="meta-item" v-if="currentSession.durationMinutes">
            <n-icon :component="TimerOutline" size="18" />
            <span>{{ currentSession.durationMinutes }} 分钟</span>
          </div>
          <div class="meta-item" v-if="currentSession.caloriesMin && currentSession.caloriesMax">
            <n-icon :component="FlameOutline" size="18" />
            <span>{{ currentSession.caloriesMin }}-{{ currentSession.caloriesMax }} 千卡</span>
          </div>
        </div>

        <div class="detail-tags">
          <n-tag v-if="currentSession.difficultyLevel" type="info" size="small">
            难度：{{ currentSession.difficultyLevel }}
          </n-tag>
          <n-tag :type="getStatusType(currentSession.status)" size="small">
            {{ getStatusLabel(currentSession.status) }}
          </n-tag>
        </div>

        <n-divider />

        <div class="detail-description">
          <h3 class="section-label">课程描述</h3>
          <p>{{ currentSession.description || '暂无课程描述' }}</p>
        </div>
      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, h, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import {
  SearchOutline,
  CalendarOutline,
  FitnessOutline,
  PeopleOutline,
  TrendingUpOutline,
  ImageOutline,
  TimeOutline,
  TimerOutline,
  FlameOutline,
  PersonOutline
} from '@vicons/ionicons5'
import { getCoachMySessions } from '@/api/course'

const message = useMessage()

const loading = ref(false)
const sessions = ref([])
const showDetailModal = ref(false)
const currentSession = ref(null)
const totalCount = ref(0)

const searchForm = reactive({
  courseName: null,
  dayOfWeek: null
})

const weekOptions = [
  { label: '周一', value: 1 },
  { label: '周二', value: 2 },
  { label: '周三', value: 3 },
  { label: '周四', value: 4 },
  { label: '周五', value: 5 },
  { label: '周六', value: 6 },
  { label: '周日', value: 7 }
]

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  onUpdatePage: (page) => {
    pagination.page = page
    fetchSessions()
  },
  onUpdatePageSize: (pageSize) => {
    pagination.pageSize = pageSize
    pagination.page = 1
    fetchSessions()
  }
})

const upcomingCount = computed(() =>
  sessions.value.filter(s => s.status === 0).length
)

const totalBookings = computed(() =>
  sessions.value.reduce((sum, s) => sum + (s.bookedCount || 0), 0)
)

const todayCount = computed(() => {
  const today = new Date().toISOString().split('T')[0]
  return sessions.value.filter(s => s.sessionDate === today).length
})

const columns = [
  {
    title: '上课日期',
    key: 'sessionDate',
    width: 120,
    render: (row) => h('div', { class: 'date-cell' }, [
      h('div', { class: 'date-text' }, row.sessionDate || '-'),
      h('div', { class: 'week-text' }, formatWeek(row.dayOfWeek))
    ])
  },
  {
    title: '时间',
    key: 'time',
    width: 130,
    render: (row) => h('span', { class: 'time-text' },
      `${row.startTime || ''} - ${row.endTime || ''}`)
  },
  {
    title: '课程名称',
    key: 'courseName',
    width: 160,
    ellipsis: { tooltip: true },
    render: (row) => h('span', { class: 'course-name-text' }, row.courseName)
  },
  {
    title: '分类',
    key: 'category',
    width: 100,
    render: (row) => {
      const typeMap = {
        '力量训练': 'info',
        '有氧燃脂': 'error',
        '瑜伽普拉提': 'success',
        '拳击格斗': 'warning'
      }
      return h('span', { class: `category-tag ${typeMap[row.category] || 'default'}` }, row.category || '-')
    }
  },
  {
    title: '预约/容量',
    key: 'booking',
    width: 110,
    render: (row) => h('div', { class: 'capacity-cell' }, [
      h('span', { class: 'booking-count' }, row.bookedCount || 0),
      h('span', { class: 'capacity-separator' }, '/'),
      h('span', { class: 'capacity-total' }, row.capacity || 0)
    ])
  },
  {
    title: '剩余',
    key: 'remaining',
    width: 70,
    render: (row) => {
      const remain = row.remainingCount ?? ((row.capacity || 0) - (row.bookedCount || 0))
      if (remain <= 0) {
        return h('span', { class: 'remaining-full' }, '已满')
      }
      return h('span', { class: 'remaining-text' }, remain)
    }
  },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render: (row) => {
      const labelMap = { 0: '待开始', 1: '进行中', 2: '已结束', 3: '已取消' }
      const colorMap = { 0: 'default', 1: 'success', 2: 'warning', 3: 'error' }
      return h('span', { class: `status-badge ${colorMap[row.status] || 'default'}` }, labelMap[row.status] || '未知')
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 90,
    fixed: 'right',
    render: (row) => h('button', {
      class: 'action-btn view',
      onClick: () => handleView(row)
    }, '查看详情')
  }
]

function formatWeek(dayOfWeek) {
  const map = { 1: '周一', 2: '周二', 3: '周三', 4: '周四', 5: '周五', 6: '周六', 7: '周日' }
  return map[dayOfWeek] || ''
}

function getCategoryType(category) {
  const map = { '力量训练': 'info', '有氧燃脂': 'error', '瑜伽普拉提': 'success', '拳击格斗': 'warning' }
  return map[category] || 'default'
}

function getStatusLabel(status) {
  const map = { 0: '待开始', 1: '进行中', 2: '已结束', 3: '已取消' }
  return map[status] || '未知'
}

function getStatusType(status) {
  const map = { 0: 'default', 1: 'success', 2: 'warning', 3: 'error' }
  return map[status] || 'default'
}

function handleSearch() {
  pagination.page = 1
  fetchSessions()
}

function handleView(row) {
  currentSession.value = { ...row }
  showDetailModal.value = true
}

async function fetchSessions() {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.page,
      pageSize: pagination.pageSize
    }
    if (searchForm.courseName) {
      params.courseName = searchForm.courseName
    }
    if (searchForm.dayOfWeek) {
      params.dayOfWeek = searchForm.dayOfWeek
    }
    const res = await getCoachMySessions(params)
    sessions.value = res?.records || []
    pagination.itemCount = res?.total || 0
    totalCount.value = res?.total || 0
  } catch (error) {
    message.error('获取课程列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchSessions()
})
</script>

<style scoped>
.coach-courses {
  max-width: 1400px;
  margin: 0 auto;
}

.action-bar {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-bottom: 24px;
  gap: 16px;
  flex-wrap: wrap;
}

.action-right {
  display: flex;
  gap: 12px;
}

.mini-stat-card {
  background: white;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  transition: all 0.3s ease;
}

.mini-stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
}

.mini-stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.mini-stat-icon.primary { background: linear-gradient(135deg, #FF6B35, #FF8C61); }
.mini-stat-icon.success { background: linear-gradient(135deg, #06D6A0, #2EC4B6); }
.mini-stat-icon.warning { background: linear-gradient(135deg, #FFD166, #FFB347); }
.mini-stat-icon.info { background: linear-gradient(135deg, #667eea, #764ba2); }

.mini-stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #1A1A2E;
  line-height: 1;
}

.mini-stat-label {
  font-size: 12px;
  color: #6B7280;
  margin-top: 4px;
}

.table-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.06);
}

:deep(.date-cell) {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

:deep(.date-text) {
  font-weight: 600;
  color: #1A1A2E;
  font-size: 13px;
}

:deep(.week-text) {
  font-size: 11px;
  color: #9CA3AF;
}

:deep(.course-name-text) {
  font-weight: 600;
  color: #1A1A2E;
}

:deep(.time-text) {
  color: #555;
  font-size: 13px;
}

:deep(.category-tag) {
  display: inline-block;
  font-size: 12px;
  font-weight: 500;
  padding: 2px 10px;
  border-radius: 12px;
}

:deep(.category-tag.info) { color: #667eea; background: rgba(102,126,234,0.1); }
:deep(.category-tag.error) { color: #EF476F; background: rgba(239,71,111,0.1); }
:deep(.category-tag.success) { color: #06D6A0; background: rgba(6,214,160,0.1); }
:deep(.category-tag.warning) { color: #FFB347; background: rgba(255,179,71,0.1); }
:deep(.category-tag.default) { color: #6B7280; background: rgba(107,114,128,0.1); }

:deep(.capacity-cell) {
  display: flex;
  align-items: center;
  gap: 4px;
}

:deep(.booking-count) {
  font-weight: 600;
  color: #FF6B35;
}

:deep(.capacity-separator) {
  color: #9CA3AF;
}

:deep(.capacity-total) {
  color: #6B7280;
}

:deep(.remaining-text) {
  font-weight: 600;
  color: #06D6A0;
}

:deep(.remaining-full) {
  font-size: 11px;
  color: #EF476F;
  font-weight: 500;
}

:deep(.status-badge) {
  display: inline-block;
  font-size: 12px;
  font-weight: 500;
  padding: 4px 10px;
  border-radius: 20px;
}

:deep(.status-badge.success) { color: #06D6A0; background: rgba(6,214,160,0.1); }
:deep(.status-badge.warning) { color: #FFB347; background: rgba(255,179,71,0.1); }
:deep(.status-badge.error) { color: #EF476F; background: rgba(239,71,111,0.1); }
:deep(.status-badge.default) { color: #6B7280; background: rgba(107,114,128,0.1); }

:deep(.action-btn) {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  border-radius: 6px;
  border: none;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

:deep(.action-btn.view) {
  color: #667eea;
  background: rgba(102,126,234,0.1);
}

:deep(.action-btn.view:hover) {
  background: rgba(102,126,234,0.2);
}

.course-detail {
  padding: 4px 0;
}

.detail-cover {
  margin-bottom: 20px;
}

.detail-no-cover {
  width: 100%;
  height: 150px;
  background: #f5f5f5;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
  gap: 8px;
  margin-bottom: 20px;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.detail-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #1A1A2E;
}

.detail-meta {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #555;
  font-size: 14px;
}

.detail-tags {
  display: flex;
  gap: 8px;
}

.section-label {
  font-size: 15px;
  font-weight: 600;
  color: #1A1A2E;
  margin: 0 0 8px 0;
}

.detail-description p {
  color: #666;
  line-height: 1.7;
  margin: 0;
  white-space: pre-wrap;
}

@media (max-width: 768px) {
  .action-bar {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>