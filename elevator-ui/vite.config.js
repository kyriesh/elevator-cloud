import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path' // 引入 Node.js 的 path 模块

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  
  // ⬇️ 新增：配置路径别名
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src') 
    }
  },

  // 之前的 server 配置保持不变
  server: {
    host: '0.0.0.0',
    port: 5173,
    open: false
  }
})