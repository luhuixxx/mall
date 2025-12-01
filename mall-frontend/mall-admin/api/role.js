import request from '../utils/request'

export function createRole(payload) {
  return request.post('/api/role', payload)
}

export function updateRole(id, payload) {
  return request.put(`/api/role/${id}`, payload)
}

export function deleteRole(id) {
  return request.delete(`/api/role/${id}`)
}

export function getRole(id) {
  return request.get(`/api/role/${id}`)
}

export function listRole(params) {
  return request.get('/api/role', { params })
}

export function setRolePermissions(id, payload) {
  return request.post(`/api/role/${id}/permissions`, payload)
}

export function getRolePermissions(id) {
  return request.get(`/api/role/${id}/permissions`)
}

export function getAllPermissions() {
  return request.get('/api/role/permissions')
}

export function assignRoles(payload) {
  return request.post('/api/role/assign', payload)
}

export function getEmployeePermissions(employeeId) {
  return request.get(`/api/role/employee/${employeeId}/permissions`)
}
