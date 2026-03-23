<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-card">
        <h2>注册</h2>
        <n-form ref="formRef" :model="form" :rules="rules">
          <n-form-item path="username" label="用户名">
            <n-input v-model:value="form.username" placeholder="请输入用户名" />
          </n-form-item>
          <n-form-item path="password" label="密码">
            <n-input v-model:value="form.password" type="password" placeholder="请输入密码" />
          </n-form-item>
          <n-form-item path="confirmPassword" label="确认密码">
            <n-input v-model:value="form.confirmPassword" type="password" placeholder="请再次输入密码" />
          </n-form-item>
          <n-form-item path="phone" label="手机号">
            <n-input v-model:value="form.phone" placeholder="请输入手机号" />
          </n-form-item>
          <n-form-item path="roleCode" label="角色">
            <n-select v-model:value="form.roleCode" :options="roleOptions" placeholder="请选择角色" />
          </n-form-item>
          <n-form-item>
            <n-button type="primary" block @click="handleRegister" :loading="loading">
              注册
            </n-button>
          </n-form-item>
        </n-form>
        <div class="register-footer">
          <span>已有账号？</span>
          <router-link to="/">立即登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { register } from '@/api/auth'

const router = useRouter()
const message = useMessage()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  phone: '',
  roleCode: 'MEMBER'
})

const roleOptions = [
  { label: '会员', value: 'MEMBER' },
  { label: '教练', value: 'COACH' }
]

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value) => value === form.password,
      message: '两次密码输入不一致',
      trigger: 'blur'
    }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  roleCode: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

async function handleRegister() {
  try {
    await formRef.value?.validate()
    loading.value = true
    const res = await register(form)
    if (res.data?.code === 200) {
      message.success('注册成功，请登录')
      router.push('/')
    } else {
      message.error(res.data?.message || '注册失败')
    }
  } catch (error) {
    console.error('注册失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1890ff 0%, #722ed1 100%);
}

.register-container {
  width: 100%;
  max-width: 400px;
  padding: 20px;
}

.register-card {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.register-card h2 {
  text-align: center;
  margin-bottom: 24px;
  color: #333;
}

.register-footer {
  text-align: center;
  margin-top: 16px;
  color: #666;
}

.register-footer a {
  color: #1890ff;
  margin-left: 4px;
}
</style>
