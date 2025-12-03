import { useAuthStore } from '../store/auth'

const BASE_URL = 'http://localhost:52000'
const TIMEOUT = 15000
const whitelist = ['/api/security/public-key', '/api/user/register', '/api/user/login']

function toQuery(params = {}) {
  const entries = Object.entries(params).filter(([_, v]) => v !== undefined && v !== null && v !== '')
  if (!entries.length) return ''
  return '?' + entries.map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`).join('&')
}

function isWhitelisted(url) {
  return whitelist.some(p => url.includes(p))
}

function handleUnauthorized() {
  try {
    const auth = useAuthStore()
    auth.logout()
    uni.reLaunch({ url: '/pages/login/login' })
  } catch (e) {}
}

function request(method, url, data = undefined, options = {}) {
  const { params, headers } = options
  const fullUrl = BASE_URL + url + (method === 'GET' && params ? toQuery(params) : '')
  const auth = useAuthStore()
  const token = auth?.token
  const finalHeaders = Object.assign({ 'Content-Type': 'application/json' }, headers || {})
  if (token && !isWhitelisted(url)) {
    finalHeaders['Authorization'] = 'Bearer ' + token
  }
  return new Promise((resolve, reject) => {
    const timer = setTimeout(() => {}, TIMEOUT)
    uni.request({
      url: fullUrl,
      method,
      header: finalHeaders,
      data: method === 'GET' ? undefined : data,
      success: (res) => {
        clearTimeout(timer)
        const status = res.statusCode
        const body = res.data
        if (status === 401) {
          handleUnauthorized()
          const err = new Error('Unauthorized')
          err.response = { status: 401, data: body }
          reject(err)
          return
        }
        if (body && typeof body.code !== 'undefined' && body.code !== 0) {
          if (body.code === 401) handleUnauthorized()
          const err = new Error(body.message || 'Request error')
          err.response = { status: status, data: body }
          reject(err)
          return
        }
        resolve(body && typeof body.data !== 'undefined' ? body.data : body)
      },
      fail: (err) => {
        clearTimeout(timer)
        reject(err)
      }
    })
  })
}

export default {
  get(url, options) { return request('GET', url, undefined, options) },
  post(url, data, options) { return request('POST', url, data, options) },
  put(url, data, options) { return request('PUT', url, data, options) },
  delete(url, data, options) { return request('DELETE', url, data, options) }
}

export function getBaseUrl() { return BASE_URL }
