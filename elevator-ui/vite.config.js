import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  // ⬇️ 添加以下 server 配置
  server: {
    host: '0.0.0.0', // 允许从外部 IP (如 Windows 宿主机) 访问
    port: 5173,      // 固定端口，防止自动跳到 5174
    open: false      // 禁止自动打开 VM 里的浏览器
  }
})