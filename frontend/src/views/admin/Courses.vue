<template>
  <div class="admin-courses">
    <n-card title="课程管理">
      <template #header-extra>
        <n-button type="primary" @click="handleAdd">新增课程</n-button>
      </template>
      <n-data-table :columns="columns" :data="courses" :loading="loading" :pagination="pagination" :row-key="row => row.id" />
    </n-card>

    <n-modal v-model:show="showModal" preset="card" :title="isEdit ? '编辑课程' : '新增课程'" style="width: 600px">
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="left" label-width="100">
        <n-form-item label="课程名称" path="courseName">
          <n-input v-model:value="form.courseName" placeholder="请输入课程名称" />
        </n-form-item>
        <n-form-item label="分类" path="category">
          <n-select v-model:value="form.category" :options="categoryOptions" placeholder="请选择分类" />
        </n-form-item>
        <n-grid :cols="2" :x-gap="12">
          <n-grid-item>
            <n-form-item label="开始时间" path="startTime">
              <n-date-picker v-model:value="form.startTime" type="datetime" style="width: 100%" />
            </n-form-item>
          </n-grid-item>
          <n-grid-item>
            <n-form-item label="结束时间" path="endTime">
              <n-date-picker v-model:value="form.endTime" type="datetime" style="width: 100%" />
            </n-form-item>
          </n-grid-item>
        </n-grid>
        <n-form-item label="容量" path="capacity">
          <n-input-number v-model:value="form.capacity" :min="1" style="width: 100%" />
        </n-form-item>
        <n-form-item label="描述" path="description">
          <n-input v-model:value="form.description" type="textarea" :rows="3" placeholder="请输入课程描述" />
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
import { createCourse, updateCourse, deleteCourse } from '@/api/course'
import request from '@/utils/request'

const message = useMessage()
const dialog = useDialog()
const loading = ref(false)
const submitting = ref(false)
const showModal = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const courses = ref([])
const currentId = ref(null)

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  onChange: (page) => {
    pagination.page = page
    fetchCourses()
  }
})

const form = reactive({
  courseName: '',
  category: null,
  startTime: null,
  endTime: null,
  capacity: 20,
  description: ''
})

const rules = {
  courseName: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change', type: 'number' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change', type: 'number' }],
  capacity: [{ required: true, message: '请输入容量', trigger: 'blur', type: 'number' }]
}

const categoryOptions = [
  { label: '瑜伽', value: 'YOGA' },
  { label: '普拉提', value: 'PILATES' },
  { label: 'HIIT', value: 'HIIT' },
  { label: '力量训练', value: 'STRENGTH' },
  { label: '动感单车', value: 'SPINNING' }
]

const columns = [
  { title: '课程名称', key: 'courseName' },
  {
    title: '分类',
    key: 'category',
    render: (row) => {
      const item = categoryOptions.find(c => c.value === row.category)
      return h(NTag, { type: 'info' }, () => item?.label || row.category)
    }
  },
  { title: '教练', key: 'coachName' },
  { title: '开始时间', key: 'startTime', render: (row) => formatTime(row.startTime) },
  { title: '容量', key: 'capacity' },
  { title: '预约数', key: 'bookingCount' },
  {
    title: '操作',
    key: 'actions',
    render: (row) => h(NSpace, null, () => [
      h(NButton, { size: 'small', onClick: () => handleEdit(row) }, () => '编辑'),
      h(NButton, { size: 'small', type: 'error', onClick: () => handleDelete(row) }, () => '删除')
    ])
  }
]

onMounted(() => {
  fetchCourses()
})

async function fetchCourses() {
  loading.value = true
  try {
    const res = await request({
      url: '/courses/list',
      method: 'get',
      params: { pageNum: pagination.page, pageSize: pagination.pageSize }
    })
    courses.value = res.records || []
    pagination.itemCount = res.total || 0
  } catch (error) {
    message.error('获取课程列表失败')
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  isEdit.value = false
  currentId.value = null
  Object.assign(form, { courseName: '', category: null, startTime: null, endTime: null, capacity: 20, description: '' })
  showModal.value = true
}

function handleEdit(row) {
  isEdit.value = true
  currentId.value = row.id
  Object.assign(form, {
    courseName: row.courseName,
    category: row.category,
    startTime: row.startTime ? new Date(row.startTime).getTime() : null,
    endTime: row.endTime ? new Date(row.endTime).getTime() : null,
    capacity: row.capacity,
    description: row.description || ''
  })
  showModal.value = true
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    submitting.value = true
    
    const data = {
      ...form,
      startTime: form.startTime ? new Date(form.startTime).toISOString() : null,
      endTime: form.endTime ? new Date(form.endTime).toISOString() : null
    }
    
    if (isEdit.value) {
      await updateCourse(currentId.value, data)
      message.success('更新成功')
    } else {
      await createCourse(data)
      message.success('创建成功')
    }
    
    showModal.value = false
    fetchCourses()
  } catch (error) {
    message.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

function handleDelete(row) {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除课程 "${row.courseName}" 吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteCourse(row.id)
        message.success('删除成功')
        fetchCourses()
      } catch (error) {
        message.error('删除失败')
      }
    }
  })
}

function formatTime(time) {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.admin-courses {
  padding: 0;
}
</style>
