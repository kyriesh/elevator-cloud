<template>
  <div class="device-board">
    
    <div class="overview-section">
      <div class="stat-card total">
        <div class="label">设备总数</div>
        <div class="value">{{ stats.total }}</div>
        <div class="icon">🏢</div>
      </div>
      <div class="stat-card online">
        <div class="label">电梯在线</div>
        <div class="value">{{ stats.elevatorOnline }}</div>
        <div class="sub">共 {{ stats.elevatorTotal }} 台</div>
      </div>
      <div class="stat-card maintenance">
        <div class="label">维修中</div>
        <div class="value">{{ stats.maintenance }}</div>
        <div class="icon">🛠️</div>
      </div>
      <div class="stat-card fault">
        <div class="label">严重故障</div>
        <div class="value">{{ stats.fault }}</div>
        <div class="icon animate-pulse">🚨</div>
      </div>
      <div class="stat-card gateway">
        <div class="label">边缘网关</div>
        <div class="value">{{ stats.gateway }}</div>
        <div class="icon">📟</div>
      </div>
    </div>

    <div class="chart-section">
      <div class="chart-box">
        <div class="section-title">📊 24小时告警与故障趋势</div>
        <div id="trendChart" style="width: 100%; height: 250px;"></div>
      </div>

      <div class="chart-box">
        <div class="section-title">⚠️ 设备健康风险 Top 5</div>
        <div class="risk-list">
          <div v-for="(item, index) in riskList" :key="index" class="risk-item">
            <div class="rank-idx">{{ index + 1 }}</div>
            <div class="risk-info">
              <div class="risk-name" @click="goToDetailById(item.id, 'Elevator')" style="cursor: pointer;">{{ item.name }}</div>
              <div class="risk-loc">{{ item.location }}</div>
            </div>
            <div class="risk-score">
              <span>{{ item.score }}分</span>
              <el-progress :percentage="item.score" :color="getScoreColor(item.score)" :show-text="false" :stroke-width="6"/>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="list-section">
      <div class="section-header">
        <div class="section-title">📋 实时设备状态列表</div>
        <div class="filters">
          <el-input v-model="search" placeholder="搜索ID/名称..." size="small" style="width: 200px" class="dark-input"/>
          <el-radio-group v-model="filterStatus" size="small" class="dark-radio">
            <el-radio-button label="ALL">全部</el-radio-button>
            <el-radio-button label="ONLINE">在线</el-radio-button>
            <el-radio-button label="FAULT">故障</el-radio-button>
          </el-radio-group>
        </div>
      </div>
      
      <el-table 
        :data="filteredList" 
        style="width: 100%" 
        height="300"
        :row-class-name="tableRowClassName"
        header-row-class-name="dark-header"
      >
        <el-table-column prop="id" label="设备ID" width="120" />
        <el-table-column label="设备名称" min-width="180">
          <template #default="{ row }">
            <span :class="{'text-blue': row.type==='Elevator', 'text-purple': row.type==='Gateway'}">
              <el-icon v-if="row.type==='Gateway'"><Cpu /></el-icon>
              <el-icon v-else><OfficeBuilding /></el-icon>
              {{ row.name }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="120" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" effect="dark">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alert" label="当前状态描述" min-width="200">
          <template #default="{ row }">
            <span v-if="row.alert" class="text-red"><el-icon><Warning /></el-icon> {{ row.alert }}</span>
            <span v-else class="text-gray">运行平稳</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="goToDetail(row)">详情 ></el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="alarm-section mt-4">
      <div class="section-header">
        <div class="section-title text-red">🔔 全局告警实时监控中心</div>
        <div class="filters">
           <el-checkbox v-model="onlyUnresolved" label="仅看未恢复" border size="small" style="margin-right: 10px; color: #cbd5e1;" />
           <el-button type="danger" size="small" plain icon="Download">导出日志</el-button>
        </div>
      </div>

      <el-table 
        :data="filteredAlarms" 
        style="width: 100%" 
        height="350"
        header-row-class-name="dark-header"
      >
        <el-table-column prop="time" label="发生时间" width="180" />
        <el-table-column prop="level" label="等级" width="100">
          <template #default="{ row }">
            <el-tag :type="row.level === '严重' ? 'danger' : 'warning'" effect="dark">{{ row.level }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deviceName" label="关联设备" width="180">
          <template #default="{ row }">
            <span class="device-link" @click="goToDetailById(row.deviceId, 'Elevator')">{{ row.deviceName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="code" label="错误码" width="100">
          <template #default="{ row }">
            <span class="font-mono">{{ row.code }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="告警内容" min-width="250" />
        <el-table-column prop="status" label="当前状态" width="120">
          <template #default="{ row }">
            <span :class="row.status === '未恢复' ? 'text-red blink-text' : 'text-green'">
              <el-icon v-if="row.status === '未恢复'"><CircleClose /></el-icon>
              <el-icon v-else><CircleCheck /></el-icon>
              {{ row.status }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="处理" width="100" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status==='未恢复'" type="danger" size="small" plain>派单</el-button>
            <el-button v-else type="info" size="small" text disabled>已归档</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'

const router = useRouter()
const search = ref('')
const filterStatus = ref('ALL')
const onlyUnresolved = ref(true) // 默认开启只看未恢复

// 1. Mock Data: 概览
const stats = ref({
  total: 142, elevatorTotal: 120, elevatorOnline: 115, maintenance: 3, fault: 2, gateway: 22
})

// 2. Mock Data: 风险排名
const riskList = ref([
  { id: 'EL-002', name: '3号楼-货梯-B', location: 'A栋-3单元', score: 65 },
  { id: 'EL-003', name: '5号楼-消防梯', location: 'B栋-地下', score: 72 },
  { id: 'EL-004', name: '2号楼-客梯-A', location: 'C栋-大厅', score: 78 },
  { id: 'GW-001', name: 'NeuronEX-A区', location: 'A栋-机房', score: 82 },
  { id: 'EL-001', name: '1号楼-客梯-C', location: 'A栋-2单元', score: 88 }
])

// 3. Mock Data: 设备列表
const deviceList = ref([
  { id: 'EL-001', name: '1号楼-客梯-A', type: 'Elevator', status: '运行中', alert: '' },
  { id: 'EL-002', name: '3号楼-货梯-B', type: 'Elevator', status: '故障中', alert: 'E51 门锁回路断开' },
  { id: 'GW-001', name: 'NeuronEX-A区', type: 'Gateway', status: '运行中', alert: '' },
  { id: 'EL-003', name: '5号楼-消防梯', type: 'Elevator', status: '维修中', alert: '正在进行季度保养' },
  { id: 'EL-004', name: '2号楼-客梯-A', type: 'Elevator', status: '运行中', alert: '震动轻微异常' },
  { id: 'GW-002', name: 'NeuronEX-B区', type: 'Gateway', status: '在线', alert: '' },
])

// 4. Mock Data: 全局告警日志 (NEW)
const globalAlarms = ref([
  { time: '2023-12-01 14:22:05', deviceId: 'EL-002', deviceName: '3号楼-货梯-B', level: '严重', code: 'E51', content: '门锁回路异常断开，电梯困人风险', status: '未恢复' },
  { time: '2023-12-01 13:15:00', deviceId: 'GW-002', deviceName: 'NeuronEX-B区', level: '警告', code: 'NET_ERR', content: '上行链路丢包率 > 5%', status: '未恢复' },
  { time: '2023-12-01 10:30:00', deviceId: 'EL-003', deviceName: '5号楼-消防梯', level: '警告', code: 'W02', content: '平层精度偏差 12mm', status: '自动恢复' },
  { time: '2023-11-30 22:00:00', deviceId: 'EL-004', deviceName: '2号楼-客梯-A', level: '严重', code: 'E30', content: '变频器过热保护', status: '已修复' },
  { time: '2023-11-30 18:45:12', deviceId: 'EL-001', deviceName: '1号楼-客梯-A', level: '警告', code: 'W10', content: '轿厢照明电压不稳', status: '已修复' },
  { time: '2023-11-30 09:00:00', deviceId: 'EL-002', deviceName: '3号楼-货梯-B', level: '严重', code: 'E51', content: '门刀卡阻', status: '已修复' },
])

// --- 计算属性 ---
const filteredList = computed(() => {
  return deviceList.value.filter(d => {
    const matchSearch = d.name.includes(search.value) || d.id.includes(search.value)
    let matchStatus = true
    if (filterStatus.value === 'ONLINE') matchStatus = d.status === '运行中'
    if (filterStatus.value === 'FAULT') matchStatus = d.status === '故障中'
    return matchSearch && matchStatus
  })
})

const filteredAlarms = computed(() => {
  if (onlyUnresolved.value) {
    return globalAlarms.value.filter(a => a.status === '未恢复')
  }
  return globalAlarms.value
})

// --- 方法 ---
const getStatusType = (status) => {
  if (status === '运行中' || status === '在线') return 'success'
  if (status === '故障中') return 'danger'
  return 'warning'
}

const getScoreColor = (score) => {
  if (score < 70) return '#F56C6C'
  if (score < 85) return '#E6A23C'
  return '#67C23A'
}

const goToDetail = (row) => {
  router.push({ name: 'DeviceDetail', params: { id: row.id }, query: { type: row.type } })
}

const goToDetailById = (id, type) => {
  router.push({ name: 'DeviceDetail', params: { id: id }, query: { type: type || 'Elevator' } })
}

onMounted(() => {
  const chart = echarts.init(document.getElementById('trendChart'))
  chart.setOption({
    grid: { top: 30, right: 20, bottom: 20, left: 40, containLabel: true },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: ['00:00','04:00','08:00','12:00','16:00','20:00'], axisLabel: { color: '#94a3b8' } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#334155', type: 'dashed' } }, axisLabel: { color: '#94a3b8' } },
    series: [{
      name: '告警次数', type: 'line', smooth: true, data: [2, 1, 5, 12, 8, 3],
      itemStyle: { color: '#38bdf8' },
      areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(56, 189, 248, 0.5)' }, { offset: 1, color: 'rgba(56, 189, 248, 0)' }]) }
    }]
  })
})
</script>

<style scoped>
.device-board { display: flex; flex-direction: column; gap: 20px; }

/* 1. 总览卡片 */
.overview-section { display: grid; grid-template-columns: repeat(5, 1fr); gap: 15px; }
.stat-card {
  background: rgba(30, 41, 59, 0.7); border: 1px solid rgba(56, 189, 248, 0.1); border-radius: 8px; padding: 15px; position: relative;
}
.stat-card .label { color: #94a3b8; font-size: 13px; margin-bottom: 5px; }
.stat-card .value { font-size: 28px; font-weight: bold; color: #f1f5f9; }
.stat-card .sub { font-size: 12px; color: #64748b; margin-top: 5px; }
.stat-card .icon { position: absolute; right: 10px; bottom: 10px; font-size: 30px; opacity: 0.2; }
.stat-card.total { border-top: 3px solid #38bdf8; }
.stat-card.online { border-top: 3px solid #10b981; }
.stat-card.maintenance { border-top: 3px solid #f59e0b; }
.stat-card.fault { border-top: 3px solid #ef4444; }
.stat-card.fault .value { color: #ef4444; }
.stat-card.gateway { border-top: 3px solid #8b5cf6; }

/* 2. 中间图表区 */
.chart-section { display: grid; grid-template-columns: 2fr 1fr; gap: 20px; }
.chart-box { background: rgba(30, 41, 59, 0.7); border: 1px solid rgba(56, 189, 248, 0.1); border-radius: 8px; padding: 15px; }
.section-title { font-size: 16px; font-weight: bold; color: #e2e8f0; margin-bottom: 15px; border-left: 3px solid #38bdf8; padding-left: 10px; }

/* 风险排名列表 */
.risk-item { display: flex; align-items: center; padding: 10px 0; border-bottom: 1px dashed #334155; }
.rank-idx { width: 20px; height: 20px; background: #ef4444; color: #fff; text-align: center; line-height: 20px; border-radius: 3px; font-size: 12px; margin-right: 10px; }
.risk-info { flex: 1; }
.risk-name { font-size: 14px; color: #cbd5e1; }
.risk-name:hover { color: #38bdf8; text-decoration: underline; }
.risk-loc { font-size: 12px; color: #64748b; }
.risk-score { width: 60px; text-align: right; font-size: 12px; color: #fff; }

/* 3. 列表通用样式 */
.list-section, .alarm-section {
  background: rgba(30, 41, 59, 0.7); border: 1px solid rgba(56, 189, 248, 0.1); border-radius: 8px; padding: 20px;
}
.section-header { display: flex; justify-content: space-between; margin-bottom: 15px; }
.mt-4 { margin-top: 20px; }

/* 表格深色适配 */
:deep(.el-table) {
  background-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: rgba(15, 23, 42, 0.5);
  --el-table-row-hover-bg-color: rgba(56, 189, 248, 0.1);
  --el-table-border-color: #334155;
  color: #cbd5e1;
}
:deep(.el-table th.el-table__cell) { background-color: rgba(15, 23, 42, 0.8); color: #94a3b8; }
:deep(.el-table td.el-table__cell) { border-bottom: 1px solid #334155; }
:deep(.el-input__wrapper) { background-color: rgba(15, 23, 42, 0.5); box-shadow: 0 0 0 1px #334155 inset; }
:deep(.el-input__inner) { color: #fff; }

/* 单选框深色适配 */
:deep(.dark-radio .el-radio-button__inner) { background: transparent; color: #94a3b8; border-color: #334155; }
:deep(.dark-radio .el-radio-button__original-radio:checked + .el-radio-button__inner) { background: #38bdf8; color: #fff; border-color: #38bdf8; box-shadow: none; }

/* 颜色工具类 */
.text-blue { color: #38bdf8; }
.text-purple { color: #a78bfa; }
.text-red { color: #f87171; }
.text-gray { color: #64748b; }
.text-green { color: #34d399; }
.animate-pulse { animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite; }
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: .5; } }

/* 告警日志特有 */
.device-link { cursor: pointer; color: #38bdf8; text-decoration: underline; }
.device-link:hover { color: #7dd3fc; }
.font-mono { font-family: monospace; }
.blink-text { animation: blink 1.5s infinite; }
@keyframes blink { 0% { opacity: 1; } 50% { opacity: 0.5; } 100% { opacity: 1; } }
</style>