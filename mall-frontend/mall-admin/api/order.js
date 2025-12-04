import request from '../utils/request'

export function listOrders(params) {
  return request.get('/api/order', { params })
}

export function getOrderDetails(id) {
  return request.get(`/api/order/${id}`)
}

export function cancelOrder(id) {
  return request.put(`/api/order/${id}/cancel`)
}

export function getOrderStatsSummary() {
  return request.get('/api/order/stats/summary')
}

export function getRecentOrders(limit = 10) {
  return request.get('/api/order/recent', { params: { limit } })
}
