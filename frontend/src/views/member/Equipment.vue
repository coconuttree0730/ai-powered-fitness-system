<template>
  <div class="equipment-page">
    <n-card title="器材列表">
      <template #header-extra>
        <n-input v-model:value="keyword" placeholder="搜索器材" clearable style="width: 200px" />
      </template>
      <n-data-table :columns="columns" :data="filteredEquipment" :loading="loading" />
    </n-card>
  </div>
</template>

<script setup>
import { ref, h, computed, onMounted } from 'vue'
import { NTag } from 'naive-ui'
import { getEquipmentList } from '@/api/equipment'

const loading = ref(false)
const equipment = ref([])
const keyword = ref('')

const columns = [
  { title: '器材名称', key: 'name' },
  { title: '类型', key: 'type' },
  { title: '位置', key: 'location' },
  {
    title: '状态',
    key: 'status',
    render: (row) => {
      const statusMap = {
        AVAILABLE: { type: 'success', text: '可用' },
        IN_USE: { type: 'warning', text: '使用中' },
        MAINTENANCE: { type: 'error', text: '维护中' },
        BROKEN: { type: 'default', text: '已损坏' }
      }
      const status = statusMap[row.status] || { type: 'default', text: row.status }
      return h(NTag, { type: status.type }, () => status.text)
    }
  },
  { title: '描述', key: 'description', ellipsis: { tooltip: true } }
]

const filteredEquipment = computed(() => {
  if (!keyword.value) return equipment.value
  return equipment.value.filter(e => e.name?.includes(keyword.value) || e.type?.includes(keyword.value))
})

onMounted(() => {
  fetchEquipment()
})

async function fetchEquipment() {
  loading.value = true
  try {
    const res = await getEquipmentList()
    equipment.value = res.data || []
  } catch (error) {
    console.error('获取器材列表失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.equipment-page {
  padding: 0;
}
</style>
