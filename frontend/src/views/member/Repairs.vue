<template>
  <div class="repairs-page">
    <!-- 报修表单 -->
    <div class="card-section">
      <div class="section-header">
        <h3 class="section-title">提交报修</h3>
      </div>
      <n-form ref="formRef" :model="form" :rules="rules" :label-placement="isMobile ? 'top' : 'left'" :label-width="isMobile ? undefined : 100">
        <n-form-item label="器械编号" path="equipmentId">
          <n-select
            v-model:value="form.equipmentId"
            :options="equipmentOptions"
            placeholder="请选择报修的器械"
            :loading="equipmentLoading"
            filterable
            clearable
            size="large"
          />
        </n-form-item>

        <n-form-item label="问题描述" path="description">
          <n-input
            v-model:value="form.description"
            type="textarea"
            placeholder="请详细描述器材的问题情况..."
            :rows="4"
            size="large"
          />
        </n-form-item>

        <n-form-item label="上传图片">
          <div class="upload-container">
            <template v-if="isMobile">
              <n-button size="large" dashed type="primary" @click="showUploadOptions = true" style="width: 100%; max-width: 280px;">
                <template #icon>
                  <n-icon><camera-icon /></n-icon>
                </template>
                添加图片
              </n-button>
              <input
                ref="cameraInputRef"
                type="file"
                accept="image/*"
                capture="environment"
                style="display: none"
                @change="handleCameraCapture"
              />
              <input
                ref="albumInputRef"
                type="file"
                accept="image/*"
                style="display: none"
                @change="handleAlbumCapture"
              />
              <div v-if="fileList.length > 0" class="mobile-image-preview">
                <div v-for="(item, idx) in fileList" :key="item.id" class="mobile-image-item">
                  <img :src="item.thumbnailUrl || item.url" alt="预览图" />
                  <div class="mobile-image-delete" @click="removeImage(idx)">
                    <n-icon><close-icon /></n-icon>
                  </div>
                </div>
              </div>
            </template>
            <template v-else>
              <n-upload
                v-model:file-list="fileList"
                action="/api/v1/files/upload?folder=repairs"
                list-type="image-card"
                :max="5"
                :headers="uploadHeaders"
                accept="image/jpeg,image/png,image/jpg"
                @change="handleUploadChange"
                @finish="handleUploadFinish"
                @error="handleUploadError"
              >
                <n-button size="large" dashed style="width: 120px; height: 120px;">
                  <template #icon>
                    <n-icon><camera-icon /></n-icon>
                  </template>
                  上传图片
                </n-button>
              </n-upload>
            </template>
          </div>
          <p class="upload-tip">最多上传5张图片，支持 jpg/png 格式，单张不超过 5MB</p>
        </n-form-item>

        <n-form-item>
          <n-button
            type="primary"
            size="large"
            :loading="submitting"
            @click="handleSubmit"
            style="width: 200px;"
          >
            提交报修
          </n-button>
        </n-form-item>
      </n-form>
    </div>

    <!-- 报修记录 -->
    <div class="card-section">
      <div class="section-header" :class="{ 'mobile-header': isMobile }">
        <h3 class="section-title">报修记录</h3>
        <n-radio-group v-model:value="filterStatus" size="small">
          <n-radio-button value="all">全部</n-radio-button>
          <n-radio-button value="0">待处理</n-radio-button>
          <n-radio-button value="1">处理中</n-radio-button>
          <n-radio-button value="2">已完成</n-radio-button>
          <n-radio-button value="3">已关闭</n-radio-button>
        </n-radio-group>
      </div>
      
      <!-- 桌面端表格 -->
      <div v-if="!isMobile" class="table-wrapper">
        <n-data-table
          :columns="columns"
          :data="filteredRepairs"
          :loading="loading"
          :pagination="pagination"
          :bordered="false"
          class="repair-table"
        />
      </div>
      
      <!-- 移动端卡片列表 -->
      <div v-else class="mobile-repair-list">
        <n-spin :show="loading">
          <n-empty v-if="filteredRepairs.length === 0" description="暂无报修记录" />
          <div v-else class="repair-cards">
            <div 
              v-for="item in filteredRepairs" 
              :key="item.id" 
              class="repair-card"
              @click="viewDetail(item)"
            >
              <div class="repair-card-header">
                <div class="repair-equipment">
                  <n-icon :component="ConstructOutline" size="16" />
                  <span>{{ item.equipmentNo || '未知器械' }}</span>
                </div>
                <n-tag :type="getStatusType(item.status)" size="small" round>
                  {{ getStatusText(item.status) }}
                </n-tag>
              </div>
              <div class="repair-card-body">
                <p class="repair-desc">{{ item.description }}</p>
                <span class="repair-time">{{ formatTime(item.createTime) }}</span>
              </div>
              <div class="repair-card-footer" v-if="item.status === 0" @click.stop>
                <n-button type="error" size="small" @click="handleCancel(item.id)">
                  取消报修
                </n-button>
              </div>
            </div>
          </div>
          <!-- 移动端分页 -->
          <div class="mobile-pagination" v-if="filteredRepairs.length > 0">
            <n-pagination
              v-model:page="pagination.page"
              :page-count="Math.ceil(filteredRepairs.length / pagination.pageSize)"
              :page-slot="5"
              size="small"
            />
          </div>
        </n-spin>
      </div>
    </div>

    <!-- 报修详情弹窗 -->
    <n-modal v-model:show="showDetailModal" preset="card" style="width: 700px" :show-header="false">
      <div v-if="selectedRepair" class="repair-detail">
        <div class="detail-header">
          <h3>报修详情</h3>
          <n-tag :type="getStatusType(selectedRepair.status)" size="large" round>
            {{ getStatusText(selectedRepair.status) }}
          </n-tag>
        </div>

        <n-divider />

        <div class="detail-row">
          <span class="detail-label">报修时间</span>
          <span class="detail-value">{{ formatTime(selectedRepair.createTime) }}</span>
        </div>
        <div v-if="selectedRepair.equipmentNo || selectedRepair.equipmentName" class="detail-row">
          <span class="detail-label">报修器械</span>
          <span class="detail-value">{{ selectedRepair.equipmentNo }} - {{ selectedRepair.equipmentName }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">问题描述</span>
          <span class="detail-value">{{ selectedRepair.description }}</span>
        </div>

        <div v-if="selectedRepair.imageUrls && selectedRepair.imageUrls.length" class="detail-images">
          <span class="detail-label">问题图片</span>
          <div class="image-list">
            <div v-for="(img, idx) in selectedRepair.imageUrls" :key="idx" class="image-item">
              <img :src="img" alt="问题图片" @click="previewImage(img)" />
            </div>
          </div>
        </div>

        <div v-if="selectedRepair.handleRemark" class="detail-row">
          <span class="detail-label">处理备注</span>
          <span class="detail-value">{{ selectedRepair.handleRemark }}</span>
        </div>

        <n-divider />

        <div class="repair-timeline">
          <h4>处理进度</h4>
          <n-timeline v-if="selectedRepair.records && selectedRepair.records.length">
            <n-timeline-item
              v-for="(item, idx) in selectedRepair.records"
              :key="idx"
              :type="getRecordType(item.recordType)"
              :title="getRecordTitle(item)"
              :content="item.content"
              :time="formatTime(item.createTime)"
            />
          </n-timeline>
          <n-empty v-else description="暂无处理记录" />
        </div>
      </div>
    </n-modal>

    <!-- 图片预览 -->
    <n-image-preview
      v-model:show="showImagePreview"
      :src="previewImageUrl"
    />

    <!-- 移动端上传选项弹窗 -->
    <n-modal v-model:show="showUploadOptions" preset="card" style="width: 320px; border-radius: 16px;" :bordered="false" :show-header="false">
      <div class="upload-options">
        <div class="upload-option" @click="handleUploadClick('camera')">
          <n-icon :component="CameraIcon" size="24" />
          <span>拍照</span>
        </div>
        <div class="upload-option" @click="handleUploadClick('album')">
          <n-icon :component="ImageIcon" size="24" />
          <span>从相册选择</span>
        </div>
        <div class="upload-option upload-option-cancel" @click="showUploadOptions = false">
          <span>取消</span>
        </div>
      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, h } from 'vue'
import { NTag, NButton, useMessage, NIcon, NTimeline, NTimelineItem, NEmpty, useDialog } from 'naive-ui'
import { Camera as CameraIcon, Image as ImageIcon, Close as CloseIcon, ConstructOutline } from '@vicons/ionicons5'
import { submitRepair, getMyRepairs, cancelRepair, getActiveEquipmentList } from '@/api/equipment'
import { uploadFile } from '@/api/file'

const message = useMessage()
const dialog = useDialog()
const loading = ref(false)

// 响应式状态 - 使用更大断点避免横屏误判，同时结合触摸设备检测
const windowWidth = ref(window.innerWidth)
const isTouchDevice = ref('ontouchstart' in window || navigator.maxTouchPoints > 0)
const isMobile = computed(() => isTouchDevice.value || windowWidth.value < 900)

// 监听窗口大小变化
function handleResize() {
  windowWidth.value = window.innerWidth
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
const submitting = ref(false)
const showDetailModal = ref(false)
const showImagePreview = ref(false)
const previewImageUrl = ref('')
const selectedRepair = ref(null)
const filterStatus = ref('all')
const fileList = ref([])
const repairs = ref([])

const formRef = ref(null)
const form = reactive({
  equipmentId: null,
  description: ''
})

const uploadedUrls = ref([])
const equipmentOptions = ref([])
const equipmentLoading = ref(false)

// 上传请求头（携带认证token）
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken')
  return token ? { Authorization: `Bearer ${token}` } : {}
})
const rules = {
  description: [{ required: true, message: '请描述问题', trigger: 'blur' }]
}
const pagination = reactive({
  page: 1,
  pageSize: 10,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  onChange: (page) => {
    pagination.page = page
  },
  onUpdatePageSize: (pageSize) => {
    pagination.pageSize = pageSize
    pagination.page = 1
  }
})

const filteredRepairs = computed(() => {
  console.log('filteredRepairs计算, repairs:', repairs.value, 'filterStatus:', filterStatus.value)
  if (!repairs.value || !Array.isArray(repairs.value)) {
    console.log('repairs为空或不是数组')
    return []
  }
  if (filterStatus.value === 'all') return repairs.value
  return repairs.value.filter(r => r.status === parseInt(filterStatus.value))
})

const columns = [
  {
    title: '器械编号',
    key: 'equipmentNo',
    width: 150,
    render: (row) => row.equipmentNo || '-'
  },
  {
    title: '问题描述',
    key: 'description',
    ellipsis: { tooltip: true },
    width: 350
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) => {
      const types = { 0: 'warning', 1: 'info', 2: 'success', 3: 'default' }
      return h(NTag, { type: types[row.status] || 'default', size: 'small' }, () => getStatusText(row.status))
    }
  },
  {
    title: '报修时间',
    key: 'createTime',
    width: 160,
    render: (row) => formatTime(row.createTime)
  },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    render: (row) => h('div', { class: 'action-buttons' }, [
      h(NButton, {
        type: 'primary',
        size: 'small',
        onClick: () => viewDetail(row)
      }, () => '查看详情'),
      row.status === 0 ? h(NButton, {
        type: 'error',
        size: 'small',
        style: { marginLeft: '8px' },
        onClick: () => handleCancel(row.id)
      }, () => '取消') : null
    ].filter(Boolean))
  }
]

function getStatusType(status) {
  const map = { 0: 'warning', 1: 'info', 2: 'success', 3: 'default' }
  return map[status] || 'default'
}

function getStatusText(status) {
  const map = { 0: '待处理', 1: '处理中', 2: '已完成', 3: '已关闭' }
  return map[status] || '未知'
}

function getRecordType(recordType) {
  const map = { 1: 'success', 2: 'info', 3: 'warning', 4: 'error' }
  return map[recordType] || 'default'
}

function getRecordTitle(item) {
  const map = { 1: '提交报修', 2: '状态变更', 3: '处理备注', 4: '取消报修' }
  return map[item.recordType] || '处理记录'
}

function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const cameraInputRef = ref(null)
const albumInputRef = ref(null)
const showUploadOptions = ref(false)
const uploadingCamera = ref(false)

function handleUploadClick(type) {
  showUploadOptions.value = false
  if (fileList.value.length >= 5) {
    message.warning('最多只能上传5张图片')
    return
  }
  if (type === 'camera') {
    cameraInputRef.value?.click()
  } else {
    albumInputRef.value?.click()
  }
}

function removeImage(idx) {
  const item = fileList.value[idx]
  if (item && item.url) {
    uploadedUrls.value = uploadedUrls.value.filter(u => u !== item.url)
  }
  if (item && item.thumbnailUrl && item.thumbnailUrl.startsWith('blob:')) {
    URL.revokeObjectURL(item.thumbnailUrl)
  }
  fileList.value.splice(idx, 1)
}

async function handleCameraCapture(event) {
  await handleFileUpload(event, '拍照')
}

async function handleAlbumCapture(event) {
  await handleFileUpload(event, '相册选择')
}

async function handleFileUpload(event, source) {
  const file = event.target.files[0]
  if (!file) return

  if (fileList.value.length >= 5) {
    message.warning('最多只能上传5张图片')
    event.target.value = ''
    return
  }

  if (!file.type.match(/image\/(jpeg|png|jpg)/i)) {
    message.error('仅支持 jpg/png 格式的图片')
    event.target.value = ''
    return
  }

  if (file.size > 5 * 1024 * 1024) {
    message.error('图片大小不能超过5MB')
    event.target.value = ''
    return
  }

  uploadingCamera.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    
    const res = await uploadFile(formData, 'repairs')
    if (res && res.fileUrl) {
      uploadedUrls.value.push(res.fileUrl)
      
      const thumbnailUrl = URL.createObjectURL(file)
      fileList.value.push({
        id: Date.now(),
        name: file.name,
        status: 'finished',
        url: res.fileUrl,
        thumbnailUrl
      })
      
      message.success(`${source}成功`)
    } else {
      message.error('图片上传失败')
    }
  } catch (error) {
    console.error('Upload error:', error)
    message.error(error.message || '图片上传失败')
  } finally {
    uploadingCamera.value = false
    event.target.value = ''
  }
}

function handleUploadChange(data) {
  if (data.file.status === 'removed') {
    const url = uploadedUrls.value.find(u => data.file.url?.includes(u) || data.file.thumbnailUrl?.includes(u))
    if (url) {
      uploadedUrls.value = uploadedUrls.value.filter(u => u !== url)
    }
    fileList.value = data.fileList
  }
}

function handleUploadFinish({ file, event }) {
  try {
    const response = JSON.parse(event.target?.responseText || '{}')
    console.log('Upload response:', response)
    if (response.code === 200 && response.data && response.data.fileUrl) {
      uploadedUrls.value.push(response.data.fileUrl)
      message.success('图片上传成功')
    } else {
      message.error(response.message || '图片上传失败')
      return false
    }
  } catch (e) {
    console.error('Upload response parse error:', e)
    message.error('图片上传响应解析失败')
    return false
  }
  return true
}

function handleUploadError({ file, event }) {
  console.error('Upload error:', event)
  message.error('图片上传失败，请检查网络或联系管理员')
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    submitting.value = true

    const data = {
      equipmentId: form.equipmentId || null,
      description: form.description,
      imageUrls: uploadedUrls.value
    }

    await submitRepair(data)

    message.success('报修提交成功！')
    form.description = ''
    form.equipmentId = null
    fileList.value = []
    uploadedUrls.value = []

    fetchRepairs()
  } catch (error) {
    console.error('Submit error:', error)
    message.error(error.message || '提交失败，请重试')
  } finally {
    submitting.value = false
  }
}

function viewDetail(repair) {
  selectedRepair.value = repair
  showDetailModal.value = true
}

function previewImage(url) {
  previewImageUrl.value = url
  showImagePreview.value = true
}

function handleCancel(repairId) {
  dialog.warning({
    title: '取消报修',
    content: '确定要取消这个报修申请吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await cancelRepair(repairId)
        message.success('报修已取消')
        fetchRepairs()
      } catch (error) {
        message.error(error.message || '取消失败')
      }
    }
  })
}

async function fetchRepairs() {
  loading.value = true
  try {
    const res = await getMyRepairs()
    console.log('获取报修记录响应:', res)
    repairs.value = res || []
    console.log('报修记录赋值后:', repairs.value)
  } catch (error) {
    console.error('Fetch repairs error:', error)
    message.error('获取报修记录失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchEquipmentList()
  fetchRepairs()
})

async function fetchEquipmentList() {
  equipmentLoading.value = true
  try {
    const res = await getActiveEquipmentList()
    equipmentOptions.value = (res || []).map(e => ({
      label: `${e.equipmentNo || '-'} - ${e.equipmentName}`,
      value: e.id
    }))
  } catch (error) {
    console.error('获取器械列表失败', error)
    message.error('获取器械列表失败')
  } finally {
    equipmentLoading.value = false
  }
}
</script>

<style scoped>
.repairs-page {
  max-width: 1400px;
  margin: 0 auto;
}

.card-section {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.06);
  margin-bottom: 24px;
  transition: all 0.3s;
}

.card-section:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(0,0,0,0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #1A1A2E;
  margin: 0;
}

.upload-tip {
  font-size: 12px;
  color: #9CA3AF;
  margin-top: 8px;
  margin-bottom: 0;
}

.upload-container {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.mobile-camera-btn {
  margin-top: 0;
}

.mobile-image-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 12px;
  width: 100%;
}

.mobile-image-item {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
}

.mobile-image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.mobile-image-delete {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 22px;
  height: 22px;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  cursor: pointer;
  font-size: 12px;
}

.upload-options {
  display: flex;
  flex-direction: column;
}

.upload-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  cursor: pointer;
  transition: background 0.2s;
  font-size: 15px;
  color: #1a1a2e;
}

.upload-option:hover {
  background: #f5f5f5;
}

.upload-option:first-child {
  border-radius: 16px 16px 0 0;
}

.upload-option:last-child {
  border-radius: 0 0 16px 16px;
}

.upload-option-cancel {
  justify-content: center;
  color: #9ca3af;
  border-top: 1px solid #f3f4f6;
  margin-top: 4px;
}

.repair-table :deep(.n-data-table-th) {
  font-weight: 600;
  color: #6B7280;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding: 14px 16px;
}

.repair-table :deep(.n-data-table-td) {
  padding: 14px 16px;
  font-size: 14px;
}

.repair-detail {
  padding: 8px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-header h3 {
  font-size: 20px;
  font-weight: 600;
  color: #1A1A2E;
  margin: 0;
}

.detail-row {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px solid #F3F4F6;
}

.detail-label {
  width: 100px;
  color: #6B7280;
  font-size: 14px;
  flex-shrink: 0;
}

.detail-value {
  flex: 1;
  color: #1A1A2E;
  font-size: 14px;
}

.detail-images {
  padding: 16px 0;
}

.image-list {
  display: flex;
  gap: 12px;
  margin-top: 12px;
  flex-wrap: wrap;
}

.image-item {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
}

.image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.image-item:hover img {
  transform: scale(1.05);
}

.repair-timeline {
  padding-top: 16px;
}

.repair-timeline h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1A1A2E;
  margin: 0 0 16px 0;
}

.action-buttons {
  display: flex;
  align-items: center;
}

/* ==================== 移动端卡片样式 ==================== */
.mobile-repair-list {
  padding: 8px 0;
}

.repair-cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.repair-card {
  background: #f8fafc;
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid transparent;
}

.repair-card:hover {
  background: white;
  border-color: #e5e7eb;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.repair-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.repair-equipment {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #1a1a2e;
  font-weight: 600;
  font-size: 15px;
}

.repair-card-body {
  margin-bottom: 12px;
}

.repair-desc {
  margin: 0 0 8px 0;
  color: #4b5563;
  font-size: 14px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.repair-time {
  font-size: 12px;
  color: #9ca3af;
}

.repair-card-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 12px;
  border-top: 1px dashed #e5e7eb;
}

.mobile-pagination {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}

/* ==================== 响应式适配 ==================== */
@media (max-width: 768px) {
  .repairs-page {
    padding: 0;
  }
  
  .card-section {
    padding: 16px;
    border-radius: 12px;
    margin-bottom: 16px;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
    margin-bottom: 16px;
  }
  
  .section-header.mobile-header {
    flex-direction: row;
    flex-wrap: wrap;
    align-items: center;
  }
  
  .section-title {
    font-size: 16px;
  }
  
  /* 表单移动端适配 */
  :deep(.n-form-item) {
    margin-bottom: 16px;
  }
  
  :deep(.n-form-item-label) {
    font-size: 14px;
  }
  
  :deep(.n-input__input) {
    font-size: 16px; /* 防止iOS缩放 */
  }
  
  /* 弹窗移动端适配 */
  .repair-detail {
    padding: 4px;
  }
  
  .detail-header h3 {
    font-size: 18px;
  }
  
  .detail-row {
    flex-direction: column;
    gap: 4px;
    padding: 10px 0;
  }
  
  .detail-label {
    width: auto;
    font-size: 13px;
  }
  
  .detail-value {
    font-size: 14px;
  }
  
  .image-list {
    gap: 8px;
  }
  
  .image-item {
    width: 80px;
    height: 80px;
  }
}

@media (max-width: 480px) {
  .card-section {
    padding: 12px;
    border-radius: 10px;
  }
  
  .section-title {
    font-size: 15px;
  }
  
  .repair-card {
    padding: 12px;
  }
  
  .repair-equipment {
    font-size: 14px;
  }
  
  .repair-desc {
    font-size: 13px;
  }
  
  /* 上传容器在小屏幕上的样式 */
  .upload-container {
    justify-content: flex-start;
  }
  
  /* 上传提示文字 */
  .upload-tip {
    font-size: 11px;
    line-height: 1.5;
    word-break: break-word;
  }
}
</style>
