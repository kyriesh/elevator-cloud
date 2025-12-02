<template>
  <div>
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>设备总数</template>
          <h2 style="color: #409EFF">{{ stats.total }}</h2>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>当前故障</template>
          <h2 style="color: #F56C6C">{{ stats.faults }}</h2>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 20px;">
      <div id="chart" style="height: 300px;"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import * as echarts from 'echarts'

const stats = ref({})

onMounted(async () => {
  // 1. 调用后端接口
  const res = await axios.get('http://localhost:8080/api/dashboard/stats')
  stats.value = res.data

  // 2. 渲染图表
  const myChart = echarts.init(document.getElementById('chart'))
  myChart.setOption({
    title: { text: '近7天健康度趋势' },
    xAxis: { type: 'category', data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'] },
    yAxis: { type: 'value' },
    series: [{ data: res.data.trend, type: 'line', smooth: true }]
  })
})
</script>