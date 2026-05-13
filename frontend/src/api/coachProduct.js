import request from '@/utils/request'

export function getMyPackages() {
  return request({ url: '/coach/products', method: 'get' })
}

export function createPackage(data) {
  return request({ url: '/coach/products', method: 'post', data })
}

export function updatePackage(id, data) {
  return request({ url: `/coach/products/${id}`, method: 'put', data })
}

export function deletePackage(id) {
  return request({ url: `/coach/products/${id}`, method: 'delete' })
}

export function updatePackageStatus(id, status) {
  return request({ url: `/coach/products/${id}/status`, method: 'put', params: { status } })
}