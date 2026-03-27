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
          
          <!-- 头像区域 -->
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
              <span class="info-label">用户名</span>
              <span v-if="!isEditing" class="info-value">{{ profile.username || '-' }}</span>
              <n-input
                v-else
                v-model:value="profileForm.username"
                placeholder="请输入用户名"
                :maxlength="20"
                :status="usernameValidationStatus"
              />
              <n-text v-if="isEditing && usernameValidationMsg" type="error" style="font-size: 12px;">
                {{ usernameValidationMsg }}
              </n-text>
            </div>
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

        <!-- 个人展示图片 -->
        <div class="profile-card personal-image-card">
          <div class="card-header">
            <n-icon :component="ImageOutline" size="20" />
            <span>个人展示图片</span>
            <n-tag v-if="profileForm.personalImageUrl" type="success" size="small">已上传</n-tag>
            <n-tag v-else type="warning" size="small">未上传</n-tag>
          </div>
          <div class="personal-image-section">
            <div v-if="profileForm.personalImageUrl" class="personal-image-preview">
              <img :src="profileForm.personalImageUrl" alt="个人展示图片" />
              <div v-if="isEditing" class="image-actions">
                <n-button type="primary" size="small" @click="triggerPersonalImageUpload">
                  <template #icon>
                    <n-icon :component="RefreshOutline" />
                  </template>
                  更换图片
                </n-button>
                <n-button type="error" size="small" @click="handleDeletePersonalImage">
                  <template #icon>
                    <n-icon :component="TrashOutline" />
                  </template>
                  删除
                </n-button>
              </div>
            </div>
            <div v-else class="personal-image-upload" @click="triggerPersonalImageUpload">
              <n-icon :component="CloudUploadOutline" size="48" />
              <span>点击上传个人展示图片</span>
              <span class="upload-hint">支持 JPG、PNG、GIF 格式，最大 10MB</span>
            </div>
            <input 
              type="file" 
              ref="personalImageInput" 
              style="display: none" 
              accept="image/jpeg,image/jpg,image/png,image/gif,image/webp"
              @change="handlePersonalImageUpload"
            />
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

        <!-- 教练标签管理 -->
        <div class="profile-card tags-card">
          <div class="card-header">
            <n-icon :component="PricetagsOutline" size="20" />
            <span>个人标签</span>
            <n-tooltip trigger="hover">
              <template #trigger>
                <n-icon :component="InformationCircleOutline" size="16" class="info-icon" />
              </template>
              最多可添加5个标签，每个标签最多10个字符。标签将展示在首页教练介绍中。
            </n-tooltip>
          </div>
          <div class="tags-section">
            <div v-if="!isEditing" class="tags-display">
              <n-space v-if="profile.tags?.length" size="small" wrap>
                <n-tag 
                  v-for="tag in profile.tags" 
                  :key="tag" 
                  type="warning"
                  size="medium"
                  class="coach-tag"
                >
                  {{ tag }}
                </n-tag>
              </n-space>
              <n-empty v-else description="暂无标签" />
            </div>
            <div v-else class="tags-edit">
              <n-space size="small" wrap class="selected-tags">
                <n-tag
                  v-for="(tag, index) in profileForm.tags"
                  :key="index"
                  type="warning"
                  size="medium"
                  closable
                  @close="removeTag(index)"
                >
                  {{ tag }}
                </n-tag>
              </n-space>
              <div v-if="profileForm.tags.length < 5" class="tag-input-section">
                <n-input
                  v-model:value="newTag"
                  placeholder="输入标签后按回车添加"
                  :maxlength="10"
                  @keyup.enter="addTag"
                />
                <n-button type="primary" size="small" @click="addTag">添加</n-button>
              </div>
              <n-text v-else type="warning" class="tag-limit-hint">
                已达到最大标签数量（5个）
              </n-text>
              <div class="preset-tags">
                <n-text depth="3">推荐标签：</n-text>
                <n-space size="small" wrap>
                  <n-tag
                    v-for="tag in presetTags"
                    :key="tag"
                    size="small"
                    :disabled="profileForm.tags.includes(tag) || profileForm.tags.length >= 5"
                    @click="addPresetTag(tag)"
                    class="preset-tag"
                  >
                    + {{ tag }}
                  </n-tag>
                </n-space>
              </div>
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

  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import {
  PersonOutline,
  FitnessOutline,
  InformationCircleOutline,
  RibbonOutline,
  CreateOutline,
  CameraOutline,
  AddOutline,
  TrophyOutline,
  ImageOutline,
  CloudUploadOutline,
  RefreshOutline,
  TrashOutline,
  PricetagsOutline
} from '@vicons/ionicons5'
import {
  getCurrentCoachDetail,
  updateCoachDetail,
  uploadPersonalImage,
  deletePersonalImage,
  updateTags
} from '@/api/coachDetail'

const message = useMessage()

// 编辑状态
const isEditing = ref(false)
const saving = ref(false)
const loading = ref(false)

// 弹窗状态
const showCertModal = ref(false)

// 头像上传
const avatarInput = ref(null)

// 个人展示图片上传
const personalImageInput = ref(null)

// 标签输入
const newTag = ref('')

// 预设标签
const presetTags = [
  '增肌塑形', '减脂瘦身', '体态矫正', '运动康复',
  '力量训练', '瑜伽冥想', '普拉提', 'CrossFit',
  '体能训练', '产后恢复', '青少年体适能', '老年健身'
]

// 表单数据
const profileForm = reactive({
  avatar: '',
  username: '',
  realName: '',
  gender: 'male',
  age: null,
  phone: '',
  email: '',
  personalImageUrl: '',
  tags: [],
  workYears: 0,
  specialties: [],
  teachingStyle: '',
  education: '',
  training: '',
  languages: [],
  bio: '',
  experience: '',
  honors: [],
  certifications: []
})

// 原始数据（用于取消编辑时恢复）
const originalProfile = ref(null)

// 计算属性
const profile = computed(() => isEditing.value ? profileForm : profileForm)

const genderText = computed(() => {
  const map = { male: '男', female: '女', other: '其他' }
  return map[profile.value.gender] || '-'
})

// 用户名验证
const usernameValidationStatus = computed(() => {
  if (!profileForm.username) return undefined
  const isValid = validateUsernameFormat(profileForm.username)
  return isValid ? undefined : 'error'
})

const usernameValidationMsg = computed(() => {
  if (!profileForm.username) return ''
  if (profileForm.username.length < 6) {
    return '用户名长度至少6个字符'
  }
  if (profileForm.username.length > 20) {
    return '用户名长度最多20个字符'
  }
  if (!/^[a-zA-Z0-9_]+$/.test(profileForm.username)) {
    return '用户名只能包含字母、数字和下划线'
  }
  return ''
})

// 验证用户名格式
function validateUsernameFormat(username) {
  if (!username) return false
  if (username.length < 6 || username.length > 20) return false
  return /^[a-zA-Z0-9_]+$/.test(username)
}

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

// 认证表单
const certForm = reactive({
  name: '',
  number: '',
  validDate: null
})

// 获取教练详情
async function fetchCoachDetail() {
  loading.value = true
  console.log('开始获取教练详情...')
  try {
    const data = await getCurrentCoachDetail()
    console.log('获取教练详情成功:', data)
    if (data) {
      Object.assign(profileForm, data)
      // 确保数组字段存在
      if (!profileForm.tags) profileForm.tags = []
      if (!profileForm.specialties) profileForm.specialties = []
      if (!profileForm.languages) profileForm.languages = []
      if (!profileForm.honors) profileForm.honors = []
      if (!profileForm.certifications) profileForm.certifications = []
    }
  } catch (error) {
    console.error('获取教练详情失败:', error)
    message.error('获取教练信息失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

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
  try {
    // 验证用户名
    if (!profileForm.username || !validateUsernameFormat(profileForm.username)) {
      message.error('请输入有效的用户名（6-20个字符，只能包含字母、数字和下划线）')
      saving.value = false
      return
    }

    // 先保存基本信息（包含用户名）
    const detailData = {
      username: profileForm.username,
      workYears: profileForm.workYears,
      specialties: profileForm.specialties,
      teachingStyle: profileForm.teachingStyle,
      education: profileForm.education,
      training: profileForm.training,
      languages: profileForm.languages,
      bio: profileForm.bio,
      experience: profileForm.experience,
      honors: profileForm.honors,
      certifications: profileForm.certifications
    }
    await updateCoachDetail(detailData)

    // 保存标签
    await updateTags(profileForm.tags)

    message.success('个人资料保存成功')
    isEditing.value = false
    // 重新获取数据
    await fetchCoachDetail()
  } catch (error) {
    message.error('保存失败：' + (error.message || '未知错误'))
    console.error(error)
  } finally {
    saving.value = false
  }
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
    message.success('头像上传成功（仅本地预览，需后端支持保存）')
  }
  reader.readAsDataURL(file)
}

// 个人展示图片上传
function triggerPersonalImageUpload() {
  personalImageInput.value?.click()
}

async function handlePersonalImageUpload(event) {
  const file = event.target.files[0]
  if (!file) return
  
  // 校验文件类型
  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    message.error('请上传 JPG、PNG、GIF 或 WebP 格式的图片')
    return
  }
  
  // 校验文件大小（10MB）
  const maxSize = 10 * 1024 * 1024
  if (file.size > maxSize) {
    message.error('图片大小不能超过 10MB')
    return
  }
  
  try {
    const res = await uploadPersonalImage(file)
    if (res.data) {
      profileForm.personalImageUrl = res.data
      message.success('个人展示图片上传成功')
    }
  } catch (error) {
    message.error('上传失败：' + (error.message || '未知错误'))
    console.error(error)
  }
  
  // 清空input，允许重复选择同一文件
  event.target.value = ''
}

async function handleDeletePersonalImage() {
  try {
    await deletePersonalImage()
    profileForm.personalImageUrl = ''
    message.success('个人展示图片已删除')
  } catch (error) {
    message.error('删除失败：' + (error.message || '未知错误'))
    console.error(error)
  }
}

// 标签管理
function addTag() {
  const tag = newTag.value.trim()
  if (!tag) {
    message.warning('请输入标签内容')
    return
  }
  if (tag.length > 10) {
    message.warning('标签长度不能超过10个字符')
    return
  }
  if (profileForm.tags.includes(tag)) {
    message.warning('该标签已存在')
    return
  }
  if (profileForm.tags.length >= 5) {
    message.warning('最多只能添加5个标签')
    return
  }
  profileForm.tags.push(tag)
  newTag.value = ''
}

function addPresetTag(tag) {
  if (profileForm.tags.includes(tag)) {
    message.warning('该标签已存在')
    return
  }
  if (profileForm.tags.length >= 5) {
    message.warning('最多只能添加5个标签')
    return
  }
  profileForm.tags.push(tag)
}

function removeTag(index) {
  profileForm.tags.splice(index, 1)
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

// 页面加载时获取数据
onMounted(() => {
  fetchCoachDetail()
})
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

.card-header .info-icon {
  color: #8c8c8c;
  cursor: help;
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

/* 个人展示图片区域 */
.personal-image-card .card-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.personal-image-section {
  padding: 8px 0;
}

.personal-image-preview {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.personal-image-preview img {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 8px;
}

.image-actions {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.personal-image-upload {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 40px 20px;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  color: #8c8c8c;
}

.personal-image-upload:hover {
  border-color: #52c41a;
  color: #52c41a;
}

.personal-image-upload .upload-hint {
  font-size: 12px;
  color: #bfbfbf;
}

/* 标签区域 */
.tags-card .card-header :deep(.n-icon) {
  color: #faad14;
}

.tags-section {
  padding: 8px 0;
}

.tags-display {
  min-height: 40px;
}

.coach-tag {
  font-size: 14px;
  padding: 4px 12px;
}

.tags-edit {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.selected-tags {
  min-height: 32px;
}

.tag-input-section {
  display: flex;
  gap: 8px;
}

.tag-input-section .n-input {
  flex: 1;
}

.tag-limit-hint {
  font-size: 13px;
}

.preset-tags {
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.preset-tags .n-text {
  display: block;
  margin-bottom: 8px;
}

.preset-tag {
  cursor: pointer;
  transition: all 0.3s;
}

.preset-tag:hover:not(.n-tag--disabled) {
  background-color: #52c41a;
  color: white;
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
