<template>
  <div class="analysis-board">
    
    <div class="filter-bar">
      <div class="bar-title">📅 历史数据回溯</div>
      <div class="bar-controls">
        <span class="label">分析周期：</span>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :shortcuts="shortcuts"
          class="dark-picker"
          @change="refreshData"
        />
        <el-button type="primary" icon="Search" class="ml-2" @click="refreshData">开始分析</el-button>
        <el-button type="success" plain icon="Download">导出报告</el-button>
      </div>
    </div>

    <div class="kpi-grid">
      <div class="kpi-card">
        <div class="kpi-title">平均故障间隔时间 (MTBF)</div>
        <div class="kpi-val text-green">1,248 <span class="unit">小时</span></div>
        <div class="kpi-trend">同比提升 12% <el-icon><Top /></el-icon></div>
      </div>
      <div class="kpi-card">
        <div class="kpi-title">平均修复时间 (MTTR)</div>
        <div class="kpi-val text-blue">45 <span class="unit">分钟</span></div>
        <div class="kpi-trend text-green">缩短 15% <el-icon><Bottom /></el-icon></div>
      </div>
      <div class="kpi-card">
        <div class="kpi-title">本月维护总成本</div>
        <div class="kpi-val text-yellow">¥ 45,200</div>
        <div class="kpi-trend text-red">超出预算 5% <el-icon><Top /></el-icon></div>
      </div>
      <div class="kpi-card">
        <div class="kpi-title">预测性维护节省</div>
        <div class="kpi-val text-purple">¥ 12,800</div>
        <div class="kpi-trend">AI 避免了 3 次停梯</div>
      </div>
    </div>

    <div class="charts-container">
      <el-row :gutter="20" class="mb-4">
        <el-col :span="16">
          <div class="chart-panel">
            <div class="panel-header">📈 历史故障趋势分析</div>
            <div id="faultTrendChart" style="width: 100%; height: 280px;"></div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="chart-panel">
            <div class="panel-header">🍰 故障类型根因分布</div>
            <div id="faultTypeChart" style="width: 100%; height: 280px;"></div>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <div class="chart-panel">
            <div class="panel-header">⚙️ 设备运行状态效能分析</div>
            <div id="statusChart" style="width: 100%; height: 280px;"></div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="chart-panel">
            <div class="panel-header">💰 维护成本效益分析</div>
            <div id="costChart" style="width: 100%; height: 280px;"></div>
          </div>
        </el-col>
      </el-row>
    </div>

    <div class="ai-insight-section">
      <div class="section-header">
        <span>🧠 智能维护建议 (AI Insights)</span>
        <el-tag effect="dark" type="success" round>算法模型 v2.4 运行中</el-tag>
      </div>
      
      <div class="insight-cards">
        <div class="insight-card high-priority">
          <div class="card-icon"><el-icon><Warning /></el-icon></div>
          <div class="card-content">
            <div class="card-title">预防性更换建议：3号楼货梯门机皮带</div>
            <div class="card-desc">基于振动频谱分析，预测该皮带将在未来 15 天内断裂风险 > 85%。</div>
            <div class="card-action">
              <span class="save-tag">预计节省抢修成本 ¥2,000</span>
              <el-button type="primary" size="small">生成维保工单</el-button>
            </div>
          </div>
        </div>

        <div class="insight-card medium-priority">
          <div class="card-icon"><el-icon><Timer /></el-icon></div>
          <div class="card-content">
            <div class="card-title">维保计划优化：A区客梯群组</div>
            <div class="card-desc">检测到 A 区客梯在周五下午 14:00-16:00 使用率最低，建议将例行维保调整至该时段。</div>
            <div class="card-action">
              <span class="save-tag">提升可用性 12%</span>
              <el-button type="warning" plain size="small">调整计划</el-button>
            </div>
          </div>
        </div>

        <div class="insight-card low-priority">
          <div class="card-icon"><el-icon><Lightning /></el-icon></div>
          <div class="card-content">
            <div class="card-title">能效优化：开启休眠模式</div>
            <div class="card-desc">夜间 02:00-05:00 期间，建议对 B 栋 4 台货梯开启深度休眠模式。</div>
            <div class="card-action">
              <span class="save-tag">预计月省电 450 kWh</span>
              <el-button type="success" plain size="small">一键配置</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { ElLoading } from 'element-plus'

// 日期快捷选项
const shortcuts = [
  { text: '最近一周', value: () => { const end = new Date(); const start = new Date(); start.setTime(start.getTime() - 3600 * 1000 * 24 * 7); return [start, end] } },
  { text: '最近一月', value: () => { const end = new Date(); const start = new Date(); start.setTime(start.getTime() - 3600 * 1000 * 24 * 30); return [start, end] } },
  { text: '最近三月', value: () => { const end = new Date(); const start = new Date(); start.setTime(start.getTime() - 3600 * 1000 * 24 * 90); return [start, end] } },
]

const dateRange = ref('')

// 模拟刷新数据
const refreshData = () => {
  const loading = ElLoading.service({ lock: true, text: 'AI 正在分析海量数据...', background: 'rgba(0, 0, 0, 0.7)' })
  setTimeout(() => {
    loading.close()
    // 这里可以重新 fetch 数据并 setOption
    initCharts() 
  }, 1000)
}

// 初始化图表
const initCharts = () => {
  // 1. 故障趋势图 (折线 + 柱状混合)
  const trendChart = echarts.init(document.getElementById('faultTrendChart'))
  trendChart.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'axis' },
    legend: { textStyle: { color: '#94a3b8' }, top: 0 },
    grid: { top: 30, right: 10, bottom: 20, left: 40, containLabel: true },
    xAxis: { type: 'category', data: ['11-01', '11-05', '11-10', '11-15', '11-20', '11-25', '11-30'], axisLabel: { color: '#94a3b8' } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#334155', type: 'dashed' } }, axisLabel: { color: '#94a3b8' } },
    series: [
      { name: '故障次数', type: 'line', smooth: true, data: [2, 4, 1, 5, 2, 8, 3], itemStyle: { color: '#F56C6C' }, areaStyle: { color: new echarts.graphic.LinearGradient(0,0,0,1, [{offset:0, color:'rgba(245,108,108,0.5)'},{offset:1, color:'rgba(245,108,108,0)'}]) } },
      { name: '自动恢复', type: 'bar', barWidth: 15, data: [1, 2, 1, 3, 1, 5, 2], itemStyle: { color: '#409EFF' } }
    ]
  })

  // 2. 故障类型分布 (南丁格尔图)
  const typeChart = echarts.init(document.getElementById('faultTypeChart'))
  typeChart.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'item' },
    legend: { top: 'bottom', textStyle: { color: '#94a3b8' } },
    series: [{
      name: '故障根因',
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '45%'],
      itemStyle: { borderRadius: 5, borderColor: '#1e293b', borderWidth: 2 },
      data: [
        { value: 40, name: '门系统', itemStyle: { color: '#E6A23C' } },
        { value: 30, name: '控制系统', itemStyle: { color: '#409EFF' } },
        { value: 20, name: '曳引系统', itemStyle: { color: '#F56C6C' } },
        { value: 10, name: '其他', itemStyle: { color: '#909399' } }
      ],
      label: { color: '#fff' }
    }]
  })

  // 3. 运行效能分析 (堆叠柱状图)
  const statusChart = echarts.init(document.getElementById('statusChart'))
  statusChart.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { textStyle: { color: '#94a3b8' } },
    grid: { top: 30, bottom: 20, left: 40, containLabel: true },
    xAxis: { type: 'value', axisLabel: { color: '#94a3b8' }, splitLine: { show: false } },
    yAxis: { type: 'category', data: ['1号楼', '2号楼', '3号楼', '5号楼'], axisLabel: { color: '#94a3b8' } },
    series: [
      { name: '正常运行', type: 'bar', stack: 'total', data: [300, 280, 250, 290], itemStyle: { color: '#67C23A' } },
      { name: '待机休眠', type: 'bar', stack: 'total', data: [120, 140, 100, 110], itemStyle: { color: '#409EFF' } },
      { name: '故障停机', type: 'bar', stack: 'total', data: [10, 5, 20, 2], itemStyle: { color: '#F56C6C' } }
    ]
  })

  // 4. 成本效益分析 (双轴图：柱状=成本，折线=效益)
  const costChart = echarts.init(document.getElementById('costChart'))
  costChart.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'axis' },
    legend: { textStyle: { color: '#94a3b8' } },
    grid: { top: 30, bottom: 20, left: 40, right: 40, containLabel: true },
    xAxis: { type: 'category', data: ['6月','7月','8月','9月','10月','11月'], axisLabel: { color: '#94a3b8' } },
    yAxis: [
      { type: 'value', name: '维护成本(元)', nameTextStyle: {color: '#94a3b8'}, axisLabel: { color: '#94a3b8' }, splitLine: { lineStyle: { color: '#334155', type: 'dashed' } } },
      { type: 'value', name: '避免损失(元)', nameTextStyle: {color: '#94a3b8'}, axisLabel: { color: '#94a3b8' }, splitLine: { show: false } }
    ],
    series: [
      { name: '实际维护成本', type: 'bar', barWidth: '30%', data: [5000, 5200, 4800, 6000, 5500, 4500], itemStyle: { color: '#E6A23C' } },
      { name: '预测挽回损失', type: 'line', yAxisIndex: 1, smooth: true, data: [2000, 2500, 3000, 8000, 4000, 6000], itemStyle: { color: '#67C23A' }, lineStyle: { width: 3 } }
    ]
  })

  // 自适应
  window.addEventListener('resize', () => {
    trendChart.resize(); typeChart.resize(); statusChart.resize(); costChart.resize();
  })
}

onMounted(() => {
  nextTick(initCharts)
})
</script>

<style scoped>
.analysis-board { display: flex; flex-direction: column; gap: 20px; }

/* 1. 顶部筛选栏 */
.filter-bar {
  background: rgba(30, 41, 59, 0.7);
  border: 1px solid rgba(56, 189, 248, 0.1);
  padding: 15px 20px;
  border-radius: 8px;
  display: flex; justify-content: space-between; align-items: center;
}
.bar-title { font-size: 18px; font-weight: bold; color: #fff; display: flex; align-items: center; gap: 10px; }
.bar-controls { display: flex; align-items: center; }
.label { color: #94a3b8; margin-right: 10px; font-size: 14px; }
.ml-2 { margin-left: 10px; }
/* 深度定制 Element 日期选择器样式 */
:deep(.dark-picker .el-input__wrapper) {
  background-color: rgba(15, 23, 42, 0.6) !important;
  box-shadow: 0 0 0 1px #334155 inset !important;
}
:deep(.dark-picker .el-range-input) { color: #fff !important; }
:deep(.dark-picker .el-range-separator) { color: #94a3b8 !important; }

/* 2. KPI 区域 */
.kpi-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }
.kpi-card {
  background: rgba(30, 41, 59, 0.7); border: 1px solid rgba(56, 189, 248, 0.1);
  border-radius: 8px; padding: 20px; text-align: center;
}
.kpi-title { color: #94a3b8; font-size: 14px; margin-bottom: 10px; }
.kpi-val { font-size: 32px; font-weight: bold; color: #fff; margin-bottom: 5px; }
.kpi-val .unit { font-size: 14px; font-weight: normal; color: #64748b; }
.kpi-trend { font-size: 12px; color: #64748b; display: flex; align-items: center; justify-content: center; gap: 4px; }

/* 3. 图表容器 */
.charts-container { display: flex; flex-direction: column; gap: 20px; }
.mb-4 { margin-bottom: 20px; }
.chart-panel {
  background: rgba(30, 41, 59, 0.7); border: 1px solid rgba(56, 189, 248, 0.1);
  border-radius: 8px; padding: 15px;
}
.panel-header { font-size: 16px; font-weight: bold; color: #e2e8f0; margin-bottom: 15px; border-left: 3px solid #38bdf8; padding-left: 10px; }

/* 4. AI 建议区 */
.ai-insight-section {
  background: rgba(30, 41, 59, 0.7); border: 1px solid rgba(56, 189, 248, 0.1);
  border-radius: 8px; padding: 20px;
}
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; font-size: 18px; font-weight: bold; color: #fff; }

.insight-cards { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.insight-card {
  background: rgba(15, 23, 42, 0.6); border-radius: 8px; padding: 20px;
  display: flex; gap: 15px; border: 1px solid transparent; transition: transform 0.2s;
}
.insight-card:hover { transform: translateY(-3px); }

/* 优先级样式 */
.high-priority { border-color: #F56C6C; background: linear-gradient(135deg, rgba(245,108,108,0.1) 0%, rgba(15,23,42,0.6) 100%); }
.high-priority .card-icon { color: #F56C6C; background: rgba(245,108,108,0.2); }

.medium-priority { border-color: #E6A23C; background: linear-gradient(135deg, rgba(230,162,60,0.1) 0%, rgba(15,23,42,0.6) 100%); }
.medium-priority .card-icon { color: #E6A23C; background: rgba(230,162,60,0.2); }

.low-priority { border-color: #67C23A; background: linear-gradient(135deg, rgba(103,194,58,0.1) 0%, rgba(15,23,42,0.6) 100%); }
.low-priority .card-icon { color: #67C23A; background: rgba(103,194,58,0.2); }

.card-icon { width: 50px; height: 50px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 24px; flex-shrink: 0; }
.card-content { flex: 1; }
.card-title { font-weight: bold; color: #fff; margin-bottom: 5px; font-size: 15px; }
.card-desc { color: #94a3b8; font-size: 13px; line-height: 1.5; margin-bottom: 12px; }
.card-action { display: flex; justify-content: space-between; align-items: center; }
.save-tag { font-size: 12px; color: #38bdf8; background: rgba(56, 189, 248, 0.1); padding: 2px 6px; border-radius: 4px; }

.text-green { color: #67C23A; }
.text-blue { color: #38bdf8; }
.text-yellow { color: #E6A23C; }
.text-red { color: #F56C6C; }
.text-purple { color: #a855f7; }
</style>