<template>
  <div class="p-4">
    <el-row :gutter="20">
      <el-col :span="6" v-for="item in parts" :key="item.id">
        <el-card shadow="hover" class="mb-4 part-card">
          <div v-if="item.stock <= item.threshold" class="smart-tag">⚠️ 库存预警</div>
          
          <div class="card-header">
            <el-icon :size="40" color="#409EFF"><Box /></el-icon>
            <div class="part-info">
              <div class="name">{{ item.name }}</div>
              <div class="model">{{ item.id }}</div>
            </div>
          </div>
          
          <div class="stock-area">
            <div class="flex justify-between text-sm mb-1">
              <span>库存: {{ item.stock }}</span>
              <span class="text-gray">阈值: {{ item.threshold }}</span>
            </div>
            <el-progress 
              :percentage="Math.min((item.stock / (item.threshold * 2)) * 100, 100)" 
              :status="item.stock <= item.threshold ? 'exception' : 'success'" 
              :show-text="false" 
            />
          </div>

          <div class="action-area">
            <span class="price">¥ {{ item.price || '-' }}</span>
            <el-button type="primary" size="small" plain @click="restock(item)">补货</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const parts = ref([])

const fetchParts = async () => {
  // 调用你后端的 API
  try {
    const res = await axios.get('http://localhost:8080/api/parts')
    parts.value = res.data
  } catch(e) {
    // 失败时使用兜底数据
    parts.value = [
      { id: 'P001', name: '门机皮带', stock: 15, threshold: 10, price: 45 },
      { id: 'P002', name: '主控板', stock: 3, threshold: 5, price: 2500 }, // 会显示预警
      { id: 'P003', name: '光幕传感器', stock: 20, threshold: 8, price: 400 },
      { id: 'P004', name: '曳引轮', stock: 1, threshold: 2, price: 1200 }
    ]
  }
}

const restock = (item) => {
  ElMessage.success(`已提交 [${item.name}] 的采购申请`)
}

onMounted(fetchParts)
</script>

<style scoped>
.p-4 { padding: 20px; }
.mb-4 { margin-bottom: 20px; }
.part-card { position: relative; overflow: hidden; }
.smart-tag { position: absolute; top: 0; right: 0; background: #F56C6C; color: white; font-size: 12px; padding: 2px 8px; border-bottom-left-radius: 8px; }
.card-header { display: flex; align-items: center; margin-bottom: 15px; }
.part-info { margin-left: 15px; }
.name { font-weight: bold; font-size: 16px; }
.model { color: #999; font-size: 12px; }
.stock-area { margin-bottom: 15px; }
.text-gray { color: #999; }
.action-area { display: flex; justify-content: space-between; align-items: center; border-top: 1px dashed #eee; padding-top: 10px; }
.price { color: #F56C6C; font-weight: bold; }
</style>