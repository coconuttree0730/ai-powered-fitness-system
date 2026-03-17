<template>
  <div class="courses-page">
    <div class="page-header">
      <h2>课程列表</h2>
    </div>
    
    <n-space class="filters" justify="space-between">
      <n-select v-model:value="filters.category" :options="categoryOptions" placeholder="选择分类" clearable style="width: 200px" />
      <n-input v-model:value="filters.keyword" placeholder="搜索课程" clearable style="width: 300px" />
    </n-space>

    <n-grid :cols="3" :x-gap="20" :y-gap="20" style="margin-top: 20px">
      <n-grid-item v-for="course in courses" :key="course.id">
        <n-card hoverable @click="goToDetail(course.id)">
          <template #cover>
            <img :src="course.imageUrl || '/placeholder.jpg'" :alt="course.courseName" style="height: 180px; object-fit: cover" />
          </template>
          <h3>{{ course.courseName }}</h3>
          <p class="description">{{ course.description }}</p>
          <n-space justify="space-between" align="center">
            <n-tag :type="getCategoryType(course.category)">{{ course.category }}</n-tag>
            <span class="remaining">剩余: {{ course.remainingCount || 0 }}/{{ course.capacity }}</span>
          </n-space>
          <div class="time">
            <n-icon><CalendarOutline /></n-icon>
            <span>{{ formatTime(course.startTime) }}</span>
          </div>
        </n-card>
      </n-grid-item>
    </n-grid>

    <n-pagination
      v-model:page="pagination.page"
      :page-count="pagination.totalPages"
      style="margin-top: 24px; justify-content: center"
      @update:page="fetchCourses"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { CalendarOutline } from '@vicons/ionicons5'
import { getPublicCourses } from '@/api/course'

const router = useRouter()
const message = useMessage()

const courses = ref([])
const filters = reactive({
  category: null,
  keyword: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 9,
  total: 0,
  totalPages: 0
})

const categoryOptions = [
  { label: '瑜伽', value: 'YOGA' },
  { label: '普拉提', value: 'PILATES' },
  { label: 'HIIT', value: 'HIIT' },
  { label: '力量训练', value: 'STRENGTH' },
  { label: '有氧训练', value: 'CARDIO' },
  { label: '舞蹈', value: 'DANCE' }
]

onMounted(() => {
  fetchCourses()
})

watch([() => filters.category, () => filters.keyword], () => {
  pagination.page = 1
  fetchCourses()
})

async function fetchCourses() {
  try {
    const res = await getPublicCourses({
      pageNum: pagination.page,
      pageSize: pagination.pageSize,
      category: filters.category,
      keyword: filters.keyword
    })
    if (res.data) {
      courses.value = res.data.records || res.data || []
      pagination.total = res.data.total || courses.value.length
      pagination.totalPages = res.data.pages || Math.ceil(pagination.total / pagination.pageSize)
    }
  } catch (error) {
    console.error('获取课程列表失败:', error)
  }
}

function getCategoryType(category) {
  const types = {
    YOGA: 'success',
    PILATES: 'info',
    HIIT: 'warning',
    STRENGTH: 'error',
    CARDIO: 'primary',
    DANCE: 'default'
  }
  return types[category] || 'default'
}

function formatTime(time) {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function goToDetail(id) {
  router.push(`/courses/${id}`)
}
</script>

<style scoped>
.courses-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
}

.filters {
  margin-bottom: 20px;
}

.description {
  color: #666;
  font-size: 14px;
  margin: 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.remaining {
  color: #1890ff;
  font-size: 14px;
}

.time {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  color: #666;
  font-size: 14px;
}
</style>
