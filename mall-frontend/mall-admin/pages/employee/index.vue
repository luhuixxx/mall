<template>
  <AdminLayout activeMenu="employee">
    <EmployeeManage />
  </AdminLayout>
</template>

<script>
  import AdminLayout from '../../components/AdminLayout.vue'
  import EmployeeManage from '../../components/EmployeeManage.vue'
  import { useAuthStore } from '../../store/auth'
  export default {
    components: { AdminLayout, EmployeeManage },
    onLoad() {
      const auth = useAuthStore(); auth.init();
      if (!auth.token) uni.reLaunch({ url: '/pages/login/login' })
    }
  }
</script>

<style>
</style>
