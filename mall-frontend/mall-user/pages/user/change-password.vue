<template>
  <view class="page">
    <u-navbar title="修改密码" placeholder></u-navbar>
    <view class="card">
      <u-form :model="form">
        <u-form-item label="原密码" required>
          <u-input v-model="form.oldPassword" type="password" placeholder="请输入原密码"></u-input>
        </u-form-item>
        <u-form-item label="新密码" required>
          <u-input v-model="form.newPassword" type="password" placeholder="请输入新密码"></u-input>
        </u-form-item>
        <u-button type="primary" text="提交" @click="onSubmit" :loading="loading"></u-button>
      </u-form>
    </view>
  </view>
</template>

<script>
import { useAuthStore } from '../../store/auth'
export default {
  data() {
    return { id: null, loading: false, form: { oldPassword: '', newPassword: '' } }
  },
  onLoad(params) {
    const auth = useAuthStore()
    const uid = params?.id || auth.user?.id
    this.id = uid
  },
  methods: {
    async onSubmit() {
      const { oldPassword, newPassword } = this.form
      if (!oldPassword || !newPassword) {
        uni.showToast({ title: '请填写原密码和新密码', icon: 'none' })
        return
      }
      this.loading = true
      try {
        const auth = useAuthStore()
        await auth.updatePassword(this.id, { oldPassword, newPassword })
        uni.showToast({ title: '修改成功', icon: 'success' })
        setTimeout(() => uni.navigateBack(), 300)
      } catch (e) {
        uni.showToast({ title: e?.message || '修改失败', icon: 'none' })
      } finally { this.loading = false }
    }
  }
}
</script>

<style>
.page { padding: 20rpx; }
.card { max-width: 700rpx; margin: 20rpx auto; padding: 30rpx; background: #fff; border-radius: 16rpx; box-shadow: 0 8rpx 24rpx rgba(0,0,0,0.06); }
</style>
