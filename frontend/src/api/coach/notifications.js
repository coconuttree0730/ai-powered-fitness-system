import request from '@/utils/request'

export function getUnreadCount() {
  return request.get('/coach/notifications/unread-count')
}

export function getNotifications() {
  return request.get('/coach/notifications')
}

export function markAsRead(id) {
  return request.put(`/coach/notifications/${id}/read`)
}