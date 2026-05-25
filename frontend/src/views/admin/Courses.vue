<template>
  <div class="admin-courses">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>课程管理</span>
        </div>
      </template>

      <!-- 搜索区域 -->
      <el-card shadow="never" style="margin-bottom: 16px;">
        <el-row justify="space-between" align="middle">
          <el-col :span="20">
            <el-space wrap>
              <el-input
                v-model="searchForm.courseName"
                placeholder="课程名称"
                clearable
                style="width: 200px"
                @keyup.enter="handleSearch"
              />
              <el-select
                v-model="searchForm.category"
                :options="categoryOptions"
                placeholder="分类"
                clearable
                style="width: 150px"
              />
              <el-select
                v-model="searchForm.coachId"
                :options="coachOptions"
                placeholder="教练"
                clearable
                style="width: 150px"
              />
              <el-select
                v-model="searchForm.dayOfWeek"
                placeholder="星期"
                clearable
                style="width: 120px"
              >
                <el-option label="周一" :value="1" />
                <el-option label="周二" :value="2" />
                <el-option label="周三" :value="3" />
                <el-option label="周四" :value="4" />
                <el-option label="周五" :value="5" />
                <el-option label="周六" :value="6" />
                <el-option label="周日" :value="7" />
              </el-select>
              <el-button type="primary" @click="handleSearch">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-space>
          </el-col>
          <el-col :span="4" style="text-align: right;">
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              新增课程
            </el-button>
          </el-col>
        </el-row>
      </el-card>

      <el-table
        :data="courses"
        v-loading="loading"
        :row-key="row => row.id"
        stripe
        style="width: 100%"
      >
        <el-table-column label="课程图片" width="120" align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.imageUrl"
              :src="row.imageUrl"
              style="width: 80px; height: 80px; border-radius: 4px; object-fit: cover;"
              :preview-src-list="[row.imageUrl]"
            />
            <div v-else class="no-image">无图片</div>
          </template>
        </el-table-column>
        <el-table-column prop="courseName" label="课程名称" />
        <el-table-column label="分类">
          <template #default="{ row }">
            <el-tag :type="getCategoryType(row.category)">{{ getCategoryLabel(row.category) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="教练头像" width="100" align="center">
          <template #default="{ row }">
            <el-avatar
              v-if="row.coachAvatar"
              :src="row.coachAvatar"
              :size="50"
              shape="circle"
            />
            <div v-else class="no-avatar">无</div>
          </template>
        </el-table-column>
        <el-table-column prop="coachName" label="教练名称" />
        <el-table-column label="上课时间" width="180">
          <template #default="{ row }">
            {{ formatWeekTime(row.dayOfWeek, row.startTime, row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="capacity" label="容量" />
        <el-table-column prop="bookedCount" label="预约数" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-space>
              <el-button size="small" @click="handleView(row)">查看</el-button>
              <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="paginationReactive.page"
          v-model:page-size="paginationReactive.pageSize"
          :total="paginationReactive.itemCount"
          :page-sizes="[5, 10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="showModal" :title="isEdit ? '编辑课程' : '新增课程'" width="700px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="课程名称" prop="courseName">
              <el-input v-model="form.courseName" placeholder="请输入课程名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="category">
              <el-select v-model="form.category" :options="categoryOptions" placeholder="请选择分类" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="教练" prop="coachId">
              <el-select v-model="form.coachId" :options="coachOptions" placeholder="请选择教练" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="容量" prop="capacity">
              <el-input-number v-model="form.capacity" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="星期" prop="dayOfWeek">
              <el-select v-model="form.dayOfWeek" placeholder="请选择星期" style="width: 100%">
                <el-option label="周一" :value="1" />
                <el-option label="周二" :value="2" />
                <el-option label="周三" :value="3" />
                <el-option label="周四" :value="4" />
                <el-option label="周五" :value="5" />
                <el-option label="周六" :value="6" />
                <el-option label="周日" :value="7" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="开始时间" prop="startTime">
              <el-time-picker v-model="form.startTime" placeholder="开始时间" style="width: 100%" value-format="HH:mm:ss" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="结束时间" prop="endTime">
              <el-time-picker v-model="form.endTime" placeholder="结束时间" style="width: 100%" value-format="HH:mm:ss" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 新增字段：难度等级、课程时长、卡路里消耗 -->
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="难度等级" prop="difficultyLevel">
              <el-select v-model="form.difficultyLevel" placeholder="请选择难度等级" style="width: 100%">
                <el-option label="入门" value="入门" />
                <el-option label="初级" value="初级" />
                <el-option label="中级" value="中级" />
                <el-option label="高级" value="高级" />
                <el-option label="进阶" value="进阶" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="课程时长" prop="durationMinutes">
              <el-input-number v-model="form.durationMinutes" :min="10" :max="180" placeholder="分钟" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="卡路里消耗" prop="caloriesMin">
              <el-input-number v-model="form.caloriesMin" :min="50" :max="2000" placeholder="最小值" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="最大卡路里" prop="caloriesMax">
              <el-input-number v-model="form.caloriesMax" :min="50" :max="2000" placeholder="最大值" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
                <el-option label="已取消" :value="0" />
                <el-option label="可预约" :value="1" />
                <el-option label="已满员" :value="2" />
                <el-option label="已结束" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 课程图片上传 -->
        <el-form-item label="课程图片" prop="imageUrl">
          <el-space direction="vertical" alignment="flex-start">
            <el-upload
              :action="uploadUrl"
              :headers="uploadHeaders"
              :data="{ folder: 'courses' }"
              name="file"
              accept="image/*"
              :limit="1"
              :file-list="fileList"
              list-type="picture-card"
              :before-upload="handleBeforeUpload"
              :on-success="handleUploadSuccess"
              :on-remove="handleUploadRemove"
              :on-error="handleUploadError"
              :class="{ 'hide-upload': fileList.length > 0 }"
            >
              <el-icon><Plus /></el-icon>
              <div style="font-size: 12px;">上传图片</div>
            </el-upload>
            <el-text type="info" size="small">支持 JPG、PNG 格式，建议尺寸 800x600</el-text>
          </el-space>
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入描述，支持ai润色..."
            maxlength="100"
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
        <el-form-item>
          <el-space>
            <el-button type="primary" :loading="submitting" @click="handleSubmit">提交</el-button>
            <el-button @click="showModal = false">取消</el-button>
          </el-space>
        </el-form-item>
      </el-form>
    </el-dialog>

    <!-- 课程详情弹窗 -->
    <el-dialog v-model="showDetailModal" title="课程详情" width="700px" destroy-on-close>
      <div class="course-detail" v-if="currentCourse">
        <!-- 课程封面区域 -->
        <div class="course-header">
          <div class="course-image-section">
            <el-image
              v-if="currentCourse.imageUrl"
              :src="currentCourse.imageUrl"
              style="width: 200px; height: 150px; border-radius: 8px; object-fit: cover;"
              :preview-src-list="[currentCourse.imageUrl]"
            />
            <div v-else class="no-image-placeholder">
              <el-icon :size="48"><Picture /></el-icon>
              <span>暂无课程图片</span>
            </div>
          </div>
          <div class="course-basic-info">
            <h2 class="course-title">{{ currentCourse.courseName }}</h2>
            <el-tag :type="getCategoryType(currentCourse.category)" size="large">
              {{ getCategoryLabel(currentCourse.category) }}
            </el-tag>
            <div class="course-meta">
              <div class="meta-item">
                <el-icon><Clock /></el-icon>
                <span>{{ formatWeekTime(currentCourse.dayOfWeek, currentCourse.startTime, currentCourse.endTime) }}</span>
              </div>
              <div class="meta-item">
                <el-icon><User /></el-icon>
                <span>容量 {{ currentCourse.capacity }} 人</span>
              </div>
              <div class="meta-item">
                <el-icon><Calendar /></el-icon>
                <span>已预约 {{ currentCourse.bookedCount || 0 }} 人</span>
              </div>
            </div>
            <!-- 新增字段显示 -->
            <div class="course-extra-info" v-if="currentCourse.difficultyLevel || currentCourse.durationMinutes || currentCourse.caloriesMin">
              <el-tag v-if="currentCourse.difficultyLevel" :type="getDifficultyType(currentCourse.difficultyLevel)" size="small">
                {{ currentCourse.difficultyLevel }}
              </el-tag>
              <span v-if="currentCourse.durationMinutes" class="extra-item">
                <el-icon><Timer /></el-icon>
                {{ currentCourse.durationMinutes }}分钟
              </span>
              <span v-if="currentCourse.caloriesMin && currentCourse.caloriesMax" class="extra-item">
                <el-icon><Aim /></el-icon>
                {{ currentCourse.caloriesMin }}-{{ currentCourse.caloriesMax }}千卡
              </span>
            </div>
          </div>
        </div>

        <el-divider />

        <!-- 教练信息区域 -->
        <div class="coach-section">
          <h3 class="section-title">
            <el-icon color="#409EFF"><UserFilled /></el-icon>
            教练信息
          </h3>
          <div class="coach-card">
            <div class="coach-avatar">
              <el-image
                v-if="currentCourse.coachAvatar"
                :src="currentCourse.coachAvatar"
                style="width: 80px; height: 80px; border-radius: 50%; object-fit: cover;"
              />
              <div v-else class="no-avatar-large">
                <el-icon :size="32"><User /></el-icon>
              </div>
            </div>
            <div class="coach-info">
              <div class="coach-name">{{ currentCourse.coachName || '暂无教练' }}</div>
              <div class="coach-label">主讲教练</div>
            </div>
          </div>
        </div>

        <el-divider />

        <!-- 课程描述区域 -->
        <div class="description-section">
          <h3 class="section-title">
            <el-icon color="#409EFF"><Document /></el-icon>
            课程描述
          </h3>
          <div class="description-content">
            {{ currentCourse.description || '暂无课程描述' }}
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, h, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Clock, User, Calendar, UserFilled, Document, Picture, Timer, Aim } from '@element-plus/icons-vue'
import { getCourseList, createCourse, updateCourse, deleteCourse, getCourseCategories } from '@/api/course'
import { getCoachList } from '@/api/coach'
import { getToken } from '@/utils/auth'
import { polishDescription } from '@/api/ai'
import { MagicStick, RefreshLeft } from '@element-plus/icons-vue'

const coachOptions = ref([])
const categoryOptions = ref([])

const message = ElMessage
const loading = ref(false)
const submitting = ref(false)
const showModal = ref(false)
const showDetailModal = ref(false)
const isEdit = ref(false)
const courses = ref([])
const currentId = ref(null)
const currentCourse = ref(null)
const formRef = ref(null)

const polishing = ref(false)
const originalText = ref('')
const hasPolished = ref(false)

// 上传相关 - 使用代理路径
const uploadUrl = '/api/v1/files/upload'
const uploadHeaders = computed(() => ({
  Authorization: 'Bearer ' + getToken()
}))
const fileList = ref([])

// 上传前校验
function handleBeforeUpload(file) {
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

const paginationReactive = reactive({
  page: 1,
  pageSize: 5,
  itemCount: 0
})

const form = reactive({
  courseName: '',
  category: null,
  coachId: null,
  dayOfWeek: null,
  startTime: null,
  endTime: null,
  capacity: 20,
  description: '',
  imageUrl: '',
  difficultyLevel: null,
  durationMinutes: null,
  caloriesMin: null,
  caloriesMax: null,
  status: 1
})

const searchForm = reactive({
  courseName: '',
  category: null,
  coachId: null,
  dayOfWeek: null
})

const rules = {
  courseName: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  coachId: [{ required: true, message: '请选择教练', trigger: 'change' }],
  dayOfWeek: [{ required: true, message: '请选择星期', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  capacity: [{ required: true, message: '请输入容量', trigger: 'blur' }]
}

function handleSizeChange(size) {
  paginationReactive.pageSize = size
  fetchCourses()
}

function handlePageChange(page) {
  paginationReactive.page = page
  fetchCourses()
}

onMounted(() => {
  fetchCoachList()
  fetchCategories()
  fetchCourses()
})

async function fetchCoachList() {
  try {
    const res = await getCoachList()
    coachOptions.value = (res || []).map(coach => ({
      label: coach.name,
      value: coach.id
    }))
  } catch (error) {
    console.error('获取教练列表失败', error)
  }
}

async function fetchCategories() {
  try {
    const res = await getCourseCategories()
    categoryOptions.value = (res || []).map(category => ({
      label: category,
      value: category
    }))
  } catch (error) {
    console.error('获取课程分类列表失败', error)
  }
}

async function fetchCourses() {
  loading.value = true
  try {
    const params = {
      pageNum: paginationReactive.page,
      pageSize: paginationReactive.pageSize,
      ...buildSearchParams()
    }
    const res = await getCourseList(params)
    courses.value = res?.records || []
    paginationReactive.itemCount = res?.total || 0
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
  if (searchForm.dayOfWeek) {
    params.dayOfWeek = searchForm.dayOfWeek
  }
  return params
}

function handleSearch() {
  paginationReactive.page = 1
  fetchCourses()
}

function handleReset() {
  searchForm.courseName = ''
  searchForm.category = null
  searchForm.coachId = null
  searchForm.dayOfWeek = null
  paginationReactive.page = 1
  fetchCourses()
}

function handleAdd() {
  isEdit.value = false
  currentId.value = null
  Object.assign(form, {
    courseName: '',
    category: null,
    coachId: null,
    dayOfWeek: null,
    startTime: null,
    endTime: null,
    capacity: 20,
    description: '',
    imageUrl: '',
    difficultyLevel: null,
    durationMinutes: null,
    caloriesMin: null,
    caloriesMax: null,
    status: 1
  })
  fileList.value = []
  showModal.value = true
}

function handleView(row) {
  currentCourse.value = { ...row }
  showDetailModal.value = true
}

function handleEdit(row) {
  isEdit.value = true
  currentId.value = row.id
  Object.assign(form, {
    courseName: row.courseName,
    category: row.category,
    coachId: row.coachId,
    dayOfWeek: row.dayOfWeek,
    startTime: row.startTime,
    endTime: row.endTime,
    capacity: row.capacity,
    description: row.description || '',
    imageUrl: row.imageUrl || '',
    difficultyLevel: row.difficultyLevel || null,
    durationMinutes: row.durationMinutes || null,
    caloriesMin: row.caloriesMin || null,
    caloriesMax: row.caloriesMax || null,
    status: row.status !== undefined ? row.status : 1
  })
  // 设置图片文件列表
  if (row.imageUrl) {
    fileList.value = [{
      name: '课程图片',
      url: row.imageUrl
    }]
  } else {
    fileList.value = []
  }
  // 重置润色状态
  hasPolished.value = false
  originalText.value = ''
  polishing.value = false
  showModal.value = true
}

// 上传成功回调
function handleUploadSuccess(response, file) {
  if (response.code === 200) {
    form.imageUrl = response.data.fileUrl
    message.success('图片上传成功')
  } else {
    message.error(response.message || '上传失败')
    fileList.value = []
  }
}

// 上传失败回调
function handleUploadError() {
  message.error('图片上传失败')
}

// 移除图片回调
function handleUploadRemove() {
  form.imageUrl = ''
  fileList.value = []
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    submitting.value = true

    // 转换数据类型以匹配后端要求
    const data = {
      ...form,
      coachId: form.coachId ? Number(form.coachId) : null,
      dayOfWeek: form.dayOfWeek ? Number(form.dayOfWeek) : null,
      capacity: form.capacity ? Number(form.capacity) : null,
      durationMinutes: form.durationMinutes ? Number(form.durationMinutes) : null,
      caloriesMin: form.caloriesMin ? Number(form.caloriesMin) : null,
      caloriesMax: form.caloriesMax ? Number(form.caloriesMax) : null,
      status: form.status !== undefined ? Number(form.status) : 1
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
  ElMessageBox.confirm(
    `确定要删除课程 "${row.courseName}" 吗？`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteCourse(row.id)
      message.success('删除成功')
      fetchCourses()
    } catch (error) {
      message.error('删除失败')
    }
  }).catch(() => {})
}

function formatTime(time) {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

function formatWeekTime(dayOfWeek, startTime, endTime) {
  const weekMap = { 1: '周一', 2: '周二', 3: '周三', 4: '周四', 5: '周五', 6: '周六', 7: '周日' }
  const week = weekMap[dayOfWeek] || ''
  const start = startTime || ''
  const end = endTime || ''
  if (week && start && end) {
    return `${week} ${start}-${end}`
  }
  return week || start || end || '-'
}

function getCategoryLabel(category) {
  const item = categoryOptions.value.find(c => c.value === category)
  return item?.label || category
}

function getCategoryType(category) {
  const typeMap = {
    'YOGA': 'success',      // 瑜伽 - 绿色
    'PILATES': 'primary',   // 普拉提 - 蓝色
    'HIIT': 'danger',       // HIIT - 红色
    'STRENGTH': 'warning',  // 力量训练 - 橙色
    'SPINNING': 'info'      // 动感单车 - 青色
  }
  return typeMap[category] || 'info'
}

function getStatusLabel(status) {
  const labelMap = {
    0: '已取消',
    1: '可预约',
    2: '已满员',
    3: '已结束'
  }
  return labelMap[status] || '未知'
}

function getStatusType(status) {
  const typeMap = {
    0: 'info',      // 已取消 - 灰色
    1: 'success',   // 可预约 - 绿色
    2: 'warning',   // 已满员 - 橙色
    3: 'danger'     // 已结束 - 红色
  }
  return typeMap[status] || 'info'
}

function getDifficultyType(level) {
  const typeMap = {
    '入门': 'success',
    '初级': 'primary',
    '中级': 'warning',
    '高级': 'danger',
    '进阶': 'info'
  }
  return typeMap[level] || 'info'
}

async function handlePolish() {
  const textLength = form.description.trim().length
  
  if (textLength === 0) {
    message.warning('请先输入描述内容')
    return
  }
  
  if (textLength < 3) {
    message.warning('文本内容至少需要3个字')
    return
  }
  
  if (textLength > 100) {
    message.warning('文本长度不能超过100字')
    return
  }
  
  originalText.value = form.description
  polishing.value = true
  hasPolished.value = false
  
  try {
    const response = await polishDescription({ text: form.description })
    form.description = response.polishedText
    hasPolished.value = true
    message.success('润色成功')
  } catch (error) {
    message.error(error.message || '润色失败，请重试')
  } finally {
    polishing.value = false
  }
}

function handleCancelPolish() {
  polishing.value = false
  message.info('已取消润色')
}

function handleRestore() {
  form.description = originalText.value
  hasPolished.value = false
  message.success('已恢复原始文本')
}
</script>

<style scoped>
.admin-courses {
  padding: 0;
}

.card-header {
  font-weight: bold;
  font-size: 16px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.no-image {
  width: 80px;
  height: 80px;
  background: #f0f0f0;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 12px;
  margin: 0 auto;
}

.no-avatar {
  width: 50px;
  height: 50px;
  background: #f0f0f0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 12px;
  margin: 0 auto;
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

.course-extra-info {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #e0e0e0;
}

.extra-item {
  display: flex;
  align-items: center;
  gap: 4px;
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
  border-radius: 8px;
}

.no-avatar-large {
  width: 80px;
  height: 80px;
  background: #e0e0e0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
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
  color: #666;
}

/* 描述区域样式 */
.description-section {
  margin: 16px 0;
}

.description-content {
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  color: #666;
  line-height: 1.6;
  white-space: pre-wrap;
}

/* 上传成功后隐藏上传按钮 */
:deep(.hide-upload .el-upload--picture-card) {
  display: none;
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
