<template>
  <div class="content-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>知识库管理</span>
          <el-button type="primary" icon="Plus" @click="handleAdd">新增内容</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="queryParams.keyword"
            placeholder="请输入标题关键词"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 130px">
            <el-option label="待审核" :value="0" />
            <el-option label="已上线" :value="1" />
            <el-option label="已下架" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容类型">
          <el-select v-model="queryParams.contentType" placeholder="全部类型" clearable style="width: 130px">
            <el-option label="文章" :value="1" />
            <el-option label="视频" :value="2" />
            <el-option label="漫画" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-select
            v-model="queryParams.categoryId"
            placeholder="全部分类"
            clearable
            filterable
            style="width: 180px"
          >
            <el-option
              v-for="item in categoryOptions"
              :key="item.id"
              :label="item.categoryName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="220" />
        <el-table-column label="类型" width="110">
          <template #default="{ row }">
            <el-tag>{{ getTypeLabel(row.contentType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览" width="90" />
        <el-table-column prop="likeCount" label="点赞" width="90" />
        <el-table-column label="分类" min-width="140">
          <template #default="{ row }">
            {{ getCategoryLabel(row) }}
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.publishTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="420" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link icon="View" @click="handleDetail(row)">详情</el-button>
            <el-button type="primary" link icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link @click="handleLike(row)">点赞+1</el-button>
            <el-button
              v-if="row.status === 0"
              type="success"
              link
              @click="handleAudit(row, 1)"
            >
              审核通过
            </el-button>
            <el-button
              v-if="row.status === 0 || row.status === 1"
              type="warning"
              link
              @click="handleAudit(row, 2)"
            >
              {{ row.status === 0 ? '审核驳回' : '下架' }}
            </el-button>
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
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="200" show-word-limit placeholder="请输入内容标题" />
        </el-form-item>
        <el-form-item label="内容类型" prop="contentType">
          <el-radio-group v-model="form.contentType">
            <el-radio :label="1">文章</el-radio>
            <el-radio :label="2">视频</el-radio>
            <el-radio :label="3">漫画</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="分类">
          <el-select
            v-model="form.categoryId"
            clearable
            filterable
            placeholder="请选择分类"
            style="width: 220px"
          >
            <el-option
              v-for="item in categoryOptions"
              :key="item.id"
              :label="item.categoryName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="封面图片">
          <div class="media-editor">
            <el-radio-group v-model="coverImageInputMode" size="small" class="media-mode-switch">
              <el-radio-button label="url">网络地址</el-radio-button>
              <el-radio-button label="upload">本地上传</el-radio-button>
            </el-radio-group>

            <el-input
              v-if="coverImageInputMode === 'url'"
              v-model="form.coverImage"
              placeholder="请输入封面图片地址，支持网络图片或本地上传后的地址"
              clearable
            />

            <div v-else class="upload-panel">
              <el-upload
                :show-file-list="false"
                :http-request="uploadCoverImage"
                :before-upload="beforeImageUpload"
                accept=".jpg,.jpeg,.png,.gif,.webp,.bmp"
              >
                <el-button type="primary">选择图片</el-button>
              </el-upload>
              <span class="upload-tip">图片会保存到项目的 `anti-fraud-platform/uploads` 目录中</span>
            </div>

            <div v-if="form.coverImage" class="media-preview image-preview">
              <el-image
                :src="form.coverImage"
                fit="contain"
                class="preview-image"
                :preview-src-list="[form.coverImage]"
                :initial-index="0"
              />
              <div class="media-preview-meta">
                <div class="media-url">{{ form.coverImage }}</div>
                <el-button link type="danger" @click="form.coverImage = ''">清空图片</el-button>
              </div>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="视频地址" prop="videoUrl" v-if="form.contentType === 2">
          <div class="media-editor">
            <el-radio-group v-model="videoInputMode" size="small" class="media-mode-switch">
              <el-radio-button label="url">网络地址</el-radio-button>
              <el-radio-button label="upload">本地上传</el-radio-button>
            </el-radio-group>

            <el-input
              v-if="videoInputMode === 'url'"
              v-model="form.videoUrl"
              placeholder="请输入视频地址，支持网络视频或本地上传后的地址"
              clearable
            />

            <div v-else class="upload-panel">
              <el-upload
                :show-file-list="false"
                :http-request="uploadVideoFile"
                :before-upload="beforeVideoUpload"
                accept=".mp4,.mov,.webm,.ogg,.m4v"
              >
                <el-button type="primary">选择视频</el-button>
              </el-upload>
              <span class="upload-tip">视频会保存到项目的 `anti-fraud-platform/uploads` 目录中</span>
            </div>

            <div v-if="form.videoUrl" class="media-preview video-preview">
              <video class="preview-video" :src="form.videoUrl" controls preload="metadata" />
              <div class="media-preview-meta">
                <div class="media-url">{{ form.videoUrl }}</div>
                <el-button link type="danger" @click="form.videoUrl = ''">清空视频</el-button>
              </div>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="正文内容" prop="content">
          <div class="content-editor">
            <el-input
              v-model="form.content"
              type="textarea"
              :rows="8"
              maxlength="10000"
              show-word-limit
              :placeholder="form.contentType === 2 ? '可填写视频简介（可选）' : '请输入正文内容，支持粘贴 HTML 图片标签'"
            />

            <div v-if="form.contentType !== 2" class="inline-media-tools">
              <el-upload
                :show-file-list="false"
                :http-request="uploadInlineImage"
                :before-upload="beforeImageUpload"
                accept=".jpg,.jpeg,.png,.gif,.webp,.bmp"
              >
                <el-button plain>上传正文图片</el-button>
              </el-upload>
              <span class="upload-tip">上传成功后会自动把图片标签插入正文末尾，位置可以继续手动调整</span>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailVisible" title="内容详情" size="45%">
      <el-descriptions :column="1" border v-if="detailData">
        <el-descriptions-item label="ID">{{ detailData.id }}</el-descriptions-item>
        <el-descriptions-item label="标题">{{ detailData.title }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ getTypeLabel(detailData.contentType) }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ getStatusLabel(detailData.status) }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ getCategoryLabel(detailData) }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(detailData.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ formatDateTime(detailData.publishTime) }}</el-descriptions-item>
        <el-descriptions-item label="封面图片">
          <div v-if="detailData.coverImage" class="drawer-media-block">
            <el-image
              :src="detailData.coverImage"
              fit="contain"
              class="drawer-image"
              :preview-src-list="[detailData.coverImage]"
              :initial-index="0"
            />
            <a :href="detailData.coverImage" target="_blank" rel="noreferrer">{{ detailData.coverImage }}</a>
          </div>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="视频地址">
          <div v-if="detailData.videoUrl" class="drawer-media-block">
            <video class="drawer-video" :src="detailData.videoUrl" controls preload="metadata" />
            <a :href="detailData.videoUrl" target="_blank" rel="noreferrer">{{ detailData.videoUrl }}</a>
          </div>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="正文内容">
          <div class="detail-content">{{ detailData.content || '-' }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllCategories } from '@/api/category'
import {
  addContent,
  auditContent,
  deleteContent,
  getContentById,
  getContentList,
  likeContent,
  updateContent,
  uploadContentMedia
} from '@/api/content'

const IMAGE_MAX_SIZE_MB = 10
const VIDEO_MAX_SIZE_MB = 100
const LOCAL_MEDIA_PREFIX = '/api/media/'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增内容')
const detailVisible = ref(false)
const tableData = ref([])
const total = ref(0)
const detailData = ref(null)
const formRef = ref()
const coverImageInputMode = ref('url')
const videoInputMode = ref('url')
const categoryOptions = ref([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  status: null,
  contentType: null,
  categoryId: null
})

const form = reactive({
  id: null,
  title: '',
  contentType: 1,
  content: '',
  coverImage: '',
  videoUrl: '',
  categoryId: null
})

const categoryMap = computed(() => {
  const map = new Map()
  ;(categoryOptions.value || []).forEach((item) => {
    map.set(item.id, item.categoryName)
  })
  return map
})

const readField = (record, ...keys) => {
  for (const key of keys) {
    const value = record?.[key]
    if (value !== undefined && value !== null) {
      return value
    }
  }
  return null
}

const normalizeContentRecord = (record = {}) => ({
  ...record,
  id: readField(record, 'id'),
  title: readField(record, 'title') || '',
  contentType: readField(record, 'contentType', 'content_type'),
  content: readField(record, 'content') || '',
  coverImage: readField(record, 'coverImage', 'cover_image') || '',
  videoUrl: readField(record, 'videoUrl', 'video_url') || '',
  categoryId: readField(record, 'categoryId', 'category_id'),
  categoryName: readField(record, 'categoryName', 'category_name') || '',
  status: readField(record, 'status'),
  viewCount: readField(record, 'viewCount', 'view_count') ?? 0,
  likeCount: readField(record, 'likeCount', 'like_count') ?? 0,
  createTime: readField(record, 'createTime', 'create_time'),
  publishTime: readField(record, 'publishTime', 'publish_time')
})

const contentValidator = (_, value, callback) => {
  if (form.contentType !== 2 && !value?.trim()) {
    callback(new Error('请输入正文内容'))
    return
  }
  callback()
}

const videoValidator = (_, value, callback) => {
  if (form.contentType === 2 && !value?.trim()) {
    callback(new Error('视频类型必须填写视频地址'))
    return
  }
  callback()
}

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  contentType: [{ required: true, message: '请选择内容类型', trigger: 'change' }],
  content: [{ validator: contentValidator, trigger: 'blur' }],
  videoUrl: [{ validator: videoValidator, trigger: 'blur' }]
}

onMounted(async () => {
  await loadCategories()
  await getList()
})

watch(() => form.contentType, (value) => {
  if (value !== 2) {
    form.videoUrl = ''
    videoInputMode.value = 'url'
  }
  formRef.value?.clearValidate(['content', 'videoUrl'])
})

const loadCategories = async () => {
  const res = await getAllCategories()
  categoryOptions.value = res.data || []
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getContentList(queryParams)
    tableData.value = (res.data?.records || []).map(normalizeContentRecord)
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const formatDateTime = (value) => {
  if (!value) return '-'
  return String(value).replace('T', ' ')
}

const getCategoryLabel = (row = {}) => {
  const categoryName = readField(row, 'categoryName', 'category_name')
  const categoryId = readField(row, 'categoryId', 'category_id')
  return categoryName || categoryMap.value.get(categoryId) || '-'
}

const getStatusLabel = (status) => {
  const map = { 0: '待审核', 1: '已上线', 2: '已下架' }
  return map[status] || '未知'
}

const getStatusTagType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'info' }
  return map[status] || ''
}

const getTypeLabel = (type) => {
  const map = { 1: '文章', 2: '视频', 3: '漫画' }
  return map[type] || '未知'
}

const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

const resetQuery = () => {
  Object.assign(queryParams, {
    pageNum: 1,
    pageSize: 10,
    keyword: '',
    status: null,
    contentType: null,
    categoryId: null
  })
  getList()
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    title: '',
    contentType: 1,
    content: '',
    coverImage: '',
    videoUrl: '',
    categoryId: null
  })
  coverImageInputMode.value = 'url'
  videoInputMode.value = 'url'
  formRef.value?.clearValidate()
}

const getMediaInputMode = (value) => {
  return value?.startsWith(LOCAL_MEDIA_PREFIX) ? 'upload' : 'url'
}

const applyFormData = (data = {}) => {
  const normalized = normalizeContentRecord(data)
  Object.assign(form, {
    id: normalized.id ?? null,
    title: normalized.title || '',
    contentType: normalized.contentType ?? 1,
    content: normalized.content || '',
    coverImage: normalized.coverImage || '',
    videoUrl: normalized.videoUrl || '',
    categoryId: normalized.categoryId || null
  })

  coverImageInputMode.value = getMediaInputMode(form.coverImage)
  videoInputMode.value = getMediaInputMode(form.videoUrl)
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增内容'
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  const res = await getContentById(row.id)
  applyFormData(res.data || {})
  dialogTitle.value = '编辑内容'
  dialogVisible.value = true
}

const normalizeOptionalText = (value) => value?.trim() || ''

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const payload = {
      title: form.title.trim(),
      contentType: form.contentType,
      content: normalizeOptionalText(form.content),
      coverImage: normalizeOptionalText(form.coverImage),
      videoUrl: form.contentType === 2 ? normalizeOptionalText(form.videoUrl) : '',
      categoryId: form.categoryId
    }

    if (form.id) {
      await updateContent(form.id, payload)
      ElMessage.success('更新成功')
    } else {
      await addContent(payload)
      ElMessage.success('新增成功')
    }

    dialogVisible.value = false
    getList()
  } finally {
    submitLoading.value = false
  }
}

const handleDetail = async (row) => {
  const res = await getContentById(row.id)
  detailData.value = normalizeContentRecord(res.data || {})
  detailVisible.value = true
}

const handleAudit = (row, status) => {
  const action = status === 1 ? '审核通过并上线' : (row.status === 1 ? '下架' : '审核驳回')
  ElMessageBox.confirm(`确认执行“${action}”操作吗？`, '提示', { type: 'warning' }).then(async () => {
    await auditContent(row.id, status)
    ElMessage.success(`${action}成功`)
    getList()
  }).catch(() => {})
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除内容《${row.title}》吗？`, '提示', { type: 'warning' }).then(async () => {
    await deleteContent(row.id)
    ElMessage.success('删除成功')
    getList()
  }).catch(() => {})
}

const handleLike = async (row) => {
  await likeContent(row.id)
  ElMessage.success('点赞成功')
  getList()
}

const validateFileSize = (file, maxSizeMb, invalidTypeMessage, tooLargeMessage, expectedPrefix) => {
  const hasValidType = file.type
    ? file.type.startsWith(expectedPrefix)
    : true
  if (!hasValidType) {
    ElMessage.error(invalidTypeMessage)
    return false
  }

  const isWithinLimit = file.size / 1024 / 1024 < maxSizeMb
  if (!isWithinLimit) {
    ElMessage.error(tooLargeMessage)
    return false
  }

  return true
}

const beforeImageUpload = (file) => validateFileSize(
  file,
  IMAGE_MAX_SIZE_MB,
  '请选择图片文件后再上传',
  `图片大小不能超过 ${IMAGE_MAX_SIZE_MB}MB`,
  'image/'
)

const beforeVideoUpload = (file) => validateFileSize(
  file,
  VIDEO_MAX_SIZE_MB,
  '请选择视频文件后再上传',
  `视频大小不能超过 ${VIDEO_MAX_SIZE_MB}MB`,
  'video/'
)

const uploadMedia = async (option, targetField, type) => {
  const formData = new FormData()
  formData.append('file', option.file)

  try {
    const res = await uploadContentMedia(formData, type)
    form[targetField] = res.data.url

    if (type === 'image') {
      coverImageInputMode.value = 'upload'
    } else {
      videoInputMode.value = 'upload'
    }

    option.onSuccess?.(res.data)
    ElMessage.success(type === 'image' ? '图片上传成功' : '视频上传成功')
  } catch (error) {
    option.onError?.(error)
  }
}

const uploadCoverImage = (option) => uploadMedia(option, 'coverImage', 'image')
const uploadVideoFile = (option) => uploadMedia(option, 'videoUrl', 'video')

const uploadInlineImage = async (option) => {
  const formData = new FormData()
  formData.append('file', option.file)

  try {
    const res = await uploadContentMedia(formData, 'image')
    const imageHtml = `<p><img src="${res.data.url}" alt="" /></p>`
    form.content = form.content ? `${form.content}\n${imageHtml}` : imageHtml
    option.onSuccess?.(res.data)
    ElMessage.success('正文图片已插入内容')
  } catch (error) {
    option.onError?.(error)
  }
}
</script>

<style scoped>
.content-manage {
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

.media-editor {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.content-editor {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.media-mode-switch {
  width: fit-content;
}

.upload-panel {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
}

.inline-media-tools {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.media-preview {
  display: flex;
  gap: 16px;
  padding: 14px;
  border: 1px solid #e4e7ed;
  border-radius: 10px;
  background: #fafafa;
}

.image-preview {
  align-items: center;
}

.video-preview {
  align-items: flex-start;
}

.preview-image {
  width: 220px;
  height: 140px;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
  border: 1px solid #ebeef5;
  flex-shrink: 0;
}

.preview-video {
  width: 260px;
  max-width: 100%;
  border-radius: 8px;
  background: #000;
  flex-shrink: 0;
}

.media-preview-meta {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
  justify-content: space-between;
}

.media-url {
  font-size: 13px;
  line-height: 1.6;
  color: #606266;
  word-break: break-all;
}

.drawer-media-block {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.drawer-image {
  width: 100%;
  max-width: 320px;
  max-height: 220px;
  border-radius: 8px;
  background: #f5f7fa;
}

.drawer-video {
  width: 100%;
  max-width: 360px;
  border-radius: 8px;
  background: #000;
}

.detail-content {
  white-space: pre-wrap;
  line-height: 1.7;
}
</style>
