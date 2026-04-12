<template>
  <div class="admin-equipment">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>器材管理</span>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-card shadow="never" style="margin-bottom: 16px;">
        <el-row justify="space-between" align="middle">
          <el-col :span="20">
            <el-space wrap>
              <el-input v-model="searchForm.keyword" placeholder="搜索器材名称" clearable style="width: 200px" @keyup.enter="handleSearch" />
              <el-select v-model="searchForm.typeCode" :options="typeOptions" placeholder="选择类型" clearable style="width: 150px" />
              <el-select v-model="searchForm.status" :options="statusOptions" placeholder="选择状态" clearable style="width: 150px" />
              <el-button type="primary" @click="handleSearch">搜索</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-space>
          </el-col>
          <el-col :span="4" style="text-align: right;">
            <el-button type="primary" @click="handleAdd">新增器材</el-button>
          </el-col>
        </el-row>
      </el-card>

      <!-- 数据表格 -->
      <el-table
        :data="equipment"
        v-loading="loading"
        :row-key="row => row.id"
        stripe
        style="width: 100%"
      >
        <el-table-column label="图片" width="100" align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.imageUrl"
              :src="row.imageUrl"
              style="width: 80px; height: 80px; border-radius: 4px; object-fit: cover;"
              :preview-src-list="[row.imageUrl]"
            />
            <div v-else class="no-image">无图片</div>
          </template>
        </el-table-column>
        <el-table-column prop="equipmentName" label="名称" />
        <el-table-column label="类型">
          <template #default="{ row }">
            {{ row.typeName || row.typeCode || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="location" label="位置" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-space>
              <el-button size="small" @click="handleView(row)">查看</el-button>
              <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
              <el-popconfirm title="确定删除该器材吗？" @confirm="handleDelete(row)">
                <template #reference>
                  <el-button size="small" type="danger">删除</el-button>
                </template>
              </el-popconfirm>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="paginationReactive.page"
          v-model:page-size="paginationReactive.pageSize"
          :total="paginationReactive.itemCount"
          :page-sizes="[5, 10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="showModal" :title="isEdit ? '编辑器材' : '新增器材'" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="器材名称" prop="equipmentName">
          <el-input v-model="form.equipmentName" placeholder="请输入器材名称" />
        </el-form-item>
        <el-form-item label="器材编号" prop="equipmentNo">
          <el-input v-model="form.equipmentNo" placeholder="请输入器材编号（可选）" />
        </el-form-item>
        <el-form-item label="器材类型" prop="typeCode">
          <el-select
            v-model="form.typeCode"
            :options="typeOptions"
            placeholder="请选择器材类型"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="form.location" placeholder="请输入器材位置，如：有氧区-A01" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select
            v-model="form.status"
            :options="statusOptions"
            placeholder="请选择状态"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="购买日期" prop="purchaseDate">
          <el-date-picker v-model="form.purchaseDate" type="date" placeholder="请选择购买日期" style="width: 100%" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="器材图片" prop="imageUrl">
          <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            :limit="1"
            accept="image/*"
            :on-success="handleUploadFinish"
            :on-remove="handleUploadRemove"
            :file-list="fileList"
            list-type="picture-card"
            :class="{ 'hide-upload': fileList.length > 0 }"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入描述，支持ai润色..."
            maxlength="100"
            :class="{ 'loading-textarea': polishing }"
          />
          
          <div class="polish-button-group">
            <el-button 
              v-if="!hasPolished && !polishing"
              type="primary" 
              @click="handlePolish"
            >
              <el-icon><MagicStick /></el-icon>
              AI润色
            </el-button>
            
            <el-button 
              v-if="polishing"
              type="primary" 
              loading
            >
              AI润色中...
            </el-button>
            <el-button 
              v-if="polishing"
              @click="handleCancelPolish"
            >
              取消
            </el-button>
            
            <el-button 
              v-if="hasPolished && !polishing"
              @click="handleRestore"
            >
              <el-icon><RefreshLeft /></el-icon>
              恢复原文
            </el-button>
          </div>
        </el-form-item>
        <el-form-item>
          <el-space>
            <el-button type="primary" :loading="submitting" @click="handleSubmit">提交</el-button>
            <el-button @click="showModal = false">取消</el-button>
          </el-space>
        </el-form-item>
      </el-form>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="showDetailModal" title="器材详情" width="700px" destroy-on-close>
      <el-descriptions v-if="currentEquipment" :column="2" border>
        <el-descriptions-item label="器材名称">{{ currentEquipment.equipmentName }}</el-descriptions-item>
        <el-descriptions-item label="器材编号">{{ currentEquipment.equipmentNo || '无' }}</el-descriptions-item>
        <el-descriptions-item label="器材类型">{{ currentEquipment.typeName || currentEquipment.typeCode }}</el-descriptions-item>
        <el-descriptions-item label="位置">{{ currentEquipment.location }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentEquipment.status)">{{ getStatusText(currentEquipment.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="购买日期">{{ currentEquipment.purchaseDate || '无' }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ currentEquipment.description || '无' }}</el-descriptions-item>
        <el-descriptions-item label="图片" :span="2">
          <el-image v-if="currentEquipment.imageUrl" :src="currentEquipment.imageUrl" style="width: 200px; border-radius: 4px;" />
          <span v-else>无图片</span>
        </el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">报修记录</el-divider>
      <el-table :data="currentEquipment?.repairRecords || []" :border="false" size="small">
        <el-table-column prop="equipmentNo" label="器械编号" width="120">
          <template #default="{ row }">
            {{ row.equipmentNo || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="报修人" width="100" />
        <el-table-column prop="description" label="问题描述" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getRepairStatusType(row.status)">{{ getRepairStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="报修时间" width="150" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, h, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getEquipmentList, createEquipment, updateEquipment, deleteEquipment, getEquipmentTypes, getEquipmentRepairs, getEquipmentDetail } from '@/api/equipment'
import { getToken } from '@/utils/auth'
import { polishDescription } from '@/api/ai'
import { MagicStick, RefreshLeft } from '@element-plus/icons-vue'

const message = ElMessage
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
const fileList = ref([])

const polishing = ref(false)
const originalText = ref('')
const hasPolished = ref(false)

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
  typeCode: [{ required: true, message: '请选择器材类型', trigger: 'change' }],
  location: [{ required: true, message: '请输入器材位置', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change', type: 'number' }]
}

const statusOptions = [
  { label: '维修中', value: 0 },
  { label: '正常', value: 1 },
  { label: '已报废', value: 2 }
]

const repairColumns = [
  { prop: 'userName', label: '报修人', width: 100 },
  { prop: 'description', label: '问题描述', showOverflowTooltip: true },
  { label: '状态', width: 100 },
  { prop: 'createTime', label: '报修时间', width: 150 }
]

const getStatusType = (status) => {
  const map = { 0: 'danger', 1: 'success', 2: 'info' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { 0: '维修中', 1: '正常', 2: '已报废' }
  return map[status] || '未知'
}

const getRepairStatusType = (status) => {
  const map = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'info' }
  return map[status] || 'info'
}

const getRepairStatusText = (status) => {
  const map = { 0: '待处理', 1: '处理中', 2: '已完成', 3: '已关闭' }
  return map[status] || '未知'
}

function handleSizeChange(size) {
  paginationReactive.pageSize = size
  fetchEquipment()
}

function handlePageChange(page) {
  paginationReactive.page = page
  fetchEquipment()
}

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
  fileList.value = []
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
    purchaseDate: row.purchaseDate,
    imageUrl: row.imageUrl || '',
    description: row.description || ''
  })
  if (row.imageUrl) {
    fileList.value = [{
      name: '器材图片',
      url: row.imageUrl
    }]
  } else {
    fileList.value = []
  }
  // 重置润色状态
  hasPolished.value = false
  originalText.value = ''
  polishing.value = false
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
  try {
    await formRef.value?.validate()
    submitting.value = true

    const submitData = {
      equipmentName: form.equipmentName,
      equipmentNo: form.equipmentNo || null,
      typeCode: form.typeCode,
      location: form.location,
      status: Number(form.status),
      purchaseDate: form.purchaseDate,
      imageUrl: form.imageUrl || null,
      description: form.description || null
    }

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

function handleUploadFinish(response) {
  if (response.code === 200) {
    form.imageUrl = response.data.fileUrl
    message.success('图片上传成功')
  } else {
    message.error(response.message || '上传失败')
  }
}

function handleUploadRemove() {
  form.imageUrl = ''
  fileList.value = []
}

async function handlePolish() {
  const textLength = form.description.trim().length
  
  if (textLength === 0) {
    message.warning('请先输入描述内容')
    return
  }
  
  if (textLength < 3) {
    message.warning('文本内容至少需要3个字')
    return
  }
  
  if (textLength > 100) {
    message.warning('文本长度不能超过100字')
    return
  }
  
  originalText.value = form.description
  polishing.value = true
  hasPolished.value = false
  
  try {
    const response = await polishDescription({ text: form.description })
    form.description = response.polishedText
    hasPolished.value = true
    message.success('润色成功')
  } catch (error) {
    message.error(error.message || '润色失败，请重试')
  } finally {
    polishing.value = false
  }
}

function handleCancelPolish() {
  polishing.value = false
  message.info('已取消润色')
}

function handleRestore() {
  form.description = originalText.value
  hasPolished.value = false
  message.success('已恢复原始文本')
}
</script>

<style scoped>
.admin-equipment {
  padding: 0;
}

.card-header {
  font-weight: bold;
  font-size: 16px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.no-image {
  width: 80px;
  height: 80px;
  background: #f0f0f0;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 12px;
  margin: 0 auto;
}

/* 上传成功后隐藏上传按钮 */
:deep(.hide-upload .el-upload--picture-card) {
  display: none;
}

.polish-button-group {
  margin-top: 8px;
  display: flex;
  gap: 8px;
}

.loading-textarea textarea {
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
</style>
