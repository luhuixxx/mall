<template>
  <AdminLayout activeMenu="home">
    <Dashboard />
  </AdminLayout>
  
</template>

<script>
  import { useAuthStore } from '../../store/auth'
  import AdminLayout from '../../components/AdminLayout.vue'
  import Dashboard from '../../components/Dashboard.vue'
  export default {
    data() { return {} },
    onLoad() {
      const auth = useAuthStore()
      auth.init()
      if (!auth.token) {
        uni.reLaunch({ url: '/pages/login/login' })
      }
    },
    components: { AdminLayout, Dashboard }
  }
</script>

<style>
  
</style>
