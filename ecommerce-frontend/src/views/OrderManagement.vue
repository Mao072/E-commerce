<template>
  <div class="container">
    <!-- Create Order Section (for Users) -->
    <div class="card" v-if="!isAdmin">
      <div class="page-title">
        <h2>🛍️ 建立訂單</h2>
      </div>

      <div v-if="error" class="alert alert-error">{{ error }}</div>
      <div v-if="success" class="alert alert-success">{{ success }}</div>

      <div v-if="loadingProducts" class="loading">
        <div class="spinner"></div>
      </div>

      <div v-else-if="availableProducts.length === 0" class="empty-state">
        <h3>目前沒有可購買的商品</h3>
        <p>請稍後再回來看看</p>
      </div>

      <div v-else>
        <div class="grid grid-3">
          <div 
            v-for="product in availableProducts" 
            :key="product.productId"
            class="product-card"
            :class="{ selected: isSelected(product.productId) }"
          >
            <h3>{{ product.productName }}</h3>
            <div class="price">NT$ {{ formatPrice(product.price) }}</div>
            <div class="stock">庫存: {{ product.quantity }}</div>
            
            <div class="quantity-input" style="margin-top: 16px;">
              <button 
                @click="decreaseQuantity(product)"
                :disabled="!isSelected(product.productId)"
              >−</button>
              <input 
                type="number" 
                :value="getQuantity(product.productId)" 
                @change="updateQuantity(product, $event)"
                min="0"
                :max="product.quantity"
              />
              <button 
                @click="increaseQuantity(product)"
                :disabled="getQuantity(product.productId) >= product.quantity"
              >+</button>
            </div>
          </div>
        </div>

        <div class="cart-summary" v-if="cartItems.length > 0">
          <h3>📝 訂單摘要</h3>
          <div v-for="item in cartItems" :key="item.productId" style="margin-bottom: 8px;">
            {{ item.productName }} x {{ item.quantity }} = NT$ {{ formatPrice(item.subtotal) }}
          </div>
          <hr style="margin: 16px 0; border-color: rgba(255,255,255,0.3);" />
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <div>
              <div>總金額</div>
              <div class="cart-total">NT$ {{ formatPrice(totalPrice) }}</div>
            </div>
            <button class="btn btn-success" @click="handleCreateOrder" :disabled="ordering">
              {{ ordering ? '建立中...' : '建立訂單' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Orders List Section -->
    <div class="card">
      <div class="page-title">
        <h2>📋 {{ isAdmin ? '所有訂單' : '我的訂單' }}</h2>
        <button class="btn btn-outline" @click="fetchOrders">🔄 重新整理</button>
      </div>

      <div v-if="loadingOrders" class="loading">
        <div class="spinner"></div>
      </div>

      <div v-else-if="orders.length === 0" class="empty-state">
        <h3>尚無訂單</h3>
        <p>{{ isAdmin ? '目前沒有任何訂單' : '開始購物建立您的第一筆訂單吧！' }}</p>
      </div>

      <div v-else class="table-container">
        <table>
          <thead>
            <tr>
              <th>訂單編號</th>
              <th v-if="isAdmin">會員編號</th>
              <th>訂單金額</th>
              <th>付款狀態</th>
              <th>訂單狀態</th>
              <th>建立時間</th>
              <th v-if="isAdmin">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="order in orders" :key="order.orderId">
              <td>{{ order.orderId }}</td>
              <td v-if="isAdmin">{{ order.memberId }}</td>
              <td class="price">NT$ {{ formatPrice(order.totalPrice) }}</td>
              <td>
                <span :class="getPayStatusClass(order.payStatus)">
                  {{ getPayStatusText(order.payStatus) }}
                </span>
              </td>
              <td>
                <span :class="getOrderStatusClass(order.orderStatus)">
                  {{ getOrderStatusText(order.orderStatus) }}
                </span>
              </td>
              <td>{{ formatDate(order.createdAt) }}</td>
              <td v-if="isAdmin">
                <select 
                  class="form-control" 
                  style="width: auto; padding: 8px;"
                  :value="order.orderStatus"
                  @change="handleUpdateStatus(order.orderId, $event)"
                >
                  <option value="0">備貨中</option>
                  <option value="1">運送中</option>
                  <option value="2">已到達</option>
                  <option value="3">已取貨</option>
                  <option value="-1">已取消</option>
                </select>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { productApi, orderApi } from '../services/api'

const isAdmin = computed(() => localStorage.getItem('role') === 'ADMIN')

// Products
const availableProducts = ref([])
const loadingProducts = ref(true)
const cart = reactive({})

// Orders
const orders = ref([])
const loadingOrders = ref(true)

// State
const error = ref('')
const success = ref('')
const ordering = ref(false)

// Computed
const cartItems = computed(() => {
  return Object.entries(cart)
    .filter(([_, qty]) => qty > 0)
    .map(([productId, quantity]) => {
      const product = availableProducts.value.find(p => p.productId === productId)
      return {
        productId,
        productName: product?.productName || '',
        quantity,
        standPrice: product?.price || 0,
        subtotal: (product?.price || 0) * quantity
      }
    })
})

const totalPrice = computed(() => {
  return cartItems.value.reduce((sum, item) => sum + item.subtotal, 0)
})

// Methods
const isSelected = (productId) => cart[productId] > 0

const getQuantity = (productId) => cart[productId] || 0

const updateQuantity = (product, event) => {
  const qty = parseInt(event.target.value) || 0
  cart[product.productId] = Math.min(Math.max(qty, 0), product.quantity)
}

const increaseQuantity = (product) => {
  if (!cart[product.productId]) cart[product.productId] = 0
  if (cart[product.productId] < product.quantity) {
    cart[product.productId]++
  }
}

const decreaseQuantity = (product) => {
  if (cart[product.productId] > 0) {
    cart[product.productId]--
  }
}

const fetchProducts = async () => {
  loadingProducts.value = true
  try {
    const response = await productApi.getAvailable()
    availableProducts.value = response.data.data || []
  } catch (err) {
    error.value = err.response?.data?.message || '載入商品失敗'
  } finally {
    loadingProducts.value = false
  }
}

const fetchOrders = async () => {
  loadingOrders.value = true
  try {
    const response = isAdmin.value 
      ? await orderApi.getAll()
      : await orderApi.getMyOrders()
    orders.value = response.data.data || []
  } catch (err) {
    console.error('載入訂單失敗', err)
  } finally {
    loadingOrders.value = false
  }
}

const handleCreateOrder = async () => {
  ordering.value = true
  error.value = ''
  success.value = ''
  
  const orderItems = cartItems.value.map(item => ({
    productId: item.productId,
    quantity: item.quantity
  }))
  
  try {
    await orderApi.create({
      memberId: localStorage.getItem('memberId'),
      orderItems
    })
    success.value = '訂單建立成功！'
    
    // Clear cart
    Object.keys(cart).forEach(key => cart[key] = 0)
    
    // Refresh data
    fetchProducts()
    fetchOrders()
  } catch (err) {
    error.value = err.response?.data?.message || '建立訂單失敗'
  } finally {
    ordering.value = false
  }
}

const handleUpdateStatus = async (orderId, event) => {
  const status = parseInt(event.target.value)
  try {
    await orderApi.updateStatus(orderId, status)
    fetchOrders()
  } catch (err) {
    error.value = err.response?.data?.message || '更新狀態失敗'
  }
}

const getPayStatusText = (status) => {
  return status === 1 ? '已付款' : '未付款'
}

const getPayStatusClass = (status) => {
  return status === 1 ? 'status-badge status-delivered' : 'status-badge status-pending'
}

const getOrderStatusText = (status) => {
  const map = { 0: '備貨中', 1: '運送中', 2: '已到達', 3: '已取貨', '-1': '已取消' }
  return map[status] || '未知'
}

const getOrderStatusClass = (status) => {
  const map = {
    0: 'status-badge status-pending',
    1: 'status-badge status-shipping',
    2: 'status-badge status-delivered',
    3: 'status-badge status-completed',
    '-1': 'status-badge status-cancelled'
  }
  return map[status] || 'status-badge'
}

const formatPrice = (price) => Number(price).toLocaleString()

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-TW')
}

onMounted(() => {
  if (!isAdmin.value) {
    fetchProducts()
  }
  fetchOrders()
})
</script>

<style scoped>
.price {
  font-weight: 600;
  color: var(--primary-color);
}

.product-card.selected {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.2);
}
</style>
