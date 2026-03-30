import { createRouter, createWebHistory } from 'vue-router'


const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        // 新增：根路径 / 重定向到 FoodRecord 路由
        {
            path: '/',
            name: 'Home',
            component: () => import('@/views/Home.vue') // 你的首页组件
        },
        {
            path: '/food-record',
            name: 'FoodRecord',
            component: () => import('@/components/FoodRecord.vue')
        },
        {
            path: '/snack-analysis',
            name: 'SnackAnalysis',
            component: () => import('@/components/SnackAnalysis.vue')
        },
        {
            path: '/medication-reminder',
            name: 'MedicationReminder',
            component: () => import('@/components/MedicationReminder.vue')
        },
        {
            path: '/settings',
            name: 'Settings',
            component: () => import('@/components/Settings.vue')
        },
        {
            path:'/login',
            name:'Login',
            component:()=>import('@/components/Login.vue')
        }
    ]
})

router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token')
    const publicRoutes = ['/login']
    if (!token && !publicRoutes.includes(to.path)) {
        next('/login')
    } else if (token && to.path === '/login') {
        next('/')
    } else {
        next()
    }
})

export default router
