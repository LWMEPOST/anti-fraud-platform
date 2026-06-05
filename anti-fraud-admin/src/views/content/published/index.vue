<template>
  <div class="content-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>反诈知识库</span>
          <div class="filters">
            <el-select v-model="categoryId" placeholder="全部分类" clearable @change="handleQuery">
              <el-option v-for="cat in categories" :key="cat.id" :label="cat.categoryName" :value="cat.id" />
            </el-select>
            <el-button type="primary" icon="Refresh" @click="handleQuery">刷新</el-button>
          </div>
        </div>
      </template>

      <el-empty v-if="tableData.length === 0 && !loading" description="暂无内容" />

      <div v-else class="content-grid">
        <el-card 
          v-for="item in tableData" 
          :key="item.id" 
          class="content-card"
          shadow="hover"
          @click="goToDetail(item.id)"
        >
          <div class="card-cover">
            <el-image 
              v-if="item.coverImage" 
              :src="item.coverImage" 
              fit="contain" 
              style="width: 100%; height: 200px; display: block; padding: 8px; box-sizing: border-box;"
              :preview-src-list="item.coverImage ? [item.coverImage] : []"
              :initial-index="0"
            >
              <template #error>
                <div class="image-error">
                  <el-icon :size="32"><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div v-else class="cover-placeholder">
              <el-icon :size="48"><Document /></el-icon>
            </div>
            <div class="card-type-badge">
              <el-tag :type="getTypeTagType(item.contentType)" size="small">
                {{ item.contentTypeName }}
              </el-tag>
            </div>
          </div>
          
          <div class="card-body">
            <h3 class="card-title">{{ item.title }}</h3>
            
            <div class="card-meta">
              <el-tag v-if="item.categoryName" type="info" size="small">{{ item.categoryName }}</el-tag>
              <span class="meta-info">
                <el-icon><View /></el-icon> {{ item.viewCount }}
              </span>
              <span class="meta-info">
                <el-icon><Clock /></el-icon> {{ formatTime(item.publishTime) }}
              </span>
            </div>
          </div>
        </el-card>
      </div>

      <el-pagination
        v-if="total > 0"
        class="pagination"
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[6, 12, 24]"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="getList"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPublishedContent } from '@/api/content'
import { getCategoryList } from '@/api/category'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const categories = ref([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 12,
  categoryId: null
})

const categoryId = ref(null)

onMounted(() => {
  getList()
  loadCategories()
})

const getList = async () => {
  loading.value = true
  try {
    const res = await getPublishedContent(queryParams)
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('加载内容失败', error)
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  try {
    const res = await getCategoryList()
    categories.value = res.data || []
  } catch (error) {
    console.error('加载分类失败', error)
  }
}

const handleQuery = () => {
  queryParams.categoryId = categoryId.value
  queryParams.pageNum = 1
  getList()
}

const goToDetail = (id) => {
  router.push(`/content/detail/${id}`)
}

const getTypeTagType = (type) => {
  switch (type) {
    case 1: return 'primary'
    case 2: return 'success'
    case 3: return 'warning'
    default: return 'info'
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getMonth() + 1}-${date.getDate()}`
}
</script>

<style scoped>
.content-list {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filters {
  display: flex;
  gap: 12px;
}

.content-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.content-card {
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  overflow: hidden;
}

.content-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.card-cover {
  position: relative;
  height: 200px;
  overflow: hidden;
  border-radius: 4px;
  margin-bottom: 12px;
  background-color: #f5f7fa;
}

.card-cover .el-image {
  width: 100%;
  height: 200px;
  display: block;
}

.cover-placeholder,
.image-error {
  width: 100%;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  color: #909399;
}

.card-type-badge {
  position: absolute;
  top: 8px;
  right: 8px;
}

.card-body {
  padding: 0 4px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.4;
  min-height: 22px;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #909399;
  flex-wrap: wrap;
}

.meta-info {
  display: flex;
  align-items: center;
  gap: 4px;
}

.pagination {
  margin-top: 20px;
  justify-content: center;
}
</style>
