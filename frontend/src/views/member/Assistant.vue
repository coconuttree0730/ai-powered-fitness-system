<template>
  <div class="assistant-page">
    <div class="chat-wrapper">
      <div class="chat-container">
        <div class="chat-messages" ref="messageContainer" @scroll="handleScroll">
          <!-- 加载更多提示 -->
          <div v-if="loadingMore" class="loading-more">
            <n-spin size="small" />
            <span>加载中...</span>
          </div>
          <div v-if="noMoreMessages" class="no-more-messages">
            <span>没有更多消息了</span>
          </div>
          <div v-for="(msg, index) in messages" :key="msg.id || index"
               :class="['message', msg.type]">
            <div class="message-avatar">{{ msg.type === 'ai' ? 'AI' : userAvatar }}</div>
            <div class="message-content">
              <div v-if="msg.isPlan" class="plan-message">
                <div class="plan-header">
                  <svg class="emoji-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg> {{ msg.title }}
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
          <div class="chat-input-wrapper">
            <div class="input-glow-layer"></div>
            <div class="input-content-area">
              <n-input
                v-model:value="inputMessage"
                type="textarea"
                class="chat-input"
                placeholder="输入您的问题，健小助随时为您服务..."
                size="large"
                :disabled="sending"
                :autosize="{ minRows: 2, maxRows: 8 }"
                @keydown="handleKeydown"
              />
            </div>
            <div class="chat-input-actions">
              <div class="input-actions-left">
                <div class="input-hint">
                  <span class="hint-icon">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M13 10V3L4 14h7v7l9-11h-7z"/>
                    </svg>
                  </span>
                  <span class="hint-text">Shift + Enter 换行</span>
                </div>
                <div class="btn-plan-wrapper">
                  <n-button 
                    size="small"
                    class="btn-plan"
                    @click="generatePlan"
                  >
                    <template #icon>
                      <n-icon :component="FitnessOutline" />
                    </template>
                    健身计划生成
                  </n-button>
                </div>
              </div>
              <n-button 
                v-if="!sending"
                type="primary" 
                size="small"
                class="btn-send"
                :disabled="!inputMessage.trim()"
                @click="sendMessage"
              >
                <template #icon>
                  <n-icon :component="SendOutline" />
                </template>
                发送
              </n-button>
              <n-button 
                v-else
                type="warning" 
                size="small"
                class="btn-pause"
                @click="stopMessage"
              >
                <template #icon>
                  <n-icon :component="PauseOutline" />
                </template>
                暂停
              </n-button>
            </div>
          </div>
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
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import {
  SendOutline,
  HelpCircleOutline,
  TrophyOutline,
  ChevronForwardOutline,
  FitnessOutline,
  PauseOutline
} from '@vicons/ionicons5'
import {
  createSession,
  sendMessage as sendChatMessage,
  sendMessageStream,
  getUserSessions,
  getSessionDetail,
  getSessionMessages
} from '@/api/chat'

const router = useRouter()
const message = useMessage()
const authStore = useAuthStore()
const messageContainer = ref(null)
const currentSessionId = ref(null)

// 调试：检查 marked 是否可用
console.log('marked import check:', typeof marked, marked ? 'available' : 'not available')

// 消息加载相关状态
const loadingMore = ref(false)
const noMoreMessages = ref(false)
const lastMessageId = ref(null)
const isFirstLoad = ref(true)
const scrollHeightBeforeLoad = ref(0)

// 响应式适配
const isMobile = ref(false)

function checkScreenSize() {
  isMobile.value = window.innerWidth < 768
}

onMounted(() => {
  checkScreenSize()
  window.addEventListener('resize', checkScreenSize)
  // 加载会话列表并初始化
  initChat()
})

onUnmounted(() => {
  window.removeEventListener('resize', checkScreenSize)
})

// 初始化聊天 - 加载用户的会话列表
async function initChat() {
  try {
    const res = await getUserSessions()
    if (res && res.length > 0) {
      // 使用最新的会话，确保ID是字符串
      const latestSession = res[0]
      currentSessionId.value = String(latestSession.id)
      // 加载该会话的历史消息
      await loadMessages()
    } else {
      // 没有历史会话，创建新会话
      await createNewSession()
    }
  } catch (error) {
    console.error('初始化聊天失败:', error)
    messages.value = [welcomeMessage]
  }
}

// 创建新会话
async function createNewSession() {
  try {
    const sessionRes = await createSession()
    if (sessionRes && sessionRes.id) {
      currentSessionId.value = String(sessionRes.id)
      messages.value = [welcomeMessage]
    } else {
      messages.value = [welcomeMessage]
    }
  } catch (error) {
    console.error('创建会话失败:', error)
    messages.value = [welcomeMessage]
  }
}

// 加载消息列表
async function loadMessages(isLoadMore = false) {
  if (!currentSessionId.value) {
    messages.value = [welcomeMessage]
    return
  }

  if (isLoadMore) {
    if (loadingMore.value || noMoreMessages.value) return
    loadingMore.value = true
    scrollHeightBeforeLoad.value = messageContainer.value?.scrollHeight || 0
  } else {
    isFirstLoad.value = true
  }

  try {
    const params = {
      limit: 10
    }
    if (isLoadMore && lastMessageId.value) {
      params.lastMessageId = lastMessageId.value
    }

    const res = await getSessionMessages(currentSessionId.value, params)

    if (res && res.length > 0) {
      // 转换消息格式，确保ID是字符串
      const formattedMessages = res.map(msg => ({
        id: String(msg.id),
        type: msg.role === 'user' ? 'user' : 'ai',
        content: msg.content,
        createdAt: msg.createdAt
      }))

      // 更新最后一条消息ID（用于分页），确保是字符串
      lastMessageId.value = String(res[res.length - 1].id)

      if (isLoadMore) {
        // 加载更多：在顶部添加消息
        messages.value = [...formattedMessages.reverse(), ...messages.value]
        // 恢复滚动位置
        nextTick(() => {
          const newScrollHeight = messageContainer.value?.scrollHeight || 0
          const heightDiff = newScrollHeight - scrollHeightBeforeLoad.value
          if (messageContainer.value) {
            messageContainer.value.scrollTop = heightDiff
          }
        })
      } else {
        // 首次加载：显示历史消息 + 欢迎消息
        messages.value = [...formattedMessages.reverse(), welcomeMessage]
        scrollToBottom()
      }

      // 如果返回的消息数小于limit，说明没有更多消息了
      if (res.length < 10) {
        noMoreMessages.value = true
      }
    } else {
      if (isLoadMore) {
        noMoreMessages.value = true
      } else {
        // 没有历史消息，只显示欢迎消息
        messages.value = [welcomeMessage]
      }
    }
  } catch (error) {
    console.error('加载消息失败:', error)
    if (!isLoadMore) {
      messages.value = [welcomeMessage]
    }
  } finally {
    loadingMore.value = false
    isFirstLoad.value = false
  }
}

// 处理滚动事件 - 下拉加载更多
function handleScroll() {
  if (!messageContainer.value) return

  const { scrollTop } = messageContainer.value

  // 当滚动到顶部附近时，加载更多消息
  if (scrollTop < 50 && !loadingMore.value && !noMoreMessages.value && currentSessionId.value) {
    loadMessages(true)
  }
}

const userAvatar = computed(() => authStore.userInfo?.username?.charAt(0) || '张')

const inputMessage = ref('')
const sending = ref(false)
const abortController = ref(null)

const messages = ref([])

// 欢迎消息
const welcomeMessage = {
  id: 'welcome',
  type: 'ai',
  content: '您好！我是健小助，您的智能健身助手<br><br>我可以帮您：<br>• 制定个性化训练计划<br>• 解答健身相关问题<br>• 介绍健身房器械使用方法<br>• 提供饮食和营养建议<br><br>有什么可以帮您的吗？'
}

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
  if (!content) return ''
  try {
    const rawHtml = marked.parse(content, {
      breaks: true,
      gfm: true,
      async: false
    })
    return DOMPurify.sanitize(rawHtml)
  } catch (error) {
    console.error('Markdown parsing error:', error)
    return content.replace(/\n/g, '<br>')
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (messageContainer.value) {
      messageContainer.value.scrollTop = messageContainer.value.scrollHeight
    }
  })
}

function handleKeydown(e) {
  if (e.key === 'Enter') {
    if (e.shiftKey) {
      // Shift + Enter: 允许默认换行行为，不阻止
      return
    } else {
      // 单独 Enter: 发送消息
      e.preventDefault()
      sendMessage()
    }
  }
}

async function sendMessage() {
  if (!inputMessage.value.trim() || sending.value) return

  // 如果没有会话ID，先创建会话
  if (!currentSessionId.value) {
    try {
      const sessionRes = await createSession()
      if (sessionRes && sessionRes.id) {
        currentSessionId.value = String(sessionRes.id)
      }
    } catch (error) {
      console.error('创建会话失败:', error)
      message.error('创建会话失败，请刷新页面重试')
      return
    }
  }

  const userMsg = inputMessage.value.trim()
  messages.value.push({ type: 'user', content: userMsg })
  inputMessage.value = ''
  sending.value = true
  scrollToBottom()

  const aiMessageIndex = messages.value.length
  messages.value.push({ type: 'ai', content: '' })

  abortController.value = new AbortController()
  let isCompleted = false

  const resetSending = () => {
    if (!isCompleted) {
      isCompleted = true
      sending.value = false
      abortController.value = null
      scrollToBottom()
      console.log('发送状态已重置')
    }
  }

  // 设置超时保护，30秒后强制重置状态
  const timeoutId = setTimeout(() => {
    if (!isCompleted) {
      console.warn('请求超时，强制重置状态')
      resetSending()
    }
  }, 30000)

  try {
    const response = await sendMessageStream({
      sessionId: currentSessionId.value,
      content: userMsg
    }, abortController.value.signal)

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    if (!response.body) {
      throw new Error('响应体为空')
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''
    let fullContent = ''
    let chunkCount = 0

    try {
      while (true) {
        const { done, value } = await reader.read()
        if (done) {
          console.log('SSE 流正常结束')
          // 流结束时立即重置发送状态，避免暂停按钮延迟消失
          resetSending()
          break
        }

        chunkCount++
        buffer += decoder.decode(value, { stream: true })

        const lines = buffer.split('\n')
        buffer = lines.pop() || ''

        for (const line of lines) {
          const trimmedLine = line.trim()
          if (trimmedLine.startsWith('data:')) {
            const data = trimmedLine.slice(5).trim()
            if (data) {
              fullContent += data
            }
          }
        }

        messages.value[aiMessageIndex].content = fullContent
        scrollToBottom()
      }

      if (buffer.trim().startsWith('data:')) {
        fullContent += buffer.trim().slice(5).trim()
        messages.value[aiMessageIndex].content = fullContent
        scrollToBottom()
      }

      console.log(`共接收 ${chunkCount} 个数据块`)
    } finally {
      reader.releaseLock()
    }

  } catch (error) {
    if (error.name === 'AbortError') {
      console.log('用户暂停了回答')
    } else {
      console.error('发送消息失败:', error)
      message.error('发送消息失败，请稍后重试')
      messages.value[aiMessageIndex].content = '抱歉，我遇到了一些问题，请稍后再试。'
    }
  } finally {
    clearTimeout(timeoutId)
    resetSending()
  }
}

function stopMessage() {
  if (abortController.value) {
    abortController.value.abort()
  }
}

function generatePlan() {
  const planPrompt = '请为我制定一个个性化的健身训练计划，包括每周训练安排、训练项目和注意事项。'
  inputMessage.value = planPrompt
  sendMessage()
}

function askQuestion(question) {
  inputMessage.value = question
  sendMessage()
}

function usePlan() {
  message.success('训练计划已保存到您的健身计划！')
}

async function regeneratePlan() {
  message.info('正在重新生成计划...')
  const planPrompt = '请为我制定一个不同的个性化健身训练计划。'
  inputMessage.value = planPrompt
  await sendMessage()
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

/* 加载更多提示 */
.loading-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  color: #6B7280;
  font-size: 13px;
}

.no-more-messages {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px;
  color: #9CA3AF;
  font-size: 12px;
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

/* Markdown 样式 */
.message-content :deep(h1),
.message-content :deep(h2),
.message-content :deep(h3),
.message-content :deep(h4) {
  margin: 16px 0 12px;
  font-weight: 600;
  line-height: 1.4;
}

.message-content :deep(h1) {
  font-size: 18px;
  border-bottom: 1px solid #E5E7EB;
  padding-bottom: 8px;
}

.message-content :deep(h2) {
  font-size: 16px;
}

.message-content :deep(h3) {
  font-size: 15px;
}

.message-content :deep(p) {
  margin: 8px 0;
  line-height: 1.7;
}

.message-content :deep(ul),
.message-content :deep(ol) {
  margin: 8px 0;
  padding-left: 20px;
}

.message-content :deep(li) {
  margin: 4px 0;
  line-height: 1.6;
}

.message-content :deep(code) {
  background: rgba(0, 0, 0, 0.05);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  color: #E55A2B;
}

.message-content :deep(pre) {
  background: #1A1A2E;
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 12px 0;
}

.message-content :deep(pre code) {
  background: transparent;
  color: #E5E7EB;
  padding: 0;
  font-size: 13px;
  line-height: 1.6;
}

.message-content :deep(blockquote) {
  border-left: 4px solid #FF6B35;
  margin: 12px 0;
  padding: 8px 16px;
  background: rgba(255, 107, 53, 0.05);
  border-radius: 0 8px 8px 0;
}

.message-content :deep(blockquote p) {
  margin: 0;
}

.message-content :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 12px 0;
  font-size: 13px;
}

.message-content :deep(th),
.message-content :deep(td) {
  padding: 8px 12px;
  border: 1px solid #E5E7EB;
  text-align: left;
}

.message-content :deep(th) {
  background: #F8FAFC;
  font-weight: 600;
}

.message-content :deep(tr:nth-child(even)) {
  background: #FAFBFC;
}

.message-content :deep(a) {
  color: #FF6B35;
  text-decoration: none;
}

.message-content :deep(a:hover) {
  text-decoration: underline;
}

.message-content :deep(strong) {
  font-weight: 600;
  color: #1A1A2E;
}

.message-content :deep(em) {
  font-style: italic;
}

.message-content :deep(hr) {
  border: none;
  border-top: 1px solid #E5E7EB;
  margin: 16px 0;
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

/* 聊天输入区域 - 现代精致设计 */
.chat-input-area {
  padding: 20px 24px;
  background: linear-gradient(180deg, rgba(250, 251, 252, 0) 0%, #FAFBFC 100%);
  flex-shrink: 0;
  position: relative;
}

.chat-input-area::before {
  content: '';
  position: absolute;
  top: 0;
  left: 24px;
  right: 24px;
  height: 1px;
  background: linear-gradient(90deg, transparent 0%, rgba(229, 231, 235, 0.6) 20%, rgba(229, 231, 235, 0.6) 80%, transparent 100%);
}

.chat-input-wrapper {
  position: relative;
  border-radius: 20px;
  padding: 0;
  background: white;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow:
    0 2px 8px rgba(0, 0, 0, 0.04),
    0 4px 16px rgba(0, 0, 0, 0.02),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(229, 231, 235, 0.8);
  overflow: hidden;
}

/* 发光层效果 */
.input-glow-layer {
  position: absolute;
  inset: -2px;
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.15) 0%, rgba(255, 140, 97, 0.1) 50%, rgba(229, 90, 43, 0.15) 100%);
  border-radius: 22px;
  opacity: 0;
  filter: blur(12px);
  transition: opacity 0.35s ease;
  z-index: -1;
}

.chat-input-wrapper:focus-within {
  border-color: rgba(255, 107, 53, 0.4);
  box-shadow:
    0 4px 20px rgba(255, 107, 53, 0.1),
    0 8px 32px rgba(255, 107, 53, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
  transform: translateY(-1px);
}

.chat-input-wrapper:focus-within .input-glow-layer {
  opacity: 1;
}

/* 输入内容区域 - 与底部操作区平行 */
.input-content-area {
  padding: 20px 24px 16px;
  background: white;
}

.chat-input {
  width: 100%;
}

.chat-input :deep(.n-input__textarea-el) {
  background: transparent;
  border: none;
  resize: none;
  font-size: 16px;
  line-height: 1.8;
  padding: 8px 12px;
  min-height: 72px;
  color: #1A1A2E;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.chat-input :deep(.n-input__textarea-el:hover) {
  background: rgba(250, 251, 252, 0.8);
}

.chat-input :deep(.n-input__textarea-el:focus) {
  background: rgba(250, 251, 252, 1);
}

.chat-input :deep(.n-input__textarea-el::placeholder) {
  color: #9CA3AF;
  font-size: 15px;
  transition: color 0.3s ease;
}

.chat-input-wrapper:focus-within :deep(.n-input__textarea-el::placeholder) {
  color: #D1D5DB;
}

.chat-input :deep(.n-input__border) {
  display: none;
}

.chat-input :deep(.n-input__state-border) {
  display: none;
}

/* 输入框底部操作区 */
.chat-input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: #FAFBFC;
  border-top: 1px solid #F0F2F5;
}

.input-actions-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

/* 输入提示 */
.input-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #9CA3AF;
  transition: all 0.3s ease;
}

.hint-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #9CA3AF;
  opacity: 0.8;
}

/* 健身计划生成按钮 - 橙色边框透明背景 */
.btn-plan-wrapper {
  position: relative;
  border-radius: 10px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.btn-plan-wrapper:hover {
  transform: translateY(-1px);
}

.btn-plan {
  color: #FF6B35 !important;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 8px;
  padding: 5px 11px;
  background: transparent;
  border: 1px solid #FF6B35 !important;
}

.btn-plan:hover {
  color: #E55A2B !important;
  border-color: #E55A2B !important;
  background: rgba(255, 107, 53, 0.05);
}

.btn-plan :deep(.n-icon) {
  color: #FF6B35 !important;
}

.btn-plan :deep(.n-button__content) {
  color: #FF6B35 !important;
}

.btn-plan :deep(.n-button__border) {
  border: 1px solid #FF6B35 !important;
}

.btn-plan :deep(.n-button__state-border) {
  border: none !important;
}

/* 发送按钮 - 现代药丸形状 */
.btn-send {
  background: linear-gradient(135deg, #FF6B35 0%, #E55A2B 50%, #FF6B35 100%);
  background-size: 200% 200%;
  border: none !important;
  border-radius: 12px;
  padding: 8px 20px;
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0.5px;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 14px rgba(255, 107, 53, 0.25);
  animation: gradientShift 3s ease infinite;
}

.btn-send :deep(.n-button__border) {
  display: none !important;
  border: none !important;
}

.btn-send :deep(.n-button__state-border) {
  display: none !important;
  border: none !important;
}

@keyframes gradientShift {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

.btn-send:hover:not(:disabled) {
  transform: translateY(-2px) scale(1.02);
  box-shadow: 0 6px 20px rgba(255, 107, 53, 0.35), 0 2px 8px rgba(255, 107, 53, 0.2);
}

.btn-send:active:not(:disabled) {
  transform: translateY(0) scale(0.98);
  box-shadow: 0 2px 10px rgba(255, 107, 53, 0.25);
}

.btn-send:disabled {
  background: linear-gradient(135deg, #E5E7EB 0%, #D1D5DB 100%);
  box-shadow: none;
  opacity: 0.7;
  cursor: not-allowed;
}

/* 暂停按钮 */
.btn-pause {
  background: linear-gradient(135deg, #F59E0B 0%, #D97706 50%, #F59E0B 100%);
  background-size: 200% 200%;
  border: none !important;
  border-radius: 12px;
  padding: 8px 20px;
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0.5px;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 14px rgba(245, 158, 11, 0.25);
  animation: gradientShift 3s ease infinite;
}

.btn-pause :deep(.n-button__border) {
  display: none !important;
  border: none !important;
}

.btn-pause :deep(.n-button__state-border) {
  display: none !important;
  border: none !important;
}

.btn-pause:hover {
  transform: translateY(-2px) scale(1.02);
  box-shadow: 0 6px 20px rgba(245, 158, 11, 0.35), 0 2px 8px rgba(245, 158, 11, 0.2);
}

.btn-pause:active {
  transform: translateY(0) scale(0.98);
  box-shadow: 0 2px 10px rgba(245, 158, 11, 0.25);
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

  .chat-input-area::before {
    left: 16px;
    right: 16px;
  }

  .chat-input-wrapper {
    border-radius: 16px;
  }

  .input-content-area {
    padding: 16px 18px 12px;
  }

  .chat-input :deep(.n-input__textarea-el) {
    font-size: 15px;
    min-height: 60px;
    line-height: 1.7;
    padding: 6px 10px;
  }

  .chat-input-actions {
    padding: 10px 14px;
  }

  .input-actions-left {
    gap: 10px;
  }

  .input-hint {
    display: none;
  }

  .btn-plan-wrapper {
    border-radius: 8px;
  }

  .btn-plan {
    font-size: 12px;
    padding: 4px 9px;
    border-radius: 6px;
  }

  .btn-send {
    padding: 6px 14px;
    font-size: 13px;
    border-radius: 10px;
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
