<template>
  <div class="admin-equipment">
    <n-card title="器材管理">
      <!-- 搜索栏 -->
      <n-card embedded style="margin-bottom: 16px;">
        <n-space align="center" wrap justify="space-between">
          <n-space align="center" wrap>
            <n-input v-model:value="searchForm.keyword" placeholder="搜索器材名称" clearable style="width: 200px" @keyup.enter="handleSearch" />
            <n-select v-model:value="searchForm.typeCode" :options="typeOptions" placeholder="选择类型" clearable style="width: 150px" />
            <n-select v-model:value="searchForm.status" :options="statusOptions" placeholder="选择状态" clearable style="width: 150px" />
            <n-button type="primary" @click="handleSearch">搜索</n-button>
            <n-button @click="handleReset">重置</n-button>
          </n-space>
          <n-button type="primary" @click="handleAdd">新增器材</n-button>
        </n-space>
      </n-card>

      <!-- 数据表格 -->
      <n-data-table :columns="columns" :data="equipment" :loading="loading" :pagination="pagination" :row-key="row => row.id" remote />
    </n-card>

    <!-- 新增/编辑弹窗 -->
    <n-modal v-model:show="showModal" preset="card" :title="isEdit ? '编辑器材' : '新增器材'" style="width: 600px">
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="left" label-width="100">
        <n-form-item label="器材名称" path="equipmentName">
          <n-input v-model:value="form.equipmentName" placeholder="请输入器材名称" />
        </n-form-item>
        <n-form-item label="器材编号" path="equipmentNo">
          <n-input v-model:value="form.equipmentNo" placeholder="请输入器材编号（可选）" />
        </n-form-item>
        <n-form-item label="器材类型" path="typeCode">
          <n-select
            v-model:value="form.typeCode"
            :options="typeOptions"
            placeholder="请选择器材类型"
            value-field="value"
            label-field="label"
          />
        </n-form-item>
        <n-form-item label="位置" path="location">
          <n-input v-model:value="form.location" placeholder="请输入器材位置，如：有氧区-A01" />
        </n-form-item>
        <n-form-item label="状态" path="status">
          <n-select
            v-model:value="form.status"
            :options="statusOptions"
            placeholder="请选择状态"
            value-field="value"
            label-field="label"
          />
        </n-form-item>
        <n-form-item label="购买日期" path="purchaseDate">
          <n-date-picker v-model:value="form.purchaseDate" type="date" placeholder="请选择购买日期" style="width: 100%" />
        </n-form-item>
        <n-form-item label="器材图片" path="imageUrl">
          <n-upload :action="uploadUrl" :headers="uploadHeaders" :max="1" accept="image/*" @finish="handleUploadFinish" @remove="handleUploadRemove">
            <n-button>上传图片</n-button>
          </n-upload>
          <n-image v-if="form.imageUrl" :src="form.imageUrl" width="100" style="margin-top: 8px" />
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

    <!-- 详情弹窗 -->
    <n-modal v-model:show="showDetailModal" preset="card" title="器材详情" style="width: 700px">
      <n-descriptions v-if="currentEquipment" :column="2" bordered>
        <n-descriptions-item label="器材名称">{{ currentEquipment.equipmentName }}</n-descriptions-item>
        <n-descriptions-item label="器材编号">{{ currentEquipment.equipmentNo || '无' }}</n-descriptions-item>
        <n-descriptions-item label="器材类型">{{ currentEquipment.typeName || currentEquipment.typeCode }}</n-descriptions-item>
        <n-descriptions-item label="位置">{{ currentEquipment.location }}</n-descriptions-item>
        <n-descriptions-item label="状态">
          <n-tag :type="getStatusType(currentEquipment.status)">{{ getStatusText(currentEquipment.status) }}</n-tag>
        </n-descriptions-item>
        <n-descriptions-item label="购买日期">{{ currentEquipment.purchaseDate || '无' }}</n-descriptions-item>
        <n-descriptions-item label="描述" :span="2">{{ currentEquipment.description || '无' }}</n-descriptions-item>
        <n-descriptions-item label="图片" :span="2">
          <n-image v-if="currentEquipment.imageUrl" :src="currentEquipment.imageUrl" width="200" />
          <span v-else>无图片</span>
        </n-descriptions-item>
      </n-descriptions>

      <n-divider title-placement="left">报修记录</n-divider>
      <n-data-table :columns="repairColumns" :data="currentEquipment?.repairRecords || []" :bordered="false" size="small" />
    </n-modal>
  </div>
</template>

<script setup>
import { ref, h, reactive, onMounted, computed } from 'vue'
import { NTag, NButton, NSpace, useMessage, useDialog, NImage, NPopconfirm } from 'naive-ui'
import { getEquipmentList, createEquipment, updateEquipment, deleteEquipment, getEquipmentTypes, getEquipmentRepairs, getEquipmentDetail } from '@/api/equipment'
import { getToken } from '@/utils/auth'

const message = useMessage()
const dialog = useDialog()
const loading = ref(false)
const submitting = ref(false)
const showModal = ref(false)
const showDetailModal = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const equipment = ref([])
const currentId = ref(null)
const currentEquipment = ref(null)
const typeOptions = ref([])

const uploadUrl = '/api/v1/files/upload/image'
const uploadHeaders = { Authorization: 'Bearer ' + getToken() }

const searchForm = reactive({
  keyword: '',
  typeCode: null,
  status: null
})

const paginationReactive = reactive({
  page: 1,
  pageSize: 5,
  itemCount: 0
})

const pagination = computed(() => ({
  ...paginationReactive,
  onChange: (page) => {
    paginationReactive.page = page
    fetchEquipment()
  }
}))

const form = reactive({
  equipmentName: '',
  equipmentNo: '',
  typeCode: null,
  location: '',
  status: 1,
  purchaseDate: null,
  imageUrl: '',
  description: ''
})

const rules = {
  equipmentName: [{ required: true, message: '请输入器材名称', trigger: 'blur' }],
  typeCode: [{ required: true, message: '请选择器材类型', trigger: 'change', type: 'string' }],
  location: [{ required: true, message: '请输入器材位置', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change', type: 'number' }]
}

const statusOptions = [
  { label: '维修中', value: 0 },
  { label: '正常', value: 1 },
  { label: '已报废', value: 2 }
]

// 自定义校验函数
const validateStatus = (rule, value) => {
  console.log('校验status:', value, typeof value)
  if (value === null || value === undefined) {
    return new Error('请选择状态')
  }
  return true
}

const repairColumns = [
  { title: '报修人', key: 'userName', width: 100 },
  { title: '问题描述', key: 'description', ellipsis: { tooltip: true } },
  { title: '状态', key: 'status', width: 100, render: row => h(NTag, { type: getRepairStatusType(row.status) }, () => getRepairStatusText(row.status)) },
  { title: '报修时间', key: 'createTime', width: 150 }
]

const getStatusType = (status) => {
  const map = { 0: 'error', 1: 'success', 2: 'default' }
  return map[status] || 'default'
}

const getStatusText = (status) => {
  const map = { 0: '维修中', 1: '正常', 2: '已报废' }
  return map[status] || '未知'
}

const getRepairStatusType = (status) => {
  const map = { 0: 'warning', 1: 'processing', 2: 'success', 3: 'default' }
  return map[status] || 'default'
}

const getRepairStatusText = (status) => {
  const map = { 0: '待处理', 1: '处理中', 2: '已完成', 3: '已关闭' }
  return map[status] || '未知'
}

const columns = [
  {
    title: '图片',
    key: 'imageUrl',
    width: 100,
    render: (row) => {
      if (row.imageUrl) {
        return h(NImage, {
          src: row.imageUrl,
          width: 80,
          height: 80,
          style: 'border-radius: 4px; object-fit: cover;',
          fallbackSrc: '/default-equipment.png'
        })
      }
      return h('div', {
        style: 'width: 80px; height: 80px; background: #f0f0f0; border-radius: 4px; display: flex; align-items: center; justify-content: center; color: #999; font-size: 12px;'
      }, '无图片')
    }
  },
  { title: '名称', key: 'equipmentName' },
  { title: '类型', key: 'typeName', render: row => row.typeName || row.typeCode || '-' },
  { title: '位置', key: 'location' },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) => h(NTag, { type: getStatusType(row.status) }, () => getStatusText(row.status))
  },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    render: (row) => h(NSpace, null, () => [
      h(NButton, { size: 'small', onClick: () => handleView(row) }, () => '查看'),
      h(NButton, { size: 'small', type: 'primary', onClick: () => handleEdit(row) }, () => '编辑'),
      h(NPopconfirm, { onPositiveClick: () => handleDelete(row) }, {
        trigger: () => h(NButton, { size: 'small', type: 'error' }, () => '删除'),
        default: () => '确定删除该器材吗？'
      })
    ])
  }
]

onMounted(() => {
  fetchEquipment()
  fetchEquipmentTypes()
})

async function fetchEquipment() {
  loading.value = true
  try {
    const res = await getEquipmentList({
      pageNum: paginationReactive.page,
      pageSize: paginationReactive.pageSize,
      keyword: searchForm.keyword || undefined,
      typeCode: searchForm.typeCode || undefined,
      status: searchForm.status !== null ? searchForm.status : undefined
    })
    equipment.value = res.records || []
    paginationReactive.itemCount = res.total || 0
  } catch (error) {
    message.error('获取器材列表失败')
  } finally {
    loading.value = false
  }
}

async function fetchEquipmentTypes() {
  try {
    const res = await getEquipmentTypes()
    typeOptions.value = (res || []).map(t => ({ label: t.typeName, value: t.typeCode }))
  } catch (error) {
    console.error('获取器材类型失败', error)
  }
}

function handleSearch() {
  paginationReactive.page = 1
  fetchEquipment()
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.typeCode = null
  searchForm.status = null
  paginationReactive.page = 1
  fetchEquipment()
}

function handleAdd() {
  isEdit.value = false
  currentId.value = null
  Object.assign(form, { equipmentName: '', equipmentNo: '', typeCode: null, location: '', status: 1, purchaseDate: null, imageUrl: '', description: '' })
  showModal.value = true
}

function handleEdit(row) {
  isEdit.value = true
  currentId.value = row.id
  Object.assign(form, {
    equipmentName: row.equipmentName,
    equipmentNo: row.equipmentNo || '',
    typeCode: row.typeCode,
    location: row.location || '',
    status: row.status,
    purchaseDate: row.purchaseDate ? new Date(row.purchaseDate).getTime() : null,
    imageUrl: row.imageUrl || '',
    description: row.description || ''
  })
  showModal.value = true
}

async function handleView(row) {
  loading.value = true
  try {
    const res = await getEquipmentDetail(row.id)
    currentEquipment.value = res
    const repairsRes = await getEquipmentRepairs(row.id)
    currentEquipment.value.repairRecords = repairsRes || []
    showDetailModal.value = true
  } catch (error) {
    message.error('获取器材详情失败')
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  console.log('表单数据:', {
    equipmentName: form.equipmentName,
    typeCode: form.typeCode,
    location: form.location,
    status: form.status,
    statusType: typeof form.status
  })

  try {
    const valid = await formRef.value?.validate()
    console.log('表单校验结果:', valid)
    submitting.value = true

    const submitData = {
      equipmentName: form.equipmentName,
      equipmentNo: form.equipmentNo || null,
      typeCode: form.typeCode,
      location: form.location,
      status: Number(form.status),
      purchaseDate: form.purchaseDate ? new Date(form.purchaseDate).toISOString().split('T')[0] : null,
      imageUrl: form.imageUrl || null,
      description: form.description || null
    }

    console.log('提交数据:', submitData)

    if (isEdit.value) {
      await updateEquipment(currentId.value, submitData)
      message.success('更新成功')
    } else {
      await createEquipment(submitData)
      message.success('创建成功')
    }

    showModal.value = false
    fetchEquipment()
  } catch (error) {
    console.error('提交失败:', error)
    if (error.message && error.message.includes('验证')) {
      message.error('请检查表单填写是否正确')
    } else {
      message.error(error.response?.data?.message || error.message || '操作失败')
    }
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  try {
    await deleteEquipment(row.id)
    message.success('删除成功')
    fetchEquipment()
  } catch (error) {
    message.error(error.message || '删除失败')
  }
}

function handleUploadFinish({ event }) {
  try {
    const response = JSON.parse(event.target.response)
    if (response.code === 200) {
      form.imageUrl = response.data.fileUrl
      message.success('图片上传成功')
    } else {
      message.error(response.message || '上传失败')
    }
  } catch (error) {
    message.error('上传响应解析失败')
  }
}

function handleUploadRemove() {
  form.imageUrl = ''
}
</script>

<style scoped>
.admin-equipment {
  padding: 0;
}
</style>
