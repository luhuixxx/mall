import App from './App'

// #ifndef VUE3
import Vue from 'vue'
import './uni.promisify.adaptor'
Vue.config.productionTip = false
App.mpType = 'app'
const app = new Vue({
  ...App
})
app.$mount()
// #endif

// #ifdef VUE3
import { createSSRApp } from 'vue'
import { createPinia } from 'pinia'
import uviewPlus  from 'uview-plus'
import { useAuthStore } from './store/auth'
import { setupAuthGuard } from './utils/guard'
export function createApp() {
  const app = createSSRApp(App)
  const pinia = createPinia()
  app.use(pinia)
  app.use(uviewPlus)
  const auth = useAuthStore()
  auth.init()
  setupAuthGuard(app)
  return {
    app
  }
}
// #endif
