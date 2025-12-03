<template>
  <view class="page">
    <u-navbar title="更新用户信息" placeholder></u-navbar>
    <view class="card">
      <u-form :model="form">
        <u-form-item label="昵称" required>
          <u-input v-model="form.name" placeholder="请输入昵称"></u-input>
        </u-form-item>
        <u-form-item label="邮箱" required>
          <u-input v-model="form.email" placeholder="请输入邮箱"></u-input>
        </u-form-item>
        <u-button type="primary" text="保存" @click="onSave" :loading="loading"></u-button>
      </u-form>
    </view>
  </view>
</template>

<script>
import { useAuthStore } from '../../store/auth'
export default {
  data() {
    return { id: null, loading: false, form: { name: '', email: '' } }
  },
  onLoad(params) {
    const auth = useAuthStore()
    const uid = params?.id || auth.user?.id
    this.id = uid
    if (auth.user) {
      this.form.name = auth.user.name || ''
      this.form.email = auth.user.email || ''
    }
  },
  methods: {
    async onSave() {
      const { name, email } = this.form
      if (!name || !email) {
        uni.showToast({ title: '请填写昵称和邮箱', icon: 'none' })
        return
      }
      this.loading = true
      try {
        const auth = useAuthStore()
        await auth.updateProfile(this.id, { name, email })
        uni.showToast({ title: '更新成功', icon: 'success' })
        setTimeout(() => uni.navigateBack(), 300)
      } catch (e) {
        uni.showToast({ title: e?.message || '更新失败', icon: 'none' })
      } finally { this.loading = false }
    }
  }
}
</script>

<style>
.page { padding: 20rpx; }
.card { max-width: 700rpx; margin: 20rpx auto; padding: 30rpx; background: #fff; border-radius: 16rpx; box-shadow: 0 8rpx 24rpx rgba(0,0,0,0.06); }
</style>
