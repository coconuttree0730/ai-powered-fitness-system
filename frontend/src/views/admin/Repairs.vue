<template>
  <div class="admin-repairs">
    <n-card title="报修管理">
      <n-data-table :columns="columns" :data="repairs" :loading="loading" :pagination="pagination" />
    </n-card>
  </div>
</template>

<script setup>
import { ref, h, reactive, onMounted } from 'vue'
import { NTag, NButton, NSpace, useMessage } from 'naive-ui'

const message = useMessage()
const loading = ref(false)
const repairs = ref([])

const pagination = reactive({ pageSize: 10 })

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
        COMPLETED: { type: 'success', text: '已完成' }
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
      if (row.status === 'PENDING') {
        return h(NButton, { size: 'small', type: 'primary', onClick: () => handleProcess(row.id) }, () => '开始处理')
      }
      if (row.status === 'PROCESSING') {
        return h(NButton, { size: 'small', type: 'success', onClick: () => handleComplete(row.id) }, () => '完成')
      }
      return null
    }
  }
]

onMounted(() => {
  fetchRepairs()
})

async function fetchRepairs() {
  loading.value = true
  repairs.value = []
  loading.value = false
}

function handleProcess(id) {
  message.info('开始处理')
}

function handleComplete(id) {
  message.info('已完成')
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
