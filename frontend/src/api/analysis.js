import request from '@/utils/request'

/**
 * 保存AI分析报告
 * @param {Object} data - 报告数据
 * @returns {Promise}
 */
export function saveAnalysisReport(data) {
  return request({
    url: '/analysis-reports',
    method: 'post',
    data
  })
}

/**
 * 获取报告列表
 * @param {Object} params - 查询参数 { page, size, type, timeFilter }
 * @returns {Promise}
 */
export function getAnalysisReportList(params) {
  return request({
    url: '/analysis-reports',
    method: 'get',
    params
  })
}

/**
 * 获取报告详情
 * @param {number} id - 报告ID
 * @returns {Promise}
 */
export function getAnalysisReportDetail(id) {
  return request({
    url: `/analysis-reports/${id}`,
    method: 'get'
  })
}

/**
 * 删除报告
 * @param {number} id - 报告ID
 * @returns {Promise}
 */
export function deleteAnalysisReport(id) {
  return request({
    url: `/analysis-reports/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除报告
 * @param {Array} ids - 报告ID数组
 * @returns {Promise}
 */
export function batchDeleteAnalysisReports(ids) {
  return request({
    url: '/analysis-reports/batch',
    method: 'delete',
    data: ids
  })
}
