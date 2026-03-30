import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { setToastDefaultOptions } from 'vant'
import './style.css'
import './styles/android-webview.css' // 引入Android WebView优化样式
import App from './App.vue'
import router from "@/router/index.js";

const app = createApp(App)

app.use(createPinia())
app.use(router)

setToastDefaultOptions({
    position: 'middle',
    className: 'app-toast'
})

app.mount('#app')

// Android WebView 特殊处理
if (/android/i.test(navigator.userAgent) && /wv/i.test(navigator.userAgent)) {
    // 添加全局触摸事件处理
    document.addEventListener('DOMContentLoaded', () => {
        // 防止双击缩放
        let lastTouchEnd = 0
        document.addEventListener('touchend', (event) => {
            const now = Date.now()
            if (now - lastTouchEnd <= 300) {
                event.preventDefault()
            }
            lastTouchEnd = now
        }, false)

        // 强制启用平滑滚动
        document.body.style.webkitOverflowScrolling = 'touch'
    })
}
