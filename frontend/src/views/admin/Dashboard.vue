<template>
  <div class="dashboard-page">
    <!-- 页面标题和时间筛选 -->
    <el-row class="page-header" :gutter="20">
      <el-col :span="12">
        <div class="header-title">
          <h2>数据仪表盘</h2>
        </div>
      </el-col>
      <el-col :span="12" class="header-actions">
        <el-radio-group v-model="timeRange" size="small" @change="handleTimeRangeChange">
          <el-radio-button value="today">今日</el-radio-button>
          <el-radio-button value="week">本周</el-radio-button>
          <el-radio-button value="month">本月</el-radio-button>
          <el-radio-button value="year">全年</el-radio-button>
        </el-radio-group>
        <el-button
          type="primary"
          size="small"
          :icon="Refresh"
          :loading="refreshLoading"
          @click="refreshAllData"
          class="refresh-btn"
        >
          刷新数据
        </el-button>
      </el-col>
    </el-row>
    <!-- 核心指标卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :md="8" :lg="4" v-for="(stat, index) in stats" :key="stat.key">
        <el-card
          class="stat-card"
          :class="[`stat-card-${stat.type}`, { 'card-hover': true }]"
          :style="{ animationDelay: `${index * 0.1}s` }"
        >
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-value-wrapper">
                <span class="stat-value">{{ formatNumber(stat.value) }}</span>
                <span
                  class="stat-trend"
                  :class="stat.trend >= 0 ? 'trend-up' : 'trend-down'"
                  v-if="stat.trend !== undefined"
                >
                  <el-icon><component :is="stat.trend >= 0 ? ArrowUp : ArrowDown" /></el-icon>
                  {{ Math.abs(stat.trend) }}%
                </span>
              </div>
              <div class="stat-title">{{ stat.title }}</div>
              <div class="stat-compare" v-if="stat.compareText">{{ stat.compareText }}</div>
            </div>
          </div>
          <div class="stat-progress" v-if="stat.progress !== undefined">
            <el-progress
              :percentage="stat.progress"
              :color="stat.progressColor"
              :show-text="false"
              :stroke-width="4"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- AI智能分析按钮 - 右侧布局 -->
    <el-row :gutter="20" class="chart-row ai-analysis-row">
      <el-col :span="24">
        <div class="ai-analysis-bar">
          <el-button
            type="primary"
            :icon="MagicStick"
            :loading="analysisLoading"
            @click="handleAnalysis"
          >
            {{ analysisLoading ? 'AI 分析中...' : 'AI智能分析' }}
          </el-button>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域第一行 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :lg="16">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <el-icon size="18" color="#1890ff"><TrendCharts /></el-icon>
                <span class="header-title-text">营收趋势分析</span>
              </div>
              <div class="header-right">
                <el-radio-group v-model="revenueChartType" size="small">
                  <el-radio-button value="line">折线图</el-radio-button>
                  <el-radio-button value="bar">柱状图</el-radio-button>
                </el-radio-group>
              </div>
            </div>
          </template>
          <div ref="revenueChartRef" style="height: 320px"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <el-icon size="18" color="#52c41a"><PieChart /></el-icon>
                <span class="header-title-text">会员卡销售占比</span>
              </div>
            </div>
          </template>
          <div ref="memberCardChartRef" style="height: 320px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域第二行 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :lg="24">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <el-icon size="18" color="#722ed1"><Reading /></el-icon>
                <span class="header-title-text">课程分类统计</span>
              </div>
            </div>
          </template>
          <div ref="courseStatsChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域第三行 - 新增模块 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :lg="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <el-icon size="18" color="#13c2c2"><UserFilled /></el-icon>
                <span class="header-title-text">用户增长趋势</span>
              </div>
            </div>
          </template>
          <div ref="userGrowthChartRef" style="height: 280px"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <el-icon size="18" color="#eb2f96"><Box /></el-icon>
                <span class="header-title-text">器材使用状态</span>
              </div>
            </div>
          </template>
          <div ref="equipmentStatusChartRef" style="height: 280px"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <el-icon size="18" color="#f5222d"><Warning /></el-icon>
                <span class="header-title-text">报修处理统计</span>
              </div>
            </div>
          </template>
          <div ref="repairStatsChartRef" style="height: 280px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- AI分析报告对话框 -->
    <el-dialog
      v-model="analysisVisible"
      title="AI智能分析报告"
      width="1200px"
      class="analysis-dialog"
      destroy-on-close
      top="5vh"
    >
      <div class="analysis-report">
        <div class="report-header">
          <div class="header-icon-wrap">
            <el-icon size="28" color="#fff"><MagicStick /></el-icon>
          </div>
          <div class="header-info">
            <h3>{{ analysisReport.reportTitle }}</h3>
            <el-tag type="info" size="small" effect="dark" round>{{ analysisReport.analysisType }}</el-tag>
          </div>
        </div>
        <el-divider />
        <div class="report-body" v-html="renderedContent"></div>
        <div class="suggestions" v-if="analysisReport.suggestions?.length">
          <div class="suggestions-header">
            <el-icon size="18"><Opportunity /></el-icon>
            <span>优化建议</span>
          </div>
          <div class="suggestions-content" v-html="renderedSuggestions"></div>
        </div>
        <div class="report-footer">
          <el-text type="info" size="small">
            <el-icon><Clock /></el-icon>
            生成时间：{{ formatDateTime(analysisReport.generateTime) }}
          </el-text>
          <el-button type="primary" :icon="Document" @click="handleSaveReport" :loading="saveLoading">
            保存到数据分析
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import {
  User, Calendar, Tickets, Box, Goods, Money, ArrowUp, ArrowDown,
  Refresh, TrendCharts, PieChart, Reading, MagicStick,
  UserFilled, Warning, Star, Opportunity, Document
} from '@element-plus/icons-vue'
import {
  getDashboardStats, getMemberCardStats, getCourseStats,
  generateAnalysis, getRevenueTrend, getUserGrowth, getEquipmentStatus,
  getRepairStats
} from '@/api/dashboard'
import { saveAnalysisReport } from '@/api/analysis'

const router = useRouter()

// ECharts 实例
const revenueChartRef = ref(null)
const memberCardChartRef = ref(null)
const courseStatsChartRef = ref(null)
const userGrowthChartRef = ref(null)
const equipmentStatusChartRef = ref(null)
const repairStatsChartRef = ref(null)

let revenueChart = null
let memberCardChart = null
let courseStatsChart = null
let userGrowthChart = null
let equipmentStatusChart = null
let repairStatsChart = null

// 状态变量
const timeRange = ref('week')
const revenueChartType = ref('line')
const refreshLoading = ref(false)
const analysisVisible = ref(false)
const analysisReport = ref(null)
const saveLoading = ref(false)
const analysisLoading = ref(false)

// 数据存储
const dashboardData = ref({})
const memberCardData = ref({})
const courseStatsData = ref([])
const revenueData = ref([])
const userGrowthData = ref([])
const equipmentStatusData = ref({})
const repairStatsData = ref([])

// 定时器
let refreshTimer = null

// Markdown 渲染报告内容
const renderedContent = computed(() => {
  if (!analysisReport.value?.reportContent) return ''
  const raw = analysisReport.value.reportContent
  const html = marked.parse(raw, {
    breaks: true,
    gfm: true,
    headerIds: false,
    mangle: false
  })
  return DOMPurify.sanitize(html)
})

// Markdown 渲染优化建议
const renderedSuggestions = computed(() => {
  if (!analysisReport.value?.suggestions?.length) return ''
  const raw = analysisReport.value.suggestions.join('\n\n')
  const html = marked.parse(raw, {
    breaks: true,
    gfm: true,
    headerIds: false,
    mangle: false
  })
  return DOMPurify.sanitize(html)
})

// 核心指标数据
const stats = computed(() => [
  {
    key: 'members',
    title: '总会员数',
    value: dashboardData.value.totalMembers || 0,
    type: 'primary',
    trend: 12.5,
    compareText: '较上月',
    progress: 78,
    progressColor: '#1890ff'
  },
  {
    key: 'courses',
    title: '总课程数',
    value: dashboardData.value.totalCourses || 0,
    type: 'success',
    trend: 8.2,
    compareText: '较上月',
    progress: 65,
    progressColor: '#52c41a'
  },
  {
    key: 'bookings',
    title: '总预约数',
    value: dashboardData.value.totalBookings || 0,
    type: 'warning',
    trend: -3.1,
    compareText: '较上周',
    progress: 82,
    progressColor: '#fa8c16'
  },
  {
    key: 'equipment',
    title: '总器材数',
    value: dashboardData.value.totalEquipment || 0,
    type: 'purple',
    trend: 5.4,
    compareText: '较上月',
    progress: 90,
    progressColor: '#722ed1'
  },
  {
    key: 'orders',
    title: '今日订单',
    value: dashboardData.value.todayOrders || 0,
    type: 'cyan',
    trend: 15.8,
    compareText: '较昨日',
    progress: 72,
    progressColor: '#13c2c2'
  },
  {
    key: 'revenue',
    title: '今日营收',
    value: dashboardData.value.todayRevenue || 0,
    type: 'pink',
    trend: 22.3,
    compareText: '较昨日',
    progress: 85,
    progressColor: '#eb2f96'
  }
])

// 营收趋势图表配置
const revenueOption = computed(() => {
  const data = revenueData.value || []
  const isLine = revenueChartType.value === 'line'

  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' },
      formatter: (params) => {
        const p = params[0]
        return `${p.name}<br/>${p.marker} 营收: ¥${p.value.toLocaleString()}`
      }
    },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: !isLine,
      data: data.map(d => d.date),
      axisLine: { lineStyle: { color: '#d9d9d9' } },
      axisLabel: { color: '#666' }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: '#f0f0f0' } },
      axisLabel: {
        color: '#666',
        formatter: value => value >= 1000 ? `${(value / 1000).toFixed(1)}k` : value
      }
    },
    series: [{
      name: '营收',
      type: revenueChartType.value,
      data: data.map(d => d.amount),
      smooth: true,
      symbolSize: 8,
      itemStyle: { color: '#1890ff' },
      areaStyle: isLine ? {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(24, 144, 255, 0.3)' },
            { offset: 1, color: 'rgba(24, 144, 255, 0.05)' }
          ]
        }
      } : undefined,
      emphasis: {
        itemStyle: { color: '#096dd9', borderColor: '#fff', borderWidth: 2 }
      }
    }]
  }
})

// 会员卡销售图表配置
const memberCardOption = computed(() => {
  const data = memberCardData.value || {}
  const chartData = [
    { value: data.monthCard || 0, name: '月卡', itemStyle: { color: '#1890ff' } },
    { value: data.quarterCard || 0, name: '季卡', itemStyle: { color: '#52c41a' } },
    { value: data.yearCard || 0, name: '年卡', itemStyle: { color: '#faad14' } }
  ]

  return {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'horizontal',
      bottom: '5%',
      itemWidth: 12,
      itemHeight: 12,
      textStyle: { color: '#666', fontSize: 12 }
    },
    series: [{
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 8,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: false,
        position: 'center'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 16,
          fontWeight: 'bold'
        },
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.2)'
        }
      },
      labelLine: { show: false },
      data: chartData
    }]
  }
})

// 课程统计图表配置
const courseStatsOption = computed(() => {
  const data = courseStatsData.value || []

  return {
    tooltip: { trigger: 'axis', axisPointer: { type: 'cross' } },
    legend: {
      data: ['课程数', '预约数'],
      bottom: '5%',
      textStyle: { color: '#666' }
    },
    grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
    xAxis: {
      type: 'category',
      data: data.map(d => d.categoryName),
      axisLine: { lineStyle: { color: '#d9d9d9' } },
      axisLabel: { color: '#666', rotate: 30 }
    },
    yAxis: [
      {
        type: 'value',
        name: '课程数',
        position: 'left',
        axisLine: { show: false },
        axisTick: { show: false },
        splitLine: { lineStyle: { color: '#f0f0f0' } },
        axisLabel: { color: '#666' }
      },
      {
        type: 'value',
        name: '预约数',
        position: 'right',
        axisLine: { show: false },
        axisTick: { show: false },
        splitLine: { show: false },
        axisLabel: { color: '#666' }
      }
    ],
    series: [
      {
        name: '课程数',
        type: 'bar',
        data: data.map(d => d.courseCount),
        itemStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: '#1890ff' },
              { offset: 1, color: '#69c0ff' }
            ]
          },
          borderRadius: [4, 4, 0, 0]
        },
        barWidth: '40%'
      },
      {
        name: '预约数',
        type: 'line',
        yAxisIndex: 1,
        data: data.map(d => d.bookingCount),
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        itemStyle: { color: '#52c41a' },
        lineStyle: { width: 3 },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(82, 196, 26, 0.2)' },
              { offset: 1, color: 'rgba(82, 196, 26, 0.05)' }
            ]
          }
        }
      }
    ]
  }
})

// 用户增长趋势图表配置
const userGrowthOption = computed(() => {
  const data = userGrowthData.value || []

  return {
    tooltip: {
      trigger: 'axis',
      formatter: params => `${params[0].name}<br/>新增用户: ${params[0].value}人`
    },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: data.map(d => d.date),
      axisLine: { lineStyle: { color: '#d9d9d9' } },
      axisLabel: { color: '#666' }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: '#f0f0f0' } },
      axisLabel: { color: '#666' }
    },
    series: [{
      type: 'line',
      data: data.map(d => d.count),
      smooth: true,
      symbol: 'none',
      lineStyle: { width: 3, color: '#13c2c2' },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(19, 194, 194, 0.4)' },
            { offset: 1, color: 'rgba(19, 194, 194, 0.05)' }
          ]
        }
      }
    }]
  }
})

// 器材使用状态图表配置
const equipmentStatusOption = computed(() => {
  const data = equipmentStatusData.value || {}

  return {
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'center',
      textStyle: { color: '#666' }
    },
    series: [{
      type: 'pie',
      radius: ['40%', '65%'],
      center: ['60%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 14, fontWeight: 'bold' }
      },
      data: [
        { value: data.normal || 0, name: '正常使用', itemStyle: { color: '#52c41a' } },
        { value: data.maintenance || 0, name: '维护中', itemStyle: { color: '#faad14' } },
        { value: data.repair || 0, name: '待维修', itemStyle: { color: '#f5222d' } },
        { value: data.offline || 0, name: '已停用', itemStyle: { color: '#bfbfbf' } }
      ]
    }]
  }
})

// 报修处理统计图表配置
const repairStatsOption = computed(() => {
  const data = repairStatsData.value || []

  return {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: '#f0f0f0' } },
      axisLabel: { color: '#666' }
    },
    yAxis: {
      type: 'category',
      data: data.map(d => d.status),
      axisLine: { lineStyle: { color: '#d9d9d9' } },
      axisLabel: { color: '#666' }
    },
    series: [{
      type: 'bar',
      data: data.map((d, i) => ({
        value: d.count,
        itemStyle: {
          color: ['#f5222d', '#faad14', '#1890ff', '#52c41a'][i % 4],
          borderRadius: [0, 4, 4, 0]
        }
      })),
      barWidth: '50%',
      label: {
        show: true,
        position: 'right',
        color: '#666'
      }
    }]
  }
})



// 格式化数字
function formatNumber(num) {
  if (num === undefined || num === null) return '0'
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  return num.toLocaleString()
}

// 格式化日期时间
function formatDateTime(dateTime) {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN')
}

// 获取所有数据
async function fetchAllData() {
  await Promise.all([
    fetchStats(),
    fetchMemberCards(),
    fetchCourseStats(),
    fetchRevenueTrend(),
    fetchUserGrowth(),
    fetchEquipmentStatus(),
    fetchRepairStats()
  ])
}

// 获取统计数据
async function fetchStats() {
  try {
    const res = await getDashboardStats()
    if (res) {
      dashboardData.value = res
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 获取会员卡统计
async function fetchMemberCards() {
  try {
    const res = await getMemberCardStats()
    memberCardData.value = res || {}
  } catch (error) {
    console.error('获取会员卡统计失败:', error)
  }
}

// 获取课程统计
async function fetchCourseStats() {
  try {
    const res = await getCourseStats()
    courseStatsData.value = res || []
  } catch (error) {
    console.error('获取课程统计失败:', error)
  }
}

// 获取营收趋势
async function fetchRevenueTrend() {
  try {
    const res = await getRevenueTrend({ range: timeRange.value })
    revenueData.value = res || []
  } catch (error) {
    console.error('获取营收趋势失败:', error)
    // 使用模拟数据
    revenueData.value = generateMockRevenueData()
  }
}

// 获取用户增长
async function fetchUserGrowth() {
  try {
    const res = await getUserGrowth({ range: timeRange.value })
    userGrowthData.value = res || []
  } catch (error) {
    console.error('获取用户增长失败:', error)
    userGrowthData.value = generateMockUserGrowthData()
  }
}

// 获取器材状态
async function fetchEquipmentStatus() {
  try {
    const res = await getEquipmentStatus()
    equipmentStatusData.value = res || {}
  } catch (error) {
    console.error('获取器材状态失败:', error)
    equipmentStatusData.value = { normal: 35, maintenance: 5, repair: 3, offline: 2 }
  }
}

// 获取报修统计
async function fetchRepairStats() {
  try {
    const res = await getRepairStats()
    repairStatsData.value = res || []
  } catch (error) {
    console.error('获取报修统计失败:', error)
    repairStatsData.value = [
      { status: '待处理', count: 8 },
      { status: '处理中', count: 5 },
      { status: '已派工', count: 3 },
      { status: '已完成', count: 12 }
    ]
  }
}

// 生成模拟营收数据
function generateMockRevenueData() {
  const days = timeRange.value === 'today' ? 24 : 7
  const data = []
  for (let i = 0; i < days; i++) {
    data.push({
      date: timeRange.value === 'today' ? `${i}:00` : `周${['日', '一', '二', '三', '四', '五', '六'][i]}`,
      amount: Math.floor(Math.random() * 5000) + 2000
    })
  }
  return data
}

// 生成模拟用户增长数据
function generateMockUserGrowthData() {
  const days = 7
  const data = []
  for (let i = 0; i < days; i++) {
    data.push({
      date: `周${['日', '一', '二', '三', '四', '五', '六'][i]}`,
      count: Math.floor(Math.random() * 50) + 10
    })
  }
  return data
}

// 处理时间范围变化
function handleTimeRangeChange() {
  fetchRevenueTrend()
  fetchUserGrowth()
}

// 刷新所有数据
async function refreshAllData() {
  refreshLoading.value = true
  try {
    await fetchAllData()
    ElMessage.success('数据刷新成功')
  } catch (error) {
    ElMessage.error('数据刷新失败')
  } finally {
    refreshLoading.value = false
  }
}

// 生成AI分析报告
async function handleAnalysis() {
  analysisLoading.value = true

  try {
    const res = await generateAnalysis({ analysisType: 'OVERALL' })
    if (res) {
      analysisReport.value = res
      // 数据返回后才打开对话框
      analysisVisible.value = true
    }
  } catch (error) {
    console.error('生成分析报告失败:', error)
    ElMessage.error('生成分析报告失败，请稍后重试')
  } finally {
    analysisLoading.value = false
  }
}

// 保存报告到数据分析
async function handleSaveReport() {
  if (!analysisReport.value) {
    ElMessage.warning('没有可保存的报告')
    return
  }

  saveLoading.value = true
  try {
    console.log('开始保存报告...')
    console.log('报告数据:', {
      reportTitle: analysisReport.value.reportTitle,
      analysisType: analysisReport.value.analysisType,
      hasReportContent: !!analysisReport.value.reportContent,
      suggestionsLength: analysisReport.value.suggestions?.length || 0,
      generateTime: analysisReport.value.generateTime
    })

    const reportData = {
      reportTitle: analysisReport.value.reportTitle || 'AI分析报告',
      analysisType: analysisReport.value.analysisType || 'OVERALL',
      reportContent: analysisReport.value.reportContent || '',
      suggestions: analysisReport.value.suggestions || [],
      generateTime: analysisReport.value.generateTime || new Date().toISOString()
    }

    console.log('发送保存请求，数据:', reportData)

    const response = await saveAnalysisReport(reportData)
    console.log('保存成功，响应:', response)

    ElMessage.success('报告保存成功，可在数据分析菜单查看')

    // 保存成功后关闭弹窗并清空报告，防止重复提交
    analysisVisible.value = false
    analysisReport.value = null
  } catch (error) {
    console.error('保存报告失败 - 完整错误:', error)
    console.error('错误详情:', {
      message: error.message,
      response: error.response?.data,
      status: error.response?.status,
      config: error.config?.url
    })

    let errorMsg = '保存报告失败，请稍后重试'
    if (error.response) {
      switch (error.response.status) {
        case 401:
          errorMsg = '登录已过期，请重新登录'
          break
        case 403:
          errorMsg = '没有权限执行此操作'
          break
        case 400:
          errorMsg = error.response.data?.message || '请求数据格式错误'
          break
        case 500:
          errorMsg = '服务器内部错误，请稍后重试'
          break
        default:
          errorMsg = error.response.data?.message || `保存失败 (${error.response.status})`
      }
    } else if (error.message) {
      errorMsg = `网络错误: ${error.message}`
    }

    ElMessage.error(errorMsg)
  } finally {
    saveLoading.value = false
  }
}

// 自动刷新
function startAutoRefresh() {
  refreshTimer = setInterval(() => {
    fetchStats()
  }, 60000) // 每分钟刷新一次统计数据
}

function stopAutoRefresh() {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

// 初始化图表
function initCharts() {
  if (revenueChartRef.value) {
    revenueChart = echarts.init(revenueChartRef.value)
    revenueChart.setOption(revenueOption.value)
  }
  if (memberCardChartRef.value) {
    memberCardChart = echarts.init(memberCardChartRef.value)
    memberCardChart.setOption(memberCardOption.value)
  }
  if (courseStatsChartRef.value) {
    courseStatsChart = echarts.init(courseStatsChartRef.value)
    courseStatsChart.setOption(courseStatsOption.value)
  }
  if (userGrowthChartRef.value) {
    userGrowthChart = echarts.init(userGrowthChartRef.value)
    userGrowthChart.setOption(userGrowthOption.value)
  }
  if (equipmentStatusChartRef.value) {
    equipmentStatusChart = echarts.init(equipmentStatusChartRef.value)
    equipmentStatusChart.setOption(equipmentStatusOption.value)
  }
  if (repairStatsChartRef.value) {
    repairStatsChart = echarts.init(repairStatsChartRef.value)
    repairStatsChart.setOption(repairStatsOption.value)
  }
}

// 销毁图表
function disposeCharts() {
  revenueChart?.dispose()
  memberCardChart?.dispose()
  courseStatsChart?.dispose()
  userGrowthChart?.dispose()
  equipmentStatusChart?.dispose()
  repairStatsChart?.dispose()
}

// 更新图表
function updateCharts() {
  revenueChart?.setOption(revenueOption.value)
  memberCardChart?.setOption(memberCardOption.value)
  courseStatsChart?.setOption(courseStatsOption.value)
  userGrowthChart?.setOption(userGrowthOption.value)
  equipmentStatusChart?.setOption(equipmentStatusOption.value)
  repairStatsChart?.setOption(repairStatsOption.value)
}

// 监听图表配置变化
watch(revenueOption, () => revenueChart?.setOption(revenueOption.value), { deep: true })
watch(memberCardOption, () => memberCardChart?.setOption(memberCardOption.value), { deep: true })
watch(courseStatsOption, () => courseStatsChart?.setOption(courseStatsOption.value), { deep: true })
watch(userGrowthOption, () => userGrowthChart?.setOption(userGrowthOption.value), { deep: true })
watch(equipmentStatusOption, () => equipmentStatusChart?.setOption(equipmentStatusOption.value), { deep: true })
watch(repairStatsOption, () => repairStatsChart?.setOption(repairStatsOption.value), { deep: true })

// 窗口大小变化时重新调整图表大小
function handleResize() {
  revenueChart?.resize()
  memberCardChart?.resize()
  courseStatsChart?.resize()
  userGrowthChart?.resize()
  equipmentStatusChart?.resize()
  repairStatsChart?.resize()
}

onMounted(() => {
  fetchAllData()
  startAutoRefresh()
  nextTick(() => {
    initCharts()
  })
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  stopAutoRefresh()
  disposeCharts()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.dashboard-page {
  padding: 0;
}

/* 页面头部 */
.page-header {
  margin-bottom: 24px;
  align-items: center;
}

.header-title h2 {
  margin: 0 0 4px 0;
  font-size: 24px;
  font-weight: 600;
  color: #1f1f1f;
}

.subtitle {
  margin: 0;
  color: #8c8c8c;
  font-size: 14px;
}

.header-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
}

.refresh-btn {
  margin-left: 8px;
}

/* 统计卡片 */
.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  animation: slideUp 0.5s ease-out forwards;
  opacity: 0;
  transform: translateY(20px);
}

@keyframes slideUp {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-content {
  display: flex;
  align-items: flex-start;
}

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-value-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1f1f1f;
  line-height: 1.2;
}

.stat-trend {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  font-size: 12px;
  font-weight: 500;
  padding: 2px 6px;
  border-radius: 4px;
}

.trend-up {
  color: #52c41a;
  background: rgba(82, 196, 26, 0.1);
}

.trend-down {
  color: #f5222d;
  background: rgba(245, 34, 45, 0.1);
}

.stat-title {
  font-size: 14px;
  color: #8c8c8c;
  margin-bottom: 4px;
}

.stat-compare {
  font-size: 12px;
  color: #bfbfbf;
}

.stat-progress {
  margin-top: 12px;
}

/* 图表区域 */
.chart-row {
  margin-bottom: 24px;
}

.chart-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  height: 100%;
}

.chart-card :deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.chart-card :deep(.el-card__body) {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-title-text {
  font-size: 16px;
  font-weight: 600;
  color: #1f1f1f;
}

/* ==================== AI智能分析按钮栏 ==================== */
.ai-analysis-row {
  margin-bottom: 20px;
}

.ai-analysis-bar {
  display: flex;
  justify-content: flex-end;
  padding: 4px 0;
}

/* ==================== AI分析报告对话框 ==================== */
.analysis-dialog :deep(.el-dialog) {
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.15), 0 10px 30px rgba(0, 0, 0, 0.08);
}

.analysis-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 40%, #0f3460 100%);
  margin-right: 0;
  padding: 24px 32px;
  position: relative;
  overflow: hidden;
}

.analysis-dialog :deep(.el-dialog__header::after) {
  content: '';
  position: absolute;
  top: -50%;
  right: -20%;
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(102, 126, 234, 0.15) 0%, transparent 70%);
  border-radius: 50%;
}

.analysis-dialog :deep(.el-dialog__title) {
  color: #fff;
  font-weight: 700;
  font-size: 20px;
  letter-spacing: 1px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.analysis-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: rgba(255, 255, 255, 0.7);
  font-size: 18px;
  transition: all 0.3s;
}

.analysis-dialog :deep(.el-dialog__headerbtn .el-dialog__close:hover) {
  color: #fff;
  transform: rotate(90deg);
}

.analysis-dialog :deep(.el-dialog__body) {
  padding: 28px 36px;
  max-height: 75vh;
  overflow-y: auto;
  background: #fafbfc;
}

.analysis-dialog :deep(.el-dialog__body::-webkit-scrollbar) {
  width: 6px;
}

.analysis-dialog :deep(.el-dialog__body::-webkit-scrollbar-track) {
  background: transparent;
}

.analysis-dialog :deep(.el-dialog__body::-webkit-scrollbar-thumb) {
  background: linear-gradient(180deg, #667eea, #764ba2);
  border-radius: 3px;
}

/* 报告头部 */
.report-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 8px;
}

.header-icon-wrap {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.35);
  flex-shrink: 0;
}

.header-info {
  flex: 1;
}

.header-info h3 {
  margin: 0 0 6px 0;
  font-size: 20px;
  font-weight: 700;
  color: #1a1a2e;
  letter-spacing: 0.5px;
}

/* ==================== Markdown 报告内容样式 ==================== */
.report-body {
  line-height: 2;
  color: #374151;
  font-size: 14.5px;
  font-family: -apple-system, 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.report-body :deep(h1),
.report-body :deep(h2),
.report-body :deep(h3),
.report-body :deep(h4) {
  margin-top: 28px;
  margin-bottom: 14px;
  font-weight: 700;
  line-height: 1.4;
  position: relative;
  padding-left: 16px;
}

.report-body :deep(h1) {
  font-size: 22px;
  color: #1a1a2e;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  padding: 12px 20px;
  border-radius: 10px;
  background-image: linear-gradient(135deg, #667eea 0%, #764ba2 100%), linear-gradient(135deg, #f0f4ff 0%, #eef2ff 100%);
  background-clip: padding-box, border-box;
  -webkit-background-clip: text, padding-box;
  border-left: 4px solid #667eea;
  padding-left: 16px;
  -webkit-text-fill-color: #1a1a2e;
}

.report-body :deep(h2) {
  font-size: 18px;
  color: #1e293b;
  display: flex;
  align-items: center;
  gap: 10px;
}

.report-body :deep(h2)::before {
  content: '';
  width: 5px;
  height: 22px;
  background: linear-gradient(180deg, #667eea, #764ba2);
  border-radius: 3px;
  flex-shrink: 0;
}

.report-body :deep(h3) {
  font-size: 16px;
  color: #334155;
  border-left: 3px solid #94a3b8;
  padding-left: 14px;
}

.report-body :deep(h4) {
  font-size: 15px;
  color: #475569;
  border-left: 3px solid #cbd5e1;
  padding-left: 14px;
}

.report-body :deep(p) {
  margin: 10px 0;
  text-align: justify;
}

.report-body :deep(strong),
.report-body :deep(b) {
  color: #667eea;
  font-weight: 700;
  background: linear-gradient(120deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.08) 100%);
  padding: 1px 6px;
  border-radius: 4px;
}

.report-body :deep(ul),
.report-body :deep(ol) {
  margin: 14px 0;
  padding-left: 24px;
}

.report-body :deep(li) {
  margin: 8px 0;
  padding: 6px 12px 6px 16px;
  border-radius: 8px;
  transition: all 0.2s ease;
  position: relative;
  list-style: none;
}

.report-body :deep(ul li)::before {
  content: '';
  position: absolute;
  left: 0;
  top: 14px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  box-shadow: 0 2px 6px rgba(102, 126, 234, 0.3);
}

.report-body :deep(ol) {
  margin: 14px 0;
  padding-left: 24px;
}

.report-body :deep(ol li) {
  margin: 6px 0;
  padding: 2px 8px;
  border-radius: 4px;
  transition: background 0.2s ease;
}

.report-body :deep(ol li:hover) {
  background: rgba(102, 126, 234, 0.04);
}

.report-body :deep(code) {
  font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', monospace;
  font-size: 13px;
  color: #e06c75;
  background: linear-gradient(135deg, #282c34, #21252b);
  padding: 2px 8px;
  border-radius: 5px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  border: 1px solid #3a3f4b;
}

.report-body :deep(pre) {
  margin: 16px 0;
  padding: 18px 22px;
  background: linear-gradient(145deg, #1e2230, #181b24);
  border-radius: 12px;
  overflow-x: auto;
  border: 1px solid #2d3342;
  box-shadow: inset 0 2px 8px rgba(0, 0, 0, 0.2), 0 4px 16px rgba(0, 0, 0, 0.08);
}

.report-body :deep(pre code) {
  background: none;
  padding: 0;
  border: none;
  box-shadow: none;
  color: #abb2bf;
  font-size: 13.5px;
  line-height: 1.7;
}

.report-body :deep(blockquote) {
  margin: 16px 0;
  padding: 16px 20px;
  background: linear-gradient(135deg, #f0f4ff 0%, #faf5ff 100%);
  border-left: 4px solid #667eea;
  border-radius: 0 10px 10px 0;
  color: #555;
  font-style: italic;
  box-shadow: 0 2px 12px rgba(102, 126, 234, 0.08);
}

.report-body :deep(blockquote p) {
  margin: 4px 0;
}

.report-body :deep(hr) {
  border: none;
  height: 1px;
  background: linear-gradient(90deg, transparent, #cbd5e1, transparent);
  margin: 24px 0;
}

.report-body :deep(table) {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  margin: 16px 0;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.report-body :deep(th) {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  padding: 12px 16px;
  font-weight: 600;
  font-size: 13px;
  text-align: left;
}

.report-body :deep(td) {
  padding: 11px 16px;
  border-bottom: 1px solid #eef2ff;
  font-size: 13.5px;
}

.report-body :deep(tr:last-child td) {
  border-bottom: none;
}

.report-body :deep(tr:nth-child(even) td) {
  background: #f8faff;
}

.report-body :deep(tr:hover td) {
  background: #eef2ff;
}

/* ==================== 优化建议区域 ==================== */
.suggestions {
  margin-top: 28px;
  padding: 20px 24px;
  background: linear-gradient(135deg, #f0fdf4 0%, #ecfdf5 100%);
  border-radius: 12px;
  border: 1px solid #bbf7d0;
}

.suggestions-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
  color: #15803d;
  font-size: 15px;
  font-weight: 600;
}

.suggestions-header .el-icon {
  color: #22c55e;
}

.suggestions-content {
  line-height: 1.8;
  color: #374151;
  font-size: 14px;
}

.suggestions-content :deep(h1),
.suggestions-content :deep(h2),
.suggestions-content :deep(h3),
.suggestions-content :deep(h4) {
  margin-top: 16px;
  margin-bottom: 10px;
  font-weight: 600;
  color: #1e293b;
}

.suggestions-content :deep(h1) {
  font-size: 18px;
  border-left: 4px solid #22c55e;
  padding-left: 12px;
}

.suggestions-content :deep(h2) {
  font-size: 16px;
  border-left: 3px solid #4ade80;
  padding-left: 10px;
}

.suggestions-content :deep(h3) {
  font-size: 15px;
  color: #334155;
}

.suggestions-content :deep(p) {
  margin: 8px 0;
  line-height: 1.8;
}

.suggestions-content :deep(strong),
.suggestions-content :deep(b) {
  color: #15803d;
  font-weight: 600;
}

.suggestions-content :deep(ul),
.suggestions-content :deep(ol) {
  margin: 10px 0;
  padding-left: 20px;
}

.suggestions-content :deep(li) {
  margin: 6px 0;
  line-height: 1.7;
}

.suggestions-content :deep(ul li) {
  list-style-type: disc;
}

.suggestions-content :deep(ol li) {
  list-style-type: decimal;
}

/* ==================== 报告底部 ==================== */
.report-footer {
  margin-top: 28px;
  padding-top: 16px;
  border-top: 1px dashed #e2e8f0;
  text-align: right;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 6px;
}

/* ==================== 响应式适配 ==================== */
@media (max-width: 1200px) {
  .header-actions {
    flex-wrap: wrap;
    justify-content: flex-start;
    margin-top: 16px;
  }

  .analysis-dialog :deep(.el-dialog__body) {
    padding: 24px 28px;
  }

  .ai-analysis-btn {
    min-width: 300px;
    padding: 16px 32px !important;
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
    margin-top: 16px;
  }

  .stat-value {
    font-size: 24px;
  }

  .ai-analysis-btn {
    min-width: 260px;
    padding: 14px 24px !important;
  }

  .btn-text {
    font-size: 17px;
  }

  .report-body :deep(h1) {
    font-size: 19px;
  }

  .report-body :deep(h2) {
    font-size: 16px;
  }
}
</style>
