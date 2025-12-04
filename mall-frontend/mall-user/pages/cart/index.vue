<template>
  <view class="page">
    <view class="toolbar">
      <u-button type="primary" text="去下单" @click="openCheckout" :disabled="items.length===0" />
      <u-button type="error" text="清空购物车" @click="onClear" :disabled="items.length===0" />
    </view>
    <scroll-view class="list" scroll-y>
      <view v-if="!userId" class="empty">
        <u-empty text="请先登录" mode="car" />
      </view>
      <view v-else>
        <view v-if="items.length===0" class="empty">
          <u-empty text="购物车暂无数据" mode="car" />
        </view>
        <view v-else class="cards">
          <view v-for="item in items" :key="item.id" class="card">
            <image class="img" :src="item.imageUrl" mode="aspectFill" />
            <view class="body">
              <text class="spec">{{ item.specification }}</text>
              <text class="price">¥{{ item.price }}</text>
              <view class="actions">
                <u-number-box v-model="item.quantity" :min="1" @change="q=>onChangeQty(item,q)" />
                <u-button type="error" size="small" text="删除" @click="onDelete(item)" />
              </view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
    <u-popup :show="checkoutVisible" mode="center" round="16" @close="closeCheckout">
      <view class="modal">
        <view class="modal-title">填写收件信息</view>
        <u-form :model="checkoutForm">
          <u-form-item label="收件人" required>
            <u-input v-model="checkoutForm.name" placeholder="请输入收件人" />
          </u-form-item>
          <u-form-item label="手机号" required>
            <u-input v-model="checkoutForm.phone" placeholder="请输入手机号" />
          </u-form-item>
          <u-form-item label="地址" required>
            <u-input v-model="checkoutForm.address" placeholder="请输入收货地址" />
          </u-form-item>
          <u-button type="primary" text="提交订单" @click="onSubmitCheckout" :loading="checkoutLoading" />
        </u-form>
      </view>
    </u-popup>
  </view>
</template>

<script>
import { useAuthStore } from '../../store/auth'
import { listCartItems, updateCartItem, deleteCartItem, clearCart } from '../../api/cart'
import { createOrder } from '../../api/order'
export default {
  data() {
    return { items: [], loading: false, userId: null, checkoutVisible: false, checkoutLoading: false, checkoutForm: { name: '', phone: '', address: '' } }
  },
  onShow() {
    const auth = useAuthStore()
    this.userId = auth.user?.id || null
    if (this.userId) this.fetchItems()
  },
  methods: {
    async fetchItems() {
      this.loading = true
      try {
        const list = await listCartItems(this.userId)
        this.items = Array.isArray(list) ? list : []
      } catch (e) {
        uni.showToast({ title: e?.message || '加载失败', icon: 'none' })
      } finally { this.loading = false }
    },
    onChangeQty(item, q) {
      const val = typeof q === 'object' ? q.value : q
      if (!val || val < 1) return
      updateCartItem(item.id, { quantity: val })
        .then(() => {
          item.quantity = val
          uni.showToast({ title: '已更新', icon: 'none' })
        })
        .catch(e => uni.showToast({ title: e?.message || '更新失败', icon: 'none' }))
    },
    onDelete(item) {
      deleteCartItem(item.id)
        .then(() => {
          this.items = this.items.filter(i => i.id !== item.id)
          uni.showToast({ title: '已删除', icon: 'none' })
        })
        .catch(e => uni.showToast({ title: e?.message || '删除失败', icon: 'none' }))
    },
    onClear() {
      clearCart(this.userId)
        .then(() => { this.items = []; uni.showToast({ title: '已清空', icon: 'none' }) })
        .catch(e => uni.showToast({ title: e?.message || '清空失败', icon: 'none' }))
    },
    openCheckout() {
      if (!this.userId) { uni.showToast({ title: '请先登录', icon: 'none' }); return }
      this.checkoutVisible = true
    },
    closeCheckout() { this.checkoutVisible = false },
    async onSubmitCheckout() {
      if (!this.userId) { uni.showToast({ title: '请先登录', icon: 'none' }); return }
      const { name, phone, address } = this.checkoutForm
      if (!name || !phone || !address) { uni.showToast({ title: '请填写收件信息', icon: 'none' }); return }
      const items = (Array.isArray(this.items) ? this.items : []).map(c => ({ skuId: c.skuId, quantity: c.quantity }))
      if (!items.length) { uni.showToast({ title: '购物车为空', icon: 'none' }); return }
      this.checkoutLoading = true
      try {
        await createOrder({ userId: this.userId, name, phone, address, items })
        uni.showToast({ title: '创建成功', icon: 'success' })
        this.closeCheckout()
        uni.switchTab({ url: '/pages/order/index' })
      } catch (e) {
        uni.showToast({ title: e?.message || '创建失败', icon: 'none' })
      } finally { this.checkoutLoading = false }
    }
  }
}
</script>

<style>
.page { display: flex; flex-direction: column; height: 100vh; }
.toolbar { padding: 12rpx; background: #fff; border-bottom: 1px solid #eee; }
.list { flex: 1; }
.empty { padding: 40rpx; }
.cards { display: flex; flex-direction: column; gap: 16rpx; padding: 16rpx; }
.card { display: flex; gap: 12rpx; background: #fff; border-radius: 16rpx; padding: 12rpx; box-shadow: 0 8rpx 24rpx rgba(0,0,0,0.06); }
.img { width: 140rpx; height: 140rpx; background: #f6f6f6; border-radius: 12rpx; }
.body { flex: 1; display: flex; flex-direction: column; gap: 8rpx; }
.spec { font-size: 26rpx; color: #333; }
.price { font-size: 24rpx; color: #ff4d4f; }
.actions { display: flex; gap: 12rpx; align-items: center; }
.modal { width: 600rpx; max-width: 86vw; background: #fff; border-radius: 16rpx; padding: 16rpx; }
.modal-title { font-size: 28rpx; font-weight: 600; margin-bottom: 12rpx; }
</style>
