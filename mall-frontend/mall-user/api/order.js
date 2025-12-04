import request from '../utils/request'

export function createOrder(data) {
  return request.post('/api/order', data)
}

export function cancelOrder(id) {
  return request.put(`/api/order/${id}/cancel`)
}

export function payOrder(id, data) {
  return request.put(`/api/order/${id}/pay`, data)
}

export function listUserOrders(userId) {
  return request.get(`/api/order/user/${userId}`)
}

export function getOrderDetails(id) {
  return request.get(`/api/order/${id}`)
}
