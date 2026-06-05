<template>
  <div class="report-mine">
    <el-row :gutter="16">
      <el-col :xs="24" :lg="10">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>提交举报</span>
              <el-tag type="warning">实名线索</el-tag>
            </div>
          </template>

          <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
            <el-form-item label="举报标题" prop="title">
              <el-input v-model="form.title" maxlength="100" show-word-limit placeholder="请输入举报标题" />
            </el-form-item>

            <el-form-item label="诈骗类型">
              <el-input v-model="form.fraudType" maxlength="50" placeholder="例如刷单诈骗、冒充客服" />
            </el-form-item>

            <el-form-item label="联系人">
              <el-input v-model="form.contactName" maxlength="30" placeholder="请输入联系人姓名" />
            </el-form-item>

            <el-form-item label="联系电话">
              <el-input v-model="form.contactPhone" maxlength="20" placeholder="请输入联系电话" />
            </el-form-item>

            <el-form-item label="证据链接">
              <el-input
                v-model="form.evidenceImages"
                type="textarea"
                :rows="3"
                placeholder="可填写图片链接，多个链接用逗号分隔"
              />
            </el-form-item>

            <el-form-item label="详细描述" prop="description">
              <el-input
                v-model="form.description"
                type="textarea"
                :rows="6"
                maxlength="1000"
                show-word-limit
                placeholder="请尽量描述清楚时间、经过、对方信息和证据情况"
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="submitLoading" @click="handleSubmit">提交举报</el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="14">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>我的举报记录</span>
              <el-button type="primary" link @click="getList">刷新</el-button>
            </div>
          </template>

          <el-table :data="tableData" border stripe v-loading="loading">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="title" label="举报标题" min-width="180" />
            <el-table-column prop="fraudType" label="诈骗类型" width="120" />
            <el-table-column label="状态" width="110">
              <template #default="{ row }">
                <el-tag :type="getStatusTagType(row.status)">
                  {{ getStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="提交时间" width="180" />
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            class="pagination"
            v-model:current-page="queryParams.pageNum"
            v-model:page-size="queryParams.pageSize"
            :page-sizes="[5, 10, 20]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="getList"
            @current-change="getList"
          />
        </el-card>
      </el-col>
    </el-row>

    <el-drawer v-model="detailVisible" title="举报详情" size="42%">
      <el-descriptions v-if="detailData" :column="1" border>
        <el-descriptions-item label="举报标题">{{ detailData.title }}</el-descriptions-item>
        <el-descriptions-item label="诈骗类型">{{ detailData.fraudType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTagType(detailData.status)">
            {{ getStatusLabel(detailData.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="联系人">{{ detailData.contactName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ detailData.contactPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ detailData.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理时间">{{ detailData.handleTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理结果">
          <div class="desc">{{ detailData.handleResult || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="证据链接">
          <div class="desc">{{ detailData.evidenceImages || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="详细描述">
          <div class="desc">{{ detailData.description || '-' }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import useUserStore from '@/store/modules/user'
import { getMyReports, getReportDetail, submitReport } from '@/api/report'

const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const submitLoading = ref(false)
const detailVisible = ref(false)
const total = ref(0)
const tableData = ref([])
const detailData = ref(null)

const form = reactive({
  title: '',
  fraudType: '',
  description: '',
  evidenceImages: '',
  contactPhone: '',
  contactName: ''
})

const queryParams = reactive({
  pageNum: 1,
  pageSize: 5
})

const rules = {
  title: [{ required: true, message: '请输入举报标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入详细描述', trigger: 'blur' }]
}

const statusMap = {
  0: { label: '待处理', tag: 'warning' },
  1: { label: '处理中', tag: 'primary' },
  2: { label: '已解决', tag: 'success' },
  3: { label: '已关闭', tag: 'info' }
}

const getStatusLabel = (status) => statusMap[status]?.label || '未知状态'
const getStatusTagType = (status) => statusMap[status]?.tag || 'info'

const getList = async () => {
  const reporterId = userStore.userInfo?.id
  if (!reporterId) return

  loading.value = true
  try {
    const res = await getMyReports({
      reporterId,
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize
    })
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  Object.assign(form, {
    title: '',
    fraudType: '',
    description: '',
    evidenceImages: '',
    contactPhone: '',
    contactName: ''
  })
  formRef.value?.clearValidate()
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  const reporterId = userStore.userInfo?.id
  if (!reporterId) {
    ElMessage.error('未获取到当前登录用户信息，请重新登录后再试')
    return
  }

  submitLoading.value = true
  try {
    await submitReport({
      reporterId,
      ...form
    })
    ElMessage.success('举报提交成功')
    resetForm()
    queryParams.pageNum = 1
    await getList()
  } finally {
    submitLoading.value = false
  }
}

const handleDetail = async (row) => {
  const res = await getReportDetail(row.id)
  detailData.value = res.data || null
  detailVisible.value = true
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.report-mine {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

.desc {
  white-space: pre-wrap;
  line-height: 1.6;
}
</style>
