<template>
  <div class="cards-page">
    <!-- 我的会员卡区域 -->
    <div class="my-card-section" v-if="myMembership">
      <div class="section-header">
        <h3 class="section-title">
          <n-icon :component="CardOutline" size="20" />
          我的会员卡
        </h3>
        <n-button type="primary" size="small" class="renew-btn" @click="handleRenew">
          <template #icon>
            <n-icon :component="RefreshOutline" />
          </template>
          续费
        </n-button>
      </div>
      <div class="my-card">
        <div class="my-card-info">
          <div class="card-badge">{{ myMembership.membershipType || '会员' }}</div>
          <h3>VIP 会员卡</h3>
          <p v-if="myMembership.expireTime">有效期至: {{ formatDate(myMembership.expireTime) }} | 剩余: {{ calcDaysLeft(myMembership.expireTime) }}天</p>
          <p v-else>暂无会员信息</p>
        </div>
        <div class="my-card-status">
          <n-tag :type="myMembership.isActive ? 'success' : 'default'" size="large" round class="status-badge">{{ myMembership.isActive ? '有效' : '已过期' }}</n-tag>
          <p class="auto-renew" v-if="myMembership.isActive">
            <n-icon :component="SyncCircleOutline" size="14" />
            自动续费已开启
          </p>
        </div>
      </div>
    </div>

    <!-- 未登录或无会员时显示提示 -->
    <div class="my-card-section" v-else>
      <div class="section-header">
        <h3 class="section-title">
          <n-icon :component="CardOutline" size="20" />
          我的会员卡
        </h3>
      </div>
      <div class="my-card empty-state">
        <div class="my-card-info">
          <h3>暂无会员卡</h3>
          <p>购买会员卡享受更多权益</p>
        </div>
      </div>
    </div>

    <!-- 会员卡套餐 - 横向滑动卡片布局 -->
    <div class="membership-section">
      <div class="section-header">
        <h3 class="section-title">
          <n-icon :component="CartOutline" size="20" />
          购买会员卡
        </h3>
      </div>
      <n-spin :show="loadingCards">
        <!-- 会员卡滑动容器 -->
        <div class="card-slider-container">
          <!-- 左滑动按钮 -->
          <button
            class="slider-btn slider-btn-left"
            @click="scrollCardsLeft"
          >
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M15 18L9 12L15 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>

          <!-- 右滑动按钮 -->
          <button
            class="slider-btn slider-btn-right"
            @click="scrollCardsRight"
          >
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M9 18L15 12L9 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>

          <!-- 会员卡滚动区域 -->
          <div ref="cardsSliderRef" class="membership-cards-slider" @scroll="handleCardsScroll">
            <div
              v-for="(card, index) in membershipCards"
              :key="card.id"
              class="membership-card-slide"
              :class="{ featured: card.isRecommend }"
              :style="{ animationDelay: `${index * 100}ms` }"
              @mouseenter="hoveredCard = card.id"
              @mouseleave="hoveredCard = null"
            >
              <!-- 推荐标签 -->
              <div v-if="card.isRecommend" class="featured-badge">
                <n-icon :component="StarOutline" size="12" />
                推荐
              </div>

              <!-- 节省标签 -->
              <div v-if="card.originalPrice && card.originalPrice > card.price" class="save-badge">
                省¥{{ Math.round(card.originalPrice - card.price) }}
              </div>

              <!-- 卡片头部 -->
              <div class="card-header-bg">
                <div class="card-icon-wrapper">
                  <div class="card-icon" v-html="getCardIcon(card.typeCode)"></div>
                </div>
                <div class="card-glow"></div>
              </div>

              <!-- 卡片内容 -->
              <div class="card-content">
                <div class="card-type">{{ card.name }}</div>
                <div class="card-duration">
                  <n-icon :component="TimeOutline" size="14" />
                  {{ card.durationDays }}天有效期
                </div>

                <!-- 价格区域 -->
                <div class="price-section">
                  <div class="price-wrapper">
                    <span class="currency">¥</span>
                    <span class="price">{{ Math.floor(card.price) }}</span>
                    <span class="decimal">.{{ getDecimal(card.price) }}</span>
                  </div>
                  <div class="price-meta">
                    <span v-if="card.originalPrice" class="original-price">¥{{ card.originalPrice }}</span>
                    <span class="unit">/{{ card.durationDays > 30 ? (Math.floor(card.durationDays/30) + '月') : card.durationDays + '天' }}</span>
                  </div>
                </div>

                <!-- 日均价格 -->
                <div class="daily-price" v-if="card.durationDays">
                  日均 <span class="highlight">¥{{ (card.price / card.durationDays).toFixed(2) }}</span>
                </div>

                <!-- 权益列表 -->
                <ul class="card-features">
                  <li v-for="(content, idx) in getCardFeatures(card)" :key="idx">
                    <n-icon :component="CheckmarkCircleOutline" size="16" class="feature-icon" />
                    <span class="feature-text">{{ content.title }}</span>
                  </li>
                  <li v-if="card.pointsReward > 0">
                    <n-icon :component="GiftOutline" size="16" class="feature-icon bonus" />
                    <span class="feature-text bonus">赠送 {{ card.pointsReward }} 积分</span>
                  </li>
                </ul>

                <!-- 购买按钮 -->
                <n-button
                  :type="card.isRecommend ? 'primary' : 'default'"
                  :class="['buy-btn', card.isRecommend ? 'btn-primary' : 'btn-secondary']"
                  size="large"
                  block
                  @click="handleBuy(card)"
                >
                  <template #icon>
                    <n-icon :component="card.isRecommend ? FlashOutline : CartOutline" />
                  </template>
                  {{ card.isRecommend ? '立即抢购' : '立即购买' }}
                </n-button>
              </div>

              <!-- 悬浮效果遮罩 -->
              <div class="hover-overlay" :class="{ active: hoveredCard === card.id }"></div>
            </div>
          </div>
        </div>
      </n-spin>
    </div>

    <!-- 购买记录 -->
    <div class="card-section">
      <div class="section-header">
        <h3 class="section-title">
          <n-icon :component="ReceiptOutline" size="20" />
          购买记录
        </h3>
      </div>

      <!-- 桌面端表格 -->
      <div v-if="!isMobile" class="table-wrapper">
        <n-data-table
          :columns="columns"
          :data="orderData"
          :pagination="pagination"
          :bordered="false"
          class="order-table"
          :loading="loadingOrders"
        />
      </div>

      <!-- 移动端卡片列表 -->
      <div v-else class="mobile-order-list">
        <n-spin :show="loadingOrders">
          <n-empty v-if="orderData.length === 0" description="暂无购买记录" />
          <div v-else class="order-timeline">
            <div
              v-for="(order, index) in orderData"
              :key="order.orderNo"
              class="timeline-item"
              :style="{ animationDelay: `${index * 80}ms` }"
            >
              <div class="timeline-dot" :class="order.status"></div>
              <div class="timeline-card" @click="showOrderDetail(order)">
                <div class="timeline-header">
                  <div class="order-info">
                    <span class="order-type">{{ order.cardType }}</span>
                    <span class="order-no">{{ order.orderNo.slice(-8) }}</span>
                  </div>
                  <n-tag :type="getStatusType(order.status)" size="small" round class="status-tag">
                    {{ getStatusText(order.status) }}
                  </n-tag>
                </div>
                <div class="timeline-body">
                  <div class="amount-section">
                    <span class="amount">¥{{ order.amount }}</span>
                    <span class="time">{{ formatDateTime(order.createTime) }}</span>
                  </div>
                </div>
                <div class="timeline-footer" v-if="order.status === 'pending'">
                  <n-button type="primary" size="small" @click.stop="handlePay(order)">
                    立即支付
                  </n-button>
                </div>
              </div>
            </div>
          </div>
          <!-- 移动端分页 -->
          <div class="mobile-pagination" v-if="orderData.length > pagination.pageSize">
            <n-pagination
              v-model:page="currentPage"
              :page-count="Math.ceil(orderData.length / pagination.pageSize)"
              :page-slot="5"
              size="small"
            />
          </div>
        </n-spin>
      </div>
    </div>

    <!-- 购买弹窗 -->
    <n-modal
      v-model:show="showBuyModal"
      preset="card"
      style="width: 90%; max-width: 520px"
      :show-header="false"
      class="buy-modal"
      :mask-closable="!payLoading"
    >
      <div class="buy-modal-content">
        <!-- 步骤条 -->
        <div class="steps-header">
          <div class="step" :class="{ active: payStep >= 1, completed: payStep > 1 }">
            <div class="step-number">1</div>
            <span class="step-label">确认订单</span>
          </div>
          <div class="step-line" :class="{ active: payStep >= 2 }"></div>
          <div class="step" :class="{ active: payStep >= 2, completed: payStep > 2 }">
            <div class="step-number">2</div>
            <span class="step-label">选择支付</span>
          </div>
          <div class="step-line" :class="{ active: payStep >= 3 }"></div>
          <div class="step" :class="{ active: payStep >= 3 }">
            <div class="step-number">3</div>
            <span class="step-label">完成支付</span>
          </div>
        </div>

        <!-- 订单确认区域 -->
        <div class="order-confirm-section">
          <div class="selected-card-preview">
            <div class="preview-icon" v-html="selectedCard ? getCardIcon(selectedCard.typeCode) : ''"></div>
            <div class="preview-info">
              <h4>{{ selectedCard?.name }}</h4>
              <p>{{ selectedCard?.durationDays }}天有效期</p>
            </div>
            <div class="preview-price">
              <span class="currency">¥</span>
              <span class="amount">{{ selectedCard?.price }}</span>
            </div>
          </div>

          <!-- 订单详情 -->
          <div class="order-details">
            <div class="detail-row">
              <span class="label">会员卡类型</span>
              <span class="value">{{ selectedCard?.name }}</span>
            </div>
            <div class="detail-row">
              <span class="label">有效期</span>
              <span class="value">{{ selectedCard?.durationDays }}天</span>
            </div>
            <div class="detail-row" v-if="selectedCard?.pointsReward > 0">
              <span class="label">赠送积分</span>
              <span class="value highlight">+{{ selectedCard?.pointsReward }} 积分</span>
            </div>
            <n-divider class="detail-divider" />
            <div class="detail-row total">
              <span class="label">订单总额</span>
              <span class="value price">
                <span class="currency">¥</span>
                <span class="amount">{{ selectedCard?.price }}</span>
              </span>
            </div>
          </div>
        </div>

        <n-divider class="section-divider" />

        <!-- 支付方式 -->
        <div class="payment-section">
          <p class="section-label">
            <n-icon :component="WalletOutline" size="16" />
            选择支付方式
          </p>
          <div class="payment-methods">
            <div
              v-for="method in paymentMethods"
              :key="method.value"
              class="payment-method-card"
              :class="{ active: paymentMethod === method.value, recommended: method.recommended }"
              @click="paymentMethod = method.value"
            >
              <div class="method-header">
                <div class="method-icon" :class="method.iconClass">
                  <n-icon :component="method.icon" size="24" />
                </div>
                <div class="method-info">
                  <span class="method-name">{{ method.name }}</span>
                  <span class="method-desc">{{ method.description }}</span>
                </div>
                <div class="method-check">
                  <n-icon v-if="paymentMethod === method.value" :component="CheckmarkCircleOutline" size="22" class="check-icon" />
                  <div v-else class="check-circle"></div>
                </div>
              </div>
              <div v-if="method.recommended" class="method-badge">推荐</div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <n-space justify="end" size="medium" class="modal-footer">
          <n-button size="large" @click="showBuyModal = false" :disabled="payLoading">取消</n-button>
          <n-button
            type="primary"
            size="large"
            :loading="payLoading"
            @click="confirmPay"
            class="pay-btn"
          >
            <template #icon>
              <n-icon :component="LockClosedOutline" />
            </template>
            确认支付 ¥{{ selectedCard?.price }}
          </n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 支付成功弹窗 -->
    <n-modal
      v-model:show="showSuccessModal"
      preset="card"
      style="width: 90%; max-width: 400px"
      :show-header="false"
      class="success-modal"
      :mask-closable="false"
    >
      <div class="success-content">
        <div class="success-animation">
          <div class="success-circle">
            <n-icon :component="CheckmarkOutline" size="48" class="success-icon" />
          </div>
          <div class="success-ring"></div>
        </div>
        <h3>支付成功！</h3>
        <p>您已成功购买 {{ successCardName }}</p>
        <div class="success-details">
          <div class="success-row">
            <span>订单编号</span>
            <span class="order-no">{{ successOrderNo }}</span>
          </div>
          <div class="success-row">
            <span>支付金额</span>
            <span class="amount">¥{{ successAmount }}</span>
          </div>
        </div>
        <n-button type="primary" size="large" block @click="closeSuccessModal" class="confirm-btn">
          确定
        </n-button>
      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, h, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useMessage, NTag, NIcon, NPagination } from 'naive-ui'
import {
  CardOutline,
  CartOutline,
  TimeOutline,
  RefreshOutline,
  SyncCircleOutline,
  LogoAlipay,
  WalletOutline,
  CheckmarkCircleOutline,
  CheckmarkOutline,
  StarOutline,
  FlashOutline,
  GiftOutline,
  ReceiptOutline,
  ShieldCheckmarkOutline,
  LockClosedOutline
} from '@vicons/ionicons5'
import {
  getMembershipCardList,
  getMyMembership,
  getMyMembershipOrders,
  createMembershipOrder,
  payMembershipOrder,
  submitAlipayForm
} from '@/api/membership'

const message = useMessage()

// 响应式状态
const windowWidth = ref(window.innerWidth)
const isMobile = computed(() => windowWidth.value < 768)
const currentPage = ref(1)

// 会员卡滑动相关
const cardsSliderRef = ref(null)
const cardsScrollPosition = ref(0)
const cardsMaxScroll = ref(0)

// 计算属性：是否可以向左/右滚动
const showLeftBtn = computed(() => {
  return cardsScrollPosition.value > 5
})

const showRightBtn = computed(() => {
  if (!cardsSliderRef.value) return true
  const maxScroll = cardsSliderRef.value.scrollWidth - cardsSliderRef.value.clientWidth
  return cardsScrollPosition.value < maxScroll - 5
})

// 处理会员卡滚动事件
function handleCardsScroll() {
  if (!cardsSliderRef.value) return
  cardsScrollPosition.value = cardsSliderRef.value.scrollLeft
  cardsMaxScroll.value = cardsSliderRef.value.scrollWidth - cardsSliderRef.value.clientWidth
}

// 向左滚动会员卡
function scrollCardsLeft() {
  if (!cardsSliderRef.value) return
  const cardWidth = cardsSliderRef.value.querySelector('.membership-card-slide')?.offsetWidth || 300
  const gap = 20
  const scrollAmount = cardWidth + gap
  cardsSliderRef.value.scrollBy({ left: -scrollAmount, behavior: 'smooth' })
}

// 向右滚动会员卡
function scrollCardsRight() {
  if (!cardsSliderRef.value) return
  const cardWidth = cardsSliderRef.value.querySelector('.membership-card-slide')?.offsetWidth || 300
  const gap = 20
  const scrollAmount = cardWidth + gap
  cardsSliderRef.value.scrollBy({ left: scrollAmount, behavior: 'smooth' })
}

// 响应式列数
const gridCols = computed(() => {
  const width = windowWidth.value
  if (width < 640) return 1
  if (width < 1024) return 2
  return 4
})

// 监听窗口大小变化
function handleResize() {
  windowWidth.value = window.innerWidth
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  // 初始化滚动状态
  nextTick(() => {
    handleCardsScroll()
  })
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

// 悬浮卡片
const hoveredCard = ref(null)

// 支付步骤
const payStep = ref(2)

// 支付成功相关
const showSuccessModal = ref(false)
const successCardName = ref('')
const successOrderNo = ref('')
const successAmount = ref(0)

// 会员卡图标 - 优化后的SVG
const cardIcons = {
  TRIAL: `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>`,
  MONTHLY: `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/></svg>`,
  QUARTERLY: `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>`,
  YEARLY: `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>`
}

function getCardIcon(typeCode) {
  return cardIcons[typeCode] || cardIcons.MONTHLY
}

function getCardFeatures(card) {
  return (card.contents || []).filter(c => c.contentType === 'BENEFIT').slice(0, 3)
}

function getDecimal(price) {
  const decimal = (price % 1).toFixed(2).substring(2)
  return decimal
}

// 支付方式配置
const paymentMethods = [
  {
    value: 'ALIPAY',
    name: '支付宝',
    description: '数亿用户的选择，安全快捷',
    icon: LogoAlipay,
    iconClass: 'alipay',
    recommended: true
  },
  {
    value: 'BALANCE',
    name: '余额支付',
    description: '使用账户余额直接支付',
    icon: WalletOutline,
    iconClass: 'balance',
    recommended: false
  }
]

// 数据状态
const loadingCards = ref(false)
const loadingOrders = ref(false)
const membershipCards = ref([])
const myMembership = ref(null)
const orderData = ref([])

// 加载会员卡列表
async function loadCardsFromAPI() {
  loadingCards.value = true
  try {
    const data = await getMembershipCardList()
    if (Array.isArray(data)) {
      membershipCards.value = data.map(item => ({
        id: item.id,
        name: item.name,
        subtitle: item.subtitle,
        price: parseFloat(item.price),
        originalPrice: item.originalPrice ? parseFloat(item.originalPrice) : null,
        durationDays: item.durationDays,
        pointsReward: item.pointsReward,
        isRecommend: item.isRecommend,
        typeCode: item.typeCode,
        contents: item.contents || []
      }))
    }
  } catch (error) {
    console.error('加载会员卡失败:', error)
  } finally {
    loadingCards.value = false
  }
}

// 加载我的会员信息
async function loadMyMembership() {
  try {
    const data = await getMyMembership()
    if (data) {
      myMembership.value = data
    }
  } catch (error) {
    console.error('加载会员信息失败:', error)
  }
}

// 加载我的订单
async function loadMyOrders() {
  loadingOrders.value = true
  try {
    const data = await getMyMembershipOrders()
    if (data) {
      const records = data.records || data
      orderData.value = records.map(item => ({
        orderNo: item.orderNo,
        cardType: item.cardName,
        amount: parseFloat(item.payAmount),
        createTime: item.createTime,
        status: mapOrderStatus(item.status)
      }))
    }
  } catch (error) {
    console.error('加载订单失败:', error)
  } finally {
    loadingOrders.value = false
  }
}

function mapOrderStatus(status) {
  const statusMap = {
    'PAID': 'completed',
    'PENDING': 'pending',
    'CANCELLED': 'cancelled',
    'TIMEOUT': 'expired',
    'REFUNDED': 'refunded'
  }
  return statusMap[status] || status.toLowerCase()
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

function formatDateTime(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) {
    const hours = Math.floor(diff / (1000 * 60 * 60))
    if (hours === 0) {
      const minutes = Math.floor(diff / (1000 * 60))
      return minutes <= 0 ? '刚刚' : `${minutes}分钟前`
    }
    return `${hours}小时前`
  } else if (days === 1) {
    return '昨天'
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
  }
}

function getStatusType(status) {
  const statusMap = {
    'completed': 'success',
    'pending': 'warning',
    'cancelled': 'default',
    'expired': 'default',
    'refunded': 'info'
  }
  return statusMap[status] || 'default'
}

function getStatusText(status) {
  const statusMap = {
    'completed': '已完成',
    'pending': '待支付',
    'cancelled': '已取消',
    'expired': '已过期',
    'refunded': '已退款'
  }
  return statusMap[status] || status
}

function showOrderDetail(order) {
  // 可以展开显示订单详情
  message.info(`订单号: ${order.orderNo}`)
}

function handlePay(order) {
  message.info('跳转支付页面...')
}

function calcDaysLeft(expireTime) {
  if (!expireTime) return 0
  const now = new Date()
  const expire = new Date(expireTime)
  const diff = expire - now
  return Math.max(0, Math.ceil(diff / (1000 * 60 * 60 * 24)))
}

onMounted(() => {
  loadCardsFromAPI()
  loadMyMembership()
  loadMyOrders()
})

const showBuyModal = ref(false)
const payLoading = ref(false)
const paymentMethod = ref('ALIPAY')
const selectedCard = ref(null)

const columns = [
  { title: '订单号', key: 'orderNo', width: 140 },
  { title: '卡类型', key: 'cardType' },
  {
    title: '金额',
    key: 'amount',
    width: 100,
    render(row) {
      return h('span', { style: { color: '#FF6B35', fontWeight: 600 } }, `¥${row.amount}`)
    }
  },
  { title: '购买时间', key: 'createTime', width: 120 },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render(row) {
      const statusMap = {
        'completed': { type: 'success', text: '已完成' },
        'pending': { type: 'warning', text: '待支付' },
        'cancelled': { type: 'default', text: '已取消' },
        'expired': { type: 'default', text: '已过期' },
        'refunded': { type: 'info', text: '已退款' }
      }
      const status = statusMap[row.status] || { type: 'default', text: row.status }
      return h(NTag, { type: status.type, size: 'small', round: true }, { default: () => status.text })
    }
  }
]

const pagination = {
  pageSize: 5,
  showSizePicker: true,
  pageSizes: [5, 10, 20]
}

function handleBuy(card) {
  selectedCard.value = card
  paymentMethod.value = 'ALIPAY'
  payStep.value = 2
  showBuyModal.value = true
}

function handleRenew() {
  const recommendCard = membershipCards.value.find(c => c.isRecommend)
  if (recommendCard) {
    selectedCard.value = recommendCard
    paymentMethod.value = 'ALIPAY'
    payStep.value = 2
    showBuyModal.value = true
  } else {
    message.warning('暂无可购买的会员卡')
  }
}

function closeSuccessModal() {
  showSuccessModal.value = false
  loadMyMembership()
  loadMyOrders()
}

async function confirmPay() {
  payLoading.value = true
  payStep.value = 3
  try {
    // 创建订单
    const orderData = await createMembershipOrder({
      cardId: selectedCard.value.id,
      remark: `购买${selectedCard.value.name}`
    })

    if (!orderData || !orderData.orderNo) {
      message.error('创建订单失败')
      payStep.value = 2
      return
    }

    // 支付订单
    const payData = await payMembershipOrder({
      orderNo: orderData.orderNo,
      payMethod: paymentMethod.value
    })

    if (!payData) {
      message.error('支付请求失败')
      payStep.value = 2
      return
    }

    // 支付宝支付，提交表单
    if (paymentMethod.value === 'ALIPAY' && payData.payForm) {
      submitAlipayForm(payData.payForm)
    } else if (paymentMethod.value === 'BALANCE') {
      // 显示成功弹窗
      successCardName.value = selectedCard.value.name
      successOrderNo.value = orderData.orderNo
      successAmount.value = selectedCard.value.price
      showBuyModal.value = false
      showSuccessModal.value = true
    }

  } catch (error) {
    console.error('支付失败:', error)
    message.error(error.response?.data?.message || error.message || '支付失败，请重试')
    payStep.value = 2
  } finally {
    payLoading.value = false
  }
}
</script>

<style scoped>
/* ==================== 页面基础 ==================== */
.cards-page {
  max-width: 1400px;
  margin: 0 auto;
}

/* ==================== 我的会员卡区域 ==================== */
.my-card-section {
  background: linear-gradient(135deg, #1A1A2E 0%, #2D3748 100%);
  border-radius: 24px;
  padding: 28px;
  margin-bottom: 36px;
  box-shadow: 0 12px 40px rgba(26, 26, 46, 0.35);
  position: relative;
  overflow: hidden;
}

.my-card-section::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -20%;
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(255, 107, 53, 0.15) 0%, transparent 70%);
  pointer-events: none;
}

.my-card-section .section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  position: relative;
  z-index: 1;
}

.my-card-section .section-title {
  font-size: 18px;
  font-weight: 600;
  color: white;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.my-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
  position: relative;
  z-index: 1;
}

.my-card.empty-state {
  opacity: 0.7;
}

.my-card-info {
  color: white;
}

.card-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: linear-gradient(135deg, #FF6B35, #FF8C61);
  color: white;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 14px;
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.3);
}

.my-card-info h3 {
  font-size: 32px;
  margin: 0 0 10px;
  font-weight: 700;
  letter-spacing: 0.5px;
  background: linear-gradient(135deg, #fff 0%, rgba(255,255,255,0.8) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.my-card-info p {
  color: rgba(255,255,255,0.65);
  font-size: 14px;
  margin: 0;
}

.my-card-status {
  text-align: right;
}

.status-badge {
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.auto-renew {
  color: rgba(255,255,255,0.6);
  font-size: 13px;
  margin: 14px 0 0;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 6px;
}

.renew-btn {
  border-radius: 12px;
  font-weight: 600;
  background: linear-gradient(135deg, #FF6B35, #E55A2B);
  border: none;
  padding: 8px 18px;
  box-shadow: 0 4px 15px rgba(255, 107, 53, 0.4);
  transition: all 0.3s ease;
}

.renew-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 107, 53, 0.5);
}

/* ==================== 购买会员卡区域 - 横向滑动布局 ==================== */
.membership-section {
  margin-bottom: 40px;
}

.membership-section .section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 0 4px;
}

.membership-section .section-title {
  font-size: 22px;
  font-weight: 700;
  color: #1A1A2E;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 会员卡滑动容器 */
.card-slider-container {
  position: relative;
  padding: 0 60px;
}

/* 滑动按钮 - 橙色主题设计 */
.slider-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF6B35 0%, #E55A2B 100%);
  border: none;
  box-shadow: 0 4px 16px rgba(255, 107, 53, 0.4);
  cursor: pointer;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  color: white;
  outline: none;
}

.slider-btn:hover {
  background: linear-gradient(135deg, #FF8C61 0%, #FF6B35 100%);
  box-shadow: 0 6px 24px rgba(255, 107, 53, 0.55);
  transform: translateY(-50%) scale(1.1);
}

.slider-btn:active {
  transform: translateY(-50%) scale(0.95);
}

.slider-btn-left {
  left: 0;
}

.slider-btn-right {
  right: 0;
}

.slider-btn svg {
  width: 22px;
  height: 22px;
  pointer-events: none;
}

/* 会员卡滑动区域 */
.membership-cards-slider {
  display: flex;
  gap: 20px;
  overflow-x: auto;
  scroll-behavior: smooth;
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE and Edge */
  padding: 8px 4px 20px;
}

.membership-cards-slider::-webkit-scrollbar {
  display: none; /* Chrome, Safari, Opera */
}

/* 会员卡卡片 - 滑动布局 */
.membership-card-slide {
  flex: 0 0 330px;
  min-width: 330px;
  max-width: 330px;
  position: relative;
  background: white;
  border-radius: 24px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.06);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  border: 2px solid transparent;
  animation: slideInUp 0.5s ease forwards;
  opacity: 0;
}

.membership-card-slide:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 50px rgba(0,0,0,0.12);
}

.membership-card.featured {
  border-color: #FF6B35;
  background: linear-gradient(180deg, white 0%, #FFFBF9 100%);
}

.membership-card.featured::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #FF6B35, #FF8C61);
}

/* 推荐标签 */
.featured-badge {
  position: absolute;
  top: 16px;
  right: -30px;
  background: linear-gradient(135deg, #FF6B35, #E55A2B);
  color: white;
  padding: 6px 36px;
  font-size: 12px;
  font-weight: 700;
  transform: rotate(45deg);
  z-index: 10;
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.3);
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 节省标签 */
.save-badge {
  position: absolute;
  top: 16px;
  left: 16px;
  background: linear-gradient(135deg, #06D6A0, #2EC4B6);
  color: white;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 700;
  z-index: 10;
  box-shadow: 0 2px 8px rgba(6, 214, 160, 0.3);
}

/* 卡片头部背景 */
.card-header-bg {
  position: relative;
  height: 100px;
  background: linear-gradient(135deg, #F8F9FA 0%, #E9ECEF 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.membership-card.featured .card-header-bg {
  background: linear-gradient(135deg, #FFF5F2 0%, #FFE8E0 100%);
}

.card-icon-wrapper {
  position: relative;
  z-index: 2;
}

.card-icon {
  font-size: 48px;
  color: #6B7280;
  transition: all 0.3s ease;
}

.membership-card.featured .card-icon {
  color: #FF6B35;
}

.membership-card:hover .card-icon {
  transform: scale(1.1);
}

.card-glow {
  position: absolute;
  width: 120px;
  height: 120px;
  background: radial-gradient(circle, rgba(255, 107, 53, 0.15) 0%, transparent 70%);
  border-radius: 50%;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.membership-card:hover .card-glow {
  opacity: 1;
}

/* 卡片内容 */
.card-content {
  padding: 20px 24px 24px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.card-type {
  font-size: 22px;
  font-weight: 700;
  color: #1A1A2E;
  margin-bottom: 6px;
  text-align: center;
}

.card-duration {
  font-size: 13px;
  color: #6B7280;
  margin-bottom: 16px;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

/* 价格区域 */
.price-section {
  text-align: center;
  margin-bottom: 8px;
}

.price-wrapper {
  display: inline-flex;
  align-items: flex-start;
  color: #FF6B35;
  font-weight: 800;
}

.price-wrapper .currency {
  font-size: 20px;
  margin-top: 4px;
}

.price-wrapper .price {
  font-size: 42px;
  line-height: 1;
}

.price-wrapper .decimal {
  font-size: 20px;
  margin-top: 4px;
  opacity: 0.8;
}

.price-meta {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-top: 4px;
}

.original-price {
  font-size: 14px;
  color: #9CA3AF;
  text-decoration: line-through;
}

.unit {
  font-size: 13px;
  color: #6B7280;
}

.daily-price {
  text-align: center;
  font-size: 12px;
  color: #6B7280;
  margin-bottom: 16px;
}

.daily-price .highlight {
  color: #06D6A0;
  font-weight: 600;
}

/* 权益列表 */
.card-features {
  list-style: none;
  padding: 0;
  margin: 0 0 20px;
  flex: 1;
}

.card-features li {
  padding: 8px 0;
  display: flex;
  align-items: flex-start;
  gap: 10px;
  font-size: 13px;
  color: #4B5563;
  border-bottom: 1px dashed #E5E7EB;
}

.card-features li:last-child {
  border-bottom: none;
}

.feature-icon {
  color: #06D6A0;
  flex-shrink: 0;
  margin-top: 1px;
}

.feature-icon.bonus {
  color: #FF6B35;
}

.feature-text {
  flex: 1;
  line-height: 1.5;
}

.feature-text.bonus {
  color: #FF6B35;
  font-weight: 600;
}

/* 购买按钮 */
.buy-btn {
  width: 100%;
  border-radius: 14px;
  font-weight: 600;
  font-size: 15px;
  height: 48px;
  transition: all 0.3s ease;
}

.btn-primary {
  background: linear-gradient(135deg, #FF6B35, #E55A2B);
  border: none;
  box-shadow: 0 6px 20px rgba(255, 107, 53, 0.35);
}

.btn-primary:hover {
  background: linear-gradient(135deg, #FF8C61, #FF6B35);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(255, 107, 53, 0.45);
}

.btn-secondary {
  background: #F3F4F6;
  color: #1A1A2E;
  border: 2px solid #E5E7EB;
}

.btn-secondary:hover {
  background: #E5E7EB;
  border-color: #D1D5DB;
}

/* 悬浮遮罩 */
.hover-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, transparent 50%, rgba(255, 107, 53, 0.03) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}

.hover-overlay.active {
  opacity: 1;
}

/* 入场动画 */
@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.slide-in {
  animation: slideInUp 0.5s ease forwards;
  opacity: 0;
}

/* ==================== 购买记录区域 ==================== */
.card-section {
  background: white;
  border-radius: 20px;
  padding: 28px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.06);
  transition: all 0.3s ease;
}

.card-section:hover {
  box-shadow: 0 8px 32px rgba(0,0,0,0.1);
}

.card-section .section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.card-section .section-title {
  font-size: 20px;
  font-weight: 700;
  color: #1A1A2E;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.order-table :deep(.n-data-table-th) {
  font-weight: 600;
  color: #6B7280;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding: 16px;
  background: #F9FAFB;
}

.order-table :deep(.n-data-table-td) {
  padding: 16px;
  font-size: 14px;
}

/* ==================== 购买弹窗 ==================== */
.buy-modal :deep(.n-card) {
  border-radius: 24px;
  overflow: hidden;
}

.buy-modal-content {
  padding: 8px;
}

/* 步骤条 */
.steps-header {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 28px;
  padding: 0 20px;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.step-number {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #E5E7EB;
  color: #9CA3AF;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 14px;
  transition: all 0.3s ease;
}

.step.active .step-number {
  background: linear-gradient(135deg, #FF6B35, #E55A2B);
  color: white;
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.35);
}

.step.completed .step-number {
  background: #06D6A0;
  color: white;
}

.step-label {
  font-size: 12px;
  color: #9CA3AF;
  font-weight: 500;
  transition: all 0.3s ease;
}

.step.active .step-label {
  color: #FF6B35;
  font-weight: 600;
}

.step-line {
  flex: 1;
  height: 2px;
  background: #E5E7EB;
  margin: 0 12px;
  margin-bottom: 24px;
  transition: all 0.3s ease;
  max-width: 60px;
}

.step-line.active {
  background: linear-gradient(90deg, #06D6A0, #FF6B35);
}

/* 订单确认区域 */
.order-confirm-section {
  margin-bottom: 8px;
}

.selected-card-preview {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: linear-gradient(135deg, #FFFBF9 0%, #FFF5F2 100%);
  border-radius: 16px;
  border: 2px solid rgba(255, 107, 53, 0.15);
  margin-bottom: 20px;
}

.preview-icon {
  width: 56px;
  height: 56px;
  background: white;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #FF6B35;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.preview-info {
  flex: 1;
}

.preview-info h4 {
  font-size: 18px;
  font-weight: 700;
  color: #1A1A2E;
  margin: 0 0 4px;
}

.preview-info p {
  font-size: 13px;
  color: #6B7280;
  margin: 0;
}

.preview-price {
  color: #FF6B35;
  font-weight: 800;
}

.preview-price .currency {
  font-size: 16px;
}

.preview-price .amount {
  font-size: 28px;
}

/* 订单详情 */
.order-details {
  background: #F9FAFB;
  border-radius: 12px;
  padding: 16px 20px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  font-size: 14px;
}

.detail-row .label {
  color: #6B7280;
}

.detail-row .value {
  color: #1A1A2E;
  font-weight: 500;
}

.detail-row .value.highlight {
  color: #FF6B35;
  font-weight: 600;
}

.detail-row.total {
  padding-top: 12px;
}

.detail-row.total .label {
  font-size: 15px;
  font-weight: 600;
  color: #1A1A2E;
}

.detail-row.total .price {
  color: #FF6B35;
  font-weight: 800;
}

.detail-row.total .price .currency {
  font-size: 16px;
}

.detail-row.total .price .amount {
  font-size: 28px;
}

.detail-divider {
  margin: 8px 0 !important;
}

/* 分割线 */
.section-divider {
  margin: 24px 0 !important;
}

/* 支付方式区域 */
.payment-section {
  margin-bottom: 20px;
}

.section-label {
  font-size: 15px;
  font-weight: 600;
  color: #1A1A2E;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.payment-methods {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.payment-method-card {
  position: relative;
  padding: 16px 20px;
  border: 2px solid #E5E7EB;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: white;
}

.payment-method-card:hover {
  border-color: #D1D5DB;
  background: #FAFAFA;
}

.payment-method-card.active {
  border-color: #FF6B35;
  background: #FFFBF9;
  box-shadow: 0 4px 16px rgba(255, 107, 53, 0.15);
}

.method-header {
  display: flex;
  align-items: center;
  gap: 14px;
}

.method-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.method-icon.alipay {
  background: linear-gradient(135deg, #E3F2FD, #BBDEFB);
  color: #1677FF;
}

.method-icon.balance {
  background: linear-gradient(135deg, #FFF7E6, #FFE4B5);
  color: #FA8C16;
}

.method-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.method-name {
  font-size: 15px;
  font-weight: 600;
  color: #1A1A2E;
}

.method-desc {
  font-size: 12px;
  color: #9CA3AF;
}

.method-check {
  display: flex;
  align-items: center;
}

.check-icon {
  color: #FF6B35;
}

.check-circle {
  width: 22px;
  height: 22px;
  border: 2px solid #D1D5DB;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.payment-method-card.active .check-circle {
  border-color: #FF6B35;
  background: #FF6B35;
}

.method-badge {
  position: absolute;
  top: -1px;
  right: 20px;
  background: linear-gradient(135deg, #FF6B35, #E55A2B);
  color: white;
  padding: 3px 10px;
  border-radius: 0 0 8px 8px;
  font-size: 11px;
  font-weight: 600;
}

/* 安全提示 */
.security-notice {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  background: #F0FDF4;
  border-radius: 10px;
  font-size: 13px;
  color: #06D6A0;
}

/* 支付按钮 */
.pay-btn {
  background: linear-gradient(135deg, #FF6B35, #E55A2B);
  border: none;
  border-radius: 12px;
  font-weight: 600;
  padding: 0 28px;
  box-shadow: 0 6px 20px rgba(255, 107, 53, 0.35);
}

.pay-btn:hover {
  background: linear-gradient(135deg, #FF8C61, #FF6B35);
  box-shadow: 0 8px 25px rgba(255, 107, 53, 0.45);
}

.modal-footer {
  padding-top: 8px;
}

/* ==================== 支付成功弹窗 ==================== */
.success-modal :deep(.n-card) {
  border-radius: 24px;
}

.success-content {
  text-align: center;
  padding: 20px 16px;
}

.success-animation {
  position: relative;
  width: 100px;
  height: 100px;
  margin: 0 auto 24px;
}

.success-circle {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #06D6A0, #2EC4B6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 10px auto;
  box-shadow: 0 8px 24px rgba(6, 214, 160, 0.35);
  animation: scaleIn 0.5s ease;
}

@keyframes scaleIn {
  0% {
    transform: scale(0);
    opacity: 0;
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.success-icon {
  color: white;
}

.success-ring {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100px;
  height: 100px;
  border: 3px solid #06D6A0;
  border-radius: 50%;
  opacity: 0;
  animation: ringPulse 1.5s ease infinite;
}

@keyframes ringPulse {
  0% {
    transform: translateX(-50%) scale(0.8);
    opacity: 0.5;
  }
  100% {
    transform: translateX(-50%) scale(1.2);
    opacity: 0;
  }
}

.success-content h3 {
  font-size: 24px;
  font-weight: 700;
  color: #1A1A2E;
  margin: 0 0 8px;
}

.success-content > p {
  color: #6B7280;
  font-size: 14px;
  margin: 0 0 24px;
}

.success-details {
  background: #F9FAFB;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 24px;
}

.success-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 14px;
}

.success-row span:first-child {
  color: #6B7280;
}

.success-row .order-no {
  color: #1A1A2E;
  font-weight: 500;
  font-family: monospace;
}

.success-row .amount {
  color: #FF6B35;
  font-weight: 700;
  font-size: 16px;
}

.confirm-btn {
  background: linear-gradient(135deg, #FF6B35, #E55A2B);
  border: none;
  border-radius: 12px;
  font-weight: 600;
  height: 48px;
  box-shadow: 0 6px 20px rgba(255, 107, 53, 0.35);
}

.confirm-btn:hover {
  background: linear-gradient(135deg, #FF8C61, #FF6B35);
  box-shadow: 0 8px 25px rgba(255, 107, 53, 0.45);
}

/* ==================== 响应式适配 ==================== */
@media (max-width: 1024px) {
  .my-card-section,
  .card-section {
    padding: 22px;
    border-radius: 20px;
  }

  .membership-card-slide {
    border-radius: 20px;
    flex: 0 0 310px;
    min-width: 310px;
    max-width: 310px;
  }

  .card-content {
    padding: 18px 20px 20px;
  }

  .price-wrapper .price {
    font-size: 36px;
  }
}

@media (max-width: 768px) {
  .my-card {
    flex-direction: column;
    text-align: center;
  }

  .my-card-status {
    text-align: center;
  }

  .auto-renew {
    justify-content: center;
  }

  .my-card-info h3 {
    font-size: 26px;
  }

  .membership-card-slide {
    border-radius: 20px;
    flex: 0 0 290px;
    min-width: 290px;
    max-width: 290px;
  }

  .featured-badge {
    padding: 5px 28px;
    font-size: 11px;
  }

  .steps-header {
    padding: 0;
  }

  .step-label {
    font-size: 11px;
  }

  .selected-card-preview {
    flex-direction: column;
    text-align: center;
  }

  .card-slider-container {
    padding: 0 16px;
  }

  .slider-btn {
    width: 40px;
    height: 40px;
  }

  .slider-btn svg {
    width: 20px;
    height: 20px;
  }
}

@media (max-width: 480px) {
  .my-card-section,
  .card-section {
    padding: 18px;
    border-radius: 16px;
  }

  .my-card-info h3 {
    font-size: 22px;
  }

  .section-title {
    font-size: 18px !important;
  }

  .price-wrapper .price {
    font-size: 32px;
  }

  .card-type {
    font-size: 18px;
  }

  .buy-modal-content {
    padding: 4px;
  }

  .payment-method-card {
    padding: 14px 16px;
  }

  .method-icon {
    width: 40px;
    height: 40px;
  }

  /* 移动端会员卡滑动适配 */
  .card-slider-container {
    padding: 0 12px;
  }

  .membership-card-slide {
    flex: 0 0 270px;
    min-width: 270px;
    max-width: 270px;
    border-radius: 18px;
  }

  .slider-btn {
    width: 36px;
    height: 36px;
    display: none; /* 移动端隐藏滑动按钮，支持手势滑动 */
  }

  .membership-cards-slider {
    padding: 8px 0 16px;
    gap: 12px;
  }

  .card-header-bg {
    height: 90px;
  }

  .card-icon-wrapper {
    width: 56px;
    height: 56px;
  }

  .card-content {
    padding: 16px;
  }

  .card-type {
    font-size: 16px;
  }

  .price-wrapper .price {
    font-size: 28px;
  }

  .card-features li {
    font-size: 12px;
    padding: 6px 0;
  }
}

/* ==================== 移动端时间线列表样式 ==================== */
.mobile-order-list {
  padding: 8px 0;
}

.order-timeline {
  position: relative;
  padding-left: 28px;
}

.order-timeline::before {
  content: '';
  position: absolute;
  left: 8px;
  top: 8px;
  bottom: 8px;
  width: 2px;
  background: linear-gradient(180deg, #E5E7EB 0%, #D1D5DB 100%);
  border-radius: 1px;
}

.timeline-item {
  position: relative;
  margin-bottom: 16px;
  animation: slideInUp 0.4s ease forwards;
  opacity: 0;
}

.timeline-dot {
  position: absolute;
  left: -24px;
  top: 16px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #E5E7EB;
  border: 2px solid white;
  box-shadow: 0 0 0 2px #E5E7EB;
  z-index: 1;
  transition: all 0.3s ease;
}

.timeline-dot.completed {
  background: #06D6A0;
  box-shadow: 0 0 0 2px #06D6A0;
}

.timeline-dot.pending {
  background: #FF6B35;
  box-shadow: 0 0 0 2px #FF6B35;
  animation: pulse 2s infinite;
}

.timeline-dot.cancelled,
.timeline-dot.expired {
  background: #9CA3AF;
  box-shadow: 0 0 0 2px #9CA3AF;
}

.timeline-dot.refunded {
  background: #3B82F6;
  box-shadow: 0 0 0 2px #3B82F6;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.8;
  }
}

.timeline-card {
  background: white;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  border: 1px solid #F3F4F6;
  transition: all 0.3s ease;
  cursor: pointer;
}

.timeline-card:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  border-color: #E5E7EB;
}

.timeline-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.order-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.order-type {
  font-size: 15px;
  font-weight: 600;
  color: #1A1A2E;
}

.order-no {
  font-size: 12px;
  color: #9CA3AF;
  font-family: monospace;
}

.status-tag {
  flex-shrink: 0;
}

.timeline-body {
  margin-bottom: 12px;
}

.amount-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.amount {
  font-size: 22px;
  font-weight: 700;
  color: #FF6B35;
}

.time {
  font-size: 12px;
  color: #9CA3AF;
}

.timeline-footer {
  padding-top: 12px;
  border-top: 1px dashed #E5E7EB;
  display: flex;
  justify-content: flex-end;
}

.mobile-pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* 响应式时间线调整 */
@media (max-width: 480px) {
  .order-timeline {
    padding-left: 24px;
  }

  .timeline-dot {
    left: -20px;
    width: 10px;
    height: 10px;
  }

  .timeline-card {
    padding: 14px;
    border-radius: 14px;
  }

  .order-type {
    font-size: 14px;
  }

  .amount {
    font-size: 20px;
  }
}
</style>
