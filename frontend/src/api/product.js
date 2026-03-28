import request from '@/utils/request'

// 获取商品列表
export function getProducts(category) {
  return request({
    url: '/products',
    method: 'get',
    params: { category }
  })
}

// 获取商品详情
export function getProductDetail(id) {
  return request({
    url: `/products/${id}`,
    method: 'get'
  })
}

// 计算价格
export function calculatePrice(data) {
  return request({
    url: '/product-orders/calculate',
    method: 'post',
    data
  })
}

// 创建订单
export function createOrder(data) {
  return request({
    url: '/product-orders',
    method: 'post',
    data
  })
}

// 获取订单列表
export function getOrders() {
  return request({
    url: '/product-orders',
    method: 'get'
  })
}

// 获取订单详情
export function getOrderDetail(orderNo) {
  return request({
    url: `/product-orders/${orderNo}`,
    method: 'get'
  })
}

// 支付订单
export function payOrder(orderNo, payMethod) {
  return request({
    url: `/product-orders/${orderNo}/pay`,
    method: 'post',
    params: { payMethod }
  })
}

// 取消订单
export function cancelOrder(orderNo) {
  return request({
    url: `/product-orders/${orderNo}/cancel`,
    method: 'post'
  })
}
