<template>
  <AdminLayout activeMenu="user">
    <UserManage />
  </AdminLayout>
</template>

<script>
  import AdminLayout from '../../components/AdminLayout.vue'
  import UserManage from '../../components/UserManage.vue'
  import { useAuthStore } from '../../store/auth'
  export default {
    components: { AdminLayout, UserManage },
    onLoad() {
      const auth = useAuthStore(); auth.init();
      if (!auth.token) uni.reLaunch({ url: '/pages/login/login' })
    }
  }
</script>

<style>
</style>
