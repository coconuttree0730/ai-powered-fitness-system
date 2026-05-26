<template>
  <div class="users-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
        </div>
      </template>

      <!-- 搜索区域 -->
      <el-card shadow="never" style="margin-bottom: 16px;">
        <el-row justify="space-between" align="middle">
          <el-col :span="20">
            <el-space wrap>
              <el-input
                v-model="searchForm.username"
                placeholder="用户名"
                clearable
                style="width: 200px"
                @keyup.enter="handleSearch"
              />
              <el-input
                v-model="searchForm.phone"
                placeholder="手机号"
                clearable
                style="width: 150px"
                @keyup.enter="handleSearch"
              />
              <el-select
                v-model="searchForm.role"
                :options="roleOptions"
                placeholder="角色"
                clearable
                style="width: 150px"
              />
              <el-select
                v-model="searchForm.status"
                :options="statusOptions"
                placeholder="状态"
                clearable
                style="width: 120px"
              />
              <el-button type="primary" @click="handleSearch">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-space>
          </el-col>
          <el-col :span="4" style="text-align: right;">
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              新增用户
            </el-button>
          </el-col>
        </el-row>
      </el-card>

      <el-table
        :data="users"
        v-loading="loading"
        :row-key="row => row.id"
        stripe
        style="width: 100%"
      >
        <el-table-column label="头像" width="100" align="center">
          <template #default="{ row }">
            <el-avatar
              v-if="row.avatar"
              :src="row.avatar"
              :size="60"
              shape="circle"
            />
            <div v-else class="no-avatar">无</div>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column label="角色">
          <template #default="{ row }">
            <el-space>
              <el-tag v-for="role in (row.roles || [])" :key="role" type="info" size="small">{{ role }}</el-tag>
            </el-space>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-space>
              <el-button size="small" @click="handleEdit(row)">编辑</el-button>
              <el-button size="small" type="danger" plain @click="handleKick(row)">强制下线</el-button>
              <el-button
                size="small"
                :type="row.status === 1 ? 'warning' : 'success'"
                @click="handleToggleStatus(row)"
              >
                {{ row.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="paginationReactive.page"
          v-model:page-size="paginationReactive.pageSize"
          :total="paginationReactive.itemCount"
          :page-sizes="[5, 10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="showModal" :title="isEdit ? '编辑用户' : '新增用户'" width="680px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" placeholder="请输入用户名" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item v-if="!isEdit" label="密码" prop="password">
              <el-input v-model="form.password" type="password" placeholder="请输入密码" />
            </el-form-item>
            <el-form-item v-else label="密码">
              <el-space>
                <el-input v-model="passwordPlaceholder" disabled placeholder="密码已加密" style="width: 150px" />
                <el-button type="warning" size="small" :loading="resettingPassword" @click="handleResetPasswordClick">重置密码</el-button>
              </el-space>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item v-if="!isEdit" label="角色" prop="roleCode">
          <el-select v-model="form.roleCode" :options="roleOptions" placeholder="请选择角色" style="width: 100%" />
        </el-form-item>
        <el-form-item v-else label="角色" prop="roleCode">
          <el-tag type="info">{{ getRoleLabel(form.roleCode) }}</el-tag>
        </el-form-item>

        <!-- 头像上传 -->
        <el-form-item label="头像" prop="avatar">
          <el-space direction="vertical" alignment="flex-start">
            <el-upload
              :action="uploadUrl"
              :headers="uploadHeaders"
              :data="{ folder: 'avatars' }"
              name="file"
              accept="image/*"
              :limit="1"
              :file-list="fileList"
              list-type="picture-card"
              :before-upload="handleBeforeUpload"
              :on-success="handleUploadSuccess"
              :on-remove="handleUploadRemove"
              :on-error="handleUploadError"
              :class="{ 'hide-upload': fileList.length > 0 }"
            >
              <el-icon><Plus /></el-icon>
              <div style="font-size: 12px;">上传头像</div>
            </el-upload>
            <el-text type="info" size="small">支持 JPG、PNG 格式，建议尺寸 200x200</el-text>
          </el-space>
        </el-form-item>

        <el-form-item>
          <el-space>
            <el-button type="primary" :loading="submitting" @click="handleSubmit">提交</el-button>
            <el-button @click="showModal = false">取消</el-button>
          </el-space>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, h, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { getUserList, createUser, updateUser, deleteUser, updateUserStatus, resetUserPassword, kickUser } from '@/api/user'
import { getToken } from '@/utils/auth'

const message = ElMessage
const passwordPlaceholder = ref('********')
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

const paginationReactive = reactive({
  page: 1,
  pageSize: 5,
  itemCount: 0
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
function handleBeforeUpload(file) {
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

// 上传成功回调
function handleUploadSuccess(response, file) {
  if (response.code === 200) {
    form.avatar = response.data.fileUrl
    message.success('头像上传成功')
  } else {
    message.error(response.message || '上传失败')
    fileList.value = []
  }
}

// 上传失败回调
function handleUploadError() {
  message.error('头像上传失败')
}

// 移除图片回调
function handleUploadRemove() {
  form.avatar = ''
  fileList.value = []
}

function handleSizeChange(size) {
  paginationReactive.pageSize = size
  fetchUsers()
}

function handlePageChange(page) {
  paginationReactive.page = page
  fetchUsers()
}

onMounted(() => {
  fetchUsers()
})

async function fetchUsers() {
  loading.value = true
  try {
    const params = {
      pageNum: paginationReactive.page,
      pageSize: paginationReactive.pageSize,
      ...buildSearchParams()
    }
    const res = await getUserList(params)
    users.value = res.records || []
    paginationReactive.itemCount = res.total || 0
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
  paginationReactive.page = 1
  fetchUsers()
}

function handleReset() {
  searchForm.username = ''
  searchForm.phone = ''
  searchForm.role = null
  searchForm.status = null
  paginationReactive.page = 1
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
      name: '头像',
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

  try {
    await ElMessageBox.confirm(
      '确定要将该用户的密码重置为默认密码 "123456" 吗？',
      '确认重置密码',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    resettingPassword.value = true
    await resetUserPassword(currentId.value, '123456')
    message.success('密码已重置为 123456')
  } catch (error) {
    if (error !== 'cancel') {
      message.error(error.message || '密码重置失败')
    }
  } finally {
    resettingPassword.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm(
    `确定要删除用户 "${row.username}" 吗？`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteUser(row.id)
      message.success('删除成功')
      fetchUsers()
    } catch (error) {
      message.error('删除失败')
    }
  }).catch(() => {})
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

function handleKick(row) {
  ElMessageBox.confirm(
    `确定要强制用户 "${row.username}" 下线吗？该用户的所有设备将被要求重新登录。`,
    '确认强制下线',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await kickUser(row.id)
      message.success('操作成功，该用户已被强制下线')
    } catch (error) {
      message.error(error.message || '操作失败')
    }
  }).catch(() => {})
}
</script>

<style scoped>
.users-page {
  padding: 0;
}

.card-header {
  font-weight: bold;
  font-size: 16px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.no-avatar {
  width: 60px;
  height: 60px;
  background: #f0f0f0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 12px;
  margin: 0 auto;
}

/* 上传成功后隐藏上传按钮 */
:deep(.hide-upload .el-upload--picture-card) {
  display: none;
}
</style>
