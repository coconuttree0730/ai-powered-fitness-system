<template>
  <div class="knowledge-base-page">
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
              placeholder="知识库名称/描述"
              clearable
              style="width: 220px"
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
              <el-option label="健身知识" value="FITNESS" />
              <el-option label="营养饮食" value="NUTRITION" />
              <el-option label="运动康复" value="REHABILITATION" />
              <el-option label="训练计划" value="TRAINING" />
              <el-option label="常见问题" value="FAQ" />
            </el-select>
            <el-select
              v-model="searchForm.status"
              placeholder="全部状态"
              clearable
              style="width: 150px"
            >
              <el-option label="已发布" value="PUBLISHED" />
              <el-option label="草稿" value="DRAFT" />
              <el-option label="已归档" value="ARCHIVED" />
            </el-select>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>搜索
            </el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-space>
        </el-col>
        <el-col :span="6" style="text-align: right">
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增知识库
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
        <el-table-column label="知识库信息" min-width="250">
          <template #default="{ row }">
            <div class="kb-info">
              <el-avatar :size="48" :src="row.coverImage" :icon="Collection" shape="square" />
              <div class="kb-detail">
                <div class="kb-name">{{ row.name }}</div>
                <div class="kb-desc">{{ row.description }}</div>
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
        <el-table-column label="文章数" width="100" align="center">
          <template #default="{ row }">
            <el-link type="primary" @click="viewArticles(row)">{{ row.articleCount }}</el-link>
          </template>
        </el-table-column>
        <el-table-column label="浏览量" width="120" align="center">
          <template #default="{ row }">
            <span class="view-count">
              <el-icon><View /></el-icon>
              {{ formatNumber(row.viewCount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              active-value="PUBLISHED"
              inactive-value="DRAFT"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="更新时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button type="primary" link @click="viewArticles(row)">
              <el-icon><Document /></el-icon>文章
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
      :title="isEdit ? '编辑知识库' : '新增知识库'"
      width="700px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="kb-form"
      >
        <el-form-item label="封面图片" prop="coverImage">
          <el-upload
            class="cover-uploader"
            action="#"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleCoverChange"
            accept="image/*"
          >
            <img v-if="form.coverImage" :src="form.coverImage" class="cover-preview" />
            <div v-else class="cover-placeholder">
              <el-icon :size="28"><Plus /></el-icon>
              <span>点击上传封面</span>
            </div>
          </el-upload>
          <div class="form-tip">建议尺寸 400x300，支持 jpg、png 格式</div>
        </el-form-item>

        <el-form-item label="知识库名称" prop="name">
          <el-input
            v-model="form.name"
            placeholder="请输入知识库名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="所属分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类" style="width: 100%">
            <el-option label="健身知识" value="FITNESS" />
            <el-option label="营养饮食" value="NUTRITION" />
            <el-option label="运动康复" value="REHABILITATION" />
            <el-option label="训练计划" value="TRAINING" />
            <el-option label="常见问题" value="FAQ" />
          </el-select>
        </el-form-item>

        <el-form-item label="知识库描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入知识库描述"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="PUBLISHED">发布</el-radio>
            <el-radio label="DRAFT">草稿</el-radio>
            <el-radio label="ARCHIVED">归档</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
          <span class="form-tip" style="margin-left: 10px">数字越小排序越靠前</span>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 文章列表弹窗 -->
    <el-dialog
      v-model="articleDialogVisible"
      title="文章管理"
      width="900px"
      destroy-on-close
    >
      <div class="article-header">
        <span class="kb-title">{{ currentKb?.name }}</span>
        <el-button type="primary" @click="handleAddArticle">
          <el-icon><Plus /></el-icon>新增文章
        </el-button>
      </div>
      <el-table :data="articleList" stripe style="width: 100%">
        <el-table-column type="index" width="50" />
        <el-table-column label="文章标题" min-width="200">
          <template #default="{ row }">
            <el-link type="primary" @click="previewArticle(row)">{{ row.title }}</el-link>
          </template>
        </el-table-column>
        <el-table-column label="作者" width="120" prop="author" />
        <el-table-column label="浏览量" width="100" align="center">
          <template #default="{ row }">
            {{ formatNumber(row.viewCount) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PUBLISHED' ? 'success' : 'info'" size="small">
              {{ row.status === 'PUBLISHED' ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEditArticle(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDeleteArticle(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, Plus, Edit, Delete, View, Document,
  Collection
} from '@element-plus/icons-vue'

// 统计数据
const stats = ref([
  { title: '知识库总数', value: 12, icon: 'Collection', color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
  { title: '文章总数', value: 156, icon: 'Document', color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
  { title: '本月新增', value: 8, icon: 'Plus', color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' },
  { title: '总浏览量', value: '12.5K', icon: 'View', color: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)' }
])

// 搜索表单
const searchForm = reactive({
  keyword: '',
  category: '',
  status: ''
})

// 表格数据
const loading = ref(false)
const tableData = ref([
  {
    id: 1,
    name: '健身入门指南',
    description: '为健身新手提供全面的入门指导，包括基础动作、训练计划等',
    category: 'FITNESS',
    coverImage: '',
    articleCount: 25,
    viewCount: 5680,
    status: 'PUBLISHED',
    sort: 1,
    updateTime: '2024-01-15 14:30:00'
  },
  {
    id: 2,
    name: '营养饮食百科',
    description: '科学的饮食建议，营养搭配方案，助力健身效果最大化',
    category: 'NUTRITION',
    coverImage: '',
    articleCount: 18,
    viewCount: 3420,
    status: 'PUBLISHED',
    sort: 2,
    updateTime: '2024-01-14 10:20:00'
  },
  {
    id: 3,
    name: '运动康复知识',
    description: '运动损伤预防与康复训练，保护你的身体',
    category: 'REHABILITATION',
    coverImage: '',
    articleCount: 12,
    viewCount: 2150,
    status: 'DRAFT',
    sort: 3,
    updateTime: '2024-01-13 16:45:00'
  },
  {
    id: 4,
    name: '增肌训练计划',
    description: '专业的增肌训练方案，适合不同阶段的健身爱好者',
    category: 'TRAINING',
    coverImage: '',
    articleCount: 30,
    viewCount: 8900,
    status: 'PUBLISHED',
    sort: 4,
    updateTime: '2024-01-12 09:15:00'
  },
  {
    id: 5,
    name: '常见问题解答',
    description: '健身过程中常见问题的解答与建议',
    category: 'FAQ',
    coverImage: '',
    articleCount: 45,
    viewCount: 12300,
    status: 'PUBLISHED',
    sort: 5,
    updateTime: '2024-01-11 11:30:00'
  }
])

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 5
})

// 弹窗控制
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const submitting = ref(false)

// 表单数据
const form = reactive({
  id: null,
  name: '',
  description: '',
  category: '',
  coverImage: '',
  status: 'DRAFT',
  sort: 0
})

// 表单验证规则
const rules = {
  name: [{ required: true, message: '请输入知识库名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  description: [{ required: true, message: '请输入知识库描述', trigger: 'blur' }]
}

// 文章弹窗
const articleDialogVisible = ref(false)
const currentKb = ref(null)
const articleList = ref([
  { id: 1, title: '如何制定个人健身计划', author: '张教练', viewCount: 1200, status: 'PUBLISHED', updateTime: '2024-01-15 10:00:00' },
  { id: 2, title: '新手必知的10个健身常识', author: '李教练', viewCount: 980, status: 'PUBLISHED', updateTime: '2024-01-14 14:30:00' },
  { id: 3, title: '健身前的热身运动指南', author: '王教练', viewCount: 750, status: 'DRAFT', updateTime: '2024-01-13 09:20:00' }
])

// 分类映射
const categoryMap = {
  FITNESS: { label: '健身知识', type: 'primary' },
  NUTRITION: { label: '营养饮食', type: 'success' },
  REHABILITATION: { label: '运动康复', type: 'warning' },
  TRAINING: { label: '训练计划', type: 'danger' },
  FAQ: { label: '常见问题', type: 'info' }
}

function getCategoryLabel(category) {
  return categoryMap[category]?.label || category
}

function getCategoryType(category) {
  return categoryMap[category]?.type || ''
}

// 搜索
function handleSearch() {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('搜索完成')
  }, 500)
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.category = ''
  searchForm.status = ''
  handleSearch()
}

// 新增
function handleAdd() {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    name: '',
    description: '',
    category: '',
    coverImage: '',
    status: 'DRAFT',
    sort: 0
  })
  dialogVisible.value = true
}

// 编辑
function handleEdit(row) {
  isEdit.value = true
  Object.assign(form, { ...row })
  dialogVisible.value = true
}

// 删除
function handleDelete(row) {
  ElMessageBox.confirm(
    `确定要删除知识库"${row.name}"吗？删除后将无法恢复，关联的文章也会被删除。`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    const index = tableData.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      tableData.value.splice(index, 1)
      pagination.total--
    }
    ElMessage.success('删除成功')
  })
}

// 状态切换
function handleStatusChange(row) {
  const statusText = row.status === 'PUBLISHED' ? '发布' : '草稿'
  ElMessage.success(`已设置为${statusText}状态`)
}

// 提交表单
function handleSubmit() {
  formRef.value.validate((valid) => {
    if (valid) {
      submitting.value = true
      setTimeout(() => {
        if (isEdit.value) {
          const index = tableData.value.findIndex(item => item.id === form.id)
          if (index > -1) {
            tableData.value[index] = { ...form, updateTime: new Date().toLocaleString() }
          }
          ElMessage.success('编辑成功')
        } else {
          const newKb = {
            ...form,
            id: Date.now(),
            articleCount: 0,
            viewCount: 0,
            updateTime: new Date().toLocaleString()
          }
          tableData.value.unshift(newKb)
          pagination.total++
          ElMessage.success('新增成功')
        }
        submitting.value = false
        dialogVisible.value = false
      }, 500)
    }
  })
}

// 封面上传
function handleCoverChange(file) {
  const reader = new FileReader()
  reader.onload = (e) => {
    form.coverImage = e.target.result
  }
  reader.readAsDataURL(file.raw)
}

// 查看文章
function viewArticles(row) {
  currentKb.value = row
  articleDialogVisible.value = true
}

// 新增文章
function handleAddArticle() {
  ElMessage.info('打开文章编辑器')
}

// 编辑文章
function handleEditArticle(row) {
  ElMessage.info(`编辑文章: ${row.title}`)
}

// 删除文章
function handleDeleteArticle(row) {
  ElMessageBox.confirm(`确定要删除文章"${row.title}"吗？`, '确认删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const index = articleList.value.findIndex(item => item.id === row.id)
    if (index > -1) {
      articleList.value.splice(index, 1)
    }
    ElMessage.success('删除成功')
  })
}

// 预览文章
function previewArticle(row) {
  ElMessage.info(`预览文章: ${row.title}`)
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

// 格式化数字
function formatNumber(num) {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num
}

// 格式化日期
function formatDate(date) {
  return date
}

onMounted(() => {
  handleSearch()
})
</script>

<style scoped>
.knowledge-base-page {
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

.kb-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.kb-detail {
  flex: 1;
  min-width: 0;
}

.kb-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.kb-desc {
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.view-count {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #606266;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.kb-form :deep(.el-form-item__label) {
  font-weight: 500;
}

.cover-uploader {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
  width: 200px;
  height: 150px;
}

.cover-uploader:hover {
  border-color: var(--el-color-primary);
}

.cover-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8c939d;
  gap: 8px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.article-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.kb-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}
</style>
