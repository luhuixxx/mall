import { defineStore } from 'pinia'
import JSEncrypt from 'jsencrypt'
import { getPublicKey, loginUser, registerUser, getUserById, updateUser, changePassword } from '../api/user'

const storageKeys = {
  token: 'mall_user_token',
  user: 'mall_user_profile'
}

export const useAuthStore = defineStore('user_auth', {
  state: () => ({
    token: '',
    user: null,
    publicKey: ''
  }),
  actions: {
    init() {
      try {
        const t = uni.getStorageSync(storageKeys.token)
        const u = uni.getStorageSync(storageKeys.user)
        if (t) this.token = t
        if (u) this.user = u
      } catch (e) {}
    },
    async fetchPublicKey() {
      const key = await getPublicKey()
      this.publicKey = key
      return key
    },
    async login({ username, password }) {
      if (!this.publicKey) await this.fetchPublicKey()
      const enc = new JSEncrypt()
      enc.setPublicKey(this.publicKey)
      const encrypted = enc.encrypt(password) || ''
      const token = await loginUser({ username, password: encrypted })
      this.token = token
      try { uni.setStorageSync(storageKeys.token, this.token) } catch (e) {}
      this.user = { username }
      try { uni.setStorageSync(storageKeys.user, this.user) } catch (e) {}
      try {
        const parts = String(token).split('.')
        if (parts.length === 3) {
          let b64 = parts[1].replace(/-/g, '+').replace(/_/g, '/')
          const pad = '='.repeat((4 - (b64.length % 4)) % 4)
          const json = atob(b64 + pad)
          const payload = JSON.parse(json)
          let uid = payload.userId || payload.uid || payload.id
          if (!uid) {
            const sub = payload.sub
            if (typeof sub === 'string') {
              const m = sub.match(/\d+/)
              if (m) uid = m[0]
            } else if (sub) {
              uid = sub
            }
          }
          if (uid) await this.loadUser(uid)
        }
      } catch (e) {}
      return token
    },
    async register({ name, username, password, email }) {
      if (!this.publicKey) await this.fetchPublicKey()
      const enc = new JSEncrypt()
      enc.setPublicKey(this.publicKey)
      const encrypted = enc.encrypt(password) || ''
      const user = await registerUser({ name, username, password: encrypted, email })
      // password should be null in response
      return user
    },
    async loadUser(id) {
      const u = await getUserById(id)
      this.user = u
      try { uni.setStorageSync(storageKeys.user, this.user) } catch (e) {}
      return u
    },
    async updateProfile(id, payload) {
      const u = await updateUser(id, payload)
      this.user = u
      try { uni.setStorageSync(storageKeys.user, this.user) } catch (e) {}
      return u
    },
    async updatePassword(id, { oldPassword, newPassword }) {
      if (!this.publicKey) await this.fetchPublicKey()
      const enc = new JSEncrypt()
      enc.setPublicKey(this.publicKey)
      const oldEnc = enc.encrypt(oldPassword) || ''
      const newEnc = enc.encrypt(newPassword) || ''
      await changePassword(id, { oldPassword: oldEnc, newPassword: newEnc })
      return true
    },
    logout() {
      this.token = ''
      this.user = null
      this.publicKey = ''
      try {
        uni.removeStorageSync(storageKeys.token)
        uni.removeStorageSync(storageKeys.user)
      } catch (e) {}
    }
  }
})
