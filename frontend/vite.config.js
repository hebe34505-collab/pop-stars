import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 开发时，页面里的 /api 会转发到 Java 后端。
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': 'http://localhost:8080'
    }
  }
})
