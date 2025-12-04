import request from '../utils/request'

export function getUser(id) {
  return request.get(`/api/user/${id}`)
}

export function updateUser(id, payload) {
  return request.put(`/api/user/${id}`, payload)
}

export function listUsers(params) {
  return request.get('/api/user', { params })
}

export function resetUserPassword(id) {
  return request.put(`/api/user/${id}/reset-password`)
}

export function updateUserBalance(id, payload) {
  return request.put(`/api/user/${id}/balance`, payload)
}

export function getUserStatsSummary() {
  return request.get('/api/user/stats/summary')
}
