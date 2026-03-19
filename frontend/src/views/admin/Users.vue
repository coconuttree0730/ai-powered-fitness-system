<template>
  <div class="users-page">
    <n-card title="用户管理">
      <!-- 搜索区域 -->
      <n-card embedded style="margin-bottom: 16px;">
        <n-space align="center" wrap justify="space-between">
          <n-space align="center" wrap>
            <n-input
              v-model:value="searchForm.username"
              placeholder="用户名"
              clearable
              style="width: 200px"
              @keyup.enter="handleSearch"
            />
            <n-input
              v-model:value="searchForm.phone"
              placeholder="手机号"
              clearable
              style="width: 150px"
              @keyup.enter="handleSearch"
            />
            <n-select
              v-model:value="searchForm.role"
              :options="roleOptions"
              placeholder="角色"
              clearable
              style="width: 150px"
            />
            <n-select
              v-model:value="searchForm.status"
              :options="statusOptions"
              placeholder="状态"
              clearable
              style="width: 120px"
            />
            <n-button type="primary" @click="handleSearch">
              <template #icon>
                <n-icon><SearchOutline /></n-icon>
              </template>
              搜索
            </n-button>
            <n-button @click="handleReset">重置</n-button>
          </n-space>
          <n-button type="primary" @click="handleAdd">
            <template #icon>
              <n-icon><AddOutline /></n-icon>
            </template>
            新增用户
          </n-button>
        </n-space>
      </n-card>

      <n-data-table :columns="columns" :data="users" :loading="loading" :pagination="pagination" :row-key="row => row.id" />
    </n-card>

    <n-modal v-model:show="showModal" preset="card" :title="isEdit ? '编辑用户' : '新增用户'" style="width: 600px">
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="left" label-width="80">
        <n-grid :cols="2" :x-gap="12">
          <n-grid-item>
            <n-form-item label="用户名" path="username">
              <n-input v-model:value="form.username" placeholder="请输入用户名" :disabled="isEdit" />
            </n-form-item>
          </n-grid-item>
          <n-grid-item>
            <n-form-item v-if="!isEdit" label="密码" path="password">
              <n-input v-model:value="form.password" type="password" placeholder="请输入密码" />
            </n-form-item>
            <n-form-item v-else label="密码">
              <n-space align="center">
                <n-input value="********" disabled placeholder="密码已加密" style="width: 150px" />
                <n-button type="warning" size="small" :loading="resettingPassword" @click="handleResetPasswordClick">重置密码</n-button>
              </n-space>
            </n-form-item>
          </n-grid-item>
        </n-grid>
        <n-grid :cols="2" :x-gap="12">
          <n-grid-item>
            <n-form-item label="手机号" path="phone">
              <n-input v-model:value="form.phone" placeholder="请输入手机号" />
            </n-form-item>
          </n-grid-item>
          <n-grid-item>
            <n-form-item label="邮箱" path="email">
              <n-input v-model:value="form.email" placeholder="请输入邮箱" />
            </n-form-item>
          </n-grid-item>
        </n-grid>
        <n-form-item v-if="!isEdit" label="角色" path="roleCode">
          <n-select v-model:value="form.roleCode" :options="roleOptions" placeholder="请选择角色" />
        </n-form-item>
        <n-form-item v-else label="角色" path="roleCode">
          <n-tag type="info">{{ getRoleLabel(form.roleCode) }}</n-tag>
        </n-form-item>

        <!-- 头像上传 -->
        <n-form-item label="头像" path="avatar">
          <n-space vertical>
            <n-upload
              :action="uploadUrl"
              :headers="uploadHeaders"
              :data="{ folder: 'avatars' }"
              name="file"
              accept="image/*"
              :max="1"
              v-model:file-list="fileList"
              list-type="image-card"
              style="--n-image-width: 100px; --n-image-height: 100px;"
              @before-upload="handleBeforeUpload"
              @finish="handleUploadFinish"
              @remove="handleUploadRemove"
              @error="handleUploadError"
            >
              <n-button style="width: 100px; height: 100px;">
                <n-space vertical align="center">
                  <n-icon size="24"><CloudUploadOutline /></n-icon>
                  <span>上传头像</span>
                </n-space>
              </n-button>
            </n-upload>
            <n-text depth="3" style="font-size: 12px;">支持 JPG、PNG 格式，建议尺寸 200x200</n-text>
          </n-space>
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
import { ref, h, reactive, onMounted, computed } from 'vue'
import { NTag, NButton, NSpace, NIcon, NImage, useMessage, useDialog, NText } from 'naive-ui'
import { SearchOutline, AddOutline, CloudUploadOutline } from '@vicons/ionicons5'
import { getUserList, createUser, updateUser, deleteUser, updateUserStatus, resetUserPassword } from '@/api/user'
import { getToken } from '@/utils/auth'

const message = useMessage()
const dialog = useDialog()
const loading = ref(false)
const submitting = ref(false)
const showModal = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const users = ref([])
const currentId = ref(null)

// 上传相关
const uploadUrl = '/api/v1/files/upload'
const uploadHeaders = computed(() => ({
  Authorization: 'Bearer ' + getToken()
}))
const fileList = ref([])

const pagination = reactive({
  page: 1,
  pageSize: 5,
  itemCount: 0,
  onChange: (page) => {
    pagination.page = page
    fetchUsers()
  }
})

const searchForm = reactive({
  username: '',
  phone: '',
  role: null,
  status: null
})

const form = reactive({
  username: '',
  password: '',
  phone: '',
  email: '',
  roleCode: null,
  avatar: ''
})

const rules = computed(() => {
  const baseRules = {
    username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
    roleCode: [{ required: true, message: '请选择角色', trigger: 'change' }]
  }
  // 新增时才需要校验密码
  if (!isEdit.value) {
    baseRules.password = [{ required: true, message: '请输入密码', trigger: 'blur' }]
  }
  return baseRules
})

const roleOptions = [
  { label: '管理员', value: 'ADMIN' },
  { label: '教练', value: 'COACH' },
  { label: '会员', value: 'MEMBER' }
]

const statusOptions = [
  { label: '正常', value: 1 },
  { label: '禁用', value: 0 }
]

const getRoleLabel = (roleCode) => {
  const role = roleOptions.find(r => r.value === roleCode)
  return role ? role.label : roleCode
}

// 上传前校验
function handleBeforeUpload({ file }) {
  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    message.error('只支持 JPG、PNG、GIF、WebP 格式的图片')
    return false
  }
  const maxSize = 10 * 1024 * 1024
  if (file.size > maxSize) {
    message.error('图片大小不能超过 10MB')
    return false
  }
  return true
}

// 上传完成回调
function handleUploadFinish({ file, event }) {
  try {
    const response = JSON.parse(event.target.response)
    if (response.code === 200) {
      form.avatar = response.data.fileUrl
      message.success('头像上传成功')
    } else {
      message.error(response.message || '上传失败')
      fileList.value = []
    }
  } catch (error) {
    console.error('解析上传响应失败:', error)
    message.error('上传响应解析失败')
    fileList.value = []
  }
}

// 上传失败回调
function handleUploadError({ file, event }) {
  message.error('头像上传失败')
}

// 移除图片回调
function handleUploadRemove({ file, fileList: newFileList }) {
  form.avatar = ''
  fileList.value = newFileList
}

const columns = [
  {
    title: '头像',
    key: 'avatar',
    width: 80,
    render: (row) => {
      if (row.avatar) {
        return h(NImage, {
          src: row.avatar,
          width: 80,
          height: 80,
          style: 'border-radius: 50%; object-fit: cover;',
          fallbackSrc: '/default-avatar.png'
        })
      }
      return h('div', {
        style: 'width: 80px; height: 80px; background: #f0f0f0; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: #999; font-size: 12px;'
      }, '无')
    }
  },
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
    const params = {
      pageNum: pagination.page,
      pageSize: pagination.pageSize,
      ...buildSearchParams()
    }
    const res = await getUserList(params)
    users.value = res.records || []
    pagination.itemCount = res.total || 0
  } catch (error) {
    message.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

function buildSearchParams() {
  const params = {}
  if (searchForm.username) {
    params.username = searchForm.username
  }
  if (searchForm.phone) {
    params.phone = searchForm.phone
  }
  if (searchForm.role) {
    params.role = searchForm.role
  }
  if (searchForm.status !== null && searchForm.status !== undefined) {
    params.status = searchForm.status
  }
  return params
}

function handleSearch() {
  pagination.page = 1
  fetchUsers()
}

function handleReset() {
  searchForm.username = ''
  searchForm.phone = ''
  searchForm.role = null
  searchForm.status = null
  pagination.page = 1
  fetchUsers()
}

function handleAdd() {
  isEdit.value = false
  currentId.value = null
  Object.assign(form, { username: '', password: '', phone: '', email: '', roleCode: null, avatar: '' })
  fileList.value = []
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
    roleCode: row.roles && row.roles[0] ? row.roles[0] : null,
    avatar: row.avatar || ''
  })
  // 设置图片文件列表
  if (row.avatar) {
    fileList.value = [{
      id: 'existing',
      name: '头像',
      status: 'finished',
      url: row.avatar
    }]
  } else {
    fileList.value = []
  }
  showModal.value = true
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    submitting.value = true

    if (isEdit.value) {
      // 编辑时只提交必要的字段，不包含密码
      const updateData = {
        phone: form.phone,
        email: form.email,
        avatar: form.avatar
      }
      await updateUser(currentId.value, updateData)
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

// 重置密码相关
const resettingPassword = ref(false)

async function handleResetPasswordClick() {
  if (resettingPassword.value) return
  
  dialog.warning({
    title: '确认重置密码',
    content: '确定要将该用户的密码重置为默认密码 "123456" 吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        resettingPassword.value = true
        await resetUserPassword(currentId.value, '123456')
        message.success('密码已重置为 123456')
      } catch (error) {
        message.error(error.message || '密码重置失败')
      } finally {
        resettingPassword.value = false
      }
    }
  })
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
