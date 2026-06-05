<template>
  <div class="question-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>题库管理</span>
          <el-button type="primary" icon="Plus" @click="handleAdd">新增题目</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="题型">
          <el-select v-model="queryParams.type" placeholder="全部题型" clearable style="width: 140px">
            <el-option label="单选题" :value="1" />
            <el-option label="多选题" :value="2" />
            <el-option label="判断题" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="queryParams.categoryId" placeholder="全部分类" clearable filterable style="width: 180px">
            <el-option
              v-for="item in categoryOptions"
              :key="item.id"
              :label="item.categoryName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="queryParams.difficulty" placeholder="全部难度" clearable style="width: 140px">
            <el-option label="简单" :value="1" />
            <el-option label="中等" :value="2" />
            <el-option label="困难" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="questionText" label="题目内容" min-width="280" />
        <el-table-column label="题型" width="100">
          <template #default="{ row }">
            <el-tag>{{ getQuestionTypeLabel(row.questionType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="分类" width="140">
          <template #default="{ row }">{{ getCategoryName(row.categoryId) }}</template>
        </el-table-column>
        <el-table-column label="难度" width="100">
          <template #default="{ row }">
            <el-tag :type="getDifficultyTagType(row.difficulty)">{{ getDifficultyLabel(row.difficulty) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="answer" label="答案" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link icon="View" @click="handleDetail(row)">详情</el-button>
            <el-button type="primary" link icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link icon="Delete" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="760px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="题目内容" prop="questionText">
          <el-input
            v-model="form.questionText"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="请输入题目内容"
          />
        </el-form-item>
        <el-form-item label="题型" prop="questionType">
          <el-radio-group v-model="form.questionType">
            <el-radio :label="1">单选题</el-radio>
            <el-radio :label="2">多选题</el-radio>
            <el-radio :label="3">判断题</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" clearable filterable placeholder="请选择分类" style="width: 220px">
            <el-option
              v-for="item in categoryOptions"
              :key="item.id"
              :label="item.categoryName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="难度" prop="difficulty">
          <el-radio-group v-model="form.difficulty">
            <el-radio :label="1">简单</el-radio>
            <el-radio :label="2">中等</el-radio>
            <el-radio :label="3">困难</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="题目状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>

        <template v-if="form.questionType !== 3">
          <el-form-item label="选项 A" prop="optionA">
            <el-input v-model="form.optionA" maxlength="200" placeholder="请输入选项 A" />
          </el-form-item>
          <el-form-item label="选项 B" prop="optionB">
            <el-input v-model="form.optionB" maxlength="200" placeholder="请输入选项 B" />
          </el-form-item>
          <el-form-item label="选项 C">
            <el-input v-model="form.optionC" maxlength="200" placeholder="请输入选项 C" />
          </el-form-item>
          <el-form-item label="选项 D">
            <el-input v-model="form.optionD" maxlength="200" placeholder="请输入选项 D" />
          </el-form-item>
        </template>

        <el-form-item v-else label="判断结果" prop="answer">
          <el-radio-group v-model="form.answer">
            <el-radio label="A">正确</el-radio>
            <el-radio label="B">错误</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="form.questionType === 1" label="正确答案" prop="answer">
          <el-radio-group v-model="form.answer">
            <el-radio label="A">A</el-radio>
            <el-radio label="B">B</el-radio>
            <el-radio label="C">C</el-radio>
            <el-radio label="D">D</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="form.questionType === 2" label="正确答案" prop="multiAnswer">
          <el-checkbox-group v-model="form.multiAnswer">
            <el-checkbox label="A">A</el-checkbox>
            <el-checkbox label="B">B</el-checkbox>
            <el-checkbox label="C">C</el-checkbox>
            <el-checkbox label="D">D</el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <el-form-item label="答案解析">
          <el-input
            v-model="form.analysis"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="请输入答案解析"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailVisible" title="题目详情" size="42%">
      <el-descriptions v-if="detailData" :column="1" border>
        <el-descriptions-item label="题目内容">{{ detailData.questionText }}</el-descriptions-item>
        <el-descriptions-item label="题型">{{ getQuestionTypeLabel(detailData.questionType) }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ getCategoryName(detailData.categoryId) }}</el-descriptions-item>
        <el-descriptions-item label="难度">{{ getDifficultyLabel(detailData.difficulty) }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detailData.status === 1 ? '启用' : '停用' }}</el-descriptions-item>
        <el-descriptions-item label="选项 A">{{ detailData.optionA || '-' }}</el-descriptions-item>
        <el-descriptions-item label="选项 B">{{ detailData.optionB || '-' }}</el-descriptions-item>
        <el-descriptions-item label="选项 C">{{ detailData.optionC || '-' }}</el-descriptions-item>
        <el-descriptions-item label="选项 D">{{ detailData.optionD || '-' }}</el-descriptions-item>
        <el-descriptions-item label="正确答案">{{ formatAnswerLabel(detailData.answer, detailData.questionType) }}</el-descriptions-item>
        <el-descriptions-item label="答案解析">
          <div class="detail-text">{{ detailData.analysis || '-' }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllCategories } from '@/api/category'
import { addQuestion, deleteQuestion, getQuestionList, updateQuestion } from '@/api/exam'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const dialogTitle = ref('新增题目')
const total = ref(0)
const tableData = ref([])
const detailData = ref(null)
const categoryOptions = ref([])
const formRef = ref()

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  type: null,
  categoryId: null,
  difficulty: null
})

const form = reactive({
  id: null,
  questionText: '',
  questionType: 1,
  categoryId: null,
  difficulty: 1,
  optionA: '',
  optionB: '',
  optionC: '',
  optionD: '',
  answer: 'A',
  multiAnswer: [],
  analysis: '',
  status: 1
})

const rules = computed(() => ({
  questionText: [{ required: true, message: '请输入题目内容', trigger: 'blur' }],
  questionType: [{ required: true, message: '请选择题型', trigger: 'change' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  optionA: form.questionType !== 3 ? [{ required: true, message: '请输入选项 A', trigger: 'blur' }] : [],
  optionB: form.questionType !== 3 ? [{ required: true, message: '请输入选项 B', trigger: 'blur' }] : [],
  answer: form.questionType !== 2 ? [{ required: true, message: '请选择正确答案', trigger: 'change' }] : [],
  multiAnswer: form.questionType === 2
    ? [{
        validator: (_, value, callback) => {
          if (!value || !value.length) {
            callback(new Error('请至少选择一个正确答案'))
            return
          }
          callback()
        },
        trigger: 'change'
      }]
    : []
}))

const categoryMap = computed(() => {
  const map = new Map()
  ;(categoryOptions.value || []).forEach((item) => {
    map.set(item.id, item.categoryName)
  })
  return map
})

watch(() => form.questionType, (value) => {
  if (value === 3) {
    form.optionA = '正确'
    form.optionB = '错误'
    form.optionC = ''
    form.optionD = ''
    if (!['A', 'B'].includes(form.answer)) {
      form.answer = 'A'
    }
  } else {
    if (form.optionA === '正确' && form.optionB === '错误') {
      form.optionA = ''
      form.optionB = ''
    }
    if (value === 2) {
      form.answer = ''
    } else if (!form.answer) {
      form.answer = 'A'
    }
  }

  if (value !== 2) {
    form.multiAnswer = []
  }

  formRef.value?.clearValidate()
})

const getList = async () => {
  loading.value = true
  try {
    const res = await getQuestionList(queryParams)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  const res = await getAllCategories()
  categoryOptions.value = res.data || []
}

const getQuestionTypeLabel = (type) => {
  const map = { 1: '单选题', 2: '多选题', 3: '判断题' }
  return map[type] || '未知'
}

const getDifficultyLabel = (difficulty) => {
  const map = { 1: '简单', 2: '中等', 3: '困难' }
  return map[difficulty] || '未知'
}

const getDifficultyTagType = (difficulty) => {
  const map = { 1: 'success', 2: 'warning', 3: 'danger' }
  return map[difficulty] || 'info'
}

const getCategoryName = (categoryId) => categoryMap.value.get(categoryId) || '-'

const normalizeMultiAnswer = (value = []) => {
  return [...new Set(value)].sort().join(',')
}

const formatAnswerLabel = (answer, questionType) => {
  if (!answer) return '-'
  if (questionType === 3) {
    return answer === 'A' ? '正确' : '错误'
  }
  if (questionType === 2) {
    return answer.split(',').join('、')
  }
  return answer
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    questionText: '',
    questionType: 1,
    categoryId: null,
    difficulty: 1,
    optionA: '',
    optionB: '',
    optionC: '',
    optionD: '',
    answer: 'A',
    multiAnswer: [],
    analysis: '',
    status: 1
  })
  formRef.value?.clearValidate()
}

const fillForm = (row) => {
  Object.assign(form, {
    id: row.id ?? null,
    questionText: row.questionText || '',
    questionType: row.questionType ?? 1,
    categoryId: row.categoryId || null,
    difficulty: row.difficulty ?? 1,
    optionA: row.optionA || '',
    optionB: row.optionB || '',
    optionC: row.optionC || '',
    optionD: row.optionD || '',
    answer: row.questionType === 2 ? '' : (row.answer || 'A'),
    multiAnswer: row.questionType === 2 && row.answer ? row.answer.split(',') : [],
    analysis: row.analysis || '',
    status: row.status ?? 1
  })
}

const buildPayload = () => ({
  questionText: form.questionText.trim(),
  questionType: form.questionType,
  categoryId: form.categoryId,
  difficulty: form.difficulty,
  optionA: form.questionType === 3 ? '正确' : form.optionA.trim(),
  optionB: form.questionType === 3 ? '错误' : form.optionB.trim(),
  optionC: form.questionType === 3 ? '' : (form.optionC?.trim() || ''),
  optionD: form.questionType === 3 ? '' : (form.optionD?.trim() || ''),
  answer: form.questionType === 2 ? normalizeMultiAnswer(form.multiAnswer) : form.answer,
  analysis: form.analysis?.trim() || '',
  status: form.status
})

const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

const resetQuery = () => {
  Object.assign(queryParams, {
    pageNum: 1,
    pageSize: 10,
    type: null,
    categoryId: null,
    difficulty: null
  })
  getList()
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增题目'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  resetForm()
  fillForm(row)
  dialogTitle.value = '编辑题目'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const payload = buildPayload()
    if (form.id) {
      await updateQuestion(form.id, payload)
      ElMessage.success('题目更新成功')
    } else {
      await addQuestion(payload)
      ElMessage.success('题目新增成功')
    }
    dialogVisible.value = false
    await getList()
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除题目“${row.questionText}”吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteQuestion(row.id)
      ElMessage.success('删除成功')
      getList()
    })
    .catch(() => {})
}

const handleDetail = (row) => {
  detailData.value = row
  detailVisible.value = true
}

onMounted(async () => {
  await loadCategories()
  await getList()
})
</script>

<style scoped>
.question-manage {
  padding: 20px;
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

.detail-text {
  white-space: pre-wrap;
  line-height: 1.7;
}
</style>
