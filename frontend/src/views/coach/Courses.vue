<template>
  <div class="coach-courses">
    <!-- 操作栏 -->
    <div class="action-bar">
      <div class="action-left">
        <n-button type="primary" size="large" @click="showPublishModal = true">
          <template #icon>
            <n-icon :component="AddOutline" />
          </template>
          发布课程
        </n-button>
        <n-button 
          type="error" 
          size="large" 
          :disabled="selectedRows.length === 0"
          @click="handleBatchDelete"
        >
          <template #icon>
            <n-icon :component="TrashOutline" />
          </template>
          批量删除 ({{ selectedRows.length }})
        </n-button>
      </div>
      <div class="action-right">
        <n-input-group>
          <n-input 
            v-model:value="searchKeyword" 
            placeholder="搜索课程名称..."
            clearable
            style="width: 240px"
          >
            <template #prefix>
              <n-icon :component="SearchOutline" />
            </template>
          </n-input>
          <n-select 
            v-model:value="filterType" 
            :options="typeOptions" 
            placeholder="课程类型"
            style="width: 140px"
            clearable
          />
        </n-input-group>
      </div>
    </div>

    <!-- 课程统计卡片 -->
    <n-grid :cols="4" :x-gap="16" class="stats-grid" style="margin-bottom: 24px;">
      <n-grid-item>
        <div class="mini-stat-card">
          <div class="mini-stat-icon primary">
            <n-icon :component="CalendarOutline" size="24" />
          </div>
          <div class="mini-stat-info">
            <div class="mini-stat-value">{{ courseStats.total }}</div>
            <div class="mini-stat-label">总课程数</div>
          </div>
        </div>
      </n-grid-item>
      <n-grid-item>
        <div class="mini-stat-card">
          <div class="mini-stat-icon success">
            <n-icon :component="FitnessOutline" size="24" />
          </div>
          <div class="mini-stat-info">
            <div class="mini-stat-value">{{ courseStats.private }}</div>
            <div class="mini-stat-label">私教课</div>
          </div>
        </div>
      </n-grid-item>
      <n-grid-item>
        <div class="mini-stat-card">
          <div class="mini-stat-icon warning">
            <n-icon :component="PeopleOutline" size="24" />
          </div>
          <div class="mini-stat-info">
            <div class="mini-stat-value">{{ courseStats.public }}</div>
            <div class="mini-stat-label">公开课</div>
          </div>
        </div>
      </n-grid-item>
      <n-grid-item>
        <div class="mini-stat-card">
          <div class="mini-stat-icon info">
            <n-icon :component="TrendingUpOutline" size="24" />
          </div>
          <div class="mini-stat-info">
            <div class="mini-stat-value">{{ courseStats.totalBookings }}</div>
            <div class="mini-stat-label">总预约数</div>
          </div>
        </div>
      </n-grid-item>
    </n-grid>

    <!-- 课程列表 -->
    <div class="table-card">
      <n-data-table
        :columns="columns"
        :data="filteredCourses"
        :loading="loading"
        :pagination="pagination"
        :row-key="row => row.id"
        @update:checked-row-keys="handleCheck"
      />
    </div>

    <!-- 发布课程弹窗 -->
    <n-modal 
      v-model:show="showPublishModal" 
      preset="card" 
      title="发布新课程" 
      style="width: 600px"
      :mask-closable="false"
    >
      <n-form 
        ref="formRef" 
        :model="publishForm" 
        :rules="publishRules"
        label-placement="left"
        label-width="100"
      >
        <n-form-item label="课程名称" path="courseName">
          <n-input v-model:value="publishForm.courseName" placeholder="请输入课程名称" />
        </n-form-item>
        
        <n-form-item label="课程类型" path="courseType">
          <n-radio-group v-model:value="publishForm.courseType">
            <n-radio-button value="private">私教课</n-radio-button>
            <n-radio-button value="public">公开课</n-radio-button>
          </n-radio-group>
        </n-form-item>
        
        <n-form-item label="课程分类" path="category">
          <n-select 
            v-model:value="publishForm.category" 
            :options="categoryOptions" 
            placeholder="请选择课程分类"
          />
        </n-form-item>
        
        <n-form-item label="课程日期" path="courseDate">
          <n-date-picker 
            v-model:value="publishForm.courseDate" 
            type="date" 
            style="width: 100%"
            :is-date-disabled="disablePreviousDate"
          />
        </n-form-item>
        
        <n-grid :cols="2" :x-gap="16">
          <n-grid-item>
            <n-form-item label="开始时间" path="startTime">
              <n-time-picker 
                v-model:value="publishForm.startTime" 
                style="width: 100%"
                format="HH:mm"
              />
            </n-form-item>
          </n-grid-item>
          <n-grid-item>
            <n-form-item label="结束时间" path="endTime">
              <n-time-picker 
                v-model:value="publishForm.endTime" 
                style="width: 100%"
                format="HH:mm"
              />
            </n-form-item>
          </n-grid-item>
        </n-grid>
        
        <n-form-item label="课程容量" path="capacity">
          <n-input-number 
            v-model:value="publishForm.capacity" 
            :min="1" 
            :max="publishForm.courseType === 'private' ? 1 : 50"
            style="width: 100%"
          >
            <template #suffix>人</template>
          </n-input-number>
        </n-form-item>
        
        <n-form-item label="课程价格" path="price">
          <n-input-number 
            v-model:value="publishForm.price" 
            :min="0" 
            :precision="2"
            style="width: 100%"
          >
            <template #prefix>¥</template>
          </n-input-number>
        </n-form-item>
        
        <n-form-item label="课程描述" path="description">
          <n-input 
            v-model:value="publishForm.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入课程描述..."
          />
        </n-form-item>
      </n-form>
      
      <template #footer>
        <n-space justify="end">
          <n-button @click="showPublishModal = false">取消</n-button>
          <n-button type="primary" :loading="publishLoading" @click="handlePublish">
            确认发布
          </n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 删除确认弹窗 -->
    <n-modal 
      v-model:show="showDeleteModal" 
      preset="dialog" 
      title="确认删除" 
      type="warning"
    >
      <p v-if="deleteMode === 'single'">
        确定要删除课程 <strong>"{{ deleteTarget?.courseName }}"</strong> 吗？<br/>
        <span style="color: #EF476F; font-size: 13px;">删除后将无法恢复，已预约的学员将收到取消通知。</span>
      </p>
      <p v-else>
        确定要删除选中的 <strong>{{ selectedRows.length }}</strong> 门课程吗？<br/>
        <span style="color: #EF476F; font-size: 13px;">删除后将无法恢复，已预约的学员将收到取消通知。</span>
      </p>
      <template #action>
        <n-button @click="showDeleteModal = false">取消</n-button>
        <n-button type="error" :loading="deleteLoading" @click="confirmDelete">
          确认删除
        </n-button>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, h, watch } from 'vue'
import { useMessage } from 'naive-ui'
import {
  AddOutline,
  TrashOutline,
  SearchOutline,
  CalendarOutline,
  FitnessOutline,
  PeopleOutline,
  TrendingUpOutline,
  CreateOutline,
  WarningOutline
} from '@vicons/ionicons5'

const message = useMessage()

// 搜索和筛选
const searchKeyword = ref('')
const filterType = ref(null)

// 表格选择
const selectedRows = ref([])

// 加载状态
const loading = ref(false)
const publishLoading = ref(false)
const deleteLoading = ref(false)

// 弹窗显示状态
const showPublishModal = ref(false)
const showDeleteModal = ref(false)
const deleteMode = ref('single') // 'single' | 'batch'
const deleteTarget = ref(null)

// 表单引用
const formRef = ref(null)

// 发布课程表单
const publishForm = reactive({
  courseName: '',
  courseType: 'private',
  category: null,
  courseDate: null,
  startTime: null,
  endTime: null,
  capacity: 1,
  price: 0,
  description: ''
})

// 表单校验规则
const publishRules = {
  courseName: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  courseType: [{ required: true, message: '请选择课程类型', trigger: 'change' }],
  category: [{ required: true, message: '请选择课程分类', trigger: 'change' }],
  courseDate: [{ required: true, message: '请选择课程日期', trigger: 'change', type: 'number' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change', type: 'number' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change', type: 'number' }],
  capacity: [{ required: true, message: '请输入课程容量', trigger: 'blur', type: 'number' }],
  price: [{ required: true, message: '请输入课程价格', trigger: 'blur', type: 'number' }]
}

// 选项数据
const typeOptions = [
  { label: '私教课', value: 'private' },
  { label: '公开课', value: 'public' }
]

const categoryOptions = [
  { label: '增肌训练', value: 'muscle' },
  { label: '减脂塑形', value: 'fat_loss' },
  { label: '瑜伽拉伸', value: 'yoga' },
  { label: 'HIIT燃脂', value: 'hiit' },
  { label: '核心训练', value: 'core' },
  { label: '康复训练', value: 'rehab' }
]

// 模拟课程数据
const courses = ref([
  { id: 1, courseName: '增肌训练基础', courseType: 'private', category: 'muscle', courseDate: '2024-10-25', startTime: '09:00', endTime: '10:00', capacity: 1, bookingCount: 1, price: 300, status: 'active' },
  { id: 2, courseName: 'HIIT燃脂团课', courseType: 'public', category: 'hiit', courseDate: '2024-10-25', startTime: '14:00', endTime: '15:00', capacity: 20, bookingCount: 12, price: 80, status: 'active' },
  { id: 3, courseName: '瑜伽拉伸入门', courseType: 'public', category: 'yoga', courseDate: '2024-10-26', startTime: '10:00', endTime: '11:00', capacity: 15, bookingCount: 8, price: 60, status: 'active' },
  { id: 4, courseName: '核心力量强化', courseType: 'private', category: 'core', courseDate: '2024-10-26', startTime: '16:00', endTime: '17:00', capacity: 1, bookingCount: 0, price: 350, status: 'active' },
  { id: 5, courseName: '减脂塑形训练', courseType: 'public', category: 'fat_loss', courseDate: '2024-10-27', startTime: '19:00', endTime: '20:00', capacity: 25, bookingCount: 20, price: 90, status: 'active' },
  { id: 6, courseName: '康复训练指导', courseType: 'private', category: 'rehab', courseDate: '2024-10-28', startTime: '14:00', endTime: '15:00', capacity: 1, bookingCount: 1, price: 400, status: 'active' }
])

// 课程统计
const courseStats = computed(() => {
  return {
    total: courses.value.length,
    private: courses.value.filter(c => c.courseType === 'private').length,
    public: courses.value.filter(c => c.courseType === 'public').length,
    totalBookings: courses.value.reduce((sum, c) => sum + c.bookingCount, 0)
  }
})

// 筛选后的课程列表
const filteredCourses = computed(() => {
  let result = courses.value
  
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(c => c.courseName.toLowerCase().includes(keyword))
  }
  
  if (filterType.value) {
    result = result.filter(c => c.courseType === filterType.value)
  }
  
  return result
})

// 表格列定义
const columns = [
  { type: 'selection', fixed: 'left' },
  { 
    title: '课程名称', 
    key: 'courseName',
    width: 180,
    render: (row) => h('div', { class: 'course-name-cell' }, [
      h('span', { class: 'course-name-text' }, row.courseName),
      h('span', { 
        class: `course-type-tag ${row.courseType}` 
      }, row.courseType === 'private' ? '私教' : '公开课')
    ])
  },
  { 
    title: '分类', 
    key: 'category',
    width: 120,
    render: (row) => {
      const map = { muscle: '增肌训练', fat_loss: '减脂塑形', yoga: '瑜伽拉伸', hiit: 'HIIT燃脂', core: '核心训练', rehab: '康复训练' }
      return map[row.category] || row.category
    }
  },
  { 
    title: '日期时间', 
    key: 'datetime',
    width: 180,
    render: (row) => h('div', { class: 'datetime-cell' }, [
      h('div', { class: 'date-text' }, row.courseDate),
      h('div', { class: 'time-text' }, `${row.startTime} - ${row.endTime}`)
    ])
  },
  { 
    title: '容量/预约', 
    key: 'capacity',
    width: 120,
    render: (row) => h('div', { class: 'capacity-cell' }, [
      h('span', { class: 'booking-count' }, row.bookingCount),
      h('span', { class: 'capacity-separator' }, '/'),
      h('span', { class: 'capacity-total' }, row.capacity)
    ])
  },
  { 
    title: '价格', 
    key: 'price',
    width: 100,
    render: (row) => h('span', { class: 'price-text' }, `¥${row.price}`)
  },
  { 
    title: '状态', 
    key: 'status',
    width: 100,
    render: (row) => {
      const isFull = row.bookingCount >= row.capacity
      return h('span', { 
        class: `status-badge ${isFull ? 'full' : 'available'}` 
      }, isFull ? '已满员' : '可预约')
    }
  },
  {
    title: '操作',
    key: 'actions',
    fixed: 'right',
    width: 150,
    render: (row) => h('div', { class: 'action-cell' }, [
      h('button', { 
        class: 'action-btn edit',
        onClick: () => handleEdit(row)
      }, [h('span', { class: 'btn-icon' }, '✏️'), '编辑']),
      h('button', { 
        class: 'action-btn delete',
        onClick: () => handleSingleDelete(row)
      }, [h('span', { class: 'btn-icon' }, '🗑️'), '删除'])
    ])
  }
]

// 分页配置
const pagination = reactive({
  page: 1,
  pageSize: 10,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  onChange: (page) => { pagination.page = page },
  onUpdatePageSize: (pageSize) => { pagination.pageSize = pageSize; pagination.page = 1 }
})

// 处理行选择
function handleCheck(rowKeys) {
  selectedRows.value = rowKeys
}

// 禁用过去的日期
function disablePreviousDate(ts) {
  return ts < Date.now() - 24 * 60 * 60 * 1000
}

// 发布课程
async function handlePublish() {
  try {
    await formRef.value?.validate()
    publishLoading.value = true
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    const newCourse = {
      id: Date.now(),
      courseName: publishForm.courseName,
      courseType: publishForm.courseType,
      category: publishForm.category,
      courseDate: new Date(publishForm.courseDate).toISOString().split('T')[0],
      startTime: formatTime(publishForm.startTime),
      endTime: formatTime(publishForm.endTime),
      capacity: publishForm.capacity,
      bookingCount: 0,
      price: publishForm.price,
      status: 'active'
    }
    
    courses.value.unshift(newCourse)
    message.success('课程发布成功')
    showPublishModal.value = false
    resetForm()
  } catch (error) {
    console.error(error)
  } finally {
    publishLoading.value = false
  }
}

// 格式化时间
function formatTime(timestamp) {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 重置表单
function resetForm() {
  Object.assign(publishForm, {
    courseName: '',
    courseType: 'private',
    category: null,
    courseDate: null,
    startTime: null,
    endTime: null,
    capacity: 1,
    price: 0,
    description: ''
  })
  formRef.value?.restoreValidation()
}

// 编辑课程
function handleEdit(row) {
  message.info(`编辑课程: ${row.courseName}`)
}

// 单个删除
function handleSingleDelete(row) {
  deleteMode.value = 'single'
  deleteTarget.value = row
  showDeleteModal.value = true
}

// 批量删除
function handleBatchDelete() {
  if (selectedRows.value.length === 0) {
    message.warning('请先选择要删除的课程')
    return
  }
  deleteMode.value = 'batch'
  showDeleteModal.value = true
}

// 确认删除
async function confirmDelete() {
  deleteLoading.value = true
  
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 800))
    
    if (deleteMode.value === 'single') {
      const index = courses.value.findIndex(c => c.id === deleteTarget.value.id)
      if (index > -1) {
        courses.value.splice(index, 1)
        message.success('课程删除成功')
      }
    } else {
      courses.value = courses.value.filter(c => !selectedRows.value.includes(c.id))
      message.success(`成功删除 ${selectedRows.value.length} 门课程`)
      selectedRows.value = []
    }
    
    showDeleteModal.value = false
    deleteTarget.value = null
  } finally {
    deleteLoading.value = false
  }
}

// 监听课程类型变化，自动调整容量
watch(() => publishForm.courseType, (newType) => {
  publishForm.capacity = newType === 'private' ? 1 : 20
})
</script>

<style scoped>
.coach-courses {
  max-width: 1400px;
  margin: 0 auto;
}

/* 操作栏 */
.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  gap: 16px;
  flex-wrap: wrap;
}

.action-left {
  display: flex;
  gap: 12px;
}

.action-right {
  display: flex;
  gap: 12px;
}

/* 迷你统计卡片 */
.mini-stat-card {
  background: white;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  transition: all 0.3s ease;
}

.mini-stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
}

.mini-stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.mini-stat-icon.primary { background: linear-gradient(135deg, #FF6B35, #FF8C61); }
.mini-stat-icon.success { background: linear-gradient(135deg, #06D6A0, #2EC4B6); }
.mini-stat-icon.warning { background: linear-gradient(135deg, #FFD166, #FFB347); }
.mini-stat-icon.info { background: linear-gradient(135deg, #667eea, #764ba2); }

.mini-stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #1A1A2E;
  line-height: 1;
}

.mini-stat-label {
  font-size: 12px;
  color: #6B7280;
  margin-top: 4px;
}

/* 表格卡片 */
.table-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.06);
}

/* 表格单元格样式 */
:deep(.course-name-cell) {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

:deep(.course-name-text) {
  font-weight: 600;
  color: #1A1A2E;
}

:deep(.course-type-tag) {
  display: inline-block;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 4px;
  width: fit-content;
}

:deep(.course-type-tag.private) {
  color: #FF6B35;
  background: rgba(255,107,53,0.1);
}

:deep(.course-type-tag.public) {
  color: #06D6A0;
  background: rgba(6,214,160,0.1);
}

:deep(.datetime-cell) {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

:deep(.date-text) {
  font-weight: 500;
  color: #1A1A2E;
}

:deep(.time-text) {
  font-size: 12px;
  color: #6B7280;
}

:deep(.capacity-cell) {
  display: flex;
  align-items: center;
  gap: 4px;
}

:deep(.booking-count) {
  font-weight: 600;
  color: #FF6B35;
}

:deep(.capacity-separator) {
  color: #9CA3AF;
}

:deep(.capacity-total) {
  color: #6B7280;
}

:deep(.price-text) {
  font-weight: 600;
  color: #1A1A2E;
}

:deep(.status-badge) {
  display: inline-block;
  font-size: 12px;
  font-weight: 500;
  padding: 4px 10px;
  border-radius: 20px;
}

:deep(.status-badge.available) {
  color: #06D6A0;
  background: rgba(6,214,160,0.1);
}

:deep(.status-badge.full) {
  color: #EF476F;
  background: rgba(239,71,111,0.1);
}

:deep(.action-cell) {
  display: flex;
  gap: 8px;
}

:deep(.action-btn) {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border-radius: 6px;
  border: none;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

:deep(.action-btn.edit) {
  color: #667eea;
  background: rgba(102,126,234,0.1);
}

:deep(.action-btn.edit:hover) {
  background: rgba(102,126,234,0.2);
}

:deep(.action-btn.delete) {
  color: #EF476F;
  background: rgba(239,71,111,0.1);
}

:deep(.action-btn.delete:hover) {
  background: rgba(239,71,111,0.2);
}

:deep(.btn-icon) {
  font-size: 14px;
}

/* 响应式 */
@media (max-width: 768px) {
  .action-bar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .action-left,
  .action-right {
    justify-content: stretch;
  }
  
  .action-left .n-button,
  .action-right .n-input-group {
    flex: 1;
  }
}
</style>
