import request from '@/utils/request'

export function generatePlan(data) {
  return request({
    url: '/plans/generate',
    method: 'post',
    data
  })
}

export function getMyPlans() {
  return request({
    url: '/plans/my',
    method: 'get'
  })
}

export function getPlanDetail(id) {
  return request({
    url: `/plans/${id}`,
    method: 'get'
  })
}

export function deletePlan(id) {
  return request({
    url: `/plans/${id}`,
    method: 'delete'
  })
}

export function getProfile() {
  return request({
    url: '/profile',
    method: 'get'
  })
}

export function updateProfile(data) {
  return request({
    url: '/profile',
    method: 'put',
    data
  })
}
