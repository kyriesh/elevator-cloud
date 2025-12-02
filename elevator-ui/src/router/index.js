import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('../layout/Layout.vue'),
    redirect: '/cockpit', // 默认进驾驶舱
    children: [
      { 
        path: '/cockpit', 
        component: () => import('../views/Cockpit.vue'),
        meta: { title: '监控驾驶舱' }
      },
      { 
        path: '/orders', 
        component: () => import('../views/OrderList.vue'),
        meta: { title: '智能派单' }
      },
      { 
        path: '/devices', 
        component: () => import('../views/DeviceList.vue'),
        meta: { title: '设备管理' }
      },
      { 
        path: '/inventory', 
        component: () => import('../views/Inventory.vue'),
        meta: { title: '备品备件' }
      },
      { 
        path: '/energy', 
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