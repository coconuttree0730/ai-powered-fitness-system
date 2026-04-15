import request from '@/utils/request'

/**
 * ==================== 数据字典管理 API ====================
 */

/**
 * 获取字典列表
 */
export function getDictList() {
  return request({
    url: '/admin/dict/list',
    method: 'get'
  })
}

/**
 * 获取字典详情
 */
export function getDictDetail(id) {
  return request({
    url: `/admin/dict/${id}`,
    method: 'get'
  })
}

/**
 * 根据编码获取字典（含字典项）
 */
export function getDictByCode(dictCode) {
  return request({
    url: `/admin/dict/code/${dictCode}`,
    method: 'get'
  })
}

/**
 * 创建字典
 */
export function createDict(data) {
  return request({
    url: '/admin/dict',
    method: 'post',
    data
  })
}

/**
 * 更新字典
 */
export function updateDict(id, data) {
  return request({
    url: `/admin/dict/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除字典
 */
export function deleteDict(id) {
  return request({
    url: `/admin/dict/${id}`,
    method: 'delete'
  })
}

/**
 * 获取字典选项列表（用于下拉选择）
 * @param {string} dictCode 字典编码，如：membership_card_type, knowledge_category
 */
export function getDictOptions(dictCode) {
  return request({
    url: `/admin/dict/options/${dictCode}`,
    method: 'get'
  })
}
