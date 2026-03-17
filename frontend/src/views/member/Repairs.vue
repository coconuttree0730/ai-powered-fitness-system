<template>
  <div class="repairs-page">
    <n-grid :cols="1" :x-gap="20">
      <n-grid-item>
        <n-card title="我的报修">
          <template #header-extra>
            <n-button type="primary" @click="showModal = true">新增报修</n-button>
          </template>
          <n-data-table :columns="columns" :data="repairs" :loading="loading" />
        </n-card>
      </n-grid-item>
    </n-grid>

    <n-modal v-model:show="showModal" preset="card" title="新增报修" style="width: 500px">
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="left" label-width="80">
        <n-form-item label="器材" path="equipmentId">
          <n-select v-model:value="form.equipmentId" :options="equipmentOptions" placeholder="请选择器材" />
        </n-form-item>
        <n-form-item label="问题描述" path="description">
          <n-input v-model:value="form.description" type="textarea" placeholder="请描述问题" :rows="4" />
        </n-form-item>
        <n-form-item>
          <n-button type="primary" :loading="submitting" @click="handleSubmit">提交</n-button>
        </n-form-item>
      </n-form>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, h, reactive, onMounted } from 'vue'
import { NTag, useMessage } from 'naive-ui'
import { getMyRepairs, createRepair } from '@/api/equipment'
import { getEquipmentList } from '@/api/equipment'

const message = useMessage()
const loading = ref(false)
const submitting = ref(false)
const showModal = ref(false)
const formRef = ref(null)
const repairs = ref([])
const equipmentOptions = ref([])

const form = reactive({
  equipmentId: null,
  description: ''
})

const rules = {
  equipmentId: [{ required: true, message: '请选择器材', trigger: 'change' }],
  description: [{ required: true, message: '请描述问题', trigger: 'blur' }]
}

const columns = [
  { title: '器材名称', key: 'equipmentName' },
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
  { title: '创建时间', key: 'createTime', render: (row) => formatTime(row.createTime) }
]

onMounted(() => {
  fetchRepairs()
  fetchEquipment()
})

async function fetchRepairs() {
  loading.value = true
  try {
    const res = await getMyRepairs()
    repairs.value = res.data || []
  } catch (error) {
    console.error('获取报修列表失败:', error)
  } finally {
    loading.value = false
  }
}

async function fetchEquipment() {
  try {
    const res = await getEquipmentList()
    equipmentOptions.value = (res.data || []).map(e => ({ label: e.name, value: e.id }))
  } catch (error) {
    console.error('获取器材列表失败:', error)
  }
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    submitting.value = true
    await createRepair(form)
    message.success('提交成功')
    showModal.value = false
    form.equipmentId = null
    form.description = ''
    fetchRepairs()
  } catch (error) {
    message.error(error.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

function formatTime(time) {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.repairs-page {
  padding: 0;
}
</style>
