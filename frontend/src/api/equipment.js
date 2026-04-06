import request from '@/utils/request'

export function getEquipmentList(params) {
  return request({
    url: '/equipment/list',
    method: 'get',
    params
  })
}

/**
 * 获取首页展示的器材数据
 * @returns {Promise} 首页器材数据
 */
export function getHomePageEquipments() {
  return request({
    url: '/equipment/homepage',
    method: 'get'
  })
}

export function getEquipmentDetail(id) {
  return request({
    url: `/equipment/${id}`,
    method: 'get'
  })
}

export function createEquipment(data) {
  return request({
    url: '/admin/equipment',
    method: 'post',
    data
  })
}

export function updateEquipment(id, data) {
  return request({
    url: `/admin/equipment/${id}`,
    method: 'put',
    data
  })
}

export function deleteEquipment(id) {
  return request({
    url: `/admin/equipment/${id}`,
    method: 'delete'
  })
}

// 报修相关接口

/**
 * 提交报修申请
 * @param {Object} data 报修数据 { description, imageUrls }
 * @returns {Promise} 提交结果
 */
export function submitRepair(data) {
  return request({
    url: '/repairs',
    method: 'post',
    data
  })
}

/**
 * 获取我的报修记录
 * @returns {Promise} 报修记录列表
 */
export function getMyRepairs() {
  return request({
    url: '/repairs/my',
    method: 'get'
  })
}

/**
 * 获取报修详情
 * @param {number} repairId 报修ID
 * @returns {Promise} 报修详情
 */
export function getRepairDetail(repairId) {
  return request({
    url: `/repairs/${repairId}`,
    method: 'get'
  })
}

/**
 * 取消报修
 * @param {number} repairId 报修ID
 * @returns {Promise} 取消结果
 */
export function cancelRepair(repairId) {
  return request({
    url: `/repairs/${repairId}/cancel`,
    method: 'put'
  })
}

/**
 * 获取所有报修记录（管理员）
 * @returns {Promise} 报修记录列表
 */
export function getAllRepairs() {
  return request({
    url: '/admin/equipment/repairs',
    method: 'get'
  })
}

/**
 * 处理报修（管理员）
 * @param {number} repairId 报修ID
 * @param {Object} data 处理数据 { status, remark }
 * @returns {Promise} 处理结果
 */
export function handleRepair(repairId, data) {
  return request({
    url: `/admin/equipment/repairs/${repairId}`,
    method: 'put',
    data
  })
}

/**
 * 添加处理记录（管理员）
 * @param {number} repairId 报修ID
 * @param {string} content 处理内容
 * @returns {Promise} 添加结果
 */
export function addRepairRecord(repairId, content) {
  return request({
    url: `/admin/equipment/repairs/${repairId}/records`,
    method: 'post',
    params: { content }
  })
}

/**
 * 获取报修处理记录（管理员）
 * @param {number} repairId 报修ID
 * @returns {Promise} 处理记录列表
 */
export function getRepairRecords(repairId) {
  return request({
    url: `/admin/equipment/repairs/${repairId}/records`,
    method: 'get'
  })
}

/**
 * 删除报修记录（管理员）
 * @param {number} repairId 报修ID
 * @returns {Promise} 删除结果
 */
export function deleteRepair(repairId) {
  return request({
    url: `/admin/equipment/repairs/${repairId}`,
    method: 'delete'
  })
}

export function getEquipmentTypes() {
  return request({
    url: '/admin/equipment/types',
    method: 'get'
  })
}

/**
 * 获取指定器材的报修记录
 * @param {number} id 器材ID
 * @returns {Promise} 报修记录列表
 */
export function getEquipmentRepairs(id) {
  return request({
    url: `/equipment/${id}/repairs`,
    method: 'get'
  })
}

/**
 * 提交器械报修（兼容旧接口）
 * @param {Object} data 报修数据
 * @returns {Promise} 提交结果
 */
export function reportEquipmentRepair(data) {
  return request({
    url: '/repairs',
    method: 'post',
    data
  })
}
