<template>
  <div class="users-page">
    <n-card title="用户管理">
      <template #header-extra>
        <n-button type="primary" @click="handleAdd">新增用户</n-button>
      </template>
      <n-data-table :columns="columns" :data="users" :loading="loading" :pagination="pagination" :row-key="row => row.id" />
    </n-card>

    <n-modal v-model:show="showModal" preset="card" :title="isEdit ? '编辑用户' : '新增用户'" style="width: 500px">
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="left" label-width="80">
        <n-form-item label="用户名" path="username">
          <n-input v-model:value="form.username" placeholder="请输入用户名" :disabled="isEdit" />
        </n-form-item>
        <n-form-item v-if="!isEdit" label="密码" path="password">
          <n-input v-model:value="form.password" type="password" placeholder="请输入密码" />
        </n-form-item>
        <n-form-item label="手机号" path="phone">
          <n-input v-model:value="form.phone" placeholder="请输入手机号" />
        </n-form-item>
        <n-form-item label="邮箱" path="email">
          <n-input v-model:value="form.email" placeholder="请输入邮箱" />
        </n-form-item>
        <n-form-item v-if="!isEdit" label="角色" path="roleCode">
          <n-select v-model:value="form.roleCode" :options="roleOptions" placeholder="请选择角色" />
        </n-form-item>
        <n-form-item>
          <n-space>
            <n-button type="primary" :loading="submitting" @click="handleSubmit">提交</n-button>
            <n-button @click="showModal = false">取消</n-button>
          </n-space>
        </n-form-item>
      </n-form>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, h, reactive, onMounted } from 'vue'
import { NTag, NButton, NSpace, useMessage, useDialog } from 'naive-ui'
import { getUserList, createUser, updateUser, deleteUser, updateUserStatus } from '@/api/user'

const message = useMessage()
const dialog = useDialog()
const loading = ref(false)
const submitting = ref(false)
const showModal = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const users = ref([])
const currentId = ref(null)

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  onChange: (page) => {
    pagination.page = page
    fetchUsers()
  }
})

const form = reactive({
  username: '',
  password: '',
  phone: '',
  email: '',
  roleCode: null
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const roleOptions = [
  { label: '管理员', value: 'ADMIN' },
  { label: '教练', value: 'COACH' },
  { label: '会员', value: 'MEMBER' }
]

const columns = [
  { title: '用户名', key: 'username' },
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
      h(NButton, { 
        size: 'small', 
        type: row.status === 1 ? 'warning' : 'success',
        onClick: () => handleToggleStatus(row)
      }, () => row.status === 1 ? '禁用' : '启用'),
      h(NButton, { size: 'small', type: 'error', onClick: () => handleDelete(row) }, () => '删除')
    ])
  }
]

onMounted(() => {
  fetchUsers()
})

async function fetchUsers() {
  loading.value = true
  try {
    const res = await getUserList({ pageNum: pagination.page, pageSize: pagination.pageSize })
    users.value = res.records || []
    pagination.itemCount = res.total || 0
  } catch (error) {
    message.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  isEdit.value = false
  currentId.value = null
  Object.assign(form, { username: '', password: '', phone: '', email: '', roleCode: null })
  showModal.value = true
}

function handleEdit(row) {
  isEdit.value = true
  currentId.value = row.id
  Object.assign(form, {
    username: row.username,
    password: '',
    phone: row.phone || '',
    email: row.email || '',
    roleCode: null
  })
  showModal.value = true
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    submitting.value = true
    
    if (isEdit.value) {
      await updateUser(currentId.value, form)
      message.success('更新成功')
    } else {
      await createUser(form)
      message.success('创建成功')
    }
    
    showModal.value = false
    fetchUsers()
  } catch (error) {
    message.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

function handleDelete(row) {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除用户 "${row.username}" 吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteUser(row.id)
        message.success('删除成功')
        fetchUsers()
      } catch (error) {
        message.error('删除失败')
      }
    }
  })
}

async function handleToggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await updateUserStatus(row.id, newStatus)
    message.success('状态更新成功')
    fetchUsers()
  } catch (error) {
    message.error('状态更新失败')
  }
}
</script>

<style scoped>
.users-page {
  padding: 0;
}
</style>
