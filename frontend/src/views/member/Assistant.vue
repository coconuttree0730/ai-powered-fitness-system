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
          <div v-for="(msg, index) in messages" :key="msg.id || msg.type + '_' + index"
               :class="['message', msg.type]">
            <!-- 跳过渲染空的 AI 消息（当正在显示"正在输入"动画时） -->
            <template v-if="!(msg.type === 'ai' && !msg.content && !msg.statusText && isAiTyping)">
              <div class="message-avatar">
                <img v-if="msg.type === 'ai'" src="/ai.jpg" alt="AI" class="avatar-img" />
                <img v-else-if="userAvatarUrl" :src="userAvatarUrl" alt="用户" class="avatar-img" />
                <span v-else>{{ userAvatarText }}</span>
              </div>
              <div class="message-bubble-wrapper">
                <!-- 状态文字显示在气泡上方 -->
                <div v-if="msg.type === 'ai' && msg.statusText" class="stream-status">{{ msg.statusText }}</div>
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
                  <div v-else-if="msg.type === 'ai'" class="ai-stream-content">
                    <div v-if="msg.content || msg.isFinal" class="markdown-content markstream-wrapper">
                      <MarkdownRender
                        :custom-id="'chat-' + (msg.id || index)"
                        :nodes="msg.nodes"
                        :final="msg.isFinal !== false"
                        :max-live-nodes="0"
                        :batch-rendering="chatBatchRendering"
                      />
                    </div>
                    <!-- 打字动画：当正在输入且没有内容时显示在气泡内 -->
                    <div v-if="isAiTyping && !msg.content && !msg.isFinal" class="typing-indicator">
                      <span class="dot"></span>
                      <span class="dot"></span>
                      <span class="dot"></span>
                    </div>
                  </div>
                  <div v-else class="markdown-content" v-html="msg.content.replace(/\n/g, '<br>')"></div>
                </div>
              </div>
            </template>
          </div>

          <!-- 健身计划生成中动画 -->
          <div v-if="planGenerationStore.isGenerating" class="message ai plan-generating-message">
            <div class="message-avatar">
              <img src="/ai.jpg" alt="AI" class="avatar-img" />
            </div>
            <div class="message-content plan-generating-content">
              <div class="plan-generating-animation">
                <div class="generating-header">
                  <div class="generating-icon-wrapper">
                    <n-icon :component="FitnessOutline" size="24" />
                    <div class="icon-pulse-ring"></div>
                  </div>
                  <div class="generating-text">
                    <div class="generating-title">正在生成您的专属健身计划</div>
                    <div class="generating-subtitle">{{ planGenerationStore.planGenStepText }}</div>
                  </div>
                </div>
                <div class="generating-progress-bar">
                  <div class="progress-track">
                    <div class="progress-fill" :style="{ width: planGenerationStore.planGenProgress + '%' }"></div>
                    <div class="progress-glow" :style="{ left: planGenerationStore.planGenProgress + '%' }"></div>
                  </div>
                  <div class="progress-percentage">{{ planGenerationStore.planGenProgress }}%</div>
                </div>
                <div class="generating-steps">
                  <div :class="['step-item', { active: planGenerationStore.planGenStep >= 1, done: planGenerationStore.planGenStep > 1 }]">
                    <div class="step-dot"></div>
                    <span>分析档案</span>
                  </div>
                  <div :class="['step-item', { active: planGenerationStore.planGenStep >= 2, done: planGenerationStore.planGenStep > 2 }]">
                    <div class="step-dot"></div>
                    <span>匹配课程</span>
                  </div>
                  <div :class="['step-item', { active: planGenerationStore.planGenStep >= 3, done: planGenerationStore.planGenStep > 3 }]">
                    <div class="step-dot"></div>
                    <span>制定方案</span>
                  </div>
                  <div :class="['step-item', { active: planGenerationStore.planGenStep >= 4, done: planGenerationStore.planGenStep > 4 }]">
                    <div class="step-dot"></div>
                    <span>生成完成</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 健身计划预览组件 (Tab版详细展示) - 作为聊天消息展示 -->
          <transition name="plan-fade">
            <div v-if="showPlanPreview && fitnessPlanData" class="message ai plan-message-wrapper">
              <div class="message-avatar">
              <img src="/ai.jpg" alt="AI" class="avatar-img" />
            </div>
            <div class="message-content plan-message-content">
                <FitnessPlanPreview
                  :plan-data="fitnessPlanData"
                  :is-embedded="true"
                  @regenerate="handleRegenerateFromPreview"
                  @save="handleSaveFromPreview"
                  @course-click="handleCourseClick"
                />
              </div>
            </div>
          </transition>
        </div>

        <div class="chat-input-area">
          <div class="chat-input-wrapper">
            <div class="input-glow-layer"></div>
            <div class="input-content-area">
              <n-input
                v-model:value="inputMessage"
                type="textarea"
                class="chat-input"
                :placeholder="generatingPlan ? '正在生成健身计划，请稍候...' : '输入您的问题，健小助随时为您服务...'"
                size="large"
                :disabled="isBusy"
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
                    :loading="generatingPlan"
                    :disabled="isBusy"
                    @click="generatePlanFromProfile"
                  >
                    <template #icon>
                      <n-icon :component="FitnessOutline" />
                    </template>
                    健身计划生成
                  </n-button>
                </div>
                <div class="btn-plan-wrapper">
                  <n-button
                    size="small"
                    class="btn-plan btn-nutrition"
                    :disabled="isBusy"
                    @click="generateNutrition"
                  >
                    <template #icon>
                      <n-icon :component="NutritionOutline" />
                    </template>
                    营养指导
                  </n-button>
                </div>
              </div>
              <n-button
                v-if="!sending"
                type="primary"
                size="small"
                class="btn-send"
                :disabled="!inputMessage.trim() || isBusy"
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

      <!-- 推荐教练 -->
      <div class="card-section coaches-section">
        <h3 class="section-title">
          <n-icon :component="PeopleOutline" size="18" />
          推荐教练
        </h3>
        <div class="coaches-list">
          <div v-if="loadingCoaches" class="coaches-loading">
            <n-spin size="small" />
            <span>加载中...</span>
          </div>
          <div v-else-if="coaches.length === 0" class="coaches-empty">
            <n-empty description="暂无教练信息" size="small" />
          </div>
          <div v-else class="coach-items">
            <div
              v-for="coach in coaches"
              :key="coach.id"
              class="coach-item"
              @click="openCoachDetail(coach)"
            >
              <div class="coach-item-avatar">
                <img :src="coach.image || 'https://via.placeholder.com/100'" :alt="coach.name" />
                <span class="coach-status online"></span>
              </div>
              <div class="coach-item-info">
                <div class="coach-item-header">
                  <span class="coach-item-name">{{ coach.name }}</span>
                  <span class="coach-item-title">{{ coach.title }}</span>
                </div>
                <div class="coach-item-rating">
                  <div class="rating-stars">
                    <svg v-for="n in 5" :key="n" viewBox="0 0 24 24" :class="['star', { filled: n <= Math.floor(coach.ratingScore || 4.5) }]">
                      <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                    </svg>
                  </div>
                  <span class="rating-score">{{ coach.ratingScore || '4.5' }}</span>
                </div>
                <div class="coach-item-tags">
                  <span v-for="(tag, idx) in (coach.tags || []).slice(0, 3)" :key="idx" class="coach-tag">{{ tag }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
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

    <!-- 计划详情弹窗 -->
    <n-modal
      v-model:show="showPlanDetailModal"
      :title="selectedPlan?.planName || '计划详情'"
      preset="card"
      class="plan-detail-modal"
      :style="{ width: '600px', maxHeight: '80vh' }"
    >
      <div v-if="selectedPlan" class="plan-detail-content">
        <div class="plan-detail-header">
          <div class="plan-detail-meta">
            <n-tag type="primary">{{ selectedPlan.goal }}</n-tag>
            <n-tag type="info">{{ selectedPlan.bodyPart }}</n-tag>
            <n-tag type="warning">{{ selectedPlan.experience }}</n-tag>
            <span class="plan-detail-duration">{{ selectedPlan.duration }}天周期</span>
          </div>
        </div>
        <div class="plan-detail-summary">{{ selectedPlan.summary }}</div>
        <div v-if="selectedPlan.weeklyPlan && selectedPlan.weeklyPlan.length > 0" class="plan-detail-weekly">
          <div class="weekly-title">每周训练安排</div>
          <div class="weekly-list">
            <div v-for="(day, idx) in selectedPlan.weeklyPlan" :key="idx" class="weekly-item">
              <div class="weekly-day">{{ day.day }}</div>
              <div class="weekly-content">{{ day.content }}</div>
            </div>
          </div>
        </div>
        <div v-if="selectedPlan.weeklyAdvice" class="plan-detail-advice">
          <div class="advice-title">每周建议</div>
          <div class="advice-content">{{ selectedPlan.weeklyAdvice.nutrition }}</div>
          <div class="advice-content">{{ selectedPlan.weeklyAdvice.rest }}</div>
        </div>
      </div>
    </n-modal>

    <!-- 教练详情弹窗 -->
    <n-modal
      v-model:show="showCoachDetailModal"
      :title="selectedCoach?.name || '教练详情'"
      preset="card"
      class="coach-detail-modal"
      :style="{ width: '480px', maxHeight: '80vh' }"
    >
      <div v-if="selectedCoach" class="coach-detail-content">
        <div class="coach-detail-header">
          <div class="coach-detail-avatar">
            <img :src="selectedCoach.image || 'https://via.placeholder.com/100'" :alt="selectedCoach.name" />
          </div>
          <div class="coach-detail-info">
            <div class="coach-detail-name">{{ selectedCoach.name }}</div>
            <div class="coach-detail-title">{{ selectedCoach.title }}</div>
            <div class="coach-detail-rating">
              <div class="rating-stars">
                <svg v-for="n in 5" :key="n" viewBox="0 0 24 24" :class="['star', { filled: n <= Math.floor(selectedCoach.ratingScore || 4.5) }]">
                  <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
                </svg>
              </div>
              <span class="rating-score">{{ selectedCoach.ratingScore || '4.5' }}</span>
            </div>
          </div>
        </div>
        <div class="coach-detail-stats">
          <div class="stat-item">
            <div class="stat-value">{{ selectedCoach.experience || '3年' }}</div>
            <div class="stat-label">从业经验</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ selectedCoach.students || '1000+' }}</div>
            <div class="stat-label">服务学员</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ selectedCoach.rating || '98%' }}</div>
            <div class="stat-label">好评率</div>
          </div>
        </div>
        <div class="coach-detail-section">
          <div class="section-label">专业标签</div>
          <div class="coach-detail-tags">
            <n-tag v-for="(tag, idx) in (selectedCoach.tags || [])" :key="idx" type="primary" size="small" round>{{ tag }}</n-tag>
          </div>
        </div>
        <div class="coach-detail-section">
          <div class="section-label">个人简介</div>
          <div class="coach-detail-bio">{{ selectedCoach.bio || '暂无简介' }}</div>
        </div>
        <div class="coach-detail-actions">
          <n-button class="btn-chat" @click="startChatWithCoach(selectedCoach)">
            <template #icon>
              <n-icon :component="ChatbubbleOutline" />
            </template>
            在线咨询
          </n-button>
          <n-button type="primary" class="btn-buy" @click="buyCourseFromCoach(selectedCoach)">
            <template #icon>
              <n-icon :component="AddCircleOutline" />
            </template>
            购买课程
          </n-button>
        </div>
      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, onUnmounted, computed, watch } from 'vue'
import { useMessage, NIcon } from 'naive-ui'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { usePlanGenerationStore } from '@/stores/planGeneration'
import MarkdownRender, { getMarkdown, parseMarkdownToStructure } from 'markstream-vue'
import 'markstream-vue/index.css'
import {
  SendOutline,
  HelpCircleOutline,
  FitnessOutline,
  PauseOutline,
  NutritionOutline,
  CloseOutline,
  SaveOutline,
  RefreshOutline,
  PeopleOutline,
  ChatbubbleOutline,
  AddCircleOutline
} from '@vicons/ionicons5'
import {
  createSession,
  sendMessageStream,
  getUserSessions,
  getSessionMessages,
  generateFitnessPlan,
  generateFitnessPlanFromProfile,
  startAsyncPlanGeneration
} from '@/api/chat'
import { saveFitnessPlan as savePlanToServer, getMyPlans, getProfile } from '@/api/plan'
import { getCurrentUser } from '@/api/user'
import { getHomePageCoaches, getCoachDetail } from '@/api/coachDetail'
import FitnessPlanPreview from '@/components/FitnessPlanPreview.vue'

const router = useRouter()
const message = useMessage()
const authStore = useAuthStore()
const planGenerationStore = usePlanGenerationStore()
const messageContainer = ref(null)
const currentSessionId = ref(null)

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
  // 加载我的健身计划
  loadMyPlans()
  // 恢复未保存的计划卡片
  restorePlanCard()
  // 加载推荐教练列表
  loadCoaches()
  // 恢复进行中的计划生成任务
  restorePlanGeneration()
})

onUnmounted(() => {
  window.removeEventListener('resize', checkScreenSize)
})

// 恢复进行中的计划生成任务
function restorePlanGeneration() {
  // 如果当前已经在显示预览，不再恢复（避免重复显示）
  if (showPlanPreview.value && fitnessPlanData.value) {
    return
  }
  
  const restored = planGenerationStore.restoreTask()
  if (restored) {
    console.log('恢复计划生成任务:', planGenerationStore.activeTaskId)
  }
  // 如果 store 中有已完成的结果且当前没有显示预览，才显示预览
  if (planGenerationStore.isCompleted && planGenerationStore.taskResult && !showPlanPreview.value) {
    fitnessPlanData.value = planGenerationStore.taskResult
    showPlanPreview.value = true
  }
}

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
        nodes: msg.role !== 'user' ? parseToNodes(msg.content, true) : undefined,
        isFinal: true,
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

const userAvatarUrl = computed(() => authStore.userInfo?.avatar || '')
const userAvatarText = computed(() => authStore.userInfo?.username?.charAt(0) || '张')

const inputMessage = ref('')
const sending = ref(false)
const isAiTyping = ref(false)
const abortController = ref(null)

// 全局忙碌状态：用于互斥锁
const isBusy = computed(() => sending.value || generatingPlan.value)

const md = getMarkdown()
const chatBatchRendering = {
  renderBatchSize: 16,
  renderBatchDelay: 8
}

function parseToNodes(content, isFinal = false) {
  if (!content) return []
  return parseMarkdownToStructure(content, md, { final: isFinal })
}

const messages = ref([])

// 欢迎消息
const welcomeMessageRaw = '您好！我是**健小助**，您的智能健身助手\n\n我可以帮您：\n• 制定个性化训练计划\n• 解答健身相关问题\n• 介绍健身房器械使用方法\n• 提供饮食和营养建议\n\n有什么可以帮您的吗？'
const welcomeMessage = {
  id: 'welcome',
  type: 'ai',
  content: welcomeMessageRaw,
  nodes: parseToNodes(welcomeMessageRaw, true),
  isFinal: true
}

const quickQuestions = [
  '如何增肌？',
  '减脂饮食建议',
  '器械使用指南',
  '训练频率建议',
  '蛋白粉怎么选',
  '热身动作推荐'
]



function scrollToBottom() {
  nextTick(() => {
    if (messageContainer.value) {
      messageContainer.value.scrollTop = messageContainer.value.scrollHeight
    }
  })
}

function updateAiMessage(index, patch) {
  const updated = {
    ...messages.value[index],
    ...patch
  }
  messages.value.splice(index, 1, updated)
}

function flushStreamRender() {
  return new Promise((resolve) => {
    nextTick(() => {
      requestAnimationFrame(() => {
        requestAnimationFrame(() => resolve())
      })
    })
  })
}

function parseSseEventBlock(block) {
  let event = 'message'
  const dataLines = []

  for (const rawLine of block.split('\n')) {
    if (!rawLine || rawLine.startsWith(':')) continue
    if (rawLine.startsWith('event:')) {
      event = rawLine.slice(6).trim() || 'message'
      continue
    }
    if (rawLine.startsWith('data:')) {
      let data = rawLine.slice(5)
      if (data.startsWith(' ')) {
        data = data.slice(1)
      }
      dataLines.push(data)
    }
  }

  return {
    event,
    data: dataLines.join('\n')
  }
}

function extractJsonEvents(buffer) {
  const events = []
  let start = -1
  let depth = 0
  let inString = false
  let escaped = false
  let lastConsumedIndex = 0

  for (let i = 0; i < buffer.length; i++) {
    const char = buffer[i]

    if (start === -1) {
      if (char === '{') {
        start = i
        depth = 1
        inString = false
        escaped = false
      }
      continue
    }

    if (inString) {
      if (escaped) {
        escaped = false
        continue
      }
      if (char === '\\') {
        escaped = true
        continue
      }
      if (char === '"') {
        inString = false
      }
      continue
    }

    if (char === '"') {
      inString = true
      continue
    }

    if (char === '{') {
      depth++
      continue
    }

    if (char === '}') {
      depth--
      if (depth === 0) {
        events.push({
          event: 'message',
          data: buffer.slice(start, i + 1)
        })
        lastConsumedIndex = i + 1
        start = -1
      }
    }
  }

  return {
    events,
    remaining: buffer.slice(lastConsumedIndex)
  }
}

function extractStreamEvents(buffer) {
  let normalized = buffer.replace(/\r\n/g, '\n')
  normalized = normalized.replace(/\r/g, '\n')
  const events = []

  let separatorIndex = normalized.indexOf('\n\n')
  while (separatorIndex !== -1) {
    const block = normalized.slice(0, separatorIndex)
    normalized = normalized.slice(separatorIndex + 2)
    if (block.trim()) {
      events.push(parseSseEventBlock(block))
    }
    separatorIndex = normalized.indexOf('\n\n')
  }

  const jsonExtraction = extractJsonEvents(normalized)
  events.push(...jsonExtraction.events)

  return {
    events,
    remaining: jsonExtraction.remaining
  }
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
  if (!inputMessage.value.trim() || sending.value || generatingPlan.value) {
    if (generatingPlan.value) {
      message.warning('正在生成健身计划，请稍候再发送消息')
    }
    return
  }

  if (!currentSessionId.value) {
    try {
      const sessionRes = await createSession()
      if (sessionRes && sessionRes.id) {
        currentSessionId.value = String(sessionRes.id)
      }
    } catch (error) {
      console.error('Create session failed:', error)
      message.error('创建会话失败，请刷新页面重试')
      return
    }
  }

  const userMsg = inputMessage.value.trim()
  inputMessage.value = ''
  sending.value = true
  isAiTyping.value = true

  messages.value.push({ type: 'user', content: userMsg })

  const aiMessageId = 'ai_' + Date.now() + '_' + Math.random().toString(36).slice(2, 8)
  messages.value.push({
    id: aiMessageId,
    type: 'ai',
    content: '',
    nodes: [],
    isFinal: false,
    statusText: '正在理解你的问题...'
  })

  const aiMessageIndex = messages.value.length - 1
  scrollToBottom()

  abortController.value = new AbortController()
  let isCompleted = false

  const resetSending = (force = false) => {
    if (!isCompleted || force) {
      isCompleted = true
      sending.value = false
      isAiTyping.value = false
      abortController.value = null
      scrollToBottom()
    }
  }

  const timeoutId = setTimeout(() => {
    if (!isCompleted) {
      console.warn('Stream request timed out')
      resetSending()
    }
  }, 120000)

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
    let eventCount = 0
    let receivedDoneEvent = false

    const handleStreamEvent = async ({ event, data }) => {
      if (!data) return

      let payload
      try {
        payload = JSON.parse(data)
      } catch (parseError) {
        console.warn('SSE payload parse failed:', parseError, data)
        return
      }

      const resolvedEvent = event === 'message' && payload.type ? payload.type : event
      eventCount++
      if (isAiTyping.value) {
        isAiTyping.value = false
      }

      if (resolvedEvent === 'status') {
        updateAiMessage(aiMessageIndex, {
          statusText: payload.message || '正在处理中...'
        })
        scrollToBottom()
        await flushStreamRender()
        return
      }

      if (resolvedEvent === 'delta') {
        fullContent += payload.text || ''
        updateAiMessage(aiMessageIndex, {
          type: 'ai',
          content: fullContent,
          nodes: parseToNodes(fullContent, false),
          isFinal: false,
          statusText: payload.message || '正在生成回答...'
        })
        scrollToBottom()
        await flushStreamRender()
        return
      }

      if (resolvedEvent === 'done') {
        receivedDoneEvent = true
        updateAiMessage(aiMessageIndex, {
          type: 'ai',
          content: fullContent,
          nodes: parseToNodes(fullContent, true),
          isFinal: true,
          statusText: ''
        })
        resetSending(true)
        scrollToBottom()
        await flushStreamRender()
        return
      }

      if (resolvedEvent === 'error') {
        const errorContent = payload.message || '抱歉，我遇到了一些问题，请稍后再试。'
        updateAiMessage(aiMessageIndex, {
          type: 'ai',
          content: fullContent || errorContent,
          nodes: parseToNodes(fullContent || errorContent, true),
          isFinal: true,
          statusText: ''
        })
        if (!fullContent) {
          message.error(errorContent)
        }
        await flushStreamRender()
      }
    }

    try {
      while (true) {
        const { done, value } = await reader.read()
        if (done) {
          break
        }

        buffer += decoder.decode(value, { stream: true })
        const extracted = extractStreamEvents(buffer)
        buffer = extracted.remaining
        for (const streamEvent of extracted.events) {
          await handleStreamEvent(streamEvent)
        }
      }

      if (buffer.trim()) {
        await handleStreamEvent(parseSseEventBlock(buffer))
      }

      if (!receivedDoneEvent && !isCompleted) {
        updateAiMessage(aiMessageIndex, {
          type: 'ai',
          content: fullContent,
          nodes: parseToNodes(fullContent, true),
          isFinal: true,
          statusText: ''
        })
        scrollToBottom()
      }

      console.log(`Received ${eventCount} SSE events, final content length: ${fullContent.length}`)
    } finally {
      reader.releaseLock()
    }
  } catch (error) {
    if (error.name === 'AbortError') {
      console.log('Stream aborted by user')
    } else {
      console.error('Send message failed:', error)
      message.error('发送消息失败，请稍后重试')
      if (!isCompleted) {
        const errorContent = '抱歉，我遇到了一些问题，请稍后再试。'
        updateAiMessage(aiMessageIndex, {
          type: 'ai',
          content: errorContent,
          nodes: parseToNodes(errorContent, true),
          isFinal: true,
          statusText: ''
        })
      }
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

// ==================== 健身计划相关功能 ====================

// 计划弹窗显示状态
const showPlanModal = ref(false)
const showPlanDetailModal = ref(false)
const generatingPlan = computed(() => planGenerationStore.isGenerating)
const savingPlan = ref(false)
const loadingPlans = ref(false)

// 当前生成的计划卡片
const currentPlanCard = ref(null)

// 健身计划预览组件相关状态
const showPlanPreview = ref(false)
const fitnessPlanData = ref(null)

// 监听 store 中的任务完成状态
watch(() => planGenerationStore.isCompleted, (completed) => {
  if (completed && planGenerationStore.taskResult) {
    // 任务完成，显示计划预览
    fitnessPlanData.value = planGenerationStore.taskResult
    showPlanPreview.value = true
    message.success('健身计划生成成功！')
  }
})

// 监听 store 中的错误状态
watch(() => planGenerationStore.taskError, (error) => {
  if (error) {
    message.error(error)
  }
})

// 我的健身计划列表
const myPlans = ref([])

// 推荐教练列表
const coaches = ref([])
const loadingCoaches = ref(false)

// 选中的计划（用于详情弹窗）
const selectedPlan = ref(null)

// 选中的教练
const selectedCoach = ref(null)
const showCoachDetailModal = ref(false)

// 计划表单
const planForm = ref({
  goal: '增肌',
  bodyPart: '全身',
  experience: '初级'
})

// 选项
const goalOptions = [
  { label: '增肌', value: '增肌' },
  { label: '减脂', value: '减脂' },
  { label: '塑形', value: '塑形' },
  { label: '力量提升', value: '力量提升' },
  { label: '体能改善', value: '体能改善' }
]

const bodyPartOptions = [
  { label: '全身', value: '全身' },
  { label: '胸部', value: '胸部' },
  { label: '背部', value: '背部' },
  { label: '腿部', value: '腿部' },
  { label: '肩部', value: '肩部' },
  { label: '手臂', value: '手臂' },
  { label: '核心', value: '核心' }
]

const experienceOptions = [
  { label: '初级', value: '初级' },
  { label: '中级', value: '中级' },
  { label: '高级', value: '高级' }
]

// 从个人档案生成健身计划（异步+轮询）
async function generatePlanFromProfile() {
  if (sending.value) {
    message.warning('正在回复消息，请稍候再生成计划')
    return
  }

  try {
    const { taskId } = await startAsyncPlanGeneration()
    if (!taskId) {
      throw new Error('未能获取任务ID')
    }

    // 使用 store 管理任务状态，确保跨页面保持
    planGenerationStore.startGeneration(taskId)
  } catch (error) {
    console.error('生成计划失败:', error)
    message.error(error.message || '生成计划失败，请稍后重试')
  }
}

// 从API响应构建预览数据（后端已返回完整结构化数据）
function buildFitnessPlanPreviewDataFromResponse(apiResponse) {
  // 如果API返回的是新格式（包含weeklyPlan），直接使用
  if (apiResponse.weeklyPlan && Array.isArray(apiResponse.weeklyPlan)) {
    return {
      subtitle: apiResponse.subtitle || `AI为您量身定制的7天${apiResponse.userInfo?.goal || '增肌塑形'}计划`,
      userInfo: apiResponse.userInfo || {
        height: '175cm',
        weight: '70kg',
        bmi: '22.9',
        goal: '增肌塑形',
        intensity: '中等'
      },
      weeklyPlan: apiResponse.weeklyPlan
    }
  }

  // 兼容旧格式：构建默认数据
  return {
    subtitle: `AI为您量身定制的7天增肌塑形计划`,
    userInfo: {
      height: apiResponse.height ? `${apiResponse.height}cm` : '175cm',
      weight: apiResponse.weight ? `${apiResponse.weight}kg` : '70kg',
      bmi: apiResponse.bmi || '22.9',
      goal: apiResponse.goal || '增肌塑形',
      intensity: apiResponse.experience || '中等'
    },
    weeklyPlan: generateDefaultWeeklyPlan({
      goal: apiResponse.goal || '增肌',
      bodyPart: apiResponse.bodyPart || '全身',
      experience: apiResponse.experience || '中级'
    }, apiResponse)
  }
}

// 生成健身计划（保留兼容）
async function generatePlan() {
  if (!planForm.value.goal || !planForm.value.bodyPart || !planForm.value.experience) {
    message.warning('请填写完整的计划信息')
    return
  }

  if (sending.value) {
    message.warning('正在回复消息，请稍候再生成计划')
    return
  }

  try {
    // 获取用户档案数据用于填充计划预览
    let userProfile = {}
    try {
      const userRes = await getCurrentUser()
      if (userRes && userRes.fitnessProfile) {
        userProfile = userRes.fitnessProfile
      }
    } catch (e) {
      console.log('获取用户档案失败，使用默认值')
    }

    const res = await generateFitnessPlan({
      goal: planForm.value.goal,
      bodyPart: planForm.value.bodyPart,
      experience: planForm.value.experience
    })

    if (res) {
      // 构建健身计划预览数据（兼容新旧格式）
      const previewData = buildFitnessPlanPreviewData(res, userProfile, planForm.value)

      // 设置预览数据
      fitnessPlanData.value = previewData
      showPlanPreview.value = true

      // 同时保留旧的卡片格式（向后兼容）
      currentPlanCard.value = res
      savePlanCardToSession()

      message.success('健身计划生成成功！')
      showPlanModal.value = false
    }
  } catch (error) {
    console.error('生成计划失败:', error)
    message.error(error.message || '生成计划失败，请稍后重试')
  }
}

// 构建健身计划预览数据
function buildFitnessPlanPreviewData(apiResponse, userProfile, form) {
  // 计算BMI
  const height = userProfile?.height || 175
  const weight = userProfile?.weight || 70
  const bmi = (weight / ((height / 100) ** 2)).toFixed(1)

  // 如果API返回的是新格式（包含weeklyPlan），直接使用
  if (apiResponse.weeklyPlan && Array.isArray(apiResponse.weeklyPlan)) {
    return {
      subtitle: `AI为您量身定制的7天${form.goal || '增肌塑形'}计划`,
      userInfo: {
        height: `${height}cm`,
        weight: `${weight}kg`,
        bmi: bmi,
        goal: form.goal || '增肌塑形',
        intensity: form.experience || '中等'
      },
      weeklyPlan: apiResponse.weeklyPlan
    }
  }

  // 否则构建默认格式（基于API返回的数据或使用模板数据）
  return {
    subtitle: `AI为您量身定制的7天${form.goal || '增肌塑形'}计划`,
    userInfo: {
      height: `${height}cm`,
      weight: `${weight}kg`,
      bmi: bmi,
      goal: form.goal || '增肌塑形',
      intensity: form.experience || '中等'
    },
    weeklyPlan: generateDefaultWeeklyPlan(form, apiResponse)
  }
}

// 生成默认的周计划数据（当API未返回结构化数据时使用）
function generateDefaultWeeklyPlan(form, apiResponse) {
  const dayNames = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  const focuses = getFocusByGoal(form.goal)

  return dayNames.map((dayName, index) => {
    const isRestDay = index === 6
    const focus = focuses[index] || '休息'

    if (isRestDay) {
      return {
        dayName,
        focus: '休息',
        tips: ['建议进行轻度拉伸和泡沫轴放松', '保证充足睡眠，促进肌肉恢复', '可以做一些散步或瑜伽等低强度活动']
      }
    }

    return {
      dayName,
      focus,
      course: {
        name: `${focus}专项训练课程`,
        description: `针对${focus}的高强度训练课程，包含多种训练动作，有效刺激${focus}肌肉生长，适合${form.experience || '中级'}健身者。`,
        coverImage: getDefaultCourseImage(focus),
        duration: 45 + Math.floor(Math.random() * 20),
        id: index + 1
      },
      equipment: getDefaultEquipment(focus).map((name, idx) => ({
        name,
        image: getDefaultEquipmentImage(name)
      })),
      exercises: getDefaultExercises(focus, form.experience),
      tips: [
        '训练前进行10分钟热身，避免肌肉拉伤',
        '保持正确的动作姿势，质量优于数量',
        '训练后补充蛋白质，促进肌肉恢复'
      ]
    }
  })
}

// 根据目标获取训练重点
function getFocusByGoal(goal) {
  const focusMap = {
    '增肌': ['胸部', '背部', '腿部', '肩部', '手臂', '核心', '休息'],
    '减脂': ['全身有氧', 'HIIT训练', '核心燃脂', '上肢塑形', '下肢塑形', '拉伸放松', '休息'],
    '塑形': ['臀腿', '背部雕塑', '肩臂线条', '核心强化', '全身协调', '柔韧拉伸', '休息'],
    '力量提升': ['深蹲硬拉', '卧推划船', '推举臂弯举', '核心稳定', '功能性', '恢复训练', '休息'],
    '体能改善': ['心肺耐力', '速度灵敏', '力量耐力', '爆发力', '综合体能', '主动恢复', '休息']
  }
  return focusMap[goal] || focusMap['增肌']
}

// 获取默认课程图片
function getDefaultCourseImage(focus) {
  const imageMap = {
    '胸部': 'https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=400&h=250&fit=crop',
    '背部': 'https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=400&h=250&fit=crop',
    '腿部': 'https://images.unsplash.com/photo-1434682882158-0d4a9cc89bb3?w=400&h=250&fit=crop',
    '肩部': 'https://images.unsplash.com/photo-1571902943202-507ec2618e8f?w=400&h=250&fit=crop',
    '手臂': 'https://images.unsplash.com/photo-1583454110551-21f2fe6907ba?w=400&h=250&fit=crop',
    '核心': 'https://images.unsplash.com/photo-1518611012118-696072aa579a?w=400&h=250&fit=crop',
    '臀腿': 'https://images.unsplash.com/photo-1434682882158-0d4a9cc89bb3?w=400&h=250&fit=crop',
    '全身有氧': 'https://images.unsplash.com/photo-1476480862126-209bfaa8edc8?w=400&h=250&fit=crop',
    'HIIT训练': 'https://images.unsplash.com/photo-1517838277539-f91ace7160b8?w=400&h=250&fit=crop'
  }
  return imageMap[focus] || 'https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=400&h=250&fit=crop'
}

// 获取默认器械列表
function getDefaultEquipment(focus) {
  const equipMap = {
    '胸部': ['杠铃', '哑铃', '卧推凳', '蝴蝶机'],
    '背部': ['杠铃', '高位下拉器', '哑铃', '坐姿划船机'],
    '腿部': ['深蹲架', '杠铃', '腿举机', '腿屈伸机'],
    '肩部': ['哑铃', '史密斯机', '侧平举器', '阿诺德推举器'],
    '手臂': ['哑铃', '杠铃曲杆', '绳索机', '牧师凳'],
    '核心': ['瑜伽垫', '药球', '健腹轮', '罗马椅']
  }
  return equipMap[focus] || ['哑铃', '杠铃', '瑜伽垫']
}

// 获取默认器械图片
function getDefaultEquipmentImage(name) {
  const imageMap = {
    '杠铃': 'https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=100&h=100&fit=crop',
    '哑铃': 'https://images.unsplash.com/photo-1597452485669-2c7bb5fef90d?w=100&h=100&fit=crop',
    '卧推凳': 'https://images.unsplash.com/photo-1571902949628-41aaea4ce747?w=100&h=100&fit=crop',
    '蝴蝶机': 'https://images.unsplash.com/photo-1638536532686-d610adfc8e5c?w=100&h=100&fit=crop',
    '深蹲架': 'https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=100&h=100&fit=crop',
    '瑜伽垫': 'https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=100&h=100&fit=crop'
  }
  return imageMap[name] || 'https://via.placeholder.com/100x100/e8e8e8/666?text=' + name.charAt(0)
}

// 获取默认训练动作
function getDefaultExercises(focus, experience) {
  const levelMultiplier = experience === '初级' ? 0.8 : experience === '高级' ? 1.2 : 1
  const exerciseMap = {
    '胸部': [
      { name: '平板杠铃卧推', sets: Math.round(4 * levelMultiplier), reps: 12, restSeconds: 60 },
      { name: '上斜哑铃卧推', sets: Math.round(4 * levelMultiplier), reps: 10, restSeconds: 60 },
      { name: '哑铃飞鸟', sets: Math.round(3 * levelMultiplier), reps: 15, restSeconds: 45 },
      { name: '蝴蝶机夹胸', sets: Math.round(3 * levelMultiplier), reps: 12, restSeconds: 45 }
    ],
    '背部': [
      { name: '引体向上', sets: Math.round(4 * levelMultiplier), reps: 8, restSeconds: 60 },
      { name: '杠铃划船', sets: Math.round(4 * levelMultiplier), reps: 12, restSeconds: 60 },
      { name: '单臂哑铃划船', sets: Math.round(3 * levelMultiplier), reps: 12, restSeconds: 60 },
      { name: '高位下拉', sets: Math.round(3 * levelMultiplier), reps: 15, restSeconds: 45 }
    ],
    '腿部': [
      { name: '杠铃深蹲', sets: Math.round(4 * levelMultiplier), reps: 12, restSeconds: 90 },
      { name: '腿举', sets: Math.round(4 * levelMultiplier), reps: 15, restSeconds: 60 },
      { name: '弓步蹲', sets: Math.round(3 * levelMultiplier), reps: 12, restSeconds: 60 },
      { name: '腿弯举', sets: Math.round(3 * levelMultiplier), reps: 15, restSeconds: 45 }
    ],
    '肩部': [
      { name: '坐姿哑铃推举', sets: Math.round(4 * levelMultiplier), reps: 12, restSeconds: 60 },
      { name: '哑铃侧平举', sets: Math.round(4 * levelMultiplier), reps: 15, restSeconds: 45 },
      { name: '哑铃前平举', sets: Math.round(3 * levelMultiplier), reps: 12, restSeconds: 45 },
      { name: '俯身飞鸟', sets: Math.round(3 * levelMultiplier), reps: 15, restSeconds: 45 }
    ],
    '手臂': [
      { name: '杠铃弯举', sets: Math.round(4 * levelMultiplier), reps: 12, restSeconds: 60 },
      { name: '锤式弯举', sets: Math.round(3 * levelMultiplier), reps: 12, restSeconds: 60 },
      { name: '绳索下压', sets: Math.round(4 * levelMultiplier), reps: 15, restSeconds: 45 },
      { name: '仰卧臂屈伸', sets: Math.round(3 * levelMultiplier), reps: 12, restSeconds: 45 }
    ],
    '核心': [
      { name: '平板支撑', sets: 3, reps: 60, restSeconds: 30 },
      { name: '卷腹', sets: Math.round(4 * levelMultiplier), reps: 20, restSeconds: 45 },
      { name: '俄罗斯转体', sets: Math.round(3 * levelMultiplier), reps: 30, restSeconds: 45 },
      { name: '登山跑', sets: Math.round(3 * levelMultiplier), reps: 20, restSeconds: 45 }
    ]
  }

  return exerciseMap[focus] || exerciseMap['胸部']
}

// 从预览组件触发的事件处理
function handleRegenerateFromPreview() {
  showPlanPreview.value = false
  fitnessPlanData.value = null
  // 清除 store 中的状态，避免切换页面后重复显示
  planGenerationStore.clearState()
  handleRegeneratePlan()
}

async function handleSaveFromPreview() {
  await handleSavePlan()
}

function handleCourseClick(course) {
  message.info(`查看课程: ${course.name}`)
}

// 保存计划到 sessionStorage
function savePlanCardToSession() {
  if (currentPlanCard.value) {
    sessionStorage.setItem('currentPlanCard', JSON.stringify(currentPlanCard.value))
  }
}

// 从 sessionStorage 恢复计划卡片
function restorePlanCard() {
  const saved = sessionStorage.getItem('currentPlanCard')
  if (saved) {
    try {
      currentPlanCard.value = JSON.parse(saved)
    } catch (e) {
      console.error('恢复计划卡片失败:', e)
    }
  }
}

// 关闭计划卡片
function dismissPlanCard() {
  currentPlanCard.value = null
  sessionStorage.removeItem('currentPlanCard')
}

// 保存计划到我的计划列表
async function handleSavePlan() {
  if (!fitnessPlanData.value) return

  savingPlan.value = true
  try {
    const planData = fitnessPlanData.value
    let height = null, weight = null, age = null, gender = null, experience = null, goal = null

    if (planData.userInfo) {
      const h = String(planData.userInfo.height || '').replace(/[^0-9.]/g, '')
      const w = String(planData.userInfo.weight || '').replace(/[^0-9.]/g, '')
      height = h ? parseFloat(h) : null
      weight = w ? parseFloat(w) : null
      experience = planData.userInfo.intensity || null
      goal = planData.userInfo.goal || null
      const a = String(planData.userInfo.age || '').replace(/[^0-9]/g, '')
      if (a) age = parseInt(a, 10)
      gender = planData.userInfo.gender || null
    }

    try {
      const fp = await getProfile()
      if (fp) {
        console.log('[handleSavePlan] getProfile返回:', JSON.stringify(fp, null, 2))
        if (!height) height = fp.height
        if (!weight) weight = fp.weight
        if (!age) age = fp.age
        if (!gender) gender = fp.gender
        if (!experience) experience = fp.experience
        if (!goal) goal = fp.fitnessGoal
      }
    } catch (e) {
      console.warn('[handleSavePlan] getProfile兜底失败:', e)
    }

    const payload = {
      planDataJson: planData,
      height,
      weight,
      age,
      gender,
      experience,
      fitnessGoal: goal
    }
    console.log('[handleSavePlan] 发送保存请求payload:', JSON.stringify(payload, null, 2))
    console.log('[handleSavePlan] planData.userInfo原始:', JSON.stringify(planData.userInfo, null, 2))

    await savePlanToServer(payload)
    message.success('计划已保存到我的健身计划！')
    showPlanPreview.value = false
    fitnessPlanData.value = null
    currentPlanCard.value = null
    sessionStorage.removeItem('currentPlanCard')
    // 清除 store 中的状态，避免切换页面后重复显示
    planGenerationStore.clearState()
    await loadMyPlans()
  } catch (error) {
    console.error('保存计划失败:', error)
    message.error(error.message || '保存计划失败，请稍后重试')
  } finally {
    savingPlan.value = false
  }
}

// 重新生成计划
async function handleRegeneratePlan() {
  if (!planForm.value.goal) {
    // 如果表单为空，使用当前卡片的参数
    if (currentPlanCard.value) {
      planForm.value.goal = currentPlanCard.value.goal
      planForm.value.bodyPart = currentPlanCard.value.bodyPart
      planForm.value.experience = currentPlanCard.value.experience
    }
  }
  await generatePlan()
}

// 加载我的健身计划列表
async function loadMyPlans() {
  loadingPlans.value = true
  try {
    const res = await getMyPlans()
    if (res) {
      myPlans.value = res
    }
  } catch (error) {
    console.error('加载健身计划失败:', error)
  } finally {
    loadingPlans.value = false
  }
}

// 查看计划详情
function viewPlanDetail(plan) {
  selectedPlan.value = plan
  showPlanDetailModal.value = true
}

// 格式化日期
function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    month: 'short',
    day: 'numeric'
  })
}

function generateNutrition() {
  if (generatingPlan.value) {
    message.warning('正在生成健身计划，请稍候再操作')
    return
  }
  const nutritionPrompt = '请为我提供个性化的营养指导建议，包括每日饮食搭配、营养素摄入建议和饮食注意事项。'
  inputMessage.value = nutritionPrompt
  sendMessage()
}

// ==================== 推荐教练相关功能 ====================

// 加载推荐教练列表
async function loadCoaches() {
  loadingCoaches.value = true
  try {
    const res = await getHomePageCoaches(8)
    if (res) {
      coaches.value = res
    }
  } catch (error) {
    console.error('加载教练列表失败:', error)
    // 使用模拟数据作为后备
    coaches.value = getMockCoaches()
  } finally {
    loadingCoaches.value = false
  }
}

// 模拟教练数据
function getMockCoaches() {
  return [
    {
      id: 1,
      name: '张教练',
      title: '高级私教',
      image: 'https://images.unsplash.com/photo-1567013127542-490d757e51fc?w=200&h=200&fit=crop&crop=face',
      experience: '8年',
      students: '2000+',
      rating: '99%',
      ratingScore: 4.9,
      tags: ['增肌塑形', '体能训练', '康复训练'],
      bio: '拥有8年健身教学经验，擅长增肌塑形和体能训练。曾帮助超过2000名学员实现健身目标，好评率高达99%。'
    },
    {
      id: 2,
      name: '李教练',
      title: '瑜伽导师',
      image: 'https://images.unsplash.com/photo-1594381898411-846e7d193883?w=200&h=200&fit=crop&crop=face',
      experience: '6年',
      students: '1500+',
      rating: '98%',
      ratingScore: 4.8,
      tags: ['瑜伽', '普拉提', '体态调整'],
      bio: '资深瑜伽导师，专注瑜伽教学6年。擅长哈他瑜伽、流瑜伽和普拉提，帮助学员改善体态、增强柔韧性。'
    },
    {
      id: 3,
      name: '王教练',
      title: '力量训练专家',
      image: 'https://images.unsplash.com/photo-1583454110551-21f2fa2afe61?w=200&h=200&fit=crop&crop=face',
      experience: '10年',
      students: '3000+',
      rating: '99%',
      ratingScore: 5.0,
      tags: ['力量训练', '举重', 'CrossFit'],
      bio: '力量训练专家，10年从业经验。精通举重、CrossFit等多种训练方式，曾获得多项力量比赛冠军。'
    },
    {
      id: 4,
      name: '刘教练',
      title: '减脂专家',
      image: 'https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=200&h=200&fit=crop&crop=face',
      experience: '5年',
      students: '1800+',
      rating: '97%',
      ratingScore: 4.7,
      tags: ['减脂', '有氧训练', '营养指导'],
      bio: '专注减脂领域5年，帮助超过1800名学员成功减重。结合科学训练和营养指导，让减脂更高效、更健康。'
    }
  ]
}

// 打开教练详情
function openCoachDetail(coach) {
  selectedCoach.value = coach
  showCoachDetailModal.value = true
}

// 与教练开始聊天
function startChatWithCoach(coach) {
  message.info(`开始与 ${coach.name} 对话`)
  // TODO: 实现跳转到聊天页面或打开聊天窗口
  // router.push(`/chat/coach/${coach.id}`)
}

// 购买教练课程
function buyCourseFromCoach(coach) {
  message.info(`购买 ${coach.name} 的课程`)
  // TODO: 实现跳转到购课页面
  // router.push(`/courses/buy?coachId=${coach.id}`)
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
  overflow: hidden;
}

.message-avatar .avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
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
  max-width: 80%;
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
  white-space: normal;
  word-break: break-word;
  min-width: 60px;
  width: fit-content;
  max-width: 100%;
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

/* 健身计划卡片固定显示区域 */
.plan-card-fixed-area {
  background: linear-gradient(135deg, #FFF5F2 0%, #FFF 100%);
  border-top: 1px solid #FFE8E0;
  padding: 16px 24px;
  flex-shrink: 0;
}

.plan-card-container {
  background: white;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(255, 107, 53, 0.1);
  border: 1px solid #FFE8E0;
}

.plan-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.plan-card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1A1A2E;
}

.plan-card-title .n-icon {
  color: #FF6B35;
}

.close-plan-btn {
  color: #9CA3AF;
}

.close-plan-btn:hover {
  color: #6B7280;
}

.plan-card-content {
  margin-bottom: 12px;
}

.plan-card-summary {
  font-size: 14px;
  color: #6B7280;
  margin-bottom: 12px;
  line-height: 1.5;
}

.plan-card-details {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 12px;
}

.plan-detail-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
}

.detail-label {
  color: #9CA3AF;
}

.detail-value {
  color: #1A1A2E;
  font-weight: 500;
}

.plan-recommendations {
  margin-top: 12px;
}

.recommendation-title {
  font-size: 13px;
  font-weight: 600;
  color: #1A1A2E;
  margin-bottom: 8px;
}

.recommendation-list {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.recommendation-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background: #F8FAFC;
  border-radius: 8px;
  font-size: 13px;
}

.recommendation-name {
  color: #4B5563;
}

.plan-card-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

/* 健身计划生成中消息 */
.plan-generating-message {
  animation: fadeIn 0.3s ease;
}

.plan-generating-content {
  background: #F0F2F5 !important;
  border-bottom-left-radius: 4px !important;
  padding: 18px 22px !important;
  max-width: 480px !important;
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 20px;
}

.typing-indicator .dot {
  width: 8px;
  height: 8px;
  background: linear-gradient(135deg, #FF6B35, #FF8C61);
  border-radius: 50%;
  animation: typingBounce 1.6s ease-in-out infinite;
}

.typing-indicator .dot:nth-child(1) {
  animation-delay: 0s;
}

.typing-indicator .dot:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator .dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typingBounce {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  30% {
    transform: translateY(-10px);
    opacity: 1;
  }
}

/* 健身计划生成动态等待效果 - 全新设计 */
.plan-generating-animation {
  background: linear-gradient(135deg, #FFF8F6 0%, #FFFFFF 50%, #FFF8F6 100%);
  border: 1px solid rgba(255, 107, 53, 0.15);
  border-radius: 16px;
  padding: 24px;
  min-width: 360px;
  max-width: 420px;
  box-shadow: 0 4px 20px rgba(255, 107, 53, 0.08);
  animation: generatingCardIn 0.4s ease;
}

@keyframes generatingCardIn {
  from {
    opacity: 0;
    transform: translateY(10px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.generating-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 20px;
}

.generating-icon-wrapper {
  position: relative;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #FF6B35;
}

.generating-icon-wrapper .n-icon {
  animation: iconBounce 1s ease-in-out infinite;
  z-index: 2;
}

@keyframes iconBounce {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.15);
  }
}

.icon-pulse-ring {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: rgba(255, 107, 53, 0.15);
  animation: pulseRing 1.5s ease-out infinite;
}

@keyframes pulseRing {
  0% {
    transform: scale(0.8);
    opacity: 1;
  }
  100% {
    transform: scale(1.4);
    opacity: 0;
  }
}

.generating-text {
  flex: 1;
}

.generating-title {
  font-size: 15px;
  font-weight: 700;
  color: #1A1A2E;
  margin-bottom: 4px;
}

.generating-subtitle {
  font-size: 13px;
  color: #6B7280;
  animation: textFade 0.3s ease;
}

@keyframes textFade {
  from {
    opacity: 0;
    transform: translateX(-10px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* 进度条 */
.generating-progress-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.progress-track {
  flex: 1;
  height: 8px;
  background: #F0F2F5;
  border-radius: 4px;
  overflow: hidden;
  position: relative;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #FF6B35 0%, #FF8C61 50%, #FF6B35 100%);
  background-size: 200% 100%;
  border-radius: 4px;
  transition: width 0.3s ease;
  animation: progressShine 2s linear infinite;
}

@keyframes progressShine {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

.progress-glow {
  position: absolute;
  top: 50%;
  transform: translate(-50%, -50%);
  width: 20px;
  height: 20px;
  background: radial-gradient(circle, rgba(255, 107, 53, 0.4) 0%, transparent 70%);
  border-radius: 50%;
  transition: left 0.3s ease;
  pointer-events: none;
}

.progress-percentage {
  font-size: 14px;
  font-weight: 700;
  color: #FF6B35;
  min-width: 40px;
  text-align: right;
}

/* 步骤指示器 */
.generating-steps {
  display: flex;
  justify-content: space-between;
  gap: 8px;
}

.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  flex: 1;
  transition: all 0.3s ease;
}

.step-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #E5E7EB;
  transition: all 0.3s ease;
}

.step-item span {
  font-size: 11px;
  color: #9CA3AF;
  font-weight: 500;
  text-align: center;
  transition: all 0.3s ease;
}

.step-item.active .step-dot {
  background: linear-gradient(135deg, #FF6B35, #FF8C61);
  box-shadow: 0 0 8px rgba(255, 107, 53, 0.5);
  transform: scale(1.3);
}

.step-item.active span {
  color: #FF6B35;
  font-weight: 600;
}

.step-item.done .step-dot {
  background: linear-gradient(135deg, #10B981, #059669);
}

.step-item.done span {
  color: #10B981;
  font-weight: 600;
}

/* 健身计划消息样式 - 作为AI消息显示 */
.plan-message-wrapper {
  animation: fadeIn 0.4s ease;
}

.plan-message-content {
  background: transparent !important;
  border-bottom-left-radius: 4px !important;
  padding: 0 !important;
  max-width: 900px;
  width: 100%;
  overflow: visible;
}

/* 计划卡片淡入淡出动画 */
.plan-fade-enter-active,
.plan-fade-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.plan-fade-enter-from {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}

.plan-fade-leave-to {
  opacity: 0;
  transform: translateY(-20px) scale(0.95);
  max-height: 0;
  margin: 0;
  padding: 0;
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

/* 营养指导按钮 - 绿色主题 */
.btn-nutrition {
  color: #10B981 !important;
  border: 1px solid #10B981 !important;
}

.btn-nutrition:hover {
  color: #059669 !important;
  border-color: #059669 !important;
  background: rgba(16, 185, 129, 0.05);
}

.btn-nutrition :deep(.n-icon) {
  color: #10B981 !important;
}

.btn-nutrition :deep(.n-button__content) {
  color: #10B981 !important;
}

.btn-nutrition :deep(.n-button__border) {
  border: 1px solid #10B981 !important;
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

/* 推荐教练列表 */
.coaches-list {
  flex: 1;
  overflow-y: auto;
  min-height: 0;
}

.coaches-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 24px;
  color: #6B7280;
  font-size: 13px;
}

.coaches-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 24px;
}

.coach-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.coach-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  background: #F8FAFC;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid transparent;
}

.coach-item:hover {
  background: white;
  border-color: #FF6B35;
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.15);
  transform: translateY(-2px);
}

.coach-item-avatar {
  position: relative;
  flex-shrink: 0;
}

.coach-item-avatar img {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.coach-status {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  border: 2px solid white;
}

.coach-status.online {
  background: #10B981;
}

.coach-status.busy {
  background: #F59E0B;
}

.coach-status.offline {
  background: #9CA3AF;
}

.coach-item-info {
  flex: 1;
  min-width: 0;
}

.coach-item-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.coach-item-name {
  font-weight: 600;
  font-size: 15px;
  color: #1A1A2E;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.coach-item-title {
  font-size: 10px;
  padding: 2px 6px;
  background: rgba(255, 107, 53, 0.1);
  color: #FF6B35;
  border-radius: 8px;
  font-weight: 500;
  white-space: nowrap;
  flex-shrink: 0;
}

.coach-item-rating {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
}

.rating-stars {
  display: flex;
  gap: 1px;
}

.rating-stars .star {
  width: 11px;
  height: 11px;
  fill: #E5E7EB;
}

.rating-stars .star.filled {
  fill: #F59E0B;
}

.rating-score {
  font-size: 12px;
  font-weight: 600;
  color: #F59E0B;
}

.coach-item-tags {
  display: flex;
  flex-wrap: nowrap;
  gap: 6px;
  overflow: hidden;
}

.coach-tag {
  font-size: 10px;
  padding: 2px 6px;
  background: #F0F2F5;
  color: #6B7280;
  border-radius: 8px;
  white-space: nowrap;
  flex-shrink: 0;
}

/* 教练详情弹窗 */
.coach-detail-modal :deep(.n-card__content) {
  padding: 20px;
}

.coach-detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.coach-detail-header {
  display: flex;
  gap: 16px;
  align-items: center;
}

.coach-detail-avatar img {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.coach-detail-info {
  flex: 1;
}

.coach-detail-name {
  font-size: 20px;
  font-weight: 700;
  color: #1A1A2E;
  margin-bottom: 4px;
}

.coach-detail-title {
  font-size: 14px;
  color: #6B7280;
  margin-bottom: 8px;
}

.coach-detail-rating {
  display: flex;
  align-items: center;
  gap: 8px;
}

.coach-detail-stats {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #F8FAFC;
  border-radius: 12px;
}

.stat-item {
  flex: 1;
  text-align: center;
}

.stat-value {
  font-size: 18px;
  font-weight: 700;
  color: #FF6B35;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #6B7280;
}

.coach-detail-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.section-label {
  font-size: 14px;
  font-weight: 600;
  color: #1A1A2E;
}

.coach-detail-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.coach-detail-bio {
  font-size: 14px;
  color: #6B7280;
  line-height: 1.6;
}

.coach-detail-actions {
  display: flex;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #F0F2F5;
}

.coach-detail-actions .btn-chat,
.coach-detail-actions .btn-buy {
  flex: 1;
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

/* 计划生成弹窗 */
.plan-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 计划详情弹窗 */
.plan-detail-content {
  max-height: 60vh;
  overflow-y: auto;
}

.plan-detail-header {
  margin-bottom: 16px;
}

.plan-detail-meta {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.plan-detail-duration {
  font-size: 13px;
  color: #6B7280;
}

.plan-detail-summary {
  font-size: 14px;
  color: #4B5563;
  line-height: 1.6;
  margin-bottom: 16px;
  padding: 12px;
  background: #F9FAFB;
  border-radius: 8px;
}

.plan-detail-weekly {
  margin-bottom: 16px;
}

.weekly-title,
.advice-title {
  font-size: 14px;
  font-weight: 600;
  color: #1A1A2E;
  margin-bottom: 12px;
}

.weekly-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.weekly-item {
  display: flex;
  gap: 12px;
  padding: 10px 12px;
  background: #F9FAFB;
  border-radius: 8px;
}

.weekly-day {
  font-weight: 600;
  color: #FF6B35;
  min-width: 50px;
  font-size: 13px;
}

.weekly-content {
  font-size: 13px;
  color: #4B5563;
  line-height: 1.5;
}

.advice-content {
  font-size: 13px;
  color: #4B5563;
  line-height: 1.6;
  margin-bottom: 8px;
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

  .input-content-area {
    padding: 16px 16px 12px;
  }

  .chat-input :deep(.n-input__textarea-el) {
    font-size: 16px; /* 防止iOS缩放 */
    min-height: 60px;
  }

  .chat-input-actions {
    padding: 10px 14px;
  }

  .input-actions-left {
    gap: 10px;
  }

  .input-hint {
    display: none; /* 移动端隐藏提示 */
  }

  .btn-plan {
    font-size: 12px;
    padding: 4px 10px;
  }

  .plan-card-fixed-area {
    padding: 12px 16px;
  }

  .plan-card-container {
    padding: 12px;
  }

  .plan-card-details {
    gap: 8px;
  }

  .plan-detail-item {
    font-size: 12px;
  }
}

/* markstream-vue 样式适配 */
.ai-stream-content {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

/* 消息气泡包装器：状态标签在上方 */
.message-bubble-wrapper {
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-width: 80%;
  align-items: flex-start;
}

.message.user .message-bubble-wrapper {
  align-items: flex-end;
}

.stream-status {
  display: inline-flex;
  align-items: center;
  align-self: flex-start;
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(255, 107, 53, 0.08);
  color: #E55A2B;
  font-size: 12px;
  font-weight: 500;
  line-height: 1;
}

/* 气泡内的打字动画 */
.ai-stream-content .typing-indicator {
  margin-top: 4px;
}

.markstream-wrapper :deep(.markstream-vue) {
  font-size: 14px;
  line-height: 1.6;
  color: #1A1A2E;
}

.markstream-wrapper :deep(.markstream-vue p) {
  margin: 8px 0;
}

.markstream-wrapper :deep(.markstream-vue h1),
.markstream-wrapper :deep(.markstream-vue h2),
.markstream-wrapper :deep(.markstream-vue h3),
.markstream-wrapper :deep(.markstream-vue h4) {
  margin: 16px 0 12px;
  font-weight: 600;
  line-height: 1.4;
}

.markstream-wrapper :deep(.markstream-vue h1) {
  font-size: 18px;
  border-bottom: 1px solid #E5E7EB;
  padding-bottom: 8px;
}

.markstream-wrapper :deep(.markstream-vue h2) {
  font-size: 16px;
}

.markstream-wrapper :deep(.markstream-vue h3) {
  font-size: 15px;
}

.markstream-wrapper :deep(.markstream-vue ul),
.markstream-wrapper :deep(.markstream-vue ol) {
  margin: 8px 0;
  padding-left: 20px;
}

.markstream-wrapper :deep(.markstream-vue li) {
  margin: 4px 0;
}

.markstream-wrapper :deep(.markstream-vue code) {
  background: rgba(0, 0, 0, 0.05);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  color: #E55A2B;
}

.markstream-wrapper :deep(.markstream-vue pre) {
  background: #1A1A2E;
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 12px 0;
}

.markstream-wrapper :deep(.markstream-vue pre code) {
  background: transparent;
  color: #E5E7EB;
  padding: 0;
  font-size: 13px;
  line-height: 1.6;
}

.markstream-wrapper :deep(.markstream-vue blockquote) {
  border-left: 4px solid #FF6B35;
  margin: 12px 0;
  padding: 8px 16px;
  background: rgba(255, 107, 53, 0.05);
  border-radius: 0 8px 8px 0;
}

.markstream-wrapper :deep(.markstream-vue table) {
  width: 100%;
  border-collapse: collapse;
  margin: 12px 0;
  font-size: 13px;
}

.markstream-wrapper :deep(.markstream-vue th),
.markstream-wrapper :deep(.markstream-vue td) {
  padding: 8px 12px;
  border: 1px solid #E5E7EB;
  text-align: left;
}

.markstream-wrapper :deep(.markstream-vue th) {
  background: #F8FAFC;
  font-weight: 600;
}

.markstream-wrapper :deep(.markstream-vue a) {
  color: #FF6B35;
  text-decoration: none;
}

.markstream-wrapper :deep(.markstream-vue a:hover) {
  text-decoration: underline;
}

.markstream-wrapper :deep(.markstream-vue strong) {
  font-weight: 600;
  color: #1A1A2E;
}

.markstream-wrapper :deep(.markstream-vue hr) {
  border: none;
  border-top: 1px solid #E5E7EB;
  margin: 16px 0;
}
</style>
