import request from '@/utils/request'

export function getCoachList() {
  return request({
    url: '/coaches/list',
    method: 'get'
  })
}

export function getCoachDetail(id) {
  return request({
    url: `/coaches/${id}`,
    method: 'get'
  })
}
