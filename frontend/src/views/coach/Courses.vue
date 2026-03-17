<template>
  <div class="coach-courses">
    <n-card title="我的课程">
      <n-data-table :columns="columns" :data="courses" :loading="loading" />
    </n-card>
  </div>
</template>

<script setup>
import { ref, h, onMounted } from 'vue'
import { NTag } from 'naive-ui'

const loading = ref(false)
const courses = ref([])

const columns = [
  { title: '课程名称', key: 'courseName' },
  { title: '分类', key: 'category' },
  { title: '开始时间', key: 'startTime', render: (row) => formatTime(row.startTime) },
  { title: '结束时间', key: 'endTime', render: (row) => formatTime(row.endTime) },
  { title: '容量', key: 'capacity' },
  { title: '预约数', key: 'bookingCount' },
  {
    title: '状态',
    key: 'status',
    render: (row) => h(NTag, { type: 'success' }, () => '正常')
  }
]

onMounted(() => {
  fetchCourses()
})

async function fetchCourses() {
  loading.value = true
  courses.value = []
  loading.value = false
}

function formatTime(time) {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.coach-courses {
  padding: 0;
}
</style>
