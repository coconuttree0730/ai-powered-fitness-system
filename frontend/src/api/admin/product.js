import request from '@/utils/request'

// 获取商品列表
export function getAdminProducts(params) {
  return request({
    url: '/admin/products',
    method: 'get',
    params
  })
}

// 创建商品
export function createAdminProduct(data) {
  return request({
    url: '/admin/products',
    method: 'post',
    data
  })
}

// 更新商品
export function updateAdminProduct(id, data) {
  return request({
    url: `/admin/products/${id}`,
    method: 'put',
    data
  })
}

// 删除商品
export function deleteAdminProduct(id) {
  return request({
    url: `/admin/products/${id}`,
    method: 'delete'
  })
}

// 更新商品状态
export function updateAdminProductStatus(id, status) {
  return request({
    url: `/admin/products/${id}/status`,
    method: 'put',
    params: { status }
  })
}

// 更新商品库存
export function updateAdminProductStock(id, data) {
  return request({
    url: `/admin/products/${id}/stock`,
    method: 'put',
    data
  })
}
