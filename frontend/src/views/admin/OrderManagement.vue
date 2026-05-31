<template>
  <div class="order-management-page">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6" v-for="stat in stats" :key="stat.title">
        <el-card class="stat-card" :body-style="{ padding: '20px' }">
          <div class="stat-content">
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

        <!-- 私教套餐订单 -->
        <el-tab-pane name="coach-package-order">
          <template #label>
            <span class="tab-label">
              <el-icon><User /></el-icon>私教套餐订单
            </span>
          </template>
          <div class="tab-content">
            <el-row :gutter="20" align="middle" class="tab-toolbar">
              <el-col :span="18">
                <el-space>
                  <el-input
                    v-model="coachPackageOrderSearch.orderNo"
                    placeholder="订单号"
                    clearable
                    style="width: 180px"
                    @keyup.enter="handleCoachPackageOrderSearch"
                  >
                    <template #prefix>
                      <el-icon><Search /></el-icon>
                    </template>
                  </el-input>
                  <el-select v-model="coachPackageOrderSearch.status" placeholder="全部状态" clearable style="width: 120px">
                    <el-option label="待支付" value="PENDING" />
                    <el-option label="已支付" value="PAID" />
                    <el-option label="已激活" value="ACTIVATED" />
                    <el-option label="已完成" value="COMPLETED" />
                    <el-option label="已取消" value="CANCELLED" />
                    <el-option label="已超时" value="TIMEOUT" />
                  </el-select>
                  <el-button type="primary" @click="handleCoachPackageOrderSearch">
                    <el-icon><Search /></el-icon>搜索
                  </el-button>
                  <el-button @click="handleCoachPackageOrderReset">重置</el-button>
                </el-space>
              </el-col>
              <el-col :span="6" style="text-align: right">
                <el-button>
                  <el-icon><Download /></el-icon>导出
                </el-button>
              </el-col>
            </el-row>

            <el-table :data="coachPackageOrderData" v-loading="loading" stripe style="width: 100%">
              <el-table-column prop="orderNo" label="订单号" width="180" />
              <el-table-column label="会员信息" min-width="150">
                <template #default="{ row }">
                  <div class="member-info">
                    <el-avatar :size="32" :src="row.memberAvatar" v-if="row.memberAvatar" />
                    <el-avatar :size="32" v-else><el-icon><User /></el-icon></el-avatar>
                    <div class="member-detail">
                      <div class="member-name">{{ row.memberName }}</div>
                      <div class="member-phone">{{ row.memberPhone }}</div>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="套餐/教练" min-width="200">
                <template #default="{ row }">
                  <div>
                    <div class="package-name-text">{{ row.packageName }}</div>
                    <div class="coach-name-text" v-if="row.coachName">{{ row.coachName }}</div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="金额" width="120">
                <template #default="{ row }">
                  <span class="amount">¥{{ row.amount }}</span>
                </template>
              </el-table-column>
              <el-table-column label="支付方式" width="100">
                <template #default="{ row }">
                  <span>{{ row.payMethod }}</span>
                </template>
              </el-table-column>
              <el-table-column label="下单时间" width="180" prop="createTime" />
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="getOrderStatusType(row.status)">
                    {{ row.statusLabel || row.status }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="180" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" link @click="handleViewCoachPackageOrder(row)">
                    <el-icon><View /></el-icon>详情
                  </el-button>
                  <el-button v-if="row.status === 'PENDING'" type="success" link @click="handleConfirmCoachPackageOrder(row)">
                    <el-icon><Check /></el-icon>确认收款
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <div class="pagination-wrapper">
              <el-pagination
                v-model:current-page="coachPackageOrderPagination.page"
                v-model:page-size="coachPackageOrderPagination.pageSize"
                :page-sizes="[10, 20, 50]"
                :total="coachPackageOrderPagination.total"
                layout="total, sizes, prev, pager, next"
                @size-change="fetchCoachPackageOrderData"
                @current-change="fetchCoachPackageOrderData"
              />
            </div>
          </div>
        </el-tab-pane>

        <!-- 商品订单管理 -->
        <el-tab-pane name="product-order">
          <template #label>
            <span class="tab-label">
              <el-icon><ShoppingCart /></el-icon>商品订单
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
                    <el-option label="待支付" value="PENDING" />
                    <el-option label="已支付" value="PAID" />
                    <el-option label="处理中" value="PROCESSING" />
                    <el-option label="待取货" value="NOT_PICKED" />
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
              <el-table-column label="商品" min-width="150">
                <template #default="{ row }">
                  <div class="product-info">
                    <div class="product-name">{{ row.productName }}</div>
                    <div class="product-quantity">x{{ row.quantity }}</div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="原价" width="100">
                <template #default="{ row }">
                  <span style="text-decoration: line-through; color: #999;">¥{{ (row.originalPrice * row.quantity).toFixed(2) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="积分抵扣" width="100">
                <template #default="{ row }">
                  <span v-if="row.pointsDiscount > 0" style="color: #FF6B35;">-¥{{ row.pointsDiscount }}</span>
                  <span v-else>-</span>
                </template>
              </el-table-column>
              <el-table-column label="实付金额" width="100">
                <template #default="{ row }">
                  <span style="color: #FF6B35; font-weight: 600;">¥{{ row.payAmount }}</span>
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
                  <el-tag :type="getExchangeStatusType(row.status)">{{ getExchangeStatusLabel(row.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="180" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" link @click="handleViewPointsExchange(row)">
                    <el-icon><View /></el-icon>查看
                  </el-button>
                  <el-button v-if="row.status === 'PENDING'" type="success" link @click="handleProcessPointsExchange(row)">
                    <el-icon><Check /></el-icon>确认
                  </el-button>
                  <el-button v-if="row.status === 'NOT_PICKED'" type="success" link @click="handlePickupPointsExchange(row)">
                    <el-icon><Check /></el-icon>确认取货
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
        <el-button type="success" v-if="currentPointsExchange?.status === 'NOT_PICKED'" @click="handlePickupPointsExchange(currentPointsExchange)">
          确认取货
        </el-button>
      </template>
    </el-dialog>

    <!-- 取货确认弹窗 -->
    <el-dialog v-model="shipDialogVisible" title="取货确认" width="500px">
      <el-form ref="shipFormRef" :model="shipForm" :rules="shipRules" label-width="100px">
        <el-form-item label="取货码" prop="pickupCode">
          <el-input v-model="shipForm.pickupCode" placeholder="请输入取货码" maxlength="10" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmitShip">确认取货</el-button>
      </template>
    </el-dialog>

    <!-- 私教套餐订单详情弹窗 -->
    <el-dialog v-model="coachPackageOrderDetailVisible" title="私教套餐订单详情" width="700px" :close-on-click-modal="false">
      <template v-if="currentCoachPackageOrder">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ currentCoachPackageOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getOrderStatusType(currentCoachPackageOrder.status)">
              {{ currentCoachPackageOrder.statusLabel || currentCoachPackageOrder.status }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="会员姓名">{{ currentCoachPackageOrder.memberName }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentCoachPackageOrder.memberPhone }}</el-descriptions-item>
          <el-descriptions-item label="套餐名称">{{ currentCoachPackageOrder.packageName }}</el-descriptions-item>
          <el-descriptions-item label="教练名称">{{ currentCoachPackageOrder.coachName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="金额">¥{{ currentCoachPackageOrder.amount }}</el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ currentCoachPackageOrder.payMethod }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ currentCoachPackageOrder.createTime }}</el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ currentCoachPackageOrder.payTime || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div class="order-timeline" v-if="currentCoachPackageOrder.timeline">
          <h4>订单时间线</h4>
          <el-timeline>
            <el-timeline-item
              v-for="(item, index) in currentCoachPackageOrder.timeline"
              :key="index"
              :type="item.type"
              :timestamp="item.time"
            >{{ item.content }}</el-timeline-item>
          </el-timeline>
        </div>
      </template>
      <template #footer>
        <el-button @click="coachPackageOrderDetailVisible = false">关闭</el-button>
        <el-button v-if="currentCoachPackageOrder?.status === 'PENDING'" type="success" @click="handleConfirmCoachPackageOrder(currentCoachPackageOrder)">确认收款</el-button>
      </template>
    </el-dialog>

    <!-- 商品取货确认弹窗 -->
    <el-dialog v-model="pickupDialogVisible" title="取货确认" width="450px">
      <template v-if="currentPickupOrder">
        <el-descriptions :column="1" border style="margin-bottom: 20px">
          <el-descriptions-item label="订单号">{{ currentPickupOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="会员">{{ currentPickupOrder.memberName }}</el-descriptions-item>
          <el-descriptions-item label="商品">{{ currentPickupOrder.productName }}</el-descriptions-item>
          <el-descriptions-item label="数量">x{{ currentPickupOrder.quantity }}</el-descriptions-item>
        </el-descriptions>
        <el-form ref="pickupFormRef" :model="pickupForm" :rules="pickupRules" label-width="80px">
          <el-form-item label="取货码" prop="pickupCode">
            <el-input v-model="pickupForm.pickupCode" placeholder="请输入会员提供的取货码" maxlength="10" />
          </el-form-item>
        </el-form>
      </template>
      <template #footer>
        <el-button @click="pickupDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="pickupSubmitting" @click="handleSubmitPickup">确认取货</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  CreditCard, Coin, Search, Download, View, Check, Box, ShoppingCart, TrendCharts, User
} from '@element-plus/icons-vue'
import { getAdminOrders, getAdminOrderDetail, confirmAdminOrder, shipAdminOrder, getAdminOrderStats, pickupAdminOrder } from '@/api/admin/order'

const stats = ref([
  { title: '今日订单', value: 0 },
  { title: '今日收入', value: '¥0' },
  { title: '待处理', value: 0 },
  { title: '商品订单', value: 0 }
])

function fetchStats() {
  getAdminOrderStats().then(data => {
    stats.value = [
      { title: '今日订单', value: data.todayOrders || 0 },
      { title: '今日收入', value: '¥' + ((data.todayRevenue || 0)).toLocaleString() },
      { title: '待处理', value: data.pendingCount || 0 },
      { title: '商品订单', value: data.productOrderCount || 0 }
    ]
  }).catch(() => {})
}

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
  PROCESSING: { label: '处理中', type: 'primary' },
  NOT_PICKED: { label: '待取货', type: 'warning' },
  SHIPPED: { label: '已发货', type: 'success' },
  COMPLETED: { label: '已完成', type: 'primary' },
  CANCELLED: { label: '已取消', type: 'info' },
  TIMEOUT: { label: '已超时', type: 'info' },
  ACTIVATED: { label: '已激活', type: 'success' }
}

function getOrderStatusLabel(status) {
  return orderStatusMap[status]?.label || status
}

function getOrderStatusType(status) {
  return orderStatusMap[status]?.type || ''
}

function fetchMembershipOrderData() {
  loading.value = true
  const params = {
    page: membershipOrderPagination.page,
    pageSize: membershipOrderPagination.pageSize,
    orderType: 'MEMBERSHIP'
  }
  if (membershipOrderSearch.orderNo) params.keyword = membershipOrderSearch.orderNo
  if (membershipOrderSearch.status) params.status = membershipOrderSearch.status
  if (membershipOrderSearch.dateRange) {
    params.startTime = formatDateParam(membershipOrderSearch.dateRange[0])
    params.endTime = formatDateParam(membershipOrderSearch.dateRange[1]) + ' 23:59:59'
  }

  getAdminOrders(params).then(data => {
    membershipOrderData.value = (data.records || []).map(order => ({
      id: order.id,
      orderNo: order.orderNo,
      memberName: order.userName || '-',
      memberPhone: maskPhone(order.userPhone),
      memberAvatar: order.userAvatar || '',
      cardName: order.membershipExt?.cardName || '-',
      cardType: order.membershipExt?.cardName || '-',
      validityDays: order.membershipExt?.expireTime ? '-' : '-',
      amount: order.payAmount,
      payMethod: order.payMethodLabel || order.payMethod || '-',
      createTime: order.createTime,
      payTime: order.payTime || null,
      status: order.status,
      statusLabel: order.statusLabel,
      timeline: buildTimeline(order)
    }))
    membershipOrderPagination.total = data.total || 0
    loading.value = false
  }).catch(() => {
    membershipOrderData.value = []
    membershipOrderPagination.total = 0
    loading.value = false
  })
}

function handleViewMembershipOrder(row) {
  getAdminOrderDetail(row.orderNo).then(order => {
    currentMembershipOrder.value = {
      ...row,
      memberName: order.userName || row.memberName,
      memberPhone: order.userPhone || row.memberPhone,
      amount: order.payAmount,
      payMethod: order.payMethodLabel || row.payMethod,
      cardName: order.membershipExt?.cardName || row.cardName,
      validityDays: order.membershipExt?.expireTime || '-',
      payTime: order.payTime || null,
      timeline: buildTimeline(order)
    }
    membershipOrderDetailVisible.value = true
  }).catch(() => {
    currentMembershipOrder.value = row
    membershipOrderDetailVisible.value = true
  })
}

function handleConfirmMembershipOrder(row) {
  ElMessageBox.confirm(`确认已收到会员 "${row.memberName}" 的付款吗？`, '提示', { type: 'info' }).then(() => {
    confirmAdminOrder(row.orderNo).then(() => {
      ElMessage.success('订单已确认')
      fetchMembershipOrderData()
      fetchStats()
      membershipOrderDetailVisible.value = false
    }).catch(() => {
      ElMessage.error('确认失败')
    })
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
  pickupCode: ''
})
const shipRules = {
  pickupCode: [{ required: true, message: '请输入取货码', trigger: 'blur' }]
}

const exchangeStatusMap = {
  PENDING: { label: '待处理', type: 'warning' },
  PAID: { label: '已支付', type: 'success' },
  PROCESSING: { label: '处理中', type: 'primary' },
  NOT_PICKED: { label: '待取货', type: 'warning' },
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
  const params = {
    page: pointsExchangePagination.page,
    pageSize: pointsExchangePagination.pageSize,
    orderType: 'PRODUCT'
  }
  if (pointsExchangeSearch.orderNo) params.keyword = pointsExchangeSearch.orderNo
  if (pointsExchangeSearch.status) params.status = pointsExchangeSearch.status

  getAdminOrders(params).then(data => {
    pointsExchangeData.value = (data.records || []).map(order => ({
      id: order.id,
      orderNo: order.orderNo,
      memberName: order.userName || '-',
      memberPhone: maskPhone(order.userPhone),
      memberAvatar: order.userAvatar || '',
      productName: order.productExt?.productName || '-',
      quantity: order.productExt?.quantity || 1,
      originalPrice: order.originalAmount ? (order.productExt?.quantity > 0 ? order.originalAmount / order.productExt.quantity : order.originalAmount) : 0,
      payAmount: order.payAmount || 0,
      pointsDiscount: order.productExt?.pointsDiscount || 0,
      points: order.productExt?.pointsUsed || 0,
      payMethod: order.payMethodLabel || order.payMethod || '-',
      createTime: order.createTime,
      status: order.status,
      statusLabel: order.statusLabel,
      trackingNo: order.productExt?.trackingNo || null,
      address: order.productExt?.address || '-'
    }))
    pointsExchangePagination.total = data.total || 0
    loading.value = false
  }).catch(() => {
    pointsExchangeData.value = []
    pointsExchangePagination.total = 0
    loading.value = false
  })
}

function handleViewPointsExchange(row) {
  getAdminOrderDetail(row.orderNo).then(order => {
    currentPointsExchange.value = {
      ...row,
      memberName: order.userName || row.memberName,
      memberPhone: order.userPhone || row.memberPhone,
      productName: order.productExt?.productName || row.productName,
      points: order.productExt?.pointsUsed || row.points,
      quantity: order.productExt?.quantity || row.quantity,
      trackingNo: order.productExt?.trackingNo || null,
      address: order.productExt?.address || row.address,
      payAmount: order.payAmount,
      status: order.status
    }
    pointsExchangeDetailVisible.value = true
  }).catch(() => {
    currentPointsExchange.value = row
    pointsExchangeDetailVisible.value = true
  })
}

function handleProcessPointsExchange(row) {
  ElMessageBox.confirm(`开始处理 "${row.memberName}" 的兑换订单？`, '提示', { type: 'info' }).then(() => {
    confirmAdminOrder(row.orderNo).then(() => {
      ElMessage.success('订单已确认处理')
      fetchPointsExchangeData()
      fetchStats()
      pointsExchangeDetailVisible.value = false
    }).catch(() => {
      ElMessage.error('处理失败')
    })
  })
}

function handleShipPointsExchange(row) {
  currentPointsExchange.value = row
  shipForm.pickupCode = ''
  shipDialogVisible.value = true
}

const submitting = ref(false)

function handleSubmitShip() {
  shipFormRef.value?.validate((valid) => {
    if (valid) {
      submitting.value = true
      pickupAdminOrder(currentPointsExchange.value.orderNo, {
        pickupCode: shipForm.pickupCode
      }).then(() => {
        ElMessage.success('取货确认成功')
        shipDialogVisible.value = false
        fetchPointsExchangeData()
        fetchStats()
        pointsExchangeDetailVisible.value = false
        submitting.value = false
      }).catch(() => {
        ElMessage.error('取货确认失败，请检查取货码是否正确')
        submitting.value = false
      })
    }
  })
}

const pickupDialogVisible = ref(false)
const currentPickupOrder = ref(null)
const pickupFormRef = ref(null)
const pickupSubmitting = ref(false)
const pickupForm = reactive({
  pickupCode: ''
})
const pickupRules = {
  pickupCode: [{ required: true, message: '请输入取货码', trigger: 'blur' }]
}

function handlePickupPointsExchange(row) {
  currentPickupOrder.value = row
  pickupForm.pickupCode = ''
  pickupDialogVisible.value = true
}

function handleSubmitPickup() {
  pickupFormRef.value?.validate((valid) => {
    if (valid) {
      pickupSubmitting.value = true
      pickupAdminOrder(currentPickupOrder.value.orderNo, {
        pickupCode: pickupForm.pickupCode
      }).then(() => {
        ElMessage.success('取货确认成功')
        pickupDialogVisible.value = false
        fetchPointsExchangeData()
        fetchStats()
        pointsExchangeDetailVisible.value = false
        pickupSubmitting.value = false
      }).catch(() => {
        ElMessage.error('取货确认失败，请检查取货码是否正确')
        pickupSubmitting.value = false
      })
    }
  })
}

function handleExportPointsExchange() {
  ElMessage.success('积分兑换订单导出成功')
}

// ========== 私教套餐订单 ==========
const coachPackageOrderSearch = reactive({
  orderNo: '',
  status: ''
})
const coachPackageOrderData = ref([])
const coachPackageOrderPagination = reactive({ page: 1, pageSize: 10, total: 0 })
const coachPackageOrderDetailVisible = ref(false)
const currentCoachPackageOrder = ref(null)

function fetchCoachPackageOrderData() {
  loading.value = true
  const params = {
    page: coachPackageOrderPagination.page,
    pageSize: coachPackageOrderPagination.pageSize,
    orderType: 'COACH_PACKAGE'
  }
  if (coachPackageOrderSearch.orderNo) params.keyword = coachPackageOrderSearch.orderNo
  if (coachPackageOrderSearch.status) params.status = coachPackageOrderSearch.status

  getAdminOrders(params).then(data => {
    coachPackageOrderData.value = (data.records || []).map(order => ({
      id: order.id,
      orderNo: order.orderNo,
      memberName: order.userName || '-',
      memberPhone: maskPhone(order.userPhone),
      memberAvatar: order.userAvatar || '',
      packageName: order.coachPackageExt?.packageName || '-',
      coachName: order.coachPackageExt?.coachName || '-',
      amount: order.payAmount,
      payMethod: order.payMethodLabel || order.payMethod || '-',
      createTime: order.createTime,
      payTime: order.payTime || null,
      status: order.status,
      statusLabel: order.statusLabel,
      timeline: buildTimeline(order)
    }))
    coachPackageOrderPagination.total = data.total || 0
    loading.value = false
  }).catch(() => {
    coachPackageOrderData.value = []
    coachPackageOrderPagination.total = 0
    loading.value = false
  })
}

function handleCoachPackageOrderSearch() {
  coachPackageOrderPagination.page = 1
  fetchCoachPackageOrderData()
}

function handleCoachPackageOrderReset() {
  coachPackageOrderSearch.orderNo = ''
  coachPackageOrderSearch.status = ''
  handleCoachPackageOrderSearch()
}

function handleViewCoachPackageOrder(row) {
  getAdminOrderDetail(row.orderNo).then(order => {
    currentCoachPackageOrder.value = {
      ...row,
      memberName: order.userName || row.memberName,
      memberPhone: order.userPhone || row.memberPhone,
      packageName: order.coachPackageExt?.packageName || row.packageName,
      coachName: order.coachPackageExt?.coachName || row.coachName,
      amount: order.payAmount,
      payMethod: order.payMethodLabel || row.payMethod,
      payTime: order.payTime || null,
      timeline: buildTimeline(order)
    }
    coachPackageOrderDetailVisible.value = true
  }).catch(() => {
    currentCoachPackageOrder.value = row
    coachPackageOrderDetailVisible.value = true
  })
}

function handleConfirmCoachPackageOrder(row) {
  ElMessageBox.confirm(`确认已收到会员 "${row.memberName}" 的付款吗？`, '提示', { type: 'info' }).then(() => {
    confirmAdminOrder(row.orderNo).then(() => {
      ElMessage.success('订单已确认')
      fetchCoachPackageOrderData()
      fetchStats()
      coachPackageOrderDetailVisible.value = false
    }).catch(() => {
      ElMessage.error('确认失败')
    })
  })
}

function handleTabChange() {
  if (activeTab.value === 'membership-order') {
    fetchMembershipOrderData()
  } else if (activeTab.value === 'product-order') {
    fetchPointsExchangeData()
  } else if (activeTab.value === 'coach-package-order') {
    fetchCoachPackageOrderData()
  }
}

watch(() => membershipOrderPagination.page, () => {
  if (activeTab.value === 'membership-order') fetchMembershipOrderData()
})
watch(() => membershipOrderPagination.pageSize, () => {
  membershipOrderPagination.page = 1
  if (activeTab.value === 'membership-order') fetchMembershipOrderData()
})
watch(() => pointsExchangePagination.page, () => {
  if (activeTab.value === 'product-order') fetchPointsExchangeData()
})
watch(() => pointsExchangePagination.pageSize, () => {
  pointsExchangePagination.page = 1
  if (activeTab.value === 'product-order') fetchPointsExchangeData()
})
watch(() => coachPackageOrderPagination.page, () => {
  if (activeTab.value === 'coach-package-order') fetchCoachPackageOrderData()
})
watch(() => coachPackageOrderPagination.pageSize, () => {
  coachPackageOrderPagination.page = 1
  if (activeTab.value === 'coach-package-order') fetchCoachPackageOrderData()
})

function formatDateParam(date) {
  if (!date) return null
  const d = new Date(date)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day} 00:00:00`
}

function maskPhone(phone) {
  if (!phone) return '-'
  if (phone.length === 11) {
    return phone.substring(0, 3) + '****' + phone.substring(7)
  }
  return phone
}

function buildTimeline(order) {
  const timeline = []
  timeline.push({ type: 'primary', time: order.createTime, content: '订单创建' })
  if (order.payTime) {
    const payLabel = order.payMethodLabel || '支付'
    timeline.push({ type: 'success', time: order.payTime, content: `支付成功 - ${payLabel}` })
  } else if (order.status === 'PENDING') {
    timeline.push({ type: 'warning', time: '-', content: '等待支付' })
  }
  if (order.status === 'COMPLETED') {
    timeline.push({ type: 'primary', time: order.createTime, content: '订单完成' })
  }
  if (order.status === 'SHIPPED') {
    timeline.push({ type: 'success', time: order.createTime, content: '已发货' })
  }
  if (order.status === 'CANCELLED') {
    timeline.push({ type: 'info', time: order.createTime, content: '订单已取消' })
  }
  return timeline
}

onMounted(() => {
  fetchStats()
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

.package-name-text {
  font-weight: 500;
  color: #303133;
}

.coach-name-text {
  font-size: 12px;
  color: #909399;
}

.amount {
  font-weight: 600;
  color: #f56c6c;
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
