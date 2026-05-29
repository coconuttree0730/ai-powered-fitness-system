import request from '@/utils/request'
import { getDictOptions } from '@/api/dict'

/**
 * ==================== 会员卡管理 API ====================
 */

/**
 * 获取会员卡列表（上架的）
 * @returns {Promise} 会员卡列表
 */
export function getMembershipCardList() {
  return request({
    url: '/membership/cards',
    method: 'get'
  })
}

/**
 * 获取推荐会员卡
 * @param {number} limit 数量限制，默认4
 * @returns {Promise} 推荐会员卡列表
 */
export function getRecommendCards(limit = 4) {
  return request({
    url: '/membership/cards/recommend',
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取会员卡详情
 * @param {number} id 会员卡ID
 * @returns {Promise} 会员卡详情
 */
export function getMembershipCardDetail(id) {
  return request({
    url: `/membership/cards/${id}`,
    method: 'get'
  })
}

/**
 * ==================== 会员卡订单 API ====================
 */

/**
 * 创建会员卡订单
 * @param {Object} data 订单信息 { cardId, remark }
 * @returns {Promise} 创建的订单信息
 */
export function createMembershipOrder(data) {
  return request({
    url: '/membership/orders',
    method: 'post',
    data
  })
}

/**
 * 支付订单（支付宝）
 * @param {Object} data 支付信息 { orderNo, payMethod }
 * @returns {Promise} 支付表单HTML
 */
export function payMembershipOrder(data) {
  return request({
    url: '/membership/orders/pay',
    method: 'post',
    data
  })
}

/**
 * 获取订单详情
 * @param {string} orderNo 订单编号
 * @returns {Promise} 订单详情
 */
export function getMembershipOrderDetail(orderNo) {
  return request({
    url: `/membership/orders/${orderNo}`,
    method: 'get'
  })
}

/**
 * 获取我的全部订单列表（统一接口，含会员卡/商品/私教课）
 * @returns {Promise} 订单列表
 */
export function getMyAllOrders() {
  return request({
    url: '/orders',
    method: 'get'
  })
}

export function payUnifiedOrder(orderNo, payMethod = 'ALIPAY') {
  return request({
    url: `/orders/${orderNo}/pay`,
    method: 'post',
    params: { payMethod }
  })
}

export function getUnifiedOrderDetail(orderNo) {
  return request({
    url: `/orders/${orderNo}`,
    method: 'get'
  })
}

/**
 * 获取我的订单列表
 * @returns {Promise} 订单列表
 */
export function getMyMembershipOrders() {
  return request({
    url: '/membership/orders/my',
    method: 'get'
  })
}

/**
 * 取消订单
 * @param {string} orderNo 订单编号
 * @returns {Promise} 取消结果
 */
export function cancelMembershipOrder(orderNo) {
  return request({
    url: `/membership/orders/${orderNo}/cancel`,
    method: 'post'
  })
}

/**
 * ==================== 用户会员信息 API ====================
 */

/**
 * 获取我的会员信息
 * @returns {Promise} 会员信息
 */
export function getMyMembership() {
  return request({
    url: '/membership/my',
    method: 'get'
  })
}

/**
 * 检查会员是否有效
 * @returns {Promise<boolean>} 是否有效
 */
export function checkMembershipValid() {
  return request({
    url: '/membership/check',
    method: 'get'
  })
}

/**
 * 购卡前校验
 * @param {number} cardId 会员卡ID
 * @returns {Promise} { hasExistingMembership, membershipType, remainingDays, warningMessage }
 */
export function checkPurchaseEligibility(cardId) {
  return request({
    url: `/membership/cards/${cardId}/purchase-check`,
    method: 'get'
  })
}

/**
 * ==================== 支付宝支付工具函数 ====================
 */

/**
 * 提交支付宝支付表单
 * @param {string} payForm 支付宝返回的HTML表单
 */
export function submitAlipayForm(payForm) {
  if (!payForm) {
    console.error('支付宝支付表单为空')
    return
  }

  try {
    const div = document.createElement('div')
    div.innerHTML = payForm
    document.body.appendChild(div)

    const form = div.querySelector('form')
    if (form) {
      form.submit()
    } else {
      document.write(payForm)
    }
  } catch (error) {
    console.error('支付宝支付表单提交失败:', error)
  }
}

/**
 * ==================== 管理端会员卡管理 API ====================
 */

/**
 * 获取会员卡列表（管理端）- 不分页
 * @returns {Promise} 会员卡列表
 */
export function getAdminCardList() {
  return request({
    url: '/admin/membership/cards',
    method: 'get'
  })
}

/**
 * 获取会员卡分页列表（管理端）
 * @param {Object} params 查询参数 { name, typeCode, status, page, pageSize }
 * @returns {Promise} 分页列表
 */
export function getAdminCardPage(params = {}) {
  return request({
    url: '/admin/membership/cards/page',
    method: 'get',
    params
  })
}

/**
 * 获取会员卡详情（管理端）
 * @param {number} id 会员卡ID
 * @returns {Promise} 会员卡详情
 */
export function getAdminCardDetail(id) {
  return request({
    url: `/admin/membership/cards/${id}`,
    method: 'get'
  })
}

/**
 * 创建会员卡（管理端）
 * @param {Object} data 会员卡信息
 * @returns {Promise} 创建结果
 */
export function createAdminCard(data) {
  return request({
    url: '/admin/membership/cards',
    method: 'post',
    data
  })
}

/**
 * 更新会员卡（管理端）
 * @param {number} id 会员卡ID
 * @param {Object} data 会员卡信息
 * @returns {Promise} 更新结果
 */
export function updateAdminCard(id, data) {
  return request({
    url: `/admin/membership/cards/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除会员卡（管理端）
 * @param {number} id 会员卡ID
 * @returns {Promise} 删除结果
 */
export function deleteAdminCard(id) {
  return request({
    url: `/admin/membership/cards/${id}`,
    method: 'delete'
  })
}

/**
 * 更新会员卡状态（上架/下架）
 * @param {number} id 会员卡ID
 * @param {string} status 状态 ACTIVE/INACTIVE
 * @returns {Promise} 更新结果
 */
export function updateCardStatus(id, status) {
  return request({
    url: `/admin/membership/cards/${id}/status`,
    method: 'put',
    data: { status }
  })
}

/**
 * 获取会员卡类型列表（从字典获取）
 * @returns {Promise} 类型列表
 */
export function getCardTypeList() {
  return getDictOptions('membership_card_type')
}

/**
 * 创建会员卡类型
 * @param {Object} data 类型信息
 * @returns {Promise} 创建结果
 */
export function createCardType(data) {
  return request({
    url: '/admin/membership/card-types',
    method: 'post',
    data
  })
}

/**
 * 获取会员卡钱包信息
 * @returns {Promise} { walletStatus, membership, pendingOrder, paidOrder }
 */
export function getCardWallet() {
  return request({
    url: '/membership/card-wallet',
    method: 'get'
  })
}

/**
 * 激活会员卡
 * @param {string} orderNo 订单号
 * @returns {Promise}
 */
export function activateMembership(orderNo) {
  return request({
    url: '/membership/activate',
    method: 'post',
    data: { orderNo }
  })
}

/**
 * 获取统计信息
 * @returns {Promise} 统计数据
 */
export function getMembershipStats() {
  return request({
    url: '/admin/membership/stats',
    method: 'get'
  })
}
