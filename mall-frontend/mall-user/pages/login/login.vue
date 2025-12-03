<template>
  <view class="login-page">
    <u-navbar title="用户登录" placeholder></u-navbar>
    <view class="card">
      <u-tabs :list="tabs" :current="tabIndex" @change="tabIndex = $event.index" lineColor="#2979ff"></u-tabs>
      <view v-if="tabIndex === 0" class="form-wrap">
        <u-form :model="loginForm" ref="loginFormRef" label-position="left" label-width="150rpx">
          <u-form-item label="用户名" required>
            <u-input v-model="loginForm.username" placeholder="请输入用户名"></u-input>
          </u-form-item>
          <u-form-item label="密码" required>
            <u-input v-model="loginForm.password" type="password" placeholder="请输入密码"></u-input>
          </u-form-item>
          <u-button type="primary" text="登录" @click="onLogin" :loading="loading"></u-button>
        </u-form>
      </view>
      <view v-else class="form-wrap">
        <u-form :model="registerForm" ref="registerFormRef" label-position="left" label-width="150rpx">
          <u-form-item label="昵称" required>
            <u-input v-model="registerForm.name" placeholder="请输入昵称"></u-input>
          </u-form-item>
          <u-form-item label="用户名" required>
            <u-input v-model="registerForm.username" placeholder="请输入用户名"></u-input>
          </u-form-item>
          <u-form-item label="密码" required>
            <u-input v-model="registerForm.password" type="password" placeholder="请输入密码"></u-input>
          </u-form-item>
          <u-form-item label="邮箱" required>
            <u-input v-model="registerForm.email" placeholder="请输入邮箱"></u-input>
          </u-form-item>
          <u-button type="success" text="注册并登录" @click="onRegister" :loading="loading"></u-button>
        </u-form>
      </view>
    </view>
  </view>
</template>

<script>
import { useAuthStore } from '../../store/auth'
export default {
  data() {
    return {
      tabs: [{ name: '登录' }, { name: '注册' }],
      tabIndex: 0,
      loading: false,
      loginForm: { username: '', password: '' },
      registerForm: { name: '', username: '', password: '', email: '' }
    }
  },
  methods: {
    async onLogin() {
      if (!this.loginForm.username || !this.loginForm.password) {
        uni.showToast({ title: '请输入用户名和密码', icon: 'none' })
        return
      }
      this.loading = true
      try {
        const auth = useAuthStore()
        await auth.login({ username: this.loginForm.username, password: this.loginForm.password })
        uni.showToast({ title: '登录成功', icon: 'success' })
        setTimeout(() => {
          uni.reLaunch({ url: '/pages/index/index' })
        }, 300)
      } catch (e) {
        uni.showToast({ title: e?.message || '登录失败', icon: 'none' })
      } finally {
        this.loading = false
      }
    },
    async onRegister() {
      const { name, username, password, email } = this.registerForm
      if (!name || !username || !password || !email) {
        uni.showToast({ title: '请完整填写注册信息', icon: 'none' })
        return
      }
      this.loading = true
      try {
        const auth = useAuthStore()
        await auth.register({ name, username, password, email })
        uni.showToast({ title: '注册成功', icon: 'success' })
        // 自动登录
        await auth.login({ username, password })
        setTimeout(() => {
          uni.reLaunch({ url: '/pages/index/index' })
        }, 300)
      } catch (e) {
        uni.showToast({ title: e?.message || '注册失败', icon: 'none' })
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style>
.login-page { padding: 20rpx; }
.card { max-width: 700rpx; margin: 40rpx auto; padding: 30rpx; background: #fff; border-radius: 16rpx; box-shadow: 0 8rpx 24rpx rgba(0,0,0,0.06); }
.form-wrap { margin-top: 30rpx; }
::v-deep .u-form-item__label { white-space: nowrap; }
</style>
