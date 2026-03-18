<template>
  <div class="admin-equipment">
    <n-card title="器材管理">
      <template #header-extra>
        <n-button type="primary" @click="handleAdd">新增器材</n-button>
      </template>
      <n-data-table :columns="columns" :data="equipment" :loading="loading" :pagination="pagination" :row-key="row => row.id" />
    </n-card>

    <n-modal v-model:show="showModal" preset="card" :title="isEdit ? '编辑器材' : '新增器材'" style="width: 500px">
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="left" label-width="80">
        <n-form-item label="名称" path="equipmentName">
          <n-input v-model:value="form.equipmentName" placeholder="请输入器材名称" />
        </n-form-item>
        <n-form-item label="类型" path="equipmentType">
          <n-input v-model:value="form.equipmentType" placeholder="请输入器材类型" />
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
          <n-space>
            <n-button type="primary" :loading="submitting" @click="handleSubmit">提交</n-button>
            <n-button @click="showModal = false">取消</n-button>
          </n-space>
        </n-form-item>
      </n-form>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, h, reactive, onMounted } from 'vue'
import { NTag, NButton, NSpace, useMessage, useDialog } from 'naive-ui'
import { getEquipmentList, createEquipment, updateEquipment, deleteEquipment } from '@/api/equipment'

const message = useMessage()
const dialog = useDialog()
const loading = ref(false)
const submitting = ref(false)
const showModal = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const equipment = ref([])
const currentId = ref(null)

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  onChange: (page) => {
    pagination.page = page
    fetchEquipment()
  }
})

const form = reactive({
  equipmentName: '',
  equipmentType: '',
  location: '',
  status: 'AVAILABLE',
  description: ''
})

const rules = {
  equipmentName: [{ required: true, message: '请输入器材名称', trigger: 'blur' }],
  equipmentType: [{ required: true, message: '请输入器材类型', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const statusOptions = [
  { label: '可用', value: 'AVAILABLE' },
  { label: '使用中', value: 'IN_USE' },
  { label: '维护中', value: 'MAINTENANCE' },
  { label: '已损坏', value: 'BROKEN' }
]

const columns = [
  { title: '名称', key: 'equipmentName' },
  { title: '类型', key: 'equipmentType' },
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
      h(NButton, { size: 'small', type: 'error', onClick: () => handleDelete(row) }, () => '删除')
    ])
  }
]

onMounted(() => {
  fetchEquipment()
})

async function fetchEquipment() {
  loading.value = true
  try {
    const res = await getEquipmentList({ pageNum: pagination.page, pageSize: pagination.pageSize })
    equipment.value = res.records || []
    pagination.itemCount = res.total || 0
  } catch (error) {
    message.error('获取器材列表失败')
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  isEdit.value = false
  currentId.value = null
  Object.assign(form, { equipmentName: '', equipmentType: '', location: '', status: 'AVAILABLE', description: '' })
  showModal.value = true
}

function handleEdit(row) {
  isEdit.value = true
  currentId.value = row.id
  Object.assign(form, {
    equipmentName: row.equipmentName,
    equipmentType: row.equipmentType,
    location: row.location || '',
    status: row.status,
    description: row.description || ''
  })
  showModal.value = true
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    submitting.value = true
    
    if (isEdit.value) {
      await updateEquipment(currentId.value, form)
      message.success('更新成功')
    } else {
      await createEquipment(form)
      message.success('创建成功')
    }
    
    showModal.value = false
    fetchEquipment()
  } catch (error) {
    message.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

function handleDelete(row) {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除器材 "${row.equipmentName}" 吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteEquipment(row.id)
        message.success('删除成功')
        fetchEquipment()
      } catch (error) {
        message.error('删除失败')
      }
    }
  })
}
</script>

<style scoped>
.admin-equipment {
  padding: 0;
}
</style>
