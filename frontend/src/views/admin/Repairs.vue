<template>
  <div class="admin-repairs">
    <n-card title="报修管理">
      <n-data-table :columns="columns" :data="repairs" :loading="loading" :pagination="pagination" :row-key="row => row.id" />
    </n-card>
  </div>
</template>

<script setup>
import { ref, h, reactive, onMounted } from 'vue'
import { NTag, NButton, NSpace, useMessage } from 'naive-ui'
import { getAllRepairs, handleRepair } from '@/api/equipment'

const message = useMessage()
const loading = ref(false)
const repairs = ref([])

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  onChange: (page) => {
    pagination.page = page
    fetchRepairs()
  }
})

const columns = [
  { title: '器材名称', key: 'equipmentName' },
  { title: '报修人', key: 'reporterName' },
  { title: '问题描述', key: 'description', ellipsis: { tooltip: true } },
  {
    title: '状态',
    key: 'status',
    render: (row) => {
      const statusMap = {
        PENDING: { type: 'warning', text: '待处理' },
        PROCESSING: { type: 'info', text: '处理中' },
        COMPLETED: { type: 'success', text: '已完成' },
        CANCELLED: { type: 'default', text: '已取消' },
        REJECTED: { type: 'error', text: '已拒绝' },
        APPROVED: { type: 'success', text: '已批准' },
        IN_PROGRESS: { type: 'info', text: '进行中' },
        DONE: { type: 'success', text: '已完成' }
      }
      const status = statusMap[row.status] || { type: 'default', text: row.status }
      return h(NTag, { type: status.type }, () => status.text)
    }
  },
  { title: '创建时间', key: 'createTime', render: (row) => formatTime(row.createTime) },
  {
    title: '操作',
    key: 'actions',
    render: (row) => {
    const buttons = []
    if (row.status === 'PENDING') {
      buttons.push(h(NButton, { size: 'small', type: 'primary', onClick: () => handleProcessClick(row.id, 'PROCESSING') }, () => '开始处理'))
      buttons.push(h(NButton, { size: 'small', type: 'error', onClick: () => handleProcessClick(row.id, 'REJECTED') }, () => '拒绝'))
    }
    if (row.status === 'PROCESSING' || row.status === 'IN_PROGRESS') {
      buttons.push(h(NButton, { size: 'small', type: 'success', onClick: () => handleProcessClick(row.id, 'DONE') }, () => '完成'))
    }
    return h(NSpace, null, () => buttons)
  }
  }
]

onMounted(() => {
  fetchRepairs()
})

async function fetchRepairs() {
  loading.value = true
  try {
    const res = await getAllRepairs({ pageNum: pagination.page, pageSize: pagination.pageSize })
    repairs.value = res || []
    pagination.itemCount = repairs.value.length
  } catch (error) {
    message.error('获取报修列表失败')
  } finally {
    loading.value = false
  }
}

async function handleProcessClick(id, status) {
  try {
    await handleRepair(id, { status })
    message.success('操作成功')
    fetchRepairs()
  } catch (error) {
    message.error('操作失败')
  }
}

function formatTime(time) {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.admin-repairs {
  padding: 0;
}
</style>
