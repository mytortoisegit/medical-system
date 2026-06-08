<template>
  <div class="medicine-page">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="query" inline>
        <el-form-item label="药品名称">
          <el-input v-model="query.medicineName" placeholder="输入名称搜索" clearable
            @keyup.enter="handleSearch" style="width:200px" />
        </el-form-item>
        <el-form-item label="性味">
          <el-select v-model="query.property" placeholder="全部" clearable style="width:120px">
            <el-option v-for="p in properties" :key="p" :label="p" :value="p" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width:100px">
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作栏 -->
    <div class="toolbar">
      <el-button type="primary" @click="$router.push('/medicine/form')">
        <el-icon><Plus /></el-icon> 新增药品
      </el-button>
      <el-button type="success" @click="$router.push('/stock-alert')">
        <el-icon><Warning /></el-icon> 库存预警
      </el-button>
    </div>

    <!-- 表格 -->
    <el-card shadow="never">
      <el-table :data="tableData" v-loading="loading" stripe border style="width:100%">
        <el-table-column prop="medicineName" label="药品名称" min-width="150" />
        <el-table-column prop="medicineCode" label="编码" width="100" />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="property" label="性味" width="70" />
        <el-table-column prop="origin" label="产地" width="120" />
        <el-table-column prop="stockQuantity" label="库存" width="80" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="$router.push(`/medicine/form?id=${row.id}`)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @change="fetchData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getMedicinePage, deleteMedicine } from '@/api/medicine'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const properties = ['寒', '热', '温', '凉', '平']

const query = reactive({
  pageNum: 1, pageSize: 10,
  medicineName: '', property: '', status: null
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMedicinePage(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.pageNum = 1
  fetchData()
}
const handleReset = () => {
  Object.assign(query, { pageNum: 1, pageSize: 10, medicineName: '', property: '', status: null })
  fetchData()
}
const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定删除「${row.medicineName}」吗？`, '警告', { type: 'warning' })
  await deleteMedicine(row.id)
  ElMessage.success('删除成功')
  fetchData()
}

onMounted(() => fetchData())
</script>

<style scoped>
.search-card { margin-bottom: 16px; }
.toolbar { margin-bottom: 16px; display: flex; gap: 12px; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
