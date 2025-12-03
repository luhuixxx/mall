<template>
  <view class="topbar">
    <view class="left">
      <text class="brand">Mall 用户中心</text>
    </view>
    <view class="right" v-if="user">
      <view class="user-menu" @click="toggleMenu">
        <u-avatar size="32" :text="avatarText"></u-avatar>
        <text class="username">{{ displayName }}</text>
        <view class="dropdown" v-show="menuOpen" @click.stop>
          <u-button size="small" type="primary" text="修改密码" @click="goChangePassword"></u-button>
          <u-button size="small" type="success" text="更新用户信息" @click="goUpdate"></u-button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { useAuthStore } from '../store/auth'
export default {
  data() {
    return { menuOpen: false }
  },
  computed: {
    auth() { return useAuthStore() },
    user() { return this.auth.user },
    displayName() { return this.user?.name || this.user?.username || '' },
    avatarText() { return (this.displayName || 'U').slice(0, 1).toUpperCase() }
  },
  methods: {
    toggleMenu() {
      this.menuOpen = !this.menuOpen
    },
    goChangePassword() {
      const id = this.user?.id
      if (!id) return
      this.menuOpen = false
      uni.navigateTo({ url: `/pages/user/change-password?id=${id}` })
    },
    goUpdate() {
      const id = this.user?.id
      if (!id) return
      this.menuOpen = false
      uni.navigateTo({ url: `/pages/user/update?id=${id}` })
    }
  }
}
</script>

<style>
.topbar {
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20rpx;
  background: #ffffff;
  border-bottom: 1px solid #eee;
}

.brand {
  font-weight: 600;
  color: #333;
}

.user-menu {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.username {
  color: #333;
}

.dropdown {
  position: absolute;
  top: 48rpx;
  right: 0;
  background: #fff;
  border: 1px solid #e5e5e5;
  border-radius: 12rpx;
  padding: 12rpx;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.08);
  z-index: 1000;
}
</style>
