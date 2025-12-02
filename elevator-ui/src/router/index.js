import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('../layout/TopLayout.vue'), // 使用顶部导航布局
    redirect: '/cockpit', // 默认首页
    children: [
      // 1. 监控驾驶舱
      { 
        path: '/cockpit', 
        name: 'Cockpit',
        component: () => import('../views/Cockpit.vue'),
        meta: { title: '监控驾驶舱' }
      },
      
      // 2. 设备管理 (看板 + 详情)
      { 
        path: '/devices', 
        name: 'DeviceBoard', 
        component: () => import('../views/device/DeviceBoard.vue'),
        meta: { title: '设备管理' }
      },
      { 
        path: '/devices/:id', 
        name: 'DeviceDetail', 
        component: () => import('../views/device/DeviceDetail.vue'),
        meta: { title: '设备详情' }
      },
      
      // 3. 智能派单
      { 
        path: '/orders', 
        name: 'OrderList',
        component: () => import('../views/OrderList.vue'),
        meta: { title: '智能派单' }
      },
      
      // 4. 备品备件
      { 
        path: '/inventory', 
        name: 'Inventory',
        component: () => import('../views/Inventory.vue'),
        meta: { title: '备品备件' }
      },

      // 5. 数据分析 (本次新增)
      { 
        path: '/analysis', 
        name: 'Analysis',
        component: () => import('../views/Analysis.vue'),
        meta: { title: '数据分析' }
      },

      // 6. 能耗监测
      { 
        path: '/energy', 
        name: 'Energy',
        component: () => import('../views/Energy.vue'),
        meta: { title: '能耗监测' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router