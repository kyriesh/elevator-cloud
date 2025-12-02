<template>
  <div class="app-wrapper">
    <header class="top-header">
      <div class="brand">
        <img src="/logo02.png" alt="Company Logo" class="logo-img" />
        <div class="app-name">Elevator Ops Cloud</div>
      </div>

      <nav class="nav-menu">
        <router-link to="/cockpit" class="nav-item" active-class="active">监控驾驶舱</router-link>
        <span class="divider">/</span>
        <router-link to="/devices" class="nav-item" active-class="active">设备管理</router-link>
        <span class="divider">/</span>
        <router-link to="/orders" class="nav-item" active-class="active">智能派单</router-link>
        <span class="divider">/</span>
        <router-link to="/inventory" class="nav-item" active-class="active">备品备件</router-link>
        <span class="divider">/</span>
        <router-link to="/analysis" class="nav-item" active-class="active">数据分析</router-link>
        <span class="divider">/</span>
        <router-link to="/energy" class="nav-item" active-class="active">能耗监测</router-link>
      </nav>

      <div class="right-section">
        
        <div class="copyright-box">
          <div class="sys-cn-name">电梯智能运维云平台</div>
          <div class="company-copyright">© 深圳市睿景技术服务有限公司版权所有</div>
        </div>

        <div class="vertical-line"></div>

        <span class="time">{{ currentTime }}</span>
        
        <div class="user-profile">
          <el-avatar :size="30" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
          <span class="username">Admin</span>
        </div>
      </div>
    </header>

    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import dayjs from 'dayjs' 

const currentTime = ref('')
let timer = null

const updateTime = () => {
  currentTime.value = dayjs().format('YYYY-MM-DD HH:mm:ss')
}

onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 1000)
})

onUnmounted(() => {
  clearInterval(timer)
})
</script>

<style scoped>
/* 全局深色背景 */
.app-wrapper {
  background-color: #020617; /* 深蓝黑 */
  min-height: 100vh;
  color: #e2e8f0;
  font-family: 'Inter', sans-serif;
}

/* 顶栏样式 */
.top-header {
  height: 64px;
  background: rgba(15, 23, 42, 0.95); /* 稍微加深不透明度，提升文字可读性 */
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(56, 189, 248, 0.2);
  display: flex;
  align-items: center;
  justify-content: space-between; /* 确保左右撑开 */
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 100;
}

/* 左侧 Brand */
.brand {
  display: flex;
  align-items: center;
  margin-right: 40px;
}
.logo-img {
  height: 40px;
  margin-right: 10px;
}
.app-name { font-size: 20px; font-weight: 700; letter-spacing: 1px; color: #fff; text-transform: uppercase; }

/* 中间导航 */
.nav-menu { display: flex; align-items: center; flex: 1; }
.nav-item {
  color: #94a3b8;
  text-decoration: none;
  font-size: 14px;
  padding: 5px 8px;
  border-radius: 4px;
  transition: all 0.3s;
}
.nav-item:hover { color: #38bdf8; }
.nav-item.active {
  color: #38bdf8;
  background: rgba(56, 189, 248, 0.1);
  font-weight: 600;
}
.divider { color: #334155; margin: 0 4px; font-size: 12px; }

/* 右侧区域容器 */
.right-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

/* 新增：版权信息样式 */
.copyright-box {
  display: flex;
  flex-direction: column;
  align-items: flex-end; /* 右对齐 */
  justify-content: center;
  line-height: 1.2;
}
.sys-cn-name {
  font-size: 14px;
  font-weight: bold;
  color: #e2e8f0;
  letter-spacing: 1px;
}
.company-copyright {
  font-size: 14px; /* 增大字体 */
  font-weight: bold; /* 加粗 */
  color: #e2e8f0;  /* 颜色调整为更显眼 */
  /* 移除 transform: scale(0.9); */
}

/* 竖分割线 */
.vertical-line {
  width: 1px;
  height: 24px;
  background-color: #334155;
}

/* 时间 */
.time {
  font-family: 'Monaco', monospace;
  color: #38bdf8;
  font-size: 14px;
  font-weight: 500;
}

/* 用户 */
.user-profile { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.username { font-size: 14px; color: #fff; }

.main-content { padding: 20px; }

/* 路由动画 */
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>