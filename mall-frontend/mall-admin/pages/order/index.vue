<template>
  <AdminLayout activeMenu="order">
    <div class="placeholder">
      <el-card shadow="hover" class="card">
        <div class="card-title">订单管理</div>
        <div class="card-desc">订单查询与发货流程占位</div>
      </el-card>
    </div>
  </AdminLayout>
</template>

<script>
  import AdminLayout from '../../components/AdminLayout.vue'
  import { useAuthStore } from '../../store/auth'
  export default {
    components: { AdminLayout },
    onLoad() {
      const auth = useAuthStore(); auth.init();
      if (!auth.token) uni.reLaunch({ url: '/pages/login/login' })
    }
  }
</script>

<style>
  .placeholder { padding: 16px }
  .card { max-width: 960px; margin: 24px auto }
  .card-title { font-size: 20px; font-weight: 600; margin-bottom: 8px }
  .card-desc { color: #606266 }
</style>
