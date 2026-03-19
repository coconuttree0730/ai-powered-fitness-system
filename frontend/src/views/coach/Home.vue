<template>
  <div class="coach-home">
    <!-- 数据面板 - 统计卡片 -->
    <n-grid :cols="4" :x-gap="16" :y-gap="16" class="stats-grid">
      <n-grid-item>
        <div class="stat-card primary">
          <div class="stat-icon">
            <n-icon size="28" :component="PeopleOutline" />
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalStudents }}</div>
            <div class="stat-label">学员总人数</div>
          </div>
          <div class="stat-trend up">
            <n-icon size="14" :component="TrendingUpOutline" />
            +12%
          </div>
        </div>
      </n-grid-item>
      <n-grid-item>
        <div class="stat-card success">
          <div class="stat-icon">
            <n-icon size="28" :component="CalendarOutline" />
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.weekCourses }}</div>
            <div class="stat-label">本周课程</div>
          </div>
          <div class="stat-trend up">
            <n-icon size="14" :component="TrendingUpOutline" />
            +5%
          </div>
        </div>
      </n-grid-item>
      <n-grid-item>
        <div class="stat-card warning">
          <div class="stat-icon">
            <n-icon size="28" :component="FitnessOutline" />
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.completionRate }}%</div>
            <div class="stat-label">课程完成率</div>
          </div>
          <div class="stat-trend down">
            <n-icon size="14" :component="TrendingDownOutline" />
            -2%
          </div>
        </div>
      </n-grid-item>
      <n-grid-item>
        <div class="stat-card info">
          <div class="stat-icon">
            <n-icon size="28" :component="RefreshOutline" />
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.renewalRate }}%</div>
            <div class="stat-label">学员续费率</div>
          </div>
          <div class="stat-trend up">
            <n-icon size="14" :component="TrendingUpOutline" />
            +8%
          </div>
        </div>
      </n-grid-item>
    </n-grid>

    <!-- 图表区域 -->
    <n-grid :cols="3" :x-gap="20" :y-gap="20" style="margin-top: 24px;">
      <!-- 课程销量趋势图 -->
      <n-grid-item :span="2">
        <div class="chart-card">
          <div class="chart-header">
            <h3 class="chart-title">课程销量趋势</h3>
            <n-radio-group v-model:value="chartPeriod" size="small">
              <n-radio-button value="day">日</n-radio-button>
              <n-radio-button value="week">周</n-radio-button>
              <n-radio-button value="month">月</n-radio-button>
            </n-radio-group>
          </div>
          <div ref="salesChartRef" class="chart-container"></div>
        </div>
      </n-grid-item>
      
      <!-- 课程类型分布 -->
      <n-grid-item>
        <div class="chart-card">
          <div class="chart-header">
            <h3 class="chart-title">课程类型分布</h3>
          </div>
          <div ref="typeChartRef" class="chart-container"></div>
        </div>
      </n-grid-item>
    </n-grid>

    <!-- 今日课程与快捷操作 -->
    <n-grid :cols="2" :x-gap="20" :y-gap="20" style="margin-top: 24px;">
      <n-grid-item>
        <div class="section-card">
          <div class="section-header">
            <h3 class="section-title">
              <n-icon :component="TimeOutline" size="20" />
              今日课程
            </h3>
            <n-button text type="primary" @click="$router.push('/coach/schedule')">
              查看日程
            </n-button>
          </div>
          <div v-if="todayCourses.length === 0" class="empty-state">
            <n-empty description="今日暂无课程" />
          </div>
          <div v-else class="course-timeline">
            <div v-for="course in todayCourses" :key="course.id" class="timeline-item">
              <div class="timeline-time">{{ formatTime(course.startTime) }}</div>
              <div class="timeline-content">
                <div class="course-tag" :class="course.type">{{ course.type === 'private' ? '私教' : '公开课' }}</div>
                <div class="course-name">{{ course.courseName }}</div>
                <div class="course-meta">
                  <span class="booking-count">
                    <n-icon :component="PersonOutline" size="14" />
                    {{ course.bookingCount }}/{{ course.capacity }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </n-grid-item>
      
      <n-grid-item>
        <div class="section-card">
          <div class="section-header">
            <h3 class="section-title">
              <n-icon :component="FlashOutline" size="20" />
              快捷操作
            </h3>
          </div>
          <div class="quick-actions">
            <div class="action-item" @click="$router.push('/coach/courses')">
              <div class="action-icon primary">
                <n-icon :component="AddCircleOutline" size="24" />
              </div>
              <span class="action-label">发布课程</span>
            </div>
            <div class="action-item" @click="$router.push('/coach/students')">
              <div class="action-icon success">
                <n-icon :component="ChatbubbleOutline" size="24" />
              </div>
              <span class="action-label">学员私信</span>
            </div>
            <div class="action-item" @click="$router.push('/coach/schedule')">
              <div class="action-icon warning">
                <n-icon :component="CalendarOutline" size="24" />
              </div>
              <span class="action-label">查看日程</span>
            </div>
            <div class="action-item" @click="showStatsModal = true">
              <div class="action-icon info">
                <n-icon :component="BarChartOutline" size="24" />
              </div>
              <span class="action-label">数据详情</span>
            </div>
          </div>
        </div>
      </n-grid-item>
    </n-grid>

    <!-- 数据详情弹窗 -->
    <n-modal v-model:show="showStatsModal" preset="card" title="详细数据" style="width: 700px">
      <n-descriptions bordered :column="2">
        <n-descriptions-item label="本月收入">¥{{ stats.monthIncome }}</n-descriptions-item>
        <n-descriptions-item label="本月课时">{{ stats.monthHours }}小时</n-descriptions-item>
        <n-descriptions-item label="平均评分">{{ stats.avgRating }}分</n-descriptions-item>
        <n-descriptions-item label="好评率">{{ stats.positiveRate }}%</n-descriptions-item>
      </n-descriptions>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import {
  PeopleOutline,
  CalendarOutline,
  FitnessOutline,
  RefreshOutline,
  TrendingUpOutline,
  TrendingDownOutline,
  TimeOutline,
  FlashOutline,
  AddCircleOutline,
  ChatbubbleOutline,
  BarChartOutline,
  PersonOutline
} from '@vicons/ionicons5'

// 统计数据
const stats = reactive({
  totalStudents: 156,
  weekCourses: 24,
  monthCourses: 96,
  completionRate: 92,
  renewalRate: 78,
  monthIncome: 25800,
  monthHours: 120,
  avgRating: 4.9,
  positiveRate: 98
})

// 今日课程
const todayCourses = ref([
  { id: 1, courseName: '增肌训练基础', startTime: '09:00', endTime: '10:00', type: 'private', bookingCount: 1, capacity: 1 },
  { id: 2, courseName: 'HIIT燃脂团课', startTime: '14:00', endTime: '15:00', type: 'public', bookingCount: 12, capacity: 20 },
  { id: 3, courseName: '瑜伽拉伸', startTime: '16:00', endTime: '17:00', type: 'private', bookingCount: 1, capacity: 1 },
  { id: 4, courseName: '核心力量训练', startTime: '19:00', endTime: '20:00', type: 'public', bookingCount: 8, capacity: 15 }
])

// 图表相关
const chartPeriod = ref('week')
const salesChartRef = ref(null)
const typeChartRef = ref(null)
let salesChart = null
let typeChart = null
const showStatsModal = ref(false)

// 初始化销量趋势图
function initSalesChart() {
  if (!salesChartRef.value) return
  
  salesChart = echarts.init(salesChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255,255,255,0.95)',
      borderColor: '#E5E7EB',
      borderWidth: 1,
      textStyle: { color: '#1A1A2E' },
      formatter: '{b}<br/>课程销量: {c}节'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
      axisLine: { lineStyle: { color: '#E5E7EB' } },
      axisLabel: { color: '#6B7280' }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#F3F4F6' } },
      axisLabel: { color: '#6B7280' }
    },
    series: [{
      data: [8, 12, 10, 15, 18, 22, 20],
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: {
        color: '#FF6B35',
        width: 3
      },
      itemStyle: {
        color: '#FF6B35',
        borderWidth: 2,
        borderColor: '#fff'
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(255,107,53,0.3)' },
            { offset: 1, color: 'rgba(255,107,53,0.05)' }
          ]
        }
      }
    }]
  }
  salesChart.setOption(option)
}

// 初始化课程类型分布图
function initTypeChart() {
  if (!typeChartRef.value) return
  
  typeChart = echarts.init(typeChartRef.value)
  const option = {
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255,255,255,0.95)',
      borderColor: '#E5E7EB',
      borderWidth: 1,
      textStyle: { color: '#1A1A2E' }
    },
    legend: {
      bottom: '5%',
      left: 'center',
      itemWidth: 12,
      itemHeight: 12,
      textStyle: { color: '#6B7280', fontSize: 12 }
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
      label: { show: false },
      emphasis: {
        label: {
          show: true,
          fontSize: 14,
          fontWeight: 'bold',
          color: '#1A1A2E'
        }
      },
      data: [
        { value: 45, name: '私教课', itemStyle: { color: '#FF6B35' } },
        { value: 30, name: '团课', itemStyle: { color: '#06D6A0' } },
        { value: 15, name: '瑜伽', itemStyle: { color: '#667eea' } },
        { value: 10, name: '其他', itemStyle: { color: '#FFD166' } }
      ]
    }]
  }
  typeChart.setOption(option)
}

// 监听图表周期变化
watch(chartPeriod, (newVal) => {
  if (salesChart) {
    const dataMap = {
      day: { x: ['08:00', '10:00', '12:00', '14:00', '16:00', '18:00', '20:00'], y: [2, 5, 3, 8, 6, 10, 7] },
      week: { x: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'], y: [8, 12, 10, 15, 18, 22, 20] },
      month: { x: ['第1周', '第2周', '第3周', '第4周'], y: [65, 78, 82, 91] }
    }
    const data = dataMap[newVal]
    salesChart.setOption({
      xAxis: { data: data.x },
      series: [{ data: data.y }]
    })
  }
})

function formatTime(time) {
  return time
}

onMounted(() => {
  nextTick(() => {
    initSalesChart()
    initTypeChart()
  })
  
  window.addEventListener('resize', () => {
    salesChart?.resize()
    typeChart?.resize()
  })
})

onUnmounted(() => {
  salesChart?.dispose()
  typeChart?.dispose()
  window.removeEventListener('resize', () => {})
})
</script>

<style scoped>
.coach-home {
  max-width: 1400px;
  margin: 0 auto;
}

/* 统计卡片 */
.stats-grid {
  margin-bottom: 8px;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.06);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0,0,0,0.12);
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
}

.stat-card.primary::before { background: linear-gradient(90deg, #FF6B35, #FF8C61); }
.stat-card.success::before { background: linear-gradient(90deg, #06D6A0, #2EC4B6); }
.stat-card.warning::before { background: linear-gradient(90deg, #FFD166, #FFB347); }
.stat-card.info::before { background: linear-gradient(90deg, #667eea, #764ba2); }

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-card.primary .stat-icon { background: linear-gradient(135deg, #FF6B35, #FF8C61); }
.stat-card.success .stat-icon { background: linear-gradient(135deg, #06D6A0, #2EC4B6); }
.stat-card.warning .stat-icon { background: linear-gradient(135deg, #FFD166, #FFB347); }
.stat-card.info .stat-icon { background: linear-gradient(135deg, #667eea, #764ba2); }

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1A1A2E;
  line-height: 1;
}

.stat-label {
  font-size: 13px;
  color: #6B7280;
  margin-top: 6px;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 600;
  padding: 4px 8px;
  border-radius: 20px;
}

.stat-trend.up {
  color: #06D6A0;
  background: rgba(6,214,160,0.1);
}

.stat-trend.down {
  color: #EF476F;
  background: rgba(239,71,111,0.1);
}

/* 图表卡片 */
.chart-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.06);
  transition: all 0.3s ease;
}

.chart-card:hover {
  box-shadow: 0 8px 30px rgba(0,0,0,0.1);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #1A1A2E;
  margin: 0;
}

.chart-container {
  height: 280px;
}

/* 区域卡片 */
.section-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.06);
  transition: all 0.3s ease;
  height: 100%;
}

.section-card:hover {
  box-shadow: 0 8px 30px rgba(0,0,0,0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1A1A2E;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 课程时间线 */
.course-timeline {
  position: relative;
}

.course-timeline::before {
  content: '';
  position: absolute;
  left: 40px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: linear-gradient(180deg, #FF6B35, #06D6A0);
  opacity: 0.3;
}

.timeline-item {
  display: flex;
  gap: 16px;
  padding: 16px 0;
  position: relative;
}

.timeline-item:first-child { padding-top: 0; }
.timeline-item:last-child { padding-bottom: 0; }

.timeline-time {
  width: 48px;
  font-size: 13px;
  font-weight: 600;
  color: #6B7280;
  text-align: right;
  flex-shrink: 0;
}

.timeline-content {
  flex: 1;
  padding-left: 24px;
  position: relative;
}

.timeline-content::before {
  content: '';
  position: absolute;
  left: -5px;
  top: 6px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: white;
  border: 2px solid #FF6B35;
}

.course-tag {
  display: inline-block;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 4px;
  margin-bottom: 6px;
}

.course-tag.private {
  color: #FF6B35;
  background: rgba(255,107,53,0.1);
}

.course-tag.public {
  color: #06D6A0;
  background: rgba(6,214,160,0.1);
}

.course-name {
  font-size: 14px;
  font-weight: 600;
  color: #1A1A2E;
  margin-bottom: 4px;
}

.course-meta {
  font-size: 12px;
  color: #6B7280;
}

.booking-count {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 快捷操作 */
.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 24px 16px;
  border-radius: 12px;
  background: #F8FAFC;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-item:hover {
  background: white;
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
  transform: translateY(-2px);
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.action-icon.primary { background: linear-gradient(135deg, #FF6B35, #FF8C61); }
.action-icon.success { background: linear-gradient(135deg, #06D6A0, #2EC4B6); }
.action-icon.warning { background: linear-gradient(135deg, #FFD166, #FFB347); }
.action-icon.info { background: linear-gradient(135deg, #667eea, #764ba2); }

.action-label {
  font-size: 13px;
  font-weight: 500;
  color: #4B5563;
}

.empty-state {
  padding: 40px 0;
}

/* 响应式 */
@media (max-width: 1024px) {
  .stats-grid :deep(.n-grid-item) {
    grid-column: span 2;
  }
}

@media (max-width: 768px) {
  .stats-grid :deep(.n-grid-item) {
    grid-column: span 4;
  }
  
  .chart-container {
    height: 220px;
  }
}
</style>
