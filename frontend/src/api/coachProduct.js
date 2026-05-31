import request from '@/utils/request'

export function getMyPackages() {
  return request({ url: '/coach/coach-packages', method: 'get' })
}

export function createPackage(data) {
  return request({ url: '/coach/coach-packages', method: 'post', data })
}

export function updatePackage(id, data) {
  return request({ url: `/coach/coach-packages/${id}`, method: 'put', data })
}

export function deletePackage(id) {
  return request({ url: `/coach/coach-packages/${id}`, method: 'delete' })
}

export function updatePackageStatus(id, status) {
  return request({ url: `/coach/coach-packages/${id}/status`, method: 'put', data: { status } })
}

// 创建私教套餐订单
export function createCoachPackageOrder(coachPackageId) {
  return request({
    url: `/coach-packages/${coachPackageId}/order`,
    method: 'post'
  })
}

// 支付私教套餐订单
export function payCoachOrder(orderNo, payMethod = 'ALIPAY') {
  return request({
    url: `/orders/${orderNo}/pay`,
    method: 'post',
    params: { payMethod }
  })
}

// 获取订单详情
export function getCoachOrderDetail(orderNo) {
  return request({
    url: `/orders/${orderNo}`,
    method: 'get'
  })
}