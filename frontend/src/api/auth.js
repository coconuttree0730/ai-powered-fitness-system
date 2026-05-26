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

export function getSliderVerifyToken() {
  return request({
    url: '/auth/slider-verify/token',
    method: 'get'
  })
}

export function verifySlider(data) {
  return request({
    url: '/auth/slider-verify/verify',
    method: 'post',
    data
  })
}

export function sendSmsCode(data) {
  return request({
    url: '/auth/sms-code',
    method: 'post',
    data
  })
}

export function loginBySms(data) {
  return request({
    url: '/auth/login/sms',
    method: 'post',
    data
  })
}

export function refreshAccessToken(refreshToken) {
  return request({
    url: '/auth/refresh',
    method: 'post',
    data: { refreshToken }
  })
}

export function logout(refreshToken) {
  return request({
    url: '/auth/logout',
    method: 'post',
    data: refreshToken ? { refreshToken } : {}
  })
}
