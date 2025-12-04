<template>
  <view class="page">
    <view class="content">
      <view class="card">
        <view class="title">个人信息</view>
        <view class="profile-header">
          <u-avatar :text="(user?.name || user?.username || 'U').slice(0,1).toUpperCase()" size="64" src="/static/logo.png"></u-avatar>
          <view class="profile-meta">
            <text class="nickname">{{ user?.name || user?.username || '未命名用户' }}</text>
            <text class="email">{{ user?.email || '未设置邮箱' }}</text>
          </view>
        </view>
        <view class="info">
          <view class="row">
            <view class="left"><u-icon name="account-fill" size="32rpx" color="#999"></u-icon><text class="label">用户名</text></view>
            <text class="value">{{ user?.username || '' }}</text>
          </view>
          <view class="row">
            <view class="left"><u-icon name="tags-fill" size="32rpx" color="#999"></u-icon><text class="label">昵称</text></view>
            <text class="value">{{ user?.name || '' }}</text>
          </view>
          <view class="row">
            <view class="left"><u-icon name="email-fill" size="32rpx" color="#999"></u-icon><text class="label">邮箱</text></view>
            <text class="value">{{ user?.email || '' }}</text>
          </view>
          <view class="row">
            <view class="left"><u-icon name="rmb-circle-fill" size="32rpx" color="#999"></u-icon><text class="label">余额</text></view>
            <text class="value">¥{{ user?.balance != null ? user.balance : '' }}</text>
          </view>
        </view>
        <view class="actions">
          <u-button type="primary" text="更新资料" @click="openUpdate" />
          <u-button type="warning" text="修改密码" @click="openPassword" />
          <u-button type="error" text="退出登录" @click="onLogout" />
        </view>
      </view>

      <u-popup :show="updateVisible" mode="center" round="16" @close="closeUpdate">
        <view class="modal">
          <view class="modal-title">更新资料</view>
          <u-form :model="updateForm" label-position="left" label-width="150rpx">
            <u-form-item label="昵称" required>
              <view class="field">
                <u-icon name="tags-fill" size="32rpx" color="#999"></u-icon>
                <u-input v-model="updateForm.name" placeholder="请输入昵称" border="surround" />
              </view>
            </u-form-item>
            <u-form-item label="邮箱" required>
              <view class="field">
                <u-icon name="email-fill" size="32rpx" color="#999"></u-icon>
                <u-input v-model="updateForm.email" placeholder="请输入邮箱" border="surround" />
              </view>
            </u-form-item>
            <u-button type="primary" text="保存" @click="onSaveUpdate" :loading="updateLoading" shape="circle" />
          </u-form>
        </view>
      </u-popup>

      <u-popup :show="passwordVisible" mode="center" round="16" @close="closePassword">
        <view class="modal">
          <view class="modal-title">修改密码</view>
          <u-form :model="passwordForm" label-position="left" label-width="150rpx">
            <u-form-item label="原密码" required>
              <view class="field">
                <u-icon name="lock-fill" size="32rpx" color="#999"></u-icon>
                <u-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" border="surround" />
              </view>
            </u-form-item>
            <u-form-item label="新密码" required>
              <view class="field">
                <u-icon name="lock-fill" size="32rpx" color="#999"></u-icon>
                <u-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" border="surround" />
              </view>
            </u-form-item>
            <u-button type="primary" text="提交" @click="onSubmitPassword" :loading="passwordLoading" shape="circle" />
          </u-form>
        </view>
      </u-popup>
    </view>
  </view>
</template>

<script>
import { useAuthStore } from '../../store/auth'
export default {
  data() {
    return {
      updateVisible: false,
      passwordVisible: false,
      updateForm: { name: '', email: '' },
      passwordForm: { oldPassword: '', newPassword: '' },
      updateLoading: false,
      passwordLoading: false
    }
  },
  computed: {
    auth() { return useAuthStore() },
    user() { return this.auth.user }
  },
  onShow() {
    const id = this.auth.user?.id
    if (id) this.auth.loadUser(id)
  },
  methods: {
    openUpdate() {
      this.updateForm.name = this.user?.name || ''
      this.updateForm.email = this.user?.email || ''
      this.updateVisible = true
    },
    closeUpdate() { this.updateVisible = false },
    async onSaveUpdate() {
      const name = this.updateForm.name
      const email = this.updateForm.email
      if (!name || !email) {
        uni.showToast({ title: '请填写昵称和邮箱', icon: 'none' })
        return
      }
      this.updateLoading = true
      try {
        const id = this.user?.id
        await this.auth.updateProfile(id, { name, email })
        uni.showToast({ title: '更新成功', icon: 'success' })
        this.closeUpdate()
      } catch (e) {
        uni.showToast({ title: e?.message || '更新失败', icon: 'none' })
      } finally { this.updateLoading = false }
    },
    openPassword() {
      this.passwordForm.oldPassword = ''
      this.passwordForm.newPassword = ''
      this.passwordVisible = true
    },
    closePassword() { this.passwordVisible = false },
    async onSubmitPassword() {
      const oldPassword = this.passwordForm.oldPassword
      const newPassword = this.passwordForm.newPassword
      if (!oldPassword || !newPassword) {
        uni.showToast({ title: '请填写原密码和新密码', icon: 'none' })
        return
      }
      this.passwordLoading = true
      try {
        const id = this.user?.id
        await this.auth.updatePassword(id, { oldPassword, newPassword })
        uni.showToast({ title: '修改成功', icon: 'success' })
        this.closePassword()
      } catch (e) {
        uni.showToast({ title: e?.message || '修改失败', icon: 'none' })
      } finally { this.passwordLoading = false }
    },
    onLogout() {
      try {
        this.auth.logout()
        uni.showToast({ title: '已退出登录', icon: 'success' })
        setTimeout(() => {
          uni.reLaunch({ url: '/pages/login/login' })
        }, 300)
      } catch (e) {
        uni.showToast({ title: e?.message || '退出失败', icon: 'none' })
      }
    }
  }
}
</script>

<style>
.page {
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.content {
  flex: 1;
  padding: 20rpx;
}

.card {
  background: #fff;
  border-radius: 16rpx;
  padding: 20rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.06);
}

.title {
  font-size: 30rpx;
  font-weight: 600;
  margin-bottom: 12rpx;
}

.profile-header { display: flex; align-items: center; gap: 16rpx; margin-bottom: 12rpx; }
.profile-meta { display: flex; flex-direction: column; }
.nickname { font-size: 28rpx; font-weight: 600; color: #333; }
.email { font-size: 24rpx; color: #666; }

.info {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.row { display: flex; justify-content: space-between; align-items: center; gap: 12rpx; }
.left { display: flex; align-items: center; gap: 8rpx; }

.label {
  color: #666;
}

.value {
  color: #333;
}

.actions {
  display: flex;
  gap: 12rpx;
  margin-top: 16rpx;
}

.modal {
  margin: 20rpx;
  width: 600rpx;
  max-width: 86vw;
  background: #fff;
  border-radius: 16rpx;
  padding: 16rpx;
}

.modal-title {
  font-size: 28rpx;
  font-weight: 600;
  margin-bottom: 12rpx;
}

.field { display: flex; align-items: center; gap: 12rpx; }
@media (min-width: 1024px) { .modal { width: 720rpx; } }
</style>
