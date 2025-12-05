<template>
  <div class="detail-wrapper">
    <el-page-header @back="goBack" class="custom-header">
      <template #content>
        <div class="header-info">
          <span class="device-title">{{ deviceId }} - {{ deviceBaseInfo.name }}</span>
          <el-tag :type="statusMap[realtimeData.status]?.type || 'info'" effect="dark" class="ml-2">
            {{ statusMap[realtimeData.status]?.text || '离线' }}
          </el-tag>
          <span class="update-time" v-if="lastUpdateTime">更新于: {{ lastUpdateTime }}</span>
        </div>
      </template>
      <template #extra>
        <div v-if="deviceType === 'Elevator'">
          <el-button type="primary" plain size="small">远程复位</el-button>
          <el-button type="danger" size="small">发起急修</el-button>
        </div>
      </template>
    </el-page-header>

    <div v-if="deviceType === 'Gateway'" class="empty-state">
      <el-empty description="边缘网关详情页正在建设中..." />
    </div>

    <div v-else class="content-grid">
      
      <div class="main-panel">
        <el-tabs v-model="activeTab" class="custom-tabs">
          
          <el-tab-pane label="🚀 实时监控与预测" name="status">
            
            <div class="monitor-cards">
              <div class="monitor-card" :class="{active: currentMetric==='floor'}" @click="switchMetric('floor', '楼层')">
                <div class="label">当前楼层</div>
                <div class="value text-blue">{{ realtimeData.floor || '-' }} <span class="unit">F</span></div>
              </div>
              <div class="monitor-card" :class="{active: currentMetric==='speed'}" @click="switchMetric('speed', '速度')">
                <div class="label">运行速度</div>
                <div class="value text-green">{{ formatNum(realtimeData.speed) }} <span class="unit">m/s</span></div>
              </div>
              <div class="monitor-card" :class="{active: currentMetric==='temp'}" @click="switchMetric('temp', '温度')">
                <div class="label">机房温度</div>
                <div class="value text-purple">{{ formatNum(realtimeData.temp) }} <span class="unit">℃</span></div>
              </div>
              <div class="monitor-card" :class="{active: currentMetric==='current'}" @click="switchMetric('current', '电流')">
                <div class="label">电机电流</div>
                <div class="value text-yellow">{{ formatNum(realtimeData.current) }} <span class="unit">A</span></div>
              </div>
              <div class="monitor-card" :class="{active: currentMetric==='vibration'}" @click="switchMetric('vibration', '震动')">
                <div class="label">震动幅值</div>
                <div class="value text-red">{{ formatNum(realtimeData.vibration, 3) }} <span class="unit">g</span></div>
              </div>
            </div>

            <div class="chart-container">
              <div class="chart-title">
                📊 {{ metricName }}实时趋势 (24H)
                <el-tag size="small" effect="dark" class="ml-2 animate-pulse" type="success">LIVE</el-tag>
              </div>
              <div id="detailTrendChart" style="width: 100%; height: 300px;"></div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="🔔 告警日志" name="alarms">
             <div class="empty-placeholder">暂无告警数据 (等待接入 sys_alarm 表)</div>
          </el-tab-pane>
        </el-tabs>
      </div>

      <div class="side-panel">
        <div class="panel-header">📄 基础档案</div>
        <div class="profile-list">
          <div class="profile-item"><span class="label">设备编号</span><span class="val">{{ deviceId }}</span></div>
          <div class="profile-item"><span class="label">安装位置</span><span class="val">{{ deviceBaseInfo.location }}</span></div>
          <div class="profile-item"><span class="label">品牌型号</span><span class="val">{{ deviceBaseInfo.model }}</span></div>
          <div class="profile-item"><span class="label">投产日期</span><span class="val">2020-05-12</span></div>
        </div>
        <div class="device-img">
          <div class="elevator-animation" :class="getElevatorAnimClass(realtimeData.status)">
            <div class="door left"></div>
            <div class="door right"></div>
            <div class="floor-display">{{ realtimeData.floor || 1 }}</div>
          </div>
          <div style="margin-top: 10px; font-size: 12px; color: #64748b;">设备实时状态仿真</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import * as echarts from 'echarts'
import { API_BASE_URL } from '@/config.js'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()

const deviceId = ref(route.params.id)
const deviceType = ref(route.query.type || 'Elevator')
const activeTab = ref('status')

// 实时数据对象 (对应 Java 的 JSONObject)
const realtimeData = ref({})
const lastUpdateTime = ref('')

// 图表控制
let chartInstance = null
let timer = null
const currentMetric = ref('speed') // 当前显示的指标 key
const metricName = ref('速度')     // 当前显示的指标名称

// 基础信息
const deviceBaseInfo = ref({ name: '加载中...', model: '-', status: '-', location: '-' })

// 状态映射表 (Modbus: 0=停止, 1=上行, 2=下行, 3=故障)
const statusMap = {
  0: { text: '停止待机', type: 'info' },
  1: { text: '正在上行', type: 'success' },
  2: { text: '正在下行', type: 'primary' },
  3: { text: '故障告警', type: 'danger' }
}

const goBack = () => router.push('/devices')
const formatNum = (val, fixed=2) => val ? parseFloat(val).toFixed(fixed) : '0'

// 切换图表指标
const switchMetric = (key, name) => {
  currentMetric.value = key
  metricName.value = name
  updateTrendChart(true) // 强制刷新图表
}

// 1. 获取设备档案
const fetchDetail = async () => {
  try {
    const res = await axios.get(`${API_BASE_URL}/devices/${deviceId.value}`)
    if (res.data) {
      deviceBaseInfo.value = {
        name: res.data.name,
        model: res.data.model || res.data.deviceCode,
        status: res.data.status,
        location: res.data.address
      }
      deviceType.value = res.data.type
    }
  } catch (e) {}
}

// 2. 获取实时状态 (Short Polling)
const fetchRealtimeStatus = async () => {
  try {
    const res = await axios.get(`${API_BASE_URL}/status/${deviceId.value}`)
    if (res.data && Object.keys(res.data).length > 0) {
      realtimeData.value = res.data
      if (res.data.ts) {
        lastUpdateTime.value = dayjs(res.data.ts).format('HH:mm:ss')
      }
    }
  } catch (e) { console.error('状态同步失败') }
}

// 3. 更新趋势图 (查 IoTDB)
const updateTrendChart = async (isInit = false) => {
  if (!chartInstance) return
  if (isInit) chartInstance.showLoading({ maskColor: 'rgba(0,0,0,0.2)', color: '#38bdf8' })

  try {
    // 动态查询当前选中的指标 (speed/current/temp/vibration)
    const res = await axios.get(`${API_BASE_URL}/telemetry/${deviceId.value}/${currentMetric.value}`)
    const data = res.data
    
    chartInstance.hideLoading()
    if (data && data.values) {
      chartInstance.setOption({
        xAxis: { data: data.times },
        series: [{ 
          name: metricName.value, 
          data: data.values,
          itemStyle: { color: getMetricColor() },
          areaStyle: {
             color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: getMetricColor(0.5) },
              { offset: 1, color: getMetricColor(0) }
            ])
          }
        }]
      })
    }
  } catch (e) { chartInstance.hideLoading() }
}

const getMetricColor = (opacity=1) => {
  const map = { speed: '#10b981', current: '#f59e0b', temp: '#a78bfa', vibration: '#f56c6c', floor: '#38bdf8' }
  const c = map[currentMetric.value] || '#38bdf8'
  if(opacity===1) return c
  return c // 简化处理，实际需转 rgba
}

const initChart = () => {
  const chartDom = document.getElementById('detailTrendChart')
  if (!chartDom) return
  if (echarts.getInstanceByDom(chartDom)) echarts.dispose(chartDom);
  chartInstance = echarts.init(chartDom)
  chartInstance.setOption({
    backgroundColor: 'transparent',
    grid: { top: 30, right: 20, bottom: 20, left: 50, containLabel: true },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: [], axisLabel: { color: '#94a3b8' } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#334155', type: 'dashed' } }, axisLabel: { color: '#94a3b8' } },
    series: [{ type: 'line', smooth: true, showSymbol: false }]
  })
  window.addEventListener('resize', () => chartInstance?.resize())
}

const getElevatorAnimClass = (status) => {
  if (status === 1) return 'moving-up'
  if (status === 2) return 'moving-down'
  return ''
}

// 生命周期
watch(() => route.params.id, (newId) => {
  deviceId.value = newId
  deviceType.value = route.query.type
  fetchDetail()
  if (timer) clearInterval(timer)
  if(deviceType.value === 'Elevator') {
    nextTick(() => {
      initChart()
      startPolling()
    })
  }
})

const startPolling = () => {
  fetchRealtimeStatus() // 立即执行一次
  updateTrendChart(true)
  timer = setInterval(() => {
    fetchRealtimeStatus() // 每秒刷状态
    updateTrendChart(false) // 每秒刷图表 (生产环境建议图表频率低一点，比如5s)
  }, 2000) // 2秒刷新一次
}

onMounted(() => {
  fetchDetail()
  if (deviceType.value === 'Elevator') {
    nextTick(() => {
      initChart()
      startPolling()
    })
  }
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  if (chartInstance) chartInstance.dispose()
})
</script>

<style scoped>
/* 保持原有布局样式 */
.detail-wrapper { display: flex; flex-direction: column; gap: 20px; }
.custom-header { background: rgba(30, 41, 59, 0.7); border: 1px solid rgba(56, 189, 248, 0.1); padding: 15px 20px; border-radius: 8px; color: #fff; }
:deep(.el-page-header__content) { color: #fff; }
.header-info { display: flex; align-items: center; }
.update-time { font-size: 12px; color: #64748b; margin-left: auto; font-family: monospace; }
.device-title { font-size: 18px; font-weight: bold; }
.ml-2 { margin-left: 10px; }
.content-grid { display: grid; grid-template-columns: 3fr 1fr; gap: 20px; }
.main-panel, .side-panel { background: rgba(30, 41, 59, 0.7); border: 1px solid rgba(56, 189, 248, 0.1); border-radius: 8px; padding: 20px; color: #e2e8f0; }

/* 监控卡片 (可点击) */
.monitor-cards { display: grid; grid-template-columns: repeat(5, 1fr); gap: 10px; margin-bottom: 25px; } /* 改为5列 */
.monitor-card { 
  background: rgba(15, 23, 42, 0.6); border: 1px solid #334155; padding: 15px 10px; border-radius: 6px; text-align: center; cursor: pointer; transition: all 0.2s;
}
.monitor-card:hover { background: rgba(56, 189, 248, 0.1); }
.monitor-card.active { border-color: #38bdf8; background: rgba(56, 189, 248, 0.2); box-shadow: 0 0 10px rgba(56, 189, 248, 0.2); }
.monitor-card .label { color: #94a3b8; font-size: 12px; margin-bottom: 5px; }
.monitor-card .value { font-size: 20px; font-weight: bold; }
.monitor-card .unit { font-size: 12px; color: #64748b; font-weight: normal; }

/* 颜色 */
.text-blue { color: #38bdf8; } .text-green { color: #10b981; } .text-yellow { color: #f59e0b; } .text-red { color: #F56C6C; } .text-purple { color: #a78bfa; }

/* 图表区 */
.chart-container { background: rgba(15, 23, 42, 0.4); border: 1px solid #334155; border-radius: 6px; padding: 15px; }
.chart-title { font-size: 14px; font-weight: bold; margin-bottom: 10px; border-left: 3px solid #38bdf8; padding-left: 10px; display: flex; align-items: center; }

/* 档案列表 */
.panel-header { font-size: 16px; font-weight: bold; margin-bottom: 15px; border-left: 3px solid #38bdf8; padding-left: 8px; }
.profile-item { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px dashed #334155; font-size: 13px; }
.profile-item .label { color: #94a3b8; } .profile-item .val { color: #e2e8f0; }

/* 简易电梯动画 */
.device-img { margin-top: 20px; height: 150px; background: rgba(15, 23, 42, 0.6); border: 1px dashed #334155; border-radius: 6px; display: flex; flex-direction: column; align-items: center; justify-content: center; position: relative; overflow: hidden; }
.elevator-animation { width: 80px; height: 100px; background: #334155; border: 2px solid #64748b; position: relative; display: flex; justify-content: center; align-items: center; }
.door { position: absolute; top: 0; width: 50%; height: 100%; background: #475569; border: 1px solid #1e293b; transition: all 0.5s; }
.door.left { left: 0; } .door.right { right: 0; }
.floor-display { z-index: 10; font-family: monospace; font-size: 24px; color: #f59e0b; font-weight: bold; background: #000; padding: 2px 6px; border-radius: 4px; }
/* 上下行微动效果 */
.moving-up { animation: moveUp 1s infinite alternate; }
.moving-down { animation: moveDown 1s infinite alternate; }
@keyframes moveUp { from { transform: translateY(0); } to { transform: translateY(-5px); } }
@keyframes moveDown { from { transform: translateY(0); } to { transform: translateY(5px); } }

/* 通用覆盖 */
:deep(.el-tabs__item) { color: #94a3b8; } :deep(.el-tabs__item.is-active) { color: #38bdf8; } :deep(.el-tabs__nav-wrap::after) { background-color: #334155; }
.empty-placeholder { color: #64748b; text-align: center; padding: 40px; }
.animate-pulse { animation: pulse 2s infinite; }
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.5; } }
</style>