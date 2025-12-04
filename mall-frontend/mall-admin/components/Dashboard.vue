<template>
  <div class="dashboard">
    <el-row :gutter="16">
      <el-col :span="6">
        <el-card class="kpi" shadow="hover">
          <div class="kpi-title">总用户数</div>
          <div class="kpi-value">{{ userStats.totalUsers ?? '-' }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="kpi" shadow="hover">
          <div class="kpi-title">近7日新增</div>
          <div class="kpi-value">{{ userStats.newUsersLast7Days ?? '-' }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="kpi" shadow="hover">
          <div class="kpi-title">订单总数</div>
          <div class="kpi-value">{{ orderStats.totalOrders ?? '-' }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="kpi" shadow="hover">
          <div class="kpi-title">已支付订单</div>
          <div class="kpi-value">{{ orderStats.paidOrders ?? '-' }}</div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="16" class="mt16">
      <el-col :span="8">
        <el-card class="kpi" shadow="hover">
          <div class="kpi-title">今日订单数</div>
          <div class="kpi-value">{{ orderStats.todayOrders ?? '-' }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="kpi" shadow="hover">
          <div class="kpi-title">今日金额</div>
          <div class="kpi-value">{{ toCurrency(orderStats.todayAmount) }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="kpi" shadow="hover">
          <div class="kpi-title">近7日金额</div>
          <div class="kpi-value">{{ toCurrency(orderStats.last7DaysAmount) }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="mt16">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header"><span>最近订单</span></div>
          </template>
          <el-table :data="recentOrders" height="320">
            <el-table-column prop="id" label="订单ID" width="120" />
            <el-table-column prop="userId" label="用户ID" width="120" />
            <el-table-column prop="name" label="收件人" width="160" />
            <el-table-column label="金额" width="140">
              <template #default="{ row }">{{ toCurrency(row.amount) }}</template>
            </el-table-column>
            <el-table-column prop="createdTime" label="创建时间" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header"><span>Top 销售商品</span></div>
          </template>
          <el-table :data="topSpu" height="320">
            <el-table-column prop="id" label="SPU ID" width="120" />
            <el-table-column prop="name" label="名称" />
            <el-table-column prop="salesCount" label="销量" width="140" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="mt16">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header"><span>低库存提醒</span></div>
          </template>
          <el-table :data="lowStockSku" height="280">
            <el-table-column prop="id" label="SKU ID" width="120" />
            <el-table-column prop="spuId" label="SPU ID" width="120" />
            <el-table-column prop="specification" label="规格" />
            <el-table-column prop="stockQuantity" label="库存" width="120" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="mt16" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>订单搜索</span>
          <div class="tools-grid">
            <el-input v-model="orderQuery.orderId" placeholder="订单ID" size="small" clearable />
            <el-input v-model="orderQuery.userId" placeholder="用户ID" size="small" clearable />
            <el-select v-model="orderQuery.status" placeholder="状态" clearable size="small" style="min-width:140px">
              <el-option label="待支付" :value="0" />
              <el-option label="已支付" :value="1" />
              <el-option label="已取消" :value="2" />
            </el-select>
            <el-input v-model="orderQuery.name" placeholder="收件人姓名" size="small" clearable />
            <el-input v-model="orderQuery.phone" placeholder="手机号" size="small" clearable />
            <el-input v-model="orderQuery.trackingNumber" placeholder="快递单号" size="small" clearable />
            <el-button size="small" type="primary" @click="fetchOrderPage">查询</el-button>
          </div>
        </div>
      </template>
      <el-table :data="orderPage.records" height="420">
        <el-table-column prop="id" label="订单ID" width="120" />
        <el-table-column prop="userId" label="用户ID" width="120" />
        <el-table-column prop="name" label="收件人" width="160" />
        <el-table-column prop="phone" label="手机号" width="160" />
        <el-table-column prop="address" label="地址" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">{{ statusText(row.status) }}</template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="180" />
      </el-table>
      <div class="table-footer">
        <el-pagination
          background
          layout="prev, pager, next"
          :current-page="orderQuery.page"
          :page-size="orderQuery.size"
          :total="orderPage.total || 0"
          @current-change="p=>{orderQuery.page=p;fetchOrderPage()}"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserStatsSummary } from '../api/user'
import { getOrderStatsSummary, getRecentOrders, listOrders } from '../api/order'
import { getTopSpu, getLowStockSku } from '../api/product'

const userStats = reactive({ totalUsers: 0, newUsersLast7Days: 0 })
const orderStats = reactive({ totalOrders: 0, paidOrders: 0, todayOrders: 0, todayAmount: 0, last7DaysAmount: 0 })
const recentOrders = ref([])
const topSpu = ref([])
const lowStockSku = ref([])

const orderQuery = reactive({ page: 1, size: 10, userId: '', status: '', name: '', phone: '', trackingNumber: '', orderId: '' })
const orderPage = reactive({ records: [], total: 0 })

function isUnauthorized(err) {
  return !!(err && (err.response?.status === 401 || String(err?.message).includes('401')))
}

function toCurrency(v) {
  if (v == null) return '-'
  return Number(v).toFixed(2)
}

function statusText(s) {
  if (s === 0) return '待支付'
  if (s === 1) return '已支付'
  if (s === 2) return '已取消'
  return String(s)
}

async function loadStats() {
  try {
    const us = await getUserStatsSummary()
    userStats.totalUsers = us.totalUsers || 0
    userStats.newUsersLast7Days = us.newUsersLast7Days || 0
  } catch (e) {
    if (!isUnauthorized(e)) ElMessage.error(e.message || '用户统计获取失败')
  }
  try {
    const os = await getOrderStatsSummary()
    orderStats.totalOrders = os.totalOrders || 0
    orderStats.paidOrders = os.paidOrders || 0
    orderStats.todayOrders = os.todayOrders || 0
    orderStats.todayAmount = os.todayAmount || 0
    orderStats.last7DaysAmount = os.last7DaysAmount || 0
  } catch (e) {
    if (!isUnauthorized(e)) ElMessage.error(e.message || '订单统计获取失败')
  }
}

async function loadRecentAndProduct() {
  try {
    recentOrders.value = await getRecentOrders(10)
  } catch (e) {
    if (!isUnauthorized(e)) ElMessage.error(e.message || '最近订单获取失败')
  }
  try {
    topSpu.value = await getTopSpu(10)
  } catch (e) {
    if (!isUnauthorized(e)) ElMessage.error(e.message || 'Top 商品获取失败')
  }
  try {
    lowStockSku.value = await getLowStockSku(10, 10)
  } catch (e) {
    if (!isUnauthorized(e)) ElMessage.error(e.message || '低库存获取失败')
  }
}

async function fetchOrderPage() {
  try {
    const s = (orderQuery.status === '' || orderQuery.status === null || orderQuery.status === undefined) ? undefined : (typeof orderQuery.status === 'number' ? orderQuery.status : Number(orderQuery.status))
    const params = {
      page: orderQuery.page, size: orderQuery.size,
      userId: orderQuery.userId !== '' ? Number(orderQuery.userId) : undefined,
      status: Number.isFinite(s) ? s : undefined,
      name: orderQuery.name || undefined,
      phone: orderQuery.phone || undefined,
      trackingNumber: orderQuery.trackingNumber || undefined,
      orderId: orderQuery.orderId !== '' ? Number(orderQuery.orderId) : undefined
    }
    const data = await listOrders(params)
    orderPage.records = data.records || data.list || []
    orderPage.total = data.total || 0
  } catch (e) {
    if (!isUnauthorized(e)) ElMessage.error(e.message || '订单搜索失败')
  }
}

onMounted(async () => {
  await loadStats()
  await loadRecentAndProduct()
  await fetchOrderPage()
})
</script>

<style>
.dashboard { padding: 8px }
.kpi { text-align: center }
.kpi-title { color: #606266; font-size: 14px }
.kpi-value { font-size: 24px; font-weight: 600; margin-top: 8px }
.card-header { display: flex; align-items: center; justify-content: space-between }
.mt16 { margin-top: 16px }
.tools-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); grid-gap: 8px }
.table-footer { padding: 8px 0 }
</style>
