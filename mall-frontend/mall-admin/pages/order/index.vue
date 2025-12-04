<template>
  <AdminLayout activeMenu="order">
    <OrderManage />
  </AdminLayout>
</template>

<script>
  import AdminLayout from '../../components/AdminLayout.vue'
  import OrderManage from '../../components/OrderManage.vue'
  import { useAuthStore } from '../../store/auth'
  export default {
    components: { AdminLayout, OrderManage },
    onLoad() {
      const auth = useAuthStore(); auth.init();
      if (!auth.token) uni.reLaunch({ url: '/pages/login/login' })
    }
  }
</script>

<style>
</style>
