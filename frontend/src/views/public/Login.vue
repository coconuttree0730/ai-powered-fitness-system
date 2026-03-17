<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-card">
        <h2>登录</h2>
        <n-form ref="formRef" :model="form" :rules="rules">
          <n-form-item path="username" label="用户名">
            <n-input v-model:value="form.username" placeholder="请输入用户名" />
          </n-form-item>
          <n-form-item path="password" label="密码">
            <n-input v-model:value="form.password" type="password" placeholder="请输入密码" />
          </n-form-item>
          <n-form-item>
            <n-button type="primary" block @click="handleLogin" :loading="loading">
              登录
            </n-button>
          </n-form-item>
        </n-form>
        <div class="login-footer">
          <span>还没有账号？</span>
          <router-link to="/register">立即注册</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useMessage } from 'naive-ui'
import { useAuthStore } from '@/stores/auth'

const message = useMessage()
const authStore = useAuthStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  try {
    await formRef.value?.validate()
    loading.value = true
    const result = await authStore.login(form)
    if (!result.success) {
      message.error(result.message)
    }
  } catch (error) {
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1890ff 0%, #722ed1 100%);
}

.login-container {
  width: 100%;
  max-width: 400px;
  padding: 20px;
}

.login-card {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.login-card h2 {
  text-align: center;
  margin-bottom: 24px;
  color: #333;
}

.login-footer {
  text-align: center;
  margin-top: 16px;
  color: #666;
}

.login-footer a {
  color: #1890ff;
  margin-left: 4px;
}
</style>
