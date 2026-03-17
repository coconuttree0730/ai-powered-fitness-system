<template>
  <div class="coach-students">
    <n-card title="我的学员">
      <n-data-table :columns="columns" :data="students" :loading="loading" />
    </n-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const loading = ref(false)
const students = ref([])

const columns = [
  { title: '姓名', key: 'realName' },
  { title: '手机号', key: 'phone' },
  { title: '预约课程数', key: 'bookingCount' },
  { title: '最近预约', key: 'lastBooking', render: (row) => formatTime(row.lastBooking) }
]

onMounted(() => {
  fetchStudents()
})

async function fetchStudents() {
  loading.value = true
  students.value = []
  loading.value = false
}

function formatTime(time) {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.coach-students {
  padding: 0;
}
</style>
