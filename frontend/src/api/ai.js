import request from '@/utils/request'

export function polishDescription(data) {
  return request({
    url: '/api/v1/ai/polish',
    method: 'post',
    data,
    timeout: 30000
  })
}
