<template>
  <div class="admin-courses">
  <!-- 搜索区域 -->
    <n-card title="课程管理">
      <!-- 搜索区域 - 新增课程按钮与搜索栏平行 -->
      <n-card embedded style="margin-bottom: 16px;">
        <n-space align="center" wrap justify="space-between">
          <n-space align="center" wrap>
            <n-input
              v-model:value="searchForm.courseName"
              placeholder="课程名称"
              clearable
              style="width: 200px"
              @keyup.enter="handleSearch"
            />
            <n-select
              v-model:value="searchForm.category"
              :options="categoryOptions"
              placeholder="分类"
              clearable
              style="width: 150px"
            />
            <n-select
              v-model:value="searchForm.coachId"
              :options="coachOptions"
              placeholder="教练"
              clearable
              style="width: 150px"
            />
            <n-date-picker
              v-model:value="searchForm.startDate"
              type="date"
              placeholder="开始日期"
              clearable
              style="width: 150px"
            />
            <n-date-picker
              v-model:value="searchForm.endDate"
              type="date"
              placeholder="结束日期"
              clearable
              style="width: 150px"
            />
            <n-button type="primary" @click="handleSearch">
              <template #icon>
                <n-icon><SearchOutline /></n-icon>
              </template>
              搜索
            </n-button>
            <n-button @click="handleReset">重置</n-button>
          </n-space>
          <n-button type="primary" @click="handleAdd">
            <template #icon>
              <n-icon><AddOutline /></n-icon>
            </template>
            新增课程
          </n-button>
        </n-space>
      </n-card>
      <n-data-table :columns="columns" :data="courses" :loading="loading" :pagination="pagination" :row-key="row => row.id" />
    </n-card>

    <n-modal v-model:show="showModal" preset="card" :title="isEdit ? '编辑课程' : '新增课程'" style="width: 700px">
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="left" label-width="100">
        <n-grid :cols="2" :x-gap="12">
          <n-grid-item>
            <n-form-item label="课程名称" path="courseName">
              <n-input v-model:value="form.courseName" placeholder="请输入课程名称" />
            </n-form-item>
          </n-grid-item>
          <n-grid-item>
            <n-form-item label="分类" path="category">
              <n-select v-model:value="form.category" :options="categoryOptions" placeholder="请选择分类" />
            </n-form-item>
          </n-grid-item>
        </n-grid>
        <n-grid :cols="2" :x-gap="12">
          <n-grid-item>
            <n-form-item label="教练" path="coachId">
              <n-select v-model:value="form.coachId" :options="coachOptions" placeholder="请选择教练" />
            </n-form-item>
          </n-grid-item>
          <n-grid-item>
            <n-form-item label="容量" path="capacity">
              <n-input-number v-model:value="form.capacity" :min="1" style="width: 100%" />
            </n-form-item>
          </n-grid-item>
        </n-grid>
        <n-grid :cols="2" :x-gap="12">
          <n-grid-item>
            <n-form-item label="开始时间" path="startTime">
              <n-date-picker v-model:value="form.startTime" type="datetime" style="width: 100%" />
            </n-form-item>
          </n-grid-item>
          <n-grid-item>
            <n-form-item label="结束时间" path="endTime">
              <n-date-picker v-model:value="form.endTime" type="datetime" style="width: 100%" />
            </n-form-item>
          </n-grid-item>
        </n-grid>

        <!-- 课程图片上传 -->
        <n-form-item label="课程图片" path="imageUrl">
          <n-space vertical>
            <n-upload
              :action="uploadUrl"
              :headers="uploadHeaders"
              :data="{ folder: 'courses' }"
              name="file"
              accept="image/*"
              :max="1"
              v-model:file-list="fileList"
              list-type="image-card"
              style="--n-image-width: 100px; --n-image-height: 100px;"
              @before-upload="handleBeforeUpload"
              @finish="handleUploadFinish"
              @remove="handleUploadRemove"
              @error="handleUploadError"
            >
              <n-button style="width: 100px; height: 100px;">
                <n-space vertical align="center">
                  <n-icon size="24"><CloudUploadOutline /></n-icon>
                  <span>上传图片</span>
                </n-space>
              </n-button>
            </n-upload>
            <n-text depth="3" style="font-size: 12px;">支持 JPG、PNG 格式，建议尺寸 800x600</n-text>
          </n-space>
        </n-form-item>

        <n-form-item label="描述" path="description">
          <n-input v-model:value="form.description" type="textarea" :rows="3" placeholder="请输入课程描述" />
        </n-form-item>
        <n-form-item>
          <n-space>
            <n-button type="primary" :loading="submitting" @click="handleSubmit">提交</n-button>
            <n-button @click="showModal = false">取消</n-button>
          </n-space>
        </n-form-item>
      </n-form>
    </n-modal>

    <!-- 课程详情弹窗 -->
    <n-modal v-model:show="showDetailModal" preset="card" title="课程详情" style="width: 700px">
      <div class="course-detail" v-if="currentCourse">
        <!-- 课程封面区域 -->
        <div class="course-header">
          <div class="course-image-section">
            <n-image
              v-if="currentCourse.imageUrl"
              :src="currentCourse.imageUrl"
              width="200"
              height="150"
              style="border-radius: 8px; object-fit: cover;"
              :fallback-src="'/default-course.png'"
            />
            <div v-else class="no-image-placeholder">
              <n-icon size="48" depth="3"><ImageOutline /></n-icon>
              <span>暂无课程图片</span>
            </div>
          </div>
          <div class="course-basic-info">
            <h2 class="course-title">{{ currentCourse.courseName }}</h2>
            <n-tag :type="getCategoryType(currentCourse.category)" size="large">
              {{ getCategoryLabel(currentCourse.category) }}
            </n-tag>
            <div class="course-meta">
              <div class="meta-item">
                <n-icon size="16"><TimeOutline /></n-icon>
                <span>{{ formatTime(currentCourse.startTime) }}</span>
              </div>
              <div class="meta-item">
                <n-icon size="16"><PeopleOutline /></n-icon>
                <span>容量 {{ currentCourse.capacity }} 人</span>
              </div>
              <div class="meta-item">
                <n-icon size="16"><CalendarOutline /></n-icon>
                <span>已预约 {{ currentCourse.bookingCount || 0 }} 人</span>
              </div>
            </div>
          </div>
        </div>

        <n-divider />

        <!-- 教练信息区域 -->
        <div class="coach-section">
          <h3 class="section-title">
            <n-icon size="20" color="#2080f0"><PersonCircleOutline /></n-icon>
            教练信息
          </h3>
          <div class="coach-card">
            <div class="coach-avatar">
              <n-image
                v-if="currentCourse.coachAvatar"
                :src="currentCourse.coachAvatar"
                width="80"
                height="80"
                style="border-radius: 50%; object-fit: cover;"
                :fallback-src="'/default-avatar.png'"
              />
              <div v-else class="no-avatar">
                <n-icon size="32" depth="3"><PersonOutline /></n-icon>
              </div>
            </div>
            <div class="coach-info">
              <div class="coach-name">{{ currentCourse.coachName || '暂无教练' }}</div>
              <div class="coach-label">主讲教练</div>
            </div>
          </div>
        </div>

        <n-divider />

        <!-- 课程描述区域 -->
        <div class="description-section">
          <h3 class="section-title">
            <n-icon size="20" color="#2080f0"><DocumentTextOutline /></n-icon>
            课程描述
          </h3>
          <div class="description-content">
            {{ currentCourse.description || '暂无课程描述' }}
          </div>
        </div>

      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, h, reactive, onMounted, computed } from 'vue'
import { NTag, NButton, NSpace, NIcon, NImage, useMessage, useDialog, NText, NDivider } from 'naive-ui'
import { SearchOutline, AddOutline, CloudUploadOutline, TimeOutline, PeopleOutline, CalendarOutline, PersonCircleOutline, PersonOutline, DocumentTextOutline, ImageOutline } from '@vicons/ionicons5'
import { createCourse, updateCourse, deleteCourse } from '@/api/course'
import request from '@/utils/request'
import { getToken } from '@/utils/auth'

const coachOptions = ref([])

const message = useMessage()
const dialog = useDialog()
const loading = ref(false)
const submitting = ref(false)
const showModal = ref(false)
const showDetailModal = ref(false)
const isEdit = ref(false)
const courses = ref([])
const currentId = ref(null)
const currentCourse = ref(null)
const formRef = ref(null)

// 上传相关 - 使用代理路径
const uploadUrl = '/api/v1/files/upload'
const uploadHeaders = computed(() => ({
  Authorization: 'Bearer ' + getToken()
}))
const fileList = ref([])

// 上传前校验
function handleBeforeUpload({ file }) {
  // 校验文件类型
  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    message.error('只支持 JPG、PNG、GIF、WebP 格式的图片')
    return false
  }
  // 校验文件大小 (最大 10MB)
  const maxSize = 10 * 1024 * 1024
  if (file.size > maxSize) {
    message.error('图片大小不能超过 10MB')
    return false
  }
  return true
}

const pagination = reactive({
  page: 1,
  pageSize: 5,
  itemCount: 0,
  onChange: (page) => {
    pagination.page = page
    fetchCourses()
  }
})

const form = reactive({
  courseName: '',
  category: null,
  coachId: null,
  startTime: null,
  endTime: null,
  capacity: 20,
  description: '',
  imageUrl: ''
})

const searchForm = reactive({
  courseName: '',
  category: null,
  coachId: null,
  startDate: null,
  endDate: null
})

const rules = {
  courseName: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  coachId: [{ required: true, message: '请选择教练', trigger: 'change', type: 'number' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change', type: 'number' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change', type: 'number' }],
  capacity: [{ required: true, message: '请输入容量', trigger: 'blur', type: 'number' }]
}

const categoryOptions = [
  { label: '瑜伽', value: 'YOGA' },
  { label: '普拉提', value: 'PILATES' },
  { label: 'HIIT', value: 'HIIT' },
  { label: '力量训练', value: 'STRENGTH' },
  { label: '动感单车', value: 'SPINNING' }
]

const columns = [
  {
    title: '课程图片',
    key: 'imageUrl',
    width: 120,
    render: (row) => {
      if (row.imageUrl) {
        return h(NImage, {
          src: row.imageUrl,
          width: 80,
          height: 80,
          style: 'border-radius: 4px; object-fit: cover;',
          fallbackSrc: '/default-course.png'
        })
      }
      return h('div', {
        style: 'width: 80px; height: 80px; background: #f0f0f0; border-radius: 4px; display: flex; align-items: center; justify-content: center; color: #999; font-size: 12px;'
      }, '无图片')
    }
  },
  { title: '课程名称', key: 'courseName' },
  {
    title: '分类',
    key: 'category',
    render: (row) => {
      const item = categoryOptions.find(c => c.value === row.category)
      return h(NTag, { type: 'info' }, () => item?.label || row.category)
    }
  },
  {
    title: '教练头像',
    key: 'coachAvatar',
    width: 100,
    render: (row) => {
      if (row.coachAvatar) {
        return h(NImage, {
          src: row.coachAvatar,
          width: 60,
          height: 60,
          style: 'border-radius: 50%; object-fit: cover;',
          fallbackSrc: '/default-avatar.png'
        })
      }
      return h('div', {
        style: 'width: 60px; height: 60px; background: #f0f0f0; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: #999; font-size: 12px;'
      }, '无')
    }
  },
  { title: '教练名称', key: 'coachName' },
  { title: '开始时间', key: 'startTime', render: (row) => formatTime(row.startTime) },
  { title: '容量', key: 'capacity' },
  { title: '预约数', key: 'bookingCount' },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    render: (row) => h(NSpace, null, () => [
      h(NButton, { size: 'small', onClick: () => handleView(row) }, () => '查看'),
      h(NButton, { size: 'small', type: 'primary', onClick: () => handleEdit(row) }, () => '编辑'),
      h(NButton, { size: 'small', type: 'error', onClick: () => handleDelete(row) }, () => '删除')
    ])
  }
]

onMounted(() => {
  fetchCoachList()
  fetchCourses()
})

async function fetchCoachList() {
  try {
    const res = await request({
      url: '/coaches/list',
      method: 'get'
    })
    coachOptions.value = (res || []).map(coach => ({
      label: coach.name,
      value: coach.id
    }))
  } catch (error) {
    console.error('获取教练列表失败', error)
  }
}

async function fetchCourses() {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.page,
      pageSize: pagination.pageSize,
      ...buildSearchParams()
    }
    const res = await request({
      url: '/courses/list',
      method: 'get',
      params
    })
    courses.value = res.records || []
    pagination.itemCount = res.total || 0
  } catch (error) {
    message.error('获取课程列表失败')
  } finally {
    loading.value = false
  }
}

function buildSearchParams() {
  const params = {}
  if (searchForm.courseName) {
    params.courseName = searchForm.courseName
  }
  if (searchForm.category) {
    params.category = searchForm.category
  }
  if (searchForm.coachId) {
    params.coachId = searchForm.coachId
  }
  if (searchForm.startDate) {
    params.startDate = new Date(searchForm.startDate).toISOString().split('T')[0]
  }
  if (searchForm.endDate) {
    params.endDate = new Date(searchForm.endDate).toISOString().split('T')[0]
  }
  return params
}

function handleSearch() {
  pagination.page = 1
  fetchCourses()
}

function handleReset() {
  searchForm.courseName = ''
  searchForm.category = null
  searchForm.coachId = null
  searchForm.startDate = null
  searchForm.endDate = null
  pagination.page = 1
  fetchCourses()
}

function handleAdd() {
  isEdit.value = false
  currentId.value = null
  Object.assign(form, { courseName: '', category: null, coachId: null, startTime: null, endTime: null, capacity: 20, description: '', imageUrl: '' })
  fileList.value = []
  showModal.value = true
}

function handleView(row) {
  currentCourse.value = { ...row }
  showDetailModal.value = true
}

function handleEditFromDetail() {
  showDetailModal.value = false
  handleEdit(currentCourse.value)
}

function handleEdit(row) {
  isEdit.value = true
  currentId.value = row.id
  Object.assign(form, {
    courseName: row.courseName,
    category: row.category,
    coachId: row.coachId,
    startTime: row.startTime ? new Date(row.startTime).getTime() : null,
    endTime: row.endTime ? new Date(row.endTime).getTime() : null,
    capacity: row.capacity,
    description: row.description || '',
    imageUrl: row.imageUrl || ''
  })
  // 设置图片文件列表
  if (row.imageUrl) {
    fileList.value = [{
      id: 'existing',
      name: '课程图片',
      status: 'finished',
      url: row.imageUrl
    }]
  } else {
    fileList.value = []
  }
  showModal.value = true
}

// 上传完成回调
function handleUploadFinish({ file, event }) {
  try {
    const response = JSON.parse(event.target.response)
    if (response.code === 200) {
      form.imageUrl = response.data.fileUrl
      message.success('图片上传成功')
    } else {
      message.error(response.message || '上传失败')
      // 上传失败时清空文件列表
      fileList.value = []
    }
  } catch (error) {
    console.error('解析上传响应失败:', error)
    message.error('上传响应解析失败')
    fileList.value = []
  }
}

// 上传失败回调
function handleUploadError({ file, event }) {
  message.error('图片上传失败')
}

// 移除图片回调
function handleUploadRemove({ file, fileList: newFileList }) {
  form.imageUrl = ''
  fileList.value = newFileList
}

function formatDateTimeLocal(date) {
  if (!date) return null
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    submitting.value = true

    const data = {
      ...form,
      startTime: formatDateTimeLocal(form.startTime),
      endTime: formatDateTimeLocal(form.endTime)
    }

    if (isEdit.value) {
      await updateCourse(currentId.value, data)
      message.success('更新成功')
    } else {
      await createCourse(data)
      message.success('创建成功')
    }

    showModal.value = false
    fetchCourses()
  } catch (error) {
    message.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

function handleDelete(row) {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除课程 "${row.courseName}" 吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteCourse(row.id)
        message.success('删除成功')
        fetchCourses()
      } catch (error) {
        message.error('删除失败')
      }
    }
  })
}

function formatTime(time) {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

function getCategoryLabel(category) {
  const item = categoryOptions.find(c => c.value === category)
  return item?.label || category
}

function getCategoryType(category) {
  const typeMap = {
    'YOGA': 'success',
    'HIIT': 'error',
    'STRENGTH': 'warning',
    'SPINNING': 'info'
  }
  return typeMap[category] || 'default'
}
</script>

<style scoped>
.admin-courses {
  padding: 0;
}

/* 课程详情样式 */
.course-detail {
  padding: 8px;
}

.course-header {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.course-image-section {
  flex-shrink: 0;
}

.no-image-placeholder {
  width: 200px;
  height: 150px;
  background: #f5f5f5;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
  gap: 8px;
}

.course-basic-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.course-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.course-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 14px;
}

/* 教练信息样式 */
.coach-section {
  margin: 16px 0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.coach-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 12px;
}

.coach-avatar {
  flex-shrink: 0;
}

.no-avatar {
  width: 80px;
  height: 80px;
  background: #e0e0e0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.coach-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.coach-name {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.coach-label {
  font-size: 14px;
  color: #999;
}

/* 课程描述样式 */
.description-section {
  margin: 16px 0;
}

.description-content {
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  color: #666;
  line-height: 1.6;
  min-height: 60px;
}

/* 底部操作按钮 */
.detail-actions {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding-top: 16px;
}
</style>
