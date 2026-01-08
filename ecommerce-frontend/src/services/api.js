import axios from 'axios'

const api = axios.create({
    baseURL: '/api',
    headers: {
        'Content-Type': 'application/json'
    }
})

// Request interceptor for adding JWT token
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    (error) => {
        return Promise.reject(error)
    }
)

// Response interceptor for handling errors
api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            localStorage.removeItem('token')
            localStorage.removeItem('role')
            localStorage.removeItem('username')
            localStorage.removeItem('memberId')
            window.location.href = '/login'
        }
        return Promise.reject(error)
    }
)

// Auth APIs
export const authApi = {
    login: (data) => api.post('/auth/login', data),
    register: (data) => api.post('/auth/register', data)
}

// Product APIs
export const productApi = {
    getAll: () => api.get('/products'),
    getAvailable: () => api.get('/products/available'),
    add: (data) => api.post('/products', data)
}

// Order APIs
export const orderApi = {
    create: (data) => api.post('/orders', data),
    getMyOrders: () => api.get('/orders/my'),
    getAll: () => api.get('/orders/all'),
    getDetails: (orderId) => api.get(`/orders/${orderId}`),
    updateStatus: (orderId, status) => api.patch(`/orders/${orderId}/status`, { status })
}

export default api
