const ACCESS_TOKEN_KEY = 'accessToken'
const REFRESH_TOKEN_KEY = 'refreshToken'

export function getAccessToken() {
  return localStorage.getItem(ACCESS_TOKEN_KEY) || sessionStorage.getItem(ACCESS_TOKEN_KEY) || ''
}

export function getRefreshToken() {
  return localStorage.getItem(REFRESH_TOKEN_KEY) || sessionStorage.getItem(REFRESH_TOKEN_KEY) || ''
}

export function getToken() {
  return getAccessToken()
}

function getActiveStorage() {
  return localStorage.getItem(ACCESS_TOKEN_KEY) ? localStorage : sessionStorage
}

export function saveAccessToken(token) {
  const storage = getActiveStorage()
  storage.setItem(ACCESS_TOKEN_KEY, token)
}

export function removeToken() {
  localStorage.removeItem(ACCESS_TOKEN_KEY)
  localStorage.removeItem(REFRESH_TOKEN_KEY)
  sessionStorage.removeItem(ACCESS_TOKEN_KEY)
  sessionStorage.removeItem(REFRESH_TOKEN_KEY)
}

export function getUploadConfig(folder = 'files') {
  return {
    action: '/api/v1/files/upload',
    headers: { Authorization: 'Bearer ' + getAccessToken() },
    data: { folder }
  }
}