import { defineStore } from 'pinia'
import { getPublicKey, loginEmployee } from '../api/employee'
import JSEncrypt from 'jsencrypt'
import request from '../utils/request'

const storageKeys = {
  token: 'mall_admin_token',
  employee: 'mall_admin_employee',
  roles: 'mall_admin_roles',
  permissions: 'mall_admin_permissions'
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: '',
    employee: null,
    roles: [],
    permissions: [],
    publicKey: ''
  }),
  actions: {
    init() {
      try {
        const t = uni.getStorageSync(storageKeys.token)
        const e = uni.getStorageSync(storageKeys.employee)
        const r = uni.getStorageSync(storageKeys.roles)
        const p = uni.getStorageSync(storageKeys.permissions)
        if (t) this.token = t
        if (e) this.employee = e
        if (r) this.roles = r
        if (p) this.permissions = p
      } catch (e) {}
    },
    async fetchPublicKey() {
      const key = await getPublicKey()
      this.publicKey = key
      return key
    },
    async login({ username, password }) {
      if (!this.publicKey) {
        await this.fetchPublicKey()
      }
      const enc = new JSEncrypt()
      enc.setPublicKey(this.publicKey)
      const encrypted = enc.encrypt(password) || ''
      const data = await loginEmployee({ username, password: encrypted })
      this.token = data.token || ''
      this.employee = data.employee || null
      this.roles = data.roles || []
      this.permissions = data.permissions || []
      try {
        uni.setStorageSync(storageKeys.token, this.token)
        uni.setStorageSync(storageKeys.employee, this.employee)
        uni.setStorageSync(storageKeys.roles, this.roles)
        uni.setStorageSync(storageKeys.permissions, this.permissions)
      } catch (e) {}
      return data
    },
    logout() {
      this.token = ''
      this.employee = null
      this.roles = []
      this.permissions = []
      this.publicKey = ''
      try {
        uni.removeStorageSync(storageKeys.token)
        uni.removeStorageSync(storageKeys.employee)
        uni.removeStorageSync(storageKeys.roles)
        uni.removeStorageSync(storageKeys.permissions)
      } catch (e) {}
      // headers 由请求封装按需附加，无需手动移除
    }
  }
})
