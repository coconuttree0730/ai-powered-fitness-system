<template>
  <div class="equipment-detail-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-bg"></div>
      <div class="header-content">
        <div class="header-breadcrumb">
          <router-link to="/">首页</router-link>
          <span class="breadcrumb-separator">/</span>
          <router-link to="/equipments">全部器械</router-link>
          <span class="breadcrumb-separator">/</span>
          <span class="current">{{ equipment?.equipmentName || '器械详情' }}</span>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>正在加载器械信息...</p>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="error" class="error-container">
      <div class="error-icon">
        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <circle cx="12" cy="12" r="10"/>
          <line x1="12" y1="8" x2="12" y2="12"/>
          <line x1="12" y1="16" x2="12.01" y2="16"/>
        </svg>
      </div>
      <h3>{{ error }}</h3>
      <button class="btn btn-primary" @click="fetchEquipment">重新加载</button>
    </div>

    <!-- 器械详情内容 -->
    <div v-else-if="equipment" class="detail-container">
      <!-- 器械主图区域 -->
      <div class="equipment-hero">
        <div class="equipment-image-section">
          <img
            :src="equipment.imageUrl || defaultImage"
            :alt="equipment.equipmentName"
            class="equipment-main-image"
            @error="handleImageError"
          />
          <div class="image-overlay">
            <span class="type-badge" :class="getTypeClass(equipment.typeCode)">
              {{ equipment.typeName || '其他' }}
            </span>
            <span v-if="equipment.location" class="location-badge">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                <circle cx="12" cy="10" r="3"/>
              </svg>
              {{ equipment.location }}
            </span>
          </div>
        </div>

        <!-- 器械基本信息 -->
        <div class="equipment-info-section">
          <h1 class="equipment-title">{{ equipment.equipmentName }}</h1>
          <p class="equipment-subtitle">{{ equipment.description?.substring(0, 150) || '专业健身器材，助力您的训练目标' }}{{ equipment.description?.length > 150 ? '...' : '' }}</p>

          <!-- 器械统计 -->
          <div class="equipment-stats">
            <div class="stat-item">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M4 7V4h3M4 17v3h3M20 7V4h-3M20 17v3h-3M9 9h6v6H9z"/>
              </svg>
              <span>编号: {{ equipment.equipmentNo || 'N/A' }}</span>
            </div>
            <div class="stat-item">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <polyline points="12 6 12 12 16 14"/>
              </svg>
              <span>添加时间: {{ formatDate(equipment.createTime) }}</span>
            </div>
            <div class="stat-item">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/>
              </svg>
              <span>维护次数: {{ equipment.repairCount || 0 }}</span>
            </div>
          </div>

          <!-- 状态信息 -->
          <div class="status-section">
            <div class="status-header">
              <span class="status-label">当前状态</span>
              <span class="equipment-status" :class="getStatusClass(equipment.status)">
                <span class="status-dot"></span>
                {{ getStatusLabel(equipment.status) }}
              </span>
            </div>
            <div v-if="equipment.status === 0" class="status-reason">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <line x1="12" y1="8" x2="12" y2="12"/>
                <line x1="12" y1="16" x2="12.01" y2="16"/>
              </svg>
              <span>该器械正在维修中，暂时无法使用</span>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="action-section">
            <button
              v-if="equipment.status === 1"
              class="btn btn-primary btn-action"
              @click="handleUseEquipment"
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/>
              </svg>
              我要使用
            </button>
            <button
              v-else
              class="btn btn-disabled btn-action"
              disabled
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <line x1="15" y1="9" x2="9" y2="15"/>
                <line x1="9" y1="9" x2="15" y2="15"/>
              </svg>
              暂不可用
            </button>
            <button
              class="btn btn-outline btn-action"
              @click="handleReportRepair"
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
                <line x1="12" y1="9" x2="12" y2="13"/>
                <line x1="12" y1="17" x2="12.01" y2="17"/>
              </svg>
              报修反馈
            </button>
          </div>
        </div>
      </div>

      <!-- 器械详情标签页 -->
      <div class="equipment-tabs">
        <div class="tabs-header">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            :class="['tab-btn', { active: activeTab === tab.key }]"
            @click="activeTab = tab.key"
          >
            {{ tab.label }}
          </button>
        </div>

        <div class="tabs-content">
          <!-- 器械介绍 -->
          <div v-if="activeTab === 'intro'" class="tab-panel">
            <div class="intro-section">
              <h3 class="section-title">器械介绍</h3>
              <p class="intro-text">{{ equipment.description || '暂无器械介绍' }}</p>
            </div>

            <div class="specs-section" v-if="equipment.specifications">
              <h3 class="section-title">技术参数</h3>
              <div class="specs-grid">
                <div v-for="(value, key) in parseSpecs(equipment.specifications)" :key="key" class="spec-item">
                  <span class="spec-label">{{ key }}</span>
                  <span class="spec-value">{{ value }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 使用说明 -->
          <div v-if="activeTab === 'guide'" class="tab-panel">
            <div class="guide-section">
              <h3 class="section-title">使用说明</h3>
              <div class="guide-content">
                <div class="guide-item">
                  <div class="guide-number">1</div>
                  <div class="guide-text">
                    <h4>调整器械</h4>
                    <p>根据您的身高和体型，调整座椅高度和配重位置，确保使用舒适且安全。</p>
                  </div>
                </div>
                <div class="guide-item">
                  <div class="guide-number">2</div>
                  <div class="guide-text">
                    <h4>选择合适重量</h4>
                    <p>初学者建议从轻重量开始，循序渐进增加负荷，避免运动损伤。</p>
                  </div>
                </div>
                <div class="guide-item">
                  <div class="guide-number">3</div>
                  <div class="guide-text">
                    <h4>保持正确姿势</h4>
                    <p>注意核心收紧，动作缓慢控制，感受目标肌群的收缩。</p>
                  </div>
                </div>
                <div class="guide-item">
                  <div class="guide-number">4</div>
                  <div class="guide-text">
                    <h4>使用后整理</h4>
                    <p>使用完毕后请将器械归位，擦拭汗水，保持器械清洁。</p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 维护记录 -->
          <div v-if="activeTab === 'maintenance'" class="tab-panel">
            <div class="maintenance-section">
              <h3 class="section-title">维护记录</h3>
              <div v-if="repairRecords.length > 0" class="maintenance-list">
                <div
                  v-for="(record, index) in repairRecords"
                  :key="index"
                  class="maintenance-item"
                >
                  <div class="maintenance-header">
                    <span class="maintenance-date">{{ formatDate(record.repairTime) }}</span>
                    <span class="maintenance-status" :class="getRepairStatusClass(record.status)">
                      {{ getRepairStatusLabel(record.status) }}
                    </span>
                  </div>
                  <p class="maintenance-desc">{{ record.description || '常规维护' }}</p>
                  <p v-if="record.repairCost" class="maintenance-cost">
                    维修费用: ¥{{ record.repairCost }}
                  </p>
                </div>
              </div>
              <div v-else class="empty-maintenance">
                <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <circle cx="12" cy="12" r="10"/>
                  <line x1="8" y1="12" x2="16" y2="12"/>
                </svg>
                <p>暂无维护记录</p>
              </div>
            </div>
          </div>

          <!-- 注意事项 -->
          <div v-if="activeTab === 'notice'" class="tab-panel">
            <div class="notice-section">
              <h3 class="section-title">使用须知</h3>
              <ul class="notice-list">
                <li v-for="(notice, index) in notices" :key="index" class="notice-item">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#FF6B35" stroke-width="2">
                    <circle cx="12" cy="12" r="10"/>
                    <line x1="12" y1="8" x2="12" y2="12"/>
                    <line x1="12" y1="16" x2="12.01" y2="16"/>
                  </svg>
                  <span>{{ notice }}</span>
                </li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 报修模态框 -->
    <div v-if="showRepairModal" class="modal-overlay" @click.self="closeRepairModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>报修反馈</h3>
          <button class="modal-close" @click="closeRepairModal">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>问题描述 <span class="required">*</span></label>
            <textarea
              v-model="repairForm.description"
              rows="4"
              placeholder="请详细描述器械的问题..."
              class="form-textarea"
            ></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-outline" @click="closeRepairModal">取消</button>
          <button
            class="btn btn-primary"
            :disabled="!repairForm.description || submitting"
            @click="submitRepair"
          >
            <span v-if="submitting" class="btn-spinner"></span>
            <span v-else>提交报修</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'
import { getEquipmentDetail, reportEquipmentRepair, getEquipmentRepairs } from '@/api/equipment'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const authStore = useAuthStore()

const loading = ref(false)
const error = ref(null)
const equipment = ref(null)
const activeTab = ref('intro')
const repairRecords = ref([])

// 报修模态框
const showRepairModal = ref(false)
const submitting = ref(false)
const repairForm = ref({
  description: ''
})

const defaultImage = 'https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=800&h=600&fit=crop'

const tabs = [
  { key: 'intro', label: '器械介绍' },
  { key: 'guide', label: '使用说明' },
  { key: 'maintenance', label: '维护记录' },
  { key: 'notice', label: '注意事项' }
]

// 注意事项
const notices = [
  '使用前请检查器械是否完好，如有异常请勿使用',
  '请根据自身能力选择合适的重量，避免运动损伤',
  '使用时请保持正确姿势，必要时可咨询教练',
  '使用完毕后请将器械归位，方便他人使用',
  '发现器械故障请及时报修，确保训练安全'
]

onMounted(() => {
  fetchEquipment()
})

async function fetchEquipment() {
  loading.value = true
  error.value = null
  try {
    const res = await getEquipmentDetail(route.params.id)
    if (res) {
      equipment.value = res
      // 获取维护记录
      fetchRepairRecords()
    } else {
      error.value = '器械不存在或已下架'
    }
  } catch (err) {
    error.value = '获取器械详情失败，请稍后重试'
    console.error('获取器械详情失败:', err)
  } finally {
    loading.value = false
  }
}

async function fetchRepairRecords() {
  try {
    const res = await getEquipmentRepairs(route.params.id)
    if (Array.isArray(res)) {
      repairRecords.value = res
    } else {
      repairRecords.value = []
    }
  } catch (err) {
    console.error('获取维护记录失败:', err)
    repairRecords.value = []
  }
}

// 获取类型样式类
function getTypeClass(typeCode) {
  const map = {
    'STRENGTH': 'type-strength',
    'CARDIO': 'type-cardio',
    'FREE_WEIGHT': 'type-freeweight',
    'FUNCTIONAL': 'type-functional',
    'YOGA': 'type-yoga'
  }
  return map[typeCode] || 'type-default'
}

// 获取状态样式类
function getStatusClass(status) {
  const map = {
    0: 'status-repair',
    1: 'status-normal',
    2: 'status-scrapped'
  }
  return map[status] || 'status-normal'
}

// 获取状态标签
function getStatusLabel(status) {
  const map = {
    0: '维修中',
    1: '正常使用',
    2: '已报废'
  }
  return map[status] || '未知'
}

// 获取维修状态样式类
function getRepairStatusClass(status) {
  const map = {
    0: 'repair-pending',
    1: 'repair-processing',
    2: 'repair-completed'
  }
  return map[status] || 'repair-pending'
}

// 获取维修状态标签
function getRepairStatusLabel(status) {
  const map = {
    0: '待处理',
    1: '处理中',
    2: '已完成'
  }
  return map[status] || '待处理'
}

// 解析技术参数
function parseSpecs(specs) {
  if (!specs) return {}
  try {
    return JSON.parse(specs)
  } catch {
    return {}
  }
}

// 格式化日期
function formatDate(dateStr) {
  if (!dateStr) return '未知'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

// 图片加载失败处理
function handleImageError(event) {
  event.target.src = defaultImage
}

// 使用器械
function handleUseEquipment() {
  if (!authStore.isLoggedIn) {
    message.info('请先登录后再使用器械')
    sessionStorage.setItem('pendingEquipmentId', route.params.id)
    router.push('/login?redirect=' + encodeURIComponent(`/equipments/${route.params.id}`))
    return
  }
  message.success('器械使用登记成功，请合理使用器械')
}

// 报修反馈
function handleReportRepair() {
  if (!authStore.isLoggedIn) {
    message.info('请先登录后再进行报修')
    sessionStorage.setItem('pendingEquipmentId', route.params.id)
    router.push('/login?redirect=' + encodeURIComponent(`/equipments/${route.params.id}`))
    return
  }
  showRepairModal.value = true
}

// 关闭报修模态框
function closeRepairModal() {
  showRepairModal.value = false
  repairForm.value.description = ''
}

// 提交报修
async function submitRepair() {
  if (!repairForm.value.description.trim()) {
    message.warning('请填写问题描述')
    return
  }

  submitting.value = true
  try {
    await reportEquipmentRepair({
      equipmentId: route.params.id,
      description: repairForm.value.description
    })
    message.success('报修提交成功，我们会尽快处理')
    closeRepairModal()
    // 刷新维护记录
    fetchRepairRecords()
  } catch (err) {
    message.error(err.message || '报修提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.equipment-detail-page {
  min-height: 100vh;
  background: #0A0A0F;
  color: #fff;
}

/* 页面头部 */
.page-header {
  position: relative;
  padding: 40px 0 30px;
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.1) 0%, rgba(26, 26, 37, 0.8) 100%);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.header-bg {
  position: absolute;
  inset: 0;
  background: radial-gradient(ellipse at top right, rgba(255, 107, 53, 0.15) 0%, transparent 50%);
}

.header-content {
  position: relative;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.header-breadcrumb {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
}

.header-breadcrumb a {
  color: rgba(255, 255, 255, 0.6);
  text-decoration: none;
  transition: color 0.3s;
}

.header-breadcrumb a:hover {
  color: #FF6B35;
}

.breadcrumb-separator {
  color: rgba(255, 255, 255, 0.3);
}

.header-breadcrumb .current {
  color: #fff;
  font-weight: 500;
}

/* 加载和错误状态 */
.loading-container,
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  gap: 20px;
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 3px solid rgba(255, 107, 53, 0.2);
  border-top-color: #FF6B35;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-icon {
  color: #FF6B35;
}

/* 详情容器 */
.detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

/* 器械主图区域 */
.equipment-hero {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 40px;
  margin-bottom: 50px;
}

.equipment-image-section {
  position: relative;
  border-radius: 24px;
  overflow: hidden;
  aspect-ratio: 4/3;
}

.equipment-main-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-overlay {
  position: absolute;
  top: 20px;
  left: 20px;
  display: flex;
  gap: 10px;
}

.type-badge,
.location-badge {
  padding: 8px 16px;
  border-radius: 100px;
  font-size: 13px;
  font-weight: 600;
}

.type-strength { background: linear-gradient(135deg, #F59E0B 0%, #FBBF24 100%); }
.type-cardio { background: linear-gradient(135deg, #EF4444 0%, #F87171 100%); }
.type-freeweight { background: linear-gradient(135deg, #8B5CF6 0%, #A78BFA 100%); }
.type-functional { background: linear-gradient(135deg, #10B981 0%, #34D399 100%); }
.type-yoga { background: linear-gradient(135deg, #3B82F6 0%, #60A5FA 100%); }
.type-default { background: linear-gradient(135deg, #FF6B35 0%, #FF8C61 100%); }

.location-badge {
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 器械信息区域 */
.equipment-info-section {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.equipment-title {
  font-size: 36px;
  font-weight: 700;
  line-height: 1.3;
  margin: 0;
  background: linear-gradient(135deg, #fff 0%, rgba(255, 255, 255, 0.8) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.equipment-subtitle {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.6);
  line-height: 1.6;
  margin: 0;
}

.equipment-stats {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 15px;
}

.stat-item svg {
  color: #FF6B35;
}

/* 状态区域 */
.status-section {
  padding: 20px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 16px;
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.status-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
}

.equipment-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  padding: 6px 12px;
  border-radius: 100px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-normal {
  background: rgba(34, 197, 94, 0.2);
  color: #22C55E;
}

.status-normal .status-dot {
  background: #22C55E;
}

.status-repair {
  background: rgba(255, 193, 7, 0.2);
  color: #FFC107;
}

.status-repair .status-dot {
  background: #FFC107;
}

.status-scrapped {
  background: rgba(239, 68, 68, 0.2);
  color: #EF4444;
}

.status-scrapped .status-dot {
  background: #EF4444;
}

.status-reason {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: rgba(255, 193, 7, 0.1);
  border-radius: 8px;
  font-size: 14px;
  color: #FFC107;
}

/* 操作按钮 */
.action-section {
  display: flex;
  gap: 12px;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px 32px;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-primary {
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C61 100%);
  color: #fff;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(255, 107, 53, 0.4);
}

.btn-outline {
  background: transparent;
  border: 2px solid rgba(255, 255, 255, 0.2);
  color: #fff;
}

.btn-outline:hover {
  border-color: #FF6B35;
  color: #FF6B35;
}

.btn-disabled {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.4);
  cursor: not-allowed;
}

.btn-action {
  flex: 1;
  padding: 16px;
}

.btn-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

/* 标签页 */
.equipment-tabs {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 24px;
  overflow: hidden;
}

.tabs-header {
  display: flex;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.tab-btn {
  flex: 1;
  padding: 20px;
  background: transparent;
  border: none;
  color: rgba(255, 255, 255, 0.6);
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.tab-btn:hover {
  color: rgba(255, 255, 255, 0.9);
  background: rgba(255, 255, 255, 0.02);
}

.tab-btn.active {
  color: #FF6B35;
}

.tab-btn.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 20%;
  right: 20%;
  height: 3px;
  background: linear-gradient(90deg, transparent, #FF6B35, transparent);
  border-radius: 3px;
}

.tabs-content {
  padding: 40px;
}

.tab-panel {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
  color: #fff;
}

.intro-text {
  font-size: 15px;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 30px;
}

/* 技术参数 */
.specs-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.spec-item {
  display: flex;
  justify-content: space-between;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.04);
  border-radius: 12px;
}

.spec-label {
  color: rgba(255, 255, 255, 0.5);
  font-size: 14px;
}

.spec-value {
  color: #fff;
  font-weight: 500;
  font-size: 14px;
}

/* 使用说明 */
.guide-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.guide-item {
  display: flex;
  gap: 20px;
  padding: 24px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.04);
  border-radius: 16px;
  transition: all 0.3s;
}

.guide-item:hover {
  background: rgba(255, 255, 255, 0.04);
  border-color: rgba(255, 107, 53, 0.2);
}

.guide-number {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C61 100%);
  border-radius: 12px;
  font-weight: 700;
  font-size: 16px;
  flex-shrink: 0;
}

.guide-text h4 {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 8px;
  color: #fff;
}

.guide-text p {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
}

/* 维护记录 */
.maintenance-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.maintenance-item {
  padding: 20px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.04);
  border-radius: 12px;
}

.maintenance-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.maintenance-date {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.5);
}

.maintenance-status {
  padding: 4px 12px;
  border-radius: 100px;
  font-size: 12px;
  font-weight: 600;
}

.repair-pending {
  background: rgba(255, 193, 7, 0.2);
  color: #FFC107;
}

.repair-processing {
  background: rgba(59, 130, 246, 0.2);
  color: #3B82F6;
}

.repair-completed {
  background: rgba(34, 197, 94, 0.2);
  color: #22C55E;
}

.maintenance-desc {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 8px;
}

.maintenance-cost {
  font-size: 14px;
  color: #FF6B35;
  font-weight: 500;
}

.empty-maintenance {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: rgba(255, 255, 255, 0.4);
}

.empty-maintenance svg {
  margin-bottom: 16px;
}

/* 注意事项 */
.notice-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  list-style: none;
  padding: 0;
  margin: 0;
}

.notice-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.04);
  border-radius: 12px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
}

.notice-item svg {
  flex-shrink: 0;
  margin-top: 2px;
}

/* 模态框 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal-content {
  background: #1A1A25;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 24px;
  width: 100%;
  max-width: 500px;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.modal-header h3 {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}

.modal-close {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.05);
  border: none;
  border-radius: 8px;
  color: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  transition: all 0.3s;
}

.modal-close:hover {
  background: rgba(255, 107, 53, 0.1);
  color: #FF6B35;
}

.modal-body {
  padding: 24px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

.required {
  color: #FF6B35;
}

.form-textarea {
  width: 100%;
  padding: 14px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  color: #fff;
  font-size: 15px;
  resize: vertical;
  min-height: 120px;
  outline: none;
  transition: all 0.3s;
}

.form-textarea:focus {
  border-color: #FF6B35;
  background: rgba(255, 255, 255, 0.05);
}

.form-textarea::placeholder {
  color: rgba(255, 255, 255, 0.3);
}

.modal-footer {
  display: flex;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.modal-footer .btn {
  flex: 1;
}

/* 响应式 */
@media (max-width: 968px) {
  .equipment-hero {
    grid-template-columns: 1fr;
  }

  .equipment-title {
    font-size: 28px;
  }

  .tabs-content {
    padding: 24px;
  }

  .specs-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .equipment-stats {
    gap: 16px;
  }

  .action-section {
    flex-direction: column;
  }

  .tabs-header {
    flex-wrap: wrap;
  }

  .tab-btn {
    padding: 16px 12px;
    font-size: 14px;
  }

  .guide-item {
    flex-direction: column;
    gap: 12px;
  }
}
</style>
