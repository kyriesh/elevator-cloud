<template>
  <div class="inventory-board">
    
    <div class="kpi-section">
      <div class="kpi-card">
        <div class="kpi-icon bg-blue-alpha"><el-icon><Money /></el-icon></div>
        <div class="kpi-info">
          <div class="label">库存总货值</div>
          <div class="value">¥ 458,200</div>
        </div>
      </div>
      <div class="kpi-card">
        <div class="kpi-icon bg-red-alpha"><el-icon><Warning /></el-icon></div>
        <div class="kpi-info">
          <div class="label">缺货预警品种</div>
          <div class="value text-red">3 <span class="sub">/ 142</span></div>
        </div>
      </div>
      <div class="kpi-card">
        <div class="kpi-icon bg-green-alpha"><el-icon><Refresh /></el-icon></div>
        <div class="kpi-info">
          <div class="label">年周转率</div>
          <div class="value">4.2 <span class="sub">次/年</span></div>
        </div>
      </div>
      <div class="kpi-card">
        <div class="kpi-icon bg-purple-alpha"><el-icon><Box /></el-icon></div>
        <div class="kpi-info">
          <div class="label">呆滞库存 (AI)</div>
          <div class="value">5 <span class="sub">种</span></div>
        </div>
      </div>
    </div>

    <div class="lifecycle-dashboard">
      <div class="section-header">🔄 备件全生命周期监控</div>
      <div class="charts-row">
        <div class="chart-wrapper">
          <div class="chart-title">📊 库存库龄结构分析 (Aging Analysis)</div>
          <div id="agingChart" style="width: 100%; height: 220px;"></div>
        </div>
        <div class="chart-wrapper">
          <div class="chart-title">🧬 备件状态流转分布</div>
          <div id="statusChart" style="width: 100%; height: 220px;"></div>
        </div>
      </div>
    </div>

    <div class="toolbar">
      <div class="left-tools">
        <el-input 
          v-model="search" 
          placeholder="🔍 搜索备件名称/型号/编码..." 
          style="width: 300px" 
          class="dark-input"
        />
        <el-select v-model="categoryFilter" placeholder="分类筛选" style="width: 140px" clearable>
          <el-option label="机械部件" value="mech" />
          <el-option label="电气部件" value="elec" />
          <el-option label="易损耗材" value="consumable" />
        </el-select>
        <el-checkbox v-model="onlyLowStock" label="仅显示缺货" border style="margin-left: 10px; color: #cbd5e1;" />
      </div>
      <div class="right-tools">
        <el-button type="primary" icon="Plus">入库登记</el-button>
        <el-button type="success" plain icon="Download">导出报表</el-button>
      </div>
    </div>

    <div class="parts-grid">
      <div 
        v-for="item in filteredParts" 
        :key="item.id" 
        class="part-card"
        :class="{'is-low-stock': item.stock <= item.threshold}"
      >
        <div class="ai-tag" v-if="item.prediction">
          <el-icon><MagicStick /></el-icon> {{ item.prediction }}
        </div>

        <div class="card-body">
          <div class="part-img-box">
            <el-icon :size="40" :color="getCategoryColor(item.category)"><Component /></el-icon>
          </div>
          <div class="part-details">
            <div class="p-name">{{ item.name }}</div>
            <div class="p-model">{{ item.model }}</div>
            <div class="p-price">¥ {{ item.price }}</div>
          </div>
        </div>

        <div class="stock-info">
          <div class="stock-label">
            <span>库存: <b :class="getStockColorClass(item)">{{ item.stock }}</b></span>
            <span class="threshold">安全线: {{ item.threshold }}</span>
          </div>
          <el-progress 
            :percentage="calcPercentage(item)" 
            :color="getStockColor(item)" 
            :stroke-width="8" 
            :show-text="false"
          />
        </div>

        <div class="card-footer">
          <div class="loc-tag">📍 {{ item.location }}</div>
          <el-button 
            size="small" 
            :type="item.stock <= item.threshold ? 'danger' : 'primary'" 
            plain 
            @click="openRestockDrawer(item)"
          >
            {{ item.stock <= item.threshold ? '🚨 补货/详情' : '详情/出库' }}
          </el-button>
        </div>
      </div>
    </div>

    <el-drawer v-model="drawerVisible" title="📦 备件全景档案" size="450px" custom-class="dark-drawer">
      <div v-if="currentItem" class="drawer-content">
        <div class="drawer-header-info">
          <h2>{{ currentItem.name }}</h2>
          <p>{{ currentItem.model }}</p>
          <el-tag effect="dark" size="small">{{ getCategoryName(currentItem.category) }}</el-tag>
        </div>

        <el-tabs v-model="drawerTab">
          
          <el-tab-pane label="📝 库存操作" name="action">
             <div class="chart-box">
              <div class="chart-title">📊 近6个月消耗趋势</div>
              <div id="consumptionChart" style="width: 100%; height: 200px;"></div>
            </div>

            <div class="restock-form">
              <el-form label-position="top">
                <el-form-item label="操作类型">
                  <el-radio-group v-model="actionType">
                    <el-radio-button label="in">采购入库</el-radio-button>
                    <el-radio-button label="out">领用出库</el-radio-button>
                  </el-radio-group>
                </el-form-item>
                <el-form-item label="数量">
                  <el-input-number v-model="actionNum" :min="1" />
                </el-form-item>
                <el-form-item label="关联工单/备注">
                  <el-input type="textarea" v-model="actionRemark" placeholder="请输入关联的维修工单号..." />
                </el-form-item>
              </el-form>
              <div class="drawer-btn-group">
                <el-button type="primary" class="w-full" @click="submitAction">确认提交</el-button>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="⏳ 生命周期追踪" name="lifecycle">
            <div class="lifecycle-timeline">
              <el-timeline>
                <el-timeline-item timestamp="2023-11-20" placement="top" color="#409EFF">
                  <div class="lc-card">
                    <div class="lc-title">最近一次采购入库</div>
                    <div class="lc-desc">供应商：上海三菱原厂 | 批次：PO-20231120</div>
                    <div class="lc-tag in">+20 件</div>
                  </div>
                </el-timeline-item>
                <el-timeline-item timestamp="2023-10-15" placement="top" color="#E6A23C">
                  <div class="lc-card">
                    <div class="lc-title">维修领用出库</div>
                    <div class="lc-desc">工单：WO-20231015-02 | 领用人：张伟</div>
                    <div class="lc-tag out">-1 件</div>
                  </div>
                </el-timeline-item>
                <el-timeline-item timestamp="2023-08-01" placement="top" color="#909399">
                  <div class="lc-card">
                    <div class="lc-title">盘点校准</div>
                    <div class="lc-desc">季度资产大盘点</div>
                    <div class="lc-tag check">库存确认</div>
                  </div>
                </el-timeline-item>
                 <el-timeline-item timestamp="2023-01-10" placement="top" color="#67C23A">
                  <div class="lc-card">
                    <div class="lc-title">备件建档初始化</div>
                    <div class="lc-desc">系统上线初始化录入</div>
                  </div>
                </el-timeline-item>
              </el-timeline>
            </div>
          </el-tab-pane>
        </el-tabs>

      </div>
    </el-drawer>

  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'

// --- 数据 ---
const search = ref('')
const categoryFilter = ref('')
const onlyLowStock = ref(false)
const drawerVisible = ref(false)
const drawerTab = ref('action')
const currentItem = ref(null)

const actionType = ref('in')
const actionNum = ref(1)
const actionRemark = ref('')

const parts = ref([
  { id: 'P001', name: '门机传动皮带', model: 'BELT-5M-1200', category: 'consumable', price: 45.0, stock: 15, threshold: 10, location: 'A-01-02', prediction: null },
  { id: 'P002', name: '电梯主控板', model: 'MC-2000-V2', category: 'elec', price: 2800.0, stock: 2, threshold: 5, location: 'B-03-01', prediction: '下月故障高发期' },
  { id: 'P003', name: '光幕传感器', model: 'WECO-917', category: 'elec', price: 450.0, stock: 20, threshold: 8, location: 'A-02-05', prediction: null },
  { id: 'P004', name: '曳引轮', model: 'TR-800', category: 'mech', price: 1200.0, stock: 0, threshold: 1, location: 'C-01-01', prediction: '急需补货' },
  { id: 'P005', name: '层门锁钩', model: 'LOCK-K1', category: 'mech', price: 85.0, stock: 50, threshold: 20, location: 'A-01-05', prediction: null },
  { id: 'P006', name: '接触器', model: 'SC-N1', category: 'elec', price: 120.0, stock: 4, threshold: 10, location: 'B-02-01', prediction: '建议备货' },
])

// --- 计算属性 ---
const filteredParts = computed(() => {
  return parts.value.filter(p => {
    const matchSearch = p.name.includes(search.value) || p.model.includes(search.value)
    const matchCat = categoryFilter.value ? p.category === categoryFilter.value : true
    const matchLow = onlyLowStock.value ? p.stock <= p.threshold : true
    return matchSearch && matchCat && matchLow
  })
})

// --- 方法 ---
const getCategoryColor = (cat) => {
  if (cat === 'elec') return '#409EFF'
  if (cat === 'mech') return '#E6A23C'
  return '#67C23A'
}

const getCategoryName = (cat) => {
  if (cat === 'elec') return '电气部件'
  if (cat === 'mech') return '机械部件'
  return '易损耗材'
}

const getStockColor = (item) => {
  if (item.stock === 0) return '#F56C6C' // 红
  if (item.stock <= item.threshold) return '#E6A23C' // 黄
  return '#67C23A' // 绿
}

const getStockColorClass = (item) => {
  if (item.stock <= item.threshold) return 'text-red'
  return 'text-white'
}

const calcPercentage = (item) => {
  if (item.stock === 0) return 0
  const max = item.threshold * 3
  const pct = (item.stock / max) * 100
  return Math.min(pct, 100)
}

// 打开抽屉并渲染图表
const openRestockDrawer = (item) => {
  currentItem.value = item
  drawerTab.value = 'action'
  actionNum.value = 1
  actionRemark.value = ''
  drawerVisible.value = true
  
  nextTick(() => {
    initChart()
  })
}

// 提交操作
const submitAction = () => {
  if (actionType.value === 'in') {
    currentItem.value.stock += actionNum.value
    ElMessage.success(`入库成功：库存 +${actionNum.value}`)
  } else {
    if (currentItem.value.stock < actionNum.value) {
      ElMessage.error('库存不足，无法出库')
      return
    }
    currentItem.value.stock -= actionNum.value
    ElMessage.success(`出库成功：库存 -${actionNum.value}`)
  }
  drawerVisible.value = false
}

// 初始化所有图表
const initLifecycleCharts = () => {
  // 1. 库龄分析图
  const agingDom = document.getElementById('agingChart')
  if (agingDom) {
    const chart = echarts.init(agingDom)
    chart.setOption({
      backgroundColor: 'transparent',
      tooltip: { trigger: 'axis' },
      grid: { top: 30, right: 20, bottom: 20, left: 40, containLabel: true },
      xAxis: { type: 'category', data: ['<30天', '30-90天', '90-180天', '>180天'], axisLabel: { color: '#94a3b8' } },
      yAxis: { type: 'value', splitLine: { lineStyle: { color: '#334155', type: 'dashed' } }, axisLabel: { color: '#94a3b8' } },
      series: [{
        data: [80, 45, 12, 5],
        type: 'bar',
        barWidth: '40%',
        itemStyle: { 
           color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#38bdf8' },
            { offset: 1, color: '#1d4ed8' }
          ])
        }
      }]
    })
    window.addEventListener('resize', () => chart.resize())
  }

  // 2. 状态流转图 (南丁格尔玫瑰图)
  const statusDom = document.getElementById('statusChart')
  if (statusDom) {
    const chart = echarts.init(statusDom)
    chart.setOption({
      backgroundColor: 'transparent',
      tooltip: { trigger: 'item' },
      series: [{
        name: '生命周期状态',
        type: 'pie',
        radius: [20, 80],
        center: ['50%', '50%'],
        roseType: 'area',
        itemStyle: { borderRadius: 5 },
        data: [
          { value: 120, name: '正常库存', itemStyle: { color: '#38bdf8' } },
          { value: 30, name: '已预定', itemStyle: { color: '#f59e0b' } },
          { value: 18, name: '待翻新', itemStyle: { color: '#a78bfa' } },
          { value: 12, name: '待报废', itemStyle: { color: '#f43f5e' } }
        ],
        label: { color: '#e2e8f0' }
      }]
    })
    window.addEventListener('resize', () => chart.resize())
  }
}

const initChart = () => {
  const chartDom = document.getElementById('consumptionChart')
  if (!chartDom) return
  if (echarts.getInstanceByDom(chartDom)) echarts.dispose(chartDom)
  
  const myChart = echarts.init(chartDom)
  // 模拟数据
  const data = Array.from({length: 6}, () => Math.floor(Math.random() * 20))
  
  myChart.setOption({
    grid: { top: 10, bottom: 20, left: 30, right: 10 },
    xAxis: { type: 'category', data: ['6月','7月','8月','9月','10月','11月'], axisLine: { lineStyle: { color: '#64748b' } } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#334155', type: 'dashed' } } },
    series: [{
      data: data,
      type: 'bar',
      barWidth: '40%',
      itemStyle: { color: '#38bdf8' }
    }]
  })
}

onMounted(() => {
  nextTick(() => {
    initLifecycleCharts()
  })
})
</script>

<style scoped>
.inventory-board { display: flex; flex-direction: column; gap: 20px; }

/* 1. KPI 区域 */
.kpi-section { display: grid; grid-template-columns: repeat(4, 1fr); gap: 15px; }
.kpi-card {
  background: rgba(30, 41, 59, 0.7); border: 1px solid rgba(56, 189, 248, 0.1);
  border-radius: 8px; padding: 15px; display: flex; align-items: center;
}
.kpi-icon { width: 48px; height: 48px; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-size: 24px; margin-right: 15px; color: #fff; }
.bg-blue-alpha { background: rgba(56, 189, 248, 0.2); color: #38bdf8; }
.bg-red-alpha { background: rgba(245, 108, 108, 0.2); color: #f56c6c; }
.bg-green-alpha { background: rgba(103, 194, 58, 0.2); color: #67c23a; }
.bg-purple-alpha { background: rgba(168, 85, 247, 0.2); color: #a855f7; }

.kpi-info .label { color: #94a3b8; font-size: 13px; }
.kpi-info .value { font-size: 20px; font-weight: bold; color: #f1f5f9; margin-top: 5px; }
.kpi-info .sub { font-size: 12px; color: #64748b; font-weight: normal; }

/* 2. 生命周期看板 (NEW) */
.lifecycle-dashboard {
  background: rgba(30, 41, 59, 0.7); border: 1px solid rgba(56, 189, 248, 0.1);
  border-radius: 8px; padding: 20px;
}
.section-header { font-size: 16px; font-weight: bold; color: #e2e8f0; margin-bottom: 15px; border-left: 3px solid #38bdf8; padding-left: 10px; }
.charts-row { display: grid; grid-template-columns: 1fr 1fr; gap: 30px; }
.chart-title { text-align: center; color: #94a3b8; font-size: 13px; margin-bottom: 10px; }
.chart-wrapper { background: rgba(15, 23, 42, 0.5); border-radius: 6px; padding: 15px; border: 1px solid #334155; }

/* 3. 工具栏 */
.toolbar {
  background: rgba(30, 41, 59, 0.7); border: 1px solid rgba(56, 189, 248, 0.1);
  border-radius: 8px; padding: 15px; display: flex; justify-content: space-between; align-items: center;
}
.left-tools { display: flex; align-items: center; gap: 10px; }

/* 4. 网格卡片 */
.parts-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 15px; }
.part-card {
  background: rgba(30, 41, 59, 0.7); border: 1px solid rgba(56, 189, 248, 0.1);
  border-radius: 8px; padding: 15px; position: relative; overflow: hidden; transition: transform 0.2s;
}
.part-card:hover { transform: translateY(-3px); border-color: #38bdf8; }
.part-card.is-low-stock { border: 1px solid #f56c6c; background: rgba(245, 108, 108, 0.05); }

/* AI 标签 */
.ai-tag {
  position: absolute; top: 0; right: 0;
  background: linear-gradient(135deg, #a855f7, #ec4899);
  color: white; font-size: 10px; padding: 3px 8px;
  border-bottom-left-radius: 8px; font-weight: bold; display: flex; align-items: center; gap: 3px;
}

.card-body { display: flex; margin-bottom: 15px; margin-top: 10px; }
.part-img-box {
  width: 60px; height: 60px; background: rgba(15, 23, 42, 0.6); border-radius: 6px;
  display: flex; align-items: center; justify-content: center; margin-right: 15px;
}
.p-name { color: #e2e8f0; font-weight: bold; font-size: 15px; margin-bottom: 4px; }
.p-model { color: #94a3b8; font-size: 12px; margin-bottom: 4px; }
.p-price { color: #38bdf8; font-weight: 500; font-family: 'Monaco', monospace; }

.stock-info { margin-bottom: 15px; }
.stock-label { display: flex; justify-content: space-between; font-size: 12px; margin-bottom: 5px; color: #94a3b8; }
.threshold { color: #64748b; }

.card-footer { display: flex; justify-content: space-between; align-items: center; border-top: 1px dashed #334155; padding-top: 10px; }
.loc-tag { font-size: 12px; color: #64748b; background: rgba(15, 23, 42, 0.6); padding: 2px 6px; border-radius: 4px; }

/* 抽屉内样式 */
.drawer-content { padding: 0 5px; }
.drawer-header-info { margin-bottom: 20px; }
.drawer-header-info h2 { margin: 0 0 5px 0; color: #303133; }
.drawer-header-info p { margin: 0 0 10px 0; color: #909399; font-size: 13px; }

.chart-box { background: #f8fafc; padding: 15px; border-radius: 6px; margin-bottom: 20px; border: 1px solid #e2e8f0; }
.chart-title { font-size: 13px; font-weight: bold; color: #64748b; margin-bottom: 10px; }

.restock-form { margin-bottom: 20px; }
.drawer-btn-group { margin-top: 20px; }
.w-full { width: 100%; }

/* 生命周期时间轴 (NEW) */
.lifecycle-timeline {
  padding: 10px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}
.lc-card {
  background: #fff; padding: 10px; border-radius: 4px; border: 1px solid #ebeef5; box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}
.lc-title { font-weight: bold; color: #303133; font-size: 13px; margin-bottom: 4px; }
.lc-desc { font-size: 12px; color: #909399; margin-bottom: 6px; }
.lc-tag { display: inline-block; font-size: 11px; padding: 2px 6px; border-radius: 3px; font-weight: bold; }
.lc-tag.in { background: #e1f3d8; color: #67c23a; }
.lc-tag.out { background: #fde2e2; color: #f56c6c; }
.lc-tag.check { background: #f4f4f5; color: #909399; }

/* 字体颜色工具类 */
.text-red { color: #f56c6c; }
.text-white { color: #e2e8f0; }
</style>