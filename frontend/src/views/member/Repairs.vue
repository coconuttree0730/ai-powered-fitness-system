<template>
  <div class="repairs-page">
    <!-- 报修统计 -->
    <n-grid :cols="4" :x-gap="16" class="stats-grid">
      <n-grid-item>
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #FF6B35, #FF8C61);">🔧</div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.total }}</div>
            <div class="stat-label">总报修</div>
          </div>
        </div>
      </n-grid-item>
      <n-grid-item>
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #FFD166, #FFB347);">⏳</div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.pending }}</div>
            <div class="stat-label">正在检查</div>
          </div>
        </div>
      </n-grid-item>
      <n-grid-item>
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #667eea, #764ba2);">🔨</div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.repairing }}</div>
            <div class="stat-label">在维修</div>
          </div>
        </div>
      </n-grid-item>
      <n-grid-item>
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #06D6A0, #2EC4B6);">✅</div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.completed }}</div>
            <div class="stat-label">已修复</div>
          </div>
        </div>
      </n-grid-item>
    </n-grid>

    <!-- 报修表单 -->
    <div class="card-section">
      <div class="section-header">
        <h3 class="section-title">提交报修</h3>
      </div>
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="left" label-width="100">
        <n-grid :cols="2" :x-gap="24">
          <n-grid-item>
            <n-form-item label="选择器材" path="equipmentId">
              <n-select 
                v-model:value="form.equipmentId" 
                :options="equipmentOptions" 
                placeholder="请选择需要报修的器材"
                size="large"
              />
            </n-form-item>
          </n-grid-item>
          <n-grid-item>
            <n-form-item label="问题类型" path="issueType">
              <n-select 
                v-model:value="form.issueType" 
                :options="issueTypeOptions" 
                placeholder="请选择问题类型"
                size="large"
              />
            </n-form-item>
          </n-grid-item>
        </n-grid>
        
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
            action="/api/upload"
            list-type="image-card"
            :max="3"
            :file-list="fileList"
            @change="handleUploadChange"
          >
            <n-button size="large" dashed style="width: 120px; height: 120px;">
              <template #icon>
                <n-icon><camera-icon /></n-icon>
              </template>
              上传图片
            </n-button>
          </n-upload>
          <p class="upload-tip">最多上传3张图片，支持 jpg/png 格式，单张不超过 10MB</p>
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
          <n-radio-button value="checking">正在检查</n-radio-button>
          <n-radio-button value="repairing">在维修</n-radio-button>
          <n-radio-button value="completed">已修复</n-radio-button>
        </n-radio-group>
      </div>
      <n-data-table 
        :columns="columns" 
        :data="filteredRepairs" 
        :loading="loading"
        :pagination="{ pageSize: 10 }"
        :bordered="false"
        class="repair-table"
      />
    </div>

    <!-- 报修详情弹窗 -->
    <n-modal v-model:show="showDetailModal" preset="card" style="width: 600px" :show-header="false">
      <div v-if="selectedRepair" class="repair-detail">
        <div class="detail-header">
          <h3>报修详情</h3>
          <n-tag :type="getStatusType(selectedRepair.status)" size="large" round>
            {{ getStatusText(selectedRepair.status) }}
          </n-tag>
        </div>
        
        <n-divider />
        
        <div class="detail-row">
          <span class="detail-label">器材名称</span>
          <span class="detail-value">{{ selectedRepair.equipmentName }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">问题类型</span>
          <span class="detail-value">{{ selectedRepair.issueType }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">报修时间</span>
          <span class="detail-value">{{ formatTime(selectedRepair.createTime) }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">问题描述</span>
          <span class="detail-value">{{ selectedRepair.description }}</span>
        </div>
        
        <div v-if="selectedRepair.images?.length" class="detail-images">
          <span class="detail-label">问题图片</span>
          <div class="image-list">
            <div v-for="(img, idx) in selectedRepair.images" :key="idx" class="image-item">
              <img :src="img" alt="问题图片" @click="previewImage(img)" />
            </div>
          </div>
        </div>
        
        <n-divider />
        
        <div class="repair-timeline">
          <h4>处理进度</h4>
          <n-timeline>
            <n-timeline-item 
              v-for="(item, idx) in selectedRepair.timeline" 
              :key="idx"
              :type="item.type"
              :title="item.title"
              :content="item.content"
              :time="item.time"
            />
          </n-timeline>
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
import { ref, reactive, computed, h, onMounted } from 'vue'
import { NTag, NButton, useMessage, NIcon, NTimeline, NTimelineItem } from 'naive-ui'
import { Camera as CameraIcon } from '@vicons/ionicons5'

const message = useMessage()
const loading = ref(false)
const submitting = ref(false)
const showDetailModal = ref(false)
const showImagePreview = ref(false)
const previewImageUrl = ref('')
const selectedRepair = ref(null)
const filterStatus = ref('all')
const fileList = ref([])

const formRef = ref(null)
const form = reactive({
  equipmentId: null,
  issueType: null,
  description: ''
})

const rules = {
  equipmentId: [{ required: true, message: '请选择器材', trigger: 'change' }],
  issueType: [{ required: true, message: '请选择问题类型', trigger: 'change' }],
  description: [{ required: true, message: '请描述问题', trigger: 'blur' }]
}

const equipmentOptions = [
  { label: '跑步机 #01', value: 1 },
  { label: '跑步机 #02', value: 2 },
  { label: '椭圆机 #01', value: 3 },
  { label: '动感单车 #01', value: 4 },
  { label: '史密斯机', value: 5 },
  { label: '哑铃组 A区', value: 6 },
  { label: '龙门架', value: 7 }
]

const issueTypeOptions = [
  { label: '无法启动', value: '无法启动' },
  { label: '运行异常', value: '运行异常' },
  { label: '噪音过大', value: '噪音过大' },
  { label: '显示屏故障', value: '显示屏故障' },
  { label: '配件损坏', value: '配件损坏' },
  { label: '其他问题', value: '其他问题' }
]

// 模拟报修数据
const repairs = ref([
  { 
    id: 1, 
    equipmentName: '跑步机 #01', 
    issueType: '显示屏故障',
    description: '显示屏无法正常显示，黑屏状态，但跑步机可以正常运转。', 
    status: 'checking',
    createTime: '2024-10-18T10:30:00',
    images: [],
    timeline: [
      { type: 'success', title: '报修提交', content: '您的报修申请已提交成功', time: '2024-10-18 10:30' },
      { type: 'info', title: '正在检查', content: '维修人员正在检查问题', time: '2024-10-18 14:20' }
    ]
  },
  { 
    id: 2, 
    equipmentName: '史密斯机', 
    issueType: '配件损坏',
    description: '安全锁扣松动，使用时会有异响。', 
    status: 'repairing',
    createTime: '2024-10-15T16:45:00',
    images: [],
    timeline: [
      { type: 'success', title: '报修提交', content: '您的报修申请已提交成功', time: '2024-10-15 16:45' },
      { type: 'success', title: '检查完成', content: '问题已确认，需要更换配件', time: '2024-10-16 09:10' },
      { type: 'warning', title: '正在维修', content: '配件已订购，预计3天内完成维修', time: '2024-10-16 14:30' }
    ]
  },
  { 
    id: 3, 
    equipmentName: '椭圆机 #02', 
    issueType: '噪音过大',
    description: '使用时踏板处有异常摩擦声。', 
    status: 'completed',
    createTime: '2024-10-10T09:20:00',
    images: [],
    timeline: [
      { type: 'success', title: '报修提交', content: '您的报修申请已提交成功', time: '2024-10-10 09:20' },
      { type: 'success', title: '检查完成', content: '问题已确认', time: '2024-10-10 11:30' },
      { type: 'success', title: '维修完成', content: '问题已修复，请测试使用', time: '2024-10-11 10:00' }
    ]
  },
])

const stats = computed(() => {
  return {
    total: repairs.value.length,
    pending: repairs.value.filter(r => r.status === 'checking').length,
    repairing: repairs.value.filter(r => r.status === 'repairing').length,
    completed: repairs.value.filter(r => r.status === 'completed').length
  }
})

const filteredRepairs = computed(() => {
  if (filterStatus.value === 'all') return repairs.value
  return repairs.value.filter(r => r.status === filterStatus.value)
})

const columns = [
  { title: '器材名称', key: 'equipmentName' },
  { title: '问题类型', key: 'issueType' },
  { 
    title: '问题描述', 
    key: 'description', 
    ellipsis: { tooltip: true },
    width: 250
  },
  {
    title: '状态',
    key: 'status',
    width: 120,
    render: (row) => h(NTag, { type: getStatusType(row.status), size: 'small' }, () => getStatusText(row.status))
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
    width: 100,
    render: (row) => h(NButton, { 
      type: 'primary', 
      size: 'small',
      onClick: () => viewDetail(row)
    }, () => '查看详情')
  }
]

function getStatusType(status) {
  const map = { checking: 'warning', repairing: 'info', completed: 'success' }
  return map[status] || 'default'
}

function getStatusText(status) {
  const map = { checking: '正在检查', repairing: '在维修', completed: '已修复' }
  return map[status] || status
}

function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

function handleUploadChange(data) {
  fileList.value = data.fileList
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    submitting.value = true
    
    setTimeout(() => {
      repairs.value.unshift({
        id: Date.now(),
        equipmentName: equipmentOptions.find(e => e.value === form.equipmentId)?.label,
        issueType: form.issueType,
        description: form.description,
        status: 'checking',
        createTime: new Date().toISOString(),
        images: fileList.value.map(f => f.url || f.thumbnailUrl),
        timeline: [
          { type: 'success', title: '报修提交', content: '您的报修申请已提交成功', time: formatTime(new Date().toISOString()) }
        ]
      })
      
      message.success('报修提交成功！')
      form.equipmentId = null
      form.issueType = null
      form.description = ''
      fileList.value = []
      submitting.value = false
    }, 1000)
  } catch (error) {
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

onMounted(() => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 500)
})
</script>

<style scoped>
.repairs-page {
  max-width: 1400px;
  margin: 0 auto;
}

.stats-grid {
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.06);
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(0,0,0,0.1);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1A1A2E;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #6B7280;
  margin-top: 4px;
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
</style>
