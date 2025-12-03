<template>
  <view class="login-page">
    <view class="hero">
      <image class="logo" src="/static/logo.png" mode="widthFix"></image>
      <view class="brand">
        <text class="brand-title">Mall 用户登录</text>
        <text class="brand-sub">欢迎回来，开始你的购物之旅</text>
      </view>
    </view>
    <view class="card">
      <u-tabs :list="tabs" :current="tabIndex" @change="tabIndex = $event.index" lineColor="#2979ff"></u-tabs>
      <view v-if="tabIndex === 0" class="form-wrap">
        <u-form :model="loginForm" ref="loginFormRef" label-position="left" label-width="150rpx">
          <u-form-item label="用户名" required>
            <view class="field">
              <u-icon name="account-fill" size="32rpx" color="#999"></u-icon>
              <u-input v-model="loginForm.username" placeholder="请输入用户名" border="surround" ></u-input>
            </view>
          </u-form-item>
          <u-form-item label="密码" required>
            <view class="field">
              <u-icon name="lock-fill" size="32rpx" color="#999"></u-icon>
              <u-input v-model="loginForm.password" type="password" placeholder="请输入密码" border="surround"></u-input>
            </view>
          </u-form-item>
          <view class="form-extra">
            <u-checkbox v-model="rememberMe" label="记住我"></u-checkbox>
            <text class="link" @click="tabIndex = 1">没有账号？去注册</text>
          </view>
          <u-button type="primary" text="登录" @click="onLogin" :loading="loading" shape="circle"></u-button>
          <view class="oauth">
            <text class="oauth-title">其他登录方式</text>
            <view class="oauth-icons">
              <u-icon name="weixin-fill" size="40rpx" color="#09bb07"></u-icon>
              <u-icon name="qq-fill" size="40rpx" color="#4989ff"></u-icon>
              <u-icon name="weibo-circle-fill" size="40rpx" color="#e6162d"></u-icon>
            </view>
          </view>
        </u-form>
      </view>
      <view v-else class="form-wrap">
        <u-form :model="registerForm" ref="registerFormRef" label-position="left" label-width="150rpx">
          <u-form-item label="昵称" required>
            <view class="field">
              <u-icon name="account-fill" size="32rpx" color="#999"></u-icon>
              <u-input v-model="registerForm.name" placeholder="请输入昵称" border="surround"></u-input>
            </view>
          </u-form-item>
          <u-form-item label="用户名" required>
            <view class="field">
              <u-icon name="account-fill" size="32rpx" color="#999"></u-icon>
              <u-input v-model="registerForm.username" placeholder="请输入用户名" border="surround"></u-input>
            </view>
          </u-form-item>
          <u-form-item label="密码" required>
            <view class="field">
              <u-icon name="lock-fill" size="32rpx" color="#999"></u-icon>
              <u-input v-model="registerForm.password" type="password" placeholder="请输入密码" border="surround"></u-input>
            </view>
          </u-form-item>
          <u-form-item label="邮箱" required>
            <view class="field">
              <u-icon name="email-fill" size="32rpx" color="#999"></u-icon>
              <u-input v-model="registerForm.email" placeholder="请输入邮箱" border="surround"></u-input>
            </view>
          </u-form-item>
          <u-button type="success" text="注册并登录" @click="onRegister" :loading="loading" shape="circle"></u-button>
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
      registerForm: { name: '', username: '', password: '', email: '' },
      rememberMe: true
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
.login-page {
  min-height: 100vh;
  padding: 20rpx;
  background: linear-gradient(180deg, #f0f5ff 0%, #ffffff 100%);
}

.hero {
  display: flex;
  align-items: center;
  gap: 16rpx;
  justify-content: center;
  padding-top: 20rpx;
}

.logo {
  width: 80rpx;
  height: 80rpx;
  border-radius: 16rpx;
}

.brand {
  text-align: center;
}

.brand-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #2b2f36;
}

.brand-sub {
  font-size: 24rpx;
  color: #7a7f88;
}

.card {
  width: 86vw;
  max-width: 820rpx;
  margin: 30rpx auto;
  padding: 30rpx 50rpx;
  background: #fff;
  border-radius: 20rpx;
  box-shadow: 0 12rpx 32rpx rgba(0, 0, 0, 0.08);
}

.form-wrap {
  margin-top: 24rpx;
}

.field {
  display: flex;
  align-items: center;
  gap: 12rpx;
  width: 80%;
}


.form-extra {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 16rpx 0 24rpx;
  color: #666;
}

.link {
  color: #2979ff;
}

.oauth {
  margin-top: 24rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
}

.oauth-title {
  color: #999;
  font-size: 24rpx;
}

.oauth-icons {
  display: flex;
  gap: 24rpx;
}

::v-deep .u-form-item__label {
  white-space: nowrap;
}

@media (min-width: 768px) {
  .card {
    max-width: 960rpx;
  }
}
</style>
