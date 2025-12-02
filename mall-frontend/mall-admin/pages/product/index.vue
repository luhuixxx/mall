<template>
  <AdminLayout activeMenu="product">
    <ProductManage />
  </AdminLayout>
</template>

<script>
  import AdminLayout from '../../components/AdminLayout.vue'
  import ProductManage from '../../components/ProductManage.vue'
  import { useAuthStore } from '../../store/auth'
  export default {
    components: { AdminLayout, ProductManage },
    onLoad() {
      const auth = useAuthStore(); auth.init();
      if (!auth.token) uni.reLaunch({ url: '/pages/login/login' })
    }
  }
</script>

<style>
</style>
