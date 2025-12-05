<template>
  <div class="device-board">
    
    <div class="overview-section">
      <div class="stat-card total">
        <div class="label">设备总数</div>
        <div class="value">{{ stats.total }}</div>
        <div class="icon">🏢</div>
      </div>
      <div class="stat-card online">
        <div class="label">设备在线</div>
        <div class="value">{{ stats.online }}</div>
        <div class="sub">在线率 {{ stats.onlineRate }}%</div>
      </div>
      <div class="stat-card maintenance">
        <div class="label">离线/维修</div>
        <div class="value">{{ stats.offline }}</div>
        <div class="icon">🛠️</div>
      </div>
      <div class="stat-card fault">
        <div class="label">故障告警</div>
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
              <div class="risk-name" @click="goToDetailById(item.id)" style="cursor: pointer;">{{ item.name }}</div>
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
          <el-input v-model="search" placeholder="搜索编号/名称..." size="small" style="width: 200px" class="dark-input"/>
          <el-radio-group v-model="filterStatus" size="small" class="dark-radio">
            <el-radio-button label="ALL">全部</el-radio-button>
            <el-radio-button label="ONLINE">在线</el-radio-button>
            <el-radio-button label="FAULT">故障</el-radio-button>
          </el-radio-group>
          <el-button type="primary" size="small" icon="Refresh" @click="fetchDevices" :loading="loading">刷新</el-button>
        </div>
      </div>
      
      <el-table 
        :data="filteredList" 
        style="width: 100%" 
        height="300"
        v-loading="loading"
        element-loading-background="rgba(0, 0, 0, 0.5)"
        header-row-class-name="dark-header"
      >
        <el-table-column prop="deviceCode" label="设备编号" width="120" />
        
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
        <el-table-column prop="brand" label="品牌" width="120" />
        
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" effect="dark">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="address" label="部署地址" min-width="200" show-overflow-tooltip />
        
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
        </div>
      </div>
      <div style="color: #64748b; padding: 20px; text-align: center;">(告警日志模块数据暂未对接数据库)</div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios' // 引入 axios
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { API_BASE_URL } from '@/config'

const router = useRouter()
const search = ref('')
const filterStatus = ref('ALL')
const onlyUnresolved = ref(true)
const loading = ref(false)

// 真实设备列表
const deviceList = ref([])

// 统计数据 (计算属性)
const stats = computed(() => {
  const total = deviceList.value.length
  const online = deviceList.value.filter(d => d.status === '在线').length
  const fault = deviceList.value.filter(d => d.status === '故障').length
  const gateway = deviceList.value.filter(d => d.type === 'Gateway').length
  const offline = total - online
  
  return {
    total,
    online,
    fault,
    offline,
    gateway,
    elevatorTotal: total - gateway,
    elevatorOnline: online, // 简化计算
    maintenance: offline - fault,
    onlineRate: total > 0 ? ((online / total) * 100).toFixed(1) : 0
  }
})

// --- 核心：从后端获取数据 ---
const fetchDevices = async () => {
  loading.value = true
  try {
    // 请求 Spring Boot 接口
    const res = await axios.get(`${API_BASE_URL}/devices`)
    deviceList.value = res.data
    ElMessage.success('设备数据同步成功')
  } catch (error) {
    console.error(error)
    ElMessage.error('连接后端失败，请检查服务是否启动')
    // 失败时使用兜底 Mock 数据，方便演示
    deviceList.value = [
      { deviceCode: 'EL-MOCK-01', name: '演示电梯(离线)', type: 'Elevator', status: '离线', brand: 'Mock', address: '本地演示' }
    ]
  } finally {
    loading.value = false
  }
}

// 过滤逻辑
const filteredList = computed(() => {
  return deviceList.value.filter(d => {
    // 兼容 id 和 deviceCode 搜索
    const matchSearch = (d.name && d.name.includes(search.value)) || (d.deviceCode && d.deviceCode.includes(search.value))
    let matchStatus = true
    if (filterStatus.value === 'ONLINE') matchStatus = d.status === '在线'
    if (filterStatus.value === 'FAULT') matchStatus = d.status === '故障'
    return matchSearch && matchStatus
  })
})

const getStatusType = (status) => {
  if (status === '在线' || status === '运行中') return 'success'
  if (status === '故障') return 'danger'
  return 'info' // 离线或其他
}

const getScoreColor = (score) => {
  if (score < 70) return '#F56C6C'
  if (score < 85) return '#E6A23C'
  return '#67C23A'
}

const goToDetail = (row) => {
  // 使用 deviceCode 作为 ID 跳转
  router.push({ name: 'DeviceDetail', params: { id: row.deviceCode }, query: { type: row.type } })
}

// 模拟的 Risk 数据
const riskList = ref([
  { id: 'EL-002', name: '3号楼-货梯-B', location: 'A栋-3单元', score: 65 },
  { id: 'EL-003', name: '5号楼-消防梯', location: 'B栋-地下', score: 72 },
  { id: 'GW-001', name: 'NeuronEX-A区', location: 'A栋-机房', score: 82 }
])

const goToDetailById = (id) => {
  router.push({ name: 'DeviceDetail', params: { id: id }, query: { type: 'Elevator' } })
}

onMounted(() => {
  fetchDevices() // 页面加载时请求后端
  
  // 初始化图表 (保持不变)
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
.risk-loc { font-size: 12px; color: #64748b; }
.risk-score { width: 60px; text-align: right; font-size: 12px; color: #fff; }

/* 3. 列表通用样式 */
.list-section, .alarm-section {
  background: rgba(30, 41, 59, 0.7); border: 1px solid rgba(56, 189, 248, 0.1); border-radius: 8px; padding: 20px;
}
.section-header { display: flex; justify-content: space-between; margin-bottom: 15px; }
.mt-4 { margin-top: 20px; }

/* 表格样式 */
:deep(.el-table) { background-color: transparent; --el-table-tr-bg-color: transparent; --el-table-header-bg-color: rgba(15, 23, 42, 0.5); --el-table-row-hover-bg-color: rgba(56, 189, 248, 0.1); --el-table-border-color: #334155; color: #cbd5e1; }
:deep(.el-table th.el-table__cell) { background-color: rgba(15, 23, 42, 0.8); color: #94a3b8; }
:deep(.el-table td.el-table__cell) { border-bottom: 1px solid #334155; }
:deep(.el-input__wrapper) { background-color: rgba(15, 23, 42, 0.5); box-shadow: 0 0 0 1px #334155 inset; }
:deep(.el-input__inner) { color: #fff; }
:deep(.dark-radio .el-radio-button__inner) { background: transparent; color: #94a3b8; border-color: #334155; }
:deep(.dark-radio .el-radio-button__original-radio:checked + .el-radio-button__inner) { background: #38bdf8; color: #fff; border-color: #38bdf8; box-shadow: none; }

.text-blue { color: #38bdf8; } .text-purple { color: #a78bfa; } .text-red { color: #f87171; } .text-gray { color: #64748b; } .animate-pulse { animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite; }
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: .5; } }
</style>