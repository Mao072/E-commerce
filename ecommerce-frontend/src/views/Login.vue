<template>
  <div class="auth-container">
    <div class="auth-card">
      <h2>🔐 登入</h2>
      <p class="subtitle">歡迎回來！請登入您的帳號</p>
      
      <div v-if="error" class="alert alert-error">
        {{ error }}
      </div>
      
      <form @submit.prevent="handleLogin">
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
            placeholder="請輸入密碼"
            required
          />
        </div>
        
        <button type="submit" class="btn btn-primary" style="width: 100%; padding: 14px;" :disabled="loading">
          {{ loading ? '登入中...' : '登入' }}
        </button>
      </form>
      
      <p class="auth-links">
        還沒有帳號？ <router-link to="/register">立即註冊</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '@/services/api'

const router = useRouter()
const loading = ref(false)
const error = ref('')

const form = reactive({
  username: '',
  password: ''
})

const handleLogin = async () => {
  loading.value = true
  error.value = ''
  
  try {
    const response = await login(form)
    
    if (response.success) {
      localStorage.setItem('token', response.data.token)
      localStorage.setItem('role', response.data.role)
      localStorage.setItem('username', response.data.username)
      localStorage.setItem('memberId', response.data.memberId)
      
      if (response.data.role === 'ADMIN') {
        window.location.href = '/products'
      } else {
        window.location.href = '/orders'
      }
    } else {
      error.value = response.message || '登入失敗'
    }
  } catch (err) {
    error.value = err.message || '登入失敗，請稍後再試'
  } finally {
    loading.value = false
  }
}
</script>
