import request from '@/utils/request'

export function getAdminCoachPackages(params) {
  return request({
    url: '/admin/coach-packages',
    method: 'get',
    params
  })
}

export function createAdminCoachPackage(coachId, data) {
  return request({
    url: '/admin/coach-packages',
    method: 'post',
    params: { coachId },
    data
  })
}

export function updateAdminCoachPackage(id, data) {
  return request({
    url: `/admin/coach-packages/${id}`,
    method: 'put',
    data
  })
}

export function updateAdminCoachPackageStatus(id, status) {
  return request({
    url: `/admin/coach-packages/${id}/status`,
    method: 'put',
    data: { status }
  })
}

export function deleteAdminCoachPackage(id) {
  return request({
    url: `/admin/coach-packages/${id}`,
    method: 'delete'
  })
}