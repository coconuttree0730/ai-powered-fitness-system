import request from '@/utils/request'

export function createSession() {
  return request({
    url: '/chat/sessions',
    method: 'post'
  })
}

export function sendMessage(data) {
  return request({
    url: '/chat/messages',
    method: 'post',
    data
  })
}

export function sendMessageStream(data, signal) {
  // 同时检查 localStorage 和 sessionStorage
  const token = localStorage.getItem('token') || sessionStorage.getItem('token')
  return fetch('/api/v1/chat/messages/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify(data),
    signal
  })
}

export function getUserSessions() {
  return request({
    url: '/chat/sessions',
    method: 'get'
  })
}

export function getSessionDetail(sessionId) {
  return request({
    url: `/chat/sessions/${sessionId}`,
    method: 'get'
  })
}

export function deleteSession(sessionId) {
  return request({
    url: `/chat/sessions/${sessionId}`,
    method: 'delete'
  })
}

export function getSessionMessages(sessionId, params = {}) {
  return request({
    url: `/chat/sessions/${sessionId}/messages`,
    method: 'get',
    params
  })
}

/**
 * 生成健身计划（旧版兼容）
 * @param {Object} data - 计划参数
 * @param {string} data.goal - 健身目标
 * @param {string} data.bodyPart - 训练部位
 * @param {string} data.experience - 健身经验
 */
export function generateFitnessPlan(data) {
  return request({
    url: '/plans/generate',
    method: 'post',
    data
  })
}

/**
 * 从个人档案生成健身计划（返回结构化JSON给前端渲染）
 * 后端自动从用户档案获取身高、体重、目标、经验等数据
 */
export function generateFitnessPlanFromProfile() {
  return request({
    url: '/plans/generate-from-profile',
    method: 'post'
  })
}

/**
 * 保存健身计划
 * @param {Object} data - 计划数据
 */
export function saveFitnessPlan(data) {
  return request({
    url: '/chat/fitness-plan/save',
    method: 'post',
    data
  })
}

/**
 * 获取我的健身计划列表
 */
export function getMyFitnessPlans() {
  return request({
    url: '/chat/fitness-plan/my',
    method: 'get'
  })
}
