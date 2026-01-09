<template>
  <div class="auth-container">
    <div class="auth-card">
      <h2>📝 註冊</h2>
      <p class="subtitle">建立新帳號開始購物</p>
      
      <div v-if="error" class="alert alert-error">
        {{ error }}
      </div>
      
      <div v-if="success" class="alert alert-success">
        {{ success }}
      </div>
      
      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label>帳號</label>
          <input 
            v-model="form.username" 
            type="text" 
            class="form-control" 
            placeholder="請輸入帳號"
            required
          />
        </div>
        
        <div class="form-group">
          <label>密碼</label>
          <input 
            v-model="form.password" 
            type="password" 
            class="form-control" 
            placeholder="請輸入密碼（至少 6 字元）"
            required
            minlength="6"
          />
        </div>
        
        <div class="form-group">
          <label>角色</label>
          <select v-model="form.role" class="form-control">
            <option value="USER">一般會員</option>
            <option value="ADMIN">管理員</option>
          </select>
        </div>
        
        <button type="submit" class="btn btn-primary" style="width: 100%; padding: 14px;" :disabled="loading">
          {{ loading ? '註冊中...' : '註冊' }}
        </button>
      </form>
      
      <p class="auth-links">
        已有帳號？ <router-link to="/login">立即登入</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '@/services/api'

const router = useRouter()
const loading = ref(false)
const error = ref('')
const success = ref('')

const form = reactive({
  username: '',
  password: '',
  role: 'USER'
})

const handleRegister = async () => {
  loading.value = true
  error.value = ''
  success.value = ''
  
  try {
    const response = await register(form)
    
    if (response.success) {
      success.value = '註冊成功！正在跳轉至登入頁面...'
      setTimeout(() => {
        router.push('/login')
      }, 1500)
    } else {
      error.value = response.message || '註冊失敗'
    }
  } catch (err) {
    error.value = err.message || '註冊失敗，請稍後再試'
  } finally {
    loading.value = false
  }
}
</script>
