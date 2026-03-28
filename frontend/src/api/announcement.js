import request from '@/utils/request'

/**
 * 获取所有已发布的公告（前台展示）
 * @returns {Promise} 公告列表
 */
export function getPublishedAnnouncements() {
  return request({
    url: '/announcements/published',
    method: 'get'
  })
}

/**
 * 获取所有公告（管理后台使用）
 * @returns {Promise} 公告列表
 */
export function getAllAnnouncements() {
  return request({
    url: '/announcements',
    method: 'get'
  })
}

/**
 * 根据ID获取公告详情
 * @param {number} id 公告ID
 * @returns {Promise} 公告详情
 */
export function getAnnouncementById(id) {
  return request({
    url: `/announcements/${id}`,
    method: 'get'
  })
}

/**
 * 创建公告
 * @param {Object} data 公告信息
 * @returns {Promise} 创建后的公告
 */
export function createAnnouncement(data) {
  return request({
    url: '/announcements',
    method: 'post',
    data
  })
}

/**
 * 更新公告
 * @param {number} id 公告ID
 * @param {Object} data 公告信息
 * @returns {Promise} 更新后的公告
 */
export function updateAnnouncement(id, data) {
  return request({
    url: `/announcements/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除公告
 * @param {number} id 公告ID
 * @returns {Promise} 删除结果
 */
export function deleteAnnouncement(id) {
  return request({
    url: `/announcements/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除公告
 * @param {number[]} ids 公告ID列表
 * @returns {Promise} 删除结果
 */
export function deleteAnnouncements(ids) {
  return request({
    url: '/announcements',
    method: 'delete',
    data: ids
  })
}

/**
 * 发布公告
 * @param {number} id 公告ID
 * @returns {Promise} 发布结果
 */
export function publishAnnouncement(id) {
  return request({
    url: `/announcements/${id}/publish`,
    method: 'patch'
  })
}

/**
 * 下架公告
 * @param {number} id 公告ID
 * @returns {Promise} 下架结果
 */
export function unpublishAnnouncement(id) {
  return request({
    url: `/announcements/${id}/unpublish`,
    method: 'patch'
  })
}

/**
 * 增加浏览量
 * @param {number} id 公告ID
 * @returns {Promise} 结果
 */
export function incrementViewCount(id) {
  return request({
    url: `/announcements/${id}/view`,
    method: 'patch'
  })
}
