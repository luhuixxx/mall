<template>
  <el-container class="admin-layout">
    <el-aside :width="collapsed ? '64px' : '220px'" class="aside">
      <div class="logo">Mall Admin</div>
      <el-menu :default-active="activeMenu" class="menu" @select="onSelect" :collapse="collapsed">
        <el-menu-item index="home">首页</el-menu-item>
        <el-menu-item index="employee">员工管理</el-menu-item>
        <el-menu-item index="user">用户管理</el-menu-item>
        <el-menu-item index="product">商品管理</el-menu-item>
        <el-menu-item index="order">订单管理</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-button text @click="toggleCollapse">{{ collapsed ? '展开' : '收起' }}</el-button>
        </div>
        <div class="header-right">
          <el-dropdown trigger="hover">
            <span class="user-name">{{ displayName }}</span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main">
        <slot />
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
  import { useAuthStore } from '../store/auth'
  export default {
    props: { activeMenu: { type: String, default: 'home' } },
    data() { return { collapsed: false } },
    computed: {
      displayName() {
        const auth = useAuthStore()
        const e = auth.employee || {}
        return e.nickname || e.username || e.name || '用户'
      }
    },
    mounted() {
      const auth = useAuthStore(); auth.init();
      if (!auth.token) uni.reLaunch({ url: '/pages/login/login' })
    },
    methods: {
      toggleCollapse() { this.collapsed = !this.collapsed },
      logout() { const auth = useAuthStore(); auth.logout(); uni.reLaunch({ url: '/pages/login/login' }) },
      onSelect(key) {
        const map = {
          home: '/pages/index/index',
          employee: '/pages/employee/index',
          user: '/pages/user/index',
          product: '/pages/product/index',
          order: '/pages/order/index'
        }
        const url = map[key] || '/pages/index/index'
        if (('/' + this.$page?.route) === url) return
        uni.reLaunch({ url })
      }
    }
  }
</script>

<style>
  :root { --aside-bg: #0f172a; --aside-text: #e2e8f0; --header-bg: #ffffff; --main-bg: #f5f7fa }
  .admin-layout { height: 100vh }
  .aside { background: var(--aside-bg); color: var(--aside-text) }
  .logo { padding: 16px; font-weight: 600; font-size: 18px; text-align: center }
  .menu { border-right: none; background: transparent; color: var(--aside-text) }
  .header { display: flex; align-items: center; justify-content: space-between; background: var(--header-bg); box-shadow: 0 1px 6px rgba(0,0,0,.08) }
  .header-left { padding-left: 8px }
  .header-right { padding-right: 16px }
  .user-name { cursor: pointer; font-weight: 500 }
  .main { background: var(--main-bg) }
</style>
