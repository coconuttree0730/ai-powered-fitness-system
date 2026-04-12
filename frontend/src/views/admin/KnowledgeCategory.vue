<template>
  <div class="knowledge-category-page">
    <!-- 页面标题 -->
    <el-card class="header-card" :body-style="{ padding: '20px' }">
      <div class="page-header">
        <div class="header-title">
          <h2>知识库分类管理</h2>
          <p class="header-desc">管理知识库文档的分类，用于RAG检索时的分类过滤</p>
        </div>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>新增分类
        </el-button>
      </div>
    </el-card>

    <!-- 分类列表 -->
    <el-card class="table-card">
      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column type="index" width="50" label="序号" />
        <el-table-column label="分类名称" min-width="150">
          <template #default="{ row }">
            <div class="category-name">
              <el-tag size="small" type="primary">{{ row.name }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="分类代码" width="180">
          <template #default="{ row }">
            <code class="category-code">{{ row.code }}</code>
          </template>
        </el-table-column>
        <el-table-column label="描述" min-width="300">
          <template #default="{ row }">
            <span class="category-desc">{{ row.description || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="排序" width="80" align="center">
          <template #default="{ row }">
            {{ row.sortOrder || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
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
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑分类' : '新增分类'"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="category-form"
      >
        <el-form-item label="分类名称" prop="name">
          <el-input
            v-model="form.name"
            placeholder="请输入分类名称，如：场馆基础信息"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="分类代码" prop="code">
          <el-input
            v-model="form.code"
            placeholder="请输入分类代码，如：VENUE_INFO"
            maxlength="50"
            show-word-limit
          />
          <div class="form-tip">建议使用大写字母和下划线，如：COURSE_BOOKING</div>
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入分类描述，说明该分类包含哪些内容"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="排序" prop="sortOrder">
          <el-input-number
            v-model="form.sortOrder"
            :min="0"
            :max="999"
            placeholder="数字越小排序越靠前"
          />
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
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import {
  getKnowledgeCategories,
  getKnowledgeCategoryById,
  createKnowledgeCategory,
  updateKnowledgeCategory,
  deleteKnowledgeCategory
} from '@/api/knowledge'

// 表格数据
const loading = ref(false)
const tableData = ref([])

// 弹窗控制
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const submitting = ref(false)

// 表单数据
const form = reactive({
  id: null,
  name: '',
  code: '',
  description: '',
  sortOrder: 0
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入分类代码', trigger: 'blur' },
    { pattern: /^[A-Z_]+$/, message: '只能包含大写字母和下划线', trigger: 'blur' }
  ],
  sortOrder: [
    { required: true, message: '请输入排序号', trigger: 'blur' }
  ]
}

// 获取分类列表
async function fetchCategories() {
  loading.value = true
  try {
    const data = await getKnowledgeCategories()
    tableData.value = data || []
  } catch (error) {
    ElMessage.error('获取分类列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 新增
function handleAdd() {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    name: '',
    code: '',
    description: '',
    sortOrder: tableData.value.length + 1
  })
  dialogVisible.value = true
}

// 编辑
async function handleEdit(row) {
  isEdit.value = true
  try {
    const data = await getKnowledgeCategoryById(row.id)
    Object.assign(form, {
      id: data.id,
      name: data.name,
      code: data.code,
      description: data.description,
      sortOrder: data.sortOrder || 0
    })
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取分类详情失败')
  }
}

// 删除
function handleDelete(row) {
  ElMessageBox.confirm(
    `确定要删除分类"${row.name}"吗？删除后关联的文档将变为未分类状态。`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteKnowledgeCategory(row.id)
      ElMessage.success('删除成功')
      fetchCategories()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

// 提交表单
async function handleSubmit() {
  formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      if (isEdit.value) {
        // 编辑模式
        await updateKnowledgeCategory(form.id, {
          name: form.name,
          code: form.code,
          description: form.description,
          sortOrder: form.sortOrder
        })
        ElMessage.success('编辑成功')
      } else {
        // 新增模式
        await createKnowledgeCategory({
          name: form.name,
          code: form.code,
          description: form.description,
          sortOrder: form.sortOrder
        })
        ElMessage.success('新增成功')
      }

      dialogVisible.value = false
      fetchCategories()
    } catch (error) {
      console.error('操作失败:', error)
      ElMessage.error(isEdit.value ? '编辑失败' : '新增失败')
    } finally {
      submitting.value = false
    }
  })
}

// 格式化日期
function formatDate(date) {
  if (!date) return '-'
  return new Date(date).toLocaleString()
}

onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
.knowledge-category-page {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title h2 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.header-desc {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

.table-card {
  margin-bottom: 20px;
}

.category-name {
  font-weight: 500;
}

.category-code {
  background-color: #f5f7fa;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 13px;
  color: #606266;
  font-family: 'Courier New', monospace;
}

.category-desc {
  color: #606266;
  font-size: 14px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.category-form :deep(.el-form-item__label) {
  font-weight: 500;
}
</style>
