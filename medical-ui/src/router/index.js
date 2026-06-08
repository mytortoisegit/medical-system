import { createRouter, createWebHashHistory } from 'vue-router'
import { getToken } from '@/utils/auth'

/** 静态路由 */
const constantRoutes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layout/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '工作台', icon: 'Odometer' }
      },
      {
        path: 'medicine',
        name: 'Medicine',
        component: () => import('@/views/medicine/index.vue'),
        meta: { title: '药品管理', icon: 'FirstAidKit' }
      },
      {
        path: 'medicine/form',
        name: 'MedicineForm',
        component: () => import('@/views/medicine/form.vue'),
        meta: { title: '药品编辑', hidden: true }
      },
      {
        path: 'stock-alert',
        name: 'StockAlert',
        component: () => import('@/views/medicine/alert.vue'),
        meta: { title: '库存预警', icon: 'Warning' }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/user/index.vue'),
        meta: { title: '用户管理', icon: 'User' }
      }
    ]
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404' }
  },
  { path: '/:pathMatch(.*)*', redirect: '/404' }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes: constantRoutes,
  scrollBehavior: () => ({ top: 0 })
})

/** 路由守卫 — Token 校验 */
router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 中医药品管理系统` : '中医药品管理系统'

  const token = getToken()
  if (to.path === '/login') {
    token ? next('/dashboard') : next()   // 已登录跳工作台
  } else {
    token ? next() : next(`/login?redirect=${to.path}`)  // 未登录跳登录页
  }
})

export default router
