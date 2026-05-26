<template>
  <div class="admin-sessions">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-icon style="margin-right: 8px;"><Clock /></el-icon>
          <span>排期管理</span>
        </div>
      </template>

      <!-- 搜索区域 -->
      <el-card shadow="never" style="margin-bottom: 16px;">
        <el-row justify="space-between" align="middle">
          <el-col :span="20">
            <el-space wrap>
              <el-date-picker
                v-model="searchForm.dateRange"
                type="daterange"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
                style="width: 280px"
              />
              <el-select
                v-model="searchForm.dayOfWeek"
                placeholder="星期"
                clearable
                style="width: 120px"
              >
                <el-option label="周一" :value="1" />
                <el-option label="周二" :value="2" />
                <el-option label="周三" :value="3" />
                <el-option label="周四" :value="4" />
                <el-option label="周五" :value="5" />
                <el-option label="周六" :value="6" />
                <el-option label="周日" :value="7" />
              </el-select>
              <el-input
                v-model="searchForm.keyword"
                placeholder="课程名/教练名"
                clearable
                style="width: 180px"
                @keyup.enter="handleSearch"
              />
              <el-button type="primary" @click="handleSearch">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-space>
          </el-col>
        </el-row>
      </el-card>

      <el-table
        :data="sessions"
        v-loading="loading"
        :row-key="row => row.id"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="sessionDate" label="上课日期" width="120">
          <template #default="{ row }">
            {{ formatDate(row.sessionDate) }}
          </template>
        </el-table-column>
        <el-table-column label="星期" width="80">
          <template #default="{ row }">
            {{ dayOfWeekLabel(row.dayOfWeek) }}
          </template>
        </el-table-column>
        <el-table-column label="时间" width="140">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }} - {{ formatTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="courseName" label="课程名称" />
        <el-table-column prop="coachName" label="教练" width="100" />
        <el-table-column label="容量" width="120">
          <template #default="{ row }">
            {{ row.bookedCount ?? 0 }} / {{ row.capacity ?? 0 }}
          </template>
        </el-table-column>
        <el-table-column label="剩余" width="80">
          <template #default="{ row }">
            <el-tag :type="getRemainingType(row)" size="small">
              {{ row.remainingCount ?? (row.capacity - (row.bookedCount ?? 0)) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-popconfirm
              v-if="row.status === 0"
              title="确认取消该场次？已预约的会员将收到取消通知。"
              confirm-button-text="确认取消"
              cancel-button-text="再想想"
              @confirm="handleCancel(row)"
            >
              <template #reference>
                <el-button type="danger" size="small" :loading="cancelId === row.id">
                  取消场次
                </el-button>
              </template>
            </el-popconfirm>
            <span v-else-if="row.status === 3" class="text-muted">已取消</span>
            <span v-else-if="row.status === 2" class="text-muted">已结束</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadSessions"
          @current-change="loadSessions"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Clock } from '@element-plus/icons-vue'
import { getAdminSessions, cancelSession } from '@/api/admin/session'

const loading = ref(false)
const sessions = ref([])
const cancelId = ref(null)

const searchForm = reactive({
  dateRange: null,
  dayOfWeek: null,
  keyword: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 20,
  total: 0
})

const dayNames = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日']

function dayOfWeekLabel(val) {
  return dayNames[val] || '-'
}

function formatDate(val) {
  if (!val) return '-'
  const d = new Date(`${val}T00:00:00`)
  return `${d.getMonth() + 1}月${d.getDate()}日`
}

function formatTime(val) {
  if (!val) return '--:--'
  return String(val).slice(0, 5)
}

function getStatusType(status) {
  const map = { 0: 'primary', 1: 'warning', 2: 'info', 3: 'danger' }
  return map[status] || 'info'
}

function getStatusLabel(status) {
  const map = { 0: '待开始', 1: '进行中', 2: '已结束', 3: '已取消' }
  return map[status] || '未知'
}

function getRemainingType(row) {
  const remaining = row.remainingCount ?? (row.capacity - (row.bookedCount ?? 0))
  if (remaining <= 0) return 'danger'
  if (remaining <= 5) return 'warning'
  return 'success'
}

async function loadSessions() {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    if (searchForm.dateRange?.[0]) params.startDate = searchForm.dateRange[0]
    if (searchForm.dateRange?.[1]) params.endDate = searchForm.dateRange[1]
    if (searchForm.dayOfWeek) params.dayOfWeek = searchForm.dayOfWeek
    if (searchForm.keyword) params.keyword = searchForm.keyword

    const res = await getAdminSessions(params)
    sessions.value = res?.records || []
    pagination.total = res?.total || 0
  } catch (e) {
    ElMessage.error(e?.message || '加载排期失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.pageNum = 1
  loadSessions()
}

function handleReset() {
  searchForm.dateRange = null
  searchForm.dayOfWeek = null
  searchForm.keyword = ''
  pagination.pageNum = 1
  loadSessions()
}

async function handleCancel(row) {
  cancelId.value = row.id
  try {
    await cancelSession(row.id)
    ElMessage.success('已取消该场次')
    loadSessions()
  } catch (e) {
    ElMessage.error(e?.message || '取消失败')
  } finally {
    cancelId.value = null
  }
}

onMounted(loadSessions)
</script>

<style scoped>
.admin-sessions {
  padding: 0;
}

.card-header {
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.text-muted {
  color: #909399;
  font-size: 12px;
}
</style>
