<template>
  <div class="container">
    <div class="card">
      <div class="page-title">
        <h2>📦 商品管理</h2>
        <button class="btn btn-primary" @click="showAddModal = true">
          ➕ 新增商品
        </button>
      </div>

      <div v-if="error" class="alert alert-error">{{ error }}</div>
      <div v-if="success" class="alert alert-success">{{ success }}</div>

      <div v-if="loading" class="loading">
        <div class="spinner"></div>
      </div>

      <div v-else-if="products.length === 0" class="empty-state">
        <h3>尚無商品</h3>
        <p>點擊「新增商品」開始建立商品資料</p>
      </div>

      <div v-else class="table-container">
        <table>
          <thead>
            <tr>
              <th>商品編號</th>
              <th>商品名稱</th>
              <th>售價</th>
              <th>庫存</th>
              <th>建立時間</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="product in products" :key="product.productId">
              <td>{{ product.productId }}</td>
              <td>{{ product.productName }}</td>
              <td class="price">NT$ {{ formatPrice(product.price) }}</td>
              <td>
                <span 
                  :class="product.quantity > 0 ? 'status-badge status-delivered' : 'status-badge status-cancelled'"
                >
                  {{ product.quantity }}
                </span>
              </td>
              <td>{{ formatDate(product.createdAt) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Add Product Modal -->
    <div v-if="showAddModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal">
        <h3>新增商品</h3>
        <form @submit.prevent="handleAddProduct">
          <div class="form-group">
            <label for="productId">商品編號</label>
            <input 
              id="productId"
              v-model="newProduct.productId" 
              type="text" 
              class="form-control"
              placeholder="例: P004"
              required
              maxlength="20"
            />
          </div>
          
          <div class="form-group">
            <label for="productName">商品名稱</label>
            <input 
              id="productName"
              v-model="newProduct.productName" 
              type="text" 
              class="form-control"
              placeholder="請輸入商品名稱"
              required
              maxlength="100"
            />
          </div>
          
          <div class="form-group">
            <label for="price">售價</label>
            <input 
              id="price"
              v-model.number="newProduct.price" 
              type="number" 
              class="form-control"
              placeholder="請輸入售價"
              required
              min="0"
              step="0.01"
            />
          </div>
          
          <div class="form-group">
            <label for="quantity">庫存數量</label>
            <input 
              id="quantity"
              v-model.number="newProduct.quantity" 
              type="number" 
              class="form-control"
              placeholder="請輸入庫存數量"
              required
              min="0"
            />
          </div>
          
          <div class="modal-actions">
            <button type="button" class="btn btn-secondary" @click="closeModal">取消</button>
            <button type="submit" class="btn btn-success" :disabled="addLoading">
              {{ addLoading ? '新增中...' : '確定新增' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { productApi } from '../services/api'

const products = ref([])
const loading = ref(true)
const addLoading = ref(false)
const error = ref('')
const success = ref('')
const showAddModal = ref(false)

const newProduct = reactive({
  productId: '',
  productName: '',
  price: 0,
  quantity: 0
})

const fetchProducts = async () => {
  loading.value = true
  error.value = ''
  
  try {
    const response = await productApi.getAll()
    products.value = response.data.data || []
  } catch (err) {
    error.value = err.response?.data?.message || '載入商品失敗'
  } finally {
    loading.value = false
  }
}

const handleAddProduct = async () => {
  addLoading.value = true
  error.value = ''
  success.value = ''
  
  try {
    await productApi.add(newProduct)
    success.value = '商品新增成功！'
    closeModal()
    fetchProducts()
  } catch (err) {
    error.value = err.response?.data?.message || '新增商品失敗'
  } finally {
    addLoading.value = false
  }
}

const closeModal = () => {
  showAddModal.value = false
  newProduct.productId = ''
  newProduct.productName = ''
  newProduct.price = 0
  newProduct.quantity = 0
}

const formatPrice = (price) => {
  return Number(price).toLocaleString()
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('zh-TW')
}

onMounted(fetchProducts)
</script>

<style scoped>
.price {
  font-weight: 600;
  color: var(--primary-color);
}
</style>
