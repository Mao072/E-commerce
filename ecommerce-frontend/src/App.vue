<template>
  <div id="app">
    <header class="header" v-if="isLoggedIn">
      <h1>🛒 電商購物中心</h1>
      <nav class="header-nav">
        <router-link to="/orders" :class="{ active: $route.path === '/orders' }">
          訂單管理
        </router-link>
        <router-link 
          v-if="isAdmin" 
          to="/products" 
          :class="{ active: $route.path === '/products' }"
        >
          商品管理
        </router-link>
        <div class="user-info">
          <span>{{ username }}</span>
          <span class="role-badge" :class="isAdmin ? 'admin' : 'user'">
            {{ isAdmin ? '管理員' : '會員' }}
          </span>
          <button class="btn btn-sm btn-outline" @click="logout">登出</button>
        </div>
      </nav>
    </header>
    <main>
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const isLoggedIn = computed(() => !!localStorage.getItem('token'))
const username = computed(() => localStorage.getItem('username') || '')
const isAdmin = computed(() => localStorage.getItem('role') === 'ADMIN')

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  localStorage.removeItem('username')
  localStorage.removeItem('memberId')
  router.push('/login')
}
</script>
