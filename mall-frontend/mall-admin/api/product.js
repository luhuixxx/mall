import request from '../utils/request'

export function listSpu(params) {
  return request.get('/api/product/spu', { params })
}

export function getSpu(id) {
  return request.get(`/api/product/spu/${id}`)
}

export function createSpu(payload) {
  return request.post('/api/product/spu', payload)
}

export function updateSpu(id, payload) {
  return request.put(`/api/product/spu/${id}`, payload)
}

export function deleteSpu(id) {
  return request.delete(`/api/product/spu/${id}`)
}

export function listSkuBySpu(spuId) {
  return request.get(`/api/product/sku/spu/${spuId}`)
}

export function getSku(id) {
  return request.get(`/api/product/sku/${id}`)
}

export function createSku(payload) {
  return request.post('/api/product/sku', payload)
}

export function updateSku(id, payload) {
  return request.put(`/api/product/sku/${id}`, payload)
}

export function deleteSku(id) {
  return request.delete(`/api/product/sku/${id}`)
}

export function updateSkuStock(id, payload) {
  return request.put(`/api/product/sku/${id}/stock`, payload)
}
