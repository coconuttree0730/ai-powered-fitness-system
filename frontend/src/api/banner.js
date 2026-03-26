import request from '@/utils/request'

/**
 * 获取所有显示的轮播图（公开接口）
 * @returns {Promise} 轮播图列表
 */
export function getActiveBanners() {
  return request({
    url: '/banners/active',
    method: 'get'
  })
}

/**
 * 获取所有轮播图（管理后台使用）
 * @returns {Promise} 轮播图列表
 */
export function getAllBanners() {
  return request({
    url: '/banners',
    method: 'get'
  })
}

/**
 * 根据ID获取轮播图详情
 * @param {number} id 轮播图ID
 * @returns {Promise} 轮播图详情
 */
export function getBannerById(id) {
  return request({
    url: `/banners/${id}`,
    method: 'get'
  })
}

/**
 * 创建轮播图
 * @param {Object} data 轮播图信息
 * @returns {Promise} 创建后的轮播图
 */
export function createBanner(data) {
  return request({
    url: '/banners',
    method: 'post',
    data
  })
}

/**
 * 更新轮播图
 * @param {number} id 轮播图ID
 * @param {Object} data 轮播图信息
 * @returns {Promise} 更新后的轮播图
 */
export function updateBanner(id, data) {
  return request({
    url: `/banners/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除轮播图
 * @param {number} id 轮播图ID
 * @returns {Promise} 删除结果
 */
export function deleteBanner(id) {
  return request({
    url: `/banners/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除轮播图
 * @param {number[]} ids 轮播图ID列表
 * @returns {Promise} 删除结果
 */
export function deleteBanners(ids) {
  return request({
    url: '/banners',
    method: 'delete',
    data: ids
  })
}

/**
 * 更新轮播图状态
 * @param {number} id 轮播图ID
 * @param {number} status 状态：0-隐藏，1-显示
 * @returns {Promise} 更新结果
 */
export function updateBannerStatus(id, status) {
  return request({
    url: `/banners/${id}/status`,
    method: 'patch',
    params: { status }
  })
}
