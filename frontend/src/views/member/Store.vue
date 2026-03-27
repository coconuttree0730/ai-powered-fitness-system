<template>
  <div class="store-page">
    <!-- 分类标签 -->
    <div class="tabs">
      <button 
        v-for="tab in tabs" 
        :key="tab.key"
        :class="['tab', activeTab === tab.key ? 'active' : '']"
        @click="activeTab = tab.key"
      >
        {{ tab.label }}
      </button>
    </div>

    <!-- 商品网格 -->
    <n-grid :cols="4" :x-gap="20" :y-gap="20" class="product-grid">
      <n-grid-item v-for="product in filteredProducts" :key="product.id">
        <div class="product-card">
          <div class="product-image" :style="{ background: product.gradient }" v-html="product.icon">
          </div>
          <div class="product-info">
            <div class="product-name">{{ product.name }}</div>
            <div class="product-points">{{ product.points }} 积分</div>
            <n-button 
              type="primary" 
              size="small" 
              class="exchange-btn"
              :disabled="userPoints < product.points"
              @click="openExchangeModal(product)"
            >
              立即兑换
            </n-button>
          </div>
        </div>
      </n-grid-item>
    </n-grid>

    <!-- 兑换记录 -->
    <div class="card-section" style="margin-top: 32px;">
      <div class="section-header">
        <h3 class="section-title">兑换记录</h3>
      </div>
      <n-data-table
        :columns="recordColumns"
        :data="exchangeRecords"
        :pagination="{ pageSize: 5 }"
        :bordered="false"
        class="record-table"
      />
    </div>

    <!-- 兑换确认弹窗 -->
    <n-modal v-model:show="showExchangeModal" preset="card" style="width: 450px" :show-header="false">
      <div class="exchange-content">
        <div class="exchange-header">
          <svg class="gift-icon" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 12 20 22 4 22 4 12"/><rect x="2" y="7" width="20" height="5"/><line x1="12" y1="22" x2="12" y2="7"/><path d="M12 7H7.5a2.5 2.5 0 0 1 0-5C11 2 12 7 12 7z"/><path d="M12 7h4.5a2.5 2.5 0 0 0 0-5C13 2 12 7 12 7z"/></svg>
          <span>确认兑换</span>
        </div>
        <div v-if="selectedProduct" class="exchange-product">
          <div class="exchange-icon" :style="{ background: selectedProduct.gradient }" v-html="selectedProduct.icon">
          </div>
          <div class="exchange-info">
            <h4>{{ selectedProduct.name }}</h4>
            <p>{{ selectedProduct.description }}</p>
          </div>
        </div>
        <n-divider />
        <div class="exchange-detail">
          <div class="detail-row">
            <span>所需积分</span>
            <span class="points">{{ selectedProduct?.points }}</span>
          </div>
          <div class="detail-row">
            <span>当前积分</span>
            <span>{{ userPoints }}</span>
          </div>
          <div class="detail-row">
            <span>兑换后剩余</span>
            <span :class="{ 'insufficient': userPoints - (selectedProduct?.points || 0) < 0 }">
              {{ userPoints - (selectedProduct?.points || 0) }}
            </span>
          </div>
        </div>
      </div>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showExchangeModal = false">取消</n-button>
          <n-button 
            type="primary" 
            :loading="exchanging"
            :disabled="userPoints < selectedProduct?.points"
            @click="confirmExchange"
          >
            确认兑换
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, computed, h } from 'vue'
import { useMessage, NTag } from 'naive-ui'

const message = useMessage()

const activeTab = ref('all')
const userPoints = ref(580)
const showExchangeModal = ref(false)
const exchanging = ref(false)
const selectedProduct = ref(null)

const tabs = [
  { key: 'all', label: '全部' },
  { key: 'equipment', label: '运动装备' },
  { key: 'supplement', label: '营养补剂' },
  { key: 'course', label: '课程优惠' },
  { key: 'other', label: '其他' }
]

// 商品图标
const productIcons = {
  bottle: `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M9 2v2"/><path d="M15 2v2"/><path d="M12 2v8"/><path d="M8 10h8v10a2 2 0 0 1-2 2h-4a2 2 0 0 1-2-2V10z"/></svg>`,
  protein: `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8h1a4 4 0 0 1 0 8h-1"/><path d="M2 8h16v9a4 4 0 0 1-4 4H6a4 4 0 0 1-4-4V8z"/><line x1="6" y1="2" x2="6" y2="4"/><line x1="10" y1="2" x2="10" y2="4"/><line x1="14" y1="2" x2="14" y2="4"/></svg>`,
  yoga: `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg>`,
  coach: `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>`,
  bag: `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/><path d="M16 10a4 4 0 0 1-8 0"/></svg>`,
  vitamin: `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M10.5 20.5l10-10a4.95 4.95 0 1 0-7-7l-10 10a4.95 4.95 0 1 0 7 7Z"/><path d="M8.5 8.5l7 7"/></svg>`,
  coupon: `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M2 9a3 3 0 0 1 0 6v2a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-2a3 3 0 0 1 0-6V7a2 2 0 0 0-2-2H4a2 2 0 0 0-2 2Z"/><path d="M9 12h6"/></svg>`,
  glove: `<svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M8 12a4 4 0 1 0 8 0V4a2 2 0 0 0-4 0v8"/><path d="M8 12v6a2 2 0 0 0 2 2h4a2 2 0 0 0 2-2v-6"/></svg>`
}

const products = [
  { id: 1, name: '运动水壶 750ml', description: '大容量防漏设计', icon: productIcons.bottle, points: 200, category: 'equipment', gradient: 'linear-gradient(135deg, #E3F2FD, #BBDEFB)' },
  { id: 2, name: '乳清蛋白粉 2磅', description: '高纯度乳清蛋白', icon: productIcons.protein, points: 500, category: 'supplement', gradient: 'linear-gradient(135deg, #FFE5B4, #FFDAB9)' },
  { id: 3, name: '专业瑜伽垫', description: '防滑加厚环保材质', icon: productIcons.yoga, points: 300, category: 'equipment', gradient: 'linear-gradient(135deg, #E8F5E9, #C8E6C9)' },
  { id: 4, name: '私教体验课 1节', description: '一对一专业指导', icon: productIcons.coach, points: 1000, category: 'course', gradient: 'linear-gradient(135deg, #FFF3E0, #FFE0B2)' },
  { id: 5, name: '健身背包', description: '干湿分离大容量', icon: productIcons.bag, points: 400, category: 'equipment', gradient: 'linear-gradient(135deg, #F3E5F5, #E1BEE7)' },
  { id: 6, name: '复合维生素 60粒', description: '全面营养补充', icon: productIcons.vitamin, points: 350, category: 'supplement', gradient: 'linear-gradient(135deg, #E0F2F1, #B2DFDB)' },
  { id: 7, name: '月卡抵扣券 ¥50', description: '购买月卡立减', icon: productIcons.coupon, points: 800, category: 'course', gradient: 'linear-gradient(135deg, #E0F2F1, #B2DFDB)' },
  { id: 8, name: '健身手套', description: '透气防滑护掌', icon: productIcons.glove, points: 150, category: 'equipment', gradient: 'linear-gradient(135deg, #FBE9E7, #FFCCBC)' }
]

const filteredProducts = computed(() => {
  if (activeTab.value === 'all') {
    return products
  }
  return products.filter(p => p.category === activeTab.value)
})

const recordColumns = [
  { title: '日期', key: 'date', width: 120 },
  { title: '商品名称', key: 'productName' },
  { 
    title: '消耗积分', 
    key: 'points',
    width: 100,
    render(row) {
      return h('span', { style: { color: '#FF6B35', fontWeight: 600 } }, `-${row.points}`)
    }
  },
  { 
    title: '状态', 
    key: 'status',
    width: 100,
    render(row) {
      const statusMap = {
        'shipped': { type: 'success', text: '已发货' },
        'used': { type: 'info', text: '已使用' },
        'completed': { type: 'default', text: '已完成' }
      }
      const status = statusMap[row.status]
      return h(NTag, { type: status.type, size: 'small' }, { default: () => status.text })
    }
  }
]

const exchangeRecords = [
  { date: '2024-10-15', productName: '运动水壶 750ml', points: 200, status: 'shipped' },
  { date: '2024-10-05', productName: '私教体验课 1节', points: 1000, status: 'used' },
  { date: '2024-09-20', productName: '健身手套', points: 150, status: 'completed' },
  { date: '2024-09-10', productName: '摇摇杯 600ml', points: 120, status: 'completed' }
]

function openExchangeModal(product) {
  selectedProduct.value = product
  showExchangeModal.value = true
}

function confirmExchange() {
  exchanging.value = true
  setTimeout(() => {
    userPoints.value -= selectedProduct.value.points
    exchangeRecords.unshift({
      date: new Date().toISOString().split('T')[0],
      productName: selectedProduct.value.name,
      points: selectedProduct.value.points,
      status: 'shipped'
    })
    exchanging.value = false
    showExchangeModal.value = false
    message.success(`成功兑换 ${selectedProduct.value.name}！`)
  }, 1500)
}
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

.product-points {
  color: #FF6B35;
  font-weight: 700;
  font-size: 18px;
  margin-bottom: 12px;
  font-family: 'Outfit', sans-serif;
}

.exchange-btn {
  width: 100%;
  border-radius: 10px;
  font-weight: 600;
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

.exchange-content {
  padding: 8px;
}

.exchange-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
}

.gift-icon {
  font-size: 24px;
}

.exchange-product {
  display: flex;
  gap: 16px;
  align-items: center;
}

.exchange-icon {
  width: 80px;
  height: 80px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  flex-shrink: 0;
}

.exchange-info h4 {
  font-size: 18px;
  margin-bottom: 4px;
  font-weight: 600;
  color: #1A1A2E;
}

.exchange-info p {
  color: #6B7280;
  font-size: 14px;
  margin: 0;
}

.exchange-detail {
  padding: 8px 0;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 14px;
}

.detail-row .points {
  color: #FF6B35;
  font-weight: 700;
}

.detail-row .insufficient {
  color: #EF476F;
  font-weight: 600;
}
</style>
