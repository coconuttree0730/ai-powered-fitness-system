import request from '@/utils/request'

export function polishDescription(data) {
  return request({
    url: '/ai/polish',
    method: 'post',
    data,
    timeout: 30000
  })
}
