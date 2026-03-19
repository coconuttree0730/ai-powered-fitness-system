<template>
  <div class="coach-schedule">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">课程日程</h2>
        <div class="date-nav">
          <n-button text @click="changeMonth(-1)">
            <n-icon :component="ChevronBackOutline" size="20" />
          </n-button>
          <span class="current-month">{{ currentYearMonth }}</span>
          <n-button text @click="changeMonth(1)">
            <n-icon :component="ChevronForwardOutline" size="20" />
          </n-button>
        </div>
      </div>
      <div class="header-right">
        <n-button type="primary" @click="showPublishModal = true">
          <template #icon>
            <n-icon :component="AddOutline" />
          </template>
          发布课程
        </n-button>
        <n-radio-group v-model:value="viewMode" size="medium">
          <n-radio-button value="month">月视图</n-radio-button>
          <n-radio-button value="week">周视图</n-radio-button>
        </n-radio-group>
      </div>
    </div>

    <!-- 图例 -->
    <div class="legend-bar">
      <div class="legend-item">
        <span class="legend-dot private"></span>
        <span class="legend-text">私教课</span>
      </div>
      <div class="legend-item">
        <span class="legend-dot public"></span>
        <span class="legend-text">公开课</span>
      </div>
      <div class="legend-item">
        <span class="legend-dot completed"></span>
        <span class="legend-text">已完成</span>
      </div>
    </div>

    <!-- 月视图日历 -->
    <div v-if="viewMode === 'month'" class="calendar-container">
      <!-- 星期标题 -->
      <div class="week-header">
        <div v-for="day in weekDays" :key="day" class="week-day">{{ day }}</div>
      </div>
      
      <!-- 日历格子 -->
      <div class="calendar-grid">
        <div 
          v-for="(day, index) in calendarDays" 
          :key="index"
          class="calendar-cell"
          :class="{ 
            'other-month': !day.isCurrentMonth,
            'today': day.isToday,
            'has-courses': day.courses.length > 0
          }"
          @click="handleDayClick(day)"
        >
          <div class="cell-header">
            <span class="day-number">{{ day.date.getDate() }}</span>
            <span v-if="day.isToday" class="today-badge">今天</span>
          </div>
          
          <div class="cell-content">
            <div 
              v-for="course in day.courses.slice(0, 3)" 
              :key="course.id"
              class="course-item"
              :class="course.type"
              @click.stop="handleCourseClick(course)"
            >
              <span class="course-time">{{ course.startTime }}</span>
              <span class="course-name">{{ course.courseName }}</span>
              <span v-if="course.type === 'public'" class="public-badge">公开</span>
            </div>
            
            <div v-if="day.courses.length > 3" class="more-courses">
              +{{ day.courses.length - 3 }} 更多
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 周视图 -->
    <div v-else class="week-view">
      <div class="week-grid">
        <div class="time-column">
          <div class="time-header">时间</div>
          <div v-for="hour in hours" :key="hour" class="time-slot">
            {{ hour }}:00
          </div>
        </div>
        
        <div v-for="(day, index) in weekDaysData" :key="index" class="day-column">
          <div class="day-header" :class="{ 'today': day.isToday }">
            <div class="day-name">{{ day.name }}</div>
            <div class="day-date">{{ day.date }}</div>
          </div>
          
          <div class="day-slots">
            <div 
              v-for="hour in hours" 
              :key="hour" 
              class="hour-slot"
              @click="handleSlotClick(day, hour)"
            >
              <div 
                v-for="course in getCoursesForSlot(day, hour)" 
                :key="course.id"
                class="week-course-item"
                :class="course.type"
                :style="{ height: course.duration * 60 + 'px' }"
                @click.stop="handleCourseClick(course)"
              >
                <div class="course-title">{{ course.courseName }}</div>
                <div class="course-info">
                  {{ course.startTime }} - {{ course.endTime }}
                </div>
                <div v-if="course.type === 'public'" class="public-tag">公开课</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 当日课程详情弹窗 -->
    <n-modal 
      v-model:show="showDayModal" 
      preset="card" 
      :title="`${selectedDate} 的课程安排`"
      style="width: 600px"
    >
      <div v-if="selectedDayCourses.length === 0" class="empty-day">
        <n-empty description="当日暂无课程">
          <template #extra>
            <n-button type="primary" @click="showPublishModal = true">
              发布课程
            </n-button>
          </template>
        </n-empty>
      </div>
      
      <div v-else class="day-courses-list">
        <div 
          v-for="course in selectedDayCourses" 
          :key="course.id"
          class="day-course-card"
          :class="course.type"
        >
          <div class="course-time-block">
            <div class="start-time">{{ course.startTime }}</div>
            <div class="time-line"></div>
            <div class="end-time">{{ course.endTime }}</div>
          </div>
          
          <div class="course-details">
            <div class="detail-header">
              <h4 class="detail-name">{{ course.courseName }}</h4>
              <n-tag :type="course.type === 'private' ? 'warning' : 'success'" size="small">
                {{ course.type === 'private' ? '私教课' : '公开课' }}
              </n-tag>
            </div>
            
            <div class="detail-info">
              <span class="info-item">
                <n-icon :component="PersonOutline" size="14" />
                {{ course.bookingCount }}/{{ course.capacity }} 人
              </span>
              <span class="info-item">
                <n-icon :component="LocationOutline" size="14" />
                {{ course.location || 'A区训练室' }}
              </span>
            </div>
            
            <div v-if="course.type === 'public'" class="public-notice">
              <n-icon :component="InformationCircleOutline" size="14" />
              公开课 - 所有会员可预约
            </div>
            
            <div class="detail-actions">
              <n-button size="small" @click="editCourse(course)">编辑</n-button>
              <n-button size="small" type="error" @click="deleteCourse(course)">删除</n-button>
            </div>
          </div>
        </div>
      </div>
    </n-modal>

    <!-- 发布课程弹窗 -->
    <n-modal 
      v-model:show="showPublishModal" 
      preset="card" 
      title="发布课程" 
      style="width: 550px"
    >
      <n-form 
        ref="formRef"
        :model="publishForm"
        :rules="publishRules"
        label-placement="left"
        label-width="90"
      >
        <n-form-item label="课程名称" path="courseName">
          <n-input v-model:value="publishForm.courseName" placeholder="请输入课程名称" />
        </n-form-item>
        
        <n-form-item label="课程类型" path="courseType">
          <n-radio-group v-model:value="publishForm.courseType">
            <n-radio-button value="private">私教课</n-radio-button>
            <n-radio-button value="public">公开课</n-radio-button>
          </n-radio-group>
        </n-form-item>
        
        <n-grid :cols="2" :x-gap="16">
          <n-grid-item>
            <n-form-item label="日期" path="courseDate">
              <n-date-picker 
                v-model:value="publishForm.courseDate" 
                type="date" 
                style="width: 100%"
              />
            </n-form-item>
          </n-grid-item>
          <n-grid-item>
            <n-form-item label="场地" path="location">
              <n-select 
                v-model:value="publishForm.location" 
                :options="locationOptions"
                placeholder="选择场地"
              />
            </n-form-item>
          </n-grid-item>
        </n-grid>
        
        <n-grid :cols="2" :x-gap="16">
          <n-grid-item>
            <n-form-item label="开始时间" path="startTime">
              <n-time-picker 
                v-model:value="publishForm.startTime" 
                style="width: 100%"
                format="HH:mm"
              />
            </n-form-item>
          </n-grid-item>
          <n-grid-item>
            <n-form-item label="结束时间" path="endTime">
              <n-time-picker 
                v-model:value="publishForm.endTime" 
                style="width: 100%"
                format="HH:mm"
              />
            </n-form-item>
          </n-grid-item>
        </n-grid>
        
        <n-form-item label="课程容量" path="capacity">
          <n-input-number 
            v-model:value="publishForm.capacity" 
            :min="1"
            :max="publishForm.courseType === 'private' ? 1 : 50"
            style="width: 100%"
          >
            <template #suffix>人</template>
          </n-input-number>
        </n-form-item>
      </n-form>
      
      <template #footer>
        <n-space justify="end">
          <n-button @click="showPublishModal = false">取消</n-button>
          <n-button type="primary" :loading="publishLoading" @click="handlePublish">
            确认发布
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, computed, h, onMounted } from 'vue'
import { useMessage, useDialog } from 'naive-ui'
import { useScheduleStore } from '@/stores/schedule'
import {
  ChevronBackOutline,
  ChevronForwardOutline,
  AddOutline,
  PersonOutline,
  LocationOutline,
  InformationCircleOutline,
  TimeOutline
} from '@vicons/ionicons5'

const message = useMessage()
const dialog = useDialog()
const scheduleStore = useScheduleStore()

// 当前日期
const currentDate = ref(new Date())
const viewMode = ref('month')

// 弹窗状态
const showDayModal = ref(false)
const showPublishModal = ref(false)
const selectedDate = ref('')
const selectedDayCourses = ref([])
const publishLoading = ref(false)

// 表单
const formRef = ref(null)
const publishForm = ref({
  courseName: '',
  courseType: 'private',
  courseDate: null,
  startTime: null,
  endTime: null,
  capacity: 1,
  location: null
})

const publishRules = {
  courseName: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  courseType: [{ required: true, message: '请选择课程类型', trigger: 'change' }],
  courseDate: [{ required: true, message: '请选择日期', trigger: 'change', type: 'number' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change', type: 'number' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change', type: 'number' }],
  capacity: [{ required: true, message: '请输入容量', trigger: 'blur', type: 'number' }]
}

const locationOptions = [
  { label: 'A区训练室', value: 'A区训练室' },
  { label: 'B区瑜伽室', value: 'B区瑜伽室' },
  { label: 'C区有氧区', value: 'C区有氧区' },
  { label: 'D区力量区', value: 'D区力量区' }
]

// 星期标题
const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']

// 小时列表
const hours = Array.from({ length: 14 }, (_, i) => i + 8) // 8:00 - 21:00

// 当前年月显示
const currentYearMonth = computed(() => {
  const year = currentDate.value.getFullYear()
  const month = currentDate.value.getMonth() + 1
  return `${year}年${month}月`
})

// 从 store 获取课程数据
const courses = computed(() => {
  return scheduleStore.schedules.map(s => ({
    id: s.id,
    courseName: s.studentName || '课程',
    courseType: s.courseType,
    date: s.date,
    startTime: s.startTime,
    endTime: s.endTime,
    capacity: s.courseType === 'private' ? 1 : 20,
    bookingCount: s.courseType === 'private' ? 1 : 0,
    location: s.venue,
    description: s.description
  }))
})

// 初始化示例数据
onMounted(() => {
  if (scheduleStore.schedules.length === 0) {
    scheduleStore.initSampleData()
  }
})

// 生成日历数据
const calendarDays = computed(() => {
  const year = currentDate.value.getFullYear()
  const month = currentDate.value.getMonth()
  
  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)
  const startDate = new Date(firstDay)
  startDate.setDate(startDate.getDate() - firstDay.getDay())
  
  const days = []
  const today = new Date()
  
  for (let i = 0; i < 42; i++) {
    const date = new Date(startDate)
    date.setDate(startDate.getDate() + i)
    
    const dateStr = date.toISOString().split('T')[0]
    const dayCourses = courses.value.filter(c => c.date === dateStr)
    
    days.push({
      date,
      isCurrentMonth: date.getMonth() === month,
      isToday: date.toDateString() === today.toDateString(),
      courses: dayCourses
    })
  }
  
  return days
})

// 周视图数据
const weekDaysData = computed(() => {
  const startOfWeek = new Date(currentDate.value)
  startOfWeek.setDate(startOfWeek.getDate() - startOfWeek.getDay())
  
  const today = new Date()
  
  return weekDays.map((name, index) => {
    const date = new Date(startOfWeek)
    date.setDate(startOfWeek.getDate() + index)
    
    return {
      name,
      date: `${date.getMonth() + 1}/${date.getDate()}`,
      fullDate: date.toISOString().split('T')[0],
      isToday: date.toDateString() === today.toDateString()
    }
  })
})

// 切换月份
function changeMonth(delta) {
  const newDate = new Date(currentDate.value)
  newDate.setMonth(newDate.getMonth() + delta)
  currentDate.value = newDate
}

// 处理日期点击
function handleDayClick(day) {
  selectedDate.value = `${day.date.getMonth() + 1}月${day.date.getDate()}日`
  selectedDayCourses.value = day.courses
  showDayModal.value = true
}

// 处理课程点击
function handleCourseClick(course) {
  dialog.info({
    title: course.courseName,
    content: () => h('div', null, [
      h('p', null, `类型: ${course.courseType === 'private' ? '私教课' : '公开课'}`),
      h('p', null, `时间: ${course.startTime} - ${course.endTime}`),
      h('p', null, `地点: ${course.location}`),
      h('p', null, `预约: ${course.bookingCount}/${course.capacity}`)
    ])
  })
}

// 处理时间段点击
function handleSlotClick(day, hour) {
  publishForm.value.courseDate = new Date(day.fullDate).getTime()
  publishForm.value.startTime = new Date(`${day.fullDate} ${hour}:00`).getTime()
  showPublishModal.value = true
}

// 获取时间段的课程
function getCoursesForSlot(day, hour) {
  return courses.value.filter(c => {
    if (c.date !== day.fullDate) return false
    const startHour = parseInt(c.startTime.split(':')[0])
    return startHour === hour
  }).map(c => ({
    ...c,
    duration: parseInt(c.endTime.split(':')[0]) - parseInt(c.startTime.split(':')[0])
  }))
}

// 发布课程
async function handlePublish() {
  try {
    await formRef.value?.validate()
    publishLoading.value = true
    
    await new Promise(resolve => setTimeout(resolve, 800))
    
    const newCourse = {
      id: Date.now(),
      courseName: publishForm.value.courseName,
      courseType: publishForm.value.courseType,
      date: new Date(publishForm.value.courseDate).toISOString().split('T')[0],
      startTime: formatTime(publishForm.value.startTime),
      endTime: formatTime(publishForm.value.endTime),
      capacity: publishForm.value.capacity,
      bookingCount: 0,
      location: publishForm.value.location || 'A区训练室'
    }
    
    courses.value.push(newCourse)
    message.success('课程发布成功')
    showPublishModal.value = false
    resetForm()
  } catch (error) {
    console.error(error)
  } finally {
    publishLoading.value = false
  }
}

// 格式化时间
function formatTime(timestamp) {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 重置表单
function resetForm() {
  publishForm.value = {
    courseName: '',
    courseType: 'private',
    courseDate: null,
    startTime: null,
    endTime: null,
    capacity: 1,
    location: null
  }
  formRef.value?.restoreValidation()
}

// 编辑课程
function editCourse(course) {
  message.info(`编辑课程: ${course.courseName}`)
}

// 删除课程
function deleteCourse(course) {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除课程 "${course.courseName}" 吗？`,
    positiveText: '确认删除',
    negativeText: '取消',
    onPositiveClick: () => {
      const index = courses.value.findIndex(c => c.id === course.id)
      if (index > -1) {
        courses.value.splice(index, 1)
        selectedDayCourses.value = selectedDayCourses.value.filter(c => c.id !== course.id)
        message.success('课程已删除')
      }
    }
  })
}
</script>

<style scoped>
.coach-schedule {
  max-width: 1400px;
  margin: 0 auto;
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  gap: 16px;
  flex-wrap: wrap;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1A1A2E;
  margin: 0;
}

.date-nav {
  display: flex;
  align-items: center;
  gap: 12px;
  background: white;
  padding: 8px 16px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.current-month {
  font-size: 16px;
  font-weight: 600;
  color: #1A1A2E;
  min-width: 100px;
  text-align: center;
}

.header-right {
  display: flex;
  gap: 12px;
}

/* 图例 */
.legend-bar {
  display: flex;
  gap: 24px;
  margin-bottom: 20px;
  padding: 12px 20px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  width: fit-content;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.legend-dot.private { background: linear-gradient(135deg, #FF6B35, #FF8C61); }
.legend-dot.public { background: linear-gradient(135deg, #06D6A0, #2EC4B6); }
.legend-dot.completed { background: #9CA3AF; }

.legend-text {
  font-size: 13px;
  color: #6B7280;
}

/* 日历容器 */
.calendar-container {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.06);
}

/* 星期标题 */
.week-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8px;
  margin-bottom: 12px;
}

.week-day {
  text-align: center;
  font-size: 14px;
  font-weight: 600;
  color: #6B7280;
  padding: 12px;
}

/* 日历网格 */
.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8px;
}

.calendar-cell {
  min-height: 120px;
  background: #F8FAFC;
  border-radius: 12px;
  padding: 12px;
  cursor: pointer;
  transition: all 0.2s;
  border: 2px solid transparent;
}

.calendar-cell:hover {
  background: white;
  border-color: #E5E7EB;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.calendar-cell.other-month {
  opacity: 0.4;
}

.calendar-cell.today {
  background: linear-gradient(135deg, rgba(255,107,53,0.05), rgba(255,140,97,0.1));
  border-color: #FF6B35;
}

.calendar-cell.has-courses {
  background: white;
}

.cell-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.day-number {
  font-size: 16px;
  font-weight: 600;
  color: #1A1A2E;
}

.today-badge {
  font-size: 10px;
  font-weight: 600;
  color: #FF6B35;
  background: rgba(255,107,53,0.1);
  padding: 2px 6px;
  border-radius: 4px;
}

.cell-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.course-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 12px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.course-item.private {
  background: linear-gradient(90deg, rgba(255,107,53,0.15), rgba(255,107,53,0.05));
  color: #FF6B35;
}

.course-item.public {
  background: linear-gradient(90deg, rgba(6,214,160,0.15), rgba(6,214,160,0.05));
  color: #06D6A0;
}

.course-time {
  font-weight: 600;
  font-size: 11px;
}

.course-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
}

.public-badge {
  font-size: 9px;
  padding: 1px 4px;
  background: #06D6A0;
  color: white;
  border-radius: 3px;
}

.more-courses {
  font-size: 11px;
  color: #9CA3AF;
  text-align: center;
  padding: 4px;
}

/* 周视图 */
.week-view {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.06);
  overflow-x: auto;
}

.week-grid {
  display: grid;
  grid-template-columns: 80px repeat(7, 1fr);
  gap: 8px;
  min-width: 900px;
}

.time-column,
.day-column {
  display: flex;
  flex-direction: column;
}

.time-header,
.day-header {
  height: 50px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #F8FAFC;
  border-radius: 8px;
  margin-bottom: 8px;
}

.day-header.today {
  background: linear-gradient(135deg, #FF6B35, #FF8C61);
  color: white;
}

.day-name {
  font-size: 13px;
  font-weight: 600;
}

.day-date {
  font-size: 11px;
  opacity: 0.8;
}

.time-slot {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #6B7280;
  border-bottom: 1px solid #F3F4F6;
}

.day-slots {
  display: flex;
  flex-direction: column;
}

.hour-slot {
  height: 60px;
  border-bottom: 1px solid #F3F4F6;
  position: relative;
  cursor: pointer;
}

.hour-slot:hover {
  background: #F8FAFC;
}

.week-course-item {
  position: absolute;
  left: 2px;
  right: 2px;
  top: 2px;
  border-radius: 6px;
  padding: 6px;
  font-size: 11px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s;
}

.week-course-item:hover {
  transform: scale(1.02);
  z-index: 10;
}

.week-course-item.private {
  background: linear-gradient(135deg, rgba(255,107,53,0.9), rgba(255,140,97,0.9));
  color: white;
}

.week-course-item.public {
  background: linear-gradient(135deg, rgba(6,214,160,0.9), rgba(46,196,182,0.9));
  color: white;
}

.course-title {
  font-weight: 600;
  margin-bottom: 2px;
}

.course-info {
  font-size: 10px;
  opacity: 0.9;
}

.public-tag {
  position: absolute;
  top: 2px;
  right: 2px;
  font-size: 9px;
  padding: 1px 4px;
  background: rgba(255,255,255,0.3);
  border-radius: 3px;
}

/* 当日课程弹窗 */
.empty-day {
  padding: 60px 0;
}

.day-courses-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.day-course-card {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #F8FAFC;
  border-radius: 12px;
  border-left: 4px solid transparent;
}

.day-course-card.private {
  border-left-color: #FF6B35;
}

.day-course-card.public {
  border-left-color: #06D6A0;
}

.course-time-block {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  width: 60px;
  flex-shrink: 0;
}

.start-time,
.end-time {
  font-size: 13px;
  font-weight: 600;
  color: #1A1A2E;
}

.time-line {
  width: 2px;
  flex: 1;
  background: #E5E7EB;
  min-height: 20px;
}

.course-details {
  flex: 1;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.detail-name {
  font-size: 16px;
  font-weight: 600;
  color: #1A1A2E;
  margin: 0;
}

.detail-info {
  display: flex;
  gap: 16px;
  margin-bottom: 8px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #6B7280;
}

.public-notice {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #06D6A0;
  background: rgba(6,214,160,0.1);
  padding: 6px 10px;
  border-radius: 6px;
  margin-bottom: 12px;
}

.detail-actions {
  display: flex;
  gap: 8px;
}

/* 响应式 */
@media (max-width: 1024px) {
  .calendar-grid {
    gap: 4px;
  }
  
  .calendar-cell {
    min-height: 80px;
    padding: 8px;
  }
  
  .course-item {
    font-size: 10px;
    padding: 2px 4px;
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: stretch;
  }
  
  .header-left,
  .header-right {
    justify-content: space-between;
  }
  
  .calendar-container {
    padding: 12px;
  }
  
  .week-day {
    padding: 8px 4px;
    font-size: 12px;
  }
  
  .calendar-cell {
    min-height: 60px;
    padding: 6px;
  }
  
  .day-number {
    font-size: 12px;
  }
}
</style>
