import request from '@/utils/request'

export function getUserList(params) {
  return request({
    url: '/admin/users',
    method: 'get',
    params
  })
}

export function createUser(data) {
  return request({
    url: '/admin/users',
    method: 'post',
    data
  })
}

export function updateUser(id, data) {
  return request({
    url: `/admin/users/${id}`,
    method: 'put',
    data
  })
}

export function deleteUser(id) {
  return request({
    url: `/admin/users/${id}`,
    method: 'delete'
  })
}

export function updateUserStatus(id, status) {
  return request({
    url: `/admin/users/${id}/status`,
    method: 'put',
    params: { status }
  })
}

export function resetUserPassword(id, password) {
  return request({
    url: `/admin/users/${id}/password`,
    method: 'put',
    data: { password }
  })
}

export function kickUser(id) {
  return request({
    url: `/admin/users/${id}/kick`,
    method: 'put'
  })
}

// ==================== 会员端用户相关接口 ====================

/**
 * 获取当前登录用户信息
 */
export function getCurrentUser() {
  return request({
    url: '/users/me',
    method: 'get'
  })
}

/**
 * 更新用户名
 * @param {string} username - 新用户名
 */
export function updateUsername(username) {
  return request({
    url: '/users/username',
    method: 'put',
    data: { username }
  })
}

/**
 * 发送验证码到当前绑定的手机号（更换手机号时验证身份）
 */
export function sendOldPhoneCode() {
  return request({
    url: '/users/phone/code/old',
    method: 'post'
  })
}

/**
 * 发送验证码到新手机号
 * @param {string} phone - 新手机号
 */
export function sendNewPhoneCode(phone) {
  return request({
    url: '/users/phone/code/new',
    method: 'post',
    params: { phone }
  })
}

/**
 * 更新手机号
 * @param {Object} data - { phone, code, oldCode }
 */
export function updatePhone(data) {
  return request({
    url: '/users/phone',
    method: 'put',
    data
  })
}

/**
 * 发送邮箱验证码
 * @param {string} email - 邮箱地址
 */
export function sendEmailCode(email) {
  return request({
    url: '/users/email/code',
    method: 'post',
    data: { email }
  })
}

/**
 * 更新邮箱
 * @param {Object} data - { email, code }
 */
export function updateEmail(data) {
  return request({
    url: '/users/email',
    method: 'put',
    data
  })
}

/**
 * 发送修改密码的短信验证码
 */
export function sendPasswordChangeCode() {
  return request({
    url: '/users/password/code',
    method: 'post'
  })
}

/**
 * 修改密码（通过短信验证码）
 * @param {Object} data - { smsCode, newPassword }
 */
export function updatePasswordBySms(data) {
  return request({
    url: '/users/password/by-sms',
    method: 'put',
    data
  })
}

/**
 * 上传用户头像
 * @param {FormData} formData 包含头像文件的 FormData 对象
 * @returns {Promise} 上传结果，返回更新后的用户信息
 */
export function uploadAvatar(formData) {
  return request({
    url: '/users/avatar',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 检查用户名是否已存在
 * @param {string} username - 用户名
 * @returns {Promise} { exists: boolean, available: boolean }
 */
export function checkUsernameExists(username) {
  return request({
    url: '/users/username/check',
    method: 'get',
    params: { username }
  })
}

/**
 * 更新用户昵称
 * @param {string} nickname - 新昵称
 * @returns {Promise} 更新后的用户信息
 */
export function updateNickname(nickname) {
  return request({
    url: '/users/nickname',
    method: 'put',
    data: { nickname }
  })
}
