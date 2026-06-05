<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="aside">
      <div class="logo">
        <span v-if="!isCollapse">反诈平台</span>
        <span v-else>反诈</span>
      </div>

      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>

        <el-sub-menu index="system" v-if="userStore.hasAnyPermission(['system:user', 'system:role'])">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item v-if="userStore.hasPermission('system:user')" index="/system/user">
            <el-icon><User /></el-icon>
            用户管理
          </el-menu-item>
          <el-menu-item v-if="userStore.hasPermission('system:role')" index="/system/role">
            <el-icon><UserFilled /></el-icon>
            角色管理
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="content">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>内容管理</span>
          </template>
          <el-menu-item index="/content/published">
            <el-icon><Reading /></el-icon>
            已发布内容
          </el-menu-item>
          <el-menu-item v-if="userStore.hasPermission('content:knowledge')" index="/content/knowledge">
            <el-icon><Reading /></el-icon>
            知识库管理
          </el-menu-item>
          <el-menu-item v-if="userStore.hasPermission('content:question')" index="/content/question">
            <el-icon><Collection /></el-icon>
            题库管理
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="exam" v-if="userStore.hasAnyPermission(['exam:online', 'exam:record'])">
          <template #title>
            <el-icon><EditPen /></el-icon>
            <span>测试中心</span>
          </template>
          <el-menu-item v-if="userStore.hasPermission('exam:online')" index="/exam/online">
            <el-icon><Notebook /></el-icon>
            在线测试
          </el-menu-item>
          <el-menu-item v-if="userStore.hasPermission('exam:record')" index="/exam/record">
            <el-icon><DataLine /></el-icon>
            成绩查询
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu
          index="report"
          v-if="userStore.hasAnyPermission(['report:mine', 'report:manage', 'report:statistics'])"
        >
          <template #title>
            <el-icon><Warning /></el-icon>
            <span>举报中心</span>
          </template>
          <el-menu-item v-if="userStore.hasPermission('report:mine')" index="/report/mine">
            <el-icon><Message /></el-icon>
            我的举报
          </el-menu-item>
          <el-menu-item v-if="userStore.hasPermission('report:manage')" index="/report/manage">
            <el-icon><List /></el-icon>
            举报管理
          </el-menu-item>
          <el-menu-item v-if="userStore.hasPermission('report:statistics')" index="/report/statistics">
            <el-icon><PieChart /></el-icon>
            举报统计
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>

          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ $route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" icon="UserFilled" />
              <span class="username">{{ userStore.realName || userStore.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import useUserStore from '@/store/modules/user'

const route = useRoute()
const userStore = useUserStore()

const isCollapse = ref(false)
const activeMenu = computed(() => route.path)

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.aside {
  background-color: #304156;
  transition: width 0.3s;
  overflow-x: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
  font-weight: bold;
  background-color: #263445;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  padding: 0 20px;
}

.left {
  display: flex;
  align-items: center;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  margin-right: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.username {
  font-size: 14px;
}

.main {
  background-color: #f0f2f5;
  padding: 20px;
}

:deep(.el-menu) {
  border-right: none;
}
</style>
