<template>
  <div class="store-page">
    <!-- 分类标签 -->
    <div class="tabs" :class="{ 'tabs-mobile': isMobile }">
      <button 
        v-for="tab in tabs" 
        :key="tab.key"
        :class="['tab', activeTab === tab.key ? 'active' : '']"
        @click="activeTab = tab.key"
      >
        {{ tab.label }}
      </button>
    </div>

    <!-- 商品网格 - 移动端使用不规则瀑布流布局 -->
    <div v-if="isMobile" class="mobile-product-masonry">
      <div 
        v-for="(product, index) in filteredProducts" 
        :key="product.id"
        class="masonry-item"
        :class="{ 'item-large': index % 3 === 0, 'item-small': index % 3 !== 0 }"
        @click="openProductDetail(product)"
      >
        <div class="masonry-card" :style="{ background: product.gradient }">
          <div class="masonry-image-wrapper">
            <img 
              v-if="product.imageUrl" 
              :src="product.imageUrl" 
              :alt="product.name"
              class="masonry-img"
              @error="handleImageError($event, product)"
            />
            <div v-else class="masonry-icon" v-html="getCategoryIcon(product.category)"></div>
          </div>
          <div class="masonry-content">
            <h4 class="masonry-title">{{ product.name }}</h4>
            <p class="masonry-desc">{{ product.description }}</p>
            <div class="masonry-price">
              <span class="masonry-final">¥{{ calculateFinalPrice(product) }}</span>
              <span v-if="product.originalPrice > calculateFinalPrice(product)" class="masonry-original">
                ¥{{ product.originalPrice }}
              </span>
            </div>
            <n-button 
              type="primary" 
              size="small"
              class="masonry-btn"
              :disabled="product.stock <= 0"
              @click.stop="openOrderModal(product)"
            >
              {{ product.stock > 0 ? '购买' : '缺货' }}
            </n-button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 桌面端商品网格 -->
    <n-grid v-else :cols="gridCols" :x-gap="20" :y-gap="20" class="product-grid">
      <n-grid-item v-for="product in filteredProducts" :key="product.id">
        <div class="product-card" @click="openProductDetail(product)">
          <div class="product-image" :style="{ background: product.gradient }">
            <img 
              v-if="product.imageUrl" 
              :src="product.imageUrl" 
              :alt="product.name"
              class="product-img"
              @error="handleImageError($event, product)"
            />
            <div v-else class="product-icon-default" v-html="getCategoryIcon(product.category)"></div>
          </div>
          <div class="product-info">
            <div class="product-name">{{ product.name }}</div>
            <div class="product-desc">{{ product.description }}</div>
            
            <!-- 价格展示区域 -->
            <div class="price-section">
              <div class="original-price">¥{{ product.originalPrice }}</div>
              <div class="discount-info" v-if="product.pointsDiscountValue > 0">
                <span class="discount-tag">积分抵扣</span>
                <span class="discount-amount">最高可抵¥{{ product.maxPointsDiscount }}</span>
              </div>
              <div class="final-price">
                <span class="price-label">到手价</span>
                <span class="price-value">¥{{ calculateFinalPrice(product) }}</span>
                <span class="points-needed" v-if="product.maxPointsDiscount > 0">
                  或 {{ product.maxPointsDiscount * 100 }} 积分
                </span>
              </div>
            </div>
            
            <n-button 
              type="primary" 
              size="small"
              class="buy-btn"
              :disabled="product.stock <= 0"
              @click.stop="openOrderModal(product)"
            >
              {{ product.stock > 0 ? '立即购买' : '暂时缺货' }}
            </n-button>
          </div>
        </div>
      </n-grid-item>
    </n-grid>

    <!-- 订单记录 -->
    <div class="card-section" style="margin-top: 32px;">
      <div class="section-header">
        <h3 class="section-title">我的订单</h3>
      </div>
      
      <!-- 桌面端表格 -->
      <div v-if="!isMobile" class="table-wrapper">
        <n-data-table
          :columns="orderColumns"
          :data="orderRecords"
          :pagination="{ pageSize: 5 }"
          :bordered="false"
          class="record-table"
        />
      </div>
      
      <!-- 移动端订单卡片列表 -->
      <div v-else class="mobile-order-list">
        <div v-if="orderRecords.length === 0" class="empty-orders">
          <n-empty description="暂无订单记录" />
        </div>
        <div v-else class="order-cards">
          <div 
            v-for="(order, index) in orderRecords" 
            :key="order.orderNo"
            class="order-card"
            :style="{ animationDelay: `${index * 80}ms` }"
          >
            <div class="order-card-header">
              <div class="order-meta">
                <span class="order-no">{{ order.orderNo }}</span>
                <span class="order-time">{{ formatOrderTime(order.createdAt) }}</span>
              </div>
              <n-tag :type="getOrderStatusType(order.status)" size="small" round class="order-status">
                {{ getOrderStatusText(order.status) }}
              </n-tag>
            </div>
            <div class="order-card-body">
              <div class="product-info-row">
                <div class="product-icon-mini" :style="{ background: order.productGradient || '#f3f4f6' }">
                  <span v-if="!order.productImage">{{ order.productName?.[0] || '商' }}</span>
                  <img v-else :src="order.productImage" class="mini-img" />
                </div>
                <div class="product-detail">
                  <h4 class="product-name-text">{{ order.productName }}</h4>
                  <div class="price-row">
                    <span class="final-price">¥{{ order.payAmount }}</span>
                    <span v-if="order.pointsDiscount > 0" class="discount-tag-mini">
                      省¥{{ order.pointsDiscount }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
            <div class="order-card-footer" v-if="order.status === 'PENDING'">
              <n-button type="primary" size="small" @click="handlePay(order)">
                去支付
              </n-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 购买确认弹窗 -->
    <n-modal v-model:show="showOrderModal" preset="card" style="width: 500px" :show-header="false">
      <div class="order-content">
        <div class="order-header">
          <svg class="cart-icon" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="9" cy="21" r="1"/>
            <circle cx="20" cy="21" r="1"/>
            <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>
          </svg>
          <span>确认购买</span>
        </div>
        
        <div v-if="selectedProduct" class="order-product">
          <div class="product-image-small" :style="{ background: selectedProduct.gradient }">
            <img
              v-if="selectedProduct.imageUrl"
              :src="selectedProduct.imageUrl"
              :alt="selectedProduct.name"
              class="product-img-small"
              @error="handleImageError($event, selectedProduct)"
            />
            <div v-else class="product-icon-small" v-html="selectedProduct.icon || getCategoryIcon(selectedProduct.category)"></div>
          </div>
          <div class="product-info-small">
            <h4>{{ selectedProduct.name }}</h4>
            <p>{{ selectedProduct.description }}</p>
          </div>
        </div>
        
        <n-divider />
        
        <!-- 数量选择 -->
        <div class="quantity-section">
          <span>购买数量</span>
          <n-input-number v-model:value="orderQuantity" :min="1" :max="selectedProduct?.stock || 1" />
        </div>
        
        <n-divider />
        
        <!-- 价格计算明细 -->
        <div class="price-detail">
          <div class="detail-row">
            <span>商品原价</span>
            <span class="original">¥{{ (selectedProduct?.originalPrice * orderQuantity || 0).toFixed(2) }}</span>
          </div>
          
          <!-- 积分抵扣滑块 -->
          <div class="points-section" v-if="userPoints > 0 && selectedProduct?.pointsDiscountValue > 0 && selectedProduct?.pointsDiscountType !== 'NONE'">
            <div class="points-header">
              <span>使用积分抵扣</span>
              <span class="points-available">可用积分: {{ userPoints }}</span>
            </div>
            <n-slider 
              v-model:value="usePoints" 
              :max="maxUsablePoints" 
              :step="100"
              :disabled="!canUsePoints"
            />
            <div class="points-input">
              <span>使用 {{ usePoints }} 积分</span>
              <span class="discount-amount">-¥{{ calculatedPrice.pointsDiscount }}</span>
            </div>
            <n-alert v-if="!calculatedPrice.pointsSufficient" type="warning" :show-icon="false">
              积分不足，当前可用积分: {{ userPoints }}
            </n-alert>
          </div>
          
          <div class="detail-row total">
            <span>最终支付</span>
            <span class="final">¥{{ calculatedPrice.finalPrice }}</span>
          </div>
        </div>
        
        <!-- 支付方式 -->
        <div class="pay-method-section">
          <span>支付方式</span>
          <n-radio-group v-model:value="payMethod">
            <n-radio-button value="WECHAT">微信支付</n-radio-button>
            <n-radio-button value="ALIPAY">支付宝</n-radio-button>
          </n-radio-group>
        </div>
      </div>
      
      <template #footer>
        <n-space justify="end">
          <n-button @click="showOrderModal = false">取消</n-button>
          <n-button 
            type="primary" 
            :loading="submitting"
            :disabled="!calculatedPrice.pointsSufficient || selectedProduct?.stock <= 0"
            @click="confirmOrder"
          >
            确认支付 ¥{{ calculatedPrice.finalPrice }}
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, computed, h, watch, onMounted, onUnmounted } from 'vue'
import { useMessage, NTag, NButton } from 'naive-ui'
import { getProducts, calculatePrice, createOrder, getOrders, payOrder } from '@/api/product'
import { submitAlipayForm } from '@/api/membership'
import {
  clearPaymentMarker,
  isPaymentFinished,
  markPaymentStarted,
  readPaymentMarker
} from '@/utils/paymentMarker'

const message = useMessage()

// 响应式状态
const windowWidth = ref(window.innerWidth)
const isMobile = computed(() => windowWidth.value < 768)
const isTablet = computed(() => windowWidth.value >= 768 && windowWidth.value < 1024)

// 计算网格列数
const gridCols = computed(() => {
  if (windowWidth.value < 480) return 1
  if (windowWidth.value < 768) return 2
  if (windowWidth.value < 1024) return 3
  return 4
})

// 监听窗口大小变化
function handleResize() {
  windowWidth.value = window.innerWidth
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

const activeTab = ref('all')
const userPoints = ref(0)
const showOrderModal = ref(false)
const submitting = ref(false)
const selectedProduct = ref(null)
const orderQuantity = ref(1)
const usePoints = ref(0)
const payMethod = ref('ALIPAY')
const products = ref([])
const orderRecords = ref([])
const loading = ref(false)

// 计算价格
const calculatedPrice = ref({
  originalTotalPrice: 0,
  pointsDiscount: 0,
  finalPrice: 0,
  pointsSufficient: true
})

// 最大可用积分
const maxUsablePoints = computed(() => {
  if (!selectedProduct.value) return 0
  const maxDiscount = selectedProduct.value.maxPointsDiscount || 0
  return Math.min(userPoints.value, maxDiscount * 100)
})

// 是否可以使用积分
const canUsePoints = computed(() => {
  return userPoints.value > 0 && 
         selectedProduct.value?.pointsDiscountValue > 0 &&
         selectedProduct.value?.pointsDiscountType !== 'NONE'
})

// 监听积分使用变化
watch([() => selectedProduct.value, orderQuantity, usePoints], async () => {
  if (selectedProduct.value) {
    await recalculatePrice()
  }
}, { immediate: true })

async function recalculatePrice() {
  if (!selectedProduct.value) return

  try {
    const res = await calculatePrice({
      productId: selectedProduct.value.id,
      quantity: orderQuantity.value,
      usePoints: usePoints.value
    })
    calculatedPrice.value = res
    userPoints.value = res.userAvailablePoints ?? userPoints.value
  } catch (error) {
    // 如果API失败，本地计算
    const originalTotal = (selectedProduct.value.originalPrice || 0) * orderQuantity.value
    const maxDiscount = selectedProduct.value.maxPointsDiscount || 0
    const pointsValue = usePoints.value * 0.01
    const discount = Math.min(pointsValue, maxDiscount, originalTotal)
    
    calculatedPrice.value = {
      originalTotalPrice: originalTotal,
      pointsDiscount: discount.toFixed(2),
      finalPrice: (originalTotal - discount).toFixed(2),
      pointsSufficient: userPoints.value >= usePoints.value
    }
  }
}

// 计算最终价格
function calculateFinalPrice(product) {
  if (!product) return 0
  const originalPrice = product.originalPrice || 0
  const maxDiscount = product.maxPointsDiscount || 0
  return (originalPrice - maxDiscount).toFixed(2)
}

const tabs = [
  { key: 'all', label: '全部' },
  { key: 'EQUIPMENT', label: '运动装备' },
  { key: 'SUPPLEMENT', label: '营养补剂' },
  { key: 'COURSE', label: '课程优惠' },
  { key: 'OTHER', label: '其他' }
]

const filteredProducts = computed(() => {
  if (activeTab.value === 'all') {
    return products.value
  }
  return products.value.filter(p => p.category === activeTab.value)
})

const orderColumns = [
  { title: '订单号', key: 'orderNo', width: 150 },
  { title: '日期', key: 'createdAt', width: 120 },
  { title: '商品名称', key: 'productName' },
  { 
    title: '原价', 
    key: 'originalPrice',
    width: 100,
    render(row) {
      return h('span', { style: { textDecoration: 'line-through', color: '#999' } }, 
        `¥${((row.originalPrice || 0) * (row.quantity || 1)).toFixed(2)}`)
    }
  },
  { 
    title: '积分抵扣', 
    key: 'pointsDiscount',
    width: 100,
    render(row) {
      const discount = row.pointsDiscount || 0
      return h('span', { style: { color: '#FF6B35' } }, 
        discount > 0 ? `-¥${discount}` : '-')
    }
  },
  { 
    title: '实付金额', 
    key: 'payAmount',
    width: 100,
    render(row) {
      return h('span', { style: { color: '#FF6B35', fontWeight: 600 } }, `¥${row.payAmount || 0}`)
    }
  },
  { 
    title: '状态', 
    key: 'status',
    width: 100,
    render(row) {
      const statusMap = {
        'PENDING': { type: 'warning', text: '待支付' },
        'PAID': { type: 'processing', text: '已支付' },
        'NOT_PICKED': { type: 'warning', text: '待取货' },
        'SHIPPED': { type: 'success', text: '已发货' },
        'COMPLETED': { type: 'default', text: '已完成' },
        'CANCELLED': { type: 'error', text: '已取消' }
      }
      const status = statusMap[row.status] || { type: 'default', text: row.status }
      return h(NTag, { type: status.type, size: 'small' }, { default: () => status.text })
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 120,
    render(row) {
      if (row.status === 'PENDING') {
        return h(NButton, { 
          type: 'primary', 
          size: 'small',
          onClick: () => handlePay(row)
        }, { default: () => '去支付' })
      }
      return null
    }
  }
]

// 格式化订单时间
function formatOrderTime(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (days === 0) {
    return '今天'
  } else if (days === 1) {
    return '昨天'
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
  }
}

// 获取订单状态类型
function getOrderStatusType(status) {
  const statusMap = {
    'PENDING': 'warning',
    'PAID': 'processing',
    'NOT_PICKED': 'warning',
    'SHIPPED': 'success',
    'COMPLETED': 'default',
    'CANCELLED': 'error'
  }
  return statusMap[status] || 'default'
}

// 获取订单状态文本
function getOrderStatusText(status) {
  const statusMap = {
    'PENDING': '待支付',
    'PAID': '已支付',
    'NOT_PICKED': '待取货',
    'SHIPPED': '已发货',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

// 商品图标
function getProductIcon(type) {
  const icons = {
    bottle: `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M9 2v2"/><path d="M15 2v2"/><path d="M12 2v8"/><path d="M8 10h8v10a2 2 0 0 1-2 2h-4a2 2 0 0 1-2-2V10z"/></svg>`,
    protein: `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8h1a4 4 0 0 1 0 8h-1"/><path d="M2 8h16v9a4 4 0 0 1-4 4H6a4 4 0 0 1-4-4V8z"/><line x1="6" y1="2" x2="6" y2="4"/><line x1="10" y1="2" x2="10" y2="4"/><line x1="14" y1="2" x2="14" y2="4"/></svg>`,
    yoga: `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg>`,
    coach: `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>`,
    bag: `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/><path d="M16 10a4 4 0 0 1-8 0"/></svg>`,
    vitamin: `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M10.5 20.5l10-10a4.95 4.95 0 1 0-7-7l-10 10a4.95 4.95 0 1 0 7 7Z"/><path d="M8.5 8.5l7 7"/></svg>`,
    coupon: `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M2 9a3 3 0 0 1 0 6v2a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-2a3 3 0 0 1 0-6V7a2 2 0 0 0-2-2H4a2 2 0 0 0-2 2Z"/><path d="M9 12h6"/></svg>`,
    glove: `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M8 12a4 4 0 1 0 8 0V4a2 2 0 0 0-4 0v8"/><path d="M8 12v6a2 2 0 0 0 2 2h4a2 2 0 0 0 2-2v-6"/></svg>`
  }
  return icons[type] || icons.bag
}

function getIconType(category) {
  const map = {
    'EQUIPMENT': 'bottle',
    'SUPPLEMENT': 'protein',
    'COURSE': 'coach',
    'OTHER': 'bag'
  }
  return map[category] || 'bag'
}

function getGradient(category) {
  const gradients = {
    'EQUIPMENT': 'linear-gradient(135deg, #E3F2FD, #BBDEFB)',
    'SUPPLEMENT': 'linear-gradient(135deg, #FFE5B4, #FFDAB9)',
    'COURSE': 'linear-gradient(135deg, #FFF3E0, #FFE0B2)',
    'OTHER': 'linear-gradient(135deg, #F3E5F5, #E1BEE7)'
  }
  return gradients[category] || 'linear-gradient(135deg, #F5F5F5, #EEEEEE)'
}

// 根据分类获取默认图标
function getCategoryIcon(category) {
  const icons = {
    'EQUIPMENT': `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M6.5 6.5h11"/><path d="M6.5 17.5h11"/><path d="M6 20v-2a6 6 0 1 1 12 0v2"/></svg>`,
    'SUPPLEMENT': `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M10.5 20.5l10-10a4.95 4.95 0 1 0-7-7l-10 10a4.95 4.95 0 1 0 7 7Z"/><path d="M8.5 8.5l7 7"/></svg>`,
    'COURSE': `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg>`,
    'OTHER': `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M12 16v-4"/><path d="M12 8h.01"/></svg>`
  }
  return icons[category] || icons['OTHER']
}

// 处理图片加载失败
function handleImageError(event, product) {
  // 图片加载失败，移除图片元素，显示默认图标
  product.imageUrl = null
}

function openOrderModal(product) {
  selectedProduct.value = product
  orderQuantity.value = 1
  usePoints.value = 0
  showOrderModal.value = true
}

async function confirmOrder() {
  submitting.value = true
  try {
    const res = await createOrder({
      productId: selectedProduct.value.id,
      quantity: orderQuantity.value,
      usePoints: usePoints.value,
      address: '',
      pickupType: 'IN_STORE'
    })

    showOrderModal.value = false
    message.success('订单创建成功，正在跳转支付...')

    await handlePayOrder(res.orderNo)
    await loadData()
  } catch (error) {
    message.error(error.message || '订单创建失败')
  } finally {
    submitting.value = false
  }
}

async function handlePayOrder(orderNo) {
  try {
    message.info('开始支付，请在支付宝完成付款')
    const res = await payOrder(orderNo, payMethod.value)

    if (res.payForm) {
      markPaymentStarted({ orderNo, orderType: 'PRODUCT' })
      submitAlipayForm(res.payForm)
    }
  } catch (error) {
    message.error(error.message || '支付请求失败，请重试')
  }
}

async function handlePay(row) {
  await handlePayOrder(row.orderNo)
  await loadData()
}

function notifyPaymentCompletion(orders) {
  const marker = readPaymentMarker()
  if (!marker?.orderNo) return

  const paidOrder = orders.find(order =>
    order.orderNo === marker.orderNo && isPaymentFinished(order)
  )
  if (!paidOrder) return

  clearPaymentMarker()
  message.success('支付完成，订单状态已更新')
}

async function loadData() {
  loading.value = true
  try {
    // 从API获取商品数据
    const productData = await getProducts()
    
    // 处理API返回的数据，添加前端展示字段
    products.value = productData.map(p => ({
      ...p,
      icon: getProductIcon(getIconType(p.category)),
      gradient: getGradient(p.category)
    })) || []
    
    // 获取订单数据
    const orderData = await getOrders()
    orderRecords.value = orderData || []
    notifyPaymentCompletion(orderRecords.value)
  } catch (error) {
    console.error('数据加载失败:', error)
    message.error('数据加载失败，请检查网络连接')
    products.value = []
    orderRecords.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.store-page {
  max-width: 1400px;
  margin: 0 auto;
}

.tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  background: #F0F2F5;
  padding: 6px;
  border-radius: 12px;
  width: fit-content;
}

.tab {
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
  border: none;
  background: transparent;
  color: #6B7280;
}

.tab.active {
  background: white;
  color: #FF6B35;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}

.product-grid {
  margin-bottom: 32px;
}

.product-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0,0,0,0.06);
  transition: all 0.3s;
  cursor: pointer;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0,0,0,0.12);
}

.product-image {
  width: 100%;
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  overflow: hidden;
}

.product-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.product-card:hover .product-img {
  transform: scale(1.05);
}

.product-icon-default {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: rgba(0, 0, 0, 0.3);
}

.product-info {
  padding: 16px;
}

.product-name {
  font-weight: 600;
  margin-bottom: 4px;
  font-size: 15px;
  color: #1A1A2E;
}

.product-desc {
  color: #6B7280;
  font-size: 12px;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 价格区域 */
.price-section {
  margin: 12px 0;
  padding: 12px;
  background: linear-gradient(135deg, #FFF8F0 0%, #FFF0E5 100%);
  border-radius: 10px;
}

.original-price {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
  margin-bottom: 4px;
}

.discount-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.discount-tag {
  background: linear-gradient(135deg, #FF6B35, #FF8C61);
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.discount-amount {
  color: #FF6B35;
  font-weight: 600;
  font-size: 14px;
}

.final-price {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.price-label {
  font-size: 12px;
  color: #666;
}

.price-value {
  font-size: 22px;
  font-weight: 700;
  color: #FF6B35;
  font-family: 'Outfit', sans-serif;
}

.points-needed {
  font-size: 12px;
  color: #999;
}

/* 购买按钮 */
.buy-btn {
  width: 100%;
  border-radius: 10px;
  font-weight: 600;
  background: linear-gradient(135deg, #FF6B35, #FF8C61);
}

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

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #1A1A2E;
  margin: 0;
}

.record-table :deep(.n-data-table-th) {
  font-weight: 600;
  color: #6B7280;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding: 14px 16px;
}

.record-table :deep(.n-data-table-td) {
  padding: 14px 16px;
  font-size: 14px;
}

/* 订单弹窗样式 */
.order-content {
  padding: 8px;
}

.order-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
}

.order-product {
  display: flex;
  gap: 16px;
  align-items: center;
  margin-bottom: 16px;
}

.product-image-small {
  width: 80px;
  height: 80px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  flex-shrink: 0;
  overflow: hidden;
}

.product-img-small {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-icon-small {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: rgba(0, 0, 0, 0.3);
}

.product-info-small h4 {
  font-size: 18px;
  margin-bottom: 4px;
  font-weight: 600;
  color: #1A1A2E;
}

.product-info-small p {
  color: #6B7280;
  font-size: 14px;
  margin: 0;
}

.quantity-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
}

.points-section {
  background: #F8F9FA;
  padding: 16px;
  border-radius: 8px;
  margin: 12px 0;
}

.points-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.points-available {
  color: #FF6B35;
  font-weight: 500;
}

.points-input {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  font-size: 14px;
}

.pay-method-section {
  margin-top: 16px;
}

.price-detail {
  padding: 8px 0;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 14px;
}

.detail-row .original {
  color: #999;
  text-decoration: line-through;
}

.detail-row.total {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #ddd;
  font-size: 16px;
  font-weight: 600;
}

.detail-row.total .final {
  color: #FF6B35;
  font-size: 24px;
}

/* ==================== 移动端瀑布流布局 ==================== */
.mobile-product-masonry {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  padding: 0 4px;
}

.masonry-item {
  break-inside: avoid;
}

.masonry-item.item-large {
  grid-column: span 2;
}

.masonry-card {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  cursor: pointer;
  background: white;
}

.masonry-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.masonry-image-wrapper {
  width: 100%;
  height: 140px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.item-large .masonry-image-wrapper {
  height: 180px;
}

.masonry-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.masonry-icon {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(0, 0, 0, 0.3);
}

.masonry-icon svg {
  width: 48px;
  height: 48px;
}

.masonry-content {
  padding: 12px;
  background: white;
}

.masonry-title {
  margin: 0 0 6px 0;
  font-size: 14px;
  font-weight: 600;
  color: #1a1a2e;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-large .masonry-title {
  font-size: 16px;
}

.masonry-desc {
  margin: 0 0 10px 0;
  font-size: 12px;
  color: #6b7280;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-large .masonry-desc {
  font-size: 13px;
  -webkit-line-clamp: 3;
}

.masonry-price {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 10px;
}

.masonry-final {
  font-size: 18px;
  font-weight: 700;
  color: #FF6B35;
}

.item-large .masonry-final {
  font-size: 22px;
}

.masonry-original {
  font-size: 12px;
  color: #9ca3af;
  text-decoration: line-through;
}

.masonry-btn {
  width: 100%;
  border-radius: 8px;
  font-weight: 600;
  background: linear-gradient(135deg, #FF6B35, #FF8C61);
}

/* ==================== 响应式适配 ==================== */
@media (max-width: 1024px) {
  .store-page {
    padding: 0;
  }
  
  .tabs {
    margin-bottom: 20px;
  }
  
  .tab {
    padding: 8px 16px;
    font-size: 13px;
  }
  
  .product-image {
    height: 140px;
  }
  
  .card-section {
    padding: 20px;
    border-radius: 14px;
  }
  
  .section-title {
    font-size: 16px;
  }
}

@media (max-width: 768px) {
  .tabs {
    gap: 6px;
    padding: 4px;
    margin-bottom: 16px;
    width: 100%;
    overflow-x: auto;
    flex-wrap: nowrap;
    -webkit-overflow-scrolling: touch;
  }
  
  .tabs::-webkit-scrollbar {
    display: none;
  }
  
  .tab {
    padding: 8px 14px;
    font-size: 13px;
    white-space: nowrap;
    flex-shrink: 0;
  }
  
  .mobile-product-masonry {
    gap: 10px;
    padding: 0 2px;
  }
  
  .masonry-image-wrapper {
    height: 120px;
  }
  
  .item-large .masonry-image-wrapper {
    height: 150px;
  }
  
  .masonry-content {
    padding: 10px;
  }
  
  .masonry-title {
    font-size: 13px;
  }
  
  .item-large .masonry-title {
    font-size: 15px;
  }
  
  .masonry-desc {
    font-size: 11px;
    margin-bottom: 8px;
  }
  
  .masonry-final {
    font-size: 16px;
  }
  
  .item-large .masonry-final {
    font-size: 20px;
  }
  
  .card-section {
    padding: 16px;
    border-radius: 12px;
    margin-top: 24px !important;
  }
  
  .section-header {
    margin-bottom: 16px;
  }
  
  .section-title {
    font-size: 15px;
  }
  
  .table-responsive {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
    margin: 0 -16px;
    padding: 0 16px;
  }
  
  .record-table {
    min-width: 100%;
  }
  
  /* 弹窗移动端适配 */
  .order-content {
    padding: 4px;
  }
  
  .order-header {
    font-size: 16px;
    margin-bottom: 16px;
  }
  
  .order-product {
    gap: 12px;
  }
  
  .product-image-small {
    width: 64px;
    height: 64px;
    border-radius: 10px;
  }
  
  .product-info-small h4 {
    font-size: 15px;
  }
  
  .product-info-small p {
    font-size: 12px;
  }
  
  .quantity-section {
    padding: 10px 0;
  }
  
  .points-section {
    padding: 12px;
  }
  
  .detail-row {
    padding: 6px 0;
    font-size: 13px;
  }
  
  .detail-row.total {
    font-size: 14px;
  }
  
  .detail-row.total .final {
    font-size: 20px;
  }
}

@media (max-width: 480px) {
  .mobile-product-masonry {
    gap: 8px;
  }
  
  .masonry-image-wrapper {
    height: 100px;
  }
  
  .item-large .masonry-image-wrapper {
    height: 130px;
  }
  
  .masonry-content {
    padding: 8px;
  }
  
  .masonry-title {
    font-size: 12px;
  }
  
  .item-large .masonry-title {
    font-size: 14px;
  }
  
  .masonry-desc {
    font-size: 10px;
    -webkit-line-clamp: 1;
  }
  
  .item-large .masonry-desc {
    -webkit-line-clamp: 2;
  }
  
  .masonry-final {
    font-size: 14px;
  }
  
  .item-large .masonry-final {
    font-size: 18px;
  }
  
  .tab {
    padding: 6px 12px;
    font-size: 12px;
  }
  
  .card-section {
    padding: 14px;
  }
  
  .section-title {
    font-size: 14px;
  }
}

/* ==================== 移动端订单卡片样式 ==================== */
.mobile-order-list {
  padding: 8px 0;
}

.empty-orders {
  padding: 40px 0;
}

.order-cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-card {
  background: white;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  border: 1px solid #f3f4f6;
  animation: slideInUp 0.4s ease forwards;
  opacity: 0;
  transition: all 0.3s ease;
}

.order-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.order-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px dashed #e5e7eb;
}

.order-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.order-no {
  font-size: 13px;
  font-weight: 600;
  color: #1a1a2e;
  font-family: monospace;
}

.order-time {
  font-size: 12px;
  color: #9ca3af;
}

.order-status {
  flex-shrink: 0;
}

.order-card-body {
  margin-bottom: 12px;
}

.product-info-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-icon-mini {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 600;
  color: #6b7280;
  flex-shrink: 0;
  overflow: hidden;
}

.product-icon-mini .mini-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-detail {
  flex: 1;
  min-width: 0;
}

.product-name-text {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 6px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.price-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.final-price {
  font-size: 18px;
  font-weight: 700;
  color: #FF6B35;
}

.discount-tag-mini {
  font-size: 11px;
  padding: 2px 8px;
  background: #fee2e2;
  color: #ef4444;
  border-radius: 10px;
}

.order-card-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 12px;
  border-top: 1px solid #f3f4f6;
}

/* 响应式调整 */
@media (max-width: 480px) {
  .order-card {
    padding: 14px;
    border-radius: 14px;
  }
  
  .product-icon-mini {
    width: 44px;
    height: 44px;
    border-radius: 10px;
    font-size: 16px;
  }
  
  .product-name-text {
    font-size: 13px;
  }
  
  .final-price {
    font-size: 16px;
  }
  
  .order-no {
    font-size: 12px;
  }
}
</style>
