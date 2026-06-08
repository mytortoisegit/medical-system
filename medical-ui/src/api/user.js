import request from './request'

export function getUserPage(params) {
  return request.get('/user/page', { params })
}
export function getUserById(id) {
  return request.get(`/user/${id}`)
}
export function addUser(data) {
  return request.post('/user', data)
}
export function updateUser(data) {
  return request.put('/user', data)
}
export function deleteUser(id) {
  return request.delete(`/user/${id}`)
}
export function resetPassword(id) {
  return request.put(`/user/resetPassword/${id}`)
}
