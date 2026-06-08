<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6" v-for="(card, i) in statCards" :key="card.title">
        <el-card shadow="hover" :body-style="{ padding: '24px' }">
          <div class="stat-card">
            <div class="stat-icon" :style="{ background: card.bg }">
              <el-icon :size="28" :color="card.color"><component :is="card.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value count-animate" :style="{ animationDelay: i * 0.1 + 's' }">{{ card.value }}</div>
              <div class="stat-title">{{ card.title }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表 + 预警 -->
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="10">
        <el-card shadow="hover">
          <template #header><span>📊 药品分类分布</span></template>
          <div ref="pieChart" style="height:300px"></div>
        </el-card>
      </el-col>
      <el-col :span="14">
        <el-card shadow="hover">
          <template #header><span>⚠️ 库存预警药品</span></template>
          <el-table :data="alertList" stripe>
            <el-table-column prop="medicineName" label="药品名称" min-width="140" />
            <el-table-column prop="categoryName" label="分类" width="100" />
            <el-table-column prop="stockQuantity" label="库存" width="80">
              <template #default="{ row }">
                <span style="color:var(--danger);font-weight:bold">{{ row.stockQuantity }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="stockAlertThreshold" label="预警阈值" width="90" />
            <el-table-column label="状态" width="80">
              <template #default>
                <el-tag type="danger" size="small" effect="dark">缺货</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作 -->
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header><span>🚀 快捷操作</span></template>
          <div class="quick-actions">
            <div class="action-item" @click="$router.push('/medicine')">
              <el-icon :size="24"><Search /></el-icon>
              <span>查询药品</span>
            </div>
            <div class="action-item" @click="$router.push('/medicine/form')">
              <el-icon :size="24"><Plus /></el-icon>
              <span>新增药品</span>
            </div>
            <div class="action-item" @click="$router.push('/stock-alert')">
              <el-icon :size="24"><Warning /></el-icon>
              <span>库存预警</span>
            </div>
            <div class="action-item" @click="$router.push('/user')">
              <el-icon :size="24"><User /></el-icon>
              <span>用户管理</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { getMedicinePage, getStockAlert, countByCategory } from '@/api/medicine'
import * as echarts from 'echarts'
import { FirstAidKit, TrendCharts, Warning, ShoppingCart } from '@element-plus/icons-vue'

const pieChart = ref(null)
const alertList = ref([])
const categoryData = ref([])

const statCards = ref([
  { title: '药品总数', value: 0, icon: 'FirstAidKit', color: '#8B5E3C', bg: 'rgba(139,94,60,0.1)' },
  { title: '本月新增', value: 0, icon: 'TrendCharts', color: '#6B8E23', bg: 'rgba(107,142,35,0.1)' },
  { title: '库存预警', value: 0, icon: 'Warning', color: '#D4A017', bg: 'rgba(212,160,23,0.1)' },
  { title: '今日处方', value: 0, icon: 'ShoppingCart', color: '#C04040', bg: 'rgba(192,64,64,0.1)' }
])

const loadData = async () => {
  try {
    const [pageRes, alertRes, categoryRes] = await Promise.all([
      getMedicinePage({ pageNum: 1, pageSize: 1 }),
      getStockAlert({ pageNum: 1, pageSize: 10 }),
      countByCategory()
    ])
    statCards.value[0].value = pageRes.data.total || 0
    statCards.value[2].value = alertRes.data.total || 0
    alertList.value = alertRes.data.records || []
    categoryData.value = (categoryRes.data || []).map(item => ({
      value: item.medicineCount || 0,
      name: item.categoryName || '未分类'
    }))
    initChart()
  } catch {}
}

const initChart = () => {
  if (!pieChart.value) return
  const chart = echarts.init(pieChart.value)
  const data = categoryData.value.length > 0
    ? categoryData.value
    : [{ value: 1, name: '暂无数据' }]
  chart.setOption({
    tooltip: { trigger: 'item' },
    color: ['#8B5E3C', '#C9A96E', '#B8860B', '#5C3D1E', '#D4C5B0', '#A0724A', '#6B8E23', '#D4A017'],
    series: [{
      type: 'pie',
      radius: ['50%', '78%'],
      center: ['50%', '55%'],
      roseType: 'area',
      label: { color: '#5C3D1E', fontSize: 13 },
      emphasis: { label: { fontSize: 18, fontWeight: 'bold' } },
      data
    }]
  })
}

onMounted(async () => {
  await loadData()
  await nextTick()
})
</script>

<style scoped>
.stat-card { display: flex; align-items: center; gap: 20px; }
.stat-icon { width: 56px; height: 56px; border-radius: 14px; display: flex; align-items: center; justify-content: center; }
.stat-value { font-size: 30px; font-weight: 700; color: #3E2723; }
.stat-title { font-size: 14px; color: #8B5E3C; margin-top: 4px; }
.quick-actions { display: flex; gap: 24px; }
.action-item {
  display: flex; flex-direction: column; align-items: center; gap: 8px;
  padding: 20px 32px; border-radius: 12px; cursor: pointer;
  background: #FDF8F0; border: 1px solid #E8D5B7; transition: all 0.3s;
  color: #8B5E3C; font-size: 14px;
}
.action-item:hover { background: #8B5E3C; color: #FFFAF5; border-color: #8B5E3C; transform: translateY(-2px); box-shadow: 0 6px 20px rgba(139,94,60,0.2); }
</style>
