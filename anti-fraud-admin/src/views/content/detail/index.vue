<template>
  <div class="content-detail">
    <el-card v-loading="loading">
      <template #header>
        <div class="detail-header">
          <div class="header-left">
            <h2>{{ content.title }}</h2>
            <div class="meta">
              <el-tag :type="getTypeTagType(content.contentType)" size="small">
                {{ content.contentTypeName }}
              </el-tag>
              <el-tag v-if="content.categoryName" type="info" size="small">
                {{ content.categoryName }}
              </el-tag>
              <span class="info-item">
                <el-icon><View /></el-icon> {{ content.viewCount }} 次浏览
              </span>
              <span class="info-item">
                <el-icon><Star /></el-icon> {{ content.likeCount }} 次点赞
              </span>
              <span class="info-item" v-if="content.publishTime">
                <el-icon><Clock /></el-icon> {{ formatTime(content.publishTime) }}
              </span>
            </div>
          </div>
          <div class="header-right">
            <el-button type="primary" icon="Thumb" @click="handleLike" :disabled="liked">
              {{ liked ? '已点赞' : '点赞' }}
            </el-button>
            <el-button icon="Back" @click="router.back()">返回</el-button>
          </div>
        </div>
      </template>

      <div v-if="content.contentType === 2" class="video-container">
        <div v-if="content.videoUrl" class="media-frame video-frame">
          <video class="detail-video" :src="content.videoUrl" controls preload="metadata">
            当前浏览器不支持 video 标签。
          </video>
        </div>
        <div v-else class="empty-video">
          <el-icon :size="64"><VideoPlay /></el-icon>
          <p>暂无视频</p>
        </div>

        <div v-if="content.content" class="video-description">
          <h3>视频简介</h3>
          <div class="article-content" v-html="content.content"></div>
        </div>
      </div>

      <div v-else-if="content.contentType === 3" class="comic-container">
        <div v-if="content.coverImage" class="media-frame image-frame">
          <el-image
            :src="content.coverImage"
            :preview-src-list="[content.coverImage]"
            fit="contain"
            class="detail-image"
            :initial-index="0"
          >
            <template #error>
              <div class="image-error">
                <el-icon :size="48"><Picture /></el-icon>
                <p>图片加载失败</p>
              </div>
            </template>
          </el-image>
        </div>
        <div v-else class="empty-image">
          <el-icon :size="64"><Picture /></el-icon>
          <p>暂无封面</p>
        </div>

        <div v-if="content.content" class="comic-description">
          <h3>内容说明</h3>
          <div class="article-content" v-html="content.content"></div>
        </div>
      </div>

      <div v-else class="article-container">
        <div v-if="content.coverImage" class="cover-image">
          <div class="media-frame image-frame">
            <el-image
              :src="content.coverImage"
              fit="contain"
              class="detail-image"
              :preview-src-list="[content.coverImage]"
              :initial-index="0"
            >
              <template #error>
                <div class="image-error">
                  <el-icon :size="48"><Picture /></el-icon>
                  <p>封面图片加载失败</p>
                </div>
              </template>
            </el-image>
          </div>
        </div>
        <div class="article-content" v-html="content.content"></div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getContentById, likeContent } from '@/api/content'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const content = ref({})
const liked = ref(false)

onMounted(() => {
  loadContent()
})

const loadContent = async () => {
  loading.value = true
  try {
    const res = await getContentById(route.params.id)
    content.value = res.data || {}
  } catch (error) {
    console.error('加载内容失败', error)
    ElMessage.error('加载内容失败')
  } finally {
    loading.value = false
  }
}

const handleLike = async () => {
  try {
    await likeContent(route.params.id)
    liked.value = true
    content.value.likeCount = (content.value.likeCount || 0) + 1
    ElMessage.success('点赞成功')
  } catch (error) {
    console.error('点赞失败', error)
  }
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
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.content-detail {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
}

.header-left {
  flex: 1;
}

.detail-header h2 {
  margin: 0 0 12px 0;
  font-size: 24px;
  color: #303133;
  line-height: 1.4;
}

.meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #909399;
}

.header-right {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.article-container,
.video-container,
.comic-container {
  padding: 20px 0;
}

.cover-image,
.video-description,
.comic-description {
  margin-top: 24px;
}

.media-frame {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, #f8fafc 0%, #eef2f7 100%);
  border-radius: 14px;
  overflow: hidden;
  padding: 16px;
}

.image-frame {
  min-height: 320px;
}

.video-frame {
  min-height: 320px;
}

.detail-image {
  width: 100%;
  max-height: 540px;
  display: block;
}

.detail-video {
  width: 100%;
  max-width: 960px;
  max-height: 540px;
  border-radius: 10px;
  background: #000;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.18);
}

.article-content {
  font-size: 16px;
  color: #606266;
  line-height: 2;
  word-wrap: break-word;
  overflow-wrap: break-word;
}

.article-content :deep(img) {
  display: block;
  width: auto !important;
  max-width: min(100%, 920px) !important;
  height: auto !important;
  max-height: 560px;
  object-fit: contain;
  border-radius: 8px;
  margin: 18px auto;
}

.article-content :deep(video) {
  display: block;
  width: min(100%, 920px) !important;
  max-width: 100% !important;
  height: auto !important;
  max-height: 560px;
  border-radius: 10px;
  margin: 20px auto;
  background: #000;
}

.article-content :deep(p) {
  margin: 16px 0;
  line-height: 1.8;
}

.video-container,
.comic-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.empty-video,
.empty-image,
.image-error {
  padding: 60px;
  color: #909399;
  background-color: #f5f7fa;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

@media (max-width: 768px) {
  .content-detail {
    padding: 12px;
  }

  .detail-header {
    flex-direction: column;
  }

  .header-right {
    width: 100%;
    justify-content: flex-start;
  }

  .media-frame {
    padding: 10px;
  }

  .image-frame,
  .video-frame {
    min-height: 220px;
  }
}
</style>
