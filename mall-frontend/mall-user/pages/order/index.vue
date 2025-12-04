<template>
  <view class="page">
    <scroll-view class="list" scroll-y>
      <view class="orders" v-if="userId">
        <view v-if="orders.length===0" class="empty">
          <u-empty text="暂未有订单" mode="order" />
        </view>
        <view v-else>
          <view class="order" v-for="o in orders" :key="o.id">
            <view class="row">
              <text class="oid">订单号：{{ o.id }}</text>
              <u-tag v-if="o.status===0" text="待支付" type="warning" plain></u-tag>
              <u-tag v-else-if="o.status===1" text="已支付" type="success" plain></u-tag>
              <u-tag v-else text="已取消" type="error" plain></u-tag>
            </view>
            <view class="row"><text>收件人：{{ o.name }}，电话：{{ o.phone }}</text></view>
            <view class="row"><text>地址：{{ o.address }}</text></view>
            <view class="row"><text>数量：{{ o.totalQuantity }}，金额：¥{{ o.amount }}</text></view>
            <view class="row" v-if="o.status===0">
              <text>取消倒计时：</text>
              <u-count-down :time="remainMs(o)" format="HH:mm:ss" @finish="onCountFinish(o)" />
            </view>
            <view class="actions">
              <u-button type="default" text="查看详情" @click="openDetail(o)" />
              <u-button v-if="o.status===0" type="primary" text="支付" @click="onPay(o)" />
              <u-button v-if="o.status===0" type="error" text="取消订单" @click="onCancel(o)" />
            </view>
          </view>
        </view>
      </view>
      <view v-else class="empty">
        <u-empty text="请先登录" mode="list" />
      </view>
    </scroll-view>

    <u-popup :show="detailVisible" mode="center" round="16" @close="closeDetail">
      <view class="detail-modal">
        <view class="detail-title">订单详情</view>
        <view v-if="detailItems.length===0">
          <u-empty text="暂无详情" mode="list" />
        </view>
        <view v-else>
          <view class="detail-card" v-for="d in detailItems" :key="(d.id || (d.skuId + '-' + d.orderId))">
            <image class="detail-img" :src="normalizeImg(d.imgUrl || d.imageUrl)" mode="aspectFill"></image>
            <view class="detail-info">
              <text class="detail-item-title">{{ d.spuName || ('SKU ' + d.skuId) }}</text>
              <text class="detail-spec">规格：{{ d.specification || '' }}</text>
              <view class="detail-meta">
                <text>数量：{{ d.quantity }}</text>
                <text>单价：¥{{ d.price }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </u-popup>
  </view>
</template>

<script>
import { useAuthStore } from '../../store/auth'
import { listUserOrders, payOrder, cancelOrder, getOrderDetails } from '../../api/order'
export default {
  data() {
    return {
      userId: null,
      orders: [],
      detailVisible: false,
      detailItems: []
    }
  },
  onShow() {
    const auth = useAuthStore()
    this.userId = auth.user?.id || null
    if (this.userId) this.fetchOrders()
  },
  methods: {
    async fetchOrders() {
      try {
        const list = await listUserOrders(this.userId)
        this.orders = Array.isArray(list) ? list : []
      } catch (e) {
        uni.showToast({ title: e?.message || '加载失败', icon: 'none' })
      }
    },
    parseTime(t) {
      if (!t) return 0
      if (typeof t === 'number') return t > 1e12 ? t : t * 1000
      const ms = Date.parse(t)
      return isNaN(ms) ? 0 : ms
    },
    remainMs(o) {
      const ct = this.parseTime(o.createdTime)
      const now = Date.now()
      const deadline = ct + 5 * 60 * 1000
      const diff = deadline - now
      return diff > 0 ? diff : 0
    },
    onCountFinish(o) {
      this.fetchOrders()
    },
    onPay(o) {
      payOrder(o.id, { userId: this.userId })
        .then(() => { uni.showToast({ title: '支付成功', icon: 'success' }); this.fetchOrders() })
        .catch(e => uni.showToast({ title: e?.message || '支付失败', icon: 'none' }))
    },
    onCancel(o) {
      cancelOrder(o.id)
        .then(() => { uni.showToast({ title: '已取消', icon: 'none' }); this.fetchOrders() })
        .catch(e => uni.showToast({ title: e?.message || '取消失败', icon: 'none' }))
    },
    async openDetail(o) {
      try {
        const list = await getOrderDetails(o.id)
        this.detailItems = Array.isArray(list) ? list : []
        this.detailVisible = true
      } catch (e) {
        uni.showToast({ title: e?.message || '获取详情失败', icon: 'none' })
      }
    },
    closeDetail() { this.detailVisible = false }
    ,
    normalizeImg(u) {
      if (!u) return ''
      const s = String(u).replace(/`/g, '').trim()
      return s
    }
  }
}
</script>

<style>
.page { display: flex; flex-direction: column; height: 100vh; }
.list { flex: 1; }
.empty { padding: 40rpx; }
.orders { padding: 16rpx; display: flex; flex-direction: column; gap: 16rpx; }
.order { background: #fff; border-radius: 16rpx; padding: 12rpx; box-shadow: 0 8rpx 24rpx rgba(0,0,0,0.06); }
.row { display: flex; align-items: center; justify-content: space-between; margin: 4rpx 0; }
.oid { color: #333; font-weight: 600; }
.actions { display: flex; gap: 12rpx; margin-top: 8rpx; }
.detail-modal { width: 600rpx; max-width: 86vw; background: #fff; border-radius: 16rpx; padding: 16rpx; }
.detail-title { font-size: 28rpx; font-weight: 600; margin-bottom: 12rpx; }
.detail-card { display: flex; gap: 12rpx; padding: 8rpx 0; }
.detail-img { width: 120rpx; height: 120rpx; background: #f6f6f6; border-radius: 12rpx; }
.detail-info { flex: 1; display: flex; flex-direction: column; gap: 6rpx; }
.detail-item-title { font-size: 26rpx; font-weight: 600; color: #333; }
.detail-spec { font-size: 24rpx; color: #666; }
.detail-meta { display: flex; gap: 16rpx; font-size: 24rpx; color: #333; }
</style>
