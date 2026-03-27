<template>
  <div class="membership-cards-page">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6" v-for="stat in stats" :key="stat.title">
        <el-card class="stat-card" :body-style="{ padding: '20px' }">
          <div class="stat-content">
            <div class="stat-icon" :style="{ background: stat.color }">
              <el-icon :size="24"><component :is="stat.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-title">{{ stat.title }}</div>
              <div class="stat-trend" v-if="stat.trend">
                <span :class="stat.trend > 0 ? 'up' : 'down'">
                  {{ stat.trend > 0 ? '↑' : '↓' }} {{ Math.abs(stat.trend) }}%
                </span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索和操作区域 -->
    <el-card class="search-card" :body-style="{ padding: '20px' }">
      <el-row :gutter="20" align="middle">
        <el-col :span="18">
          <el-space>
            <el-input
              v-model="searchForm.name"
              placeholder="会员卡名称"
              clearable
              style="width: 200px"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-select
              v-model="searchForm.type"
              placeholder="全部类型"
              clearable
              style="width: 150px"
            >
              <el-option label="月卡" value="MONTH" />
              <el-option label="季卡" value="QUARTER" />
              <el-option label="年卡" value="YEAR" />
              <el-option label="次卡" value="TIMES" />
            </el-select>
            <el-select
              v-model="searchForm.status"
              placeholder="全部状态"
              clearable
              style="width: 150px"
            >
              <el-option label="上架中" value="ACTIVE" />
              <el-option label="已下架" value="INACTIVE" />
            </el-select>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>搜索
            </el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-space>
        </el-col>
        <el-col :span="6" style="text-align: right">
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增会员卡
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card">
      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column type="index" width="50" />
        <el-table-column label="会员卡信息" min-width="200">
          <template #default="{ row }">
            <div class="card-info">
              <el-avatar :size="40" :src="row.icon" :icon="CreditCard" />
              <div class="card-detail">
                <div class="card-name">{{ row.name }}</div>
                <div class="card-id">ID: {{ row.id }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">{{ getTypeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="120">
          <template #default="{ row }">
            <span class="price">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="validityDays" label="有效期" width="100">
          <template #default="{ row }">
            {{ row.validityDays }}天
          </template>
        </el-table-column>
        <el-table-column prop="benefits" label="权益说明" min-width="200" show-overflow-tooltip />
        <el-table-column prop="salesCount" label="销量" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="'ACTIVE'"
              :inactive-value="'INACTIVE'"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑会员卡' : '新增会员卡'"
      width="700px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="membership-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="会员卡名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入会员卡名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="会员卡类型" prop="type">
              <el-select v-model="form.type" placeholder="请选择类型" style="width: 100%">
                <el-option label="月卡" value="MONTH" />
                <el-option label="季卡" value="QUARTER" />
                <el-option label="年卡" value="YEAR" />
                <el-option label="次卡" value="TIMES" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="价格" prop="price">
              <el-input-number
                v-model="form.price"
                :min="0"
                :precision="2"
                style="width: 100%"
                placeholder="请输入价格"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="有效期" prop="validityDays">
              <el-input-number
                v-model="form.validityDays"
                :min="1"
                style="width: 100%"
                placeholder="请输入天数"
              >
                <template #append>天</template>
              </el-input-number>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="权益说明" prop="benefits">
          <el-input
            v-model="form.benefits"
            type="textarea"
            :rows="3"
            placeholder="请输入权益说明，多个权益用逗号分隔"
          />
        </el-form-item>

        <el-form-item label="会员卡图标" prop="icon">
          <el-upload
            class="avatar-uploader"
            action="/api/v1/files/upload"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
            :before-upload="beforeUpload"
          >
            <img v-if="form.icon" :src="form.icon" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">建议尺寸 200x200，支持 JPG、PNG 格式</div>
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="ACTIVE">上架</el-radio>
            <el-radio label="INACTIVE">下架</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" style="width: 200px" />
          <span class="form-tip">数字越小排序越靠前</span>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, Plus, Edit, Delete, CreditCard, TrendCharts, User, Money
} from '@element-plus/icons-vue'

// 统计数据
const stats = ref([
  { title: '会员卡类型', value: 8, icon: 'CreditCard', color: '#1890ff', trend: 12.5 },
  { title: '活跃会员', value: '1,286', icon: 'User', color: '#52c41a', trend: 8.3 },
  { title: '本月收入', value: '¥89,520', icon: 'Money', color: '#faad14', trend: 15.2 },
  { title: '续费率', value: '68%', icon: 'TrendCharts', color: '#722ed1', trend: -2.1 }
])

// 搜索表单
const searchForm = reactive({
  name: '',
  type: '',
  status: ''
})

// 表格数据
const loading = ref(false)
const tableData = ref([])

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// 弹窗相关
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const submitting = ref(false)

// 会员卡图标
const cardTypeIcons = {
  YEAR: `<svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>`,
  QUARTER: `<svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>`,
  MONTH: `<svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/></svg>`,
  TIMES: `<svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2v20M2 12h20"/></svg>`
}

// 表单数据
const form = reactive({
  id: null,
  name: '',
  type: '',
  price: 0,
  validityDays: 30,
  benefits: '',
  icon: '',
  status: 'ACTIVE',
  sortOrder: 0
})

// 表单校验规则
const rules = {
  name: [{ required: true, message: '请输入会员卡名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择会员卡类型', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  validityDays: [{ required: true, message: '请输入有效期', trigger: 'blur' }],
  benefits: [{ required: true, message: '请输入权益说明', trigger: 'blur' }]
}

// 类型标签映射
const typeMap = {
  MONTH: { label: '月卡', type: 'success' },
  QUARTER: { label: '季卡', type: 'warning' },
  YEAR: { label: '年卡', type: 'danger' },
  TIMES: { label: '次卡', type: 'info' }
}

function getTypeLabel(type) {
  return typeMap[type]?.label || type
}

function getTypeTagType(type) {
  return typeMap[type]?.type || ''
}

// 获取数据
async function fetchData() {
  loading.value = true
  try {
    // 模拟数据，实际应调用API
    tableData.value = [
      {
        id: 'MC001',
        name: '至尊年卡',
        type: 'YEAR',
        price: 3999,
        validityDays: 365,
        benefits: '全场通用、私教8折、专属储物柜、免费停车',
        icon: cardTypeIcons.YEAR,
        salesCount: 256,
        status: 'ACTIVE',
        sortOrder: 1
      },
      {
        id: 'MC002',
        name: '金卡季卡',
        type: 'QUARTER',
        price: 1299,
        validityDays: 90,
        benefits: '全场通用、团课免费、专属储物柜',
        icon: cardTypeIcons.QUARTER,
        salesCount: 189,
        status: 'ACTIVE',
        sortOrder: 2
      },
      {
        id: 'MC003',
        name: '银卡月卡',
        type: 'MONTH',
        price: 499,
        validityDays: 30,
        benefits: '器械区、有氧区通用',
        icon: cardTypeIcons.MONTH,
        salesCount: 423,
        status: 'ACTIVE',
        sortOrder: 3
      },
      {
        id: 'MC004',
        name: '次卡 30次',
        type: 'TIMES',
        price: 899,
        validityDays: 180,
        benefits: '30次入场，不限时段',
        icon: cardTypeIcons.TIMES,
        salesCount: 67,
        status: 'INACTIVE',
        sortOrder: 4
      }
    ]
    pagination.total = 4
  } finally {
    loading.value = false
  }
}

// 搜索
function handleSearch() {
  pagination.page = 1
  fetchData()
}

// 重置
function handleReset() {
  searchForm.name = ''
  searchForm.type = ''
  searchForm.status = ''
  handleSearch()
}

// 新增
function handleAdd() {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑
function handleEdit(row) {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

// 删除
function handleDelete(row) {
  ElMessageBox.confirm(
    `确定要删除会员卡 "${row.name}" 吗？`,
    '提示',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  ).then(() => {
    ElMessage.success('删除成功')
    fetchData()
  })
}

// 状态变更
function handleStatusChange(row) {
  const statusText = row.status === 'ACTIVE' ? '上架' : '下架'
  ElMessage.success(`会员卡已${statusText}`)
}

// 提交表单
function handleSubmit() {
  formRef.value?.validate((valid) => {
    if (valid) {
      submitting.value = true
      setTimeout(() => {
        ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
        dialogVisible.value = false
        fetchData()
        submitting.value = false
      }, 500)
    }
  })
}

// 重置表单
function resetForm() {
  form.id = null
  form.name = ''
  form.type = ''
  form.price = 0
  form.validityDays = 30
  form.benefits = ''
  form.icon = ''
  form.status = 'ACTIVE'
  form.sortOrder = 0
}

// 上传相关
function handleUploadSuccess(res) {
  form.icon = res.data?.url || res.url
  ElMessage.success('上传成功')
}

function beforeUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) {
    ElMessage.error('请上传图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB')
    return false
  }
  return true
}

// 分页
function handleSizeChange(val) {
  pagination.pageSize = val
  fetchData()
}

function handlePageChange(val) {
  pagination.page = val
  fetchData()
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.membership-cards-page {
  padding: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
}

.stat-title {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.stat-trend {
  margin-top: 4px;
  font-size: 12px;
}

.stat-trend .up {
  color: #67c23a;
}

.stat-trend .down {
  color: #f56c6c;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  min-height: 500px;
}

.card-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.card-detail {
  flex: 1;
}

.card-name {
  font-weight: 500;
  color: #303133;
}

.card-id {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.price {
  font-weight: 600;
  color: #f56c6c;
  font-size: 16px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 上传样式 */
.avatar-uploader {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
  width: 120px;
  height: 120px;
}

.avatar-uploader:hover {
  border-color: var(--el-color-primary);
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: cover;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.form-tip {
  margin-left: 10px;
  font-size: 12px;
  color: #909399;
}

.membership-form {
  padding: 10px 0;
}
</style>
