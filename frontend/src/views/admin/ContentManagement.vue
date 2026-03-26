<template>
  <div class="content-management-page">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6" v-for="stat in stats" :key="stat.title">
        <el-card class="stat-card" :body-style="{ padding: '20px' }">
          <div class="stat-content">
            <div class="stat-icon" :style="{ background: stat.color }">
              <el-icon :size="24"><component :is="stat.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-title">{{ stat.title }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 子模块标签页 -->
    <el-card>
      <el-tabs v-model="activeTab" type="border-card" @tab-change="handleTabChange">
        <!-- 轮播图管理 -->
        <el-tab-pane name="banner">
          <template #label>
            <span class="tab-label">
              <el-icon><Picture /></el-icon>轮播图管理
            </span>
          </template>
          <div class="tab-content">
            <el-row :gutter="20" align="middle" class="tab-toolbar">
              <el-col :span="18">
                <el-space>
                  <el-input
                    v-model="bannerSearch.title"
                    placeholder="搜索标题"
                    clearable
                    style="width: 200px"
                  />
                  <el-select v-model="bannerSearch.status" placeholder="全部状态" clearable style="width: 120px">
                    <el-option label="显示中" :value="1" />
                    <el-option label="已隐藏" :value="0" />
                  </el-select>
                  <el-button type="primary" @click="fetchBannerData">
                    <el-icon><Search /></el-icon>搜索
                  </el-button>
                </el-space>
              </el-col>
              <el-col :span="6" style="text-align: right">
                <el-button type="danger" @click="handleBatchDelete" :disabled="selectedBanners.length === 0" style="margin-right: 10px">
                  <el-icon><Delete /></el-icon>批量删除
                </el-button>
                <el-button type="primary" @click="handleAddBanner">
                  <el-icon><Plus /></el-icon>新增轮播图
                </el-button>
              </el-col>
            </el-row>

            <el-table :data="filteredBannerData" v-loading="loading" stripe @selection-change="handleSelectionChange">
              <el-table-column type="selection" width="55" />
              <el-table-column type="index" width="50" />
              <el-table-column label="轮播图" width="200">
                <template #default="{ row }">
                  <el-image
                    :src="row.imageUrl"
                    :preview-src-list="[row.imageUrl]"
                    fit="cover"
                    style="width: 180px; height: 80px; border-radius: 4px"
                  />
                </template>
              </el-table-column>
              <el-table-column prop="title" label="标题" min-width="150" />
              <el-table-column prop="subtitle" label="副标题" min-width="150" show-overflow-tooltip />
              <el-table-column prop="link" label="链接" min-width="150" show-overflow-tooltip />
              <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-switch
                    v-model="row.status"
                    :active-value="1"
                    :inactive-value="0"
                    @change="handleBannerStatusChange(row)"
                  />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" link @click="handleEditBanner(row)">
                    <el-icon><Edit /></el-icon>编辑
                  </el-button>
                  <el-button type="danger" link @click="handleDeleteBanner(row)">
                    <el-icon><Delete /></el-icon>删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <!-- 公告管理 -->
        <el-tab-pane name="notice">
          <template #label>
            <span class="tab-label">
              <el-icon><Bell /></el-icon>公告管理
            </span>
          </template>
          <div class="tab-content">
            <el-row :gutter="20" align="middle" class="tab-toolbar">
              <el-col :span="18">
                <el-space>
                  <el-input
                    v-model="noticeSearch.title"
                    placeholder="搜索标题"
                    clearable
                    style="width: 200px"
                  />
                  <el-select v-model="noticeSearch.type" placeholder="全部类型" clearable style="width: 120px">
                    <el-option label="系统公告" value="SYSTEM" />
                    <el-option label="活动通知" value="ACTIVITY" />
                    <el-option label="重要提醒" value="IMPORTANT" />
                  </el-select>
                  <el-button type="primary" @click="fetchNoticeData">
                    <el-icon><Search /></el-icon>搜索
                  </el-button>
                </el-space>
              </el-col>
              <el-col :span="6" style="text-align: right">
                <el-button type="primary" @click="handleAddNotice">
                  <el-icon><Plus /></el-icon>新增公告
                </el-button>
              </el-col>
            </el-row>

            <el-table :data="noticeData" v-loading="loading" stripe>
              <el-table-column type="index" width="50" />
              <el-table-column prop="title" label="公告标题" min-width="200" />
              <el-table-column prop="type" label="类型" width="100">
                <template #default="{ row }">
                  <el-tag :type="getNoticeTypeTag(row.type)">{{ getNoticeTypeLabel(row.type) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="publishTime" label="发布时间" width="160" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.status === 'PUBLISHED' ? 'success' : 'info'">
                    {{ row.status === 'PUBLISHED' ? '已发布' : '草稿' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="viewCount" label="浏览量" width="100" align="center" />
              <el-table-column label="操作" width="180" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" link @click="handleEditNotice(row)">
                    <el-icon><Edit /></el-icon>编辑
                  </el-button>
                  <el-button type="success" link v-if="row.status === 'DRAFT'" @click="handlePublishNotice(row)">
                    <el-icon><Promotion /></el-icon>发布
                  </el-button>
                  <el-button type="danger" link @click="handleDeleteNotice(row)">
                    <el-icon><Delete /></el-icon>删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <!-- 会员卡展示管理 -->
        <el-tab-pane name="membership-display">
          <template #label>
            <span class="tab-label">
              <el-icon><CreditCard /></el-icon>会员卡展示
            </span>
          </template>
          <div class="tab-content">
            <el-row :gutter="20" align="middle" class="tab-toolbar">
              <el-col :span="18">
                <el-space>
                  <el-input
                    v-model="membershipDisplaySearch.name"
                    placeholder="搜索会员卡名称"
                    clearable
                    style="width: 200px"
                  />
                  <el-select v-model="membershipDisplaySearch.type" placeholder="全部类型" clearable style="width: 120px">
                    <el-option label="月卡" value="MONTH" />
                    <el-option label="季卡" value="QUARTER" />
                    <el-option label="年卡" value="YEAR" />
                  </el-select>
                  <el-button type="primary" @click="fetchMembershipDisplayData">
                    <el-icon><Search /></el-icon>搜索
                  </el-button>
                </el-space>
              </el-col>
              <el-col :span="6" style="text-align: right">
                <el-button type="primary" @click="handleAddMembershipDisplay">
                  <el-icon><Plus /></el-icon>配置展示
                </el-button>
              </el-col>
            </el-row>

            <el-row :gutter="20" class="card-display-grid">
              <el-col :span="8" v-for="item in membershipDisplayData" :key="item.id">
                <el-card class="membership-display-card" :body-style="{ padding: '0' }">
                  <div class="card-preview" :style="{ background: item.backgroundColor }">
                    <div class="card-icon">
                      <el-icon :size="40"><component :is="item.icon || 'CreditCard'" /></el-icon>
                    </div>
                    <div class="card-info">
                      <div class="card-name">{{ item.name }}</div>
                      <div class="card-price">¥{{ item.price }}</div>
                      <div class="card-validity">有效期 {{ item.validityDays }} 天</div>
                    </div>
                  </div>
                  <div class="card-actions">
                    <el-button type="primary" link @click="handleEditMembershipDisplay(item)">
                      <el-icon><Edit /></el-icon>编辑
                    </el-button>
                    <el-button type="danger" link @click="handleDeleteMembershipDisplay(item)">
                      <el-icon><Delete /></el-icon>删除
                    </el-button>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>

        <!-- 积分商城展示管理 -->
        <el-tab-pane name="store-display">
          <template #label>
            <span class="tab-label">
              <el-icon><ShoppingCart /></el-icon>积分商城展示
            </span>
          </template>
          <div class="tab-content">
            <el-row :gutter="20" align="middle" class="tab-toolbar">
              <el-col :span="18">
                <el-space>
                  <el-input
                    v-model="storeDisplaySearch.name"
                    placeholder="搜索商品名称"
                    clearable
                    style="width: 200px"
                  />
                  <el-select v-model="storeDisplaySearch.category" placeholder="全部分类" clearable style="width: 120px">
                    <el-option label="运动补剂" value="SUPPLEMENT" />
                    <el-option label="运动装备" value="EQUIPMENT" />
                    <el-option label="运动服饰" value="CLOTHING" />
                    <el-option label="服务类" value="SERVICE" />
                  </el-select>
                  <el-button type="primary" @click="fetchStoreDisplayData">
                    <el-icon><Search /></el-icon>搜索
                  </el-button>
                </el-space>
              </el-col>
              <el-col :span="6" style="text-align: right">
                <el-button type="primary" @click="handleAddStoreDisplay">
                  <el-icon><Plus /></el-icon>配置展示
                </el-button>
              </el-col>
            </el-row>

            <el-row :gutter="20" class="product-display-grid">
              <el-col :span="6" v-for="item in storeDisplayData" :key="item.id">
                <el-card class="product-display-card" :body-style="{ padding: '0' }">
                  <el-image :src="item.image" fit="cover" class="product-image" />
                  <div class="product-info">
                    <div class="product-name">{{ item.name }}</div>
                    <div class="product-points">
                      <el-icon><Coin /></el-icon>
                      <span>{{ item.points }} 积分</span>
                    </div>
                    <div class="product-stock">库存: {{ item.stock }}</div>
                  </div>
                  <div class="product-actions">
                    <el-button type="primary" link @click="handleEditStoreDisplay(item)">
                      <el-icon><Edit /></el-icon>编辑
                    </el-button>
                    <el-button type="danger" link @click="handleDeleteStoreDisplay(item)">
                      <el-icon><Delete /></el-icon>删除
                    </el-button>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 轮播图弹窗 -->
    <el-dialog
      v-model="bannerDialogVisible"
      :title="isEdit ? '编辑轮播图' : '新增轮播图'"
      width="600px"
    >
      <el-form ref="bannerFormRef" :model="bannerForm" :rules="bannerRules" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="bannerForm.title" placeholder="请输入轮播图标题" />
        </el-form-item>
        <el-form-item label="副标题" prop="subtitle">
          <el-input v-model="bannerForm.subtitle" placeholder="请输入副标题" />
        </el-form-item>
        <el-form-item label="图片" prop="imageUrl">
          <el-upload
            class="banner-uploader"
            action="/api/v1/files/upload"
            :data="{ folder: 'banner' }"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleBannerUploadSuccess"
            :on-error="handleBannerUploadError"
            :before-upload="beforeBannerUpload"
            accept="image/jpeg,image/png,image/gif,image/webp"
          >
            <img v-if="bannerForm.imageUrl" :src="bannerForm.imageUrl" class="banner-preview" />
            <div v-else class="banner-uploader-placeholder">
              <el-icon class="banner-uploader-icon"><Plus /></el-icon>
              <div class="upload-text">点击上传图片</div>
            </div>
          </el-upload>
          <div class="upload-tip">建议尺寸 800x1000，支持 JPG、PNG、GIF、WebP 格式，最大 10MB</div>
        </el-form-item>
        <el-form-item label="链接" prop="link">
          <el-input v-model="bannerForm.link" placeholder="请输入跳转链接（可选）" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="bannerForm.sortOrder" :min="0" :max="999" />
          <span class="form-tip">数字越小排序越靠前</span>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="bannerForm.status">
            <el-radio :label="1">显示</el-radio>
            <el-radio :label="0">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bannerDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitBanner" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 公告弹窗 -->
    <el-dialog
      v-model="noticeDialogVisible"
      :title="isEdit ? '编辑公告' : '新增公告'"
      width="700px"
    >
      <el-form ref="noticeFormRef" :model="noticeForm" :rules="noticeRules" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="noticeForm.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="noticeForm.type" placeholder="请选择类型" style="width: 100%">
            <el-option label="系统公告" value="SYSTEM" />
            <el-option label="活动通知" value="ACTIVITY" />
            <el-option label="重要提醒" value="IMPORTANT" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="noticeForm.content"
            type="textarea"
            :rows="6"
            placeholder="请输入公告内容"
          />
        </el-form-item>
        <el-form-item label="发布时间" prop="publishTime">
          <el-date-picker
            v-model="noticeForm.publishTime"
            type="datetime"
            placeholder="选择发布时间"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="noticeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitNotice" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Picture, Bell, CreditCard, ShoppingCart, Search, Plus, Edit, Delete,
  Promotion, Coin
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import {
  getAllBanners,
  createBanner,
  updateBanner,
  deleteBanner,
  deleteBanners,
  updateBannerStatus
} from '@/api/banner'

// 获取认证信息
const authStore = useAuthStore()
const uploadHeaders = computed(() => ({
  Authorization: authStore.token ? `Bearer ${authStore.token}` : ''
}))

// 统计数据
const stats = ref([
  { title: '轮播图数量', value: 0, icon: 'Picture', color: '#1890ff' },
  { title: '公告数量', value: 12, icon: 'Bell', color: '#52c41a' },
  { title: '会员卡展示', value: 4, icon: 'CreditCard', color: '#faad14' },
  { title: '商城商品展示', value: 8, icon: 'ShoppingCart', color: '#722ed1' }
])

// 当前标签页
const activeTab = ref('banner')
const loading = ref(false)

// ========== 轮播图管理 ==========
const bannerSearch = reactive({ title: '', status: '' })
const bannerData = ref([])
const selectedBanners = ref([])
const bannerDialogVisible = ref(false)
const bannerFormRef = ref(null)
const bannerForm = reactive({
  id: null,
  title: '',
  subtitle: '',
  imageUrl: '',
  link: '',
  sortOrder: 0,
  status: 1
})
const bannerRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  imageUrl: [{ required: true, message: '请上传图片', trigger: 'change' }],
  sortOrder: [{ required: true, message: '请输入排序', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// 过滤后的轮播图数据
const filteredBannerData = computed(() => {
  return bannerData.value.filter(item => {
    const matchTitle = !bannerSearch.title || item.title.toLowerCase().includes(bannerSearch.title.toLowerCase())
    const matchStatus = bannerSearch.status === '' || item.status === bannerSearch.status
    return matchTitle && matchStatus
  })
})

// 获取轮播图数据
async function fetchBannerData() {
  loading.value = true
  try {
    const res = await getAllBanners()
    bannerData.value = res || []
    // 更新统计
    stats.value[0].value = bannerData.value.length
  } catch (error) {
    ElMessage.error('获取轮播图数据失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 选择变化
function handleSelectionChange(selection) {
  selectedBanners.value = selection
}

// 新增轮播图
function handleAddBanner() {
  isEdit.value = false
  Object.assign(bannerForm, {
    id: null, title: '', subtitle: '', imageUrl: '', link: '', sortOrder: 0, status: 1
  })
  bannerDialogVisible.value = true
}

// 编辑轮播图
function handleEditBanner(row) {
  isEdit.value = true
  Object.assign(bannerForm, {
    id: row.id,
    title: row.title,
    subtitle: row.subtitle,
    imageUrl: row.imageUrl,
    link: row.link,
    sortOrder: row.sortOrder,
    status: row.status
  })
  bannerDialogVisible.value = true
}

// 删除轮播图
async function handleDeleteBanner(row) {
  try {
    await ElMessageBox.confirm(`确定要删除轮播图 "${row.title}" 吗？`, '提示', { type: 'warning' })
    await deleteBanner(row.id)
    ElMessage.success('删除成功')
    fetchBannerData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
      console.error(error)
    }
  }
}

// 批量删除
async function handleBatchDelete() {
  if (selectedBanners.value.length === 0) {
    ElMessage.warning('请选择要删除的轮播图')
    return
  }
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedBanners.value.length} 个轮播图吗？`, '提示', { type: 'warning' })
    const ids = selectedBanners.value.map(item => item.id)
    await deleteBanners(ids)
    ElMessage.success('批量删除成功')
    fetchBannerData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
      console.error(error)
    }
  }
}

// 状态变化
async function handleBannerStatusChange(row) {
  try {
    await updateBannerStatus(row.id, row.status)
    ElMessage.success(`轮播图已${row.status === 1 ? '显示' : '隐藏'}`)
  } catch (error) {
    ElMessage.error('状态更新失败')
    console.error(error)
    // 恢复状态
    row.status = row.status === 1 ? 0 : 1
  }
}

// 上传成功
function handleBannerUploadSuccess(res) {
  if (res.code === 200 && res.data) {
    bannerForm.imageUrl = res.data.fileUrl || res.data.url
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error(res.message || '上传失败')
  }
}

// 上传失败
function handleBannerUploadError() {
  ElMessage.error('图片上传失败')
}

// 上传前校验
function beforeBannerUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isImage) {
    ElMessage.error('请上传图片文件')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过10MB')
    return false
  }
  return true
}

// 提交表单
async function handleSubmitBanner() {
  bannerFormRef.value?.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      if (isEdit.value) {
        await updateBanner(bannerForm.id, bannerForm)
        ElMessage.success('编辑成功')
      } else {
        await createBanner(bannerForm)
        ElMessage.success('新增成功')
      }
      bannerDialogVisible.value = false
      fetchBannerData()
    } catch (error) {
      ElMessage.error(isEdit.value ? '编辑失败' : '新增失败')
      console.error(error)
    } finally {
      submitting.value = false
    }
  })
}

// ========== 公告管理 ==========
const noticeSearch = reactive({ title: '', type: '' })
const noticeData = ref([])
const noticeDialogVisible = ref(false)
const noticeFormRef = ref(null)
const noticeForm = reactive({
  id: null,
  title: '',
  type: 'SYSTEM',
  content: '',
  publishTime: null,
  status: 'DRAFT'
})
const noticeRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

const noticeTypeMap = {
  SYSTEM: { label: '系统公告', type: 'primary' },
  ACTIVITY: { label: '活动通知', type: 'success' },
  IMPORTANT: { label: '重要提醒', type: 'danger' }
}

function getNoticeTypeLabel(type) {
  return noticeTypeMap[type]?.label || type
}

function getNoticeTypeTag(type) {
  return noticeTypeMap[type]?.type || ''
}

function fetchNoticeData() {
  loading.value = true
  setTimeout(() => {
    noticeData.value = [
      {
        id: 1,
        title: '春节营业时间调整通知',
        type: 'IMPORTANT',
        content: '春节期间营业时间调整为...',
        publishTime: '2024-01-15 10:00:00',
        status: 'PUBLISHED',
        viewCount: 1256
      },
      {
        id: 2,
        title: '新器械上线通知',
        type: 'SYSTEM',
        content: '本月新增多台高端有氧器械...',
        publishTime: '2024-01-10 14:30:00',
        status: 'PUBLISHED',
        viewCount: 892
      },
      {
        id: 3,
        title: '会员积分兑换活动',
        type: 'ACTIVITY',
        content: '积分兑换商品8折优惠活动开始...',
        publishTime: '2024-01-08 09:00:00',
        status: 'DRAFT',
        viewCount: 0
      }
    ]
    loading.value = false
  }, 300)
}

function handleAddNotice() {
  isEdit.value = false
  Object.assign(noticeForm, {
    id: null, title: '', type: 'SYSTEM', content: '', publishTime: null, status: 'DRAFT'
  })
  noticeDialogVisible.value = true
}

function handleEditNotice(row) {
  isEdit.value = true
  Object.assign(noticeForm, row)
  noticeDialogVisible.value = true
}

function handleDeleteNotice(row) {
  ElMessageBox.confirm(`确定要删除公告 "${row.title}" 吗？`, '提示', { type: 'warning' }).then(() => {
    ElMessage.success('删除成功')
    fetchNoticeData()
  })
}

function handlePublishNotice(row) {
  ElMessageBox.confirm(`确定要发布公告 "${row.title}" 吗？`, '提示', { type: 'info' }).then(() => {
    ElMessage.success('发布成功')
    fetchNoticeData()
  })
}

function handleSubmitNotice() {
  noticeFormRef.value?.validate((valid) => {
    if (valid) {
      submitting.value = true
      setTimeout(() => {
        ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
        noticeDialogVisible.value = false
        fetchNoticeData()
        submitting.value = false
      }, 500)
    }
  })
}

// ========== 会员卡展示管理 ==========
const membershipDisplaySearch = reactive({ name: '', type: '' })
const membershipDisplayData = ref([])

function fetchMembershipDisplayData() {
  loading.value = true
  setTimeout(() => {
    membershipDisplayData.value = [
      {
        id: 1,
        name: '至尊年卡',
        price: 3999,
        validityDays: 365,
        icon: 'Trophy',
        backgroundColor: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
      },
      {
        id: 2,
        name: '金卡季卡',
        price: 1299,
        validityDays: 90,
        icon: 'Medal',
        backgroundColor: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
      },
      {
        id: 3,
        name: '银卡月卡',
        price: 499,
        validityDays: 30,
        icon: 'Star',
        backgroundColor: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
      }
    ]
    loading.value = false
  }, 300)
}

function handleAddMembershipDisplay() {
  ElMessage.info('配置会员卡展示功能')
}

function handleEditMembershipDisplay(item) {
  ElMessage.info(`编辑会员卡: ${item.name}`)
}

function handleDeleteMembershipDisplay(item) {
  ElMessageBox.confirm(`确定要删除展示 "${item.name}" 吗？`, '提示', { type: 'warning' }).then(() => {
    ElMessage.success('删除成功')
    fetchMembershipDisplayData()
  })
}

// ========== 积分商城展示管理 ==========
const storeDisplaySearch = reactive({ name: '', category: '' })
const storeDisplayData = ref([])

function fetchStoreDisplayData() {
  loading.value = true
  setTimeout(() => {
    storeDisplayData.value = [
      {
        id: 1,
        name: '乳清蛋白粉',
        points: 3500,
        stock: 89,
        image: 'https://images.unsplash.com/photo-1579722821273-0f6c7d44362f?w=400&h=300&fit=crop'
      },
      {
        id: 2,
        name: '可调节哑铃',
        points: 8800,
        stock: 23,
        image: 'https://images.unsplash.com/photo-1517836357463-d25dfeac3438?w=400&h=300&fit=crop'
      },
      {
        id: 3,
        name: '专业运动鞋',
        points: 5200,
        stock: 56,
        image: 'https://images.unsplash.com/photo-1556906781-9a412961c28c?w=400&h=300&fit=crop'
      },
      {
        id: 4,
        name: 'TPE瑜伽垫',
        points: 1800,
        stock: 12,
        image: 'https://images.unsplash.com/photo-1583454110551-21f2fa2afe61?w=400&h=300&fit=crop'
      }
    ]
    loading.value = false
  }, 300)
}

function handleAddStoreDisplay() {
  ElMessage.info('配置积分商城展示功能')
}

function handleEditStoreDisplay(item) {
  ElMessage.info(`编辑商品: ${item.name}`)
}

function handleDeleteStoreDisplay(item) {
  ElMessageBox.confirm(`确定要删除展示 "${item.name}" 吗？`, '提示', { type: 'warning' }).then(() => {
    ElMessage.success('删除成功')
    fetchStoreDisplayData()
  })
}

// 通用
const isEdit = ref(false)
const submitting = ref(false)

function handleTabChange() {
  switch (activeTab.value) {
    case 'banner':
      fetchBannerData()
      break
    case 'notice':
      fetchNoticeData()
      break
    case 'membership-display':
      fetchMembershipDisplayData()
      break
    case 'store-display':
      fetchStoreDisplayData()
      break
  }
}

onMounted(() => {
  fetchBannerData()
})
</script>

<style scoped>
.content-management-page {
  padding: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
}

.stat-title {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
}

.tab-content {
  padding: 20px 0;
}

.tab-toolbar {
  margin-bottom: 20px;
}

/* 轮播图上传 */
.banner-uploader {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
  width: 100%;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--el-fill-color-light);
}

.banner-uploader:hover {
  border-color: var(--el-color-primary);
}

.banner-uploader-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.banner-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.upload-text {
  font-size: 14px;
  color: #8c939d;
}

.banner-preview {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-left: 10px;
}

/* 会员卡展示卡片 */
.card-display-grid {
  margin-top: 20px;
}

.membership-display-card {
  margin-bottom: 20px;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s;
}

.membership-display-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.card-preview {
  padding: 30px 20px;
  color: #fff;
  text-align: center;
}

.card-icon {
  margin-bottom: 15px;
}

.card-name {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 8px;
}

.card-price {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 5px;
}

.card-validity {
  font-size: 14px;
  opacity: 0.9;
}

.card-actions {
  padding: 15px;
  display: flex;
  justify-content: center;
  gap: 20px;
  background: #f5f7fa;
}

/* 积分商城展示卡片 */
.product-display-grid {
  margin-top: 20px;
}

.product-display-card {
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
}

.product-display-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.product-image {
  width: 100%;
  height: 150px;
  object-fit: cover;
}

.product-info {
  padding: 15px;
}

.product-name {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-points {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #faad14;
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
}

.product-stock {
  font-size: 13px;
  color: #909399;
}

.product-actions {
  padding: 10px 15px;
  display: flex;
  justify-content: center;
  gap: 15px;
  border-top: 1px solid #ebeef5;
}
</style>
