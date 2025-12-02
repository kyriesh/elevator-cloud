<template>
  <div class="energy-board">
    
    <div class="kpi-row">
      <div class="kpi-card main-kpi">
        <div class="icon-box bg-blue"><el-icon><Lightning /></el-icon></div>
        <div class="info">
          <div class="label">本月总耗电</div>
          <div class="value">24,580 <span class="unit">kWh</span></div>
          <div class="trend">同比下降 5.2% <el-icon class="text-green"><Bottom /></el-icon></div>
        </div>
      </div>
      
      <div class="kpi-card green-kpi">
        <div class="icon-box bg-green"><el-icon><RefreshLeft /></el-icon></div>
        <div class="info">
          <div class="label">能量回馈 (已回收)</div>
          <div class="value text-green">4,200 <span class="unit">kWh</span></div>
          <div class="sub-text">节能率 17.1%</div>
        </div>
      </div>

      <div class="kpi-card">
        <div class="icon-box bg-teal"><el-icon><Sunny /></el-icon></div>
        <div class="info">
          <div class="label">减少碳排放</div>
          <div class="value">3.8 <span class="unit">吨</span></div>
          <div class="sub-text">相当于植树 210 棵</div>
        </div>
      </div>

      <div class="kpi-card">
        <div class="icon-box bg-yellow"><el-icon><Money /></el-icon></div>
        <div class="info">
          <div class="label">预估电费</div>
          <div class="value text-yellow">¥ 18,435</div>
          <div class="trend">预算执行率 85%</div>
        </div>
      </div>
    </div>

    <div class="charts-grid">
      
      <div class="chart-panel wide-panel">
        <div class="panel-header">
          <span class="title">📈 实时功率与能量回馈监控</span>
          <el-radio-group v-model="timeRange" size="small" class="dark-radio">
            <el-radio-button label="24h">24小时</el-radio-button>
            <el-radio-button label="7d">近7天</el-radio-button>
          </el-radio-group>
        </div>
        <div id="trendChart" style="width: 100%; height: 320px;"></div>
      </div>

      <div class="chart-panel">
        <div class="panel-header">
          <span class="title">🏆 耗电大户排行榜 (Top 5)</span>
        </div>
        <div class="ranking-list">
          <div v-for="(item, index) in rankList" :key="index" class="rank-row">
            <div class="rank-idx" :class="{'top3': index < 3}">{{ index + 1 }}</div>
            <div class="rank-info">
              <div class="r-name">{{ item.name }}</div>
              <el-progress :percentage="item.pct" :stroke-width="6" :color="getRankColor(index)" :show-text="false" />
            </div>
            <div class="rank-val">{{ item.val }} <span class="unit">kWh</span></div>
          </div>
        </div>
      </div>
    </div>

    <div class="charts-grid mt-4">
      
      <div class="chart-panel">
        <div class="panel-header">🔥 用电峰谷热力分布</div>
        <div id="heatmapChart" style="width: 100%; height: 250px;"></div>
      </div>

      <div class="chart-panel">
        <div class="panel-header">⚙️ 运行模式能耗构成</div>
        <div id="pieChart" style="width: 100%; height: 250px;"></div>
      </div>

      <div class="chart-panel ai-panel">
        <div class="panel-header text-green">🌱 AI 节能诊断报告</div>
        <div class="advice-list">
          <div class="advice-item">
            <div class="ad-icon"><el-icon><Timer /></el-icon></div>
            <div class="ad-content">
              <div class="ad-title">群控策略优化</div>
              <div class="ad-desc">检测到 B 栋夜间空载运行频繁，建议开启“单梯值班”模式。</div>
            </div>
            <el-button type="success" size="small" plain>应用</el-button>
          </div>
          <div class="advice-item">
            <div class="ad-icon"><el-icon><Setting /></el-icon></div>
            <div class="ad-content">
              <div class="ad-title">功率因数校正</div>
              <div class="ad-desc">3号货梯功率因数偏低 (0.75)，建议检查变频器配置。</div>
            </div>
            <el-button type="primary" size="small" plain>详情</el-button>
          </div>
        </div>
      </div>

    </div>

  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'

const timeRange = ref('24h')

const rankList = ref([
  { name: '3号楼-货梯-B', val: 450, pct: 90 },
  { name: '1号楼-客梯-A', val: 410, pct: 82 },
  { name: '5号楼-客梯-C', val: 380, pct: 75 },
  { name: '2号楼-消防梯', val: 220, pct: 45 },
  { name: 'A区-大厅扶梯', val: 180, pct: 35 },
])

const getRankColor = (index) => {
  if (index === 0) return '#f56c6c' // 红
  if (index === 1) return '#e6a23c' // 橙
  if (index === 2) return '#409eff' // 蓝
  return '#64748b'
}

const initCharts = () => {
  // 1. 双向趋势图 (核心)
  const trendChart = echarts.init(document.getElementById('trendChart'))
  trendChart.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'axis', axisPointer: { type: 'line' } },
    legend: { textStyle: { color: '#94a3b8' }, top: 0 },
    grid: { top: 30, right: 20, bottom: 20, left: 50, containLabel: true },
    xAxis: { 
      type: 'category', 
      data: ['00:00','03:00','06:00','09:00','12:00','15:00','18:00','21:00'],
      axisLabel: { color: '#94a3b8' }
    },
    yAxis: { 
      type: 'value', 
      name: '功率 (kW)',
      nameTextStyle: { color: '#64748b' },
      splitLine: { lineStyle: { color: '#334155', type: 'dashed' } }, 
      axisLabel: { color: '#94a3b8' } 
    },
    series: [
      { 
        name: '消耗功率', type: 'line', smooth: true, 
        data: [10, 5, 20, 80, 60, 75, 90, 40], 
        itemStyle: { color: '#38bdf8' },
        areaStyle: { color: new echarts.graphic.LinearGradient(0,0,0,1, [{offset:0, color:'rgba(56, 189, 248, 0.5)'}, {offset:1, color:'rgba(56, 189, 248, 0)'}]) }
      },
      { 
        name: '回馈功率', type: 'line', smooth: true, 
        data: [-2, -1, -5, -15, -10, -12, -18, -5], // 负值表示回馈
        itemStyle: { color: '#10b981' },
        areaStyle: { color: new echarts.graphic.LinearGradient(0,0,0,1, [{offset:0, color:'rgba(16, 185, 129, 0)'}, {offset:1, color:'rgba(16, 185, 129, 0.5)'}]) }
      }
    ]
  })

  // 2. 峰谷热力图
  const heatmapChart = echarts.init(document.getElementById('heatmapChart'))
  // 模拟 7天 x 24小时 数据
  const hours = ['12a', '2a', '4a', '6a', '8a', '10a', '12p', '2p', '4p', '6p', '8p', '10p'];
  const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
  const data = days.map((day, i) => hours.map((hour, j) => [i, j, Math.floor(Math.random() * 10)])).flat();
  
  heatmapChart.setOption({
    tooltip: { position: 'top' },
    grid: { height: '70%', top: '10%' },
    xAxis: { type: 'category', data: days, axisLabel: { color: '#94a3b8' } },
    yAxis: { type: 'category', data: hours, axisLabel: { color: '#94a3b8' } },
    visualMap: { min: 0, max: 10, calculable: true, orient: 'horizontal', left: 'center', bottom: '0%', inRange: { color: ['#1e293b', '#38bdf8', '#f56c6c'] }, textStyle: { color: '#fff' } },
    series: [{
      type: 'heatmap',
      data: data,
      label: { show: false },
      itemStyle: { borderColor: '#0f172a', borderWidth: 1 }
    }]
  })

  // 3. 能耗构成饼图
  const pieChart = echarts.init(document.getElementById('pieChart'))
  pieChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, textStyle: { color: '#94a3b8' } },
    series: [{
      name: '能耗分布',
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '45%'],
      itemStyle: { borderRadius: 5, borderColor: '#1e293b', borderWidth: 2 },
      data: [
        { value: 65, name: '曳引主机', itemStyle: { color: '#38bdf8' } },
        { value: 15, name: '门机系统', itemStyle: { color: '#e6a23c' } },
        { value: 10, name: '照明风扇', itemStyle: { color: '#10b981' } },
        { value: 10, name: '控制待机', itemStyle: { color: '#64748b' } }
      ],
      label: { color: '#fff' }
    }]
  })

  window.addEventListener('resize', () => {
    trendChart.resize(); heatmapChart.resize(); pieChart.resize();
  })
}

onMounted(() => {
  nextTick(initCharts)
})
</script>

<style scoped>
.energy-board { display: flex; flex-direction: column; gap: 20px; }

/* 1. KPI 顶栏 */
.kpi-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }
.kpi-card {
  background: rgba(30, 41, 59, 0.7); border: 1px solid rgba(56, 189, 248, 0.1);
  border-radius: 8px; padding: 20px; display: flex; align-items: center; position: relative; overflow: hidden;
}
/* 发光特效 */
.green-kpi { border: 1px solid rgba(16, 185, 129, 0.3); background: linear-gradient(135deg, rgba(16,185,129,0.05), rgba(30,41,59,0.8)); }

.icon-box { width: 50px; height: 50px; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 24px; margin-right: 15px; color: #fff; }
.bg-blue { background: #38bdf8; box-shadow: 0 0 15px rgba(56, 189, 248, 0.3); }
.bg-green { background: #10b981; box-shadow: 0 0 15px rgba(16, 185, 129, 0.3); }
.bg-teal { background: #14b8a6; }
.bg-yellow { background: #f59e0b; }

.info .label { font-size: 13px; color: #94a3b8; margin-bottom: 5px; }
.info .value { font-size: 26px; font-weight: bold; color: #f1f5f9; }
.info .unit { font-size: 14px; font-weight: normal; color: #64748b; }
.trend { font-size: 12px; color: #94a3b8; display: flex; align-items: center; gap: 4px; margin-top: 4px; }
.sub-text { font-size: 12px; color: #10b981; margin-top: 4px; font-weight: bold; }

/* 2. 图表布局 */
.charts-grid { display: grid; grid-template-columns: 2fr 1fr; gap: 20px; }
.charts-grid.mt-4 { margin-top: 20px; grid-template-columns: 1fr 1fr 1fr; }

.chart-panel {
  background: rgba(30, 41, 59, 0.7); border: 1px solid rgba(56, 189, 248, 0.1);
  border-radius: 8px; padding: 15px; display: flex; flex-direction: column;
}
.wide-panel { grid-column: span 1; }

.panel-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.panel-header .title { font-size: 16px; font-weight: bold; color: #e2e8f0; border-left: 3px solid #38bdf8; padding-left: 10px; }

/* 排行榜 */
.ranking-list { flex: 1; display: flex; flex-direction: column; justify-content: space-around; }
.rank-row { display: flex; align-items: center; gap: 10px; padding: 5px 0; }
.rank-idx { width: 24px; height: 24px; background: #334155; color: #94a3b8; border-radius: 4px; text-align: center; line-height: 24px; font-size: 12px; font-weight: bold; }
.rank-idx.top3 { background: #38bdf8; color: #0f172a; }
.rank-info { flex: 1; }
.r-name { font-size: 13px; color: #e2e8f0; margin-bottom: 4px; }
.rank-val { font-size: 14px; font-weight: bold; color: #fff; width: 80px; text-align: right; }
.rank-val .unit { font-size: 12px; color: #64748b; font-weight: normal; }

/* AI 建议面板 */
.ai-panel { border: 1px solid rgba(16, 185, 129, 0.2); background: rgba(16, 185, 129, 0.05); }
.advice-list { display: flex; flex-direction: column; gap: 10px; }
.advice-item { display: flex; align-items: center; background: rgba(30, 41, 59, 0.5); padding: 10px; border-radius: 6px; gap: 10px; }
.ad-icon { color: #10b981; font-size: 20px; }
.ad-content { flex: 1; }
.ad-title { font-size: 13px; font-weight: bold; color: #fff; }
.ad-desc { font-size: 12px; color: #94a3b8; line-height: 1.2; margin-top: 2px; }

/* 颜色工具 */
.text-green { color: #10b981; }
.text-yellow { color: #f59e0b; }
.text-red { color: #f56c6c; }

/* 单选框深色适配 */
:deep(.dark-radio .el-radio-button__inner) {
  background: transparent; color: #94a3b8; border-color: #334155;
}
:deep(.dark-radio .el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: #38bdf8; color: #fff; border-color: #38bdf8; box-shadow: none;
}
</style>