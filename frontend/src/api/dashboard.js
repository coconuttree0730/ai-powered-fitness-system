import request from '@/utils/request'

export function getDashboardStats() {
  return request({
    url: '/dashboard/stats',
    method: 'get'
  })
}

export function getMemberCardStats() {
  return request({
    url: '/dashboard/member-cards',
    method: 'get'
  })
}

export function getPeakHours() {
  return request({
    url: '/dashboard/peak-hours',
    method: 'get'
  })
}

export function getCourseStats() {
  return request({
    url: '/dashboard/course-stats',
    method: 'get'
  })
}

export function generateAnalysis(data) {
  return request({
    url: '/dashboard/analysis',
    method: 'post',
    data
  })
}
