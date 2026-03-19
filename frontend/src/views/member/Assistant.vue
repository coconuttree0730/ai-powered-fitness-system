<template>
  <div class="assistant-page">
    <div class="chat-wrapper">
      <div class="chat-container">
        <div class="chat-messages" ref="messageContainer">
          <div v-for="(msg, index) in messages" :key="index" 
               :class="['message', msg.type]">
            <div class="message-avatar">{{ msg.type === 'ai' ? '🤖' : userAvatar }}</div>
            <div class="message-content">
              <div v-if="msg.isPlan" class="plan-message">
                <div class="plan-header">
                  <span>📋 {{ msg.title }}</span>
                </div>
                <div class="plan-content">
                  <div v-for="(day, idx) in msg.planDays" :key="idx" class="plan-day">
                    <strong>{{ day.day }}:</strong> {{ day.content }}
                  </div>
                </div>
                <div class="plan-actions">
                  <n-button type="primary" size="small" @click="usePlan">使用此计划</n-button>
                  <n-button size="small" @click="regeneratePlan">重新生成</n-button>
                </div>
              </div>
              <div v-else v-html="formatMessage(msg.content)"></div>
            </div>
          </div>
        </div>
        
        <div class="chat-input-area">
          <n-input 
            v-model:value="inputMessage"
            type="text" 
            class="chat-input"
            placeholder="输入您的问题..."
            size="large"
            :disabled="sending"
            @keydown.enter="sendMessage"
          />
          <n-button 
            type="primary" 
            size="large" 
            class="btn-send"
            :loading="sending"
            @click="sendMessage"
          >
            <template #icon>
              <n-icon :component="SendOutline" />
            </template>
            发送
          </n-button>
        </div>
      </div>
    </div>

    <!-- 侧边栏 -->
    <div v-if="!isMobile" class="sidebar">
      <!-- 推荐问题 -->
      <div class="card-section quick-questions-section">
        <h3 class="section-title">
          <n-icon :component="HelpCircleOutline" size="18" />
          推荐问题
        </h3>
        <div class="quick-questions">
          <span 
            v-for="question in quickQuestions" 
            :key="question"
            class="quick-question"
            @click="askQuestion(question)"
          >
            {{ question }}
          </span>
        </div>
      </div>

      <!-- 人气教练 -->
      <div class="card-section coaches-section">
        <h3 class="section-title">
          <n-icon :component="TrophyOutline" size="18" />
          人气教练 TOP3
        </h3>
        <div class="coach-list">
          <div v-for="(coach, index) in topCoaches" :key="coach.id" class="coach-item" @click="viewCoach(coach.id)">
            <div class="coach-rank" :class="`rank-${index + 1}`">{{ index + 1 }}</div>
            <n-avatar 
              :size="48" 
              :src="coach.avatar" 
              class="coach-avatar"
              fallback-src="https://api.dicebear.com/7.x/avataaars/svg?seed=coach"
            />
            <div class="coach-info">
              <div class="coach-name">
                {{ coach.name }}
                <n-tag v-if="index === 0" type="error" size="tiny" round class="top-tag">金牌</n-tag>
              </div>
              <div class="coach-specialty">{{ coach.specialty }}</div>
              <div class="coach-stats">
                <n-rate :value="coach.rating" readonly size="small" allow-half />
                <span class="rating-score">{{ coach.rating }}分</span>
              </div>
            </div>
          </div>
        </div>
        <n-button text type="primary" class="view-all-btn" @click="goToCoaches">
          查看全部教练
          <n-icon :component="ChevronForwardOutline" />
        </n-button>
      </div>
    </div>

    <!-- 移动端底部快捷问题 -->
    <div v-if="isMobile" class="mobile-quick-questions">
      <div class="quick-questions-scroll">
        <span 
          v-for="question in quickQuestions.slice(0, 4)" 
          :key="question"
          class="quick-question"
          @click="askQuestion(question)"
        >
          {{ question }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, onUnmounted, computed } from 'vue'
import { useMessage, NIcon } from 'naive-ui'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  SendOutline,
  HelpCircleOutline,
  TrophyOutline,
  ChevronForwardOutline
} from '@vicons/ionicons5'

const router = useRouter()
const message = useMessage()
const authStore = useAuthStore()
const messageContainer = ref(null)

// 响应式适配
const isMobile = ref(false)

function checkScreenSize() {
  isMobile.value = window.innerWidth < 768
}

onMounted(() => {
  checkScreenSize()
  window.addEventListener('resize', checkScreenSize)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkScreenSize)
})

const userAvatar = computed(() => authStore.userInfo?.username?.charAt(0) || '张')

const inputMessage = ref('')
const sending = ref(false)

const messages = ref([
  {
    type: 'ai',
    content: '您好！我是健小助，您的智能健身助手 💪<br><br>我可以帮您：<br>• 制定个性化训练计划<br>• 解答健身相关问题<br>• 介绍健身房器械使用方法<br>• 提供饮食和营养建议<br><br>有什么可以帮您的吗？'
  }
])

const quickQuestions = [
  '如何增肌？',
  '减脂饮食建议',
  '器械使用指南',
  '训练频率建议',
  '蛋白粉怎么选',
  '热身动作推荐'
]

// 人气教练 TOP3（评分排名前3）
const topCoaches = ref([
  {
    id: 1,
    name: '王教练',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=wang',
    specialty: '增肌塑形',
    rating: 4.9,
    students: 128
  },
  {
    id: 2,
    name: '李教练',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=li',
    specialty: '减脂瘦身',
    rating: 4.8,
    students: 96
  },
  {
    id: 3,
    name: '张教练',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhang',
    specialty: '瑜伽普拉提',
    rating: 4.7,
    students: 85
  }
])

// 模拟从API加载人气教练
async function loadTopCoaches() {
  // 实际项目中调用后端API获取评分排名前3的教练
  // const res = await fetch('/api/coaches/top?limit=3&sort=rating')
  // topCoaches.value = await res.json()
}

onMounted(() => {
  loadTopCoaches()
})

function formatMessage(content) {
  return content.replace(/\n/g, '<br>')
}

function scrollToBottom() {
  nextTick(() => {
    if (messageContainer.value) {
      messageContainer.value.scrollTop = messageContainer.value.scrollHeight
    }
  })
}

function sendMessage() {
  if (!inputMessage.value.trim()) return
  
  const userMsg = inputMessage.value.trim()
  messages.value.push({ type: 'user', content: userMsg })
  inputMessage.value = ''
  sending.value = true
  scrollToBottom()
  
  setTimeout(() => {
    const aiResponse = generateAIResponse(userMsg)
    messages.value.push(aiResponse)
    sending.value = false
    scrollToBottom()
  }, 1000)
}

function generateAIResponse(userMsg) {
  if (userMsg.includes('减脂') || userMsg.includes('减肥')) {
    return {
      type: 'ai',
      isPlan: true,
      title: '7天减脂训练计划',
      planDays: [
        { day: 'Day 1', content: '有氧燃脂 + 核心训练 (45min)' },
        { day: 'Day 2', content: '上肢力量训练 (50min)' },
        { day: 'Day 3', content: '休息或轻度拉伸 (20min)' },
        { day: 'Day 4', content: 'HIIT高强度间歇 (30min)' },
        { day: 'Day 5', content: '下肢力量训练 (50min)' },
        { day: 'Day 6', content: '全身循环训练 (45min)' },
        { day: 'Day 7', content: '主动恢复 - 瑜伽/散步 (30min)' }
      ]
    }
  } else if (userMsg.includes('增肌')) {
    return {
      type: 'ai',
      isPlan: true,
      title: '7天增肌训练计划',
      planDays: [
        { day: 'Day 1', content: '胸部 + 三头肌训练 (60min)' },
        { day: 'Day 2', content: '背部 + 二头肌训练 (60min)' },
        { day: 'Day 3', content: '休息或轻度有氧 (30min)' },
        { day: 'Day 4', content: '肩部 + 腹部训练 (50min)' },
        { day: 'Day 5', content: '腿部训练 - 深蹲为主 (70min)' },
        { day: 'Day 6', content: '全身力量训练 (60min)' },
        { day: 'Day 7', content: '完全休息或轻度拉伸' }
      ]
    }
  } else {
    return {
      type: 'ai',
      content: '感谢您的提问！关于"' + userMsg + '"，我建议您：<br><br>1. 制定明确的健身目标<br>2. 保持规律的训练频率（每周3-5次）<br>3. 注意饮食营养均衡<br>4. 保证充足的休息时间<br><br>如需更详细的建议，可以告诉我您的具体情况，我会为您量身定制方案！'
    }
  }
}

function askQuestion(question) {
  inputMessage.value = question
  sendMessage()
}

function usePlan() {
  message.success('训练计划已保存到您的健身计划！')
}

function regeneratePlan() {
  message.info('正在重新生成计划...')
  setTimeout(() => {
    const newPlan = generateAIResponse('帮我生成一个不同的训练计划')
    messages.value.push(newPlan)
    scrollToBottom()
  }, 1000)
}

function viewCoach(coachId) {
  router.push(`/member/coach?id=${coachId}`)
}

function goToCoaches() {
  router.push('/member/coach')
}
</script>

<style scoped>
/* 页面根容器 - 占满整个视口 */
.assistant-page {
  position: fixed;
  top: 70px; /* header高度 */
  left: 220px; /* sidebar宽度 */
  right: 0;
  bottom: 0;
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #FAFBFC;
  overflow: hidden;
}

/* 聊天区域包装器 - 固定高度 */
.chat-wrapper {
  flex: 1;
  height: calc(100vh - 70px - 60px); /* 视口高度 - header - 底部留白 */
  min-height: 400px;
}

/* 聊天容器 */
.chat-container {
  background: white;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 20px rgba(0,0,0,0.06);
  height: 100%;
  overflow: hidden;
}

/* 聊天消息区域 - 可滚动 */
.chat-messages {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  min-height: 0;
}

.message {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 18px;
  font-weight: 600;
}

.message.ai .message-avatar {
  background: linear-gradient(135deg, #FF6B35, #FF8C61);
  color: white;
}

.message.user .message-avatar {
  background: #F0F2F5;
  color: #1A1A2E;
}

.message-content {
  max-width: 70%;
  padding: 14px 18px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.6;
}

.message.ai .message-content {
  background: #F0F2F5;
  color: #1A1A2E;
  border-bottom-left-radius: 4px;
}

.message.user .message-content {
  background: linear-gradient(135deg, #FF6B35, #E55A2B);
  color: white;
  border-bottom-right-radius: 4px;
}

.plan-message {
  background: white;
  border-radius: 12px;
  padding: 16px;
  border: 1px solid #E5E7EB;
}

.plan-header {
  font-weight: 600;
  font-size: 16px;
  margin-bottom: 12px;
  color: #1A1A2E;
}

.plan-content {
  margin-bottom: 16px;
}

.plan-day {
  padding: 6px 0;
  font-size: 14px;
}

.plan-actions {
  display: flex;
  gap: 12px;
}

/* 聊天输入区域 */
.chat-input-area {
  padding: 16px 24px;
  border-top: 1px solid #E5E7EB;
  display: flex;
  gap: 12px;
  background: white;
  flex-shrink: 0;
}

.chat-input {
  flex: 1;
}

.chat-input :deep(.n-input__input-el) {
  border-radius: 12px;
}

.btn-send {
  background: linear-gradient(135deg, #FF6B35, #E55A2B);
  border: none;
  border-radius: 12px;
  font-weight: 600;
}

.btn-send:hover {
  background: linear-gradient(135deg, #FF8C61, #FF6B35);
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(255, 107, 53, 0.4);
}

/* 侧边栏 - 固定宽度 */
.sidebar {
  width: 320px;
  height: calc(100vh - 70px - 60px);
  display: flex;
  flex-direction: column;
  gap: 16px;
  flex-shrink: 0;
}

.card-section {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.06);
  transition: transform 0.3s, box-shadow 0.3s;
}

.card-section:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(0,0,0,0.1);
}

.quick-questions-section {
  flex-shrink: 0;
}

.coaches-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1A1A2E;
  margin: 0 0 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.quick-questions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.quick-question {
  padding: 8px 16px;
  background: #F0F2F5;
  border: 1px solid #E5E7EB;
  border-radius: 20px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s;
}

.quick-question:hover {
  border-color: #FF6B35;
  color: #FF6B35;
  background: white;
}

/* 教练列表 */
.coach-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
  overflow-y: auto;
  min-height: 0;
}

.coach-item {
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 12px;
  background: #F8FAFC;
  border-radius: 12px;
  transition: all 0.3s;
  cursor: pointer;
  flex-shrink: 0;
}

.coach-item:hover {
  background: #F0F2F5;
  transform: translateX(4px);
}

.coach-rank {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
}

.coach-rank.rank-1 {
  background: linear-gradient(135deg, #FFD700, #FFA500);
  color: white;
}

.coach-rank.rank-2 {
  background: linear-gradient(135deg, #C0C0C0, #A0A0A0);
  color: white;
}

.coach-rank.rank-3 {
  background: linear-gradient(135deg, #CD7F32, #B87333);
  color: white;
}

.coach-avatar {
  flex-shrink: 0;
  border: 2px solid white;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.coach-info {
  flex: 1;
  min-width: 0;
}

.coach-name {
  font-weight: 600;
  font-size: 14px;
  color: #1A1A2E;
  display: flex;
  align-items: center;
  gap: 6px;
}

.top-tag {
  font-size: 10px;
  padding: 0 6px;
  height: 18px;
}

.coach-specialty {
  font-size: 12px;
  color: #6B7280;
  margin-top: 2px;
}

.coach-stats {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
}

.rating-score {
  font-size: 12px;
  color: #FF6B35;
  font-weight: 600;
}

.view-all-btn {
  margin-top: 12px;
  justify-content: center;
  flex-shrink: 0;
}

/* 移动端快捷问题 */
.mobile-quick-questions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px 16px;
  background: white;
  border-top: 1px solid #E5E7EB;
  z-index: 100;
}

.quick-questions-scroll {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.quick-questions-scroll::-webkit-scrollbar {
  display: none;
}

.quick-questions-scroll .quick-question {
  flex-shrink: 0;
}

/* 响应式适配 - 折叠侧边栏 */
@media (max-width: 1024px) {
  .assistant-page {
    left: 64px; /* 折叠后的sidebar宽度 */
  }
  
  .sidebar {
    width: 280px;
  }
  
  .chat-messages {
    padding: 20px;
  }
  
  .message-content {
    max-width: 75%;
  }
  
  .card-section {
    padding: 16px;
  }
}

/* 平板端 - 隐藏侧边栏 */
@media (max-width: 900px) {
  .sidebar {
    display: none;
  }
  
  .chat-wrapper {
    width: 100%;
  }
}

/* 移动端 */
@media (max-width: 768px) {
  .assistant-page {
    left: 0;
    top: 56px; /* 移动端header高度 */
    height: calc(100vh - 56px);
    padding: 12px;
    padding-bottom: 70px; /* 为底部快捷问题留空间 */
  }
  
  .chat-wrapper {
    height: calc(100vh - 56px - 70px - 60px);
  }
  
  .chat-messages {
    padding: 16px;
  }
  
  .message-content {
    max-width: 80%;
    padding: 12px 14px;
    font-size: 14px;
  }
  
  .message-avatar {
    width: 36px;
    height: 36px;
    font-size: 16px;
  }
  
  .chat-input-area {
    padding: 12px 16px;
  }
  
  .plan-message {
    padding: 12px;
  }
  
  .plan-actions {
    flex-direction: column;
    gap: 8px;
  }
  
  .plan-actions .n-button {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .assistant-page {
    padding: 8px;
    padding-bottom: 70px;
  }
  
  .chat-messages {
    padding: 12px;
  }
  
  .message {
    gap: 8px;
    margin-bottom: 16px;
  }
  
  .message-content {
    max-width: 85%;
    padding: 10px 12px;
    font-size: 13px;
  }
  
  .chat-input-area {
    padding: 10px 12px;
    gap: 8px;
  }
}
</style>
