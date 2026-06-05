<template>
  <div class="exam-online">
    <el-card class="mb16">
      <template #header>
        <div class="card-header">
          <span>在线测试</span>
          <el-button type="primary" :loading="startLoading" @click="handleStartExam">开始测试</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="startForm">
        <el-form-item label="抽题数量">
          <el-input-number v-model="startForm.questionCount" :min="1" :max="50" />
        </el-form-item>
      </el-form>

      <el-alert
        v-if="examState.recordId"
        type="info"
        :closable="false"
        show-icon
        :title="`本次记录ID：${examState.recordId}，已作答 ${answeredCount}/${examState.questions.length}`"
      />
    </el-card>

    <el-card v-if="resultData" class="mb16">
      <el-result icon="success" title="交卷成功">
        <template #sub-title>
          <div class="result-line">
            得分：<b>{{ resultData.score }}</b> 分，
            正确：<b>{{ resultData.correctCount }}</b>/<b>{{ resultData.totalQuestions }}</b>，
            正确率：<b>{{ resultData.accuracy }}%</b>
          </div>
        </template>
      </el-result>
    </el-card>

    <el-card v-if="examState.questions.length">
      <template #header>
        <div class="card-header">
          <span>题目列表</span>
          <el-button type="success" :loading="submitLoading" @click="handleSubmitExam">交卷</el-button>
        </div>
      </template>

      <div v-for="(question, index) in examState.questions" :key="question.id" class="question-item">
        <div class="question-title">
          {{ index + 1 }}. [{{ getQuestionTypeLabel(question.questionType) }}] {{ question.questionText }}
        </div>

        <el-radio-group
          v-if="question.questionType === 1 || question.questionType === 3"
          v-model="answerMap[question.id]"
          @change="saveAnswerCache"
        >
          <el-radio v-if="question.optionA" label="A">{{ question.optionA }}</el-radio>
          <el-radio v-if="question.optionB" label="B">{{ question.optionB }}</el-radio>
          <el-radio v-if="question.optionC" label="C">{{ question.optionC }}</el-radio>
          <el-radio v-if="question.optionD" label="D">{{ question.optionD }}</el-radio>
        </el-radio-group>

        <el-checkbox-group
          v-else-if="question.questionType === 2"
          :model-value="getMultiAnswer(question.id)"
          @change="(val) => handleMultiChange(question.id, val)"
        >
          <el-checkbox v-if="question.optionA" label="A">{{ question.optionA }}</el-checkbox>
          <el-checkbox v-if="question.optionB" label="B">{{ question.optionB }}</el-checkbox>
          <el-checkbox v-if="question.optionC" label="C">{{ question.optionC }}</el-checkbox>
          <el-checkbox v-if="question.optionD" label="D">{{ question.optionD }}</el-checkbox>
        </el-checkbox-group>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import useUserStore from '@/store/modules/user'
import { startExam, submitExam } from '@/api/exam'

const userStore = useUserStore()
const startLoading = ref(false)
const submitLoading = ref(false)
const resultData = ref(null)

const startForm = reactive({
  questionCount: 5
})

const examState = reactive({
  recordId: null,
  questions: []
})

const answerMap = reactive({})

const answeredCount = computed(() => {
  return examState.questions.filter((q) => Boolean(answerMap[q.id])).length
})

const getQuestionTypeLabel = (type) => {
  const map = { 1: '单选', 2: '多选', 3: '判断' }
  return map[type] || '未知题型'
}

const getCacheKey = () => {
  return examState.recordId ? `exam_answer_cache_${userStore.userInfo?.id}_${examState.recordId}` : ''
}

const loadAnswerCache = () => {
  const cacheKey = getCacheKey()
  if (!cacheKey) return
  const cache = localStorage.getItem(cacheKey)
  if (!cache) return
  try {
    const parsed = JSON.parse(cache)
    Object.keys(parsed).forEach((key) => {
      answerMap[key] = parsed[key]
    })
  } catch (e) {
    localStorage.removeItem(cacheKey)
  }
}

const saveAnswerCache = () => {
  const cacheKey = getCacheKey()
  if (!cacheKey) return
  localStorage.setItem(cacheKey, JSON.stringify(answerMap))
}

const clearAnswerMap = () => {
  Object.keys(answerMap).forEach((key) => {
    delete answerMap[key]
  })
}

const handleStartExam = async () => {
  if (!userStore.userInfo?.id) {
    ElMessage.error('未获取到用户信息，请重新登录')
    return
  }

  startLoading.value = true
  try {
    resultData.value = null
    clearAnswerMap()
    const res = await startExam({
      userId: userStore.userInfo.id,
      questionCount: startForm.questionCount
    })
    examState.recordId = res.data?.recordId
    examState.questions = res.data?.questions || []
    loadAnswerCache()
    ElMessage.success(`已开始测试，共 ${examState.questions.length} 题`)
  } finally {
    startLoading.value = false
  }
}

const getMultiAnswer = (questionId) => {
  const value = answerMap[questionId]
  return value ? value.split(',') : []
}

const handleMultiChange = (questionId, values) => {
  answerMap[questionId] = (values || []).slice().sort().join(',')
  saveAnswerCache()
}

const handleSubmitExam = async () => {
  if (!examState.recordId || !examState.questions.length) {
    ElMessage.warning('请先开始测试')
    return
  }

  const unanswered = examState.questions.filter((q) => !answerMap[q.id]).length
  const tip = unanswered > 0 ? `还有 ${unanswered} 题未作答，确认交卷？` : '确认交卷？'
  const confirmed = await ElMessageBox.confirm(tip, '提示', { type: 'warning' }).catch(() => false)
  if (!confirmed) return

  submitLoading.value = true
  try {
    const payload = {
      recordId: examState.recordId,
      answers: examState.questions.map((q) => ({
        questionId: q.id,
        userAnswer: answerMap[q.id] || ''
      }))
    }
    const res = await submitExam(userStore.userInfo.id, payload)
    resultData.value = res.data || null

    const cacheKey = getCacheKey()
    if (cacheKey) {
      localStorage.removeItem(cacheKey)
    }
    ElMessage.success('交卷成功')
  } finally {
    submitLoading.value = false
  }
}
</script>

<style scoped>
.exam-online {
  padding: 20px;
}

.mb16 {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.question-item {
  padding: 14px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  margin-bottom: 12px;
}

.question-title {
  font-weight: 600;
  margin-bottom: 10px;
  line-height: 1.6;
}

.result-line {
  font-size: 15px;
  color: #606266;
}
</style>
