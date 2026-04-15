<template>
  <div class="card-detail-page">
    <!-- 返回按钮 -->
    <div class="back-nav">
      <n-button text @click="$router.back()">
        <template #icon>
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 12H5M12 19l-7-7 7-7"/>
          </svg>
        </template>
        返回
      </n-button>
    </div>

    <div v-if="cardDetail" class="detail-container">
      <!-- 左侧：卡片信息 -->
      <div class="card-info-section">
        <div class="card-header">
          <span class="type-badge">{{ cardDetail.typeName }}</span>
          <span v-if="cardDetail.isRecommend" class="recommend-badge">推荐</span>
        </div>
        
        <h1 class="card-title">{{ cardDetail.name }}</h1>
        <p class="card-subtitle">{{ cardDetail.subtitle }}</p>
        
        <div class="price-box">
          <div class="main-price">
            <span class="price-symbol">¥</span>
            <span class="price-value">{{ cardDetail.price }}</span>
          </div>
          <div v-if="cardDetail.originalPrice" class="original-price">
            原价 ¥{{ cardDetail.originalPrice }}
          </div>
        </div>
        
        <div class="stats-row">
          <div class="stat-item">
            <span class="stat-value">{{ cardDetail.durationDays }}天</span>
            <span class="stat-label">有效期</span>
          </div>
          <div v-if="cardDetail.dailyPrice" class="stat-item">
            <span class="stat-value">¥{{ cardDetail.dailyPrice }}</span>
            <span class="stat-label">日均</span>
          </div>
          <div v-if="cardDetail.pointsReward > 0" class="stat-item">
            <span class="stat-value">+{{ cardDetail.pointsReward }}</span>
            <span class="stat-label">赠送积分</span>
          </div>
        </div>
        
        <n-button 
          type="primary" 
          size="large" 
          class="buy-button"
          @click="showConfirmModal = true"
        >
          立即购买
        </n-button>
      </div>

      <!-- 右侧：权益详情 -->
      <div class="benefits-section">
        <h2 class="section-title">会员权益</h2>
        
        <div v-if="cardDetail.contents && cardDetail.contents.length > 0" class="content-list">
          <div 
            v-for="content in cardDetail.contents" 
            :key="content.id" 
            class="content-item"
          >
            <div class="content-header">
              <div class="content-icon">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="20 6 9 17 4 12"/>
                </svg>
              </div>
              <h3 class="content-title">{{ content.title }}</h3>
            </div>
            <p class="content-desc">{{ content.description }}</p>
          </div>
        </div>
        
        <n-empty v-else description="暂无权益说明" />
      </div>
    </div>

    <!-- 加载状态 -->
    <n-spin v-if="loading" size="large" class="loading-spin" />

    <!-- 购买确认弹窗 -->
    <n-modal v-model:show="showConfirmModal" preset="card" style="width: 500px" title="确认购买">
      <div class="confirm-content">
        <div class="confirm-item">
          <span class="label">会员卡</span>
          <span class="value">{{ cardDetail?.name }}</span>
        </div>
        <div class="confirm-item">
          <span class="label">有效期</span>
          <span class="value">{{ cardDetail?.durationDays }} 天</span>
        </div>
        <div class="confirm-item">
          <span class="label">赠送积分</span>
          <span class="value">{{ cardDetail?.pointsReward || 0 }} 积分</span>
        </div>
        <n-divider />
        <div class="confirm-item total">
          <span class="label">支付金额</span>
          <span class="price">¥{{ cardDetail?.price }}</span>
        </div>
        
        <n-alert type="info" :show-icon="false" class="pay-notice">
          点击确认后将跳转至支付宝完成支付
        </n-alert>
      </div>
      
      <template #footer>
        <n-space justify="end">
          <n-button @click="showConfirmModal = false">取消</n-button>
          <n-button 
            type="primary" 
            :loading="submitting"
            @click="handleBuy"
          >
            确认支付
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { getMembershipCardDetail, createMembershipOrder, payMembershipOrder, submitAlipayForm } from '@/api/membership'

const route = useRoute()
const router = useRouter()
const message = useMessage()

const cardId = ref(route.params.id)
const cardDetail = ref(null)
const loading = ref(false)
const showConfirmModal = ref(false)
const submitting = ref(false)

// 获取会员卡详情
async function loadCardDetail() {
  try {
    loading.value = true
    const res = await getMembershipCardDetail(cardId.value)
    cardDetail.value = res
  } catch (error) {
    console.error('获取会员卡详情失败:', error)
    message.error('获取会员卡详情失败')
  } finally {
    loading.value = false
  }
}

// 处理购买
async function handleBuy() {
  submitting.value = true
  try {
    // 1. 创建订单
    const orderRes = await createMembershipOrder({
      cardId: cardId.value
    })
    
    message.success('订单创建成功，正在跳转支付...')
    
    // 2. 发起支付
    const payRes = await payMembershipOrder({
      orderNo: orderRes.orderNo,
      payMethod: 'ALIPAY'
    })
    
    // 3. 提交支付宝表单
    if (payRes.payForm) {
      submitAlipayForm(payRes.payForm)
    } else {
      message.error('支付表单获取失败')
    }
  } catch (error) {
    console.error('购买失败:', error)
    message.error(error.message || '购买失败')
  } finally {
    submitting.value = false
    showConfirmModal.value = false
  }
}

onMounted(() => {
  loadCardDetail()
})
</script>

<style scoped>
.card-detail-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.back-nav {
  margin-bottom: 24px;
}

.detail-container {
  display: grid;
  grid-template-columns: 400px 1fr;
  gap: 40px;
}

/* 左侧卡片信息 */
.card-info-section {
  background: white;
  border-radius: 20px;
  padding: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  height: fit-content;
  position: sticky;
  top: 24px;
}

.card-header {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.type-badge {
  background: #F0F2F5;
  color: #6B7280;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
}

.recommend-badge {
  background: linear-gradient(135deg, #FF6B35, #FF8C61);
  color: white;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
}

.card-title {
  font-size: 24px;
  font-weight: 700;
  color: #1A1A2E;
  margin-bottom: 8px;
}

.card-subtitle {
  font-size: 14px;
  color: #6B7280;
  margin-bottom: 24px;
}

.price-box {
  background: linear-gradient(135deg, #FFF8F0 0%, #FFE8D6 100%);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
}

.main-price {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.price-symbol {
  font-size: 20px;
  color: #FF6B35;
  font-weight: 600;
}

.price-value {
  font-size: 40px;
  font-weight: 700;
  color: #FF6B35;
}

.original-price {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
  margin-top: 4px;
}

.stats-row {
  display: flex;
  gap: 24px;
  margin-bottom: 24px;
  padding: 16px;
  background: #F8F9FA;
  border-radius: 12px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.stat-value {
  font-size: 18px;
  font-weight: 600;
  color: #1A1A2E;
}

.stat-label {
  font-size: 12px;
  color: #6B7280;
  margin-top: 4px;
}

.buy-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #FF6B35, #FF8C61);
}

/* 右侧权益详情 */
.benefits-section {
  background: white;
  border-radius: 20px;
  padding: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #1A1A2E;
  margin-bottom: 24px;
}

.content-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.content-item {
  padding: 20px;
  background: #F8F9FA;
  border-radius: 12px;
  border-left: 4px solid #FF6B35;
}

.content-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.content-icon {
  width: 32px;
  height: 32px;
  background: rgba(255, 107, 53, 0.1);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.content-icon svg {
  color: #FF6B35;
}

.content-title {
  font-size: 16px;
  font-weight: 600;
  color: #1A1A2E;
}

.content-desc {
  font-size: 14px;
  color: #6B7280;
  line-height: 1.6;
  margin-left: 44px;
}

/* 确认弹窗 */
.confirm-content {
  padding: 8px;
}

.confirm-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  font-size: 14px;
}

.confirm-item .label {
  color: #6B7280;
}

.confirm-item .value {
  color: #1A1A2E;
  font-weight: 500;
}

.confirm-item.total {
  font-size: 16px;
  font-weight: 600;
}

.confirm-item.total .price {
  color: #FF6B35;
  font-size: 24px;
}

.pay-notice {
  margin-top: 16px;
}

.loading-spin {
  display: flex;
  justify-content: center;
  padding: 100px;
}

@media (max-width: 768px) {
  .detail-container {
    grid-template-columns: 1fr;
  }
  
  .card-info-section {
    position: static;
  }
}
</style>
