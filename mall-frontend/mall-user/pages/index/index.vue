<template>
  <view class="page">
    <view class="banner">
      <u-swiper :list="banners" height="220" indicator indicatorMode="line" radius="16"></u-swiper>
    </view>
    <view class="toolbar">
      <u-input v-model="query.name" placeholder="搜索商品名称" clearable @confirm="onSearch" @clear="onClear" border="surround"></u-input>
      <u-button type="primary" text="搜索" @click="onSearch" shape="circle" icon="search"></u-button>
    </view>
    <scroll-view class="list" scroll-y @scrolltolower="loadMore">
      <view class="cards">
        <view v-for="spu in spus" :key="spu.id" class="card" @click="openDetail(spu)">
          <image class="card-img" :src="spu.imageUrl" mode="aspectFit"></image>
          <view class="card-body">
            <text class="card-title">{{ spu.name }}</text>
            <text class="card-desc">{{ spu.description }}</text>
            <view class="card-meta">
              <text class="price" v-if="spu.price != null">¥{{ spu.price }}</text>
              <u-tag v-else text="热卖" type="error" plain size="mini"></u-tag>
              <text class="sales">销量：{{ spu.salesCount || 0 }}</text>
            </view>
          </view>
        </view>
        <view v-if="loading" class="loading">
          <u-loading-icon text="加载中" />
        </view>
        <view v-if="finished && spus.length === 0" class="empty">
          <u-empty mode="list" icon="http://cdn.uviewui.com/uview/empty/list.png" text="暂无商品" />
        </view>
      </view>
    </scroll-view>

    <u-popup :show="detailVisible" mode="center" round="16" @close="closeDetail">
      <view class="detail">
        <image class="detail-img" :src="currentSpu?.imageUrl" mode="aspectFit"></image>
        <view class="detail-body">
          <text class="detail-title">{{ currentSpu?.name }}</text>
          <text class="detail-desc">{{ currentSpu?.description }}</text>
          <view class="detail-meta">
            <text>销量：{{ currentSpu?.salesCount || 0 }}</text>
          </view>
          <u-button type="success" text="加入购物车" @click="openSku" />
        </view>
      </view>
    </u-popup>

    <u-popup :show="skuVisible" mode="bottom" round="16" @close="closeSku">
      <view class="sku-panel">
        <view class="sku-header">
          <text>选择规格</text>
        </view>
        <view class="sku-list">
          <u-radio-group v-model="selectedSkuId">
            <u-radio v-for="sku in skus" :key="sku.id" :name="sku.id" :label="skuLabel(sku)" :disabled="sku.stockQuantity <= 0" />
          </u-radio-group>
        </view>
        <view class="sku-actions">
          <u-button type="primary" text="确定" :disabled="!selectedSkuId" @click="confirmSku" />
        </view>
      </view>
    </u-popup>
  </view>
</template>

<script>
import { getSpuPage, getSkusBySpuId } from '../../api/product'
export default {
  components: { },
  data() {
    return {
      query: { page: 1, size: 10, name: '' },
      spus: [],
      total: 0,
      loading: false,
      finished: false,
      detailVisible: false,
      currentSpu: null,
      skuVisible: false,
      skus: [],
      selectedSkuId: null,
      banners: [
        'http://cdn.uviewui.com/uview/swiper/swiper1.png',
        'http://cdn.uviewui.com/uview/swiper/swiper2.png',
        'http://cdn.uviewui.com/uview/swiper/swiper3.png'
      ]
    }
  },
  onLoad() {
    this.fetchSpus(true)
  },
  methods: {
    async fetchSpus(reset = false) {
      if (this.loading) return
      this.loading = true
      try {
        if (reset) {
          this.query.page = 1
          this.finished = false
          this.spus = []
        }
        const data = await getSpuPage({ page: this.query.page, size: this.query.size, name: this.query.name })
        const records = Array.isArray(data?.records) ? data.records : []
        this.total = data?.total || 0
        this.spus = reset ? records : this.spus.concat(records)
        const pages = data?.pages || 1
        this.finished = this.query.page >= pages || this.spus.length >= this.total
      } catch (e) {
        uni.showToast({ title: '加载失败', icon: 'none' })
      } finally {
        this.loading = false
      }
    },
    onSearch() {
      this.fetchSpus(true)
    },
    onClear() {
      this.query.name = ''
      this.fetchSpus(true)
    },
    loadMore() {
      if (this.loading || this.finished) return
      this.query.page += 1
      this.fetchSpus(false)
    },
    openDetail(spu) {
      this.currentSpu = spu
      this.detailVisible = true
    },
    closeDetail() {
      this.detailVisible = false
      this.currentSpu = null
    },
    async openSku() {
      if (!this.currentSpu) return
      try {
        this.skuVisible = true
        const list = await getSkusBySpuId(this.currentSpu.id)
        this.skus = Array.isArray(list) ? list : []
        this.selectedSkuId = null
      } catch (e) {
        uni.showToast({ title: '获取SKU失败', icon: 'none' })
      }
    },
    closeSku() {
      this.skuVisible = false
    },
    skuLabel(sku) {
      const spec = sku.specification || ''
      const price = sku.price != null ? `¥${sku.price}` : ''
      const stock = sku.stockQuantity != null ? `(库存${sku.stockQuantity})` : ''
      return `${spec} ${price} ${stock}`.trim()
    },
    confirmSku() {
      const sku = this.skus.find(s => s.id === this.selectedSkuId)
      if (!sku) return
      uni.showToast({ title: `已选择：${sku.specification || sku.id}`, icon: 'none' })
      this.closeSku()
      this.closeDetail()
    }
  }
}
</script>

<style>
.page { display: flex; flex-direction: column; height: 100vh; }
.banner { padding: 12rpx 16rpx; background: linear-gradient(180deg, #f0f5ff, #ffffff); }
.toolbar { display: flex; gap: 16rpx; padding: 16rpx; background: #fff; border-bottom: 1px solid #eee; }
.list { flex: 1; }
.cards { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16rpx; padding: 16rpx; }
.card { background: #fff; border-radius: 16rpx; overflow: hidden; box-shadow: 0 8rpx 24rpx rgba(0,0,0,0.06); transition: transform .2s ease; height: 380rpx; display: flex; flex-direction: column; }
.card:active { transform: scale(0.98); }
.card-img { width: 100%; height: 220rpx; background: #f6f6f6; }
.card-body { padding: 12rpx; flex: 1; display: flex; flex-direction: column; }
.card-title { font-size: 28rpx; font-weight: 600; color: #333; }
.card-desc { font-size: 24rpx; color: #666; margin-top: 8rpx; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.card-meta { margin-top: 8rpx; font-size: 22rpx; color: #999; display: flex; justify-content: space-between; align-items: center; }
.price { color: #e54d42; font-weight: 600; }
.sales { color: #999; }
.loading { padding: 24rpx; grid-column: 1 / -1; display: flex; justify-content: center; }
.empty { padding: 40rpx; grid-column: 1 / -1; }

.detail { width: 600rpx; max-width: 86vw; background: #fff; border-radius: 16rpx; overflow: hidden; }
.detail-img { width: 100%; height: 300rpx; background: #f6f6f6; }
.detail-body { padding: 16rpx; display: flex; flex-direction: column; gap: 12rpx; }
.detail-title { font-size: 32rpx; font-weight: 600; color: #333; }
.detail-desc { font-size: 26rpx; color: #666; }
.detail-meta { font-size: 24rpx; color: #999; }

.sku-panel { padding: 16rpx; background: #fff; }
.sku-header { font-size: 28rpx; font-weight: 600; margin-bottom: 12rpx; }
.sku-list { padding: 8rpx 0; }
.sku-actions { padding-top: 12rpx; }

@media (min-width: 1024px) {
  .cards { grid-template-columns: repeat(4, 1fr); }
  .card { height: 420rpx; }
  .card-img { height: 260rpx; }
}
</style>
