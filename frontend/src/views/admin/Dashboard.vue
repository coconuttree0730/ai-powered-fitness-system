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
          <el-radio-button label="today">今日</el-radio-button>
          <el-radio-button label="week">本周</el-radio-button>
          <el-radio-button label="month">本月</el-radio-button>
          <el-radio-button label="year">全年</el-radio-button>
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
            <div class="stat-icon-wrapper" :style="{ background: stat.gradient }">
              <el-icon :size="24" class="stat-icon">
                <component :is="stat.icon" />
              </el-icon>
            </div>
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
                  <el-radio-button label="line">折线图</el-radio-button>
                  <el-radio-button label="bar">柱状图</el-radio-button>
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
      <el-col :xs="24" :lg="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <el-icon size="18" color="#fa8c16"><Clock /></el-icon>
                <span class="header-title-text">到店高峰时段分布</span>
              </div>
            </div>
          </template>
          <div ref="peakHoursChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <el-icon size="18" color="#722ed1"><Reading /></el-icon>
                <span class="header-title-text">课程分类统计</span>
              </div>
              <el-button type="primary" link size="small" @click="handleAnalysis" :loading="analysisLoading">
                <el-icon><MagicStick /></el-icon>
                AI智能分析
              </el-button>
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

    <!-- 底部数据表格 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="24">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <el-icon size="18" color="#1890ff"><List /></el-icon>
                <span class="header-title-text">热门课程排行榜</span>
              </div>
              <el-button type="primary" link size="small" @click="goToCourses">
                查看全部 <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </template>
          <el-table :data="hotCourses" style="width: 100%" v-loading="tableLoading">
            <el-table-column type="index" label="排名" width="80" align="center">
              <template #default="{ $index }">
                <div class="rank-cell" :class="`rank-${$index + 1}`">
                  {{ $index + 1 }}
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="courseName" label="课程名称" min-width="180">
              <template #default="{ row }">
                <div class="course-name-cell">
                  <el-avatar :size="32" :src="row.coverImage" shape="square" />
                  <span>{{ row.courseName }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="categoryName" label="分类" width="120">
              <template #default="{ row }">
                <el-tag size="small" effect="plain">{{ row.categoryName }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="coachName" label="教练" width="120" />
            <el-table-column prop="bookingCount" label="预约人数" width="120" align="center">
              <template #default="{ row }">
                <el-progress 
                  :percentage="Math.min(row.bookingCount / maxBookingCount * 100, 100)" 
                  :show-text="false"
                  :stroke-width="8"
                  :color="getProgressColor(row.bookingCount)"
                  style="width: 80px"
                />
                <span class="booking-count">{{ row.bookingCount }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="satisfaction" label="满意度" width="120" align="center">
              <template #default="{ row }">
                <el-rate v-model="row.satisfaction" disabled show-score text-color="#ff9900" />
              </template>
            </el-table-column>
            <el-table-column prop="revenue" label="营收" width="120" align="right">
              <template #default="{ row }">
                <span class="revenue-text">¥{{ formatNumber(row.revenue) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- AI分析报告对话框 -->
    <el-dialog 
      v-model="analysisVisible" 
      title="AI智能分析报告" 
      width="700px"
      class="analysis-dialog"
      destroy-on-close
    >
      <div class="analysis-report" v-if="analysisReport">
        <div class="report-header">
          <el-icon size="32" color="#1890ff"><MagicStick /></el-icon>
          <h3>{{ analysisReport.reportTitle }}</h3>
          <el-tag type="info" size="small">{{ analysisReport.analysisType }}</el-tag>
        </div>
        <el-divider />
        <div class="report-content">{{ analysisReport.reportContent }}</div>
        <div class="suggestions" v-if="analysisReport.suggestions?.length">
          <h4>
            <el-icon><Opportunity /></el-icon>
            优化建议
          </h4>
          <el-timeline>
            <el-timeline-item 
              v-for="(suggestion, index) in analysisReport.suggestions" 
              :key="index"
              :type="index === 0 ? 'primary' : 'info'"
              :icon="index === 0 ? Star : null"
            >
              {{ suggestion }}
            </el-timeline-item>
          </el-timeline>
        </div>
        <div class="report-footer">
          <el-text type="info" size="small">
            生成时间：{{ formatDateTime(analysisReport.generateTime) }}
          </el-text>
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
import {
  User, Calendar, Tickets, Box, Goods, Money, ArrowUp, ArrowDown,
  Refresh, TrendCharts, PieChart, Clock, Reading, MagicStick,
  UserFilled, Warning, List, ArrowRight, Star, Opportunity
} from '@element-plus/icons-vue'
import {
  getDashboardStats, getPeakHours, getMemberCardStats, getCourseStats,
  generateAnalysis, getRevenueTrend, getUserGrowth, getEquipmentStatus,
  getRepairStats, getHotCourses
} from '@/api/dashboard'

const router = useRouter()

// ECharts 实例
const revenueChartRef = ref(null)
const memberCardChartRef = ref(null)
const peakHoursChartRef = ref(null)
const courseStatsChartRef = ref(null)
const userGrowthChartRef = ref(null)
const equipmentStatusChartRef = ref(null)
const repairStatsChartRef = ref(null)

let revenueChart = null
let memberCardChart = null
let peakHoursChart = null
let courseStatsChart = null
let userGrowthChart = null
let equipmentStatusChart = null
let repairStatsChart = null

// 状态变量
const timeRange = ref('week')
const revenueChartType = ref('line')
const refreshLoading = ref(false)
const analysisLoading = ref(false)
const analysisVisible = ref(false)
const analysisReport = ref(null)
const tableLoading = ref(false)

// 数据存储
const dashboardData = ref({})
const peakHoursData = ref([])
const memberCardData = ref({})
const courseStatsData = ref([])
const revenueData = ref([])
const userGrowthData = ref([])
const equipmentStatusData = ref({})
const repairStatsData = ref([])
const hotCourses = ref([])

// 定时器
let refreshTimer = null

// 核心指标数据
const stats = computed(() => [
  {
    key: 'members',
    title: '总会员数',
    value: dashboardData.value.totalMembers || 0,
    icon: 'User',
    type: 'primary',
    gradient: 'linear-gradient(135deg, #1890ff 0%, #36cfc9 100%)',
    trend: 12.5,
    compareText: '较上月',
    progress: 78,
    progressColor: '#1890ff'
  },
  {
    key: 'courses',
    title: '总课程数',
    value: dashboardData.value.totalCourses || 0,
    icon: 'Calendar',
    type: 'success',
    gradient: 'linear-gradient(135deg, #52c41a 0%, #95de64 100%)',
    trend: 8.2,
    compareText: '较上月',
    progress: 65,
    progressColor: '#52c41a'
  },
  {
    key: 'bookings',
    title: '总预约数',
    value: dashboardData.value.totalBookings || 0,
    icon: 'Tickets',
    type: 'warning',
    gradient: 'linear-gradient(135deg, #fa8c16 0%, #ffc53d 100%)',
    trend: -3.1,
    compareText: '较上周',
    progress: 82,
    progressColor: '#fa8c16'
  },
  {
    key: 'equipment',
    title: '总器材数',
    value: dashboardData.value.totalEquipment || 0,
    icon: 'Box',
    type: 'purple',
    gradient: 'linear-gradient(135deg, #722ed1 0%, #b37feb 100%)',
    trend: 5.4,
    compareText: '较上月',
    progress: 90,
    progressColor: '#722ed1'
  },
  {
    key: 'orders',
    title: '今日订单',
    value: dashboardData.value.todayOrders || 0,
    icon: 'Goods',
    type: 'cyan',
    gradient: 'linear-gradient(135deg, #13c2c2 0%, #5cdbd3 100%)',
    trend: 15.8,
    compareText: '较昨日',
    progress: 72,
    progressColor: '#13c2c2'
  },
  {
    key: 'revenue',
    title: '今日营收',
    value: dashboardData.value.todayRevenue || 0,
    icon: 'Money',
    type: 'pink',
    gradient: 'linear-gradient(135deg, #eb2f96 0%, #ffadd2 100%)',
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

// 到店高峰时段图表配置
const peakHoursOption = computed(() => {
  const data = peakHoursData.value || []
  
  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: params => `${params[0].name}:00<br/>到店人数: ${params[0].value}人`
    },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: data.map(d => d.hour),
      axisLine: { lineStyle: { color: '#d9d9d9' } },
      axisLabel: { color: '#666', formatter: '{value}:00' }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: '#f0f0f0' } },
      axisLabel: { color: '#666' }
    },
    series: [{
      type: 'bar',
      data: data.map(d => ({
        value: d.count,
        itemStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: '#faad14' },
              { offset: 1, color: '#ffc53d' }
            ]
          },
          borderRadius: [4, 4, 0, 0]
        }
      })),
      barWidth: '60%',
      emphasis: {
        itemStyle: { color: '#d48806' }
      }
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

// 最大预约数（用于进度条计算）
const maxBookingCount = computed(() => {
  if (!hotCourses.value.length) return 1
  return Math.max(...hotCourses.value.map(c => c.bookingCount))
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

// 获取进度条颜色
function getProgressColor(count) {
  const ratio = count / maxBookingCount.value
  if (ratio >= 0.8) return '#f5222d'
  if (ratio >= 0.6) return '#faad14'
  if (ratio >= 0.4) return '#1890ff'
  return '#52c41a'
}

// 获取所有数据
async function fetchAllData() {
  await Promise.all([
    fetchStats(),
    fetchPeakHours(),
    fetchMemberCards(),
    fetchCourseStats(),
    fetchRevenueTrend(),
    fetchUserGrowth(),
    fetchEquipmentStatus(),
    fetchRepairStats(),
    fetchHotCourses()
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

// 获取高峰时间
async function fetchPeakHours() {
  try {
    const res = await getPeakHours()
    peakHoursData.value = res || []
  } catch (error) {
    console.error('获取高峰时间失败:', error)
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

// 获取热门课程
async function fetchHotCourses() {
  tableLoading.value = true
  try {
    const res = await getHotCourses()
    hotCourses.value = res || []
  } catch (error) {
    console.error('获取热门课程失败:', error)
    hotCourses.value = generateMockHotCourses()
  } finally {
    tableLoading.value = false
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

// 生成模拟热门课程数据
function generateMockHotCourses() {
  return [
    { courseName: '瑜伽基础班', categoryName: '瑜伽', coachName: '李教练', bookingCount: 156, satisfaction: 4.8, revenue: 15600, coverImage: '' },
    { courseName: '动感单车', categoryName: '有氧', coachName: '王教练', bookingCount: 142, satisfaction: 4.7, revenue: 14200, coverImage: '' },
    { courseName: '力量训练', categoryName: '力量', coachName: '张教练', bookingCount: 128, satisfaction: 4.9, revenue: 12800, coverImage: '' },
    { courseName: '普拉提', categoryName: '塑形', coachName: '刘教练', bookingCount: 98, satisfaction: 4.6, revenue: 9800, coverImage: '' },
    { courseName: '搏击操', categoryName: '有氧', coachName: '陈教练', bookingCount: 87, satisfaction: 4.5, revenue: 8700, coverImage: '' }
  ]
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
      analysisVisible.value = true
    }
  } catch (error) {
    console.error('生成分析报告失败:', error)
    // 使用模拟数据
    analysisReport.value = {
      analysisType: 'OVERALL',
      reportTitle: '健身房运营综合分析报告',
      reportContent: '根据近期数据分析，健身房整体运营状况良好。会员活跃度较上月提升12.5%，课程预约量保持稳定增长。建议重点关注晚间高峰时段的器材调配，以及热门课程的容量扩充。',
      suggestions: [
        '建议增加晚间18:00-20:00时段的动感单车课程班次',
        '瑜伽课程需求旺盛，可考虑新增周末精品小班课',
        '部分器材使用率较低，建议开展针对性促销活动',
        '会员续费率有提升空间，建议推出老带新优惠活动'
      ],
      generateTime: new Date().toISOString()
    }
    analysisVisible.value = true
  } finally {
    analysisLoading.value = false
  }
}

// 跳转到课程管理
function goToCourses() {
  router.push('/admin/courses')
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
  if (peakHoursChartRef.value) {
    peakHoursChart = echarts.init(peakHoursChartRef.value)
    peakHoursChart.setOption(peakHoursOption.value)
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
  peakHoursChart?.dispose()
  courseStatsChart?.dispose()
  userGrowthChart?.dispose()
  equipmentStatusChart?.dispose()
  repairStatsChart?.dispose()
}

// 更新图表
function updateCharts() {
  revenueChart?.setOption(revenueOption.value)
  memberCardChart?.setOption(memberCardOption.value)
  peakHoursChart?.setOption(peakHoursOption.value)
  courseStatsChart?.setOption(courseStatsOption.value)
  userGrowthChart?.setOption(userGrowthOption.value)
  equipmentStatusChart?.setOption(equipmentStatusOption.value)
  repairStatsChart?.setOption(repairStatsOption.value)
}

// 监听图表配置变化
watch(revenueOption, () => revenueChart?.setOption(revenueOption.value), { deep: true })
watch(memberCardOption, () => memberCardChart?.setOption(memberCardOption.value), { deep: true })
watch(peakHoursOption, () => peakHoursChart?.setOption(peakHoursOption.value), { deep: true })
watch(courseStatsOption, () => courseStatsChart?.setOption(courseStatsOption.value), { deep: true })
watch(userGrowthOption, () => userGrowthChart?.setOption(userGrowthOption.value), { deep: true })
watch(equipmentStatusOption, () => equipmentStatusChart?.setOption(equipmentStatusOption.value), { deep: true })
watch(repairStatsOption, () => repairStatsChart?.setOption(repairStatsOption.value), { deep: true })

// 窗口大小变化时重新调整图表大小
function handleResize() {
  revenueChart?.resize()
  memberCardChart?.resize()
  peakHoursChart?.resize()
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
  gap: 16px;
}

.stat-icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.stat-icon {
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
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

/* 表格样式 */
.rank-cell {
  width: 28px;
  height: 28px;
  line-height: 28px;
  text-align: center;
  border-radius: 50%;
  font-weight: 600;
  font-size: 14px;
  margin: 0 auto;
  background: #f5f5f5;
  color: #666;
}

.rank-1 {
  background: linear-gradient(135deg, #ffd666 0%, #ffc53d 100%);
  color: #fff;
}

.rank-2 {
  background: linear-gradient(135deg, #d9d9d9 0%, #bfbfbf 100%);
  color: #fff;
}

.rank-3 {
  background: linear-gradient(135deg, #ff9c6e 0%, #ff7a45 100%);
  color: #fff;
}

.course-name-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.booking-count {
  margin-left: 8px;
  font-size: 13px;
  color: #666;
}

.revenue-text {
  font-weight: 600;
  color: #f5222d;
}

/* AI分析报告对话框 */
.analysis-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%);
  margin-right: 0;
  padding: 20px 24px;
}

.analysis-dialog :deep(.el-dialog__title) {
  color: #fff;
  font-weight: 600;
}

.analysis-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: #fff;
}

.report-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.report-header h3 {
  margin: 0;
  font-size: 18px;
  color: #1f1f1f;
}

.report-content {
  line-height: 1.8;
  color: #434343;
  font-size: 14px;
  white-space: pre-wrap;
}

.suggestions {
  margin-top: 24px;
  padding: 20px;
  background: #f6ffed;
  border-radius: 8px;
  border: 1px solid #b7eb8f;
}

.suggestions h4 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 16px 0;
  color: #52c41a;
  font-size: 16px;
}

.report-footer {
  margin-top: 24px;
  text-align: right;
}

/* 响应式适配 */
@media (max-width: 1200px) {
  .header-actions {
    flex-wrap: wrap;
    justify-content: flex-start;
    margin-top: 16px;
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
}
</style>
