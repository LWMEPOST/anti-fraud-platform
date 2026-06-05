<template>
  <div class="dashboard">
    <el-row :gutter="16" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
            <el-icon :size="32"><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.userCount }}</div>
            <div class="stat-label">注册用户</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
            <el-icon :size="32"><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.contentCount }}</div>
            <div class="stat-label">反诈知识</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
            <el-icon :size="32"><EditPen /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.examCount }}</div>
            <div class="stat-label">测试次数</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)">
            <el-icon :size="32"><Warning /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.reportCount }}</div>
            <div class="stat-label">举报线索</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最新反诈内容</span>
              <el-button type="primary" link @click="$router.push('/content/published')">查看更多</el-button>
            </div>
          </template>

          <div v-if="latestContent.length === 0" class="empty-content">
            <el-empty description="暂无内容" />
          </div>

          <div v-else class="content-list">
            <div
              v-for="item in latestContent"
              :key="item.id"
              class="content-item"
              @click="goToDetail(item.id)"
            >
              <div class="item-cover">
                <el-image
                  v-if="item.coverImage"
                  :src="item.coverImage"
                  fit="cover"
                  style="width: 100%; height: 100%;"
                />
                <div v-else class="cover-placeholder">
                  <el-icon :size="32"><Document /></el-icon>
                </div>
              </div>
              <div class="item-info">
                <div class="item-header">
                  <el-tag :type="getTypeTagType(item.contentType)" size="small">
                    {{ item.contentTypeName }}
                  </el-tag>
                  <el-tag v-if="item.categoryName" type="info" size="small">
                    {{ item.categoryName }}
                  </el-tag>
                </div>
                <h4 class="item-title">{{ item.title }}</h4>
                <div class="item-meta">
                  <span><el-icon><View /></el-icon> {{ item.viewCount }}</span>
                  <span><el-icon><Clock /></el-icon> {{ formatTime(item.publishTime) }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card>
          <template #header><span>快捷操作</span></template>
          <div class="quick-actions">
            <el-button
              v-if="userStore.hasPermission('system:user')"
              type="primary"
              icon="Plus"
              @click="$router.push('/system/user')"
            >添加用户</el-button>
            <el-button
              v-if="userStore.hasPermission('content:knowledge')"
              type="success"
              icon="Document"
              @click="$router.push('/content/knowledge')"
            >发布内容</el-button>
            <el-button
              v-if="userStore.hasPermission('exam:online')"
              type="warning"
              icon="EditPen"
              @click="$router.push('/exam/online')"
            >在线测试</el-button>
            <el-button
              v-if="userStore.hasPermission('report:manage')"
              type="danger"
              icon="Warning"
              @click="$router.push('/report/manage')"
            >查看举报</el-button>
            <el-button
              v-if="userStore.hasPermission('report:mine') && !userStore.hasPermission('report:manage')"
              type="danger"
              icon="Warning"
              @click="$router.push('/report/mine')"
            >我的举报</el-button>
          </div>
        </el-card>

        <el-card style="margin-top: 16px;">
          <template #header><span>平台简介</span></template>
          <div class="intro-text">
            <p>高校反诈防骗宣传平台致力于提升师生的反诈识别能力和安全意识。</p>
            <p>平台围绕反诈内容、在线测试和举报线索三个模块，帮助日常宣传、学习和线索受理形成闭环。</p>
            <el-tag type="info" size="small">Spring Boot + Vue 3</el-tag>
            <el-tag type="success" size="small">MySQL 8.0</el-tag>
            <el-tag type="warning" size="small">Element Plus</el-tag>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getLatestContent } from '@/api/content'
import useUserStore from '@/store/modules/user'

const router = useRouter()
const userStore = useUserStore()

const stats = ref({
  userCount: 0,
  contentCount: 0,
  examCount: 0,
  reportCount: 0
})

const latestContent = ref([])

onMounted(() => {
  loadStats()
  loadLatestContent()
})

const loadStats = () => {
  stats.value = {
    userCount: 1234,
    contentCount: 567,
    examCount: 8901,
    reportCount: 89
  }
}

const loadLatestContent = async () => {
  try {
    const res = await getLatestContent(6)
    latestContent.value = res.data || []
  } catch (error) {
    console.error('加载最新内容失败', error)
  }
}

const goToDetail = (id) => {
  router.push(`/content/detail/${id}`)
}

const getTypeTagType = (type) => {
  switch (type) {
    case 1:
      return 'primary'
    case 2:
      return 'success'
    case 3:
      return 'warning'
    default:
      return 'info'
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getMonth() + 1}-${date.getDate()}`
}
</script>

<style scoped>
.stat-cards {
  margin-bottom: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-right: 16px;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.content-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.content-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  background-color: #f9fafb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.content-item:hover {
  background-color: #f0f2f5;
  transform: translateX(4px);
}

.item-cover {
  width: 80px;
  height: 60px;
  border-radius: 4px;
  overflow: hidden;
  flex-shrink: 0;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #e0e0e0;
  color: #909399;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.item-header {
  display: flex;
  gap: 6px;
  margin-bottom: 4px;
}

.item-title {
  margin: 0 0 4px 0;
  font-size: 14px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #909399;
}

.item-meta span {
  display: flex;
  align-items: center;
  gap: 2px;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.intro-text {
  line-height: 1.8;
  color: #606266;
}

.intro-text p {
  margin-bottom: 8px;
}

.intro-text .el-tag {
  margin-right: 6px;
}

.empty-content {
  padding: 40px 0;
}
</style>
