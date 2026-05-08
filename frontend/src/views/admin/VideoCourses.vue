<template>
  <div class="admin-video-courses">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>视频课程管理</span>
        </div>
      </template>

      <el-card shadow="never" style="margin-bottom: 16px;">
        <el-row justify="space-between" align="middle">
          <el-col :span="20">
            <el-space wrap>
              <el-input
                v-model="searchForm.title"
                placeholder="视频标题"
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
                v-model="searchForm.status"
                placeholder="状态"
                clearable
                style="width: 120px"
              >
                <el-option label="上架" :value="1" />
                <el-option label="下架" :value="0" />
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
              新增视频课程
            </el-button>
          </el-col>
        </el-row>
      </el-card>

      <el-table
        :data="courses"
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column label="封面" width="120" align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.coverUrl"
              :src="row.coverUrl"
              style="width: 80px; height: 60px; border-radius: 4px; object-fit: cover;"
              :preview-src-list="[row.coverUrl]"
            />
            <div v-else class="no-image">无封面</div>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="视频标题" min-width="180" />
        <el-table-column label="分类" width="120">
          <template #default="{ row }">
            <el-tag>{{ row.category || '未分类' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="教练" width="120">
          <template #default="{ row }">
            {{ row.coachName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="时长" width="100" align="center">
          <template #default="{ row }">
            {{ formatDuration(row.durationSeconds) }}
          </template>
        </el-table-column>
        <el-table-column label="难度" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.difficultyLevel" :type="getDifficultyType(row.difficultyLevel)" size="small">
              {{ row.difficultyLevel }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="文件大小" width="100" align="center">
          <template #default="{ row }">
            {{ formatFileSize(row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="观看次数" width="90" align="center">
          <template #default="{ row }">
            {{ row.viewCount || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="排序" width="70" align="center">
          <template #default="{ row }">
            {{ row.sortOrder || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-space>
              <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.itemCount"
          :page-sizes="[5, 10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="showModal" :title="isEdit ? '编辑视频课程' : '新增视频课程'" width="700px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="视频标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入视频标题" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="分类" prop="category">
              <el-select v-model="form.category" :options="categoryOptions" placeholder="请选择分类" style="width: 100%" allow-create filterable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="教练" prop="coachId">
              <el-select v-model="form.coachId" :options="coachOptions" placeholder="请选择教练" clearable style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="8">
            <el-form-item label="难度等级" prop="difficultyLevel">
              <el-select v-model="form.difficultyLevel" placeholder="请选择" style="width: 100%" clearable>
                <el-option label="入门" value="入门" />
                <el-option label="初级" value="初级" />
                <el-option label="中级" value="中级" />
                <el-option label="高级" value="高级" />
                <el-option label="进阶" value="进阶" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="时长(秒)" prop="durationSeconds">
              <el-input-number v-model="form.durationSeconds" :min="0" placeholder="秒" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="排序" prop="sortOrder">
              <el-input-number v-model="form.sortOrder" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 封面上传 -->
        <el-form-item label="封面图片" prop="coverUrl">
          <el-space direction="vertical" alignment="flex-start">
            <el-upload
              :action="uploadUrl"
              :headers="uploadHeaders"
              :data="{ folder: 'video-covers' }"
              name="file"
              accept="image/*"
              :limit="1"
              :file-list="coverFileList"
              list-type="picture-card"
              :before-upload="handleBeforeCoverUpload"
              :on-success="handleCoverUploadSuccess"
              :on-remove="handleCoverUploadRemove"
              :on-error="handleUploadError"
              :class="{ 'hide-upload': coverFileList.length > 0 }"
            >
              <el-icon><Plus /></el-icon>
              <div style="font-size: 12px;">上传封面</div>
            </el-upload>
          </el-space>
        </el-form-item>

        <!-- 视频上传 -->
        <el-form-item label="视频文件" prop="videoUrl">
          <el-space direction="vertical" alignment="flex-start">
            <el-upload
              :action="uploadUrl"
              :headers="uploadHeaders"
              :data="{ folder: 'videos' }"
              name="file"
              accept="video/*"
              :limit="1"
              :file-list="videoFileList"
              :before-upload="handleBeforeVideoUpload"
              :on-success="handleVideoUploadSuccess"
              :on-remove="handleVideoUploadRemove"
              :on-error="handleUploadError"
              :class="{ 'hide-upload': videoFileList.length > 0 }"
            >
              <el-button type="primary">
                <el-icon><Upload /></el-icon>
                选择视频文件
              </el-button>
              <el-text v-if="form.videoUrl" type="success" size="small" style="margin-left: 8px;">
                已上传: {{ form.videoUrl.split('/').pop() }}
              </el-text>
            </el-upload>
          </el-space>
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述" maxlength="200" show-word-limit />
        </el-form-item>

        <el-form-item>
          <el-space>
            <el-button type="primary" :loading="submitting" @click="handleSubmit">提交</el-button>
            <el-button @click="showModal = false">取消</el-button>
          </el-space>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Upload } from '@element-plus/icons-vue'
import { getVideoCourseList, createVideoCourse, updateVideoCourse, deleteVideoCourse, getVideoCourseCategories } from '@/api/videoCourse'
import { getCoachList } from '@/api/coach'
import { getToken } from '@/utils/auth'

const coachOptions = ref([])
const categoryOptions = ref([])
const loading = ref(false)
const submitting = ref(false)
const showModal = ref(false)
const isEdit = ref(false)
const courses = ref([])
const currentId = ref(null)
const formRef = ref(null)

const uploadUrl = '/api/v1/files/upload'
const uploadHeaders = computed(() => ({
  Authorization: 'Bearer ' + getToken()
}))
const coverFileList = ref([])
const videoFileList = ref([])

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0
})

const form = reactive({
  title: '',
  category: null,
  coachId: null,
  difficultyLevel: null,
  durationSeconds: null,
  fileSize: null,
  sortOrder: 0,
  status: 1,
  coverUrl: '',
  videoUrl: '',
  description: ''
})

const searchForm = reactive({
  title: '',
  category: null,
  coachId: null,
  status: null
})

const rules = {
  title: [{ required: true, message: '请输入视频标题', trigger: 'blur' }],
  videoUrl: [{ required: true, message: '请上传视频文件', trigger: 'change' }]
}

function handleSizeChange(size) {
  pagination.pageSize = size
  fetchCourses()
}

function handlePageChange(page) {
  pagination.page = page
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
    const res = await getVideoCourseCategories()
    categoryOptions.value = (res || []).map(c => ({
      label: c,
      value: c
    }))
  } catch (error) {
    console.error('获取分类列表失败', error)
  }
}

async function fetchCourses() {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.page,
      pageSize: pagination.pageSize
    }
    if (searchForm.title) params.title = searchForm.title
    if (searchForm.category) params.category = searchForm.category
    if (searchForm.coachId) params.coachId = searchForm.coachId
    if (searchForm.status !== null && searchForm.status !== '') params.status = searchForm.status
    const res = await getVideoCourseList(params)
    courses.value = res?.records || []
    pagination.itemCount = res?.total || 0
  } catch (error) {
    ElMessage.error('获取视频课程列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchCourses()
}

function handleReset() {
  searchForm.title = ''
  searchForm.category = null
  searchForm.coachId = null
  searchForm.status = null
  pagination.page = 1
  fetchCourses()
}

function handleAdd() {
  isEdit.value = false
  currentId.value = null
  Object.assign(form, {
    title: '',
    category: null,
    coachId: null,
    difficultyLevel: null,
    durationSeconds: null,
    fileSize: null,
    sortOrder: 0,
    status: 1,
    coverUrl: '',
    videoUrl: '',
    description: ''
  })
  coverFileList.value = []
  videoFileList.value = []
  showModal.value = true
}

function handleEdit(row) {
  isEdit.value = true
  currentId.value = row.id
  Object.assign(form, {
    title: row.title,
    category: row.category,
    coachId: row.coachId,
    difficultyLevel: row.difficultyLevel || null,
    durationSeconds: row.durationSeconds || null,
    fileSize: row.fileSize || null,
    sortOrder: row.sortOrder || 0,
    status: row.status,
    coverUrl: row.coverUrl || '',
    videoUrl: row.videoUrl || '',
    description: row.description || ''
  })
  coverFileList.value = row.coverUrl ? [{ name: '封面', url: row.coverUrl }] : []
  videoFileList.value = row.videoUrl ? [{ name: row.videoUrl.split('/').pop() || '视频', url: row.videoUrl }] : []
  showModal.value = true
}

function handleBeforeCoverUpload(file) {
  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('只支持 JPG、PNG、GIF、WebP 格式的图片')
    return false
  }
  const maxSize = 10 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('图片大小不能超过 10MB')
    return false
  }
  return true
}

function handleCoverUploadSuccess(response) {
  if (response.code === 200) {
    form.coverUrl = response.data.fileUrl
    ElMessage.success('封面上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
    coverFileList.value = []
  }
}

function handleCoverUploadRemove() {
  form.coverUrl = ''
  coverFileList.value = []
}

function handleBeforeVideoUpload(file) {
  const allowedTypes = ['video/mp4', 'video/webm', 'video/ogg', 'video/quicktime', 'video/x-msvideo']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('只支持 MP4、WebM、OGG、MOV、AVI 格式的视频')
    return false
  }
  const maxSize = 500 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('视频大小不能超过 500MB')
    return false
  }
  return true
}

function handleVideoUploadSuccess(response) {
  if (response.code === 200) {
    form.videoUrl = response.data.fileUrl
    if (response.data.fileSize) {
      form.fileSize = response.data.fileSize
    }
    ElMessage.success('视频上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
    videoFileList.value = []
  }
}

function handleVideoUploadRemove() {
  form.videoUrl = ''
  videoFileList.value = []
}

function handleUploadError() {
  ElMessage.error('文件上传失败')
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    submitting.value = true

    const data = {
      ...form,
      coachId: form.coachId ? Number(form.coachId) : null,
      durationSeconds: form.durationSeconds ? Number(form.durationSeconds) : null,
      sortOrder: form.sortOrder ? Number(form.sortOrder) : 0,
      status: Number(form.status)
    }

    if (isEdit.value) {
      await updateVideoCourse(currentId.value, data)
      ElMessage.success('更新成功')
    } else {
      await createVideoCourse(data)
      ElMessage.success('创建成功')
    }

    showModal.value = false
    fetchCourses()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(
    `确定要删除视频课程 "${row.title}" 吗？`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteVideoCourse(row.id)
      ElMessage.success('删除成功')
      fetchCourses()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

function formatTime(time) {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

function formatDuration(seconds) {
  if (!seconds) return '-'
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return m > 0 ? `${m}分${s}秒` : `${s}秒`
}

function formatFileSize(bytes) {
  if (!bytes) return '-'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
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
</script>

<style scoped>
.admin-video-courses {
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
  height: 60px;
  background: #f0f0f0;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 12px;
  margin: 0 auto;
}

:deep(.hide-upload .el-upload--picture-card) {
  display: none;
}
</style>
