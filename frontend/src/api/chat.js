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
