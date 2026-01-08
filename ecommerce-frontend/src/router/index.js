import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import ProductManagement from '../views/ProductManagement.vue'
import OrderManagement from '../views/OrderManagement.vue'

const routes = [
    {
        path: '/',
        redirect: '/login'
    },
    {
        path: '/login',
        name: 'Login',
        component: Login
    },
    {
        path: '/register',
        name: 'Register',
        component: Register
    },
    {
        path: '/products',
        name: 'ProductManagement',
        component: ProductManagement,
        meta: { requiresAuth: true, role: 'ADMIN' }
    },
    {
        path: '/orders',
        name: 'OrderManagement',
        component: OrderManagement,
        meta: { requiresAuth: true }
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// Navigation guard
router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token')
    const role = localStorage.getItem('role')

    if (to.meta.requiresAuth && !token) {
        next('/login')
    } else if (to.meta.role && role !== to.meta.role) {
        // If page requires specific role and user doesn't have it, redirect
        next('/orders')
    } else {
        next()
    }
})

export default router
