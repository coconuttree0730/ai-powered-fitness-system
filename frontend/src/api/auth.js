import request from '@/utils/request'

export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

export function register(data) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

export function getCurrentUser() {
  return request({
    url: '/users/me',
    method: 'get'
  })
}

export function updatePassword(data) {
  return request({
    url: '/users/password',
    method: 'put',
    data
  })
}

/**
 * 滑块验证 - 获取验证令牌
 */
export function getSliderVerifyToken() {
  return request({
    url: '/auth/slider-verify/token',
    method: 'get'
  })
}

/**
 * 滑块验证 - 验证滑块结果
 * @param {Object} data - 验证数据 {token, sliderValue, timestamp}
 */
export function verifySlider(data) {
  return request({
    url: '/auth/slider-verify/verify',
    method: 'post',
    data
  })
}

/**
 * 发送短信验证码（需要先完成滑块验证）
 * @param {Object} data - {phone, verifyToken}
 */
export function sendSmsCode(data) {
  return request({
    url: '/auth/sms-code',
    method: 'post',
    data
  })
}

/**
 * 短信验证码登录
 * @param {Object} data - {phone, smsCode}
 */
export function loginBySms(data) {
  return request({
    url: '/auth/login/sms',
    method: 'post',
    data
  })
}
