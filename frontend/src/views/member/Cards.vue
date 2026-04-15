<template>
  <div class="cards-page">
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

    <!-- 会员卡套餐 - 支持动态添加 -->
    <div class="membership-section">
      <div class="section-header">
        <h3 class="section-title">
          <n-icon :component="CartOutline" size="20" />
          购买会员卡
        </h3>
      </div>
      <n-spin :show="loadingCards">
        <n-grid :cols="gridCols" :x-gap="16" :y-gap="16" class="membership-cards">
          <n-grid-item v-for="card in membershipCards" :key="card.id">
            <n-card
              class="membership-card"
              :class="{ featured: card.isRecommend }"
              :bordered="false"
            >
              <div v-if="card.isRecommend" class="featured-badge">推荐</div>
              <div class="card-icon" v-html="getCardIcon(card.typeCode)"></div>
              <div class="card-type">{{ card.name }}</div>
              <div class="card-duration">{{ card.durationDays }}天</div>
              <div class="card-price">
                ¥{{ card.price }}<span>/{{ card.durationDays > 30 ? (Math.floor(card.durationDays/30) + '月') : card.durationDays + '天' }}</span>
              </div>
              <ul class="card-features">
                <li v-for="(content, idx) in getCardFeatures(card)" :key="idx">{{ content.title }}：{{ content.description }}</li>
              </ul>
              <n-button
                :type="card.isRecommend ? 'primary' : 'default'"
                :class="card.isRecommend ? 'btn-primary' : 'btn-secondary'"
                size="large"
                block
                @click="handleBuy(card)"
              >
                立即购买
              </n-button>
            </n-card>
          </n-grid-item>
        </n-grid>
      </n-spin>
    </div>

    <!-- 购买记录 -->
    <div class="card-section">
      <div class="section-header">
        <h3 class="section-title">
          <n-icon :component="TimeOutline" size="20" />
          购买记录
        </h3>
      </div>
      <n-data-table
        :columns="columns"
        :data="orderData"
        :pagination="pagination"
        :bordered="false"
        class="order-table"
        :loading="loadingOrders"
      />
    </div>

    <!-- 购买弹窗 -->
    <n-modal
      v-model:show="showBuyModal"
      preset="card"
      style="width: 90%; max-width: 500px"
      :show-header="false"
      class="buy-modal"
    >
      <div class="buy-modal-content">
        <div class="modal-card-icon" v-html="selectedCard ? getCardIcon(selectedCard.typeCode) : ''"></div>
        <h3>{{ selectedCard?.name }}</h3>
        <p class="duration">{{ selectedCard?.durationDays }}天有效期</p>
        <p class="price">¥{{ selectedCard?.price }}</p>
        <n-divider />
        <div class="payment-methods">
          <p class="label">选择支付方式:</p>
          <n-radio-group v-model:value="paymentMethod">
            <n-space vertical size="large">
              <n-radio value="ALIPAY" class="payment-radio">
                <span class="payment-option">
                  <span class="payment-icon alipay">
                    <n-icon :component="LogoAlipay" />
                  </span>
                  <span class="payment-text">
                    <span class="payment-name">支付宝</span>
                    <span class="payment-desc">快捷支付</span>
                  </span>
                </span>
              </n-radio>
              <n-radio value="BALANCE" class="payment-radio">
                <span class="payment-option">
                  <span class="payment-icon balance">
                    <n-icon :component="WalletOutline" />
                  </span>
                  <span class="payment-text">
                    <span class="payment-name">余额支付</span>
                    <span class="payment-desc">使用账户余额</span>
                  </span>
                </span>
              </n-radio>
            </n-space>
          </n-radio-group>
        </div>
      </div>
      <template #footer>
        <n-space justify="end" size="medium">
          <n-button size="large" @click="showBuyModal = false">取消</n-button>
          <n-button
            type="primary"
            size="large"
            :loading="payLoading"
            @click="confirmPay"
            class="pay-btn"
          >
            确认支付 ¥{{ selectedCard?.price }}
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, h, computed, onMounted } from 'vue'
import { useMessage, NTag, NIcon } from 'naive-ui'
import {
  CardOutline,
  CartOutline,
  TimeOutline,
  RefreshOutline,
  SyncCircleOutline,
  LogoAlipay,
  WalletOutline
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

// 响应式列数
const gridCols = computed(() => {
  const width = window.innerWidth
  if (width < 640) return 1
  if (width < 1024) return 2
  return 4
})

// 会员卡图标
const cardIcons = {
  TRIAL: `<svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>`,
  MONTHLY: `<svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/></svg>`,
  QUARTERLY: `<svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>`,
  YEARLY: `<svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>`
}

function getCardIcon(typeCode) {
  return cardIcons[typeCode] || cardIcons.MONTHLY
}

function getCardFeatures(card) {
  return (card.contents || []).filter(c => c.contentType === 'BENEFIT').slice(0, 4)
}

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
    // request.js 拦截器返回 res.data 直接，data 是数组
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
    // request.js 拦截器返回 res.data 直接
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
    // request.js 拦截器返回 res.data 直接
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
  window.addEventListener('resize', () => {})
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
  showBuyModal.value = true
}

function handleRenew() {
  const recommendCard = membershipCards.value.find(c => c.isRecommend)
  if (recommendCard) {
    selectedCard.value = recommendCard
    showBuyModal.value = true
  } else {
    message.warning('暂无可购买的会员卡')
  }
}

async function confirmPay() {
  payLoading.value = true
  try {
    // 创建订单
    const orderData = await createMembershipOrder({
      cardId: selectedCard.value.id,
      remark: `购买${selectedCard.value.name}`
    })

    // request.js 拦截器返回 res.data 直接
    if (!orderData || !orderData.orderNo) {
      message.error('创建订单失败')
      return
    }

    // 支付订单
    const payData = await payMembershipOrder({
      orderNo: orderData.orderNo,
      payMethod: paymentMethod.value
    })

    if (!payData) {
      message.error('支付请求失败')
      return
    }

    // 支付宝支付，提交表单
    if (paymentMethod.value === 'ALIPAY' && payData.payForm) {
      submitAlipayForm(payData.payForm)
    } else if (paymentMethod.value === 'BALANCE') {
      message.success(`成功使用余额购买 ${selectedCard.value.name}！`)
      showBuyModal.value = false
      // 刷新数据
      loadMyMembership()
      loadMyOrders()
    }

  } catch (error) {
    console.error('支付失败:', error)
    message.error(error.response?.data?.message || error.message || '支付失败，请重试')
  } finally {
    payLoading.value = false
  }
}
</script>

<style scoped>
.cards-page {
  max-width: 1400px;
  margin: 0 auto;
}

/* 我的会员卡区域 */
.my-card-section {
  background: linear-gradient(135deg, #1A1A2E 0%, #2D3748 100%);
  border-radius: 20px;
  padding: 24px;
  margin-bottom: 32px;
  box-shadow: 0 8px 32px rgba(26, 26, 46, 0.3);
}

.my-card-section .section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.my-card-section .section-title {
  font-size: 18px;
  font-weight: 600;
  color: white;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.my-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.my-card.empty-state {
  opacity: 0.6;
}

.my-card-info {
  color: white;
}

.card-badge {
  display: inline-block;
  background: linear-gradient(135deg, #FF6B35, #FF8C61);
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 12px;
}

.my-card-info h3 {
  font-size: 28px;
  margin: 0 0 8px;
  font-weight: 700;
  letter-spacing: 1px;
}

.my-card-info p {
  color: rgba(255,255,255,0.7);
  font-size: 14px;
  margin: 0;
}

.my-card-status {
  text-align: right;
}

.status-badge {
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 600;
}

.auto-renew {
  color: rgba(255,255,255,0.7);
  font-size: 13px;
  margin: 12px 0 0;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
}

.renew-btn {
  border-radius: 10px;
  font-weight: 600;
  background: linear-gradient(135deg, #FF6B35, #E55A2B);
  border: none;
}

/* 购买会员卡区域 */
.membership-section {
  margin-bottom: 32px;
}

.membership-section .section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 0 4px;
}

.membership-section .section-title {
  font-size: 20px;
  font-weight: 700;
  color: #1A1A2E;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.membership-cards {
  margin-bottom: 32px;
  min-height: 200px;
}

.membership-card {
  text-align: center;
  padding: 24px 20px;
  border-radius: 20px;
  background: white;
  box-shadow: 0 4px 20px rgba(0,0,0,0.06);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.membership-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0,0,0,0.12);
}

.membership-card.featured {
  background: linear-gradient(135deg, white 0%, #FFF5F2 100%);
  border: 2px solid #FF6B35;
}

.featured-badge {
  position: absolute;
  top: 16px;
  right: -28px;
  background: linear-gradient(135deg, #FF6B35, #E55A2B);
  color: white;
  padding: 4px 32px;
  font-size: 12px;
  font-weight: 600;
  transform: rotate(45deg);
}

.card-icon {
  font-size: 40px;
  margin-bottom: 12px;
}

.card-type {
  font-size: 20px;
  font-weight: 700;
  color: #1A1A2E;
  margin-bottom: 4px;
}

.card-duration {
  font-size: 13px;
  color: #6B7280;
  margin-bottom: 8px;
}

.card-price {
  font-size: 36px;
  font-weight: 700;
  color: #FF6B35;
  margin: 12px 0;
}

.card-price span {
  font-size: 14px;
  color: #6B7280;
  font-weight: 400;
}

.card-features {
  list-style: none;
  padding: 0;
  margin: 16px 0 20px;
  text-align: left;
  flex: 1;
}

.card-features li {
  padding: 6px 0;
  color: #6B7280;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-features li::before {
  content: '✓';
  color: #06D6A0;
  font-weight: 700;
  font-size: 12px;
}

.btn-primary,
.btn-secondary {
  width: 100%;
  border-radius: 12px;
  font-weight: 600;
  font-size: 15px;
  height: 46px;
}

.btn-primary {
  background: linear-gradient(135deg, #FF6B35, #E55A2B);
  border: none;
  box-shadow: 0 4px 15px rgba(255, 107, 53, 0.4);
}

.btn-primary:hover {
  background: linear-gradient(135deg, #FF8C61, #FF6B35);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 107, 53, 0.5);
}

.btn-secondary {
  background: #F0F2F5;
  color: #1A1A2E;
  border: none;
}

.btn-secondary:hover {
  background: #E5E7EB;
}

/* 购买记录区域 */
.card-section {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.06);
  transition: transform 0.3s, box-shadow 0.3s;
}

.card-section:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(0,0,0,0.1);
}

.card-section .section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-section .section-title {
  font-size: 18px;
  font-weight: 600;
  color: #1A1A2E;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.order-table :deep(.n-data-table-th) {
  font-weight: 600;
  color: #6B7280;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding: 14px 16px;
}

.order-table :deep(.n-data-table-td) {
  padding: 14px 16px;
  font-size: 14px;
}

/* 购买弹窗 */
.buy-modal :deep(.n-card) {
  border-radius: 20px;
}

.buy-modal-content {
  text-align: center;
  padding: 8px;
}

.modal-card-icon {
  font-size: 48px;
  margin-bottom: 12px;
  color: #FF6B35;
  display: flex;
  justify-content: center;
  align-items: center;
}

.buy-modal-content h3 {
  font-size: 22px;
  margin: 0 0 4px;
  font-weight: 700;
  color: #1A1A2E;
}

.buy-modal-content .duration {
  color: #6B7280;
  font-size: 14px;
  margin: 0 0 12px;
}

.buy-modal-content .price {
  font-size: 42px;
  font-weight: 700;
  color: #FF6B35;
  margin: 0;
}

.payment-methods {
  text-align: left;
}

.payment-methods .label {
  font-weight: 600;
  margin-bottom: 16px;
  color: #1A1A2E;
  font-size: 15px;
}

.payment-radio {
  width: 100%;
}

.payment-radio :deep(.n-radio__label) {
  width: 100%;
}

.payment-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}

.payment-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.payment-icon.alipay {
  background: #E3F2FD;
  color: #1677FF;
}

.payment-icon.balance {
  background: #FFF7E6;
  color: #FA8C16;
}

.payment-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.payment-name {
  font-size: 15px;
  font-weight: 600;
  color: #1A1A2E;
}

.payment-desc {
  font-size: 12px;
  color: #6B7280;
}

.pay-btn {
  background: linear-gradient(135deg, #FF6B35, #E55A2B);
  border: none;
  border-radius: 12px;
  font-weight: 600;
}

/* 响应式适配 */
@media (max-width: 1024px) {
  .my-card-section,
  .card-section {
    padding: 20px;
  }

  .membership-card {
    padding: 20px 16px;
  }

  .card-icon {
    font-size: 36px;
  }

  .card-price {
    font-size: 32px;
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
    font-size: 24px;
  }

  .membership-card {
    padding: 24px 20px;
  }

  .featured-badge {
    padding: 3px 28px;
    font-size: 11px;
  }
}

@media (max-width: 480px) {
  .my-card-section,
  .card-section {
    padding: 16px;
    border-radius: 16px;
  }

  .my-card-info h3 {
    font-size: 20px;
  }

  .section-title {
    font-size: 16px !important;
  }

  .card-price {
    font-size: 28px;
  }

  .buy-modal-content .price {
    font-size: 36px;
  }
}
</style>
