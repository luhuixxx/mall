import request from '../utils/request'

export function addCartItem(data) {
  return request.post('/api/shoppingcart/item', data)
}

export function updateCartItem(id, data) {
  return request.put(`/api/shoppingcart/item/${id}`, data)
}

export function deleteCartItem(id) {
  return request.delete(`/api/shoppingcart/item/${id}`)
}

export function listCartItems(userId) {
  return request.get('/api/shoppingcart/items', { params: { userId } })
}

export function clearCart(userId) {
  return request.delete('/api/shoppingcart/clear?userId=' + userId)
}