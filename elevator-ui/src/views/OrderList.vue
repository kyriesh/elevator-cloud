<template>
  <div class="order-container">
    <el-card class="box-card" shadow="never">
      <div class="header-actions">
        <div class="title">📋 智能工单中心</div>
        <el-button type="primary" @click="fetchOrders">
          <el-icon><Refresh /></el-icon> 刷新列表
        </el-button>
      </div>
    </el-card>

    <el-card class="box-card" style="margin-top: 20px;">
      <el-table :data="orders" style="width: 100%" stripe v-loading="loading">
        <el-table-column prop="id" label="工单号" width="120" />
        
        <el-table-column prop="deviceName" label="故障设备" width="180">
          <template #default="scope">
            <el-tag effect="dark" type="info">{{ scope.row.deviceName }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="issue" label="故障描述">
          <template #default="scope">
            <span style="color: #F56C6C; font-weight: bold;">{{ scope.row.issue }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.priority === '紧急' ? 'danger' : 'warning'">
              {{ scope.row.priority }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="worker" label="当前维修工">
           <template #default="scope">
             <div v-if="scope.row.worker">
               <el-avatar :size="24" style="vertical-align: middle; margin-right: 5px;">{{ scope.row.worker.substring(0,1) }}</el-avatar>
               <span>{{ scope.row.worker }}</span>
             </div>
             <span v-else style="color: #999;">- 待指派 -</span>
           </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="120">
           <template #default="scope">
             <el-tag effect="light" :type="getStatusColor(scope.row.status)">
               {{ scope.row.status }}
             </el-tag>
           </template>
        </el-table-column>
        
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button 
              size="small" 
              type="primary" 
              v-if="scope.row.status === '待派单'"
              @click="openDispatchDialog(scope.row)">
              🤖 智能指派
            </el-button>
            <el-button size="small" type="success" plain v-else disabled>
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="🤖 系统智能推荐人选" width="500px">
      <div class="recommend-tip">
        <el-icon><Cpu /></el-icon> AI 算法根据 [距离]、[技能]、[空闲度] 为您推荐：
      </div>
      
      <el-radio-group v-model="selectedWorker" class="worker-list">
        <el-radio label="张伟" border class="worker-item">
          <div class="worker-info">
            <span class="name">张伟 (98%匹配)</span>
            <span class="sub">距离0.8km | 空闲 | 擅长通力电梯</span>
          </div>
        </el-radio>
        <el-radio label="李强" border class="worker-item">
          <div class="worker-info">
            <span class="name">李强 (70%匹配)</span>
            <span class="sub">距离3.2km | 忙碌 | 擅长三菱电梯</span>
          </div>
        </el-radio>
      </el-radio-group>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmDispatch" :loading="submitting">
            确认派单
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const orders = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const selectedWorker = ref('张伟') // 默认选中推荐第一人
const currentOrderId = ref('')

// 获取状态颜色
const getStatusColor = (status) => {
  if (status === '待派单') return 'danger'
  if (status === '处理中') return 'warning'
  return 'success'
}

// 1. 获取工单列表
const fetchOrders = async () => {
  loading.value = true
  try {
    const res = await axios.get('http://localhost:8080/api/orders')
    orders.value = res.data
  } catch (error) {
    ElMessage.error('无法连接到后端服务')
  } finally {
    loading.value = false
  }
}

// 2. 打开弹窗
const openDispatchDialog = (row) => {
  currentOrderId.value = row.id
  dialogVisible.value = true
}

// 3. 确认派单
const confirmDispatch = async () => {
  submitting.value = true
  try {
    // 调用后端 POST 接口
    const res = await axios.post('http://localhost:8080/api/dispatch', {
      orderId: currentOrderId.value,
      worker: selectedWorker.value
    })

    if (res.data.msg === 'success') {
      ElMessage.success(`已成功指派给：${selectedWorker.value}`)
      dialogVisible.value = false
      fetchOrders() // 刷新列表，状态应该变成“处理中”
    } else {
      ElMessage.error('派单失败')
    }
  } catch (error) {
    ElMessage.error('系统异常')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.order-container { padding: 20px; }
.header-actions { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 18px; font-weight: bold; color: #333; }
.recommend-tip { background: #f0f9eb; color: #67c23a; padding: 10px; margin-bottom: 15px; border-radius: 4px; font-size: 13px; display: flex; align-items: center; gap: 5px; }
.worker-list { display: flex; flex-direction: column; gap: 10px; width: 100%; }
.worker-item { margin-right: 0 !important; width: 100%; height: auto; padding: 10px; }
.worker-info { display: flex; flex-direction: column; line-height: 1.5; }
.name { font-weight: bold; color: #303133; }
.sub { font-size: 12px; color: #909399; }
</style>