import request from '@/utils/request'

/**
 * 获取仪表盘统计数据
 * @returns {Promise} 统计数据
 */
export function getDashboardStats() {
  return request({
    url: '/dashboard/stats',
    method: 'get'
  })
}

/**
 * 获取会员卡销量统计
 * @returns {Promise} 会员卡销量统计
 */
export function getMemberCardStats() {
  return request({
    url: '/dashboard/member-cards',
    method: 'get'
  })
}

/**
 * 获取到店高峰时间
 * @returns {Promise} 高峰时间列表
 */
export function getPeakHours() {
  return request({
    url: '/dashboard/peak-hours',
    method: 'get'
  })
}

/**
 * 获取课程统计
 * @returns {Promise} 课程统计列表
 */
export function getCourseStats() {
  return request({
    url: '/dashboard/course-stats',
    method: 'get'
  })
}

/**
 * 获取营收趋势数据
 * @param {Object} params - 查询参数
 * @param {string} params.range - 时间范围：today/week/month/year
 * @returns {Promise} 营收趋势数据
 */
export function getRevenueTrend(params) {
  return request({
    url: '/dashboard/revenue-trend',
    method: 'get',
    params
  })
}

/**
 * 获取用户增长趋势
 * @param {Object} params - 查询参数
 * @param {string} params.range - 时间范围：today/week/month/year
 * @returns {Promise} 用户增长数据
 */
export function getUserGrowth(params) {
  return request({
    url: '/dashboard/user-growth',
    method: 'get',
    params
  })
}

/**
 * 获取器材使用状态统计
 * @returns {Promise} 器材状态数据
 */
export function getEquipmentStatus() {
  return request({
    url: '/dashboard/equipment-status',
    method: 'get'
  })
}

/**
 * 获取报修处理统计
 * @returns {Promise} 报修统计数据
 */
export function getRepairStats() {
  return request({
    url: '/dashboard/repair-stats',
    method: 'get'
  })
}

/**
 * 获取热门课程排行榜
 * @param {Object} params - 查询参数
 * @param {number} params.limit - 限制数量
 * @returns {Promise} 热门课程列表
 */
export function getHotCourses(params) {
  return request({
    url: '/dashboard/hot-courses',
    method: 'get',
    params
  })
}

/**
 * 生成AI分析报告
 * @param {Object} data - 分析请求参数
 * @param {string} data.analysisType - 分析类型：MEMBER/COURSE/EQUIPMENT/OVERALL
 * @returns {Promise} 分析报告
 */
export function generateAnalysis(data) {
  return request({
    url: '/dashboard/analysis',
    method: 'post',
    data
  })
}
