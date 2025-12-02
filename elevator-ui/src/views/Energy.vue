<template>
  <div class="p-4">
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card shadow="never">
          <template #header>⚡ 近7天能耗趋势 (kWh)</template>
          <div id="energyChart" style="height: 350px;"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <template #header>🏆 本月耗电排行榜</template>
          <div v-for="(item, index) in rankList" :key="index" class="rank-item">
            <div class="rank-idx" :class="{'top3': index < 3}">{{ index + 1 }}</div>
            <div class="rank-name">{{ item.name }}</div>
            <div class="rank-val">{{ item.val }} kWh</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'

const rankList = ref([
  { name: '3号楼-货梯-B', val: 450 },
  { name: '1号楼-客梯-A', val: 410 },
  { name: '5号楼-客梯-C', val: 380 },
  { name: '2号楼-消防梯', val: 120 },
  { name: '1号楼-客梯-B', val: 110 }
])

onMounted(() => {
  const chart = echarts.init(document.getElementById('energyChart'))
  chart.setOption({
    color: ['#722ed1'],
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: ['周一','周二','周三','周四','周五','周六','周日'] },
    yAxis: { type: 'value' },
    series: [{
      type: 'bar',
      data: [400, 550, 700, 350, 800, 200, 250],
      barWidth: '40%'
    }]
  })
})
</script>

<style scoped>
.p-4 { padding: 20px; }
.rank-item { display: flex; align-items: center; padding: 12px 0; border-bottom: 1px solid #f0f0f0; }
.rank-idx { width: 24px; height: 24px; background: #eee; text-align: center; line-height: 24px; border-radius: 50%; margin-right: 15px; font-weight: bold; font-size: 12px; }
.top3 { background: #722ed1; color: white; }
.rank-name { flex: 1; font-size: 14px; }
.rank-val { font-weight: bold; }
</style>