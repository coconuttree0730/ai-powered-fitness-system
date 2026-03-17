<template>
  <div class="users-page">
    <n-card title="用户管理">
      <template #header-extra>
        <n-button type="primary" @click="showModal = true">新增用户</n-button>
      </template>
      <n-data-table :columns="columns" :data="users" :loading="loading" :pagination="pagination" />
    </n-card>

    <n-modal v-model:show="showModal" preset="card" title="新增用户" style="width: 500px">
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="left" label-width="80">
        <n-form-item label="用户名" path="username">
          <n-input v-model:value="form.username" placeholder="请输入用户名" />
        </n-form-item>
        <n-form-item label="密码" path="password">
          <n-input v-model:value="form.password" type="password" placeholder="请输入密码" />
        </n-form-item>
        <n-form-item label="姓名" path="realName">
          <n-input v-model:value="form.realName" placeholder="请输入姓名" />
        </n-form-item>
        <n-form-item label="手机号" path="phone">
          <n-input v-model:value="form.phone" placeholder="请输入手机号" />
        </n-form-item>
        <n-form-item label="角色" path="role">
          <n-select v-model:value="form.role" :options="roleOptions" placeholder="请选择角色" />
        </n-form-item>
        <n-form-item>
          <n-button type="primary" :loading="submitting" @click="handleSubmit">提交</n-button>
        </n-form-item>
      </n-form>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, h, reactive, onMounted } from 'vue'
import { NTag, NButton, NSpace, useMessage } from 'naive-ui'

const message = useMessage()
const loading = ref(false)
const submitting = ref(false)
const showModal = ref(false)
const formRef = ref(null)
const users = ref([])

const pagination = reactive({
  pageSize: 10
})

const form = reactive({
  username: '',
  password: '',
  realName: '',
  phone: '',
  role: null
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const roleOptions = [
  { label: '管理员', value: 'ADMIN' },
  { label: '教练', value: 'COACH' },
  { label: '会员', value: 'MEMBER' }
]

const columns = [
  { title: '用户名', key: 'username' },
  { title: '姓名', key: 'realName' },
  { title: '手机号', key: 'phone' },
  { title: '邮箱', key: 'email' },
  {
    title: '角色',
    key: 'roles',
    render: (row) => h(NSpace, null, () => (row.roles || []).map(r => h(NTag, { type: 'info', size: 'small' }, () => r)))
  },
  {
    title: '状态',
    key: 'status',
    render: (row) => h(NTag, { type: row.status === 1 ? 'success' : 'default' }, () => row.status === 1 ? '正常' : '禁用')
  },
  {
    title: '操作',
    key: 'actions',
    render: (row) => h(NSpace, null, () => [
      h(NButton, { size: 'small', onClick: () => handleEdit(row) }, () => '编辑'),
      h(NButton, { size: 'small', type: 'error', onClick: () => handleDelete(row.id) }, () => '删除')
    ])
  }
]

onMounted(() => {
  fetchUsers()
})

async function fetchUsers() {
  loading.value = true
  users.value = []
  loading.value = false
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    submitting.value = true
    message.success('创建成功')
    showModal.value = false
    fetchUsers()
  } catch (error) {
    message.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

function handleEdit(row) {
  message.info('编辑功能待实现')
}

function handleDelete(id) {
  message.info('删除功能待实现')
}
</script>

<style scoped>
.users-page {
  padding: 0;
}
</style>
