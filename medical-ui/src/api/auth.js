import request from './request'

/**
 * 获取图形验证码
 */
export function getCaptcha() {
  return request.get('/auth/captcha')
}

/**
 * 用户登录
 * @param {Object} data - { username, password, captchaKey, captcha }
 */
export function login(data) {
  return request.post('/auth/login', data)
}

/**
 * 退出登录
 */
export function logout() {
  return request.post('/auth/logout')
}

/**
 * 获取当前用户信息
 */
export function getUserInfo() {
  return request.get('/auth/userInfo')
}

/**
 * 修改密码
 */
export function updatePassword(oldPassword, newPassword) {
  return request.put('/auth/password', null, {
    params: { oldPassword, newPassword }
  })
}
