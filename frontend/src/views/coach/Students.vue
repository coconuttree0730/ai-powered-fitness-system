<template>
  <div class="coach-students">
    <!-- 页面标题与搜索 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">我的学员</h2>
        <span class="student-count">共 {{ students.length }} 位学员</span>
      </div>
      <div class="header-right">
        <n-input 
          v-model:value="searchKeyword" 
          placeholder="搜索学员姓名或手机号..."
          clearable
          style="width: 280px"
        >
          <template #prefix>
            <n-icon :component="SearchOutline" />
          </template>
        </n-input>
        <n-select 
          v-model:value="sortBy" 
          :options="sortOptions" 
          style="width: 140px"
        />
      </div>
    </div>

    <!-- 学员卡片列表 -->
    <div v-if="filteredStudents.length === 0" class="empty-state">
      <n-empty description="暂无学员数据" />
    </div>
    
    <div v-else class="students-list">
      <div 
        v-for="student in filteredStudents" 
        :key="student.id" 
        class="student-card"
        :class="{ 'low-credits': student.remainingCourses < 5 }"
      >
        <!-- 学员头像 -->
        <div class="avatar-section">
          <div class="avatar-wrapper">
            <img 
              v-if="student.avatar" 
              :src="student.avatar" 
              class="student-avatar"
              @click="previewAvatar(student)"
            />
            <div v-else class="avatar-placeholder" :style="{ background: student.avatarColor }">
              {{ student.realName.charAt(0) }}
            </div>
            <div v-if="student.unreadCount > 0" class="unread-badge">
              {{ student.unreadCount > 99 ? '99+' : student.unreadCount }}
            </div>
          </div>
          <input 
            type="file" 
            ref="fileInput" 
            style="display: none" 
            accept="image/*"
            @change="(e) => handleAvatarUpload(e, student)"
          />
        </div>

        <!-- 学员信息 -->
        <div class="info-section">
          <div class="name-row">
            <h3 class="student-name">{{ student.realName }}</h3>
            <n-tag v-if="student.remainingCourses < 5" type="error" size="small">
              课时不足
            </n-tag>
          </div>
          
          <div class="phone-row" @click="handleCall(student.phone)">
            <n-icon :component="CallOutline" size="14" />
            <span class="phone-number">{{ formatPhone(student.phone) }}</span>
          </div>
          
          <div class="courses-row">
            <span class="courses-label">剩余课时</span>
            <span class="courses-value" :class="{ 'warning': student.remainingCourses < 5 }">
              {{ student.remainingCourses }}
            </span>
            <span class="courses-unit">节</span>
          </div>
          
          <div class="meta-row">
            <span class="meta-item">
              <n-icon :component="CalendarOutline" size="12" />
              最近上课: {{ student.lastClass || '暂无' }}
            </span>
          </div>
        </div>

        <!-- 操作区域 -->
        <div class="action-section">
          <n-tooltip trigger="hover">
            <template #trigger>
              <button 
                class="action-btn message" 
                :class="{ 'has-unread': student.unreadCount > 0 }"
                @click="openChat(student)"
              >
                <n-icon :component="ChatbubbleOutline" size="18" />
                <span v-if="student.unreadCount > 0" class="btn-badge"></span>
              </button>
            </template>
            发送私信
          </n-tooltip>
          
          <n-tooltip trigger="hover">
            <template #trigger>
              <button class="action-btn detail" @click="viewDetail(student)">
                <n-icon :component="EyeOutline" size="18" />
              </button>
            </template>
            查看详情
          </n-tooltip>
          
          <n-tooltip trigger="hover">
            <template #trigger>
              <button class="action-btn add-schedule" @click="openScheduleModal(student)">
                <n-icon :component="AddOutline" size="18" />
              </button>
            </template>
            添加到日程
          </n-tooltip>
        </div>
      </div>
    </div>

    <!-- 聊天对话框 -->
    <n-modal 
      v-model:show="showChatModal" 
      preset="card" 
      :title="`与 ${currentChatStudent?.realName} 的对话`"
      style="width: 500px; height: 600px"
      :mask-closable="false"
    >
      <div class="chat-container">
        <!-- 消息列表 -->
        <div ref="messageListRef" class="message-list">
          <div 
            v-for="msg in currentMessages" 
            :key="msg.id" 
            class="message-item"
            :class="{ 'self': msg.isSelf }"
          >
            <div class="message-avatar">
              <img v-if="msg.isSelf" :src="coachAvatar" class="msg-avatar" />
              <img v-else :src="currentChatStudent?.avatar || defaultAvatar" class="msg-avatar" />
            </div>
            <div class="message-content">
              <div class="message-bubble">{{ msg.content }}</div>
              <div class="message-time">{{ msg.time }}</div>
            </div>
          </div>
        </div>
        
        <!-- 输入区域 -->
        <div class="chat-input-area">
          <n-input
            v-model:value="chatInput"
            type="textarea"
            :rows="3"
            placeholder="输入消息..."
            @keydown.enter.prevent="sendMessage"
          />
          <n-button type="primary" :disabled="!chatInput.trim()" @click="sendMessage">
            发送
          </n-button>
        </div>
      </div>
    </n-modal>

    <!-- 学员详情弹窗 -->
    <n-modal 
      v-model:show="showDetailModal" 
      preset="card" 
      :title="`${currentStudent?.realName} 的详情`"
      style="width: 600px"
    >
      <n-descriptions bordered :column="2" v-if="currentStudent">
        <n-descriptions-item label="姓名">{{ currentStudent.realName }}</n-descriptions-item>
        <n-descriptions-item label="手机号">{{ currentStudent.phone }}</n-descriptions-item>
        <n-descriptions-item label="剩余课时">{{ currentStudent.remainingCourses }} 节</n-descriptions-item>
        <n-descriptions-item label="已上课时">{{ currentStudent.completedCourses }} 节</n-descriptions-item>
        <n-descriptions-item label="入会日期">{{ currentStudent.joinDate }}</n-descriptions-item>
        <n-descriptions-item label="最近上课">{{ currentStudent.lastClass || '暂无' }}</n-descriptions-item>
        <n-descriptions-item label="健身目标" :span="2">{{ currentStudent.fitnessGoal || '未设置' }}</n-descriptions-item>
        <n-descriptions-item label="备注" :span="2">{{ currentStudent.notes || '暂无备注' }}</n-descriptions-item>
      </n-descriptions>
      
      <template #footer>
        <n-space justify="end">
          <n-button @click="showDetailModal = false">关闭</n-button>
          <n-button type="primary" @click="openChat(currentStudent)">
            <template #icon>
              <n-icon :component="ChatbubbleOutline" />
            </template>
            发送私信
          </n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 添加到课程日程弹窗 -->
    <n-modal 
      v-model:show="showScheduleModal" 
      preset="card" 
      :title="`为 ${scheduleForm.studentName} 安排课程`"
      style="width: 550px"
      :mask-closable="false"
    >
      <n-form 
        ref="scheduleFormRef"
        :model="scheduleForm" 
        :rules="scheduleRules"
        label-placement="left" 
        label-width="100"
        size="large"
      >
        <!-- 日期选择 -->
        <n-form-item label="训练日期" path="date">
          <n-date-picker 
            v-model:value="scheduleForm.date" 
            type="date" 
            style="width: 100%"
            placeholder="请选择训练日期"
            :is-date-disabled="disablePastDate"
          />
        </n-form-item>

        <!-- 时间选择 -->
        <n-grid :cols="2" :x-gap="16">
          <n-grid-item>
            <n-form-item label="开始时间" path="startTime">
              <n-time-picker 
                v-model:value="scheduleForm.startTime" 
                format="HH:mm"
                style="width: 100%"
                placeholder="开始时间"
              />
            </n-form-item>
          </n-grid-item>
          <n-grid-item>
            <n-form-item label="结束时间" path="endTime">
              <n-time-picker 
                v-model:value="scheduleForm.endTime" 
                format="HH:mm"
                style="width: 100%"
                placeholder="结束时间"
              />
            </n-form-item>
          </n-grid-item>
        </n-grid>

        <!-- 场地选择 -->
        <n-form-item label="训练场地" path="venue">
          <n-select 
            v-model:value="scheduleForm.venue" 
            :options="venueOptions"
            placeholder="请选择训练场地"
            filterable
            tag
          />
        </n-form-item>

        <!-- 课程类型 -->
        <n-form-item label="课程类型" path="courseType">
          <n-radio-group v-model:value="scheduleForm.courseType">
            <n-space>
              <n-radio value="private">私教课</n-radio>
              <n-radio value="public">公开课</n-radio>
            </n-space>
          </n-radio-group>
        </n-form-item>

        <!-- 训练描述 -->
        <n-form-item label="训练描述" path="description">
          <n-input 
            v-model:value="scheduleForm.description" 
            type="textarea" 
            :rows="4"
            placeholder="请详细描述当天为该学员安排的训练动作、训练目标及注意事项..."
          />
        </n-form-item>

        <!-- 提醒设置 -->
        <n-form-item label="提醒设置">
          <n-checkbox v-model:checked="scheduleForm.enableReminder">
            课程开始前提醒学员
          </n-checkbox>
          <n-select 
            v-if="scheduleForm.enableReminder"
            v-model:value="scheduleForm.reminderTime" 
            :options="reminderOptions"
            style="width: 150px; margin-left: 12px;"
            size="small"
          />
        </n-form-item>
      </n-form>

      <template #footer>
        <n-space justify="end">
          <n-button size="large" @click="showScheduleModal = false">取消</n-button>
          <n-button 
            type="primary" 
            size="large" 
            :loading="submittingSchedule"
            @click="submitSchedule"
          >
            确认添加
          </n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 添加成功提示 -->
    <n-modal v-model:show="showSuccessModal" preset="dialog" title="添加成功">
      <div class="success-content">
        <n-icon :component="CheckmarkCircleOutline" size="48" color="#52c41a" />
        <p>课程日程已成功添加！</p>
        <p class="success-detail">学员将收到课程提醒通知</p>
      </div>
      <template #action>
        <n-space>
          <n-button @click="showSuccessModal = false">关闭</n-button>
          <n-button type="primary" @click="goToSchedule">查看日程</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, h } from 'vue'
import { useMessage, useDialog } from 'naive-ui'
import { useRouter } from 'vue-router'
import { useScheduleStore } from '@/stores/schedule'
import {
  SearchOutline,
  CallOutline,
  ChatbubbleOutline,
  EyeOutline,
  CalendarOutline,
  PersonAddOutline,
  TrashOutline,
  WarningOutline,
  CheckmarkCircleOutline,
  AddOutline
} from '@vicons/ionicons5'

const message = useMessage()
const dialog = useDialog()
const router = useRouter()
const scheduleStore = useScheduleStore()

// 搜索和排序
const searchKeyword = ref('')
const sortBy = ref('remainingCourses')

// 弹窗状态
const showChatModal = ref(false)
const showDetailModal = ref(false)
const showScheduleModal = ref(false)
const showSuccessModal = ref(false)
const currentChatStudent = ref(null)
const currentStudent = ref(null)
const chatInput = ref('')
const messageListRef = ref(null)
const scheduleFormRef = ref(null)
const submittingSchedule = ref(false)

// 教练头像
const coachAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=coach'
const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'

// 排序选项
const sortOptions = [
  { label: '剩余课时', value: 'remainingCourses' },
  { label: '最近上课', value: 'lastClass' },
  { label: '入会时间', value: 'joinDate' },
  { label: '姓名排序', value: 'name' }
]

// 更多操作选项
const moreOptions = [
  { label: '添加到日程', key: 'addToSchedule', icon: () => h('span', '') },
  { label: '添加课时', key: 'addCredits', icon: () => h('span', '') },
  { label: '编辑备注', key: 'editNote', icon: () => h('span', '') },
  { label: '查看记录', key: 'viewRecords', icon: () => h('span', '') },
  { type: 'divider', key: 'd1' },
  { label: '移除学员', key: 'remove', icon: () => h('span', '') }
]

// 场地选项
const venueOptions = [
  { label: 'A区 - 力量训练区', value: 'A区 - 力量训练区' },
  { label: 'B区 - 有氧训练区', value: 'B区 - 有氧训练区' },
  { label: 'C区 - 私教训练室', value: 'C区 - 私教训练室' },
  { label: 'D区 - 瑜伽室', value: 'D区 - 瑜伽室' },
  { label: 'E区 - 游泳池', value: 'E区 - 游泳池' },
  { label: 'F区 - 多功能厅', value: 'F区 - 多功能厅' }
]

// 提醒选项
const reminderOptions = [
  { label: '提前15分钟', value: 15 },
  { label: '提前30分钟', value: 30 },
  { label: '提前1小时', value: 60 },
  { label: '提前2小时', value: 120 },
  { label: '提前1天', value: 1440 }
]

// 日程表单
const scheduleForm = ref({
  studentId: null,
  studentName: '',
  date: null,
  startTime: null,
  endTime: null,
  venue: null,
  courseType: 'private',
  description: '',
  enableReminder: true,
  reminderTime: 30
})

// 表单验证规则
const scheduleRules = {
  date: { required: true, message: '请选择训练日期', trigger: 'change' },
  startTime: { required: true, message: '请选择开始时间', trigger: 'change' },
  endTime: { required: true, message: '请选择结束时间', trigger: 'change' },
  venue: { required: true, message: '请选择训练场地', trigger: 'change' }
}

// 模拟学员数据
const students = ref([
  { 
    id: 1, 
    realName: '王小明', 
    phone: '13800138001', 
    remainingCourses: 12,
    completedCourses: 28,
    avatar: null,
    avatarColor: 'linear-gradient(135deg, #FF6B35, #FF8C61)',
    unreadCount: 3,
    lastClass: '2024-10-20',
    joinDate: '2024-01-15',
    fitnessGoal: '增肌塑形',
    notes: '注意肩部旧伤'
  },
  { 
    id: 2, 
    realName: '李小红', 
    phone: '13800138002', 
    remainingCourses: 3,
    completedCourses: 45,
    avatar: null,
    avatarColor: 'linear-gradient(135deg, #06D6A0, #2EC4B6)',
    unreadCount: 0,
    lastClass: '2024-10-18',
    joinDate: '2023-08-20',
    fitnessGoal: '减脂瘦身',
    notes: ''
  },
  { 
    id: 3, 
    realName: '张大力', 
    phone: '13800138003', 
    remainingCourses: 8,
    completedCourses: 15,
    avatar: null,
    avatarColor: 'linear-gradient(135deg, #667eea, #764ba2)',
    unreadCount: 1,
    lastClass: '2024-10-22',
    joinDate: '2024-03-10',
    fitnessGoal: '力量提升',
    notes: '深蹲动作需纠正'
  },
  { 
    id: 4, 
    realName: '刘美丽', 
    phone: '13800138004', 
    remainingCourses: 2,
    completedCourses: 38,
    avatar: null,
    avatarColor: 'linear-gradient(135deg, #FFD166, #FFB347)',
    unreadCount: 0,
    lastClass: '2024-10-15',
    joinDate: '2023-11-05',
    fitnessGoal: '瑜伽塑形',
    notes: '柔韧性较好'
  },
  { 
    id: 5, 
    realName: '陈健康', 
    phone: '13800138005', 
    remainingCourses: 20,
    completedCourses: 5,
    avatar: null,
    avatarColor: 'linear-gradient(135deg, #EF476F, #FF6B9D)',
    unreadCount: 0,
    lastClass: '2024-10-23',
    joinDate: '2024-09-01',
    fitnessGoal: '体能恢复',
    notes: '术后康复训练'
  },
  { 
    id: 6, 
    realName: '赵健身', 
    phone: '13800138006', 
    remainingCourses: 6,
    completedCourses: 22,
    avatar: null,
    avatarColor: 'linear-gradient(135deg, #118AB2, #06B6D4)',
    unreadCount: 5,
    lastClass: '2024-10-21',
    joinDate: '2024-02-14',
    fitnessGoal: '综合训练',
    notes: ''
  }
])

// 消息记录
const messagesMap = ref(new Map())

// 当前对话消息
const currentMessages = computed(() => {
  if (!currentChatStudent.value) return []
  return messagesMap.value.get(currentChatStudent.value.id) || []
})

// 筛选和排序后的学员列表
const filteredStudents = computed(() => {
  let result = students.value
  
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(s => 
      s.realName.toLowerCase().includes(keyword) || 
      s.phone.includes(keyword)
    )
  }
  
  // 排序
  result = [...result].sort((a, b) => {
    switch (sortBy.value) {
      case 'remainingCourses':
        return a.remainingCourses - b.remainingCourses
      case 'lastClass':
        return (b.lastClass || '').localeCompare(a.lastClass || '')
      case 'joinDate':
        return new Date(b.joinDate) - new Date(a.joinDate)
      case 'name':
        return a.realName.localeCompare(b.realName)
      default:
        return 0
    }
  })
  
  return result
})

// 格式化手机号
function formatPhone(phone) {
  return phone.replace(/(\d{3})(\d{4})(\d{4})/, '$1****$3')
}

// 拨打电话
function handleCall(phone) {
  dialog.info({
    title: '拨打电话',
    content: `是否拨打 ${phone}？`,
    positiveText: '拨打',
    negativeText: '取消',
    onPositiveClick: () => {
      window.location.href = `tel:${phone}`
    }
  })
}

// 预览头像
function previewAvatar(student) {
  if (student.avatar) {
    dialog.info({
      title: student.realName,
      content: () => h('img', { src: student.avatar, style: 'width: 100%; border-radius: 8px;' })
    })
  }
}

// 处理头像上传
function handleAvatarUpload(event, student) {
  const file = event.target.files[0]
  if (!file) return
  
  if (!file.type.startsWith('image/')) {
    message.error('请上传图片文件')
    return
  }
  
  const reader = new FileReader()
  reader.onload = (e) => {
    student.avatar = e.target.result
    message.success('头像上传成功')
  }
  reader.readAsDataURL(file)
}

// 打开聊天
function openChat(student) {
  if (!student) return
  currentChatStudent.value = student
  showChatModal.value = true
  showDetailModal.value = false
  
  // 初始化消息记录
  if (!messagesMap.value.has(student.id)) {
    messagesMap.value.set(student.id, [
      { id: 1, content: '教练您好，我想预约下周的课程', isSelf: false, time: '10:30' },
      { id: 2, content: '好的，请问您想预约什么时间？', isSelf: true, time: '10:35' }
    ])
  }
  
  // 清除未读标记
  student.unreadCount = 0
  
  nextTick(() => {
    scrollToBottom()
  })
}

// 发送消息
function sendMessage() {
  if (!chatInput.value.trim() || !currentChatStudent.value) return
  
  const messages = messagesMap.value.get(currentChatStudent.value.id) || []
  messages.push({
    id: Date.now(),
    content: chatInput.value.trim(),
    isSelf: true,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  })
  
  messagesMap.value.set(currentChatStudent.value.id, messages)
  chatInput.value = ''
  
  nextTick(() => {
    scrollToBottom()
  })
  
  // 模拟对方回复
  setTimeout(() => {
    messages.push({
      id: Date.now() + 1,
      content: '收到，谢谢教练！',
      isSelf: false,
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    })
    nextTick(() => scrollToBottom())
  }, 2000)
}

// 滚动到底部
function scrollToBottom() {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

// 查看详情
function viewDetail(student) {
  currentStudent.value = student
  showDetailModal.value = true
}

// 设置当前学员（用于下拉菜单）
function setCurrentStudent(student) {
  currentStudent.value = student
}

// 处理更多选项选择
function handleMoreSelect(key, student) {
  // 确保有当前学员
  const targetStudent = student || currentStudent.value
  if (!targetStudent) {
    message.error('请先选择学员')
    return
  }
  
  switch (key) {
    case 'addToSchedule':
      openScheduleModal(targetStudent)
      break
    case 'addCredits':
      message.success('添加课时功能')
      break
    case 'editNote':
      message.success('编辑备注功能')
      break
    case 'viewRecords':
      message.success('查看记录功能')
      break
    case 'remove':
      dialog.warning({
        title: '确认移除',
        content: `确定要移除学员 ${targetStudent.realName} 吗？`,
        positiveText: '确认',
        negativeText: '取消',
        onPositiveClick: () => {
          const index = students.value.findIndex(s => s.id === targetStudent.id)
          if (index > -1) {
            students.value.splice(index, 1)
            message.success('已移除学员')
          }
        }
      })
      break
  }
}

// 打开添加日程弹窗
function openScheduleModal(student) {
  if (!student) return
  
  // 重置表单
  scheduleForm.value = {
    studentId: student.id,
    studentName: student.realName,
    date: new Date().setHours(0, 0, 0, 0),
    startTime: null,
    endTime: null,
    venue: null,
    courseType: 'private',
    description: '',
    enableReminder: true,
    reminderTime: 30
  }
  
  showScheduleModal.value = true
}

// 禁用过去的日期
function disablePastDate(ts) {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return ts < today.getTime()
}

// 提交日程
async function submitSchedule() {
  try {
    await scheduleFormRef.value?.validate()
    
    // 验证时间逻辑
    if (scheduleForm.value.startTime && scheduleForm.value.endTime) {
      const start = new Date(scheduleForm.value.startTime)
      const end = new Date(scheduleForm.value.endTime)
      
      if (start >= end) {
        message.error('结束时间必须晚于开始时间')
        return
      }
      
      // 验证课程时长（至少15分钟）
      const duration = (end - start) / 1000 / 60
      if (duration < 15) {
        message.error('课程时长至少为15分钟')
        return
      }
      
      // 验证课程时长（不超过4小时）
      if (duration > 240) {
        message.error('单次课程时长不能超过4小时')
        return
      }
    }
    
    submittingSchedule.value = true
    
    // 模拟提交
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 构建日程数据
    const scheduleData = {
      studentId: scheduleForm.value.studentId,
      studentName: scheduleForm.value.studentName,
      date: new Date(scheduleForm.value.date).toISOString().split('T')[0],
      startTime: new Date(scheduleForm.value.startTime).toTimeString().slice(0, 5),
      endTime: new Date(scheduleForm.value.endTime).toTimeString().slice(0, 5),
      venue: scheduleForm.value.venue,
      courseType: scheduleForm.value.courseType,
      description: scheduleForm.value.description,
      enableReminder: scheduleForm.value.enableReminder,
      reminderTime: scheduleForm.value.reminderTime
    }
    
    // 使用 store 添加日程（同步到课程日程）
    scheduleStore.addSchedule(scheduleData)
    
    submittingSchedule.value = false
    showScheduleModal.value = false
    showSuccessModal.value = true
    
    message.success('课程日程添加成功')
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

// 跳转到日程页面
function goToSchedule() {
  showSuccessModal.value = false
  router.push('/coach/schedule')
}
</script>

<style scoped>
.coach-students {
  max-width: 1400px;
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
  align-items: baseline;
  gap: 12px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1A1A2E;
  margin: 0;
}

.student-count {
  font-size: 14px;
  color: #6B7280;
}

.header-right {
  display: flex;
  gap: 12px;
}

/* 学员列表 */
.students-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 学员卡片 */
.student-card {
  display: flex;
  align-items: center;
  gap: 20px;
  background: white;
  border-radius: 16px;
  padding: 20px 24px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.06);
  transition: all 0.3s ease;
  border-left: 4px solid transparent;
}

.student-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(0,0,0,0.1);
}

.student-card.low-credits {
  border-left-color: #EF476F;
  background: linear-gradient(90deg, rgba(239,71,111,0.03) 0%, white 100%);
}

/* 头像区域 */
.avatar-section {
  position: relative;
}

.avatar-wrapper {
  position: relative;
  width: 72px;
  height: 72px;
}

.student-avatar,
.avatar-placeholder {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  object-fit: cover;
  cursor: pointer;
  transition: all 0.3s;
}

.avatar-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 600;
  color: white;
}

.student-avatar:hover,
.avatar-placeholder:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 16px rgba(0,0,0,0.15);
}

.unread-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  background: #EF476F;
  color: white;
  font-size: 11px;
  font-weight: 600;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid white;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

/* 信息区域 */
.info-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.student-name {
  font-size: 18px;
  font-weight: 700;
  color: #1A1A2E;
  margin: 0;
}

.phone-row {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #6B7280;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  width: fit-content;
}

.phone-row:hover {
  color: #FF6B35;
}

.courses-row {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.courses-label {
  font-size: 13px;
  color: #6B7280;
}

.courses-value {
  font-size: 24px;
  font-weight: 700;
  color: #06D6A0;
  line-height: 1;
}

.courses-value.warning {
  color: #EF476F;
}

.courses-unit {
  font-size: 13px;
  color: #6B7280;
}

.meta-row {
  display: flex;
  gap: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #9CA3AF;
}

/* 操作区域 */
.action-section {
  display: flex;
  gap: 10px;
}

.action-btn {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
  color: white;
}

.action-btn.message {
  background: linear-gradient(135deg, #FF6B35, #FF8C61);
}

.action-btn.message:hover,
.action-btn.message.has-unread {
  box-shadow: 0 4px 12px rgba(255,107,53,0.4);
  transform: translateY(-2px);
}

.action-btn.detail {
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.action-btn.detail:hover {
  box-shadow: 0 4px 12px rgba(102,126,234,0.4);
  transform: translateY(-2px);
}

.action-btn.add-schedule {
  background: #52c41a;
  color: white;
}

.action-btn.add-schedule:hover {
  background: #73d13d;
  transform: translateY(-2px);
}

.btn-badge {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 8px;
  height: 8px;
  background: #FFD166;
  border-radius: 50%;
  animation: blink 1.5s infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* 空状态 */
.empty-state {
  padding: 80px 0;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.06);
}

/* 聊天容器 */
.chat-container {
  display: flex;
  flex-direction: column;
  height: 480px;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: #F8FAFC;
  border-radius: 12px;
  margin-bottom: 16px;
}

.message-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.message-item.self {
  flex-direction: row-reverse;
}

.message-avatar .msg-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
}

.message-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-width: 70%;
}

.message-item.self .message-content {
  align-items: flex-end;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
}

.message-item:not(.self) .message-bubble {
  background: white;
  color: #1A1A2E;
  border-bottom-left-radius: 4px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.message-item.self .message-bubble {
  background: linear-gradient(135deg, #FF6B35, #FF8C61);
  color: white;
  border-bottom-right-radius: 4px;
}

.message-time {
  font-size: 11px;
  color: #9CA3AF;
}

.chat-input-area {
  display: flex;
  gap: 12px;
}

.chat-input-area .n-input {
  flex: 1;
}

/* 成功提示样式 */
.success-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
  text-align: center;
}

.success-content p {
  margin: 8px 0 0;
  font-size: 16px;
  font-weight: 600;
  color: #1A1A2E;
}

.success-content .success-detail {
  font-size: 14px;
  color: #6B7280;
  font-weight: normal;
}

/* 响应式 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: stretch;
  }
  
  .header-right {
    flex-direction: column;
  }
  
  .student-card {
    flex-direction: column;
    text-align: center;
    padding: 24px;
  }
  
  .info-section {
    align-items: center;
  }
  
  .meta-row {
    justify-content: center;
  }
}
</style>
