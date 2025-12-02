<template>
  <div class="detail-wrapper">
    <el-page-header @back="goBack" class="custom-header">
      <template #content>
        <div class="header-info">
          <span class="device-title">{{ deviceId }} - {{ deviceBaseInfo.name }}</span>
          <el-tag :type="deviceBaseInfo.status === '在线' ? 'success' : 'danger'" effect="dark" class="ml-2">
            {{ deviceBaseInfo.status }}
          </el-tag>
          <el-tag v-if="deviceType === 'Gateway'" type="info" effect="dark" class="ml-2">边缘网关</el-tag>
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
      <el-empty description="边缘网关详情页正在建设中...">
        <template #image>
          <el-icon :size="100" color="#334155"><Cpu /></el-icon>
        </template>
        <el-button type="primary" @click="goBack">返回列表</el-button>
      </el-empty>
    </div>

    <div v-else class="content-grid">
      
      <div class="main-panel">
        <el-tabs v-model="activeTab" class="custom-tabs">
          
          <el-tab-pane label="🚀 实时监控与预测" name="status">
            <div class="prediction-section mb-4">
              <div class="pred-title">🤖 AI 故障预测分析</div>
              <div class="pred-content">
                <div class="health-score">
                  <el-progress type="dashboard" :percentage="92" :color="colors" :width="120">
                    <template #default="{ percentage }">
                      <div class="score-val">{{ percentage }}</div>
                      <div class="score-label">健康评分</div>
                    </template>
                  </el-progress>
                </div>
                <div class="pred-info">
                  <div class="info-row">
                    <span class="label">当前健康状态：</span>
                    <el-tag type="success" effect="dark">健康 (低风险)</el-tag>
                  </div>
                  <div class="info-row">
                    <span class="label">未来24h故障概率：</span>
                    <span class="text-green font-bold">0.8%</span>
                  </div>
                  <div class="info-row">
                    <span class="label">重点关注部件：</span>
                    <span class="text-yellow">门机皮带 (建议检查张紧度)</span>
                  </div>
                </div>
              </div>
            </div>

            <div class="monitor-cards">
              <div class="monitor-card">
                <div class="label">当前楼层</div>
                <div class="value text-blue">14 <span class="unit">F</span></div>
              </div>
              <div class="monitor-card">
                <div class="label">运行速度</div>
                <div class="value text-green">1.75 <span class="unit">m/s</span></div>
              </div>
              <div class="monitor-card">
                <div class="label">轿厢温度</div>
                <div class="value">26.5 <span class="unit">℃</span></div>
              </div>
              <div class="monitor-card">
                <div class="label">门机电流</div>
                <div class="value text-yellow">0.4 <span class="unit">A</span></div>
              </div>
            </div>

            <div class="chart-container">
              <div class="chart-title">📊 运行速度与震动趋势</div>
              <div id="detailTrendChart" style="width: 100%; height: 280px;"></div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="🔔 告警日志" name="alarms">
            <div class="alarm-table-container">
              <el-table :data="alarmList" style="width: 100%" height="500">
                <el-table-column prop="time" label="发生时间" width="180" />
                <el-table-column prop="level" label="等级" width="100">
                  <template #default="{ row }">
                    <el-tag :type="getAlarmLevelType(row.level)" effect="dark" size="small">{{ row.level }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="code" label="代码" width="100">
                  <template #default="{ row }">
                    <span class="font-mono text-gray">{{ row.code }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="content" label="告警内容" />
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="{ row }">
                    <span :class="row.status === '未恢复' ? 'text-red' : 'text-green'">
                      <el-icon v-if="row.status === '未恢复'"><CircleClose /></el-icon>
                      <el-icon v-else><CircleCheck /></el-icon>
                      {{ row.status }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="120" fixed="right">
                  <template #default>
                    <el-button link type="primary" size="small">查看快照</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>

          <el-tab-pane label="🛠️ 维保历史" name="records">
            <div class="timeline-box">
              <el-timeline>
                <el-timeline-item timestamp="2023-11-28" placement="top" type="danger" color="#F56C6C">
                  <div class="record-card">
                    <h4>紧急维修 - 门锁故障 (E51)</h4>
                    <p>维修工：张伟 | 耗材：门锁触点 x1</p>
                  </div>
                </el-timeline-item>
                <el-timeline-item timestamp="2023-11-15" placement="top" type="primary" color="#409EFF">
                  <div class="record-card">
                    <h4>例行保养 - 半月保</h4>
                    <p>维修工：李强 | 状态：正常</p>
                  </div>
                </el-timeline-item>
                <el-timeline-item timestamp="2023-10-30" placement="top" color="#909399">
                  <div class="record-card">
                    <h4>例行保养 - 半月保</h4>
                    <p>维修工：张伟 | 状态：正常</p>
                  </div>
                </el-timeline-item>
              </el-timeline>
            </div>
          </el-tab-pane>

        </el-tabs>
      </div>

      <div class="side-panel">
        
        <div class="lifecycle-box mb-4">
          <div class="panel-header">⏳ 生命周期</div>
          <div class="lc-item">
            <div class="lc-label">累计运行时长</div>
            <div class="lc-val text-blue">14,208 <span class="unit">小时</span></div>
          </div>
          <div class="lc-item">
            <div class="lc-label">安全运行天数</div>
            <div class="lc-val text-green">128 <span class="unit">天</span></div>
          </div>
          <div class="lc-item alert-bg">
            <div class="lc-label">距离下次维保</div>
            <div class="lc-val text-yellow">5 <span class="unit">天</span></div>
            <el-progress :percentage="80" :show-text="false" status="warning" class="mt-2"/>
          </div>
        </div>

        <div class="panel-header">📄 基础档案</div>
        <div class="profile-list">
          <div class="profile-item">
            <span class="label">设备编号</span>
            <span class="val">{{ deviceId }}</span>
          </div>
          <div class="profile-item">
            <span class="label">安装位置</span>
            <span class="val">{{ deviceBaseInfo.location }}</span>
          </div>
          <div class="profile-item">
            <span class="label">品牌型号</span>
            <span class="val">{{ deviceBaseInfo.model }}</span>
          </div>
          <div class="profile-item">
            <span class="label">投产日期</span>
            <span class="val">2020-05-12</span>
          </div>
          <div class="profile-item">
            <span class="label">维保单位</span>
            <span class="val">迅达电梯</span>
          </div>
        </div>
        
        <div class="device-img">
          <el-icon :size="40" color="#409EFF"><Picture /></el-icon>
          <div style="margin-top: 10px; font-size: 12px; color: #64748b;">设备实景图</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import * as echarts from 'echarts'

const route = useRoute()
const router = useRouter()

const deviceId = ref(route.params.id)
const deviceType = ref(route.query.type || 'Elevator')
const activeTab = ref('status')

// 仪表盘颜色配置
const colors = [
  { color: '#f56c6c', percentage: 60 },
  { color: '#e6a23c', percentage: 80 },
  { color: '#5cb87a', percentage: 100 },
]

// 模拟基础信息
const deviceBaseInfo = ref({
  name: deviceType.value === 'Gateway' ? 'NeuronEX边缘网关' : '1号楼-客梯-A',
  model: deviceType.value === 'Gateway' ? 'Raspberry Pi 4' : 'Kone MiniSpace',
  status: '在线',
  location: 'A栋-1单元'
})

// 模拟告警日志数据 (New)
const alarmList = ref([
  { time: '2023-11-28 10:42:05', level: '严重', code: 'E51', content: '门锁回路异常断开，可能为门刀卡阻', status: '未恢复' },
  { time: '2023-11-25 14:20:00', level: '警告', code: 'W02', content: '平层误差超过阈值 (15mm)', status: '自动恢复' },
  { time: '2023-11-20 09:05:32', level: '提示', code: 'I01', content: '系统自动重启完成', status: '已恢复' },
  { time: '2023-11-10 08:30:00', level: '严重', code: 'E30', content: '变频器驱动故障', status: '已修复' },
  { time: '2023-11-01 12:00:00', level: '警告', code: 'W10', content: '轿厢照明电压异常', status: '已修复' },
])

const goBack = () => {
  router.push('/devices')
}

const getAlarmLevelType = (level) => {
  if (level === '严重') return 'danger'
  if (level === '警告') return 'warning'
  return 'info'
}

const initChart = () => {
  const chartDom = document.getElementById('detailTrendChart')
  if (chartDom) {
    if (echarts.getInstanceByDom(chartDom)) echarts.dispose(chartDom);
    
    const myChart = echarts.init(chartDom)
    myChart.setOption({
      backgroundColor: 'transparent',
      grid: { top: 20, right: 20, bottom: 20, left: 40, containLabel: true },
      tooltip: { trigger: 'axis' },
      xAxis: { 
        type: 'category', 
        data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00'],
        axisLabel: { color: '#94a3b8' }
      },
      yAxis: { 
        type: 'value',
        splitLine: { lineStyle: { color: '#334155', type: 'dashed' } },
        axisLabel: { color: '#94a3b8' }
      },
      series: [{
        name: '速度(m/s)',
        data: [1.0, 1.2, 1.75, 1.6, 1.75, 1.0],
        type: 'line',
        smooth: true,
        itemStyle: { color: '#38bdf8' },
        areaStyle: {
           color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(56, 189, 248, 0.5)' },
            { offset: 1, color: 'rgba(56, 189, 248, 0)' }
          ])
        }
      }]
    })
    
    window.addEventListener('resize', () => myChart.resize())
  }
}

// 监听路由参数变化
watch(() => route.params.id, (newId) => {
  deviceId.value = newId
  deviceType.value = route.query.type
  if(deviceType.value === 'Elevator') {
    nextTick(initChart)
  }
})

onMounted(() => {
  if (deviceType.value === 'Elevator') {
    nextTick(() => {
      initChart()
    })
  }
})
</script>

<style scoped>
/* 全局容器 */
.detail-wrapper { display: flex; flex-direction: column; gap: 20px; }

/* 页头 */
.custom-header {
  background: rgba(30, 41, 59, 0.7);
  border: 1px solid rgba(56, 189, 248, 0.1);
  padding: 15px 20px;
  border-radius: 8px;
  color: #fff;
}
:deep(.el-page-header__content) { color: #fff; }
.device-title { font-size: 18px; font-weight: bold; }
.ml-2 { margin-left: 10px; }
.mb-4 { margin-bottom: 20px; }
.mt-2 { margin-top: 10px; }

/* 空状态 */
.empty-state {
  background: rgba(30, 41, 59, 0.7);
  padding: 100px 0;
  border-radius: 8px;
  display: flex;
  justify-content: center;
}

/* 布局网格 */
.content-grid { display: grid; grid-template-columns: 3fr 1fr; gap: 20px; }

/* 面板通用 */
.main-panel, .side-panel {
  background: rgba(30, 41, 59, 0.7);
  border: 1px solid rgba(56, 189, 248, 0.1);
  border-radius: 8px;
  padding: 20px;
  color: #e2e8f0;
}

/* AI 预测模块 */
.prediction-section {
  background: rgba(16, 185, 129, 0.1);
  border: 1px solid rgba(16, 185, 129, 0.3);
  border-radius: 8px;
  padding: 15px;
}
.pred-title { font-weight: bold; color: #67C23A; margin-bottom: 10px; font-size: 15px; }
.pred-content { display: flex; align-items: center; gap: 30px; }
.health-score { position: relative; }
.score-val { font-size: 20px; font-weight: bold; color: #fff; }
.score-label { font-size: 12px; color: #94a3b8; }
.info-row { margin-bottom: 8px; font-size: 13px; }
.info-row .label { color: #94a3b8; }

/* 生命周期 */
.lifecycle-box { padding-bottom: 20px; border-bottom: 1px dashed #334155; margin-bottom: 20px; }
.lc-item { margin-bottom: 12px; }
.lc-item.alert-bg { background: rgba(230, 162, 60, 0.1); padding: 10px; border-radius: 4px; border: 1px solid rgba(230, 162, 60, 0.2); }
.lc-label { font-size: 12px; color: #94a3b8; margin-bottom: 4px; }
.lc-val { font-size: 20px; font-weight: bold; }

/* 监控卡片 */
.monitor-cards { display: grid; grid-template-columns: repeat(4, 1fr); gap: 15px; margin-bottom: 25px; }
.monitor-card { background: rgba(15, 23, 42, 0.6); border: 1px solid #334155; padding: 15px; border-radius: 6px; text-align: center; }
.monitor-card .label { color: #94a3b8; font-size: 13px; margin-bottom: 5px; }
.monitor-card .value { font-size: 24px; font-weight: bold; color: #f1f5f9; }
.monitor-card .unit { font-size: 12px; color: #64748b; font-weight: normal; }

/* 通用颜色 */
.text-blue { color: #38bdf8 !important; }
.text-green { color: #10b981 !important; }
.text-yellow { color: #f59e0b !important; }
.text-red { color: #F56C6C !important; }
.text-gray { color: #64748b !important; }
.font-bold { font-weight: bold; }
.font-mono { font-family: monospace; }

/* 图表区 */
.chart-container { background: rgba(15, 23, 42, 0.4); border: 1px solid #334155; border-radius: 6px; padding: 15px; }
.chart-title { font-size: 14px; font-weight: bold; margin-bottom: 10px; border-left: 3px solid #38bdf8; padding-left: 10px; }

/* Tabs 样式 */
:deep(.el-tabs__item) { color: #94a3b8; }
:deep(.el-tabs__item.is-active) { color: #38bdf8; }
:deep(.el-tabs__nav-wrap::after) { background-color: #334155; }

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

/* 档案样式 */
.panel-header { font-size: 16px; font-weight: bold; margin-bottom: 15px; border-left: 3px solid #38bdf8; padding-left: 8px; }
.profile-item { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px dashed #334155; font-size: 13px; }
.profile-item .label { color: #94a3b8; }
.profile-item .val { color: #e2e8f0; }
.device-img { margin-top: 20px; height: 120px; background: rgba(15, 23, 42, 0.6); border: 1px dashed #334155; border-radius: 6px; display: flex; flex-direction: column; align-items: center; justify-content: center; }

/* 维保记录 */
.record-card { background: rgba(15, 23, 42, 0.8); padding: 10px; border-radius: 4px; border: 1px solid #334155; }
.record-card h4 { margin: 0 0 5px 0; font-size: 14px; color: #e2e8f0; }
.record-card p { margin: 0; font-size: 12px; color: #94a3b8; }
</style>