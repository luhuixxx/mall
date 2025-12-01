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
// #ifdef H5
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
// #endif
export function createApp() {
  const app = createSSRApp(App)
  const pinia = createPinia()
  app.use(pinia)
  // #ifdef H5
  app.use(ElementPlus)
  // #endif
  return {
    app
  }
}
// #endif
