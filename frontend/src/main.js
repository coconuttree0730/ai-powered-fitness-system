import { createApp } from 'vue'
import { createPinia } from 'pinia'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import { provideGlobalConfig } from 'element-plus/es/components/config-provider/index.mjs'
import naive from 'naive-ui'
import * as Echarts from 'echarts'
import VueEcharts from 'vue-echarts'

import App from './App.vue'
import router from './router'
import 'element-plus/dist/index.css'
import './styles/main.css'
import './styles/responsive.css'
import './style.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(naive)
provideGlobalConfig({ locale: zhCn }, app, true)

app.component('VueEcharts', VueEcharts)

app.mount('#app')
