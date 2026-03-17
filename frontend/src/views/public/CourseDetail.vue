<template>
  <div class="course-detail-page">
    <n-spin :show="loading">
      <template v-if="course">
        <n-card>
          <template #cover>
            <img :src="course.imageUrl || '/placeholder.jpg'" :alt="course.courseName" style="height: 300px; object-fit: cover" />
          </template>
          <n-page-header :title="course.courseName" @back="goBack">
            <template #extra>
              <n-button type="primary" :disabled="course.remainingCount <= 0" @click="handleBooking">
                {{ course.remainingCount > 0 ? '立即预约' : '已满' }}
              </n-button>
            </template>
          </n-page-header>
          <n-descriptions label-placement="left" :column="2" style="margin-top: 20px">
            <n-descriptions-item label="课程分类">
              <n-tag :type="getCategoryType(course.category)">{{ course.category }}</n-tag>
            </n-descriptions-item>
            <n-descriptions-item label="教练">{{ course.coachName }}</n-descriptions-item>
            <n-descriptions-item label="开始时间">{{ formatTime(course.startTime) }}</n-descriptions-item>
            <n-descriptions-item label="结束时间">{{ formatTime(course.endTime) }}</n-descriptions-item>
            <n-descriptions-item label="容量">{{ course.capacity }}人</n-descriptions-item>
            <n-descriptions-item label="剩余名额">{{ course.remainingCount }}人</n-descriptions-item>
          </n-descriptions>
          <n-divider />
          <h4>课程介绍</h4>
          <p class="description">{{ course.description || '暂无介绍' }}</p>
        </n-card>
      </template>
    </n-spin>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { getCourseDetail, bookCourse } from '@/api/course'

const route = useRoute()
const router = useRouter()
const message = useMessage()

const loading = ref(false)
const course = ref(null)

onMounted(() => {
  fetchCourse()
})

async function fetchCourse() {
  loading.value = true
  try {
    const res = await getCourseDetail(route.params.id)
    course.value = res.data
  } catch (error) {
    message.error('获取课程详情失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

async function handleBooking() {
  try {
    await bookCourse(route.params.id)
    message.success('预约成功')
    fetchCourse()
  } catch (error) {
    message.error(error.message || '预约失败')
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
  return new Date(time).toLocaleString('zh-CN')
}

function goBack() {
  router.back()
}
</script>

<style scoped>
.course-detail-page {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.description {
  color: #666;
  line-height: 1.8;
}
</style>
