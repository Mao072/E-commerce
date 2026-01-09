<template>
  <div class="container">
    <div class="page-title">
      <h2>🏷️ 商品管理</h2>
      <button class="btn btn-primary" @click="showModal = true">
        ➕ 新增商品
      </button>
    </div>
    
    <div v-if="loading" class="loading">
      <div class="spinner"></div>
    </div>
    
    <div v-else-if="products.length === 0" class="card">
      <div class="empty-state">
        <h3>尚無商品</h3>
        <p>點擊右上角按鈕新增第一個商品</p>
      </div>
    </div>
    
    <div v-else class="card">
      <div class="table-container">
        <table>
          <thead>
            <tr>
              <th>商品編號</th>
              <th>商品名稱</th>
              <th>售價</th>
              <th>庫存數量</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="product in products" :key="product.productId">
              <td>{{ product.productId }}</td>
              <td>{{ product.productName }}</td>
              <td>NT$ {{ formatNumber(product.price) }}</td>
              <td>
                <span :class="product.quantity > 0 ? 'status-delivered' : 'status-cancelled'" class="status-badge">
                  {{ product.quantity }}
                </span>
              </td>
              <td>
                <button 
                  class="btn btn-sm" 
                  style="background: #4a5568; color: white; padding: 4px 12px;"
                  @click="openRestockModal(product)"
                >
                  📦 補貨
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    
    <!-- Add Product Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal">
        <h3>新增商品</h3>
        
        <div v-if="modalError" class="alert alert-error">
          {{ modalError }}
        </div>
        
        <form @submit.prevent="handleAddProduct">
          <div class="form-group">
            <label>商品編號</label>
            <input 
              v-model="newProduct.productId" 
              type="text" 
              class="form-control" 
              placeholder="例如: P004"
              required
            />
          </div>
          
          <div class="form-group">
            <label>商品名稱</label>
            <input 
              v-model="newProduct.productName" 
              type="text" 
              class="form-control" 
              placeholder="請輸入商品名稱"
              required
            />
          </div>
          
          <div class="form-group">
            <label>售價 (NT$)</label>
            <input 
              v-model.number="newProduct.price" 
              type="number" 
              class="form-control" 
              placeholder="0"
              min="0"
              required
            />
          </div>
          
          <div class="form-group">
            <label>庫存數量</label>
            <input 
              v-model.number="newProduct.quantity" 
              type="number" 
              class="form-control" 
              placeholder="0"
              min="0"
              required
            />
          </div>
          
          <div class="modal-actions">
            <button type="button" class="btn btn-secondary" @click="showModal = false">
              取消
            </button>
            <button type="submit" class="btn btn-primary" :disabled="modalLoading">
              {{ modalLoading ? '新增中...' : '確認新增' }}
            </button>
          </div>
        </form>
      </div>
    </div>
    
    <!-- Restock Modal -->
    <div v-if="showRestockModal" class="modal-overlay" @click.self="showRestockModal = false">
      <div class="modal">
        <h3>商品補貨</h3>
        <p style="margin-bottom: 20px; color: #666;">
          目前商品：<strong>{{ activeProduct?.productName }}</strong> ({{ activeProduct?.productId }})<br>
          目前庫存：{{ activeProduct?.quantity }}
        </p>
        
        <form @submit.prevent="handleRestock">
          <div class="form-group">
            <label>增加數量</label>
            <input 
              v-model.number="restockAmount" 
              type="number" 
              class="form-control" 
              min="1"
              required
            />
          </div>
          
          <div class="modal-actions">
            <button type="button" class="btn btn-secondary" @click="showRestockModal = false">
              取消
            </button>
            <button type="submit" class="btn btn-primary" :disabled="modalLoading">
              {{ modalLoading ? '更新中...' : '確認補貨' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getAllProducts, addProduct, updateProductStock } from '@/services/api'

const products = ref([])
const loading = ref(true)
const showModal = ref(false)
const modalLoading = ref(false)
const modalError = ref('')

const newProduct = reactive({
  productId: '',
  productName: '',
  price: 0,
  quantity: 0
})

const showRestockModal = ref(false)
const activeProduct = ref(null)
const restockAmount = ref(1)

const formatNumber = (num) => {
  return new Intl.NumberFormat('zh-TW').format(num)
}

const fetchProducts = async () => {
  loading.value = true
  try {
    const response = await getAllProducts()
    if (response.success) {
      products.value = response.data
    }
  } catch (err) {
    console.error('Failed to fetch products:', err)
  } finally {
    loading.value = false
  }
}

const handleAddProduct = async () => {
  modalLoading.value = true
  modalError.value = ''
  
  try {
    const response = await addProduct(newProduct)
    
    if (response.success) {
      showModal.value = false
      newProduct.productId = ''
      newProduct.productName = ''
      newProduct.price = 0
      newProduct.quantity = 0
      await fetchProducts()
    } else {
      modalError.value = response.message || '新增失敗'
    }
  } catch (err) {
    modalError.value = err.message || '新增失敗，請稍後再試'
  } finally {
    modalLoading.value = false
  }
}

const openRestockModal = (product) => {
  activeProduct.value = product
  restockAmount.value = 1
  showRestockModal.value = true
}

const handleRestock = async () => {
  modalLoading.value = true
  try {
    const response = await updateProductStock(activeProduct.value.productId, restockAmount.value)
    if (response.success) {
      showRestockModal.value = false
      await fetchProducts()
      alert('補貨成功！')
    } else {
      alert(response.message || '補貨失敗')
    }
  } catch (err) {
    alert(err.message || '補貨失敗')
  } finally {
    modalLoading.value = false
  }
}

onMounted(() => {
  fetchProducts()
})
</script>
