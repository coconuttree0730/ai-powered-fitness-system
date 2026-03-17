<template>
  <div class="analysis-page">
    <n-card title="数据分析">
      <template #header-extra>
        <n-button type="primary" @click="handleAnalysis" :loading="analysisLoading">
          生成AI分析报告
        </n-button>
      </template>
      <n-tabs v-model:value="activeTab">
        <n-tab-pane name="overview" tab="概览">
          <n-grid :cols="4" :x-gap="20">
            <n-grid-item v-for="stat in stats" :key="stat.title">
              <n-card>
                <n-statistic :label="stat.title" :value="stat.value">
                  <template #prefix>
                    <n-icon :color="stat.color"><component :is="stat.icon" /></n-icon>
                  </template>
                </n-statistic>
              </n-card>
            </n-grid-item>
          </n-grid>
        </n-tab-pane>
        <n-tab-pane name="peak" tab="高峰时间">
          <vue-echarts :option="peakHoursOption" style="height: 400px" />
        </n-tab-pane>
        <n-tab-pane name="courses" tab="课程统计">
          <vue-echarts :option="courseStatsOption" style="height: 400px" />
        </n-tab-pane>
        <n-tab-pane name="members" tab="会员卡统计">
          <vue-echarts :option="memberCardOption" style="height: 400px" />
        </n-tab-pane>
      </n-tabs>
    </n-card>

    <n-modal v-model:show="analysisVisible" title="AI分析报告" preset="card" style="width: 700px">
      <div class="analysis-report" v-if="analysisReport">
        <h3>{{ analysisReport.reportTitle }}</h3>
        <n-divider />
        <div class="report-content">{{ analysisReport.reportContent }}</div>
        <div class="suggestions" v-if="analysisReport.suggestions?.length">
          <h4>优化建议</h4>
          <n-list>
            <n-list-item v-for="(s, i) in analysisReport.suggestions" :key="i">
              {{ s }}
            </n-list-item>
          </n-list>
        </div>
      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { getDashboardStats, getPeakHours, getMemberCardStats, getCourseStats, generateAnalysis } from '@/api/dashboard'

const message = useMessage()
const activeTab = ref('overview')
const analysisVisible = ref(false)
const analysisLoading = ref(false)
const analysisReport = ref(null)

const stats = ref([
  { title: '总会员数', value: 0, icon: 'User', color: '#1890ff' },
  { title: '总课程数', value: 0, icon: 'Calendar', color: '#52c41a' },
  { title: '总预约数', value: 0, icon: 'Tickets', color: '#faad14' },
  { title: '总器材数', value: 0, icon: 'Box', color: '#722ed1' }
])

const peakHoursData = ref([])
const memberCardData = ref({})
const courseStatsData = ref([])

const peakHoursOption = computed(() => ({
  title: { text: '到店高峰时间分布' },
  xAxis: { type: 'category', data: peakHoursData.value.map(d => `${d.hour}:00`) },
  yAxis: { type: 'value', name: '到店人数' },
  series: [{ data: peakHoursData.value.map(d => d.count), type: 'bar', itemStyle: { color: '#1890ff' } }],
  tooltip: { trigger: 'axis' }
}))

const memberCardOption = computed(() => ({
  title: { text: '会员卡销量分布' },
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

const courseStatsOption = computed(() => ({
  title: { text: '课程分类统计' },
  xAxis: { type: 'category', data: courseStatsData.value.map(d => d.categoryName) },
  yAxis: [{ type: 'value', name: '课程数' }, { type: 'value', name: '预约数' }],
  series: [
    { name: '课程数', data: courseStatsData.value.map(d => d.courseCount), type: 'bar', itemStyle: { color: '#1890ff' } },
    { name: '预约数', data: courseStatsData.value.map(d => d.bookingCount), type: 'line', yAxisIndex: 1, itemStyle: { color: '#52c41a' } }
  ],
  tooltip: { trigger: 'axis' },
  legend: { data: ['课程数', '预约数'] }
}))

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
    message.error('生成分析报告失败')
    console.error(error)
  } finally {
    analysisLoading.value = false
  }
}
</script>

<style scoped>
.analysis-page {
  padding: 0;
}

.analysis-report h3 {
  color: #1890ff;
  margin-bottom: 0;
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

.suggestions h4 {
  margin-bottom: 12px;
  color: #666;
}
</style>
