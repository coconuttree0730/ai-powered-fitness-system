<template>
  <div class="bookings-page">
    <n-card title="我的预约">
      <n-data-table :columns="columns" :data="bookings" :loading="loading" />
    </n-card>
  </div>
</template>

<script setup>
import { ref, h, onMounted } from 'vue'
import { NTag, NButton, useMessage } from 'naive-ui'
import { getMyBookings, cancelBooking } from '@/api/course'

const message = useMessage()
const loading = ref(false)
const bookings = ref([])

const columns = [
  { title: '课程名称', key: 'courseName' },
  { title: '教练', key: 'coachName' },
  { title: '开始时间', key: 'startTime', render: (row) => formatTime(row.startTime) },
  { title: '结束时间', key: 'endTime', render: (row) => formatTime(row.endTime) },
  {
    title: '状态',
    key: 'status',
    render: (row) => h(NTag, { type: row.status === 1 ? 'success' : 'default' }, () => row.status === 1 ? '已预约' : '已取消')
  },
  {
    title: '操作',
    key: 'actions',
    render: (row) => row.status === 1 ? h(NButton, { type: 'error', size: 'small', onClick: () => handleCancel(row.id) }, () => '取消预约') : null
  }
]

onMounted(() => {
  fetchBookings()
})

async function fetchBookings() {
  loading.value = true
  try {
    const res = await getMyBookings()
    bookings.value = res.data || []
  } catch (error) {
    console.error('获取预约列表失败:', error)
  } finally {
    loading.value = false
  }
}

async function handleCancel(id) {
  try {
    await cancelBooking(id)
    message.success('取消成功')
    fetchBookings()
  } catch (error) {
    message.error(error.message || '取消失败')
  }
}

function formatTime(time) {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.bookings-page {
  padding: 0;
}
</style>
