<template>
  <div class="report-statistics">
    <el-row :gutter="16" class="stats-row">
      <el-col :xs="12" :sm="8" :md="8" :lg="4" v-for="item in statsCards" :key="item.label">
        <el-card shadow="hover">
          <div class="stats-card">
            <span class="stats-label">{{ item.label }}</span>
            <span class="stats-value">{{ item.value }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="10">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>状态分布</span>
              <el-button type="primary" link @click="loadData">刷新</el-button>
            </div>
          </template>

          <div class="chart-wrap">
            <div class="pie-chart" :style="{ background: distributionGradient }">
              <div class="pie-inner">
                <div class="pie-total">{{ statistics.total || 0 }}</div>
                <div class="pie-desc">举报总量</div>
              </div>
            </div>
          </div>

          <div class="legend-list">
            <div class="legend-item" v-for="item in distributionRows" :key="item.status">
              <span class="dot" :style="{ backgroundColor: getStatusColor(item.status) }"></span>
              <span class="legend-label">{{ item.name }}</span>
              <span class="legend-value">{{ item.value }}</span>
              <span class="legend-rate">{{ item.rate }}%</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="14">
        <el-card shadow="never">
          <template #header>
            <span>统计详情</span>
          </template>

          <el-table :data="distributionRows" border stripe>
            <el-table-column prop="name" label="状态" min-width="160">
              <template #default="{ row }">
                <div class="status-cell">
                  <span class="dot" :style="{ backgroundColor: getStatusColor(row.status) }"></span>
                  <span>{{ row.name }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="value" label="数量" width="120" />
            <el-table-column prop="rate" label="占比" width="120">
              <template #default="{ row }">{{ row.rate }}%</template>
            </el-table-column>
            <el-table-column label="说明" min-width="180">
              <template #default="{ row }">{{ getStatusDescription(row.status) }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { getReportDistribution, getReportStatistics } from '@/api/report'

const distributionData = ref([])
const statistics = reactive({
  total: 0,
  pending: 0,
  processing: 0,
  resolved: 0,
  closed: 0,
  resolveRate: 0
})

const statusMap = {
  0: { label: '待处理', color: '#e6a23c', description: '线索已提交，等待管理员受理' },
  1: { label: '处理中', color: '#409eff', description: '管理员正在核查和跟进线索' },
  2: { label: '已解决', color: '#67c23a', description: '举报已核实并完成处理' },
  3: { label: '已关闭', color: '#909399', description: '举报已关闭或不再继续流转' }
}

const statsCards = computed(() => ([
  { label: '举报总量', value: statistics.total },
  { label: '待处理', value: statistics.pending },
  { label: '处理中', value: statistics.processing },
  { label: '已解决', value: statistics.resolved },
  { label: '已关闭', value: statistics.closed },
  { label: '解决率', value: `${Number(statistics.resolveRate || 0).toFixed(2)}%` }
]))

const distributionRows = computed(() => {
  const totalCount = Number(statistics.total || 0)

  return (distributionData.value || []).map((item) => ({
    ...item,
    rate: totalCount ? ((Number(item.value || 0) * 100) / totalCount).toFixed(2) : '0.00'
  }))
})

const distributionGradient = computed(() => {
  const rows = distributionRows.value
  if (!rows.length || !statistics.total) {
    return '#f5f7fa'
  }

  let start = 0
  const segments = rows.map((row) => {
    const end = start + Number(row.rate)
    const segment = `${getStatusColor(row.status)} ${start}% ${end}%`
    start = end
    return segment
  })

  return `conic-gradient(${segments.join(', ')})`
})

const getStatusColor = (status) => statusMap[status]?.color || '#909399'
const getStatusDescription = (status) => statusMap[status]?.description || '暂无说明'

const loadData = async () => {
  const [statsRes, distributionRes] = await Promise.all([
    getReportStatistics(),
    getReportDistribution()
  ])

  Object.assign(statistics, {
    total: statsRes.data?.total || 0,
    pending: statsRes.data?.pending || 0,
    processing: statsRes.data?.processing || 0,
    resolved: statsRes.data?.resolved || 0,
    closed: statsRes.data?.closed || 0,
    resolveRate: statsRes.data?.resolveRate || 0
  })

  distributionData.value = distributionRes.data || []
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.report-statistics {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stats-row {
  margin-bottom: 0;
}

.stats-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stats-label {
  color: #909399;
  font-size: 13px;
}

.stats-value {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-wrap {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}

.pie-chart {
  width: 220px;
  height: 220px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pie-inner {
  width: 140px;
  height: 140px;
  border-radius: 50%;
  background: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.pie-total {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}

.pie-desc {
  color: #909399;
}

.legend-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.legend-item {
  display: grid;
  grid-template-columns: 12px 1fr 40px 56px;
  align-items: center;
  gap: 8px;
}

.status-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.legend-label {
  color: #606266;
}

.legend-value,
.legend-rate {
  text-align: right;
  color: #303133;
}
</style>
