<template>
  <div class="analysis-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>数据分析</span>
          <el-button type="primary" @click="handleAnalysis" :loading="analysisLoading">
            生成AI分析报告
          </el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="概览" name="overview">
          <el-row :gutter="20">
            <el-col :span="6" v-for="stat in stats" :key="stat.title">
              <el-card>
                <el-statistic :title="stat.title" :value="stat.value">
                  <template #prefix>
                    <el-icon :size="20" :color="stat.color">
                      <component :is="stat.icon" />
                    </el-icon>
                  </template>
                </el-statistic>
              </el-card>
            </el-col>
          </el-row>
        </el-tab-pane>
        <el-tab-pane label="高峰时间" name="peak">
          <vue-echarts :option="peakHoursOption" style="height: 400px" />
        </el-tab-pane>
        <el-tab-pane label="课程统计" name="courses">
          <vue-echarts :option="courseStatsOption" style="height: 400px" />
        </el-tab-pane>
        <el-tab-pane label="会员卡统计" name="members">
          <vue-echarts :option="memberCardOption" style="height: 400px" />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="analysisVisible" title="AI分析报告" width="700px" destroy-on-close>
      <div class="analysis-report" v-if="analysisReport">
        <h3>{{ analysisReport.reportTitle }}</h3>
        <el-divider />
        <div class="report-content">{{ analysisReport.reportContent }}</div>
        <div class="suggestions" v-if="analysisReport.suggestions?.length">
          <h4>优化建议</h4>
          <el-list>
            <el-list-item v-for="(s, i) in analysisReport.suggestions" :key="i">
              {{ s }}
            </el-list-item>
          </el-list>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Calendar, Ticket, Box } from '@element-plus/icons-vue'
import { getDashboardStats, getPeakHours, getMemberCardStats, getCourseStats, generateAnalysis } from '@/api/dashboard'

const message = ElMessage
const activeTab = ref('overview')
const analysisVisible = ref(false)
const analysisLoading = ref(false)
const analysisReport = ref(null)

const stats = ref([
  { title: '总会员数', value: 0, icon: 'User', color: '#1890ff' },
  { title: '总课程数', value: 0, icon: 'Calendar', color: '#52c41a' },
  { title: '总预约数', value: 0, icon: 'Ticket', color: '#faad14' },
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
  legend: { bottom: 'bottom' },
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 16px;
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
