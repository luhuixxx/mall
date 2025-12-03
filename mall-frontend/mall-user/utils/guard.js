import { useAuthStore } from '../store/auth'

function isPublic(url) {
  return url.includes('/pages/login/login')
}

function hasToken() {
  const auth = useAuthStore()
  return !!auth.token
}

function guardNav(args) {
  const url = args?.url || ''
  if (!isPublic(url) && !hasToken()) {
    args.url = '/pages/login/login'
  }
  return args
}

export function setupAuthGuard(app) {
  uni.addInterceptor('navigateTo', { invoke: guardNav })
  uni.addInterceptor('redirectTo', { invoke: guardNav })
  uni.addInterceptor('reLaunch', { invoke: guardNav })
  uni.addInterceptor('switchTab', { invoke: guardNav })
  app.mixin({
    onShow() {
      const pages = getCurrentPages()
      const curr = pages[pages.length - 1]
      const route = curr?.route ? '/' + curr.route : ''
      if (route && !isPublic(route) && !hasToken()) {
        uni.reLaunch({ url: '/pages/login/login' })
      }
    }
  })
}
