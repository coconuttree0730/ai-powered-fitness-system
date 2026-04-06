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
  return fetch('/api/v1/chat/messages/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${localStorage.getItem('token')}`
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
 * 生成健身计划
 * @param {Object} data - 计划参数
 * @param {string} data.goal - 健身目标
 * @param {string} data.bodyPart - 训练部位
 * @param {string} data.experience - 健身经验
 */
export function generateFitnessPlan(data) {
  return request({
    url: '/chat/fitness-plan/generate',
    method: 'post',
    params: data
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
