<template>
  <div class="dashboard-page">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in stats" :key="stat.title">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" :style="{ background: stat.color }">
              <el-icon :size="24"><component :is="stat.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-title">{{ stat.title }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>到店高峰时间</span>
            </div>
          </template>
          <vue-echarts :option="peakHoursOption" style="height: 300px" />
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>会员卡销量</span>
            </div>
          </template>
          <vue-echarts :option="memberCardOption" style="height: 300px" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>课程统计</span>
              <el-button type="primary" @click="handleAnalysis" :loading="analysisLoading">
                智能分析
              </el-button>
            </div>
          </template>
          <vue-echarts :option="courseStatsOption" style="height: 300px" />
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="analysisVisible" title="AI分析报告" width="600px">
      <div class="analysis-report" v-if="analysisReport">
        <h4>{{ analysisReport.reportTitle }}</h4>
        <div class="report-content">{{ analysisReport.reportContent }}</div>
        <div class="suggestions" v-if="analysisReport.suggestions?.length">
          <h5>建议：</h5>
          <ul>
            <li v-for="(s, i) in analysisReport.suggestions" :key="i">{{ s }}</li>
          </ul>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { User, Calendar, Box, Tickets } from '@element-plus/icons-vue'
import { getDashboardStats, getPeakHours, getMemberCardStats, getCourseStats, generateAnalysis } from '@/api/dashboard'

const stats = ref([
  { title: '总会员数', value: 0, icon: 'User', color: '#1890ff' },
  { title: '总课程数', value: 0, icon: 'Calendar', color: '#52c41a' },
  { title: '总预约数', value: 0, icon: 'Tickets', color: '#faad14' },
  { title: '总器材数', value: 0, icon: 'Box', color: '#722ed1' }
])

const peakHoursData = ref([])
const memberCardData = ref({})
const courseStatsData = ref([])
const analysisVisible = ref(false)
const analysisLoading = ref(false)
const analysisReport = ref(null)

const peakHoursOption = computed(() => {
  const data = peakHoursData.value || []
  return {
    xAxis: { type: 'category', data: data.map(d => `${d.hour}:00`) },
    yAxis: { type: 'value' },
    series: [{ data: data.map(d => d.count), type: 'bar', itemStyle: { color: '#1890ff' } }],
    tooltip: { trigger: 'axis' }
  }
})

const memberCardOption = computed(() => ({
  series: [{
    type: 'pie',
    radius: ['40%', '70%'],
    data: [
      { value: memberCardData.value.monthCard || 0, name: '月卡' },
      { value: memberCardData.value.quarterCard || 0, name: '季卡' },
      { value: memberCardData.value.yearCard || 0, name: '年卡' }
    ]
  }],
  legend: { position: 'bottom' },
  tooltip: { trigger: 'item' }
}))

const courseStatsOption = computed(() => {
  const data = courseStatsData.value || []
  return {
    xAxis: { type: 'category', data: data.map(d => d.categoryName) },
    yAxis: [{ type: 'value', name: '课程数' }, { type: 'value', name: '预约数' }],
    series: [
      { name: '课程数', data: data.map(d => d.courseCount), type: 'bar', itemStyle: { color: '#1890ff' } },
      { name: '预约数', data: data.map(d => d.bookingCount), type: 'line', yAxisIndex: 1, itemStyle: { color: '#52c41a' } }
    ],
    tooltip: { trigger: 'axis' },
    legend: { data: ['课程数', '预约数'] }
  }
})

onMounted(async () => {
  await Promise.all([fetchStats(), fetchPeakHours(), fetchMemberCards(), fetchCourseStats()])
})

async function fetchStats() {
  try {
    const res = await getDashboardStats()
    if (res.data) {
      stats.value[0].value = res.data.totalMembers || 0
      stats.value[1].value = res.data.totalCourses || 0
      stats.value[2].value = res.data.totalBookings || 0
      stats.value[3].value = res.data.totalEquipment || 0
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

async function fetchPeakHours() {
  try {
    const res = await getPeakHours()
    peakHoursData.value = res.data || []
  } catch (error) {
    console.error('获取高峰时间失败:', error)
  }
}

async function fetchMemberCards() {
  try {
    const res = await getMemberCardStats()
    memberCardData.value = res.data || {}
  } catch (error) {
    console.error('获取会员卡统计失败:', error)
  }
}

async function fetchCourseStats() {
  try {
    const res = await getCourseStats()
    courseStatsData.value = res.data || []
  } catch (error) {
    console.error('获取课程统计失败:', error)
  }
}

async function handleAnalysis() {
  analysisLoading.value = true
  try {
    const res = await generateAnalysis({ analysisType: 'OVERALL' })
    if (res.data) {
      analysisReport.value = res.data
      analysisVisible.value = true
    }
  } catch (error) {
    console.error('生成分析报告失败:', error)
  } finally {
    analysisLoading.value = false
  }
}
</script>

<style scoped>
.stat-card {
  border-radius: 8px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #333;
}

.stat-title {
  font-size: 14px;
  color: #666;
  margin-top: 4px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.analysis-report h4 {
  margin-bottom: 16px;
  color: #1890ff;
}

.report-content {
  white-space: pre-wrap;
  line-height: 1.8;
  color: #333;
}

.suggestions {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #eee;
}

.suggestions h5 {
  margin-bottom: 12px;
}

.suggestions li {
  margin-bottom: 8px;
  color: #666;
}
</style>
