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
    (response) => response.data,
    (error) => {
        if (error.response?.status === 401) {
            localStorage.removeItem('token')
            localStorage.removeItem('role')
            localStorage.removeItem('username')
            localStorage.removeItem('memberId')
            window.location.href = '/login'
        }
        const message = error.response?.data?.message || error.message || 'Request failed'
        return Promise.reject({ message })
    }
)

// Auth APIs
export const login = (data) => api.post('/auth/login', data)
export const register = (data) => api.post('/auth/register', data)

// Product APIs
export const getAllProducts = () => api.get('/products')
export const getAvailableProducts = () => api.get('/products/available')
export const addProduct = (data) => api.post('/products', data)
export const updateProductStock = (productId, addQuantity) => api.patch(`/products/${productId}/stock`, { addQuantity })

// Order APIs
export const createOrder = (data) => api.post('/orders', data)
export const getMyOrders = () => api.get('/orders/my')
export const getAllOrders = () => api.get('/orders/all')
export const getOrderDetails = (orderId) => api.get(`/orders/${orderId}`)
export const updateOrderStatus = (orderId, status) => api.patch(`/orders/${orderId}/status`, { status })
export const payOrder = (orderId) => api.post(`/orders/${orderId}/pay`)

export default api
