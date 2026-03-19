import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export const useScheduleStore = defineStore('schedule', () => {
  // 日程列表
  const schedules = ref([])

  // 按日期分组的日程
  const schedulesByDate = computed(() => {
    const grouped = {}
    schedules.value.forEach(schedule => {
      if (!grouped[schedule.date]) {
        grouped[schedule.date] = []
      }
      grouped[schedule.date].push(schedule)
    })
    return grouped
  })

  // 获取某日期范围内的日程
  const getSchedulesByDateRange = (startDate, endDate) => {
    return schedules.value.filter(s => {
      const date = new Date(s.date)
      return date >= startDate && date <= endDate
    })
  }

  // 添加日程
  const addSchedule = (schedule) => {
    const newSchedule = {
      id: Date.now(),
      ...schedule,
      createdAt: new Date().toISOString()
    }
    schedules.value.push(newSchedule)
    return newSchedule
  }

  // 删除日程
  const removeSchedule = (id) => {
    const index = schedules.value.findIndex(s => s.id === id)
    if (index > -1) {
      schedules.value.splice(index, 1)
      return true
    }
    return false
  }

  // 更新日程
  const updateSchedule = (id, updates) => {
    const index = schedules.value.findIndex(s => s.id === id)
    if (index > -1) {
      schedules.value[index] = { ...schedules.value[index], ...updates }
      return schedules.value[index]
    }
    return null
  }

  // 获取某天的日程
  const getSchedulesByDate = (date) => {
    return schedules.value.filter(s => s.date === date)
  }

  // 初始化一些示例数据
  const initSampleData = () => {
    const today = new Date()
    const tomorrow = new Date(today)
    tomorrow.setDate(tomorrow.getDate() + 1)
    
    schedules.value = [
      {
        id: 1,
        studentId: 1,
        studentName: '王小明',
        date: today.toISOString().split('T')[0],
        startTime: '09:00',
        endTime: '10:00',
        venue: 'C区 - 私教训练室',
        courseType: 'private',
        description: '胸部训练：卧推、飞鸟、俯卧撑',
        enableReminder: true,
        reminderTime: 30,
        createdAt: new Date().toISOString()
      },
      {
        id: 2,
        studentId: 2,
        studentName: '李小红',
        date: today.toISOString().split('T')[0],
        startTime: '14:00',
        endTime: '15:00',
        venue: 'D区 - 瑜伽室',
        courseType: 'private',
        description: '瑜伽课程：基础体式练习',
        enableReminder: true,
        reminderTime: 30,
        createdAt: new Date().toISOString()
      },
      {
        id: 3,
        studentId: null,
        studentName: '公开课',
        date: tomorrow.toISOString().split('T')[0],
        startTime: '10:00',
        endTime: '11:30',
        venue: 'F区 - 多功能厅',
        courseType: 'public',
        description: '有氧健身操团体课程',
        enableReminder: false,
        reminderTime: 30,
        createdAt: new Date().toISOString()
      }
    ]
  }

  return {
    schedules,
    schedulesByDate,
    getSchedulesByDateRange,
    addSchedule,
    removeSchedule,
    updateSchedule,
    getSchedulesByDate,
    initSampleData
  }
})
