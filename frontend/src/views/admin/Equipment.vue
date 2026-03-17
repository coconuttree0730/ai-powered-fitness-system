<template>
  <div class="admin-equipment">
    <n-card title="器材管理">
      <template #header-extra>
        <n-button type="primary" @click="showModal = true">新增器材</n-button>
      </template>
      <n-data-table :columns="columns" :data="equipment" :loading="loading" :pagination="pagination" />
    </n-card>

    <n-modal v-model:show="showModal" preset="card" title="新增器材" style="width: 500px">
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="left" label-width="80">
        <n-form-item label="名称" path="name">
          <n-input v-model:value="form.name" placeholder="请输入器材名称" />
        </n-form-item>
        <n-form-item label="类型" path="type">
          <n-input v-model:value="form.type" placeholder="请输入器材类型" />
        </n-form-item>
        <n-form-item label="位置" path="location">
          <n-input v-model:value="form.location" placeholder="请输入器材位置" />
        </n-form-item>
        <n-form-item label="状态" path="status">
          <n-select v-model:value="form.status" :options="statusOptions" placeholder="请选择状态" />
        </n-form-item>
        <n-form-item label="描述" path="description">
          <n-input v-model:value="form.description" type="textarea" :rows="3" placeholder="请输入描述" />
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
import { NTag, NButton, NSpace, useMessage } from 'naive-ui'

const message = useMessage()
const loading = ref(false)
const submitting = ref(false)
const showModal = ref(false)
const formRef = ref(null)
const equipment = ref([])

const pagination = reactive({ pageSize: 10 })

const form = reactive({
  name: '',
  type: '',
  location: '',
  status: 'AVAILABLE',
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入器材名称', trigger: 'blur' }],
  type: [{ required: true, message: '请输入器材类型', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const statusOptions = [
  { label: '可用', value: 'AVAILABLE' },
  { label: '使用中', value: 'IN_USE' },
  { label: '维护中', value: 'MAINTENANCE' },
  { label: '已损坏', value: 'BROKEN' }
]

const columns = [
  { title: '名称', key: 'name' },
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
  { title: '描述', key: 'description', ellipsis: { tooltip: true } },
  {
    title: '操作',
    key: 'actions',
    render: (row) => h(NSpace, null, () => [
      h(NButton, { size: 'small', onClick: () => handleEdit(row) }, () => '编辑'),
      h(NButton, { size: 'small', type: 'error', onClick: () => handleDelete(row.id) }, () => '删除')
    ])
  }
]

onMounted(() => {
  fetchEquipment()
})

async function fetchEquipment() {
  loading.value = true
  equipment.value = []
  loading.value = false
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    submitting.value = true
    message.success('创建成功')
    showModal.value = false
    fetchEquipment()
  } catch (error) {
    message.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

function handleEdit(row) {
  message.info('编辑功能待实现')
}

function handleDelete(id) {
  message.info('删除功能待实现')
}
</script>

<style scoped>
.admin-equipment {
  padding: 0;
}
</style>
