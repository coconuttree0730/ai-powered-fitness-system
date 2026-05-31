import request from '@/utils/request'

export function getAdminOrders(params) {
  return request({
    url: '/admin/orders',
    method: 'get',
    params
  })
}

export function getAdminOrderDetail(orderNo) {
  return request({
    url: `/admin/orders/${orderNo}`,
    method: 'get'
  })
}

export function confirmAdminOrder(orderNo) {
  return request({
    url: `/admin/orders/${orderNo}/confirm`,
    method: 'put'
  })
}

export function shipAdminOrder(orderNo, data) {
  return request({
    url: `/admin/orders/${orderNo}/ship`,
    method: 'put',
    data
  })
}

export function getAdminOrderStats() {
  return request({
    url: '/admin/orders/stats',
    method: 'get'
  })
}

export function pickupAdminOrder(orderNo, data) {
  return request({
    url: `/admin/orders/${orderNo}/pickup`,
    method: 'put',
    data
  })
}