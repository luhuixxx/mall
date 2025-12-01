import request from '../utils/request'

export function getPublicKey() {
  return request.get('/api/security/public-key')
}

export function loginEmployee(payload) {
  return request.post('/api/employee/login', payload)
}
