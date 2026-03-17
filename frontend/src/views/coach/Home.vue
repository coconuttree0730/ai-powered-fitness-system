<template>
  <div class="coach-home">
    <n-grid :cols="2" :x-gap="20">
      <n-grid-item>
        <n-card title="今日课程">
          <n-empty v-if="todayCourses.length === 0" description="今日暂无课程" />
          <n-list v-else bordered>
            <n-list-item v-for="course in todayCourses" :key="course.id">
              <n-thing :title="course.courseName">
                <template #description>
                  <n-space>
                    <span>时间: {{ formatTime(course.startTime) }}</span>
                    <span>预约: {{ course.bookingCount }}/{{ course.capacity }}</span>
                  </n-space>
                </template>
              </n-thing>
            </n-list-item>
          </n-list>
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card title="统计数据">
          <n-descriptions label-placement="left" :column="1">
            <n-descriptions-item label="本周课程数">{{ stats.weekCourses }}</n-descriptions-item>
            <n-descriptions-item label="本月课程数">{{ stats.monthCourses }}</n-descriptions-item>
            <n-descriptions-item label="总学员数">{{ stats.totalStudents }}</n-descriptions-item>
          </n-descriptions>
        </n-card>
      </n-grid-item>
    </n-grid>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'

const todayCourses = ref([])
const stats = reactive({
  weekCourses: 0,
  monthCourses: 0,
  totalStudents: 0
})

onMounted(() => {
  fetchTodayCourses()
  fetchStats()
})

async function fetchTodayCourses() {
  todayCourses.value = []
}

async function fetchStats() {
  stats.weekCourses = 0
  stats.monthCourses = 0
  stats.totalStudents = 0
}

function formatTime(time) {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}
</script>

<style scoped>
.coach-home {
  padding: 0;
}
</style>
