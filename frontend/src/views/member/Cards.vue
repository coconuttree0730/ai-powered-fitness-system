<template>
  <div class="cards-page">
    <!-- 我的会员卡 - 移到顶部 -->
    <div class="my-card-section">
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
          <div class="card-badge">季卡会员</div>
          <h3>VIP 会员卡</h3>
          <p>有效期至: 2024年12月31日 | 剩余: 85天</p>
        </div>
        <div class="my-card-status">
          <n-tag type="success" size="large" round class="status-badge">有效</n-tag>
          <p class="auto-renew">
            <n-icon :component="SyncCircleOutline" size="14" />
            自动续费已开启
          </p>
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
        <n-tag type="info" size="small" round>动态加载</n-tag>
      </div>
      <n-grid :cols="gridCols" :x-gap="16" :y-gap="16" class="membership-cards">
        <n-grid-item v-for="card in membershipCards" :key="card.id">
          <n-card 
            class="membership-card" 
            :class="{ featured: card.featured }"
            :bordered="false"
          >
            <div v-if="card.featured" class="featured-badge">推荐</div>
            <div class="card-icon" v-html="card.icon"></div>
            <div class="card-type">{{ card.name }}</div>
            <div class="card-duration">{{ card.duration }}</div>
            <div class="card-price">
              ¥{{ card.price }}<span>/{{ card.unit }}</span>
            </div>
            <ul class="card-features">
              <li v-for="(feature, idx) in card.features" :key="idx">{{ feature }}</li>
            </ul>
            <n-button 
              :type="card.featured ? 'primary' : 'default'"
              :class="card.featured ? 'btn-primary' : 'btn-secondary'"
              size="large" 
              block
              @click="handleBuy(card)"
            >
              立即购买
            </n-button>
          </n-card>
        </n-grid-item>
      </n-grid>
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
        <div class="modal-card-icon">{{ selectedCard?.icon }}</div>
        <h3>{{ selectedCard?.name }}</h3>
        <p class="duration">{{ selectedCard?.duration }}</p>
        <p class="price">¥{{ selectedCard?.price }}</p>
        <n-divider />
        <div class="payment-methods">
          <p class="label">选择支付方式:</p>
          <n-radio-group v-model:value="paymentMethod">
            <n-space vertical size="large">
              <n-radio value="wechat" class="payment-radio">
                <span class="payment-option">
                  <span class="payment-icon wechat">
                    <n-icon :component="LogoWechat" />
                  </span>
                  <span class="payment-text">
                    <span class="payment-name">微信支付</span>
                    <span class="payment-desc">推荐使用</span>
                  </span>
                </span>
              </n-radio>
              <n-radio value="alipay" class="payment-radio">
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
  LogoWechat,
  LogoAlipay
} from '@vicons/ionicons5'

const message = useMessage()

// 响应式列数
const gridCols = computed(() => {
  const width = window.innerWidth
  if (width < 640) return 1
  if (width < 1024) return 2
  return 4  // 现在有4种卡，用4列
})

// 会员卡图标
const cardIcons = {
  week: `<svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>`,
  month: `<svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/></svg>`,
  quarter: `<svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>`,
  year: `<svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>`
}

// 会员卡数据 - 支持动态添加，可从API获取
const membershipCards = ref([
  {
    id: 'week',
    name: '周卡',
    icon: cardIcons.week,
    duration: '7天',
    price: 69,
    unit: '周',
    featured: false,
    features: [
      '无限次健身房使用',
      '免费储物柜',
      '基础体测服务',
      '新用户体验首选'
    ]
  },
  {
    id: 'month',
    name: '月卡',
    icon: cardIcons.month,
    duration: '30天',
    price: 199,
    unit: '月',
    featured: false,
    features: [
      '无限次健身房使用',
      '免费储物柜',
      '基础体测服务',
      '团课免费参与'
    ]
  },
  {
    id: 'quarter',
    name: '季卡',
    icon: cardIcons.quarter,
    duration: '90天',
    price: 499,
    unit: '季',
    featured: true,
    features: [
      '月卡全部权益',
      '1节私教体验课',
      '营养咨询1次',
      '优先预约热门课程',
      '赠送200积分'
    ]
  },
  {
    id: 'year',
    name: '年卡',
    icon: cardIcons.year,
    duration: '365天',
    price: 1599,
    unit: '年',
    featured: false,
    features: [
      '季卡全部权益',
      '3节私教体验课',
      '专属健身计划',
      'VIP专属区域',
      '赠送800积分'
    ]
  }
])

// 模拟从API加载更多卡类型
async function loadCardsFromAPI() {
  // 实际项目中这里调用后端API
  // const res = await fetch('/api/membership-cards')
  // membershipCards.value = await res.json()
}

onMounted(() => {
  loadCardsFromAPI()
  window.addEventListener('resize', () => {
    // 触发响应式更新
  })
})

const showBuyModal = ref(false)
const payLoading = ref(false)
const paymentMethod = ref('wechat')
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
        'expired': { type: 'default', text: '已过期' }
      }
      const status = statusMap[row.status]
      return h(NTag, { type: status.type, size: 'small', round: true }, { default: () => status.text })
    }
  }
]

const pagination = {
  pageSize: 5,
  showSizePicker: true,
  pageSizes: [5, 10, 20]
}

const orderData = [
  { orderNo: 'ORD20241015001', cardType: '季卡', amount: 499, createTime: '2024-10-15', status: 'completed' },
  { orderNo: 'ORD20240701002', cardType: '月卡', amount: 199, createTime: '2024-07-01', status: 'expired' },
  { orderNo: 'ORD20240315003', cardType: '周卡', amount: 69, createTime: '2024-03-15', status: 'expired' }
]

function handleBuy(card) {
  selectedCard.value = card
  showBuyModal.value = true
}

function handleRenew() {
  const quarterCard = membershipCards.value.find(c => c.id === 'quarter')
  selectedCard.value = quarterCard
  showBuyModal.value = true
}

function confirmPay() {
  payLoading.value = true
  setTimeout(() => {
    payLoading.value = false
    showBuyModal.value = false
    message.success(`成功购买 ${selectedCard.value.name}！`)
    // 实际项目中这里调用支付API
  }, 1500)
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

.payment-icon.wechat {
  background: #E8F5E9;
  color: #07C160;
}

.payment-icon.alipay {
  background: #E3F2FD;
  color: #1677FF;
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
