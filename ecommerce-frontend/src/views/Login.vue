<template>
  <div class="auth-container">
    <div class="auth-card">
      <h2>歡迎回來</h2>
      <p class="subtitle">登入您的帳號</p>
      
      <div v-if="error" class="alert alert-error">{{ error }}</div>
      
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="username">帳號</label>
          <input 
            id="username"
            v-model="form.username" 
            type="text" 
            class="form-control"
            placeholder="請輸入帳號"
            required
          />
        </div>
        
        <div class="form-group">
          <label for="password">密碼</label>
          <input 
            id="password"
            v-model="form.password" 
            type="password" 
            class="form-control"
            placeholder="請輸入密碼"
            required
          />
        </div>
        
        <button type="submit" class="btn btn-primary" style="width: 100%;" :disabled="loading">
          {{ loading ? '登入中...' : '登入' }}
        </button>
      </form>
      
      <p class="auth-links">
        還沒有帳號？<router-link to="/register">立即註冊</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '../services/api'

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
    const response = await authApi.login(form)
    const { token, memberId, username, role } = response.data.data
    
    localStorage.setItem('token', token)
    localStorage.setItem('memberId', memberId)
    localStorage.setItem('username', username)
    localStorage.setItem('role', role)
    
    // Redirect based on role
    if (role === 'ADMIN') {
      router.push('/products')
    } else {
      router.push('/orders')
    }
  } catch (err) {
    error.value = err.response?.data?.message || '登入失敗，請檢查帳號密碼'
  } finally {
    loading.value = false
  }
}
</script>
