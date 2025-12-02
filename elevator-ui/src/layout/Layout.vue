<template>
  <div class="common-layout">
    <el-container>
      <el-aside width="220px" class="aside-menu">
        <div class="logo">
          ⚡ 智运维 Cloud
        </div>
        
        <el-menu
          router
          :default-active="$route.path"
          background-color="#001529"
          text-color="#bbb"
          active-text-color="#fff"
          class="el-menu-vertical"
        >
          <el-menu-item index="/cockpit">
            <el-icon><Odometer /></el-icon>
            <span>监控驾驶舱</span>
          </el-menu-item>

          <el-menu-item index="/devices">
            <el-icon><Cpu /></el-icon>
            <span>设备管理</span>
          </el-menu-item>

          <el-menu-item index="/orders">
            <el-icon><List /></el-icon>
            <span>智能派单</span>
          </el-menu-item>

          <el-menu-item index="/inventory">
            <el-icon><Box /></el-icon>
            <span>备品备件</span>
          </el-menu-item>

          <el-menu-item index="/energy">
            <el-icon><Lightning /></el-icon>
            <span>能耗监测</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-main class="main-content">
        <router-view></router-view>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
// 这里的逻辑很简单，主要依赖 el-menu 的 router 属性自动处理跳转
</script>

<style scoped>
/* 让容器撑满屏幕 */
.common-layout, .el-container {
  height: 100vh;
  overflow: hidden;
}

/* 侧边栏样式 */
.aside-menu {
  background-color: #001529;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 6px rgba(0,21,41,.35);
  z-index: 10;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-weight: bold;
  font-size: 18px;
  color: #fff;
  background-color: #002140; /* 比侧边栏稍深一点 */
}

/* 菜单项样式微调 */
.el-menu-vertical {
  border-right: none; /* 去掉 Element 默认的右边框 */
}

.el-menu-item {
  margin: 4px 0;
}

/* 选中项的高亮样式 */
.el-menu-item.is-active {
  background-color: #1890ff !important;
}

/* 主内容区域背景 */
.main-content {
  background-color: #f0f2f5;
  padding: 0; /* 让子页面决定 padding，方便驾驶舱全屏 */
  overflow-y: auto; /* 内容多了可以滚动 */
}
</style>