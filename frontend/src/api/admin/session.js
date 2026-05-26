import request from '@/utils/request'

export function getAdminSessions(params) {
  return request({
    url: '/course-sessions/list',
    method: 'get',
    params
  })
}

export function cancelSession(sessionId) {
  return request({
    url: `/course-sessions/${sessionId}/cancel`,
    method: 'put'
  })
}
