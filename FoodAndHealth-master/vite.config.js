import { fileURLToPath, URL } from 'node:url'

import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const apiProxyTarget = env.VITE_API_PROXY_TARGET || env.VITE_NATIVE_API_BASE_URL || 'http://123.56.144.72:8080'

  return {
    plugins: [
      vue(),
      vueDevTools(),
    ],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      },
    },
    // 👇 👇 这里是新增的跨域代理配置 👇 👇
    server: {
      proxy: {
        '/api': {
          target: apiProxyTarget,
          changeOrigin: true, // 开启跨域
          rewrite: (path) => path
        }
      }
    }
  }
})
