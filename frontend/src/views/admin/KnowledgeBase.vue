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
              placeholder="文档标题"
              clearable
              style="width: 220px"
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
              <el-option label="已发布" :value="1" />
              <el-option label="草稿" :value="0" />
            </el-select>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>搜索
            </el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-space>
        </el-col>
        <el-col :span="6" style="text-align: right">
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>上传文档
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
        <el-table-column label="文档信息" min-width="300">
          <template #default="{ row }">
            <div class="kb-info">
              <el-avatar :size="48" :icon="Document" shape="square" />
              <div class="kb-detail">
                <div class="kb-name">{{ row.title }}</div>
                <div class="kb-desc">{{ row.summary || row.fileName || '暂无描述' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="切片数" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.chunkCount || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button 
              v-if="row.status !== 1" 
              type="success" 
              link 
              @click="handlePublish(row)"
            >
              <el-icon><Check /></el-icon>发布
            </el-button>
            <el-button 
              v-if="row.status === 1" 
              type="info" 
              link 
              @click="handleArchive(row)"
            >
              <el-icon><Close /></el-icon>设为草稿
            </el-button>
            <el-button type="warning" link @click="handleReindex(row)">
              <el-icon><Refresh /></el-icon>重建索引
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
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 上传弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑文档' : '上传文档'"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="kb-form"
      >
        <el-form-item label="文档标题" prop="title">
          <el-input
            v-model="form.title"
            placeholder="请输入文档标题"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item v-if="!isEdit" label="上传文件" prop="file">
          <el-upload
            ref="uploadRef"
            action="#"
            :auto-upload="false"
            :on-change="handleFileChange"
            :limit="1"
            accept=".md,.txt"
          >
            <el-button type="primary">
              <el-icon><Upload /></el-icon>选择文件
            </el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持 .md、.txt 格式，最大 10MB
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">草稿</el-radio>
            <el-radio :label="1">立即发布</el-radio>
          </el-radio-group>
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
  Search, Plus, Edit, Delete, Document, Refresh, Upload, Check, Close
} from '@element-plus/icons-vue'
import {
  getKnowledgeDocuments,
  getKnowledgeDocumentById,
  updateKnowledgeDocument,
  deleteKnowledgeDocument,
  publishKnowledgeDocument,
  archiveKnowledgeDocument,
  reindexKnowledgeDocument,
  uploadKnowledgeDocument
} from '@/api/knowledge'

// 统计数据
const stats = ref([
  { title: '文档总数', value: 0, icon: 'Document', color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
  { title: '已发布', value: 0, icon: 'View', color: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)' },
  { title: '草稿', value: 0, icon: 'Document', color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
  { title: '切片总数', value: 0, icon: 'Collection', color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' }
])

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: null,
  pageNum: 1,
  pageSize: 10
})

// 表格数据
const loading = ref(false)
const tableData = ref([])

// 分页
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 弹窗控制
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const submitting = ref(false)
const uploadRef = ref(null)

// 表单数据
const form = reactive({
  id: null,
  title: '',
  status: 0
})

// 当前选中的文件
const currentFile = ref(null)

// 表单验证规则
const rules = {
  title: [{ required: true, message: '请输入文档标题', trigger: 'blur' }]
}

// 获取文档列表
async function fetchDocuments() {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    const data = await getKnowledgeDocuments(params)
    tableData.value = data.records || []
    pagination.total = data.total || 0
    
    // 更新统计数据
    updateStats()
  } catch (error) {
    ElMessage.error('获取文档列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 更新统计数据
function updateStats() {
  const total = pagination.total
  const published = tableData.value.filter(item => item.status === 1).length
  const draft = tableData.value.filter(item => item.status === 0).length
  const chunks = tableData.value.reduce((sum, item) => sum + (item.chunkCount || 0), 0)
  
  stats.value[0].value = total
  stats.value[1].value = published
  stats.value[2].value = draft
  stats.value[3].value = chunks
}

// 搜索
function handleSearch() {
  pagination.pageNum = 1
  fetchDocuments()
}

// 重置
function handleReset() {
  searchForm.keyword = ''
  searchForm.status = null
  handleSearch()
}

// 新增
function handleAdd() {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    title: '',
    status: 0
  })
  currentFile.value = null
  dialogVisible.value = true
}

// 编辑
async function handleEdit(row) {
  isEdit.value = true
  try {
    const data = await getKnowledgeDocumentById(row.id)
    Object.assign(form, {
      id: data.id,
      title: data.title,
      status: data.status
    })
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取文档详情失败')
  }
}

// 发布
async function handlePublish(row) {
  try {
    await publishKnowledgeDocument(row.id)
    ElMessage.success('发布成功')
    fetchDocuments()
  } catch (error) {
    ElMessage.error('发布失败')
  }
}

// 设为草稿
async function handleArchive(row) {
  try {
    await archiveKnowledgeDocument(row.id)
    ElMessage.success('已设为草稿')
    fetchDocuments()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 删除
function handleDelete(row) {
  ElMessageBox.confirm(
    `确定要删除文档"${row.title}"吗？删除后将无法恢复。`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteKnowledgeDocument(row.id)
      ElMessage.success('删除成功')
      fetchDocuments()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

// 重建索引
async function handleReindex(row) {
  try {
    await reindexKnowledgeDocument(row.id)
    ElMessage.success('重建索引成功')
    fetchDocuments()
  } catch (error) {
    ElMessage.error('重建索引失败')
  }
}

// 文件选择
function handleFileChange(file) {
  currentFile.value = file.raw
}

// 提交表单
async function handleSubmit() {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      if (isEdit.value) {
        // 编辑模式
        await updateKnowledgeDocument(form.id, {
          title: form.title,
          status: form.status
        })
        ElMessage.success('编辑成功')
      } else {
        // 上传模式
        if (!currentFile.value) {
          ElMessage.warning('请选择文件')
          submitting.value = false
          return
        }
        const formData = new FormData()
        formData.append('file', currentFile.value)
        if (form.title) {
          formData.append('title', form.title)
        }
        const docId = await uploadKnowledgeDocument(formData)
        
        // 如果选择了立即发布
        if (form.status === 1 && docId) {
          await publishKnowledgeDocument(docId)
        }
        
        ElMessage.success(form.status === 1 ? '上传并发布成功' : '上传成功')
      }
      
      dialogVisible.value = false
      fetchDocuments()
    } catch (error) {
      console.error('操作失败:', error)
      ElMessage.error(isEdit.value ? '编辑失败' : '上传失败')
    } finally {
      submitting.value = false
    }
  })
}

// 分页
function handleSizeChange(val) {
  pagination.pageSize = val
  fetchDocuments()
}

function handlePageChange(val) {
  pagination.pageNum = val
  fetchDocuments()
}

// 格式化日期
function formatDate(date) {
  if (!date) return '-'
  return new Date(date).toLocaleString()
}

onMounted(() => {
  fetchDocuments()
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
</style>
