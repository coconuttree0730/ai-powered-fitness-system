<template>
  <div class="repairs-page">
    <!-- 报修表单 -->
    <div class="card-section">
      <div class="section-header">
        <h3 class="section-title">提交报修</h3>
      </div>
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="left" label-width="100">
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
      <div class="section-header">
        <h3 class="section-title">报修记录</h3>
        <n-radio-group v-model:value="filterStatus" size="small">
          <n-radio-button value="all">全部</n-radio-button>
          <n-radio-button value="0">待处理</n-radio-button>
          <n-radio-button value="1">处理中</n-radio-button>
          <n-radio-button value="2">已完成</n-radio-button>
          <n-radio-button value="3">已关闭</n-radio-button>
        </n-radio-group>
      </div>
      <n-data-table
        :columns="columns"
        :data="filteredRepairs"
        :loading="loading"
        :pagination="pagination"
        :bordered="false"
        class="repair-table"
      />
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
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, h } from 'vue'
import { NTag, NButton, useMessage, NIcon, NTimeline, NTimelineItem, NEmpty, useDialog } from 'naive-ui'
import { Camera as CameraIcon } from '@vicons/ionicons5'
import { submitRepair, getMyRepairs, cancelRepair } from '@/api/equipment'

const message = useMessage()
const dialog = useDialog()
const loading = ref(false)
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
  description: ''
})

const uploadedUrls = ref([])

// 上传请求头（携带认证token）
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token') || sessionStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
  return {}
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
      description: form.description,
      imageUrls: uploadedUrls.value
    }

    await submitRepair(data)

    message.success('报修提交成功！')
    form.description = ''
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
  fetchRepairs()
})
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
</style>
