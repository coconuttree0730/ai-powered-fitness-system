<template>
  <div class="admin-repairs">
    <n-card title="报修管理">
      <n-data-table :columns="columns" :data="displayRepairs" :loading="loading" :pagination="pagination" :row-key="row => row.id" remote />
    </n-card>
  </div>
</template>

<script setup>
import { ref, h, reactive, onMounted, computed } from 'vue'
import { NTag, NButton, NSpace, useMessage } from 'naive-ui'
import { getAllRepairs, handleRepair } from '@/api/equipment'

const message = useMessage()
const loading = ref(false)
const allRepairs = ref([])

const paginationReactive = reactive({
  page: 1,
  pageSize: 5,
  itemCount: 0
})

const pagination = computed(() => ({
  ...paginationReactive,
  onChange: (page) => {
    paginationReactive.page = page
  }
}))

const displayRepairs = computed(() => {
  const start = (paginationReactive.page - 1) * paginationReactive.pageSize
  const end = start + paginationReactive.pageSize
  return allRepairs.value.slice(start, end)
})

const columns = [
  { title: '器材名称', key: 'equipmentName' },
  { title: '报修人', key: 'userName' },
  { title: '问题描述', key: 'description', ellipsis: { tooltip: true } },
  {
    title: '状态',
    key: 'status',
    render: (row) => {
      const statusMap = {
        0: { type: 'warning', text: '待处理' },
        1: { type: 'info', text: '处理中' },
        2: { type: 'success', text: '已完成' },
        3: { type: 'default', text: '已关闭' }
      }
      const status = statusMap[row.status] || { type: 'default', text: '未知' }
      return h(NTag, { type: status.type }, () => status.text)
    }
  },
  { title: '创建时间', key: 'createTime', render: (row) => formatTime(row.createTime) },
  {
    title: '操作',
    key: 'actions',
    render: (row) => {
    const buttons = []
    if (row.status === 0) {
      buttons.push(h(NButton, { size: 'small', type: 'primary', onClick: () => handleProcessClick(row.id, 1) }, () => '开始处理'))
      buttons.push(h(NButton, { size: 'small', type: 'error', onClick: () => handleProcessClick(row.id, 3) }, () => '关闭'))
    }
    if (row.status === 1) {
      buttons.push(h(NButton, { size: 'small', type: 'success', onClick: () => handleProcessClick(row.id, 2) }, () => '完成'))
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
    const res = await getAllRepairs()
    allRepairs.value = res || []
    paginationReactive.itemCount = allRepairs.value.length
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
