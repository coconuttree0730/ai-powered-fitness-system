import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/public/Home.vue'),
    meta: { title: '首页', public: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/public/Register.vue'),
    meta: { title: '注册', public: true }
  },
  {
    path: '/courses',
    name: 'Courses',
    component: () => import('@/views/public/Courses.vue'),
    meta: { title: '课程列表', public: true }
  },
  {
    path: '/courses/:id',
    name: 'CourseDetail',
    component: () => import('@/views/public/CourseDetail.vue'),
    meta: { title: '课程详情', public: true }
  },
  {
    path: '/coaches/:id',
    name: 'CoachDetail',
    component: () => import('@/views/public/CoachDetail.vue'),
    meta: { title: '教练详情', public: true }
  },
  {
    path: '/equipments',
    name: 'Equipments',
    component: () => import('@/views/public/Equipments.vue'),
    meta: { title: '器械列表', public: true }
  },
  {
    path: '/equipments/:id',
    name: 'EquipmentDetail',
    component: () => import('@/views/public/EquipmentDetail.vue'),
    meta: { title: '器械详情', public: true }
  },
  {
    path: '/member',
    component: () => import('@/layouts/MemberLayout.vue'),
    meta: { requiresAuth: true, roles: ['MEMBER'] },
    children: [
      {
        path: '',
        redirect: '/member/cards'
      },
      {
        path: 'profile',
        name: 'MemberProfile',
        component: () => import('@/views/member/Profile.vue'),
        meta: { title: '个人信息' }
      },
      {
        path: 'bookings',
        name: 'MyBookings',
        component: () => import('@/views/member/Bookings.vue'),
        meta: { title: '我的预约' }
      },
      {
        path: 'plans',
        name: 'MyPlans',
        component: () => import('@/views/member/Plans.vue'),
        meta: { title: '健身计划' }
      },
      {
        path: 'equipment',
        name: 'MemberEquipment',
        component: () => import('@/views/member/Equipment.vue'),
        meta: { title: '器材列表' }
      },
      {
        path: 'repairs',
        name: 'MyRepairs',
        component: () => import('@/views/member/Repairs.vue'),
        meta: { title: '我的报修' }
      },
      {
        path: 'cards',
        name: 'MemberCards',
        component: () => import('@/views/member/Cards.vue'),
        meta: { title: '在线购卡' }
      },
      {
        path: 'membership-cards',
        name: 'MembershipCards',
        component: () => import('@/views/member/MembershipCards.vue'),
        meta: { title: '会员卡中心' }
      },
      {
        path: 'membership/:id',
        name: 'MembershipCardDetail',
        component: () => import('@/views/member/MembershipCardDetail.vue'),
        meta: { title: '会员卡详情' }
      },
      {
        path: 'coach',
        name: 'MemberCoach',
        component: () => import('@/views/member/Coach.vue'),
        meta: { title: '我的教练' }
      },
      {
        path: 'assistant',
        name: 'MemberAssistant',
        component: () => import('@/views/member/Assistant.vue'),
        meta: { title: '健小助' }
      },
      {
        path: 'store',
        name: 'MemberStore',
        component: () => import('@/views/member/Store.vue'),
        meta: { title: '购物中心' }
      }
    ]
  },
  {
    path: '/coach',
    component: () => import('@/layouts/CoachLayout.vue'),
    meta: { requiresAuth: true, roles: ['COACH'] },
    children: [
      {
        path: '',
        name: 'CoachHome',
        component: () => import('@/views/coach/Home.vue'),
        meta: { title: '教练中心' }
      },
      {
        path: 'profile',
        name: 'CoachProfile',
        component: () => import('@/views/coach/Profile.vue'),
        meta: { title: '个人信息' }
      },
      {
        path: 'courses',
        name: 'CoachCourses',
        component: () => import('@/views/coach/Courses.vue'),
        meta: { title: '我的课程' }
      },
      {
        path: 'packages',
        name: 'CoachPackages',
        component: () => import('@/views/coach/Packages.vue'),
        meta: { title: '我的套餐' }
      },
      {
        path: 'students',
        name: 'CoachStudents',
        component: () => import('@/views/coach/Students.vue'),
        meta: { title: '我的学员' }
      },
      {
        path: 'schedule',
        name: 'CoachSchedule',
        component: () => import('@/views/coach/Schedule.vue'),
        meta: { title: '课程日程' }
      }
    ]
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN'] },
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('@/views/admin/Users.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'courses',
        name: 'CourseManagement',
        component: () => import('@/views/admin/Courses.vue'),
        meta: { title: '公开课管理' }
      },
      {
        path: 'sessions',
        name: 'SessionManagement',
        component: () => import('@/views/admin/Sessions.vue'),
        meta: { title: '排期管理' }
      },
      {
        path: 'video-courses',
        name: 'VideoCourseManagement',
        component: () => import('@/views/admin/VideoCourses.vue'),
        meta: { title: '视频课程管理' }
      },
      {
        path: 'membership-cards',
        name: 'MembershipCardManagement',
        component: () => import('@/views/admin/MembershipCards.vue'),
        meta: { title: '会员卡管理' }
      },
      {
        path: 'content-management',
        name: 'ContentManagement',
        component: () => import('@/views/admin/ContentManagement.vue'),
        meta: { title: '内容管理' }
      },
      {
        path: 'order-management',
        name: 'OrderManagement',
        component: () => import('@/views/admin/OrderManagement.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'products',
        name: 'ProductManagement',
        component: () => import('@/views/admin/Products.vue'),
        meta: { title: '商品管理' }
      },
      {
        path: 'knowledge-base',
        name: 'KnowledgeBaseManagement',
        component: () => import('@/views/admin/KnowledgeBase.vue'),
        meta: { title: '知识库管理' }
      },
      {
        path: 'knowledge-categories',
        name: 'KnowledgeCategoryManagement',
        component: () => import('@/views/admin/KnowledgeCategory.vue'),
        meta: { title: '知识库分类管理' }
      },
      {
        path: 'dict-management',
        name: 'DictManagement',
        component: () => import('@/views/admin/DictManagement.vue'),
        meta: { title: '数据字典管理' }
      },
      {
        path: 'equipment',
        name: 'EquipmentManagement',
        component: () => import('@/views/admin/Equipment.vue'),
        meta: { title: '器材管理' }
      },
      {
        path: 'repairs',
        name: 'RepairManagement',
        component: () => import('@/views/admin/Repairs.vue'),
        meta: { title: '报修管理' }
      },
      {
        path: 'analysis',
        name: 'DataAnalysis',
        component: () => import('@/views/admin/Analysis.vue'),
        meta: { title: '数据分析' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/public/NotFound.vue'),
    meta: { title: '页面不存在', public: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from) => {
  document.title = to.meta.title ? `wzp·APFS |  ${to.meta.title} ` : '智能健身房'

  const authStore = useAuthStore()

  // 公开页面直接放行
  if (to.meta.public) {
    return true
  }

  // 需要认证的页面
  if (to.meta.requiresAuth) {
    // 检查是否已登录
    if (!authStore.isLoggedIn) {
      return { path: '/', query: { redirect: to.fullPath } }
    }

    // 检查角色权限
    if (to.meta.roles && to.meta.roles.length > 0) {
      const hasRole = to.meta.roles.some(role => authStore.userRoles.includes(role))
      if (!hasRole) {
        return { name: 'Home' }
      }
    }
  }

  return true
})

export default router
