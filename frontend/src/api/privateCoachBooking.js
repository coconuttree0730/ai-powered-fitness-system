import request from '@/utils/request'

export function createPrivateCoachBooking(data) {
  return request({ url: '/private-coach-bookings', method: 'post', data })
}

export function getMyPrivateCoachBookings() {
  return request({ url: '/private-coach-bookings/my', method: 'get' })
}

export function cancelPrivateCoachBooking(id, reason) {
  return request({ url: `/private-coach-bookings/${id}/cancel`, method: 'put', data: { cancelReason: reason } })
}

export function getCoachBookingsByRange(coachId, startDate, endDate) {
  return request({ url: `/private-coach-bookings/coach/${coachId}/by-range`, method: 'get', params: { startDate, endDate } })
}

export function getCoachOwnBookingsByRange(startDate, endDate) {
  return request({ url: '/coach/private-coach-bookings/by-range', method: 'get', params: { startDate, endDate } })
}

export function confirmPrivateCoachBooking(id) {
  return request({ url: `/coach/private-coach-bookings/${id}/confirm`, method: 'put' })
}

export function completePrivateCoachBooking(id) {
  return request({ url: `/coach/private-coach-bookings/${id}/complete`, method: 'put' })
}