<template>
  <div class="products-page">
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
              v-model="searchForm.keyword"
              placeholder="商品名称/编号"
              clearable
              style="width: 200px"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-select
              v-model="searchForm.category"
              placeholder="全部分类"
              clearable
              style="width: 150px"
            >
              <el-option label="运动装备" value="EQUIPMENT" />
              <el-option label="营养补剂" value="SUPPLEMENT" />
              <el-option label="课程优惠" value="COURSE" />
              <el-option label="其他" value="OTHER" />
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
            <el-icon><Plus /></el-icon>新增商品
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
        <el-table-column label="商品信息" min-width="280">
          <template #default="{ row }">
            <div class="product-info">
              <el-image
                :src="row.imageUrl"
                :preview-src-list="[row.imageUrl]"
                fit="cover"
                class="product-image"
              >
                <template #error>
                  <div class="image-placeholder">
                    <el-icon :size="24"><Goods /></el-icon>
                  </div>
                </template>
              </el-image>
              <div class="product-detail">
                <div class="product-name">{{ row.name }}</div>
                <div class="product-code">编号: {{ row.code }}</div>
                <div class="product-tags">
                  <el-tag v-if="row.isHot" type="danger" size="small" effect="plain">热销</el-tag>
                  <el-tag v-if="row.isNew" type="success" size="small" effect="plain">新品</el-tag>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="分类" width="120">
          <template #default="{ row }">
            <el-tag :type="getCategoryType(row.category)" effect="light">
              {{ getCategoryLabel(row.category) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="价格" width="150">
          <template #default="{ row }">
              <div class="price-info">
              <div class="current-price">¥{{ row.originalPrice }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="库存" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.stock > 10 ? 'success' : row.stock > 0 ? 'warning' : 'danger'" size="small">
              {{ row.stock }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="销量" width="100" align="center" prop="sales" />
        <el-table-column label="积分抵扣" width="120" align="center">
          <template #default="{ row }">
            <span v-if="row.pointsDiscountType === 'NONE'" class="text-gray">不支持</span>
            <span v-else-if="row.pointsDiscountType === 'FIXED'">¥{{ row.maxPointsDiscount }}</span>
            <span v-else-if="row.pointsDiscountType === 'PERCENT'">{{ row.pointsDiscountValue }}%</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              active-value="ACTIVE"
              inactive-value="INACTIVE"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button type="primary" link @click="handleStock(row)">
              <el-icon><Box /></el-icon>库存
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
      :title="isEdit ? '编辑商品' : '新增商品'"
      width="800px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="product-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品图片" prop="imageUrl">
              <el-upload
                class="product-uploader"
                action="#"
                :auto-upload="false"
                :show-file-list="false"
                :on-change="handleImageChange"
                accept="image/*"
              >
                <img v-if="form.imageUrl" :src="form.imageUrl" class="product-preview" />
                <div v-else class="upload-placeholder">
                  <el-icon :size="28"><Plus /></el-icon>
                  <span>点击上传</span>
                </div>
              </el-upload>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入商品名称" maxlength="50" show-word-limit />
            </el-form-item>
            <el-form-item label="商品编号" prop="code">
              <el-input v-model="form.code" placeholder="请输入商品编号" />
            </el-form-item>
            <el-form-item label="所属分类" prop="category">
              <el-select v-model="form.category" placeholder="请选择分类" style="width: 100%">
                <el-option label="运动装备" value="EQUIPMENT" />
                <el-option label="营养补剂" value="SUPPLEMENT" />
                <el-option label="课程优惠" value="COURSE" />
                <el-option label="其他" value="OTHER" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="售价" prop="originalPrice">
              <el-input-number v-model="form.originalPrice" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="库存" prop="stock">
              <el-input-number v-model="form.stock" :min="0" :precision="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="排序" prop="sortOrder">
              <el-input-number v-model="form.sortOrder" :min="0" :max="999" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 积分抵扣配置 -->
        <el-divider content-position="left">积分抵扣配置</el-divider>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="抵扣类型">
              <el-select v-model="form.pointsDiscountType" placeholder="请选择" style="width: 100%">
                <el-option label="固定金额" value="FIXED" />
                <el-option label="比例抵扣" value="PERCENT" />
                <el-option label="不支持" value="NONE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="抵扣值">
              <el-input-number 
                v-model="form.pointsDiscountValue" 
                :min="0" 
                :precision="form.pointsDiscountType === 'PERCENT' ? 0 : 2" 
                style="width: 100%" 
              />
              <span class="form-tip">{{ form.pointsDiscountType === 'PERCENT' ? '%' : '元' }}</span>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="最大抵扣">
              <el-input-number v-model="form.maxPointsDiscount" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio label="ACTIVE">上架</el-radio>
                <el-radio label="INACTIVE">下架</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="商品标签">
          <el-checkbox v-model="form.isHot">热销</el-checkbox>
          <el-checkbox v-model="form.isNew">新品</el-checkbox>
          <el-checkbox v-model="form.isRecommend">推荐</el-checkbox>
        </el-form-item>

        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入商品描述，支持AI润色..."
            maxlength="500"
            show-word-limit
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
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 库存管理弹窗 -->
    <el-dialog
      v-model="stockDialogVisible"
      title="库存管理"
      width="500px"
    >
      <div v-if="currentProduct" class="stock-info">
        <div class="product-brief">
          <el-image :src="currentProduct.imageUrl" class="brief-image" fit="cover">
            <template #error>
              <div class="brief-placeholder">
                <el-icon><Goods /></el-icon>
              </div>
            </template>
          </el-image>
          <div class="brief-info">
            <div class="brief-name">{{ currentProduct.name }}</div>
            <div class="brief-stock">当前库存: {{ currentProduct.stock }}</div>
          </div>
        </div>

        <el-form :model="stockForm" label-width="100px">
          <el-form-item label="操作类型">
            <el-radio-group v-model="stockForm.type">
              <el-radio label="IN">入库</el-radio>
              <el-radio label="OUT">出库</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="数量">
            <el-input-number v-model="stockForm.quantity" :min="1" :precision="0" style="width: 100%" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input
              v-model="stockForm.remark"
              type="textarea"
              :rows="2"
              placeholder="请输入操作备注"
            />
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="stockDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleStockSubmit" :loading="stockSubmitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, Plus, Edit, Delete, Goods, Box, MagicStick, RefreshLeft
} from '@element-plus/icons-vue'
import {
  getAdminProducts,
  createAdminProduct,
  updateAdminProduct,
  deleteAdminProduct,
  updateAdminProductStatus,
  updateAdminProductStock
} from '@/api/admin/product'
import { uploadImage } from '@/api/file'
import { polishDescription } from '@/api/ai'

// 统计数据
const stats = ref([
  { title: '商品总数', value: 86, icon: 'Goods', color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
  { title: '上架商品', value: 72, icon: 'Box', color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
  { title: '本月销量', value: 328, icon: 'TrendCharts', color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' },
  { title: '库存预警', value: 5, icon: 'Warning', color: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)' }
])

// 搜索表单
const searchForm = reactive({
  keyword: '',
  category: '',
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

// 弹窗控制
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const submitting = ref(false)

// AI润色相关状态
const polishing = ref(false)
const originalText = ref('')
const hasPolished = ref(false)

// 表单数据
const form = reactive({
  id: null,
  name: '',
  code: '',
  category: '',
  imageUrl: '',
  originalPrice: 0,
  stock: 0,
  pointsDiscountType: 'NONE',
  pointsDiscountValue: 0,
  maxPointsDiscount: 0,
  status: 'ACTIVE',
  sortOrder: 0,
  isHot: false,
  isNew: false,
  isRecommend: false,
  description: ''
})

// 表单验证规则
const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入商品编号', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  originalPrice: [{ required: true, message: '请输入售价', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
}

// 库存弹窗
const stockDialogVisible = ref(false)
const currentProduct = ref(null)
const stockSubmitting = ref(false)
const stockForm = reactive({
  type: 'IN',
  quantity: 1,
  remark: ''
})

// 分类映射
const categoryMap = {
  EQUIPMENT: { label: '运动装备', type: 'primary' },
  SUPPLEMENT: { label: '营养补剂', type: 'warning' },
  COURSE: { label: '课程优惠', type: 'success' },
  OTHER: { label: '其他', type: 'info' }
}

function getCategoryLabel(category) {
  return categoryMap[category]?.label || category
}

function getCategoryType(category) {
  return categoryMap[category]?.type || ''
}

// 搜索
async function handleSearch() {
  loading.value = true
  try {
    const params = {
      category: searchForm.category,
      status: searchForm.status,
      keyword: searchForm.keyword
    }
    console.log('开始获取商品列表，参数:', params)
    const res = await getAdminProducts(params)
    console.log('获取商品列表成功，数据:', res)
    tableData.value = res || []
    pagination.total = tableData.value.length
    
    // 更新统计数据
    updateStats()
  } catch (error) {
    console.error('获取商品列表失败:', error)
    ElMessage.error('获取商品列表失败: ' + (error.message || '未知错误'))
    tableData.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.category = ''
  searchForm.status = ''
  handleSearch()
}

// 更新统计数据
function updateStats() {
  const total = tableData.value.length
  const active = tableData.value.filter(item => item.status === 'ACTIVE').length
  const totalSales = tableData.value.reduce((sum, item) => sum + (item.sales || 0), 0)
  const lowStock = tableData.value.filter(item => item.stock < 10).length
  
  stats.value = [
    { title: '商品总数', value: total, icon: 'Goods', color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
    { title: '上架商品', value: active, icon: 'Box', color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
    { title: '总销量', value: totalSales, icon: 'TrendCharts', color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' },
    { title: '库存预警', value: lowStock, icon: 'Warning', color: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)' }
  ]
}

// 新增
function handleAdd() {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    name: '',
    code: '',
    category: '',
    imageUrl: '',
    originalPrice: 0,
    stock: 0,
    pointsDiscountType: 'NONE',
    pointsDiscountValue: 0,
    maxPointsDiscount: 0,
    status: 'ACTIVE',
    sortOrder: 0,
    isHot: false,
    isNew: false,
    isRecommend: false,
    description: ''
  })
  dialogVisible.value = true
}

// 编辑
function handleEdit(row) {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    name: row.name,
    code: row.code,
    category: row.category,
    imageUrl: row.imageUrl,
    originalPrice: row.originalPrice,
    stock: row.stock,
    pointsDiscountType: row.pointsDiscountType,
    pointsDiscountValue: row.pointsDiscountValue,
    maxPointsDiscount: row.maxPointsDiscount,
    status: row.status,
    sortOrder: row.sortOrder,
    isHot: row.isHot,
    isNew: row.isNew,
    isRecommend: row.isRecommend,
    description: row.description
  })
  dialogVisible.value = true
}

// 删除
async function handleDelete(row) {
  ElMessageBox.confirm(
    `确定要删除商品"${row.name}"吗？删除后将无法恢复。`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteAdminProduct(row.id)
      const index = tableData.value.findIndex(item => item.id === row.id)
      if (index > -1) {
        tableData.value.splice(index, 1)
        pagination.total--
      }
      updateStats()
      ElMessage.success('删除成功')
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

// 状态切换
async function handleStatusChange(row) {
  try {
    const newStatus = row.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
    await updateAdminProductStatus(row.id, newStatus)
    row.status = newStatus
    updateStats()
    const statusText = newStatus === 'ACTIVE' ? '上架' : '下架'
    ElMessage.success(`商品已${statusText}`)
  } catch (error) {
    ElMessage.error('状态更新失败')
  }
}

// 提交表单
function handleSubmit() {
  formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        // 构建提交数据，确保数字字段为数字类型
        const submitData = {
          ...form,
          originalPrice: Number(form.originalPrice),
          stock: Number(form.stock),
          sortOrder: Number(form.sortOrder),
          pointsDiscountValue: Number(form.pointsDiscountValue || 0),
          maxPointsDiscount: Number(form.maxPointsDiscount || 0)
        }
        console.log('提交数据:', submitData)
        if (isEdit.value) {
          const res = await updateAdminProduct(form.id, submitData)
          const index = tableData.value.findIndex(item => item.id === form.id)
          if (index > -1) {
            tableData.value[index] = { ...tableData.value[index], ...res }
          }
          ElMessage.success('编辑成功')
        } else {
          const res = await createAdminProduct(submitData)
          const newProduct = {
            ...res,
            sales: 0
          }
          tableData.value.unshift(newProduct)
          pagination.total++
          ElMessage.success('新增成功')
        }
        updateStats()
        dialogVisible.value = false
      } catch (error) {
        console.error(isEdit.value ? '编辑失败:' : '新增失败:', error)
        const errorMsg = error.response?.data?.message || error.message || '未知错误'
        ElMessage.error(isEdit.value ? '编辑失败: ' + errorMsg : '新增失败: ' + errorMsg)
      } finally {
        submitting.value = false
      }
    }
  })
}

// 图片上传
async function handleImageChange(file) {
  // 上传图片到服务器
  try {
    const formData = new FormData()
    formData.append('file', file.raw)
    const res = await uploadImage(formData)
    if (res && res.fileUrl) {
      form.imageUrl = res.fileUrl
      ElMessage.success('图片上传成功')
    } else {
      throw new Error('上传响应无效')
    }
  } catch (error) {
    console.error('图片上传失败:', error)
    ElMessage.error('图片上传失败，请重试')
    form.imageUrl = ''
  }
}

// 库存管理
function handleStock(row) {
  currentProduct.value = row
  stockForm.type = 'IN'
  stockForm.quantity = 1
  stockForm.remark = ''
  stockDialogVisible.value = true
}

// 提交库存操作
async function handleStockSubmit() {
  stockSubmitting.value = true
  try {
    const res = await updateAdminProductStock(currentProduct.value.id, {
      type: stockForm.type,
      quantity: stockForm.quantity,
      remark: stockForm.remark
    })
    
    // 更新本地数据
    const index = tableData.value.findIndex(item => item.id === currentProduct.value.id)
    if (index > -1) {
      tableData.value[index].stock = res.stock
    }
    
    const actionText = stockForm.type === 'IN' ? '入库' : '出库'
    ElMessage.success(`成功${actionText} ${stockForm.quantity} 件`)
    stockDialogVisible.value = false
    updateStats()
  } catch (error) {
    ElMessage.error(error.message || '库存操作失败')
  } finally {
    stockSubmitting.value = false
  }
}

// AI润色相关方法
async function handlePolish() {
  const textLength = form.description.trim().length
  
  if (textLength === 0) {
    ElMessage.warning('请先输入描述内容')
    return
  }
  
  if (textLength < 3) {
    ElMessage.warning('文本内容至少需要3个字')
    return
  }
  
  if (textLength > 500) {
    ElMessage.warning('文本长度不能超过500字')
    return
  }
  
  originalText.value = form.description
  polishing.value = true
  hasPolished.value = false
  
  try {
    const response = await polishDescription({ text: form.description })
    form.description = response.polishedText
    hasPolished.value = true
    ElMessage.success('润色成功')
  } catch (error) {
    ElMessage.error(error.message || '润色失败，请重试')
  } finally {
    polishing.value = false
  }
}

function handleCancelPolish() {
  polishing.value = false
  ElMessage.info('已取消润色')
}

function handleRestore() {
  form.description = originalText.value
  hasPolished.value = false
  ElMessage.success('已恢复原始文本')
}

// 分页
function handleSizeChange(val) {
  pagination.pageSize = val
  handleSearch()
}

function handlePageChange(val) {
  pagination.page = val
  handleSearch()
}

onMounted(() => {
  handleSearch()
})
</script>

<style scoped>
.products-page {
  padding: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
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

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.product-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-image {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  flex-shrink: 0;
}

.image-placeholder {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
}

.product-detail {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.product-code {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.product-tags {
  display: flex;
  gap: 6px;
}

.price-info {
  display: flex;
  flex-direction: column;
}

.current-price {
  font-size: 16px;
  font-weight: 600;
  color: #f56c6c;
}

.original-price {
  font-size: 12px;
  color: #909399;
  text-decoration: line-through;
}

.text-gray {
  color: #909399;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.product-form :deep(.el-form-item__label) {
  font-weight: 500;
}

.product-uploader {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
  width: 120px;
  height: 120px;
}

.product-uploader:hover {
  border-color: var(--el-color-primary);
}

.product-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8c939d;
  gap: 8px;
}

.stock-info {
  padding: 10px 0;
}

.product-brief {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
}

.brief-image {
  width: 60px;
  height: 60px;
  border-radius: 6px;
}

.brief-placeholder {
  width: 60px;
  height: 60px;
  border-radius: 6px;
  background: #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
}

.brief-info {
  flex: 1;
}

.brief-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.brief-stock {
  font-size: 14px;
  color: #606266;
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
