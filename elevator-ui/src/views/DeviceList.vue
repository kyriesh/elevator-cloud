<template>
  <div class="p-4">
    <el-card shadow="never">
      <div class="flex justify-between mb-4">
        <el-input v-model="search" placeholder="搜索设备编号/位置..." style="width: 300px" prefix-icon="Search" />
        <el-button type="primary">新增设备</el-button>
      </div>

      <el-table :data="deviceList" stripe style="width: 100%">
        <el-table-column prop="id" label="设备ID" width="120" />
        <el-table-column prop="name" label="设备名称" width="180" />
        <el-table-column prop="location" label="安装位置" />
        <el-table-column prop="brand" label="品牌" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button link type="primary" @click="openDetail(scope.row)">查看详情</el-button>
            <el-button link type="danger">远程重启</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-drawer v-model="drawer" title="设备详情档案" size="40%">
      <div v-if="currentDevice">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="设备名称">{{ currentDevice.name }}</el-descriptions-item>
          <el-descriptions-item label="设备ID">{{ currentDevice.id }}</el-descriptions-item>
          <el-descriptions-item label="维保负责人">张伟 (13800138000)</el-descriptions-item>
          <el-descriptions-item label="上次年检">2023-05-12</el-descriptions-item>
        </el-descriptions>
        
        <div class="mt-6">
          <h4 class="mb-2">实时传感器数据</h4>
          <el-row :gutter="10">
            <el-col :span="8"><div class="sensor-box">温度: 26℃</div></el-col>
            <el-col :span="8"><div class="sensor-box">速度: 1.0m/s</div></el-col>
            <el-col :span="8"><div class="sensor-box">楼层: 8F</div></el-col>
          </el-row>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const search = ref('')
const deviceList = ref([])
const drawer = ref(false)
const currentDevice = ref(null)

const getStatusType = (status) => {
  if (status === '正常') return 'success'
  if (status === '故障') return 'danger'
  return 'info'
}

const openDetail = (row) => {
  currentDevice.value = row
  drawer.value = true
}

// 暂时使用 Mock 数据，等你后端写好 DeviceController 后替换 url
onMounted(async () => {
  // const res = await axios.get('http://localhost:8080/api/devices')
  // deviceList.value = res.data
  
  // 模拟数据
  deviceList.value = [
    { id: 'DEV001', name: '1号楼-客梯-A', location: 'A栋-1单元', brand: '通力', status: '正常' },
    { id: 'DEV002', name: '3号楼-货梯-B', location: 'A栋-3单元', brand: '三菱', status: '故障' },
    { id: 'DEV003', name: '5号楼-消防梯', location: 'B栋-地下', brand: '日立', status: '离线' }
  ]
})
</script>

<style scoped>
.p-4 { padding: 20px; }
.mb-4 { margin-bottom: 20px; }
.mt-6 { margin-top: 20px; }
.mb-2 { margin-bottom: 10px; }
.sensor-box { background: #f5f7fa; padding: 15px; text-align: center; border-radius: 4px; font-weight: bold; color: #409EFF; }
</style>