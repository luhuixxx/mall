import request from '../utils/request'

export function getPublicKey() {
  return request.get('/api/security/public-key')
}

export function loginEmployee(payload) {
  return request.post('/api/employee/login', payload)
}

export function registerEmployee(payload) {
  return request.post('/api/employee/register', payload)
}

export function listEmployees(params) {
  return request.get('/api/employee', { params })
}

export function getEmployee(id) {
  return request.get(`/api/employee/${id}`)
}

export function updateEmployee(id, payload) {
  return request.put(`/api/employee/${id}`, payload)
}

export function deleteEmployee(id) {
  return request.delete(`/api/employee/${id}`)
}

export function resetEmployeePassword(id) {
  return request.post(`/api/employee/${id}/reset-password`)
}
