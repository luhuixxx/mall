<template>
  <view class="page">
    <UserTopBar />
    <view class="content">
      <image class="logo" src="/static/logo.png"></image>
      <view class="text-area">
        <text class="title">{{ title }}</text>
      </view>
    </view>
  </view>
  
</template>

<script>
import UserTopBar from '../../components/UserTopBar.vue'
export default {
  components: { UserTopBar },
  data() {
    return { title: '欢迎来到用户中心' }
  }
}
</script>

<style>
.page { display: flex; flex-direction: column; height: 100vh; }
.content { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; }

	.logo {
    height: 200rpx;
    width: 200rpx;
    margin-top: 200rpx;
    margin-left: auto;
    margin-right: auto;
    margin-bottom: 50rpx;
  }

	.text-area {
		display: flex;
		justify-content: center;
	}

  .title {
    font-size: 36rpx;
    color: #8f8f94;
  }
</style>
