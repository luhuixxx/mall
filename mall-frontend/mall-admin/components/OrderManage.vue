<template>
  <div class="order-manage">
    <el-card class="panel" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>订单列表</span>
          <div class="tools">
            <el-input v-model="query.orderId" placeholder="订单ID"  class="mr8" clearable />
            <el-input v-model="query.userId" placeholder="用户ID"  class="mr8" clearable />
            <el-select v-model="query.status" placeholder="状态" clearable  class="mr8"
              style="min-width:140px">
              <el-option label="待支付" :value="0" />
              <el-option label="已支付" :value="1" />
              <el-option label="已取消" :value="2" />
            </el-select>
            <el-input v-model="query.name" placeholder="收件人姓名"  class="mr8" clearable />
            <el-input v-model="query.phone" placeholder="手机号"  class="mr8" clearable />
            <!-- <el-input v-model="query.trackingNumber" placeholder="快递单号"  class="mr8" clearable /> -->
            <el-button  type="primary" @click="fetchOrders">查询</el-button>
          </div>
        </div>
      </template>
      <el-table :data="orders" height="520">
        <el-table-column prop="id" label="订单ID" width="120" />
        <el-table-column prop="userId" label="用户ID" width="120" />
        <el-table-column prop="name" label="收件人" width="160" />
        <el-table-column prop="phone" label="手机号" width="160" />
        <el-table-column prop="address" label="地址" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">{{ statusText(row.status) }}</template>
        </el-table-column>
        <el-table-column prop="totalQuantity" label="件数" width="100" />
        <el-table-column label="金额" width="140">
          <template #default="{ row }">{{ formatAmount(row.amount, row.discount) }}</template>
        </el-table-column>
        <!-- <el-table-column prop="trackingNumber" label="快递单号" width="200" /> -->
        <el-table-column prop="createdTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="260">
          <template #default="{ row }">
            <el-button  @click="openDetails(row)">详情</el-button>
            <el-button  type="danger" :disabled="row.status !== 0" @click="doCancel(row)">取消订单</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="table-footer">
        <el-pagination background layout="prev, pager, next" :current-page="query.page" :page-size="query.size"
          :total="total" @current-change="p => { query.page = p; fetchOrders() }" />
      </div>
    </el-card>

    <el-dialog v-model="detailDialog.visible" title="订单详情" width="760px">
      <el-table :data="details" height="360">
        <el-table-column prop="skuId" label="SKU ID" width="140" />
        <el-table-column prop="quantity" label="数量" width="100" />
        <el-table-column label="价格" width="140">
          <template #default="{ row }">{{ toCurrency(row.price) }}</template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" />
      </el-table>
      <template #footer>
        <el-button @click="detailDialog.visible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listOrders, getOrderDetails, cancelOrder } from '../api/order'

const query = reactive({ page: 1, size: 10, userId: '', status: '', name: '', phone: '', trackingNumber: '', orderId: '' })
const orders = ref([])
const total = ref(0)

const detailDialog = reactive({ visible: false })
const details = ref([])

function isUnauthorized(err) {
  return !!(err && (err.response?.status === 401 || String(err?.message).includes('401')))
}

function statusText(s) {
  if (s === 0) return '待支付'
  if (s === 1) return '已支付'
  if (s === 2) return '已取消'
  return String(s)
}

function toCurrency(v) {
  if (v == null) return '-'
  return Number(v).toFixed(2)
}

function formatAmount(amount, discount) {
  const a = Number(amount || 0)
  const d = Number(discount || 0)
  return toCurrency(a - d)
}

async function fetchOrders() {
  try {
    const s = (query.status === '' || query.status === null || query.status === undefined) ? undefined : (typeof query.status === 'number' ? query.status : Number(query.status))
    const params = {
      page: query.page, size: query.size,
      userId: query.userId !== '' ? Number(query.userId) : undefined,
      status: Number.isFinite(s) ? s : undefined,
      name: query.name || undefined,
      phone: query.phone || undefined,
      trackingNumber: query.trackingNumber || undefined,
      orderId: query.orderId !== '' ? Number(query.orderId) : undefined
    }
    const data = await listOrders(params)
    orders.value = data.records || data.list || []
    total.value = data.total || 0
  } catch (e) {
    if (!isUnauthorized(e)) ElMessage.error(e.message || '订单列表获取失败')
  }
}

async function openDetails(row) {
  try {
    details.value = await getOrderDetails(row.id)
    detailDialog.visible = true
  } catch (e) {
    if (!isUnauthorized(e)) ElMessage.error(e.message || '订单详情获取失败')
  }
}

async function doCancel(row) {
  try {
    await ElMessageBox.confirm('确认取消该订单？', '提示', { type: 'warning' })
    const ok = await cancelOrder(row.id)
    if (ok) {
      ElMessage.success('订单已取消')
      fetchOrders()
    } else {
      ElMessage.warning('取消失败或订单状态不允许取消')
    }
  } catch (e) {
    if (!isUnauthorized(e)) ElMessage.error(e.message || '取消失败')
  }
}

onMounted(async () => {
  try {
    await fetchOrders()
  } catch (e) {
    if (!isUnauthorized(e)) ElMessage.error(e.message || '初始化失败')
  }
})
</script>

<style>
.order-manage {
  padding: 8px
}

.panel {
  margin-bottom: 16px
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between
}

.tools {
  display: flex;
  align-items: center;
  flex-wrap: wrap
}

.mr8 {
  margin-right: 8px;
  margin-bottom: 8px;
  width: 180px;
}

.table-footer {
  padding: 8px 0
}
</style>
