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

    // If user is logged in and tries to access login/register, redirect to appropriate page
    if ((to.path === '/login' || to.path === '/register' || to.path === '/') && token) {
        if (role === 'ADMIN') {
            next('/products')
        } else {
            next('/orders')
        }
        return
    }

    // If page requires auth and user is not logged in
    if (to.meta.requiresAuth && !token) {
        next('/login')
        return
    }

    // If page requires specific role and user doesn't have it
    if (to.meta.role && role !== to.meta.role) {
        next('/orders')
        return
    }

    next()
})

export default router
