import request from '@/utils/request'

/**
 * 获取当前登录教练的详情
 * @returns {Promise}
 */
export function getCurrentCoachDetail() {
  return request({
    url: '/coaches/detail',
    method: 'get'
  })
}

/**
 * 获取指定教练的详情
 * @param {number} id 教练ID
 * @returns {Promise}
 */
export function getCoachDetail(id) {
  return request({
    url: `/coaches/${id}/detail`,
    method: 'get'
  })
}

/**
 * 更新教练详情
 * @param {Object} data 教练详情数据
 * @returns {Promise}
 */
export function updateCoachDetail(data) {
  return request({
    url: '/coaches/detail',
    method: 'put',
    data
  })
}

/**
 * 上传个人展示图片
 * @param {File} file 图片文件
 * @returns {Promise}
 */
export function uploadPersonalImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/coaches/detail/image',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 删除个人展示图片
 * @returns {Promise}
 */
export function deletePersonalImage() {
  return request({
    url: '/coaches/detail/image',
    method: 'delete'
  })
}

/**
 * 更新教练标签
 * @param {string[]} tags 标签列表
 * @returns {Promise}
 */
export function updateTags(tags) {
  return request({
    url: '/coaches/detail/tags',
    method: 'put',
    data: tags
  })
}

/**
 * 获取首页展示的教练列表
 * @param {number} limit 限制数量
 * @returns {Promise}
 */
export function getHomePageCoaches(limit = 4) {
  return request({
    url: '/coaches/home',
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取当前登录会员的专属教练
 * @returns {Promise}
 */
export function getMyPrivateCoach() {
  return request({
    url: '/coaches/my-private-coach',
    method: 'get'
  })
}
