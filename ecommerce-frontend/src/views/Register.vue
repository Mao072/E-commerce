<template>
  <div class="auth-container">
    <div class="auth-card">
      <h2>建立帳號</h2>
      <p class="subtitle">加入會員開始購物</p>
      
      <div v-if="error" class="alert alert-error">{{ error }}</div>
      <div v-if="success" class="alert alert-success">{{ success }}</div>
      
      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label for="username">帳號</label>
          <input 
            id="username"
            v-model="form.username" 
            type="text" 
            class="form-control"
            placeholder="請輸入帳號 (3-50 字元)"
            required
            minlength="3"
            maxlength="50"
          />
        </div>
        
        <div class="form-group">
          <label for="password">密碼</label>
          <input 
            id="password"
            v-model="form.password" 
            type="password" 
            class="form-control"
            placeholder="請輸入密碼 (至少 6 字元)"
            required
            minlength="6"
          />
        </div>
        
        <div class="form-group">
          <label for="role">角色</label>
          <select id="role" v-model="form.role" class="form-control">
            <option value="USER">一般會員</option>
            <option value="ADMIN">管理員</option>
          </select>
        </div>
        
        <button type="submit" class="btn btn-primary" style="width: 100%;" :disabled="loading">
          {{ loading ? '註冊中...' : '註冊' }}
        </button>
      </form>
      
      <p class="auth-links">
        已有帳號？<router-link to="/login">立即登入</router-link>
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
    await authApi.register(form)
    success.value = '註冊成功！即將跳轉到登入頁面...'
    
    setTimeout(() => {
      router.push('/login')
    }, 2000)
  } catch (err) {
    error.value = err.response?.data?.message || '註冊失敗，請稍後再試'
  } finally {
    loading.value = false
  }
}
</script>
