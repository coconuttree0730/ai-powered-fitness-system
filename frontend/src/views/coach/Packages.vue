<template>
  <div class="coach-packages">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">我的套餐</h2>
        <span class="page-subtitle">管理您的私教课程套餐，供学员购买</span>
      </div>
      <n-button type="primary" size="large" @click="openAddModal">
        <template #icon>
          <n-icon :component="AddOutline" />
        </template>
        新增套餐
      </n-button>
    </div>

    <!-- 统计卡片 -->
    <n-grid :cols="3" :x-gap="16" style="margin-bottom: 24px;">
      <n-grid-item>
        <div class="stat-card">
          <div class="stat-icon primary">
            <n-icon :component="PricetagsOutline" size="24" />
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ packages.length }}</div>
            <div class="stat-label">套餐总数</div>
          </div>
        </div>
      </n-grid-item>
      <n-grid-item>
        <div class="stat-card">
          <div class="stat-icon success">
            <n-icon :component="CheckmarkCircleOutline" size="24" />
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ activeCount }}</div>
            <div class="stat-label">上架中</div>
          </div>
        </div>
      </n-grid-item>
      <n-grid-item>
        <div class="stat-card">
          <div class="stat-icon default">
            <n-icon :component="CloseCircleOutline" size="24" />
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ inactiveCount }}</div>
            <div class="stat-label">已下架</div>
          </div>
        </div>
      </n-grid-item>
    </n-grid>

    <!-- 套餐列表 -->
    <div v-if="packages.length > 0" class="package-grid">
      <div v-for="pkg in packages" :key="pkg.id" class="package-card">
        <div class="package-header">
          <h3 class="package-name">{{ pkg.name }}</h3>
          <n-tag :type="pkg.status === 'ACTIVE' ? 'success' : 'default'" size="small">
            {{ pkg.status === 'ACTIVE' ? '上架中' : '已下架' }}
          </n-tag>
        </div>
        <div class="package-body">
          <div class="package-meta">
            <span class="meta-item">编码: {{ pkg.code || '-' }}</span>
            <span class="meta-item">{{ pkg.totalSessions || 0 }} 课时</span>
            <span class="meta-item">{{ pkg.validityDays || 0 }} 天</span>
            <span class="meta-item price">¥{{ pkg.originalPrice }}</span>
            <span class="meta-item">库存: {{ pkg.stock }}</span>
          </div>
          <p class="package-desc">{{ pkg.description || '暂无描述' }}</p>
        </div>
        <div class="package-actions">
          <n-button text type="primary" @click="openEditModal(pkg)">编辑</n-button>
          <n-button text :type="pkg.status === 'ACTIVE' ? 'warning' : 'success'"
            @click="toggleStatus(pkg)">
            {{ pkg.status === 'ACTIVE' ? '下架' : '上架' }}
          </n-button>
          <n-button text type="error" @click="handleDelete(pkg)">删除</n-button>
        </div>
      </div>
    </div>
    <n-empty v-else description="暂无套餐，点击右上角新增" style="padding: 60px 0;" />

    <!-- 新增/编辑弹窗 -->
    <n-modal v-model:show="showModal" preset="card" :title="modalTitle" style="width: 520px" :mask-closable="false">
      <n-form ref="formRef" :model="form" :rules="rules" label-width="100">
        <n-form-item label="套餐名称" path="name">
          <n-input v-model:value="form.name" placeholder="例如：单次体验课" />
        </n-form-item>
        <n-form-item label="套餐编码" path="packageCode">
          <n-select v-model:value="form.packageCode" :options="dictOptions" placeholder="请选择套餐编码" clearable />
        </n-form-item>
        <n-form-item label="课时数" path="totalSessions">
          <n-input-number v-model:value="form.totalSessions" :min="1" :precision="0" style="width: 100%">
            <template #suffix>课时</template>
          </n-input-number>
        </n-form-item>
        <n-form-item label="有效期" path="validityDays">
          <n-input-number v-model:value="form.validityDays" :min="1" :precision="0" style="width: 100%">
            <template #suffix>天</template>
          </n-input-number>
        </n-form-item>
        <n-form-item label="价格" path="originalPrice">
          <n-input-number v-model:value="form.originalPrice" :min="0" :precision="2" style="width: 100%">
            <template #prefix>¥</template>
          </n-input-number>
        </n-form-item>
        <n-form-item label="库存" path="stock">
          <n-input-number v-model:value="form.stock" :min="0" :precision="0" style="width: 100%" />
        </n-form-item>
        <n-form-item label="排序" path="sortOrder">
          <n-input-number v-model:value="form.sortOrder" :min="0" style="width: 100%" />
        </n-form-item>
        <n-form-item label="状态" path="status">
          <n-radio-group v-model:value="form.status">
            <n-radio-button value="ACTIVE">上架</n-radio-button>
            <n-radio-button value="INACTIVE">下架</n-radio-button>
          </n-radio-group>
        </n-form-item>
        <n-form-item label="套餐说明" path="description">
          <n-input v-model:value="form.description" type="textarea" :rows="3" placeholder="请输入套餐说明..." />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" :loading="submitting" @click="handleSubmit">保存</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useMessage, useDialog } from 'naive-ui'
import {
  AddOutline,
  PricetagsOutline,
  CheckmarkCircleOutline,
  CloseCircleOutline
} from '@vicons/ionicons5'
import { getDictOptions } from '@/api/dict'
import {
  getMyPackages,
  createPackage,
  updatePackage,
  deletePackage,
  updatePackageStatus
} from '@/api/coachProduct'

const message = useMessage()
const dialog = useDialog()

const packages = ref([])
const loading = ref(false)
const showModal = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref(null)
const dictOptions = ref([])

const form = ref({
  name: '',
  packageCode: null,
  totalSessions: 1,
  validityDays: 30,
  category: 'COURSE',
  originalPrice: 0,
  stock: 999,
  sortOrder: 0,
  status: 'ACTIVE',
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入套餐名称', trigger: 'blur' }],
  packageCode: [{ required: true, message: '请选择套餐编码', trigger: 'change' }],
  totalSessions: [{ required: true, type: 'number', message: '请输入课时数', trigger: 'blur' }],
  validityDays: [{ required: true, type: 'number', message: '请输入有效期天数', trigger: 'blur' }],
  originalPrice: [{ required: true, type: 'number', message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, type: 'number', message: '请输入库存', trigger: 'blur' }]
}

const modalTitle = computed(() => isEdit.value ? '编辑套餐' : '新增套餐')

const activeCount = computed(() => packages.value.filter(p => p.status === 'ACTIVE').length)
const inactiveCount = computed(() => packages.value.filter(p => p.status === 'INACTIVE').length)

async function fetchPackages() {
  loading.value = true
  try {
    const data = await getMyPackages()
    packages.value = data || []
  } catch (error) {
    console.error('获取套餐列表失败:', error)
    message.error('获取套餐列表失败')
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.value = {
    name: '',
    packageCode: null,
    totalSessions: 1,
    validityDays: 30,
    category: 'COURSE',
    originalPrice: 0,
    stock: 999,
    sortOrder: 0,
    status: 'ACTIVE',
    description: ''
  }
  isEdit.value = false
  editId.value = null
}

async function openAddModal() {
  resetForm()
  try {
    dictOptions.value = await getDictOptions('coach_package_code')
  } catch {
    dictOptions.value = []
  }
  showModal.value = true
}

async function openEditModal(pkg) {
  isEdit.value = true
  editId.value = pkg.id
  try {
    dictOptions.value = await getDictOptions('coach_package_code')
  } catch {
    dictOptions.value = []
  }
  form.value = {
    name: pkg.name,
    packageCode: pkg.code,
    totalSessions: pkg.totalSessions || 1,
    validityDays: pkg.validityDays || 30,
    category: 'COURSE',
    originalPrice: pkg.originalPrice,
    stock: pkg.stock,
    sortOrder: pkg.sortOrder || 0,
    status: pkg.status,
    description: pkg.description || ''
  }
  showModal.value = true
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    submitting.value = true

    if (isEdit.value) {
      await updatePackage(editId.value, form.value)
      message.success('套餐更新成功')
    } else {
      await createPackage(form.value)
      message.success('套餐创建成功')
    }

    showModal.value = false
    await fetchPackages()
  } catch (error) {
    console.error('保存套餐失败:', error)
    const msg = error?.response?.data?.message || error?.message || '保存失败'
    message.error(msg)
  } finally {
    submitting.value = false
  }
}

async function toggleStatus(pkg) {
  const newStatus = pkg.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
  const actionText = newStatus === 'ACTIVE' ? '上架' : '下架'
  try {
    await updatePackageStatus(pkg.id, newStatus)
    message.success(`套餐已${actionText}`)
    await fetchPackages()
  } catch (error) {
    console.error(`${actionText}失败:`, error)
    message.error(`${actionText}失败`)
  }
}

function handleDelete(pkg) {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除套餐「${pkg.name}」吗？删除后无法恢复。`,
    positiveText: '确认删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deletePackage(pkg.id)
        message.success('套餐已删除')
        await fetchPackages()
      } catch (error) {
        console.error('删除失败:', error)
        message.error('删除失败')
      }
    }
  })
}

onMounted(() => {
  fetchPackages()
})
</script>

<style scoped>
.coach-packages {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  gap: 16px;
  flex-wrap: wrap;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1A1A2E;
  margin: 0;
}

.page-subtitle {
  font-size: 14px;
  color: #6B7280;
}

/* 统计卡片 */
.stat-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-icon.primary {
  background: linear-gradient(135deg, #FF6B35, #FF8C61);
}

.stat-icon.success {
  background: linear-gradient(135deg, #06D6A0, #2EC4B6);
}

.stat-icon.default {
  background: linear-gradient(135deg, #9CA3AF, #D1D5DB);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1A1A2E;
  line-height: 1;
}

.stat-label {
  font-size: 13px;
  color: #6B7280;
  margin-top: 4px;
}

/* 套餐列表 */
.package-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.package-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.package-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
}

.package-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.package-name {
  font-size: 16px;
  font-weight: 600;
  color: #1A1A2E;
  margin: 0;
}

.package-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}

.package-meta {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.meta-item {
  font-size: 13px;
  color: #6B7280;
  background: #f3f4f6;
  padding: 4px 10px;
  border-radius: 8px;
}

.meta-item.price {
  color: #FF6B35;
  font-weight: 600;
  background: #fff2e8;
}

.package-desc {
  font-size: 13px;
  color: #9ca3af;
  margin: 0;
  line-height: 1.5;
  min-height: 20px;
}

.package-actions {
  display: flex;
  gap: 16px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

/* 响应式 */
@media (max-width: 768px) {
  .package-grid {
    grid-template-columns: 1fr;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>