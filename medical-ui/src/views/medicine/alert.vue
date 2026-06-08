<template>
  <div class="alert-page">
    <el-card shadow="never">
      <template #header>
        <span style="color:#e6a23c">
          <el-icon><Warning /></el-icon> 库存预警药品
        </span>
      </template>
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="medicineName" label="药品名称" min-width="150" />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="stockQuantity" label="当前库存" width="100">
          <template #default="{ row }">
            <span style="color:red;font-weight:bold">{{ row.stockQuantity }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stockAlertThreshold" label="预警阈值" width="100" />
        <el-table-column label="缺口" width="100">
          <template #default="{ row }">
            {{ row.stockAlertThreshold - row.stockQuantity }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleRestock(row)">补货</el-button>
            <el-button size="small" @click="$router.push(`/medicine/form?id=${row.id}`)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @change="fetchData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { getStockAlert, updateStock } from '@/api/medicine'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getStockAlert({ pageNum: pageNum.value, pageSize: pageSize.value })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleRestock = async (row) => {
  const { value: quantity } = await ElMessageBox.prompt('请输入补货数量', '补货', {
    confirmButtonText: '确定',
    inputType: 'number',
    inputValue: row.stockAlertThreshold - row.stockQuantity
  })
  if (!quantity) return
  await updateStock(row.id, quantity)
  ElMessage.success('补货成功')
  fetchData()
}

fetchData()
</script>

<style scoped>
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
