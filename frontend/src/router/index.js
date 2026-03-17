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
    path: '/login',
    name: 'Login',
    component: () => import('@/views/public/Login.vue'),
    meta: { title: '登录', public: true }
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
    path: '/member',
    component: () => import('@/layouts/MemberLayout.vue'),
    meta: { requiresAuth: true, roles: ['MEMBER'] },
    children: [
      {
        path: '',
        name: 'MemberHome',
        component: () => import('@/views/member/Home.vue'),
        meta: { title: '会员中心' }
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
        path: 'students',
        name: 'CoachStudents',
        component: () => import('@/views/coach/Students.vue'),
        meta: { title: '我的学员' }
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
        meta: { title: '课程管理' }
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

router.beforeEach(async (to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 智能健身房` : '智能健身房'
  
  const authStore = useAuthStore()
  
  if (to.meta.public) {
    next()
    return
  }
  
  if (to.meta.requiresAuth) {
    if (!authStore.isLoggedIn) {
      next({ name: 'Login', query: { redirect: to.fullPath } })
      return
    }
    
    if (to.meta.roles && to.meta.roles.length > 0) {
      const hasRole = to.meta.roles.some(role => authStore.userRoles.includes(role))
      if (!hasRole) {
        next({ name: 'Home' })
        return
      }
    }
  }
  
  next()
})

export default router
