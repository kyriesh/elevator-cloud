<template>
  <div class="cockpit-wrapper">
    <el-row :gutter="20" class="mb-4">
      <el-col :span="6" v-for="kpi in kpiList" :key="kpi.label">
        <div class="kpi-card">
          <div class="kpi-icon">{{ kpi.icon }}</div>
          <div class="kpi-info">
            <div class="label">{{ kpi.label }}</div>
            <div class="value" :style="{ color: kpi.color }">{{ kpi.value }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="height: calc(100vh - 200px);">
      <el-col :span="6">
        <div class="panel">
          <div class="panel-title">🔔 实时告警流</div>
          <div class="alert-list">
            <div v-for="(alert, i) in alerts" :key="i" class="alert-item">
              <div class="time">{{ alert.time }}</div>
              <div class="content">
                <el-tag size="small" type="danger" effect="dark" v-if="alert.level==='严重'">严重</el-tag>
                <el-tag size="small" type="warning" effect="dark" v-else>警告</el-tag>
                <span class="loc">{{ alert.location }}</span>
              </div>
              <div class="msg">{{ alert.msg }}</div>
            </div>
          </div>
        </div>
      </el-col>

      <el-col :span="12">
        <div class="panel map-panel">
          <div class="panel-title">🗺️ 设备分布热力图</div>
          <div id="mapChart" style="width: 100%; height: 100%;"></div>
        </div>
      </el-col>

      <el-col :span="6">
        <div class="panel h-50">
          <div class="panel-title">📈 故障类型占比</div>
          <div id="pieChart" style="width: 100%; height: 200px;"></div>
        </div>
        <div class="panel h-50 mt-4">
          <div class="panel-title">📊 健康度趋势</div>
          <div id="lineChart" style="width: 100%; height: 200px;"></div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'

const kpiList = ref([
  { label: '接入设备', value: '1,248', icon: '🏢', color: '#409EFF' },
  { label: '在线率', value: '98.5%', icon: '📶', color: '#67C23A' },
  { label: '今日告警', value: '3', icon: '🚨', color: '#F56C6C' },
  { label: '待处理工单', value: '12', icon: '🛠️', color: '#E6A23C' }
])

const alerts = ref([
  { time: '10:42', level: '严重', location: '1号楼-客梯', msg: '门锁回路断开 (E51)' },
  { time: '10:35', level: '警告', location: '3号楼-货梯', msg: '平层误差超标' },
  { time: '09:20', level: '警告', location: '5号楼-消防', msg: '运行震动异常' },
  { time: '08:15', level: '严重', location: '2号楼-观光', msg: '急停按钮被按下' },
])

const initCharts = () => {
  // 1. 模拟地图散点
  const mapChart = echarts.init(document.getElementById('mapChart'))
  mapChart.setOption({
    backgroundColor: 'transparent',
    xAxis: { show: false }, yAxis: { show: false },
    series: [{
      type: 'effectScatter',
      symbolSize: 20,
      data: [[20, 30], [50, 60], [80, 20]], // 模拟坐标
      itemStyle: { color: '#F56C6C' },
      rippleEffect: { scale: 4, brushType: 'stroke' }
    }, {
      type: 'scatter',
      symbolSize: 10,
      data: [[10, 10], [30, 80], [70, 50], [90, 90]],
      itemStyle: { color: '#409EFF' }
    }]
  })

  // 2. 饼图
  const pieChart = echarts.init(document.getElementById('pieChart'))
  pieChart.setOption({
    series: [{
      type: 'pie', radius: ['40%', '70%'],
      data: [
        { value: 48, name: '门系统', itemStyle: { color: '#E6A23C' } },
        { value: 30, name: '控制系', itemStyle: { color: '#409EFF' } },
        { value: 22, name: '曳引系', itemStyle: { color: '#67C23A' } }
      ],
      label: { color: '#fff' }
    }]
  })

  // 3. 折线图
  const lineChart = echarts.init(document.getElementById('lineChart'))
  lineChart.setOption({
    xAxis: { type: 'category', data: ['周一','周二','周三','周四','周五'], axisLabel: { color: '#ccc' } },
    yAxis: { type: 'value', axisLabel: { color: '#ccc' }, splitLine: { lineStyle: { color: '#333' } } },
    series: [{ data: [92, 93, 90, 94, 98], type: 'line', smooth: true, areaStyle: {} }]
  })
}

onMounted(() => {
  setTimeout(initCharts, 100) // 延时确保DOM渲染
})
</script>

<style scoped>
.cockpit-wrapper { background-color: #0b0f19; padding: 20px; min-height: 100vh; color: #fff; margin: -20px; /* 抵消layout的padding */ }
.mb-4 { margin-bottom: 20px; }
.mt-4 { margin-top: 20px; }
.h-50 { height: 48%; }

/* KPI 卡片 */
.kpi-card { background: rgba(255,255,255,0.05); padding: 20px; display: flex; align-items: center; border: 1px solid #333; border-radius: 4px; }
.kpi-icon { font-size: 32px; margin-right: 15px; }
.label { font-size: 14px; color: #aaa; }
.value { font-size: 24px; font-weight: bold; }

/* 通用面板 */
.panel { background: rgba(255,255,255,0.03); border: 1px solid #333; height: 100%; padding: 15px; border-radius: 4px; display: flex; flex-direction: column; }
.map-panel { border: 1px solid #409EFF; box-shadow: 0 0 10px rgba(64,158,255,0.2) inset; }
.panel-title { font-size: 16px; font-weight: bold; margin-bottom: 15px; border-left: 3px solid #409EFF; padding-left: 10px; color: #eee; }

/* 告警列表 */
.alert-list { overflow-y: auto; flex: 1; }
.alert-item { padding: 10px; border-bottom: 1px solid #333; font-size: 13px; }
.alert-item .time { color: #888; font-size: 12px; margin-bottom: 4px; }
.alert-item .loc { margin-left: 8px; font-weight: bold; color: #ddd; }
.alert-item .msg { color: #aaa; margin-top: 4px; }
</style>