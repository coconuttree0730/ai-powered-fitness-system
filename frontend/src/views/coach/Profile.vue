<template>
  <div class="coach-profile">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">个人信息</h2>
        <span class="page-subtitle">完善您的专业档案，提升学员信任度</span>
      </div>
      <div class="header-right">
        <n-button v-if="!isEditing" type="primary" @click="startEdit">
          <template #icon>
            <n-icon :component="CreateOutline" />
          </template>
          编辑资料
        </n-button>
        <n-space v-else>
          <n-button @click="cancelEdit">取消</n-button>
          <n-button type="primary" :loading="saving" @click="saveProfile">保存</n-button>
        </n-space>
      </div>
    </div>

    <!-- 个人资料卡片 -->
    <n-grid :cols="3" :x-gap="20" :y-gap="20">
      <!-- 左侧：基本信息 -->
      <n-grid-item :span="1">
        <div class="profile-card basic-info">
          <div class="card-header">
            <n-icon :component="PersonOutline" size="20" />
            <span>基本信息</span>
          </div>
          
          <div class="avatar-section">
            <div class="avatar-wrapper">
              <img v-if="profileForm.avatar" :src="profileForm.avatar" class="profile-avatar" />
              <div v-else class="avatar-placeholder" :style="{ background: avatarGradient }">
                {{ profileForm.realName?.charAt(0) || '教' }}
              </div>
              <div v-if="isEditing" class="avatar-overlay" @click="triggerAvatarUpload">
                <n-icon :component="CameraOutline" size="24" />
                <span>更换头像</span>
              </div>
            </div>
            <input 
              type="file" 
              ref="avatarInput" 
              style="display: none" 
              accept="image/*"
              @change="handleAvatarUpload"
            />
          </div>

          <div class="info-list">
            <div class="info-item">
              <span class="info-label">姓名</span>
              <span v-if="!isEditing" class="info-value">{{ profile.realName || '-' }}</span>
              <n-input v-else v-model:value="profileForm.realName" placeholder="请输入姓名" />
            </div>
            <div class="info-item">
              <span class="info-label">性别</span>
              <span v-if="!isEditing" class="info-value">{{ genderText }}</span>
              <n-select v-else v-model:value="profileForm.gender" :options="genderOptions" />
            </div>
            <div class="info-item">
              <span class="info-label">年龄</span>
              <span v-if="!isEditing" class="info-value">{{ profile.age ? profile.age + ' 岁' : '-' }}</span>
              <n-input-number v-else v-model:value="profileForm.age" :min="18" :max="80" placeholder="请输入年龄" />
            </div>
            <div class="info-item">
              <span class="info-label">手机号</span>
              <span v-if="!isEditing" class="info-value">{{ profile.phone || '-' }}</span>
              <n-input v-else v-model:value="profileForm.phone" placeholder="请输入手机号" />
            </div>
            <div class="info-item">
              <span class="info-label">邮箱</span>
              <span v-if="!isEditing" class="info-value">{{ profile.email || '-' }}</span>
              <n-input v-else v-model:value="profileForm.email" placeholder="请输入邮箱" />
            </div>
          </div>
        </div>

        <!-- 紧急联系人 -->
        <div class="profile-card emergency-contact">
          <div class="card-header">
            <n-icon :component="WarningOutline" size="20" />
            <span>紧急联系人</span>
          </div>
          <div class="info-list">
            <div class="info-item">
              <span class="info-label">联系人姓名</span>
              <span v-if="!isEditing" class="info-value">{{ profile.emergencyContact?.name || '-' }}</span>
              <n-input v-else v-model:value="profileForm.emergencyContact.name" placeholder="请输入联系人姓名" />
            </div>
            <div class="info-item">
              <span class="info-label">关系</span>
              <span v-if="!isEditing" class="info-value">{{ profile.emergencyContact?.relation || '-' }}</span>
              <n-select v-else v-model:value="profileForm.emergencyContact.relation" :options="relationOptions" placeholder="请选择关系" />
            </div>
            <div class="info-item">
              <span class="info-label">联系电话</span>
              <span v-if="!isEditing" class="info-value">{{ profile.emergencyContact?.phone || '-' }}</span>
              <n-input v-else v-model:value="profileForm.emergencyContact.phone" placeholder="请输入联系电话" />
            </div>
          </div>
        </div>
      </n-grid-item>

      <!-- 中间：专业信息 -->
      <n-grid-item :span="1">
        <div class="profile-card professional-info">
          <div class="card-header">
            <n-icon :component="FitnessOutline" size="20" />
            <span>专业信息</span>
          </div>
          <div class="info-list">
            <div class="info-item">
              <span class="info-label">从业年限</span>
              <span v-if="!isEditing" class="info-value">{{ profile.workYears ? profile.workYears + ' 年' : '-' }}</span>
              <n-input-number v-else v-model:value="profileForm.workYears" :min="0" :max="50" placeholder="请输入从业年限" />
            </div>
            <div class="info-item">
              <span class="info-label">专业领域</span>
              <span v-if="!isEditing" class="info-value">
                <n-space v-if="profile.specialties?.length" size="small">
                  <n-tag v-for="spec in profile.specialties" :key="spec" type="success" size="small">{{ spec }}</n-tag>
                </n-space>
                <span v-else>-</span>
              </span>
              <n-select v-else v-model:value="profileForm.specialties" :options="specialtyOptions" multiple placeholder="请选择专业领域" />
            </div>
            <div class="info-item">
              <span class="info-label">教学风格</span>
              <span v-if="!isEditing" class="info-value">{{ profile.teachingStyle || '-' }}</span>
              <n-select v-else v-model:value="profileForm.teachingStyle" :options="teachingStyleOptions" placeholder="请选择教学风格" />
            </div>
            <div class="info-item">
              <span class="info-label">教育背景</span>
              <span v-if="!isEditing" class="info-value">{{ profile.education || '-' }}</span>
              <n-input v-else v-model:value="profileForm.education" type="textarea" :rows="2" placeholder="请输入教育背景" />
            </div>
            <div class="info-item">
              <span class="info-label">培训经历</span>
              <span v-if="!isEditing" class="info-value">{{ profile.training || '-' }}</span>
              <n-input v-else v-model:value="profileForm.training" type="textarea" :rows="2" placeholder="请输入培训经历" />
            </div>
            <div class="info-item">
              <span class="info-label">语言能力</span>
              <span v-if="!isEditing" class="info-value">
                <n-space v-if="profile.languages?.length" size="small">
                  <n-tag v-for="lang in profile.languages" :key="lang" size="small">{{ lang }}</n-tag>
                </n-space>
                <span v-else>-</span>
              </span>
              <n-select v-else v-model:value="profileForm.languages" :options="languageOptions" multiple placeholder="请选择语言能力" />
            </div>
          </div>
        </div>

        <!-- 资格认证 -->
        <div class="profile-card certification">
          <div class="card-header">
            <n-icon :component="RibbonOutline" size="20" />
            <span>资格认证</span>
          </div>
          <div class="cert-list">
            <div v-for="(cert, index) in profileForm.certifications" :key="index" class="cert-item">
              <div class="cert-header">
                <span class="cert-name">{{ cert.name }}</span>
                <n-tag v-if="cert.status === 'valid'" type="success" size="small">有效</n-tag>
                <n-tag v-else-if="cert.status === 'expired'" type="error" size="small">已过期</n-tag>
                <n-tag v-else type="warning" size="small">即将过期</n-tag>
              </div>
              <div class="cert-info">
                <span>编号: {{ cert.number }}</span>
                <span>有效期: {{ cert.validDate }}</span>
              </div>
            </div>
            <n-button v-if="isEditing" dashed block @click="addCertification">
              <template #icon>
                <n-icon :component="AddOutline" />
              </template>
              添加认证
            </n-button>
          </div>
        </div>
      </n-grid-item>

      <!-- 右侧：其他信息 -->
      <n-grid-item :span="1">
        <div class="profile-card other-info">
          <div class="card-header">
            <n-icon :component="InformationCircleOutline" size="20" />
            <span>其他信息</span>
          </div>
          <div class="info-list">
            <div class="info-item full-width">
              <span class="info-label">个人简介</span>
              <span v-if="!isEditing" class="info-value text-block">{{ profile.bio || '暂无简介' }}</span>
              <n-input v-else v-model:value="profileForm.bio" type="textarea" :rows="4" placeholder="请输入个人简介/教学理念" />
            </div>
            <div class="info-item full-width">
              <span class="info-label">教学经验</span>
              <span v-if="!isEditing" class="info-value text-block">{{ profile.experience || '暂无教学经验描述' }}</span>
              <n-input v-else v-model:value="profileForm.experience" type="textarea" :rows="4" placeholder="请输入过往教学经验" />
            </div>
            <div class="info-item full-width">
              <span class="info-label">获得荣誉</span>
              <span v-if="!isEditing" class="info-value">
                <n-space v-if="profile.honors?.length" vertical size="small" style="width: 100%">
                  <div v-for="(honor, idx) in profile.honors" :key="idx" class="honor-item">
                    <n-icon :component="TrophyOutline" size="16" color="#f59e0b" />
                    <span>{{ honor }}</span>
                  </div>
                </n-space>
                <span v-else>-</span>
              </span>
              <n-dynamic-input v-else v-model:value="profileForm.honors" placeholder="请输入获得荣誉/奖项" />
            </div>
          </div>
        </div>

        <!-- 可用时间段 -->
        <div class="profile-card availability">
          <div class="card-header">
            <n-icon :component="TimeOutline" size="20" />
            <span>可用教学时间</span>
          </div>
          <div class="time-slots">
            <div v-for="(slots, day) in profileForm.availability" :key="day" class="day-slot">
              <span class="day-label">{{ dayLabels[day] }}</span>
              <div class="slots-list">
                <n-tag v-for="(slot, idx) in slots" :key="idx" size="small" closable @close="removeTimeSlot(day, idx)">
                  {{ slot.start }} - {{ slot.end }}
                </n-tag>
                <n-button v-if="isEditing" text size="small" @click="addTimeSlot(day)">
                  <template #icon>
                    <n-icon :component="AddOutline" />
                  </template>
                  添加
                </n-button>
              </div>
            </div>
          </div>
        </div>
      </n-grid-item>
    </n-grid>

    <!-- 添加认证弹窗 -->
    <n-modal v-model:show="showCertModal" preset="card" title="添加资格认证" style="width: 500px">
      <n-form :model="certForm" label-placement="left" label-width="100">
        <n-form-item label="认证名称">
          <n-input v-model:value="certForm.name" placeholder="例如：ACE认证私人教练" />
        </n-form-item>
        <n-form-item label="认证编号">
          <n-input v-model:value="certForm.number" placeholder="请输入认证编号" />
        </n-form-item>
        <n-form-item label="有效期至">
          <n-date-picker v-model:value="certForm.validDate" type="date" style="width: 100%" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showCertModal = false">取消</n-button>
          <n-button type="primary" @click="confirmAddCert">确认添加</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 添加时间段弹窗 -->
    <n-modal v-model:show="showTimeModal" preset="card" title="添加教学时间" style="width: 400px">
      <n-form label-placement="left" label-width="80">
        <n-form-item label="开始时间">
          <n-time-picker v-model:value="timeForm.start" format="HH:mm" style="width: 100%" />
        </n-form-item>
        <n-form-item label="结束时间">
          <n-time-picker v-model:value="timeForm.end" format="HH:mm" style="width: 100%" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showTimeModal = false">取消</n-button>
          <n-button type="primary" @click="confirmAddTimeSlot">确认添加</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'
import { useMessage } from 'naive-ui'
import {
  PersonOutline,
  FitnessOutline,
  InformationCircleOutline,
  WarningOutline,
  RibbonOutline,
  TimeOutline,
  CreateOutline,
  CameraOutline,
  AddOutline,
  TrophyOutline
} from '@vicons/ionicons5'

const message = useMessage()

// 编辑状态
const isEditing = ref(false)
const saving = ref(false)

// 弹窗状态
const showCertModal = ref(false)
const showTimeModal = ref(false)
const currentDay = ref('')

// 头像上传
const avatarInput = ref(null)

// 表单数据
const profileForm = reactive({
  avatar: '',
  realName: '张教练',
  gender: 'male',
  age: 32,
  phone: '13800138000',
  email: 'coach@example.com',
  workYears: 8,
  specialties: ['健身', '力量训练', '减脂'],
  teachingStyle: 'encouraging',
  education: '北京体育大学 运动训练专业 本科',
  training: '2018年 ACE认证培训\n2019年 NSCA-CPT认证培训\n2021年 运动康复专业进修',
  languages: ['中文', '英语'],
  bio: '拥有8年健身教练经验，专注于帮助学员实现健康目标。相信每个人都有潜力成为更好的自己，我的职责是引导你发现并释放这种潜力。',
  experience: '曾任职于多家知名健身俱乐部，累计指导学员超过500人。擅长制定个性化训练计划，帮助学员安全高效地达成目标。',
  honors: ['2022年度最佳教练', '2023年健身大赛优秀指导奖'],
  emergencyContact: {
    name: '李家属',
    relation: 'spouse',
    phone: '13900139000'
  },
  certifications: [
    { name: 'ACE认证私人教练', number: 'ACE-2020-12345', validDate: '2025-12-31', status: 'valid' },
    { name: 'NSCA-CPT认证', number: 'NSCA-2021-67890', validDate: '2024-06-30', status: 'expiring' }
  ],
  availability: {
    monday: [{ start: '09:00', end: '12:00' }, { start: '14:00', end: '20:00' }],
    tuesday: [{ start: '09:00', end: '12:00' }, { start: '14:00', end: '20:00' }],
    wednesday: [{ start: '09:00', end: '12:00' }, { start: '14:00', end: '20:00' }],
    thursday: [{ start: '09:00', end: '12:00' }, { start: '14:00', end: '20:00' }],
    friday: [{ start: '09:00', end: '12:00' }, { start: '14:00', end: '20:00' }],
    saturday: [{ start: '10:00', end: '18:00' }],
    sunday: [{ start: '10:00', end: '16:00' }]
  }
})

// 原始数据（用于取消编辑时恢复）
const originalProfile = ref(null)

// 计算属性
const profile = computed(() => isEditing.value ? profileForm : profileForm)

const genderText = computed(() => {
  const map = { male: '男', female: '女', other: '其他' }
  return map[profile.value.gender] || '-'
})

const avatarGradient = computed(() => {
  const gradients = [
    'linear-gradient(135deg, #52c41a, #73d13d)',
    'linear-gradient(135deg, #1890ff, #36cfc9)',
    'linear-gradient(135deg, #722ed1, #b37feb)',
    'linear-gradient(135deg, #eb2f96, #ff85c0)'
  ]
  return gradients[0]
})

// 选项数据
const genderOptions = [
  { label: '男', value: 'male' },
  { label: '女', value: 'female' },
  { label: '其他', value: 'other' }
]

const relationOptions = [
  { label: '配偶', value: 'spouse' },
  { label: '父母', value: 'parent' },
  { label: '子女', value: 'child' },
  { label: '兄弟姐妹', value: 'sibling' },
  { label: '朋友', value: 'friend' },
  { label: '其他', value: 'other' }
]

const specialtyOptions = [
  { label: '健身', value: '健身' },
  { label: '力量训练', value: '力量训练' },
  { label: '减脂', value: '减脂' },
  { label: '增肌', value: '增肌' },
  { label: '瑜伽', value: '瑜伽' },
  { label: '普拉提', value: '普拉提' },
  { label: '游泳', value: '游泳' },
  { label: '球类运动', value: '球类运动' },
  { label: '康复训练', value: '康复训练' },
  { label: '体能训练', value: '体能训练' }
]

const teachingStyleOptions = [
  { label: '鼓励型', value: 'encouraging' },
  { label: '严格型', value: 'strict' },
  { label: '温和型', value: 'gentle' },
  { label: '专业型', value: 'professional' },
  { label: '互动型', value: 'interactive' }
]

const languageOptions = [
  { label: '中文', value: '中文' },
  { label: '英语', value: '英语' },
  { label: '日语', value: '日语' },
  { label: '韩语', value: '韩语' },
  { label: '法语', value: '法语' },
  { label: '德语', value: '德语' }
]

const dayLabels = {
  monday: '周一',
  tuesday: '周二',
  wednesday: '周三',
  thursday: '周四',
  friday: '周五',
  saturday: '周六',
  sunday: '周日'
}

// 认证表单
const certForm = reactive({
  name: '',
  number: '',
  validDate: null
})

// 时间表单
const timeForm = reactive({
  start: null,
  end: null
})

// 方法
function startEdit() {
  originalProfile.value = JSON.parse(JSON.stringify(profileForm))
  isEditing.value = true
}

function cancelEdit() {
  if (originalProfile.value) {
    Object.assign(profileForm, originalProfile.value)
  }
  isEditing.value = false
}

async function saveProfile() {
  saving.value = true
  // 模拟保存
  await new Promise(resolve => setTimeout(resolve, 1000))
  saving.value = false
  isEditing.value = false
  message.success('个人资料保存成功')
}

function triggerAvatarUpload() {
  avatarInput.value?.click()
}

function handleAvatarUpload(event) {
  const file = event.target.files[0]
  if (!file) return
  
  if (!file.type.startsWith('image/')) {
    message.error('请上传图片文件')
    return
  }
  
  const reader = new FileReader()
  reader.onload = (e) => {
    profileForm.avatar = e.target.result
    message.success('头像上传成功')
  }
  reader.readAsDataURL(file)
}

function addCertification() {
  certForm.name = ''
  certForm.number = ''
  certForm.validDate = null
  showCertModal.value = true
}

function confirmAddCert() {
  if (!certForm.name || !certForm.number) {
    message.warning('请填写完整信息')
    return
  }
  
  profileForm.certifications.push({
    name: certForm.name,
    number: certForm.number,
    validDate: certForm.validDate ? new Date(certForm.validDate).toISOString().split('T')[0] : '',
    status: 'valid'
  })
  
  showCertModal.value = false
  message.success('认证添加成功')
}

function addTimeSlot(day) {
  currentDay.value = day
  timeForm.start = null
  timeForm.end = null
  showTimeModal.value = true
}

function confirmAddTimeSlot() {
  if (!timeForm.start || !timeForm.end) {
    message.warning('请选择完整时间段')
    return
  }
  
  const startTime = new Date(timeForm.start)
  const endTime = new Date(timeForm.end)
  
  if (startTime >= endTime) {
    message.error('结束时间必须晚于开始时间')
    return
  }
  
  const formatTime = (date) => {
    return date.toTimeString().slice(0, 5)
  }
  
  profileForm.availability[currentDay.value].push({
    start: formatTime(startTime),
    end: formatTime(endTime)
  })
  
  showTimeModal.value = false
  message.success('时间段添加成功')
}

function removeTimeSlot(day, index) {
  profileForm.availability[day].splice(index, 1)
}
</script>

<style scoped>
.coach-profile {
  max-width: 1600px;
  margin: 0 auto;
}

/* 页面头部 */
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

/* 卡片样式 */
.profile-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1A1A2E;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.card-header :deep(.n-icon) {
  color: #52c41a;
}

/* 头像区域 */
.avatar-section {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.avatar-wrapper {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
}

.profile-avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  font-weight: 600;
  color: white;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 12px;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

/* 信息列表 */
.info-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.info-label {
  font-size: 13px;
  color: #6B7280;
  font-weight: 500;
}

.info-value {
  font-size: 14px;
  color: #1A1A2E;
  font-weight: 500;
}

.info-value.text-block {
  line-height: 1.6;
  white-space: pre-line;
}

/* 认证列表 */
.cert-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cert-item {
  background: #f8fafc;
  border-radius: 8px;
  padding: 12px;
  border-left: 3px solid #52c41a;
}

.cert-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.cert-name {
  font-weight: 600;
  color: #1A1A2E;
}

.cert-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
  color: #6B7280;
}

/* 荣誉项 */
.honor-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #fef3c7;
  border-radius: 6px;
  font-size: 13px;
  color: #92400e;
}

/* 时间段 */
.time-slots {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.day-slot {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.day-label {
  font-size: 13px;
  font-weight: 600;
  color: #1A1A2E;
}

.slots-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

/* 响应式 */
@media (max-width: 1200px) {
  :deep(.n-grid) {
    grid-template-columns: repeat(2, 1fr) !important;
  }
}

@media (max-width: 768px) {
  :deep(.n-grid) {
    grid-template-columns: 1fr !important;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
