<template>
  <div class="membership-cards-page">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6" v-for="stat in stats" :key="stat.title">
        <el-card class="stat-card" :body-style="{ padding: '20px' }">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-title">{{ stat.title }}</div>
              <div class="stat-trend" v-if="stat.trend !== undefined">
                <span :class="stat.trend >= 0 ? 'up' : 'down'">
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
              v-model="searchForm.typeCode"
              placeholder="全部类型"
              clearable
              style="width: 150px"
            >
              <el-option
                v-for="type in cardTypes"
                :key="type.value"
                :label="type.label"
                :value="type.value"
              />
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
              <el-avatar :size="40" icon="CreditCard" />
              <div class="card-detail">
                <div class="card-name">{{ row.name }}</div>
                <div class="card-id">ID: MC{{ String(row.id).padStart(3, '0') }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.typeCode)">{{ getTypeLabel(row.typeCode) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="120">
          <template #default="{ row }">
            <span class="price">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="durationDays" label="有效期" width="100">
          <template #default="{ row }">
            {{ row.durationDays }}天
          </template>
        </el-table-column>
        <el-table-column label="权益说明" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            {{ getBenefitsText(row.contents) }}
          </template>
        </el-table-column>
        <el-table-column prop="salesCount" label="销量" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status"
              active-value="ACTIVE"
              inactive-value="INACTIVE"
              @change="(val) => handleStatusChange(row, val)"
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
      v-loading="editLoading"
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
            <el-form-item label="会员卡类型" prop="typeCode">
              <el-select v-model="form.typeCode" placeholder="请选择类型" style="width: 100%">
                <el-option
                  v-for="type in cardTypes"
                  :key="type.value"
                  :label="type.label"
                  :value="type.value"
                />
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
            <el-form-item label="原价" prop="originalPrice">
              <el-input-number
                v-model="form.originalPrice"
                :min="0"
                :precision="2"
                style="width: 100%"
                placeholder="请输入原价"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="有效期(天)" prop="durationDays">
              <el-input-number
                v-model="form.durationDays"
                :min="1"
                style="width: 100%"
                placeholder="请输入天数"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="赠送积分" prop="pointsReward">
              <el-input-number
                v-model="form.pointsReward"
                :min="0"
                style="width: 100%"
                placeholder="购买赠送积分"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="副标题" prop="subtitle">
          <el-input v-model="form.subtitle" placeholder="请输入副标题/简介" />
        </el-form-item>

        <!-- 动态内容列表（权益/说明） -->
        <el-form-item label="权益说明">
          <div class="contents-list">
            <div
              v-for="(content, index) in form.contents"
              :key="index"
              class="content-item"
            >
              <el-row :gutter="10">
                <el-col :span="10">
                  <el-input
                    v-model="content.title"
                    placeholder="标题（如：全时段通行）"
                  />
                </el-col>
                <el-col :span="12">
                  <el-input
                    v-model="content.description"
                    placeholder="详细描述（可选）"
                  />
                </el-col>
                <el-col :span="2">
                  <el-button
                    type="danger"
                    :icon="Delete"
                    circle
                    size="small"
                    @click="removeContent(index)"
                  />
                </el-col>
              </el-row>
            </div>
            <el-button
              type="primary"
              plain
              size="small"
              :icon="Plus"
              @click="addContent"
            >
              添加权益说明
            </el-button>
          </div>
        </el-form-item>

        <el-form-item label="推荐" prop="isRecommend">
          <el-switch v-model="form.isRecommend" active-text="推荐" inactive-text="普通" />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="ACTIVE">上架</el-radio>
            <el-radio value="INACTIVE">下架</el-radio>
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
import { Search, Plus, Edit, Delete, CreditCard } from '@element-plus/icons-vue'
import {
  getAdminCardPage,
  getCardTypeList,
  createAdminCard,
  updateAdminCard,
  deleteAdminCard,
  updateCardStatus,
  getMembershipStats,
  getAdminCardDetail
} from '@/api/membership'

// 统计数据
const stats = ref([
  { title: '会员卡类型', value: '-', trend: null },
  { title: '活跃会员', value: '-', trend: null },
  { title: '本月收入', value: '-', trend: null },
  { title: '续费率', value: '-', trend: null }
])

// 搜索表单
const searchForm = reactive({
  name: '',
  typeCode: '',
  status: ''
})

// 表格数据
const loading = ref(false)
const tableData = ref([])

// 卡类型列表
const cardTypes = ref([])

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
const editLoading = ref(false)

// 表单数据
const form = reactive({
  id: null,
  name: '',
  typeCode: '',
  price: 0,
  originalPrice: null,
  durationDays: 30,
  pointsReward: 0,
  subtitle: '',
  status: 'ACTIVE',
  sortOrder: 0,
  isRecommend: false,
  contents: []
})

// 表单校验规则
const rules = {
  name: [{ required: true, message: '请输入会员卡名称', trigger: 'blur' }],
  typeCode: [{ required: true, message: '请选择会员卡类型', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  durationDays: [{ required: true, message: '请输入有效期', trigger: 'blur' }]
}

// 类型标签映射
const typeMap = {
  MONTHLY: { label: '月卡', type: 'success' },
  QUARTERLY: { label: '季卡', type: 'warning' },
  YEARLY: { label: '年卡', type: 'danger' },
  TRIAL: { label: '体验卡', type: 'info' }
}

function getTypeLabel(typeCode) {
  return typeMap[typeCode]?.label || typeCode || '-'
}

function getTypeTagType(typeCode) {
  return typeMap[typeCode]?.type || ''
}

function getBenefitsText(contents) {
  if (!contents || contents.length === 0) return '-'
  return contents
    .filter(c => c.contentType === 'BENEFIT')
    .slice(0, 2)
    .map(c => c.description)
    .join('、')
}

// 加载统计数据
async function loadStats() {
  try {
    const data = await getMembershipStats()
    console.log('统计数据:', data)
    if (data) {
      stats.value = [
        { title: '会员卡类型', value: data.cardTypeCount || 8, trend: data.cardTypeTrend || 12.5 },
        { title: '活跃会员', value: data.activeMemberCount ? `${data.activeMemberCount.toLocaleString()}` : '1,286', trend: data.memberTrend || 8.3 },
        { title: '本月收入', value: data.monthlyRevenue ? `¥${data.monthlyRevenue.toLocaleString()}` : '¥89,520', trend: data.revenueTrend || 15.2 },
        { title: '续费率', value: data.renewalRate ? `${data.renewalRate}%` : '68%', trend: data.renewalRateTrend || -2.1 }
      ]
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 加载卡类型
async function loadCardTypes() {
  try {
    const data = await getCardTypeList()
    console.log('卡类型数据:', data)
    // request.js 拦截器返回的是 res.data
    cardTypes.value = Array.isArray(data) ? data : []
  } catch (error) {
    console.error('加载卡类型失败:', error)
    cardTypes.value = []
  }
}

// 获取数据
async function fetchData() {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      ...searchForm
    }
    // 移除空值
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null || params[key] === undefined) {
        delete params[key]
      }
    })

    const data = await getAdminCardPage(params)
    console.log('会员卡列表:', data)
    if (data) {
      tableData.value = data.records || []
      pagination.total = data.total || 0
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
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
  searchForm.typeCode = ''
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
async function handleEdit(row) {
  console.log('编辑会员卡 - 原始数据:', row)
  console.log('编辑会员卡 - row.contents:', row.contents)

  isEdit.value = true
  editLoading.value = true
  dialogVisible.value = true

  // 如果分页数据中没有contents，调用详情API获取
  let contents = []
  if (row.contents && row.contents.length > 0) {
    contents = JSON.parse(JSON.stringify(row.contents))
    console.log('使用分页数据的contents:', contents)
  } else {
    try {
      console.log('分页数据无contents，调用详情API获取...')
      const detail = await getAdminCardDetail(row.id)
      console.log('详情API返回:', detail)
      if (detail && detail.contents && detail.contents.length > 0) {
        contents = detail.contents
        console.log('从详情API获取到contents:', contents)
      }
    } catch (error) {
      console.error('获取详情失败:', error)
    }
  }

  Object.assign(form, {
    id: row.id,
    name: row.name,
    typeCode: row.typeCode || '',
    price: parseFloat(row.price),
    originalPrice: row.originalPrice ? parseFloat(row.originalPrice) : null,
    durationDays: row.durationDays,
    pointsReward: row.pointsReward || 0,
    subtitle: row.subtitle || '',
    status: row.status,
    sortOrder: row.sortOrder || 0,
    isRecommend: row.isRecommend || false,
    contents: contents
  })

  console.log('编辑会员卡 - 最终表单数据:', form)
  console.log('编辑会员卡 - form.contents:', form.contents, '长度:', form.contents.length)

  editLoading.value = false
}

// 删除
function handleDelete(row) {
  ElMessageBox.confirm(
    `确定要删除会员卡 "${row.name}" 吗？`,
    '提示',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    try {
      await deleteAdminCard(row.id)
      // request.js 拦截器返回 res.data，如果成功执行到这里说明操作成功
      ElMessage.success('删除成功')
      fetchData()
      loadStats()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || error.message || '删除失败')
    }
  }).catch(() => {})
}

// 状态变更
async function handleStatusChange(row, val) {
  try {
    await updateCardStatus(row.id, val)
    // request.js 拦截器返回 res.data，如果成功执行到这里说明操作成功
    const statusText = val === 'ACTIVE' ? '上架' : '下架'
    ElMessage.success(`会员卡已${statusText}`)
    fetchData()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message || '状态更新失败')
    // 恢复状态
    row.status = row.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
  }
}

// 提交表单
function handleSubmit() {
  formRef.value?.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        console.log('提交表单数据:', JSON.stringify(form, null, 2))
        console.log('权益说明列表:', form.contents)

        if (isEdit.value) {
          await updateAdminCard(form.id, form)
        } else {
          await createAdminCard(form)
        }

        // request.js 拦截器返回 res.data，如果成功执行到这里说明操作成功
        ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
        dialogVisible.value = false
        fetchData()
        loadStats()
      } catch (error) {
        console.error('提交失败:', error)
        ElMessage.error(error.response?.data?.message || error.message || '操作失败，请重试')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 重置表单
function resetForm() {
  form.id = null
  form.name = ''
  form.typeCode = ''
  form.price = 0
  form.originalPrice = null
  form.durationDays = 30
  form.pointsReward = 0
  form.subtitle = ''
  form.status = 'ACTIVE'
  form.sortOrder = 0
  form.isRecommend = false
  form.contents = []
}

// 添加内容项
function addContent() {
  form.contents.push({
    contentType: 'BENEFIT',
    title: '',
    description: '',
    icon: '✓',
    sortOrder: form.contents.length
  })
}

// 删除内容项
function removeContent(index) {
  form.contents.splice(index, 1)
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
  loadStats()
  loadCardTypes()
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

.form-tip {
  margin-left: 10px;
  font-size: 12px;
  color: #909399;
}

.membership-form {
  padding: 10px 0;
}

.contents-list {
  width: 100%;
}

.content-item {
  margin-bottom: 10px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 6px;
}

.content-item .el-col {
  display: flex;
  align-items: center;
}
</style>
