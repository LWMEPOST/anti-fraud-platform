<template>
  <div class="exam-record">
    <el-row :gutter="16">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>成绩查询</span>
              <el-button icon="Refresh" @click="getRecordList">刷新</el-button>
            </div>
          </template>

          <el-table :data="recordList" border stripe v-loading="recordLoading">
            <el-table-column prop="id" label="记录ID" width="100" />
            <el-table-column prop="totalScore" label="得分" width="100" />
            <el-table-column label="正确率" width="120">
              <template #default="{ row }">
                {{ getAccuracy(row) }}%
              </template>
            </el-table-column>
            <el-table-column label="答题情况" width="140">
              <template #default="{ row }">
                {{ row.correctCount || 0 }}/{{ row.totalQuestions || 0 }}
              </template>
            </el-table-column>
            <el-table-column prop="duration" label="用时(秒)" width="110" />
            <el-table-column prop="submitTime" label="交卷时间" min-width="170" />
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
            :page-sizes="[10, 20, 50]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="getRecordList"
            @current-change="getRecordList"
          />
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>成绩排行榜</span>
              <el-select v-model="topN" style="width: 110px" @change="getRankingList">
                <el-option :value="5" label="Top 5" />
                <el-option :value="10" label="Top 10" />
                <el-option :value="20" label="Top 20" />
              </el-select>
            </div>
          </template>

          <el-table :data="rankingList" size="small" v-loading="rankingLoading">
            <el-table-column prop="rank" label="#" width="50" />
            <el-table-column label="用户" min-width="130">
              <template #default="{ row }">
                {{ row.realName || row.username || `用户${row.userId}` }}
              </template>
            </el-table-column>
            <el-table-column prop="score" label="得分" width="80" />
            <el-table-column label="正确" width="95">
              <template #default="{ row }">
                {{ row.correctCount }}/{{ row.totalQuestions }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-drawer v-model="detailVisible" title="成绩详情" size="50%">
      <template v-if="detailData.record">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="记录ID">{{ detailData.record.id }}</el-descriptions-item>
          <el-descriptions-item label="得分">{{ detailData.record.totalScore }}</el-descriptions-item>
          <el-descriptions-item label="正确题数">
            {{ detailData.record.correctCount }}/{{ detailData.record.totalQuestions }}
          </el-descriptions-item>
          <el-descriptions-item label="正确率">{{ detailData.accuracy }}%</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ detailData.record.startTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="交卷时间">{{ detailData.record.submitTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="答题用时">{{ detailData.record.duration || 0 }} 秒</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">答题明细</el-divider>

        <el-table :data="detailData.answers || []" border stripe>
          <el-table-column prop="questionId" label="题号" width="80" />
          <el-table-column prop="questionText" label="题目" min-width="240" />
          <el-table-column prop="userAnswer" label="我的答案" width="90" />
          <el-table-column prop="correctAnswer" label="正确答案" width="90" />
          <el-table-column label="结果" width="90">
            <template #default="{ row }">
              <el-tag :type="row.isCorrect === 1 ? 'success' : 'danger'">
                {{ row.isCorrect === 1 ? '正确' : '错误' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="analysis" label="解析" min-width="220" />
        </el-table>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import useUserStore from '@/store/modules/user'
import { getExamRecordDetail, getExamRecordList, getExamRanking } from '@/api/exam'

const userStore = useUserStore()
const recordLoading = ref(false)
const rankingLoading = ref(false)
const detailVisible = ref(false)
const total = ref(0)
const topN = ref(10)
const recordList = ref([])
const rankingList = ref([])
const detailData = ref({
  record: null,
  answers: [],
  accuracy: 0
})

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10
})

const getAccuracy = (row) => {
  if (!row.totalQuestions) return '0.00'
  return ((row.correctCount * 100) / row.totalQuestions).toFixed(2)
}

const getRecordList = async () => {
  if (!userStore.userInfo?.id) return
  recordLoading.value = true
  try {
    const res = await getExamRecordList({
      userId: userStore.userInfo.id,
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize
    })
    recordList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    recordLoading.value = false
  }
}

const getRankingList = async () => {
  rankingLoading.value = true
  try {
    const res = await getExamRanking(topN.value)
    rankingList.value = res.data || []
  } finally {
    rankingLoading.value = false
  }
}

const handleDetail = async (row) => {
  const res = await getExamRecordDetail(row.id)
  detailData.value = res.data || { record: null, answers: [], accuracy: 0 }
  detailVisible.value = true
}

onMounted(async () => {
  if (!userStore.userInfo?.id) {
    ElMessage.error('未获取到用户信息，请重新登录')
    return
  }
  await Promise.all([getRecordList(), getRankingList()])
})
</script>

<style scoped>
.exam-record {
  padding: 20px;
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
</style>
