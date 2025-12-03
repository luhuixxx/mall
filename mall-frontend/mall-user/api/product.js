import request from '../utils/request'

export function getSpuPage(params) {
  return request.get('/api/product/spu', { params })
}

export function getSkusBySpuId(spuId) {
  return request.get(`/api/product/sku/spu/${spuId}`)
}

