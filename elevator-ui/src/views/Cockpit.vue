<template>
  <div class="cockpit-board">
    
    <div class="header-kpi">
      <div class="kpi-box">
        <div class="num text-blue">1,248</div>
        <div class="label">接入设备总数</div>
      </div>
      <div class="kpi-box">
        <div class="num text-green">99.2%</div>
        <div class="label">今日可用率</div>
      </div>
      <div class="kpi-box center-box">
        <div class="safety-score">
          <span class="score-val">96</span>
          <span class="score-label">安全指数</span>
        </div>
      </div>
      <div class="kpi-box">
        <div class="num text-red">3</div>
        <div class="label">当前严重告警</div>
      </div>
      <div class="kpi-box">
        <div class="num text-yellow">12</div>
        <div class="label">今日维保工单</div>
      </div>
    </div>

    <div class="main-grid">
      
      <div class="side-col left-col">
        <div class="panel-box">
          <div class="panel-header">
            <span class="title">🔔 实时告警监控</span>
            <el-tag size="small" type="danger" effect="dark" class="blink-tag">LIVE</el-tag>
          </div>
          <div class="scroll-list">
            <div v-for="(alarm, i) in alarmList" :key="i" class="list-item alarm-item">
              <div class="item-left">
                <div class="time">{{ alarm.time }}</div>
                <div class="loc">{{ alarm.location }}</div>
              </div>
              <div class="item-right">
                <div class="tag" :class="getLevelColor(alarm.level)">{{ alarm.code }}</div>
                <div class="desc text-gray">{{ alarm.msg }}</div>
              </div>
            </div>
          </div>
        </div>
        <div class="panel-box">
          <div class="panel-header">
            <span class="title">🛠️ 今日工单执行概览</span>
          </div>
          <div id="workOrderChart" style="width: 100%; height: 220px;"></div>
        </div>
      </div>

      <div class="center-col">
        <div id="amap-container" class="map-container"></div>
        <div class="map-legend">
          <div class="legend-item"><span class="dot bg-green"></span> 正常电梯</div>
          <div class="legend-item"><span class="dot bg-red blink"></span> 故障电梯</div>
          <div class="legend-item"><span class="dot bg-blue"></span> 维修人员</div>
        </div>
      </div>

      <div class="side-col right-col">
        <div class="panel-box">
          <div class="panel-header">
            <span class="title">📈 24H 故障与预警趋势</span>
          </div>
          <div id="trendChart" style="width: 100%; height: 220px;"></div>
        </div>
        <div class="panel-box">
          <div class="panel-header">
            <span class="title">✅ 最近完工记录</span>
            <el-tag size="small" type="success" effect="dark">Fixed</el-tag>
          </div>
          <div class="scroll-list">
            <div v-for="(record, i) in maintenanceList" :key="i" class="list-item fix-item">
              <div class="item-left">
                <div class="time text-green font-mono">{{ record.time }}</div>
                <div class="loc">{{ record.location }}</div>
              </div>
              <div class="item-right">
                <div class="worker">
                  <el-icon><User /></el-icon> {{ record.worker }}
                </div>
                <div class="desc text-blue">{{ record.type }}完成</div>
              </div>
            </div>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import AMapLoader from '@amap/amap-jsapi-loader'

// 获取路由实例
const router = useRouter()

// ==========================================
// 🚨 请在此处填入你的高德地图 Key 和 密钥
// ==========================================
const AMAP_KEY = 'ab982a51386a15c7bdc51d90bd81b587';             // 例如: 'a1b2c3d4...'
const AMAP_SECURITY_CODE = 'bf737e01172f10820ece8723dc541e6a';  // 例如: 'e5f6g7h8...'

window._AMapSecurityConfig = { securityJsCode: AMAP_SECURITY_CODE }

// --- 核心修改 1: 增加地址信息的电梯数据 ---
const elevators = [
  { id: 'E01', name: '1号楼-客梯', address: '科技园A区-1号研发楼-东侧大厅', status: 'normal', lnglat: [116.405285, 39.904989] },
  { id: 'E02', name: '3号楼-货梯', address: '科技园A区-3号生产楼-北侧货运通道', status: 'fault', lnglat: [116.406465, 39.905818] },
  { id: 'E03', name: '5号楼-消防梯', address: '科技园B区-5号行政楼-地下车库入口', status: 'normal', lnglat: [116.407265, 39.904018] },
  { id: 'E04', name: '2号楼-观光梯', address: '科技园C区-2号展示中心-中央大厅', status: 'normal', lnglat: [116.404265, 39.903818] }
]

const workers = [
  { name: '张伟', status: 'free', phone: '13800138000', lnglat: [116.405000, 39.904500] },
  { name: '李强', status: 'busy', phone: '13900139000', lnglat: [116.407000, 39.905500] }
]

// --- 其他 Mock Data ---
const alarmList = ref([
  { time: '14:22', location: '3号楼-货梯-B', level: 'high', code: 'E51', msg: '门锁回路断开' },
  { time: '14:15', location: '1号楼-客梯-A', level: 'mid', code: 'W02', msg: '平层误差超标' },
  { time: '13:40', location: '5号楼-消防梯', level: 'low', code: 'W10', msg: '运行震动异常' },
  { time: '12:05', location: '2号楼-观光梯', level: 'high', code: 'E30', msg: '变频器过热' }
])

const maintenanceList = ref([
  { time: '2023-12-01 14:10', location: '2号楼-客梯-B', worker: '张伟', type: '故障修复' },
  { time: '2023-12-01 13:50', location: '5号楼-消防梯', worker: '李强', type: '季度保养' },
  { time: '2023-12-01 11:20', location: 'A区-大厅扶梯', worker: '王师傅', type: '润滑加油' },
  { time: '2023-12-01 10:00', location: '1号楼-货梯-A', worker: '张伟', type: '更换光幕' },
])

let map = null
let charts = [] 

// --- 核心修改 2: 全局跳转函数 ---
// 因为 InfoWindow 是原生 HTML 字符串，无法直接用 @click，必须通过 window 对象调用
window.jumpToDetail = (id) => {
  router.push({ 
    name: 'DeviceDetail', 
    params: { id: id },
    query: { type: 'Elevator' } // 默认跳电梯详情
  })
}

// --- 初始化地图 ---
const initAMap = () => {
  AMapLoader.load({
    key: AMAP_KEY,
    version: '2.0',
    plugins: ['AMap.Scale', 'AMap.ToolBar', 'AMap.InfoWindow'],
  }).then((AMap) => {
    map = new AMap.Map('amap-container', {
      viewMode: '3D',
      pitch: 45,
      zoom: 16,
      center: [116.405285, 39.904989],
      mapStyle: 'amap://styles/darkblue',
    })

    // 绘制电梯
    elevators.forEach(device => {
      const isFault = device.status === 'fault'
      const iconColor = isFault ? '#f56c6c' : '#67c23a'
      const iconHtml = `
        <div style="
          background: ${iconColor}; width: 24px; height: 24px; border-radius: 50%; 
          border: 2px solid #fff; box-shadow: 0 0 10px ${iconColor};
          display: flex; justify-content: center; align-items: center; color: white; font-size: 12px;">
          ${isFault ? '!' : 'E'}
        </div>
        ${isFault ? '<div class="pulse-ring"></div>' : ''} 
      `
      
      const marker = new AMap.Marker({
        position: device.lnglat,
        content: iconHtml,
        anchor: 'bottom-center',
        offset: new AMap.Pixel(0, 0)
      })

      // 点击弹窗
      marker.on('click', () => {
        // 构建信息窗体 HTML
        const content = `
          <div style="color: #333; padding: 5px; min-width: 200px;">
            <h4 style="margin:0 0 5px 0; font-size: 16px;">${device.name}</h4>
            <div style="font-size:12px; color:#666; margin-bottom:5px;">
              <span style="display:inline-block; width:14px;">📍</span>${device.address}
            </div>
            <div style="margin-bottom:10px;">
              状态: <span style="color:${iconColor}; font-weight:bold">${isFault ? '故障告警' : '运行正常'}</span>
            </div>
            <button onclick="jumpToDetail('${device.id}')" style="
              width: 100%; background: #38bdf8; border: none; color: white; 
              padding: 6px; border-radius: 4px; cursor: pointer; font-size: 12px;">
              查看设备详情 >
            </button>
          </div>
        `
        const infoWindow = new AMap.InfoWindow({
          content: content,
          offset: new AMap.Pixel(0, -30),
          closeWhenClickMap: true
        })
        infoWindow.open(map, device.lnglat)
      })

      map.add(marker)
    })

    // 绘制工人
    workers.forEach(worker => {
      const marker = new AMap.Marker({
        position: worker.lnglat,
        content: `
          <div style="background: #409eff; color: white; padding: 2px 6px; border-radius: 4px; font-size: 12px; border: 1px solid white; display: flex; align-items: center; gap: 4px;">
            <span style="font-weight: bold;">🛠️</span> ${worker.name}
          </div>
        `,
        anchor: 'bottom-center',
        offset: new AMap.Pixel(0, -10)
      })
      map.add(marker)
    })

  }).catch(e => {
    console.error('地图加载失败:', e)
  })
}

// --- ECharts 图表逻辑 ---
const initECharts = () => {
  const woChart = echarts.init(document.getElementById('workOrderChart'))
  woChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, textStyle: { color: '#94a3b8' } },
    series: [{
      name: '工单状态', type: 'pie', radius: ['50%', '70%'], center: ['50%', '45%'], label: { show: false },
      data: [{ value: 8, name: '处理中', itemStyle: { color: '#e6a23c' } }, { value: 15, name: '已完成', itemStyle: { color: '#67c23a' } }, { value: 4, name: '待派单', itemStyle: { color: '#f56c6c' } }]
    }]
  })
  charts.push(woChart)

  const trendChart = echarts.init(document.getElementById('trendChart'))
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { top: 20, right: 20, bottom: 20, left: 30, containLabel: true },
    xAxis: { type: 'category', data: ['00','04','08','12','16','20'], axisLabel: { color: '#94a3b8' } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#334155', type: 'dashed' } }, axisLabel: { color: '#94a3b8' } },
    series: [{
      data: [2, 1, 5, 8, 3, 4], type: 'line', smooth: true, symbol: 'none',
      lineStyle: { width: 3, color: '#38bdf8' },
      areaStyle: { color: new echarts.graphic.LinearGradient(0,0,0,1, [{offset:0, color:'rgba(56,189,248,0.5)'},{offset:1, color:'rgba(56,189,248,0)'}]) }
    }]
  })
  charts.push(trendChart)
}

const getLevelColor = (level) => {
  if (level === 'high') return 'text-red'
  if (level === 'mid') return 'text-yellow'
  return 'text-blue'
}

const resizeHandler = () => charts.forEach(c => c.resize())

onMounted(() => {
  nextTick(() => {
    initECharts()
    initAMap()
    window.addEventListener('resize', resizeHandler)
  })
})

onUnmounted(() => {
  if (map) map.destroy()
  window.removeEventListener('resize', resizeHandler)
  // 清理挂载在 window 上的函数，防止污染
  delete window.jumpToDetail
  charts.forEach(c => c.dispose())
})
</script>

<style scoped>
/* 样式与之前一致，保留布局核心 */
.cockpit-board { height: calc(100vh - 84px); display: flex; flex-direction: column; padding: 0 20px 20px 20px; overflow: hidden; }
.header-kpi { display: flex; justify-content: space-between; align-items: center; padding: 0 30px; height: 80px; background: linear-gradient(to bottom, rgba(15, 23, 42, 0.9), rgba(15, 23, 42, 0)); z-index: 10; margin-bottom: 10px; }
.kpi-box { text-align: center; width: 150px; }
.kpi-box .num { font-size: 28px; font-weight: bold; font-family: 'Monaco', monospace; text-shadow: 0 0 10px rgba(255,255,255,0.3); }
.kpi-box .label { font-size: 12px; color: #94a3b8; margin-top: 5px; }
.center-box { width: 200px; transform: translateY(10px); }
.safety-score { width: 100px; height: 100px; margin: 0 auto; background: radial-gradient(circle, rgba(16,185,129,0.2) 0%, rgba(15,23,42,0.8) 70%); border: 2px solid #10b981; border-radius: 50%; display: flex; flex-direction: column; justify-content: center; align-items: center; box-shadow: 0 0 20px rgba(16,185,129,0.4); }
.score-val { font-size: 36px; font-weight: bold; color: #10b981; }
.score-label { font-size: 10px; color: #a7f3d0; }
.main-grid { flex: 1; display: grid; grid-template-columns: 1fr 2fr 1fr; gap: 20px; min-height: 0; }
.side-col { display: flex; flex-direction: column; gap: 20px; min-width: 0; }
.panel-box { flex: 1; background: rgba(30, 41, 59, 0.6); border: 1px solid rgba(56, 189, 248, 0.15); border-radius: 8px; padding: 15px; display: flex; flex-direction: column; backdrop-filter: blur(5px); min-height: 0; }
.panel-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; border-left: 3px solid #38bdf8; padding-left: 10px; }
.panel-header .title { font-size: 16px; font-weight: bold; color: #e2e8f0; }
.center-col { position: relative; border: 1px solid rgba(56, 189, 248, 0.2); background: rgba(15, 23, 42, 0.3); border-radius: 8px; min-width: 0; overflow: hidden; }
.map-container { width: 100%; height: 100%; }
.map-legend { position: absolute; bottom: 20px; left: 20px; background: rgba(15, 23, 42, 0.8); padding: 10px; border-radius: 4px; border: 1px solid #334155; display: flex; flex-direction: column; gap: 5px; }
.legend-item { color: #ccc; font-size: 12px; display: flex; align-items: center; gap: 8px; }
.dot { width: 8px; height: 8px; border-radius: 50%; display: inline-block; }
.scroll-list { flex: 1; overflow-y: auto; padding-right: 5px; }
.list-item { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px dashed #334155; font-size: 12px; }
.item-left .time { margin-bottom: 2px; }
.item-left .loc { color: #e2e8f0; font-weight: bold; }
.item-right { text-align: right; }
.item-right .desc { font-size: 12px; margin-top: 2px; }
.alarm-item .time { color: #64748b; }
.alarm-item .tag { font-weight: bold; font-family: monospace; }
.fix-item .worker { color: #e2e8f0; display: flex; align-items: center; justify-content: flex-end; gap: 4px; }
.font-mono { font-family: 'Monaco', monospace; }
.blink-tag { animation: blink 1.5s infinite; }
@keyframes blink { 0% { opacity: 1; } 50% { opacity: 0.5; } 100% { opacity: 1; } }
:deep(.pulse-ring) { position: absolute; top: -10px; left: -10px; width: 44px; height: 44px; border: 2px solid #f56c6c; border-radius: 50%; animation: pulse 1.5s infinite; }
@keyframes pulse { 0% { transform: scale(0.5); opacity: 1; } 100% { transform: scale(1.2); opacity: 0; } }
.text-blue { color: #38bdf8; } .text-green { color: #10b981; } .text-red { color: #f56c6c; } .text-yellow { color: #f59e0b; } .text-gray { color: #94a3b8; } .bg-green { background: #67c23a; } .bg-red { background: #f56c6c; } .bg-blue { background: #409eff; }
</style>