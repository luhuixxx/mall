import axios from 'axios'
import { useAuthStore } from '../store/auth'

const instance = axios.create({
  baseURL: 'http://localhost:51800',
  timeout: 15000
})

const whitelist = [ '/api/security/public-key', '/api/employee/public-key', '/api/employee/login']

function handleUnauthorized() {
  try {
    const auth = useAuthStore()
    auth.logout()
    uni.reLaunch({ url: '/pages/login/login' })
  } catch (e) {}
}

instance.interceptors.request.use(config => {
  try {
    const auth = useAuthStore()
    const token = auth.token
    const url = config.url || ''
    if (token && !whitelist.some(p => url.includes(p))) {
      config.headers = config.headers || {}
      config.headers.Authorization = 'Bearer ' + token
    }
  } catch (e) {}
  return config
})

instance.interceptors.response.use(response => {
  const data = response.data
  if (data && typeof data.code !== 'undefined' && data.code !== 0) {
    if (data.code === 401) {
      handleUnauthorized()
    }
    const err = new Error(data.message || 'Request error')
    throw err
  }
  return data && typeof data.data !== 'undefined' ? data.data : data
}, error => {
  if (error && error.response && error.response.status === 401) {
    handleUnauthorized()
  }
  throw error
})

export default instance
