import request from '../utils/request'

export function getPublicKey() {
  return request.get('/api/security/public-key')
}

export function registerUser(data) {
  return request.post('/api/user/register', data)
}

export function loginUser(data) {
  return request.post('/api/user/login', data)
}

export function getUserById(id) {
  return request.get(`/api/user/${id}`)
}

export function updateUser(id, data) {
  return request.put(`/api/user/${id}`, data)
}

export function changePassword(id, data) {
  return request.put(`/api/user/${id}/password`, data)
}
