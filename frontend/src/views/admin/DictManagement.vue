<template>
  <div class="dict-page">
    <!-- 字典列表 -->
    <el-card class="list-card">
      <template #header>
        <div class="card-header">
          <span class="header-title">数据字典列表</span>
          <el-button type="primary" @click="handleAddDict">
            <el-icon><Plus /></el-icon> 新增字典
          </el-button>
        </div>
      </template>

      <el-table :data="dictList" v-loading="loadingDicts" stripe border>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="dictName" label="字典名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="dictCode" label="字典编码" width="180">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ row.dictCode }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
        <el-table-column label="选项数" width="80" align="center">
          <template #default="{ row }">
            <el-badge :value="(row.items || []).length" :max="99" class="item-badge" />
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 'ACTIVE'"
              active-text="启用"
              inactive-text="禁用"
              @change="(val) => handleStatusChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="70" align="center" />
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEditDict(row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button type="danger" link size="small" @click="handleDeleteDict(row)">
              <el-icon><Delete /></el-icon>删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      :model-value="dictDialogVisible"
      @update:model-value="val => dictDialogVisible = val"
      :title="isEditDict ? '编辑字典' : '新增字典'"
      width="720px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form ref="dictFormRef" :model="dictForm" :rules="dictRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="字典名称" prop="dictName">
              <el-input v-model="dictForm.dictName" placeholder="如：会员卡类型" maxlength="50" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="字典编码" prop="dictCode">
              <el-input v-model="dictForm.dictCode" placeholder="如：membership_card_type" :disabled="isEditDict" maxlength="64">
                <template #prefix>
                  <span style="color:#909399;font-size:12px">code:</span>
                </template>
              </el-input>
              <div class="form-tip">仅支持小写字母和下划线，创建后不可修改</div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="排序号">
              <el-input-number v-model="dictForm.sortOrder" :min="0" style="width:100%" placeholder="数字越小越靠前" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="dictForm.status">
                <el-radio value="ACTIVE">启用</el-radio>
                <el-radio value="INACTIVE">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述">
          <el-input v-model="dictForm.description" type="textarea" :rows="2" placeholder="请输入描述信息" maxlength="200" show-word-limit />
        </el-form-item>

        <!-- 字典项管理 -->
        <el-divider content-position="left">
          <el-icon><List /></el-icon> 字典项配置
          <el-button type="primary" plain size="small" style="margin-left:12px" @click="addItem">
            <el-icon><Plus /></el-icon> 添加选项
          </el-button>
        </el-divider>

        <el-table :data="dictForm.items" border size="small" max-height="300" empty-text="暂无字典项，请添加">
          <el-table-column label="显示名称" min-width="150">
            <template #default="{ row }">
              <el-input v-model="row.label" placeholder="如：月卡" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="字典值" min-width="140">
            <template #default="{ row }">
              <el-input v-model="row.value" placeholder="如：MONTHLY" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="描述" min-width="160">
            <template #default="{ row }">
              <el-input v-model="row.description" placeholder="可选描述" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="排序" width="80" align="center">
            <template #default="{ row, $index }">
              <el-input-number v-model="row.sortOrder" :min="0" size="small" controls-position="right" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="70" align="center">
            <template #default="{ $index }">
              <el-button type="danger" link size="small" @click="removeItem($index)" :disabled="dictForm.items.length <= 1">
                <el-icon><Delete /></el-icon>
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-form>

      <template #footer>
        <el-button @click="dictDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmitDict">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, List } from '@element-plus/icons-vue'
import {
  getDictList,
  getDictDetail,
  createDict,
  updateDict,
  deleteDict
} from '@/api/dict'

const loadingDicts = ref(false)
const submitting = ref(false)
const dictList = ref([])
const dictDialogVisible = ref(false)
const isEditDict = ref(false)
const dictFormRef = ref(null)

const dictForm = reactive({
  id: null,
  dictName: '',
  dictCode: '',
  description: '',
  status: 'ACTIVE',
  sortOrder: 0,
  items: [{ label: '', value: '', description: '', sortOrder: 0 }]
})

const dictRules = {
  dictName: [
    { required: true, message: '请输入字典名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  dictCode: [
    { required: true, message: '请输入字典编码', trigger: 'blur' },
    { pattern: /^[a-z][a-z0-9_]*$/, message: '必须以小写字母开头，只能包含小写字母、数字和下划线', trigger: 'blur' }
  ]
}

async function loadDicts() {
  loadingDicts.value = true
  try {
    const data = await getDictList()
    console.log('字典列表数据:', data)
    // request.js 拦截器返回的是 res.data，直接是数组
    dictList.value = Array.isArray(data) ? data : (data?.records || [])
  } catch (error) {
    console.error('加载字典失败:', error)
    ElMessage.error('加载字典列表失败: ' + (error.message || '网络错误'))
    dictList.value = []
  } finally {
    loadingDicts.value = false
  }
}

function handleAddDict() {
  isEditDict.value = false
  resetForm()
  dictDialogVisible.value = true
}

async function handleEditDict(row) {
  isEditDict.value = true
  try {
    const data = await getDictDetail(row.id)
    console.log('字典详情:', data)
    if (data) {
      Object.assign(dictForm, {
        id: data.id,
        dictName: data.dictName || '',
        dictCode: data.dictCode || '',
        description: data.description || '',
        status: data.status || 'ACTIVE',
        sortOrder: data.sortOrder ?? 0,
        items: data.items && data.items.length > 0
          ? data.items.map(i => ({
              label: i.label || '',
              value: i.value || '',
              description: i.description || '',
              sortOrder: i.sortOrder ?? 0
            }))
          : [{ label: '', value: '', description: '', sortOrder: 0 }]
      })
      dictDialogVisible.value = true
    } else {
      ElMessage.warning('获取详情失败')
    }
  } catch (error) {
    console.error('获取字典详情失败:', error)
    ElMessage.error('获取字典详情失败')
  }
}

async function handleStatusChange(row, val) {
  const newStatus = val ? 'ACTIVE' : 'INACTIVE'
  try {
    await updateDict(row.id, { ...row, status: newStatus })
    ElMessage.success(val ? '已启用' : '已禁用')
    loadDicts()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

function handleDeleteDict(row) {
  ElMessageBox.confirm(
    `确定要删除字典「${row.dictName}」吗？<br/><span style='color:red'>删除后不可恢复，其下所有字典项也将被删除！</span>`,
    '警告',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
      dangerouslyUseHTMLString: true
    }
  ).then(async () => {
    try {
      await deleteDict(row.id)
      ElMessage.success('删除成功')
      loadDicts()
    } catch (error) {
      ElMessage.error(error.message || '删除失败')
    }
  }).catch(() => {})
}

function addItem() {
  dictForm.items.push({ label: '', value: '', description: '', sortOrder: dictForm.items.length })
}

function removeItem(index) {
  if (dictForm.items.length > 1) {
    dictForm.items.splice(index, 1)
  }
}

function resetForm() {
  Object.assign(dictForm, {
    id: null,
    dictName: '',
    dictCode: '',
    description: '',
    status: 'ACTIVE',
    sortOrder: 0,
    items: [{ label: '', value: '', description: '', sortOrder: 0 }]
  })
}

async function handleSubmitDict() {
  dictFormRef.value?.validate(async (valid) => {
    if (!valid) return

    const validItems = dictForm.items.filter(i => i.label && i.value)
    if (validItems.length === 0) {
      ElMessage.warning('至少需要填写一个有效的字典项')
      return
    }

    const values = validItems.map(i => i.value)
    if (new Set(values).size !== values.length) {
      ElMessage.warning('字典项的值不能重复')
      return
    }

    submitting.value = true
    try {
      const payload = {
        dictName: dictForm.dictName,
        dictCode: dictForm.dictCode,
        description: dictForm.description || undefined,
        status: dictForm.status,
        sortOrder: dictForm.sortOrder,
        items: validItems.map((item, idx) => ({
          label: item.label.trim(),
          value: item.value.trim(),
          description: item.description?.trim() || undefined,
          sortOrder: item.sortOrder ?? idx,
          status: 'ACTIVE'
        }))
      }

      if (isEditDict.value) {
        await updateDict(dictForm.id, payload)
      } else {
        await createDict(payload)
      }

      ElMessage.success(isEditDict.value ? '更新成功' : '创建成功')
      dictDialogVisible.value = false
      loadDicts()
    } catch (error) {
      console.error('提交失败:', error)
      ElMessage.error(error.message || '操作失败，请重试')
    } finally {
      submitting.value = false
    }
  })
}

onMounted(() => {
  loadDicts()
})
</script>

<style scoped>
.dict-page { padding: 20px; }

.list-card { margin-bottom: 20px; border-radius: 8px; }

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-title { font-weight: 600; font-size: 16px; color: #303133; }

.item-badge { margin-top: -4px; }

.form-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
  margin-top: 4px;
}
</style>
