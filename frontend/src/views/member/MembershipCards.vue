<template>
  <div class="membership-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">会员卡中心</h1>
      <p class="page-subtitle">选择适合您的会员方案，开启健身之旅</p>
    </div>

    <!-- 我的会员状态卡片 -->
    <div v-if="myMembership.isActive" class="my-membership-card">
      <div class="membership-info">
        <div class="membership-badge">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
          </svg>
        </div>
        <div class="membership-details">
          <h3>{{ myMembership.membershipType || '会员' }}</h3>
          <p>剩余 {{ myMembership.remainingDays }} 天</p>
          <p class="expire-date">到期时间: {{ formatDate(myMembership.expireTime) }}</p>
        </div>
      </div>
      <n-button type="primary" @click="$router.push('/member/cards')">
        查看详情
      </n-button>
    </div>

    <!-- 推荐会员卡 -->
    <div v-if="recommendCards.length > 0" class="section">
      <h2 class="section-title">热门推荐</h2>
      <n-grid :cols="3" :x-gap="20" :y-gap="20">
        <n-grid-item v-for="card in recommendCards" :key="card.id">
          <div class="recommend-card" @click="goToDetail(card.id)">
            <div class="recommend-badge">推荐</div>
            <h3>{{ card.name }}</h3>
            <p class="card-subtitle">{{ card.subtitle }}</p>
            <div class="price-row">
              <span class="price">¥{{ card.price }}</span>
              <span v-if="card.originalPrice" class="original-price">¥{{ card.originalPrice }}</span>
            </div>
            <div class="card-features">
              <span class="feature-tag">{{ card.durationDays }}天</span>
              <span v-if="card.pointsReward > 0" class="feature-tag">送{{ card.pointsReward }}积分</span>
            </div>
          </div>
        </n-grid-item>
      </n-grid>
    </div>

    <!-- 所有会员卡 -->
    <div class="section">
      <h2 class="section-title">全部会员卡</h2>
      <n-grid :cols="4" :x-gap="16" :y-gap="16">
        <n-grid-item v-for="card in cardList" :key="card.id">
          <div class="membership-card" :class="{ 'recommended': card.isRecommend }" @click="goToDetail(card.id)">
            <div class="card-header">
              <span v-if="card.isRecommend" class="recommend-tag">推荐</span>
              <span class="type-tag">{{ card.typeName }}</span>
            </div>
            
            <div class="card-body">
              <h3 class="card-name">{{ card.name }}</h3>
              <p class="card-subtitle">{{ card.subtitle }}</p>
              
              <div class="price-section">
                <span class="current-price">¥{{ card.price }}</span>
                <span v-if="card.originalPrice" class="original-price">¥{{ card.originalPrice }}</span>
              </div>
              
              <div class="daily-price" v-if="card.dailyPrice">
                日均 ¥{{ card.dailyPrice }}
              </div>
              
              <div class="card-benefits">
                <div class="benefit-item">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="20 6 9 17 4 12"/>
                  </svg>
                  <span>{{ card.durationDays }}天有效期</span>
                </div>
                <div class="benefit-item" v-if="card.pointsReward > 0">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="20 6 9 17 4 12"/>
                  </svg>
                  <span>赠送 {{ card.pointsReward }} 积分</span>
                </div>
              </div>
            </div>
            
            <div class="card-footer">
              <n-button type="primary" size="large" block>
                立即购买
              </n-button>
            </div>
          </div>
        </n-grid-item>
      </n-grid>
    </div>

    <!-- 空状态 -->
    <n-empty v-if="cardList.length === 0 && !loading" description="暂无会员卡">
      <template #icon>
        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
          <line x1="9" y1="9" x2="15" y2="9"/>
          <line x1="9" y1="15" x2="15" y2="15"/>
        </svg>
      </template>
    </n-empty>

    <!-- 加载状态 -->
    <n-spin v-if="loading" size="large" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { getMembershipCardList, getRecommendCards, getMyMembership } from '@/api/membership'

const router = useRouter()
const message = useMessage()

const cardList = ref([])
const recommendCards = ref([])
const myMembership = ref({
  isActive: false,
  remainingDays: 0
})
const loading = ref(false)

// 获取会员卡列表
async function loadCardList() {
  try {
    loading.value = true
    const res = await getMembershipCardList()
    cardList.value = res || []
  } catch (error) {
    console.error('获取会员卡列表失败:', error)
    message.error('获取会员卡列表失败')
  } finally {
    loading.value = false
  }
}

// 获取推荐会员卡
async function loadRecommendCards() {
  try {
    const res = await getRecommendCards(3)
    recommendCards.value = res || []
  } catch (error) {
    console.error('获取推荐会员卡失败:', error)
  }
}

// 获取我的会员信息
async function loadMyMembership() {
  try {
    const res = await getMyMembership()
    myMembership.value = res || { isActive: false, remainingDays: 0 }
  } catch (error) {
    console.error('获取会员信息失败:', error)
  }
}

// 跳转到详情页
function goToDetail(cardId) {
  router.push(`/member/membership/${cardId}`)
}

// 格式化日期
function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

onMounted(() => {
  loadCardList()
  loadRecommendCards()
  loadMyMembership()
})
</script>

<style scoped>
.membership-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  color: #1A1A2E;
  margin-bottom: 8px;
}

.page-subtitle {
  font-size: 16px;
  color: #6B7280;
}

/* 我的会员状态卡片 */
.my-membership-card {
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C61 100%);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 40px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: white;
}

.membership-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.membership-badge {
  width: 64px;
  height: 64px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.membership-badge svg {
  color: white;
}

.membership-details h3 {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 4px;
}

.membership-details p {
  font-size: 16px;
  opacity: 0.9;
}

.expire-date {
  font-size: 14px;
  opacity: 0.8;
}

/* 区块样式 */
.section {
  margin-bottom: 40px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #1A1A2E;
  margin-bottom: 20px;
}

/* 推荐卡片 */
.recommend-card {
  background: linear-gradient(135deg, #FFF8F0 0%, #FFE8D6 100%);
  border: 2px solid #FF6B35;
  border-radius: 16px;
  padding: 24px;
  position: relative;
  cursor: pointer;
  transition: all 0.3s;
}

.recommend-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(255, 107, 53, 0.2);
}

.recommend-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: #FF6B35;
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.recommend-card h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1A1A2E;
  margin-bottom: 8px;
}

.card-subtitle {
  font-size: 14px;
  color: #6B7280;
  margin-bottom: 16px;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 12px;
}

.price {
  font-size: 28px;
  font-weight: 700;
  color: #FF6B35;
}

.original-price {
  font-size: 16px;
  color: #999;
  text-decoration: line-through;
}

.card-features {
  display: flex;
  gap: 8px;
}

.feature-tag {
  background: rgba(255, 107, 53, 0.1);
  color: #FF6B35;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
}

/* 普通会员卡 */
.membership-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
  cursor: pointer;
  border: 2px solid transparent;
}

.membership-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12);
}

.membership-card.recommended {
  border-color: #FF6B35;
}

.card-header {
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #F8F9FA;
}

.recommend-tag {
  background: #FF6B35;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.type-tag {
  font-size: 12px;
  color: #6B7280;
}

.card-body {
  padding: 20px;
}

.card-name {
  font-size: 18px;
  font-weight: 600;
  color: #1A1A2E;
  margin-bottom: 8px;
}

.card-subtitle {
  font-size: 14px;
  color: #6B7280;
  margin-bottom: 16px;
  min-height: 20px;
}

.price-section {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 8px;
}

.current-price {
  font-size: 24px;
  font-weight: 700;
  color: #FF6B35;
}

.original-price {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
}

.daily-price {
  font-size: 12px;
  color: #6B7280;
  margin-bottom: 16px;
}

.card-benefits {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.benefit-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #6B7280;
}

.benefit-item svg {
  color: #52C41A;
}

.card-footer {
  padding: 0 20px 20px;
}
</style>
