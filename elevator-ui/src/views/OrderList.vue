<template>
  <div class="dispatch-board">
    <div class="kpi-bar">
      <div class="kpi-item">
        <div class="label">今日工单</div>
        <div class="val">15</div>
      </div>
      <div class="kpi-item warning">
        <div class="label">待派单</div>
        <div class="val text-red">4</div>
      </div>
      <div class="kpi-item">
        <div class="label">处理中</div>
        <div class="val text-yellow">8</div>
      </div>
      <div class="kpi-item">
        <div class="label">平均响应时间</div>
        <div class="val text-blue">12 <span class="unit">min</span></div>
      </div>
      <div class="kpi-item">
        <div class="label">人员在岗</div>
        <div class="val text-green">6/8</div>
      </div>
    </div>

    <div class="main-split">
      <div class="left-list-panel">
        <div class="panel-header">
          <div class="title">📋 待处理任务池</div>
          <div class="filter-tabs">
            <span :class="{active: filterTab==='all'}" @click="filterTab='all'">全部</span>
            <span :class="{active: filterTab==='pending'}" @click="filterTab='pending'">待派单</span>
            <span :class="{active: filterTab==='urgent'}" @click="filterTab='urgent'">紧急</span>
          </div>
        </div>
        
        <div class="scroll-list">
          <div 
            v-for="order in filteredOrders" 
            :key="order.id" 
            class="order-card"
            :class="{ 'active': currentOrder?.id === order.id, 'is-urgent': order.priority === '紧急' }"
            @click="selectOrder(order)"
          >
            <div class="card-top">
              <el-tag size="small" :type="order.priority === '紧急' ? 'danger' : 'warning'" effect="dark">
                {{ order.priority }}
              </el-tag>
              <span class="time">{{ order.time }}</span>
            </div>
            <div class="card-title">{{ order.deviceName }}</div>
            <div class="card-issue">{{ order.issue }}</div>
            <div class="card-footer">
              <span class="status-dot" :class="getStatusClass(order.status)"></span>
              {{ order.status }}
              <span class="id-tag">#{{ order.id }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="right-map-panel">
        <div id="dispatchMap" class="map-container"></div>
        
        <div class="map-legend" v-show="!currentOrder">
          <div class="legend-item"><span class="dot bg-red blink"></span> 待处理故障</div>
          <div class="legend-item"><span class="dot bg-blue"></span> 维修力量</div>
        </div>

        <transition name="slide-up">
          <div v-if="currentOrder" class="dispatch-action-area">
            <div class="action-header">
              <div class="back-btn" @click="resetMapState">
                <el-icon><Back /></el-icon> 返回全局视图
              </div>
              <div class="action-title">工单调度: {{ currentOrder.id }}</div>
            </div>

            <div class="action-body">
              <div class="detail-card">
                <div class="row">
                  <span class="label">故障设备：</span>
                  <span class="val text-blue">{{ currentOrder.deviceName }}</span>
                </div>
                <div class="row">
                  <span class="label">故障描述：</span>
                  <span class="val text-red font-bold">{{ currentOrder.issue }}</span>
                </div>
                <div class="row">
                  <span class="label">发生位置：</span>
                  <span class="val">{{ currentOrder.location }}</span>
                </div>
              </div>

              <div v-if="currentOrder.status === '待派单'" class="ai-recommend-box">
                <div class="ai-title">
                  <span>🤖 AI 智能派单推荐</span>
                  <span class="algorithm-tag">最优路径匹配</span>
                </div>
                
                <div class="worker-list">
                  <div 
                    v-for="(worker, idx) in workers" 
                    :key="idx"
                    class="worker-card"
                    :class="{ selected: selectedWorker === worker.name }"
                    @click="handleWorkerSelect(worker)"
                  >
                    <div class="worker-avatar">{{ worker.name[0] }}</div>
                    <div class="worker-info">
                      <div class="name-row">
                        <span class="name">{{ worker.name }}</span>
                        <el-tag size="small" type="success" v-if="idx===0">推荐</el-tag>
                      </div>
                      <div class="meta-row">
                        <span><el-icon><Location /></el-icon> {{ worker.dist }}km</span>
                        <span :class="{'text-yellow': worker.status==='忙碌'}">{{ worker.status }}</span>
                      </div>
                    </div>
                    <div class="match-score">
                      <div class="score-val">{{ worker.score }}</div>
                      <div class="score-label">匹配度</div>
                    </div>
                  </div>
                </div>

                <div class="confirm-btn-area">
                  <el-button type="primary" size="large" class="w-full" @click="confirmDispatch">
                    确认指派给：{{ selectedWorker }}
                  </el-button>
                </div>
              </div>
              
              <div v-else class="process-box">
                <el-steps :active="2" align-center finish-status="success">
                  <el-step title="已派单" :description="currentOrder.worker + ' 接单'" />
                  <el-step title="维修中" description="预计耗时 30min" />
                  <el-step title="待验收" />
                </el-steps>
              </div>
            </div>
          </div>
        </transition>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import AMapLoader from '@amap/amap-jsapi-loader'

// ==========================================
// 🚨 请在此处填入你的高德地图 Key 和 密钥
// ==========================================
const AMAP_KEY = 'ab982a51386a15c7bdc51d90bd81b587';             // 例如: 'a1b2c3d4...'
const AMAP_SECURITY_CODE = 'bf737e01172f10820ece8723dc541e6a';  // 例如: 'e5f6g7h8...'

window._AMapSecurityConfig = { securityJsCode: AMAP_SECURITY_CODE }

// --- 数据定义 ---
const filterTab = ref('all')
const currentOrder = ref(null)
const selectedWorker = ref('张伟')
let map = null
let polyline = null

// 模拟工单数据
const orders = ref([
  { id: 'WO-01', deviceName: '1号楼-客梯-A', issue: '门锁回路断开', priority: '紧急', time: '10:42', status: '待派单', location: 'A栋-1单元', worker: null, lnglat: [116.405285, 39.904989] },
  { id: 'WO-02', deviceName: '3号楼-货梯-B', issue: '平层误差超标', priority: '普通', time: '09:15', status: '处理中', location: 'A栋-3单元', worker: '李强', lnglat: [116.406465, 39.905818] },
  { id: 'WO-03', deviceName: '5号楼-消防梯', issue: '运行震动异常', priority: '普通', time: '08:30', status: '已完成', location: 'B栋-地下', worker: '王建国', lnglat: [116.407265, 39.904018] },
  { id: 'WO-04', deviceName: '2号楼-观光梯', issue: '轿厢照明故障', priority: '普通', time: '昨天', status: '待派单', location: 'C栋-大厅', worker: null, lnglat: [116.404265, 39.903818] },
])

// 模拟维修工数据
const workers = ref([
  { name: '张伟', dist: 0.8, status: '空闲', skill: '通力/三菱', score: 98, lnglat: [116.405000, 39.904500] },
  { name: '李强', dist: 3.2, status: '忙碌', skill: '日立', score: 75, lnglat: [116.407000, 39.905500] },
  { name: '王师傅', dist: 5.5, status: '休假', skill: '通用', score: 40, lnglat: [116.402000, 39.903000] },
])

const filteredOrders = computed(() => {
  if (filterTab.value === 'all') return orders.value
  if (filterTab.value === 'pending') return orders.value.filter(o => o.status === '待派单')
  if (filterTab.value === 'urgent') return orders.value.filter(o => o.priority === '紧急')
  return orders.value
})

const getStatusClass = (status) => {
  if (status === '待派单') return 'bg-red'
  if (status === '处理中') return 'bg-yellow'
  return 'bg-green'
}

const selectOrder = (order) => {
  currentOrder.value = order
  selectedWorker.value = '张伟'
  if (map) {
    map.setZoomAndCenter(17, order.lnglat)
    const bestWorker = workers.value[0]
    drawRoute(bestWorker.lnglat, order.lnglat)
  }
}

const handleWorkerSelect = (worker) => {
  selectedWorker.value = worker.name
  if (currentOrder.value) {
    drawRoute(worker.lnglat, currentOrder.value.lnglat)
  }
}

const drawRoute = (start, end) => {
  if (!map || !window.AMap) return
  if (polyline) map.remove(polyline)
  polyline = new window.AMap.Polyline({
    path: [start, end],
    isOutline: true,
    outlineColor: '#ffeeff',
    borderWeight: 1,
    strokeColor: "#3366FF", 
    strokeOpacity: 1,
    strokeWeight: 4,
    strokeStyle: "dashed",
    strokeDasharray: [10, 5],
    lineJoin: 'round',
    lineCap: 'round',
    zIndex: 50,
    showDir: true
  })
  map.add(polyline)
}

const resetMapState = () => {
  currentOrder.value = null
  if (polyline) map.remove(polyline)
  if (map) map.setFitView()
}

const confirmDispatch = () => {
  ElMessage.success(`调度指令下发成功！维修工 [${selectedWorker.value}] 已接单`)
  currentOrder.value.status = '处理中'
  currentOrder.value.worker = selectedWorker.value
}

const initMap = () => {
  AMapLoader.load({
    key: AMAP_KEY,
    version: '2.0',
    plugins: ['AMap.ToolBar', 'AMap.MoveAnimation'],
  }).then((AMap) => {
    map = new AMap.Map('dispatchMap', {
      viewMode: '3D',
      pitch: 40,
      zoom: 16,
      center: [116.405285, 39.904989],
      mapStyle: 'amap://styles/darkblue',
    })

    orders.value.forEach(order => {
      const isPending = order.status === '待派单'
      const color = isPending ? '#f56c6c' : '#909399'
      const marker = new AMap.Marker({
        position: order.lnglat,
        content: `<div style="position:relative;"><div style="background:${color}; width:20px; height:20px; border:2px solid #fff; border-radius:50%; box-shadow:0 0 10px ${color};"></div>${isPending ? '<div class="pulse-ring"></div>' : ''}</div>`,
        offset: new AMap.Pixel(-10, -10),
        zIndex: 10
      })
      map.add(marker)
    })

    workers.value.forEach(worker => {
      const marker = new AMap.Marker({
        position: worker.lnglat,
        content: `<div style="display:flex; flex-direction:column; align-items:center;"><div style="background:#409eff; padding:2px 6px; border-radius:4px; font-size:12px; color:#fff; white-space:nowrap; margin-bottom:2px; border:1px solid #fff;">🛠️ ${worker.name}</div><div style="width:0; height:0; border-left:6px solid transparent; border-right:6px solid transparent; border-top:6px solid #409eff;"></div></div>`,
        offset: new AMap.Pixel(0, -30),
        zIndex: 20
      })
      map.add(marker)
    })
  }).catch(e => {
    console.error(e)
  })
}

onMounted(() => {
  nextTick(initMap)
})

onUnmounted(() => {
  if (map) map.destroy()
})
</script>

<style scoped>
/* 全局布局 */
.dispatch-board { 
  display: flex; flex-direction: column; 
  height: calc(100vh - 84px); /* 高度自适应 */
  gap: 15px; 
  overflow: hidden; 
  padding: 0 20px 20px 20px; 
}

/* KPI Bar */
.kpi-bar { display: flex; justify-content: space-between; background: rgba(30, 41, 59, 0.7); border: 1px solid rgba(56, 189, 248, 0.1); border-radius: 8px; padding: 15px 30px; flex-shrink: 0; }
.kpi-item { text-align: center; }
.kpi-item .label { color: #94a3b8; font-size: 13px; margin-bottom: 5px; }
.kpi-item .val { color: #e2e8f0; font-size: 24px; font-weight: bold; }
.kpi-item .unit { font-size: 12px; font-weight: normal; color: #64748b; }

/* 核心布局修复：使用 Grid 代替 Flex，彻底解决溢出问题 */
.main-split { 
  display: grid; 
  grid-template-columns: 350px 1fr; /* 左侧固定350px，右侧自适应 */
  gap: 15px; 
  flex: 1; 
  min-height: 0; /* 关键：防止 grid item 内部撑开 */
}

/* 左侧列表 */
.left-list-panel { 
  /* 移除 width: 350px，交由 Grid 控制 */
  background: rgba(30, 41, 59, 0.7); border: 1px solid rgba(56, 189, 248, 0.1); border-radius: 8px; display: flex; flex-direction: column; 
}
.panel-header { padding: 15px; border-bottom: 1px solid #334155; }
.panel-header .title { font-weight: bold; color: #e2e8f0; margin-bottom: 10px; }
.filter-tabs { display: flex; gap: 15px; font-size: 13px; color: #94a3b8; cursor: pointer; }
.filter-tabs span.active { color: #38bdf8; font-weight: bold; border-bottom: 2px solid #38bdf8; padding-bottom: 2px; }
.scroll-list { flex: 1; overflow-y: auto; padding: 10px; }

/* 工单卡片 */
.order-card { background: rgba(15, 23, 42, 0.6); border: 1px solid #334155; border-radius: 6px; padding: 12px; margin-bottom: 10px; cursor: pointer; transition: all 0.2s; border-left: 3px solid transparent; }
.order-card:hover { background: rgba(56, 189, 248, 0.1); }
.order-card.active { background: rgba(56, 189, 248, 0.15); border-left-color: #38bdf8; }
.order-card.is-urgent { border-left-color: #f87171; }
.card-top { display: flex; justify-content: space-between; margin-bottom: 8px; font-size: 12px; }
.card-top .time { color: #64748b; }
.card-title { font-weight: bold; color: #e2e8f0; margin-bottom: 5px; font-size: 14px; }
.card-issue { color: #cbd5e1; font-size: 13px; margin-bottom: 8px; }
.card-footer { display: flex; align-items: center; justify-content: space-between; font-size: 12px; color: #94a3b8; }
.status-dot { width: 6px; height: 6px; border-radius: 50%; display: inline-block; margin-right: 4px; }

/* 右侧地图区 */
.right-map-panel { 
  background: rgba(30, 41, 59, 0.7); 
  border: 1px solid rgba(56, 189, 248, 0.1); 
  border-radius: 8px; 
  position: relative; 
  overflow: hidden; 
  min-width: 0; /* 关键：防止 Grid 溢出 */
}
.map-container { width: 100%; height: 100%; position: absolute; top:0; left:0; z-index: 1; }

.map-legend { position: absolute; bottom: 20px; left: 20px; background: rgba(15, 23, 42, 0.8); padding: 10px; border-radius: 4px; border: 1px solid #334155; display: flex; flex-direction: column; gap: 5px; z-index: 2; }
.legend-item { color: #ccc; font-size: 12px; display: flex; align-items: center; gap: 8px; }
.dot { width: 8px; height: 8px; border-radius: 50%; display: inline-block; }

/* 调度操作浮层 (遮罩) */
.dispatch-action-area {
  position: absolute; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(15, 23, 42, 0.95); /* 深色背景加深 */
  backdrop-filter: blur(5px);
  z-index: 10;
  padding: 20px;
  display: flex; flex-direction: column;
}
.action-header { display: flex; align-items: center; margin-bottom: 20px; border-bottom: 1px solid #334155; padding-bottom: 15px; }
.back-btn { cursor: pointer; color: #94a3b8; display: flex; align-items: center; margin-right: 20px; font-size: 14px; }
.back-btn:hover { color: #38bdf8; }
.action-title { font-size: 18px; font-weight: bold; color: #e2e8f0; }

.action-body { 
  display: flex; 
  gap: 20px; 
  height: 100%; 
  overflow: hidden; /* 防止内部溢出 */
}

/* 详情卡片 */
.detail-card { width: 300px; background: rgba(30, 41, 59, 0.8); padding: 20px; border-radius: 8px; border: 1px solid #334155; height: fit-content; flex-shrink: 0; }
.detail-card .row { margin-bottom: 15px; border-bottom: 1px dashed #334155; padding-bottom: 10px; font-size: 14px; }
.detail-card .label { color: #94a3b8; display: block; margin-bottom: 5px; }
.detail-card .val { color: #fff; font-weight: 500; }

/* AI 推荐区 */
.ai-recommend-box { 
  flex: 1; 
  display: flex; 
  flex-direction: column; 
  min-width: 0; /* 关键：允许 flex item 缩小 */
  overflow-y: auto; /* 允许垂直滚动 */
}
.ai-title { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; font-size: 16px; font-weight: bold; color: #38bdf8; }
.algorithm-tag { font-size: 12px; background: rgba(56, 189, 248, 0.1); padding: 2px 8px; border-radius: 4px; color: #38bdf8; font-weight: normal; }

.worker-list { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 15px; margin-bottom: 30px; }
.worker-card { background: rgba(30, 41, 59, 0.5); border: 1px solid #334155; border-radius: 8px; padding: 15px; cursor: pointer; display: flex; align-items: center; transition: all 0.2s; position: relative; }
.worker-card:hover { border-color: #38bdf8; background: rgba(56, 189, 248, 0.05); }
.worker-card.selected { border-color: #38bdf8; background: rgba(56, 189, 248, 0.1); box-shadow: 0 0 10px rgba(56, 189, 248, 0.2); }

.worker-avatar { width: 40px; height: 40px; background: #475569; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: bold; color: #fff; margin-right: 15px; flex-shrink: 0; }
.worker-info { flex: 1; min-width: 0; }
.name-row { display: flex; align-items: center; gap: 8px; margin-bottom: 5px; color: #fff; font-weight: bold; }
.meta-row { font-size: 12px; color: #94a3b8; display: flex; gap: 10px; margin-bottom: 5px; }
.match-score { text-align: center; margin-left: 10px; }
.score-val { font-size: 20px; font-weight: bold; color: #38bdf8; }
.score-label { font-size: 10px; color: #64748b; }

.w-full { width: 100%; }
.process-box { flex: 1; display: flex; align-items: center; justify-content: center; }

/* 动画与颜色 */
.slide-up-enter-active, .slide-up-leave-active { transition: all 0.3s ease-out; }
.slide-up-enter-from, .slide-up-leave-to { transform: translateY(20px); opacity: 0; }

.bg-red { background: #f87171; } .bg-yellow { background: #fbbf24; } .bg-green { background: #34d399; } .bg-blue { background: #409eff; }
.text-red { color: #f87171; } .text-yellow { color: #fbbf24; } .text-green { color: #34d399; } .text-blue { color: #38bdf8; }
.blink { animation: blink 1.5s infinite; }
@keyframes blink { 0% { opacity: 1; } 50% { opacity: 0.5; } 100% { opacity: 1; } }
:deep(.pulse-ring) { position: absolute; top: -10px; left: -10px; width: 44px; height: 44px; border: 2px solid #f56c6c; border-radius: 50%; animation: pulse 1.5s infinite; }
@keyframes pulse { 0% { transform: scale(0.5); opacity: 1; } 100% { transform: scale(1.2); opacity: 0; } }
</style>