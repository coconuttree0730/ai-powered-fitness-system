import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export const useScheduleStore = defineStore('schedule', () => {
  const schedules = ref([])

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

  const getSchedulesByDateRange = (startDate, endDate) => {
    return schedules.value.filter(s => {
      const date = new Date(s.date)
      return date >= startDate && date <= endDate
    })
  }

  const addSchedule = (schedule) => {
    const newSchedule = {
      id: crypto.randomUUID(),
      ...schedule,
      create_time: new Date().toISOString()
    }
    schedules.value.push(newSchedule)
    return newSchedule
  }

  const removeSchedule = (id) => {
    const index = schedules.value.findIndex(s => s.id === id)
    if (index > -1) {
      schedules.value.splice(index, 1)
      return true
    }
    return false
  }

  const updateSchedule = (id, updates) => {
    const index = schedules.value.findIndex(s => s.id === id)
    if (index > -1) {
      schedules.value[index] = { ...schedules.value[index], ...updates }
      return schedules.value[index]
    }
    return null
  }

  const getSchedulesByDate = (date) => {
    return schedules.value.filter(s => s.date === date)
  }

  return {
    schedules,
    schedulesByDate,
    getSchedulesByDateRange,
    addSchedule,
    removeSchedule,
    updateSchedule,
    getSchedulesByDate
  }
})
