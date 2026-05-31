<template>
  <div class="coach-package-page">
    <el-row :gutter="20" class="stats-row">
      <el-col :span="8" v-for="stat in stats" :key="stat.title">
        <el-card class="stat-card" :body-style="{ padding: '20px' }">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-title">{{ stat.title }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="search-card" :body-style="{ padding: '20px' }">
      <el-row :gutter="20" align="middle">
        <el-col :span="18">
          <el-space>
            <el-input
              v-model="searchForm.keyword"
              placeholder="套餐名称"
              clearable
              style="width: 200px"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-select
              v-model="searchForm.status"
              placeholder="全部状态"
              clearable
              style="width: 150px"
            >
              <el-option label="上架中" value="ACTIVE" />
              <el-option label="已下架" value="INACTIVE" />
            </el-select>
            <el-select
              v-model="searchForm.coachId"
              placeholder="全部教练"
              clearable
              filterable
              style="width: 150px"
            >
              <el-option
                v-for="coach in coachList"
                :key="coach.id"
                :label="coach.nickname || coach.username"
                :value="coach.id"
              />
            </el-select>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>搜索
            </el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-space>
        </el-col>
        <el-col :span="6" style="text-align: right">
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增套餐
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column type="index" width="50" />
        <el-table-column label="套餐信息" min-width="240">
          <template #default="{ row }">
            <div class="package-info">
              <el-image
                v-if="row.coverImage"
                :src="row.coverImage"
                :preview-src-list="[row.coverImage]"
                fit="cover"
                class="package-image"
              >
                <template #error>
                  <div class="image-placeholder">
                    <el-icon :size="24"><Goods /></el-icon>
                  </div>
                </template>
              </el-image>
              <div class="image-placeholder" v-else>
                <el-icon :size="24"><Goods /></el-icon>
              </div>
              <div class="package-detail">
                <div class="package-name">{{ row.name }}</div>
                <div class="package-code" v-if="row.packageCode">编号: {{ row.packageCode }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="所属教练" min-width="120">
          <template #default="{ row }">
            <span>{{ row.coachName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="原价" width="120">
          <template #default="{ row }">
            <span class="price">¥{{ row.originalPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column label="课时数" width="80" prop="totalSessions" />
        <el-table-column label="有效期(天)" width="100" prop="validityDays" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
              {{ row.status === 'ACTIVE' ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="排序" width="60" prop="sortOrder" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button
              v-if="row.status === 'ACTIVE'"
              type="warning" link
              @click="handleStatusChange(row, 'INACTIVE')"
            >下架</el-button>
            <el-button
              v-else
              type="success" link
              @click="handleStatusChange(row, 'ACTIVE')"
            >上架</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="showModal"
      :title="isEdit ? '编辑套餐' : '新增套餐'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="套餐名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入套餐名称" />
        </el-form-item>
        <el-form-item label="所属教练" prop="coachId">
          <el-select
            v-model="form.coachId"
            placeholder="请选择教练"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="coach in coachList"
              :key="coach.id"
              :label="coach.nickname || coach.username"
              :value="coach.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="套餐编码" prop="packageCode">
          <el-input v-model="form.packageCode" placeholder="请输入套餐编码" />
        </el-form-item>
        <el-form-item label="封面图片" prop="coverImage">
          <el-input v-model="form.coverImage" placeholder="请输入封面图片URL" />
        </el-form-item>
        <el-form-item label="原价" prop="originalPrice">
          <el-input-number v-model="form.originalPrice" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="课时数" prop="totalSessions">
          <el-input-number v-model="form.totalSessions" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="有效期(天)" prop="validityDays">
          <el-input-number v-model="form.validityDays" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="ACTIVE">上架</el-radio>
            <el-radio value="INACTIVE">下架</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入套餐描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showModal = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Goods } from '@element-plus/icons-vue'
import {
  getAdminCoachPackages,
  createAdminCoachPackage,
  updateAdminCoachPackage,
  updateAdminCoachPackageStatus,
  deleteAdminCoachPackage
} from '@/api/admin/coachPackage'
import { getCoachList } from '@/api/coach'

const loading = ref(false)
const tableData = ref([])
const showModal = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const currentId = ref(null)
const formRef = ref(null)
const coachList = ref([])

const pagination = reactive({ page: 1, pageSize: 10, total: 0 })
const searchForm = reactive({ keyword: '', status: '', coachId: '' })

const stats = computed(() => [
  { title: '套餐总数', value: pagination.total },
  { title: '上架数', value: tableData.value.filter(i => i.status === 'ACTIVE').length },
  { title: '下架数', value: tableData.value.filter(i => i.status === 'INACTIVE').length }
])

const form = reactive({
  name: '',
  coachId: null,
  packageCode: '',
  coverImage: '',
  originalPrice: null,
  totalSessions: null,
  validityDays: null,
  status: 'ACTIVE',
  sortOrder: 0,
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入套餐名称', trigger: 'blur' }],
  coachId: [{ required: true, message: '请选择教练', trigger: 'change' }],
  originalPrice: [{ required: true, message: '请输入原价', trigger: 'blur' }]
}

function resetForm() {
  form.name = ''
  form.coachId = null
  form.packageCode = ''
  form.coverImage = ''
  form.originalPrice = null
  form.totalSessions = null
  form.validityDays = null
  form.status = 'ACTIVE'
  form.sortOrder = 0
  form.description = ''
}

function fetchData() {
  loading.value = true
  const params = { page: pagination.page, pageSize: pagination.pageSize }
  if (searchForm.keyword) params.keyword = searchForm.keyword
  if (searchForm.status) params.status = searchForm.status
  if (searchForm.coachId) params.coachId = searchForm.coachId

  getAdminCoachPackages(params).then(data => {
    tableData.value = data.records || []
    pagination.total = data.total || 0
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

function fetchCoachList() {
  getCoachList({ page: 1, pageSize: 200 }).then(data => {
    coachList.value = data.records || []
  }).catch(() => {})
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.status = ''
  searchForm.coachId = ''
  handleSearch()
}

function handlePageChange() { fetchData() }
function handleSizeChange() { pagination.page = 1; fetchData() }

function handleAdd() {
  isEdit.value = false
  currentId.value = null
  resetForm()
  showModal.value = true
}

function handleEdit(row) {
  isEdit.value = true
  currentId.value = row.id
  form.name = row.name
  form.coachId = row.coachId
  form.packageCode = row.packageCode || ''
  form.coverImage = row.coverImage || ''
  form.originalPrice = row.originalPrice
  form.totalSessions = row.totalSessions
  form.validityDays = row.validityDays
  form.status = row.status
  form.sortOrder = row.sortOrder || 0
  form.description = row.description || ''
  showModal.value = true
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定删除套餐"${row.name}"吗？`, '确认删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deleteAdminCoachPackage(row.id).then(() => {
      ElMessage.success('删除成功')
      fetchData()
    }).catch(() => {
      ElMessage.error('删除失败')
    })
  }).catch(() => {})
}

function handleStatusChange(row, newStatus) {
  updateAdminCoachPackageStatus(row.id, newStatus).then(() => {
    ElMessage.success('状态更新成功')
    fetchData()
  }).catch(() => {
    ElMessage.error('状态更新失败')
  })
}

function handleSubmit() {
  formRef.value?.validate((valid) => {
    if (!valid) return
    submitting.value = true
    const apiFn = isEdit.value
      ? updateAdminCoachPackage(currentId.value, { ...form })
      : createAdminCoachPackage(form.coachId, { ...form })

    apiFn.then(() => {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      showModal.value = false
      fetchData()
      submitting.value = false
    }).catch(() => {
      ElMessage.error('操作失败')
      submitting.value = false
    })
  })
}

onMounted(() => {
  fetchCoachList()
  fetchData()
})
</script>

<style scoped>
.coach-package-page { padding: 0; }

.stats-row { margin-bottom: 20px; }

.stat-card { border-left: 4px solid #1890ff; }

.stat-content { display: flex; align-items: center; }

.stat-info { flex: 1; }

.stat-value { font-size: 24px; font-weight: 700; color: #1890ff; }

.stat-title { font-size: 14px; color: #999; margin-top: 4px; }

.search-card { margin-bottom: 20px; }

.table-card { }

.package-info { display: flex; align-items: center; gap: 12px; }

.package-image { width: 48px; height: 48px; border-radius: 6px; }

.image-placeholder {
  width: 48px; height: 48px; border-radius: 6px;
  background: #f5f5f5; display: flex; align-items: center; justify-content: center;
  color: #ccc;
}

.package-detail { flex: 1; }

.package-name { font-size: 14px; font-weight: 600; }

.package-code { font-size: 12px; color: #999; }

.price { color: #e74c3c; font-weight: 600; }

.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 20px; }
</style>