import { createRouter, createWebHistory } from 'vue-router'
import useUserStore from '@/store/modules/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/register/index.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      },
      {
        path: 'system/user',
        name: 'UserManage',
        component: () => import('@/views/system/user/index.vue'),
        meta: { title: '用户管理', icon: 'User', permission: 'system:user' }
      },
      {
        path: 'system/role',
        name: 'RoleManage',
        component: () => import('@/views/system/role/index.vue'),
        meta: { title: '角色管理', icon: 'UserFilled', permission: 'system:role' }
      },
      {
        path: 'content/published',
        name: 'ContentPublished',
        component: () => import('@/views/content/published/index.vue'),
        meta: { title: '已发布内容', icon: 'Reading' }
      },
      {
        path: 'content/detail/:id',
        name: 'ContentDetail',
        component: () => import('@/views/content/detail/index.vue'),
        meta: { title: '内容详情', icon: 'Document' }
      },
      {
        path: 'content/knowledge',
        name: 'ContentManage',
        component: () => import('@/views/content/knowledge/index.vue'),
        meta: { title: '知识库管理', icon: 'Document', permission: 'content:knowledge' }
      },
      {
        path: 'content/question',
        name: 'QuestionManage',
        component: () => import('@/views/content/question/index.vue'),
        meta: { title: '题库管理', icon: 'Collection', permission: 'content:question' }
      },
      {
        path: 'exam/online',
        name: 'OnlineExam',
        component: () => import('@/views/exam/online/index.vue'),
        meta: { title: '在线测试', icon: 'EditPen', permission: 'exam:online' }
      },
      {
        path: 'exam/record',
        name: 'ExamRecord',
        component: () => import('@/views/exam/record/index.vue'),
        meta: { title: '成绩查询', icon: 'DataLine', permission: 'exam:record' }
      },
      {
        path: 'report/mine',
        name: 'ReportMine',
        component: () => import('@/views/report/mine/index.vue'),
        meta: { title: '我的举报', icon: 'Message', permission: 'report:mine' }
      },
      {
        path: 'report/manage',
        name: 'ReportManage',
        component: () => import('@/views/report/manage/index.vue'),
        meta: { title: '举报管理', icon: 'Warning', permission: 'report:manage' }
      },
      {
        path: 'report/statistics',
        name: 'ReportStatistics',
        component: () => import('@/views/report/statistics/index.vue'),
        meta: { title: '举报统计', icon: 'PieChart', permission: 'report:statistics' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

const publicPaths = ['/login', '/register']

router.beforeEach(async (to) => {
  const userStore = useUserStore()
  const isPublicPage = publicPaths.includes(to.path)

  if (!userStore.token) {
    return isPublicPage ? true : '/login'
  }

  if (isPublicPage) {
    return '/'
  }

  if (!userStore.permissionsLoaded) {
    try {
      await userStore.fetchPermissions()
    } catch (error) {
      userStore.logout()
      return false
    }
  }

  const requiredPermission = to.meta?.permission
  if (requiredPermission && !userStore.hasPermission(requiredPermission)) {
    return '/dashboard'
  }

  return true
})

export default router
