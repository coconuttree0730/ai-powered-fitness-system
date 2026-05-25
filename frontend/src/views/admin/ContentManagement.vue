<template>
  <div class="content-management-page">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6" v-for="stat in stats" :key="stat.title">
        <el-card class="stat-card" :body-style="{ padding: '20px' }">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-title">{{ stat.title }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 子模块标签页 -->
    <el-card>
      <el-tabs v-model="activeTab" type="border-card" @tab-change="handleTabChange">
        <!-- 轮播图管理 -->
        <el-tab-pane name="banner">
          <template #label>
            <span class="tab-label">
              <el-icon><Picture /></el-icon>轮播图管理
            </span>
          </template>
          <div class="tab-content">
            <el-row :gutter="20" align="middle" class="tab-toolbar">
              <el-col :span="18">
                <el-space>
                  <el-input
                    v-model="bannerSearch.title"
                    placeholder="搜索标题"
                    clearable
                    style="width: 200px"
                  />
                  <el-select v-model="bannerSearch.status" placeholder="全部状态" clearable style="width: 120px">
                    <el-option label="显示中" :value="1" />
                    <el-option label="已隐藏" :value="0" />
                  </el-select>
                  <el-button type="primary" @click="fetchBannerData">
                    <el-icon><Search /></el-icon>搜索
                  </el-button>
                </el-space>
              </el-col>
              <el-col :span="6" style="text-align: right">
                <el-button type="danger" @click="handleBatchDelete" :disabled="selectedBanners.length === 0" style="margin-right: 10px">
                  <el-icon><Delete /></el-icon>批量删除
                </el-button>
                <el-button type="primary" @click="handleAddBanner">
                  <el-icon><Plus /></el-icon>新增轮播图
                </el-button>
              </el-col>
            </el-row>

            <el-table :data="filteredBannerData" v-loading="loading" stripe @selection-change="handleSelectionChange">
              <el-table-column type="selection" width="55" />
              <el-table-column type="index" width="50" />
              <el-table-column label="轮播图" width="200">
                <template #default="{ row }">
                  <el-image
                    :src="row.imageUrl"
                    :preview-src-list="[row.imageUrl]"
                    fit="cover"
                    style="width: 180px; height: 80px; border-radius: 4px"
                  />
                </template>
              </el-table-column>
              <el-table-column prop="title" label="标题" min-width="150" />
              <el-table-column prop="subtitle" label="副标题" min-width="150" show-overflow-tooltip />
              <el-table-column prop="link" label="链接" min-width="150" show-overflow-tooltip />
              <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-switch
                    v-model="row.status"
                    :active-value="1"
                    :inactive-value="0"
                    @change="handleBannerStatusChange(row)"
                  />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" link @click="handleEditBanner(row)">
                    <el-icon><Edit /></el-icon>编辑
                  </el-button>
                  <el-button type="danger" link @click="handleDeleteBanner(row)">
                    <el-icon><Delete /></el-icon>删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <!-- 公告管理 -->
        <el-tab-pane name="notice">
          <template #label>
            <span class="tab-label">
              <el-icon><Bell /></el-icon>公告管理
            </span>
          </template>
          <div class="tab-content">
            <el-row :gutter="20" align="middle" class="tab-toolbar">
              <el-col :span="18">
                <el-space>
                  <el-input
                    v-model="noticeSearch.title"
                    placeholder="搜索标题"
                    clearable
                    style="width: 200px"
                  />
                  <el-select v-model="noticeSearch.type" placeholder="全部类型" clearable style="width: 120px">
                    <el-option label="系统公告" value="SYSTEM" />
                    <el-option label="活动通知" value="ACTIVITY" />
                    <el-option label="重要提醒" value="IMPORTANT" />
                  </el-select>
                  <el-button type="primary" @click="fetchNoticeData">
                    <el-icon><Search /></el-icon>搜索
                  </el-button>
                </el-space>
              </el-col>
              <el-col :span="6" style="text-align: right">
                <el-button type="primary" @click="handleAddNotice">
                  <el-icon><Plus /></el-icon>新增公告
                </el-button>
              </el-col>
            </el-row>

            <el-table :data="noticeData" v-loading="loading" stripe>
              <el-table-column type="index" width="50" />
              <el-table-column prop="title" label="公告标题" min-width="200" />
              <el-table-column prop="type" label="类型" width="100">
                <template #default="{ row }">
                  <el-tag :type="getNoticeTypeTag(row.type)">{{ getNoticeTypeLabel(row.type) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="publishTime" label="发布时间" width="160" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'info'">
                    {{ row.status === 1 ? '已发布' : '草稿' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="viewCount" label="浏览量" width="100" align="center" />
              <el-table-column label="操作" width="200" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" link @click="handleEditNotice(row)">
                    <el-icon><Edit /></el-icon>编辑
                  </el-button>
                  <el-button type="success" link v-if="row.status === 0" @click="handlePublishNotice(row)">
                    <el-icon><Promotion /></el-icon>发布
                  </el-button>
                  <el-button type="warning" link v-else @click="handleUnpublishNotice(row)">
                    <el-icon><Promotion /></el-icon>下架
                  </el-button>
                  <el-button type="danger" link @click="handleDeleteNotice(row)">
                    <el-icon><Delete /></el-icon>删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>


      </el-tabs>
    </el-card>

    <!-- 轮播图弹窗 -->
    <el-dialog
      v-model="bannerDialogVisible"
      :title="isEdit ? '编辑轮播图' : '新增轮播图'"
      width="600px"
    >
      <el-form ref="bannerFormRef" :model="bannerForm" :rules="bannerRules" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="bannerForm.title" placeholder="请输入轮播图标题" />
        </el-form-item>
        <el-form-item label="副标题" prop="subtitle">
          <el-input v-model="bannerForm.subtitle" placeholder="请输入副标题" />
        </el-form-item>
        <el-form-item label="图片" prop="imageUrl">
          <el-upload
            class="banner-uploader"
            action="/api/v1/files/upload"
            :data="{ folder: 'banner' }"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleBannerUploadSuccess"
            :on-error="handleBannerUploadError"
            :before-upload="beforeBannerUpload"
            accept="image/jpeg,image/png,image/gif,image/webp"
          >
            <img v-if="bannerForm.imageUrl" :src="bannerForm.imageUrl" class="banner-preview" />
            <div v-else class="banner-uploader-placeholder">
              <el-icon class="banner-uploader-icon"><Plus /></el-icon>
              <div class="upload-text">点击上传图片</div>
            </div>
          </el-upload>
          <div class="upload-tip">建议尺寸 800x1000，支持 JPG、PNG、GIF、WebP 格式，最大 10MB</div>
        </el-form-item>
        <el-form-item label="链接" prop="link">
          <el-input v-model="bannerForm.link" placeholder="请输入跳转链接（可选）" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="bannerForm.sortOrder" :min="0" :max="999" />
          <span class="form-tip">数字越小排序越靠前</span>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="bannerForm.status">
            <el-radio :value="1">显示</el-radio>
            <el-radio :value="0">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bannerDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitBanner" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 公告弹窗 -->
    <el-dialog
      v-model="noticeDialogVisible"
      :title="isEdit ? '编辑公告' : '新增公告'"
      width="700px"
    >
      <el-form ref="noticeFormRef" :model="noticeForm" :rules="noticeRules" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="noticeForm.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="noticeForm.type" placeholder="请选择类型" style="width: 100%">
            <el-option label="系统公告" value="SYSTEM" />
            <el-option label="活动通知" value="ACTIVITY" />
            <el-option label="重要提醒" value="IMPORTANT" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="noticeForm.content"
            type="textarea"
            :rows="6"
            placeholder="请输入公告内容"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="noticeForm.status">
            <el-radio :value="0">草稿</el-radio>
            <el-radio :value="1">立即发布</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="发布时间" prop="publishTime" v-if="noticeForm.status === 0">
          <el-date-picker
            v-model="noticeForm.publishTime"
            type="datetime"
            placeholder="选择定时发布时间"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="noticeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitNotice" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Picture, Bell, Search, Plus, Edit, Delete,
  Promotion
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import {
  getAllBanners,
  createBanner,
  updateBanner,
  deleteBanner,
  deleteBanners,
  updateBannerStatus
} from '@/api/banner'
import {
  getAllAnnouncements,
  createAnnouncement,
  updateAnnouncement,
  deleteAnnouncement,
  publishAnnouncement,
  unpublishAnnouncement
} from '@/api/announcement'

// 获取认证信息
const authStore = useAuthStore()
const uploadHeaders = computed(() => ({
  Authorization: authStore.accessToken ? `Bearer ${authStore.accessToken}` : ''
}))

// 统计数据
const stats = ref([
  { title: '轮播图数量', value: 0 },
  { title: '公告数量', value: 12 }
])

// 当前标签页
const activeTab = ref('banner')
const loading = ref(false)

// ========== 轮播图管理 ==========
const bannerSearch = reactive({ title: '', status: '' })
const bannerData = ref([])
const selectedBanners = ref([])
const bannerDialogVisible = ref(false)
const bannerFormRef = ref(null)
const bannerForm = reactive({
  id: null,
  title: '',
  subtitle: '',
  imageUrl: '',
  link: '',
  sortOrder: 0,
  status: 1
})
const bannerRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  imageUrl: [{ required: true, message: '请上传图片', trigger: 'change' }],
  sortOrder: [{ required: true, message: '请输入排序', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// 过滤后的轮播图数据
const filteredBannerData = computed(() => {
  return bannerData.value.filter(item => {
    const matchTitle = !bannerSearch.title || item.title.toLowerCase().includes(bannerSearch.title.toLowerCase())
    const matchStatus = bannerSearch.status === '' || item.status === bannerSearch.status
    return matchTitle && matchStatus
  })
})

// 获取轮播图数据
async function fetchBannerData() {
  loading.value = true
  try {
    const res = await getAllBanners()
    bannerData.value = res || []
    // 更新统计
    stats.value[0].value = bannerData.value.length
  } catch (error) {
    ElMessage.error('获取轮播图数据失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 选择变化
function handleSelectionChange(selection) {
  selectedBanners.value = selection
}

// 新增轮播图
function handleAddBanner() {
  isEdit.value = false
  Object.assign(bannerForm, {
    id: null, title: '', subtitle: '', imageUrl: '', link: '', sortOrder: 0, status: 1
  })
  bannerDialogVisible.value = true
}

// 编辑轮播图
function handleEditBanner(row) {
  isEdit.value = true
  Object.assign(bannerForm, {
    id: row.id,
    title: row.title,
    subtitle: row.subtitle,
    imageUrl: row.imageUrl,
    link: row.link,
    sortOrder: row.sortOrder,
    status: row.status
  })
  bannerDialogVisible.value = true
}

// 删除轮播图
async function handleDeleteBanner(row) {
  try {
    await ElMessageBox.confirm(`确定要删除轮播图 "${row.title}" 吗？`, '提示', { type: 'warning' })
    await deleteBanner(row.id)
    ElMessage.success('删除成功')
    fetchBannerData()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error('删除失败')
      console.error(error)
    }
  }
}

// 批量删除
async function handleBatchDelete() {
  if (selectedBanners.value.length === 0) {
    ElMessage.warning('请选择要删除的轮播图')
    return
  }
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedBanners.value.length} 个轮播图吗？`, '提示', { type: 'warning' })
    const ids = selectedBanners.value.map(item => item.id)
    await deleteBanners(ids)
    ElMessage.success('批量删除成功')
    fetchBannerData()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error('批量删除失败')
      console.error(error)
    }
  }
}

// 状态变化
async function handleBannerStatusChange(row) {
  try {
    await updateBannerStatus(row.id, row.status)
    ElMessage.success(`轮播图已${row.status === 1 ? '显示' : '隐藏'}`)
  } catch (error) {
    ElMessage.error('状态更新失败')
    console.error(error)
    // 恢复状态
    row.status = row.status === 1 ? 0 : 1
  }
}

// 上传成功
function handleBannerUploadSuccess(res) {
  if (res.code === 200 && res.data) {
    bannerForm.imageUrl = res.data.fileUrl || res.data.url
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(res.message || '上传失败')
  }
}

// 上传失败
function handleBannerUploadError() {
  ElMessage.error('图片上传失败')
}

// 上传前校验
function beforeBannerUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isImage) {
    ElMessage.error('请上传图片文件')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过10MB')
    return false
  }
  return true
}

// 提交表单
async function handleSubmitBanner() {
  bannerFormRef.value?.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      if (isEdit.value) {
        await updateBanner(bannerForm.id, bannerForm)
        ElMessage.success('编辑成功')
      } else {
        await createBanner(bannerForm)
        ElMessage.success('新增成功')
      }
      bannerDialogVisible.value = false
      fetchBannerData()
    } catch (error) {
      ElMessage.error(isEdit.value ? '编辑失败' : '新增失败')
      console.error(error)
    } finally {
      submitting.value = false
    }
  })
}

// ========== 公告管理 ==========
const noticeSearch = reactive({ title: '', type: '' })
const noticeData = ref([])
const noticeDialogVisible = ref(false)
const noticeFormRef = ref(null)
const noticeForm = reactive({
  id: null,
  title: '',
  type: 'SYSTEM',
  content: '',
  publishTime: null,
  status: 0
})
const noticeRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

const noticeTypeMap = {
  SYSTEM: { label: '系统公告', type: 'primary' },
  ACTIVITY: { label: '活动通知', type: 'success' },
  IMPORTANT: { label: '重要提醒', type: 'danger' }
}

function getNoticeTypeLabel(type) {
  return noticeTypeMap[type]?.label || type
}

function getNoticeTypeTag(type) {
  return noticeTypeMap[type]?.type || ''
}

async function fetchNoticeData() {
  loading.value = true
  try {
    const res = await getAllAnnouncements()
    noticeData.value = res || []
    // 更新统计
    stats.value[1].value = noticeData.value.length
  } catch (error) {
    ElMessage.error('获取公告数据失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

function handleAddNotice() {
  isEdit.value = false
  Object.assign(noticeForm, {
    id: null, title: '', type: 'SYSTEM', content: '', publishTime: null, status: 0
  })
  noticeDialogVisible.value = true
}

function handleEditNotice(row) {
  isEdit.value = true
  Object.assign(noticeForm, {
    id: row.id,
    title: row.title,
    type: row.type,
    content: row.content,
    publishTime: row.publishTime,
    status: row.status
  })
  noticeDialogVisible.value = true
}

async function handleDeleteNotice(row) {
  try {
    await ElMessageBox.confirm(`确定要删除公告 "${row.title}" 吗？`, '提示', { type: 'warning' })
    await deleteAnnouncement(row.id)
    ElMessage.success('删除成功')
    fetchNoticeData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
      console.error(error)
    }
  }
}

async function handlePublishNotice(row) {
  try {
    await ElMessageBox.confirm(`确定要发布公告 "${row.title}" 吗？`, '提示', { type: 'info' })
    await publishAnnouncement(row.id)
    ElMessage.success('发布成功')
    fetchNoticeData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('发布失败')
      console.error(error)
    }
  }
}

async function handleUnpublishNotice(row) {
  try {
    await ElMessageBox.confirm(`确定要下架公告 "${row.title}" 吗？`, '提示', { type: 'warning' })
    await unpublishAnnouncement(row.id)
    ElMessage.success('下架成功')
    fetchNoticeData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('下架失败')
      console.error(error)
    }
  }
}

async function handleSubmitNotice() {
  noticeFormRef.value?.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      if (isEdit.value) {
        await updateAnnouncement(noticeForm.id, noticeForm)
        ElMessage.success('编辑成功')
      } else {
        await createAnnouncement(noticeForm)
        ElMessage.success('新增成功')
      }
      noticeDialogVisible.value = false
      fetchNoticeData()
    } catch (error) {
      ElMessage.error(isEdit.value ? '编辑失败' : '新增失败')
      console.error(error)
    } finally {
      submitting.value = false
    }
  })
}

// 通用
const isEdit = ref(false)
const submitting = ref(false)

function handleTabChange() {
  switch (activeTab.value) {
    case 'banner':
      fetchBannerData()
      break
    case 'notice':
      fetchNoticeData()
      break
  }
}

onMounted(() => {
  fetchBannerData()
})
</script>

<style scoped>
.content-management-page {
  padding: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
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

.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
}

.tab-content {
  padding: 20px 0;
}

.tab-toolbar {
  margin-bottom: 20px;
}

/* 轮播图上传 */
.banner-uploader {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
  width: 100%;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--el-fill-color-light);
}

.banner-uploader:hover {
  border-color: var(--el-color-primary);
}

.banner-uploader-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.banner-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.upload-text {
  font-size: 14px;
  color: #8c939d;
}

.banner-preview {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-left: 10px;
}


</style>
