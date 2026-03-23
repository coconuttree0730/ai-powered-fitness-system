<template>
  <div class="order-management-page">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6" v-for="stat in stats" :key="stat.title">
        <el-card class="stat-card" :body-style="{ padding: '20px' }">
          <div class="stat-content">
            <div class="stat-icon" :style="{ background: stat.color }">
              <el-icon :size="24"><component :is="stat.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-title">{{ stat.title }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 子模块标签页 -->
    <el-card>
      <el-tabs v-model="activeTab" type="border-card" @tab-change="handleTabChange">
        <!-- 会员卡订单管理 -->
        <el-tab-pane name="membership-order">
          <template #label>
            <span class="tab-label">
              <el-icon><CreditCard /></el-icon>会员卡订单
            </span>
          </template>
          <div class="tab-content">
            <el-row :gutter="20" align="middle" class="tab-toolbar">
              <el-col :span="18">
                <el-space>
                  <el-input
                    v-model="membershipOrderSearch.orderNo"
                    placeholder="订单号"
                    clearable
                    style="width: 180px"
                  />
                  <el-input
                    v-model="membershipOrderSearch.memberName"
                    placeholder="会员姓名"
                    clearable
                    style="width: 150px"
                  />
                  <el-select v-model="membershipOrderSearch.status" placeholder="全部状态" clearable style="width: 120px">
                    <el-option label="待支付" value="PENDING" />
                    <el-option label="已支付" value="PAID" />
                    <el-option label="已完成" value="COMPLETED" />
                    <el-option label="已取消" value="CANCELLED" />
                  </el-select>
                  <el-date-picker
                    v-model="membershipOrderSearch.dateRange"
                    type="daterange"
                    range-separator="至"
                    start-placeholder="开始日期"
                    end-placeholder="结束日期"
                    style="width: 240px"
                  />
                  <el-button type="primary" @click="fetchMembershipOrderData">
                    <el-icon><Search /></el-icon>搜索
                  </el-button>
                </el-space>
              </el-col>
              <el-col :span="6" style="text-align: right">
                <el-button @click="handleExportMembershipOrders">
                  <el-icon><Download /></el-icon>导出订单
                </el-button>
              </el-col>
            </el-row>

            <el-table :data="membershipOrderData" v-loading="loading" stripe>
              <el-table-column type="index" width="50" />
              <el-table-column prop="orderNo" label="订单号" width="160" />
              <el-table-column label="会员信息" min-width="150">
                <template #default="{ row }">
                  <div class="member-info">
                    <el-avatar :size="32" :src="row.memberAvatar">{{ row.memberName?.charAt(0) }}</el-avatar>
                    <div class="member-detail">
                      <div class="member-name">{{ row.memberName }}</div>
                      <div class="member-phone">{{ row.memberPhone }}</div>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="会员卡" min-width="150">
                <template #default="{ row }">
                  <div class="product-info">
                    <div class="product-name">{{ row.cardName }}</div>
                    <div class="product-type">{{ row.cardType }}</div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="amount" label="金额" width="120">
                <template #default="{ row }">
                  <span class="price">¥{{ row.amount }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="payMethod" label="支付方式" width="100">
                <template #default="{ row }">
                  <el-tag size="small">{{ row.payMethod }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="下单时间" width="160" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="getOrderStatusType(row.status)">{{ getOrderStatusLabel(row.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" link @click="handleViewMembershipOrder(row)">
                    <el-icon><View /></el-icon>查看
                  </el-button>
                  <el-button v-if="row.status === 'PENDING'" type="success" link @click="handleConfirmMembershipOrder(row)">
                    <el-icon><Check /></el-icon>确认
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <div class="pagination-wrapper">
              <el-pagination
                v-model:current-page="membershipOrderPagination.page"
                v-model:page-size="membershipOrderPagination.pageSize"
                :total="membershipOrderPagination.total"
                :page-sizes="[10, 20, 50, 100]"
                layout="total, sizes, prev, pager, next, jumper"
              />
            </div>
          </div>
        </el-tab-pane>

        <!-- 积分兑换管理 -->
        <el-tab-pane name="points-exchange">
          <template #label>
            <span class="tab-label">
              <el-icon><Coin /></el-icon>积分兑换
            </span>
          </template>
          <div class="tab-content">
            <el-row :gutter="20" align="middle" class="tab-toolbar">
              <el-col :span="18">
                <el-space>
                  <el-input
                    v-model="pointsExchangeSearch.orderNo"
                    placeholder="订单号"
                    clearable
                    style="width: 180px"
                  />
                  <el-input
                    v-model="pointsExchangeSearch.memberName"
                    placeholder="会员姓名"
                    clearable
                    style="width: 150px"
                  />
                  <el-select v-model="pointsExchangeSearch.status" placeholder="全部状态" clearable style="width: 120px">
                    <el-option label="待处理" value="PENDING" />
                    <el-option label="处理中" value="PROCESSING" />
                    <el-option label="已发货" value="SHIPPED" />
                    <el-option label="已完成" value="COMPLETED" />
                  </el-select>
                  <el-button type="primary" @click="fetchPointsExchangeData">
                    <el-icon><Search /></el-icon>搜索
                  </el-button>
                </el-space>
              </el-col>
              <el-col :span="6" style="text-align: right">
                <el-button @click="handleExportPointsExchange">
                  <el-icon><Download /></el-icon>导出订单
                </el-button>
              </el-col>
            </el-row>

            <el-table :data="pointsExchangeData" v-loading="loading" stripe>
              <el-table-column type="index" width="50" />
              <el-table-column prop="orderNo" label="订单号" width="160" />
              <el-table-column label="会员信息" min-width="150">
                <template #default="{ row }">
                  <div class="member-info">
                    <el-avatar :size="32" :src="row.memberAvatar">{{ row.memberName?.charAt(0) }}</el-avatar>
                    <div class="member-detail">
                      <div class="member-name">{{ row.memberName }}</div>
                      <div class="member-phone">{{ row.memberPhone }}</div>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="兑换商品" min-width="150">
                <template #default="{ row }">
                  <div class="product-info">
                    <div class="product-name">{{ row.productName }}</div>
                    <div class="product-quantity">x{{ row.quantity }}</div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="points" label="消耗积分" width="120">
                <template #default="{ row }">
                  <span class="points">{{ row.points }} 积分</span>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="兑换时间" width="160" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="getExchangeStatusType(row.status)">{{ getExchangeStatusLabel(row.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="180" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" link @click="handleViewPointsExchange(row)">
                    <el-icon><View /></el-icon>查看
                  </el-button>
                  <el-button v-if="row.status === 'PENDING'" type="success" link @click="handleProcessPointsExchange(row)">
                    <el-icon><Check /></el-icon>处理
                  </el-button>
                  <el-button v-if="row.status === 'PROCESSING'" type="warning" link @click="handleShipPointsExchange(row)">
                    <el-icon><Box /></el-icon>发货
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <div class="pagination-wrapper">
              <el-pagination
                v-model:current-page="pointsExchangePagination.page"
                v-model:page-size="pointsExchangePagination.pageSize"
                :total="pointsExchangePagination.total"
                :page-sizes="[10, 20, 50, 100]"
                layout="total, sizes, prev, pager, next, jumper"
              />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 会员卡订单详情弹窗 -->
    <el-dialog v-model="membershipOrderDetailVisible" title="订单详情" width="700px">
      <el-descriptions :column="2" border v-if="currentMembershipOrder">
        <el-descriptions-item label="订单号">{{ currentMembershipOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ currentMembershipOrder.createTime }}</el-descriptions-item>
        <el-descriptions-item label="会员姓名">{{ currentMembershipOrder.memberName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentMembershipOrder.memberPhone }}</el-descriptions-item>
        <el-descriptions-item label="会员卡">{{ currentMembershipOrder.cardName }}</el-descriptions-item>
        <el-descriptions-item label="有效期">{{ currentMembershipOrder.validityDays }}天</el-descriptions-item>
        <el-descriptions-item label="订单金额">
          <span class="price">¥{{ currentMembershipOrder.amount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ currentMembershipOrder.payMethod }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="getOrderStatusType(currentMembershipOrder.status)">
            {{ getOrderStatusLabel(currentMembershipOrder.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="支付时间">{{ currentMembershipOrder.payTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <div class="order-timeline" v-if="currentMembershipOrder">
        <h4>订单时间线</h4>
        <el-timeline>
          <el-timeline-item
            v-for="(item, index) in currentMembershipOrder.timeline"
            :key="index"
            :type="item.type"
            :timestamp="item.time"
          >
            {{ item.content }}
          </el-timeline-item>
        </el-timeline>
      </div>
      <template #footer>
        <el-button @click="membershipOrderDetailVisible = false">关闭</el-button>
        <el-button type="primary" v-if="currentMembershipOrder?.status === 'PENDING'" @click="handleConfirmMembershipOrder(currentMembershipOrder)">
          确认收款
        </el-button>
      </template>
    </el-dialog>

    <!-- 积分兑换详情弹窗 -->
    <el-dialog v-model="pointsExchangeDetailVisible" title="兑换详情" width="700px">
      <el-descriptions :column="2" border v-if="currentPointsExchange">
        <el-descriptions-item label="订单号">{{ currentPointsExchange.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="兑换时间">{{ currentPointsExchange.createTime }}</el-descriptions-item>
        <el-descriptions-item label="会员姓名">{{ currentPointsExchange.memberName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentPointsExchange.memberPhone }}</el-descriptions-item>
        <el-descriptions-item label="商品名称">{{ currentPointsExchange.productName }}</el-descriptions-item>
        <el-descriptions-item label="兑换数量">x{{ currentPointsExchange.quantity }}</el-descriptions-item>
        <el-descriptions-item label="消耗积分">
          <span class="points">{{ currentPointsExchange.points }} 积分</span>
        </el-descriptions-item>
        <el-descriptions-item label="剩余积分">{{ currentPointsExchange.remainingPoints }} 积分</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="getExchangeStatusType(currentPointsExchange.status)">
            {{ getExchangeStatusLabel(currentPointsExchange.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="物流单号">{{ currentPointsExchange.trackingNo || '-' }}</el-descriptions-item>
      </el-descriptions>
      <div class="delivery-address" v-if="currentPointsExchange">
        <h4>收货地址</h4>
        <p>{{ currentPointsExchange.address }}</p>
      </div>
      <template #footer>
        <el-button @click="pointsExchangeDetailVisible = false">关闭</el-button>
        <el-button type="success" v-if="currentPointsExchange?.status === 'PENDING'" @click="handleProcessPointsExchange(currentPointsExchange)">
          开始处理
        </el-button>
        <el-button type="warning" v-if="currentPointsExchange?.status === 'PROCESSING'" @click="handleShipPointsExchange(currentPointsExchange)">
          确认发货
        </el-button>
      </template>
    </el-dialog>

    <!-- 发货弹窗 -->
    <el-dialog v-model="shipDialogVisible" title="确认发货" width="500px">
      <el-form ref="shipFormRef" :model="shipForm" :rules="shipRules" label-width="100px">
        <el-form-item label="物流单号" prop="trackingNo">
          <el-input v-model="shipForm.trackingNo" placeholder="请输入物流单号" />
        </el-form-item>
        <el-form-item label="物流公司" prop="carrier">
          <el-select v-model="shipForm.carrier" placeholder="请选择物流公司" style="width: 100%">
            <el-option label="顺丰速运" value="SF" />
            <el-option label="中通快递" value="ZTO" />
            <el-option label="圆通速递" value="YTO" />
            <el-option label="申通快递" value="STO" />
            <el-option label="韵达速递" value="YD" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="shipForm.remark" type="textarea" :rows="3" placeholder="可选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitShip" :loading="submitting">确认发货</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  CreditCard, Coin, Search, Download, View, Check, Box, ShoppingCart, TrendCharts, User
} from '@element-plus/icons-vue'

// 统计数据
const stats = ref([
  { title: '今日订单', value: 28, icon: 'ShoppingCart', color: '#1890ff' },
  { title: '今日收入', value: '¥12,580', icon: 'TrendCharts', color: '#52c41a' },
  { title: '待处理', value: 5, icon: 'User', color: '#faad14' },
  { title: '积分兑换', value: 12, icon: 'Coin', color: '#722ed1' }
])

// 当前标签页
const activeTab = ref('membership-order')
const loading = ref(false)

// ========== 会员卡订单 ==========
const membershipOrderSearch = reactive({
  orderNo: '',
  memberName: '',
  status: '',
  dateRange: null
})
const membershipOrderData = ref([])
const membershipOrderPagination = reactive({ page: 1, pageSize: 10, total: 0 })
const membershipOrderDetailVisible = ref(false)
const currentMembershipOrder = ref(null)

const orderStatusMap = {
  PENDING: { label: '待支付', type: 'warning' },
  PAID: { label: '已支付', type: 'success' },
  COMPLETED: { label: '已完成', type: 'primary' },
  CANCELLED: { label: '已取消', type: 'info' }
}

function getOrderStatusLabel(status) {
  return orderStatusMap[status]?.label || status
}

function getOrderStatusType(status) {
  return orderStatusMap[status]?.type || ''
}

function fetchMembershipOrderData() {
  loading.value = true
  setTimeout(() => {
    membershipOrderData.value = [
      {
        id: 1,
        orderNo: 'MC20240115001',
        memberName: '张三',
        memberPhone: '138****8888',
        memberAvatar: '',
        cardName: '至尊年卡',
        cardType: '年卡',
        validityDays: 365,
        amount: 3999,
        payMethod: '微信支付',
        createTime: '2024-01-15 14:30:25',
        payTime: '2024-01-15 14:31:08',
        status: 'COMPLETED',
        timeline: [
          { type: 'primary', time: '2024-01-15 14:30:25', content: '订单创建' },
          { type: 'success', time: '2024-01-15 14:31:08', content: '支付成功 - 微信支付' },
          { type: 'success', time: '2024-01-15 14:32:15', content: '会员卡已激活' },
          { type: 'primary', time: '2024-01-15 14:32:15', content: '订单完成' }
        ]
      },
      {
        id: 2,
        orderNo: 'MC20240115002',
        memberName: '李四',
        memberPhone: '139****6666',
        memberAvatar: '',
        cardName: '金卡季卡',
        cardType: '季卡',
        validityDays: 90,
        amount: 1299,
        payMethod: '支付宝',
        createTime: '2024-01-15 13:22:18',
        payTime: '2024-01-15 13:23:45',
        status: 'COMPLETED',
        timeline: [
          { type: 'primary', time: '2024-01-15 13:22:18', content: '订单创建' },
          { type: 'success', time: '2024-01-15 13:23:45', content: '支付成功 - 支付宝' },
          { type: 'success', time: '2024-01-15 13:25:00', content: '会员卡已激活' },
          { type: 'primary', time: '2024-01-15 13:25:00', content: '订单完成' }
        ]
      },
      {
        id: 3,
        orderNo: 'MC20240115003',
        memberName: '王五',
        memberPhone: '137****5555',
        memberAvatar: '',
        cardName: '银卡月卡',
        cardType: '月卡',
        validityDays: 30,
        amount: 499,
        payMethod: '微信支付',
        createTime: '2024-01-15 11:05:42',
        payTime: null,
        status: 'PENDING',
        timeline: [
          { type: 'primary', time: '2024-01-15 11:05:42', content: '订单创建' },
          { type: 'warning', time: '-', content: '等待支付' }
        ]
      }
    ]
    membershipOrderPagination.total = 3
    loading.value = false
  }, 300)
}

function handleViewMembershipOrder(row) {
  currentMembershipOrder.value = row
  membershipOrderDetailVisible.value = true
}

function handleConfirmMembershipOrder(row) {
  ElMessageBox.confirm(`确认已收到会员 "${row.memberName}" 的付款吗？`, '提示', { type: 'info' }).then(() => {
    ElMessage.success('订单已确认')
    fetchMembershipOrderData()
    membershipOrderDetailVisible.value = false
  })
}

function handleExportMembershipOrders() {
  ElMessage.success('会员卡订单导出成功')
}

// ========== 积分兑换 ==========
const pointsExchangeSearch = reactive({
  orderNo: '',
  memberName: '',
  status: ''
})
const pointsExchangeData = ref([])
const pointsExchangePagination = reactive({ page: 1, pageSize: 10, total: 0 })
const pointsExchangeDetailVisible = ref(false)
const currentPointsExchange = ref(null)
const shipDialogVisible = ref(false)
const shipFormRef = ref(null)
const shipForm = reactive({
  trackingNo: '',
  carrier: '',
  remark: ''
})
const shipRules = {
  trackingNo: [{ required: true, message: '请输入物流单号', trigger: 'blur' }],
  carrier: [{ required: true, message: '请选择物流公司', trigger: 'change' }]
}

const exchangeStatusMap = {
  PENDING: { label: '待处理', type: 'warning' },
  PROCESSING: { label: '处理中', type: 'primary' },
  SHIPPED: { label: '已发货', type: 'success' },
  COMPLETED: { label: '已完成', type: 'success' }
}

function getExchangeStatusLabel(status) {
  return exchangeStatusMap[status]?.label || status
}

function getExchangeStatusType(status) {
  return exchangeStatusMap[status]?.type || ''
}

function fetchPointsExchangeData() {
  loading.value = true
  setTimeout(() => {
    pointsExchangeData.value = [
      {
        id: 1,
        orderNo: 'PE20240115001',
        memberName: '赵六',
        memberPhone: '136****4444',
        memberAvatar: '',
        productName: '乳清蛋白粉 5磅',
        quantity: 1,
        points: 3500,
        remainingPoints: 5200,
        createTime: '2024-01-15 15:20:30',
        status: 'PENDING',
        trackingNo: null,
        address: '北京市朝阳区xxx街道xxx号'
      },
      {
        id: 2,
        orderNo: 'PE20240115002',
        memberName: '钱七',
        memberPhone: '135****3333',
        memberAvatar: '',
        productName: '可调节哑铃套装',
        quantity: 1,
        points: 8800,
        remainingPoints: 1200,
        createTime: '2024-01-15 14:15:22',
        status: 'PROCESSING',
        trackingNo: null,
        address: '上海市浦东新区xxx路xxx号'
      },
      {
        id: 3,
        orderNo: 'PE20240115003',
        memberName: '孙八',
        memberPhone: '134****2222',
        memberAvatar: '',
        productName: '专业训练运动鞋',
        quantity: 1,
        points: 5200,
        remainingPoints: 800,
        createTime: '2024-01-15 10:30:45',
        status: 'SHIPPED',
        trackingNo: 'SF1234567890',
        address: '广州市天河区xxx大道xxx号'
      }
    ]
    pointsExchangePagination.total = 3
    loading.value = false
  }, 300)
}

function handleViewPointsExchange(row) {
  currentPointsExchange.value = row
  pointsExchangeDetailVisible.value = true
}

function handleProcessPointsExchange(row) {
  ElMessageBox.confirm(`开始处理 "${row.memberName}" 的兑换订单？`, '提示', { type: 'info' }).then(() => {
    ElMessage.success('订单处理中')
    fetchPointsExchangeData()
    pointsExchangeDetailVisible.value = false
  })
}

function handleShipPointsExchange(row) {
  currentPointsExchange.value = row
  shipForm.trackingNo = ''
  shipForm.carrier = ''
  shipForm.remark = ''
  shipDialogVisible.value = true
}

const submitting = ref(false)

function handleSubmitShip() {
  shipFormRef.value?.validate((valid) => {
    if (valid) {
      submitting.value = true
      setTimeout(() => {
        ElMessage.success('发货成功')
        shipDialogVisible.value = false
        fetchPointsExchangeData()
        pointsExchangeDetailVisible.value = false
        submitting.value = false
      }, 500)
    }
  })
}

function handleExportPointsExchange() {
  ElMessage.success('积分兑换订单导出成功')
}

function handleTabChange() {
  if (activeTab.value === 'membership-order') {
    fetchMembershipOrderData()
  } else {
    fetchPointsExchangeData()
  }
}

onMounted(() => {
  fetchMembershipOrderData()
})
</script>

<style scoped>
.order-management-page {
  padding: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
}

.stat-title {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
}

.tab-content {
  padding: 20px 0;
}

.tab-toolbar {
  margin-bottom: 20px;
}

.member-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.member-detail {
  flex: 1;
}

.member-name {
  font-weight: 500;
  color: #303133;
}

.member-phone {
  font-size: 12px;
  color: #909399;
}

.product-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.product-name {
  font-weight: 500;
  color: #303133;
}

.product-type,
.product-quantity {
  font-size: 12px;
  color: #909399;
}

.price {
  font-weight: 600;
  color: #f56c6c;
  font-size: 16px;
}

.points {
  font-weight: 600;
  color: #faad14;
  font-size: 14px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.order-timeline {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.order-timeline h4 {
  margin-bottom: 15px;
  color: #303133;
}

.delivery-address {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.delivery-address h4 {
  margin-bottom: 10px;
  color: #303133;
}

.delivery-address p {
  color: #606266;
  line-height: 1.6;
}
</style>
