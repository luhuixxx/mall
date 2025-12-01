<template>
  <AdminLayout activeMenu="product">
    <div class="placeholder">
      <el-card shadow="hover" class="card">
        <div class="card-title">商品管理</div>
        <div class="card-desc">分类、库存与价格占位</div>
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
