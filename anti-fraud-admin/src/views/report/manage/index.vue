<template>
  <div class="report-manage">
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
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>举报管理</span>
              <el-button icon="Refresh" @click="refreshAll">刷新</el-button>
            </div>
          </template>

          <el-form :inline="true" :model="queryParams" class="search-form">
            <el-form-item label="关键词">
              <el-input
                v-model="queryParams.keyword"
                placeholder="标题/诈骗类型"
                clearable
                @keyup.enter="handleQuery"
              />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 130px">
                <el-option label="待处理" :value="0" />
                <el-option label="处理中" :value="1" />
                <el-option label="已解决" :value="2" />
                <el-option label="已关闭" :value="3" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
              <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
          </el-form>

          <el-table :data="tableData" border stripe v-loading="loading">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="title" label="举报标题" min-width="180" />
            <el-table-column prop="fraudType" label="诈骗类型" width="120" />
            <el-table-column prop="reporterId" label="举报人ID" width="110" />
            <el-table-column label="联系人" width="180">
              <template #default="{ row }">
                <div>{{ row.contactName || '-' }}</div>
                <div>{{ row.contactPhone || '-' }}</div>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusTagType(row.status)">
                  {{ getStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="举报时间" width="165" />
            <el-table-column prop="handleTime" label="处理时间" width="165" />
            <el-table-column label="处理结果" min-width="180">
              <template #default="{ row }">
                <span class="ellipsis">{{ row.handleResult || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link icon="View" @click="handleDetail(row)">详情</el-button>
                <el-button
                  type="warning"
                  link
                  :disabled="!canHandle(row.status)"
                  @click="openHandleDialog(row)"
                >
                  处理
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            class="pagination"
            v-model:current-page="queryParams.pageNum"
            v-model:page-size="queryParams.pageSize"
            :page-sizes="[10, 20, 50]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="getList"
            @current-change="getList"
          />
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>状态分布图</span>
            </div>
          </template>
          <div class="chart-wrap">
            <div class="pie-chart" :style="{ background: distributionGradient }">
              <div class="pie-inner">
                <div class="pie-total">{{ statistics.total || 0 }}</div>
                <div class="pie-desc">总举报</div>
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
    </el-row>

    <el-dialog v-model="handleDialogVisible" title="处理举报" width="520px" destroy-on-close>
      <el-form ref="handleFormRef" :model="handleForm" :rules="handleRules" label-width="96px">
        <el-form-item label="当前状态">
          <el-tag :type="getStatusTagType(currentRow?.status)">
            {{ getStatusLabel(currentRow?.status) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="流转状态" prop="status">
          <el-select v-model="handleForm.status" placeholder="请选择目标状态" style="width: 100%">
            <el-option
              v-for="option in nextStatusOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="处理结果" prop="handleResult">
          <el-input
            v-model="handleForm.handleResult"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="请输入处理说明"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitHandle">确认处理</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailVisible" title="举报详情" size="45%">
      <el-descriptions :column="1" border v-if="detailData">
        <el-descriptions-item label="ID">{{ detailData.id }}</el-descriptions-item>
        <el-descriptions-item label="举报标题">{{ detailData.title }}</el-descriptions-item>
        <el-descriptions-item label="诈骗类型">{{ detailData.fraudType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTagType(detailData.status)">
            {{ getStatusLabel(detailData.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="举报人ID">{{ detailData.reporterId }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ detailData.contactName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ detailData.contactPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="举报时间">{{ detailData.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理时间">{{ detailData.handleTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理人ID">{{ detailData.handlerId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="详细描述">
          <div class="desc">{{ detailData.description || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="证据图片">{{ detailData.evidenceImages || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理结果">
          <div class="desc">{{ detailData.handleResult || '-' }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import useUserStore from '@/store/modules/user'
import { getReportDetail, getReportDistribution, getReportList, getReportStatistics, handleReport } from '@/api/report'

const userStore = useUserStore()
const loading = ref(false)
const submitLoading = ref(false)
const handleDialogVisible = ref(false)
const detailVisible = ref(false)
const total = ref(0)
const tableData = ref([])
const detailData = ref(null)
const currentRow = ref(null)
const distributionData = ref([])
const statistics = reactive({
  total: 0,
  pending: 0,
  processing: 0,
  resolved: 0,
  closed: 0,
  resolveRate: 0
})
const handleFormRef = ref()

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  status: null
})

const handleForm = reactive({
  status: null,
  handleResult: ''
})

const handleRules = {
  status: [{ required: true, message: '请选择流转状态', trigger: 'change' }],
  handleResult: [{ required: true, message: '请输入处理结果', trigger: 'blur' }]
}

const statusMap = {
  0: { label: '待处理', tag: 'warning', color: '#e6a23c' },
  1: { label: '处理中', tag: 'primary', color: '#409eff' },
  2: { label: '已解决', tag: 'success', color: '#67c23a' },
  3: { label: '已关闭', tag: 'info', color: '#909399' }
}

const transitionMap = {
  0: [1, 3],
  1: [2, 3],
  2: [],
  3: []
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
  const totalCount = statistics.total || 0
  return (distributionData.value || []).map((item) => ({
    ...item,
    rate: totalCount ? ((item.value * 100) / totalCount).toFixed(2) : '0.00'
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

const nextStatusOptions = computed(() => {
  const currentStatus = currentRow.value?.status
  return (transitionMap[currentStatus] || []).map((value) => ({
    value,
    label: getStatusLabel(value)
  }))
})

const getStatusLabel = (status) => statusMap[status]?.label || '未知'
const getStatusTagType = (status) => statusMap[status]?.tag || 'info'
const getStatusColor = (status) => statusMap[status]?.color || '#909399'
const canHandle = (status) => (transitionMap[status] || []).length > 0

const getList = async () => {
  loading.value = true
  try {
    const res = await getReportList(queryParams)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const getStats = async () => {
  const res = await getReportStatistics()
  Object.assign(statistics, {
    total: res.data?.total || 0,
    pending: res.data?.pending || 0,
    processing: res.data?.processing || 0,
    resolved: res.data?.resolved || 0,
    closed: res.data?.closed || 0,
    resolveRate: res.data?.resolveRate || 0
  })
}

const getDistribution = async () => {
  const res = await getReportDistribution()
  distributionData.value = res.data || []
}

const refreshAll = async () => {
  await Promise.all([getList(), getStats(), getDistribution()])
}

const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

const resetQuery = () => {
  Object.assign(queryParams, { pageNum: 1, pageSize: 10, keyword: '', status: null })
  getList()
}

const handleDetail = async (row) => {
  const res = await getReportDetail(row.id)
  detailData.value = res.data || null
  detailVisible.value = true
}

const openHandleDialog = (row) => {
  currentRow.value = row
  handleForm.status = null
  handleForm.handleResult = ''
  handleDialogVisible.value = true
}

const submitHandle = async () => {
  const valid = await handleFormRef.value.validate().catch(() => false)
  if (!valid || !currentRow.value) return

  const handlerId = userStore.userInfo?.id
  if (!handlerId) {
    ElMessage.error('未获取到当前登录用户信息，请重新登录后重试')
    return
  }

  submitLoading.value = true
  try {
    await handleReport(currentRow.value.id, {
      handlerId,
      status: handleForm.status,
      handleResult: handleForm.handleResult
    })
    ElMessage.success('处理成功')
    handleDialogVisible.value = false
    await refreshAll()
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  refreshAll()
})
</script>

<style scoped>
.report-manage {
  padding: 20px;
}

.stats-row {
  margin-bottom: 16px;
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

.search-form {
  margin-bottom: 16px;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

.chart-wrap {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}

.pie-chart {
  width: 180px;
  height: 180px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pie-inner {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.pie-total {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.pie-desc {
  color: #909399;
}

.legend-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.legend-item {
  display: grid;
  grid-template-columns: 12px 1fr 40px 56px;
  align-items: center;
  gap: 8px;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.legend-label {
  color: #606266;
}

.legend-value {
  text-align: right;
  color: #303133;
}

.legend-rate {
  text-align: right;
  color: #909399;
}

.ellipsis {
  display: inline-block;
  max-width: 170px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.desc {
  white-space: pre-wrap;
  line-height: 1.6;
}
</style>
