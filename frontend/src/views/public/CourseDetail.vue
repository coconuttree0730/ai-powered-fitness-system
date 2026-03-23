<template>
  <div class="course-detail-page">
    <el-card v-loading="loading" v-if="course">
      <template #header>
        <div class="course-header">
          <el-page-header @back="goBack" :title="course.courseName">
            <template #content>
              <el-tag :type="getCategoryType(course.category)" size="large" style="margin-left: 10px;">
                {{ getCategoryLabel(course.category) }}
              </el-tag>
            </template>
            <template #extra>
              <el-button
                type="primary"
                :disabled="course.remainingCount <= 0"
                @click="handleBooking"
              >
                {{ course.remainingCount > 0 ? '立即预约' : '已满' }}
              </el-button>
            </template>
          </el-page-header>
        </div>
      </template>

      <!-- 课程封面 -->
      <div class="course-cover">
        <el-image
          :src="course.imageUrl || '/placeholder.jpg'"
          :alt="course.courseName"
          style="width: 100%; height: 300px; object-fit: cover; border-radius: 8px;"
        />
      </div>

      <!-- 课程信息 -->
      <el-descriptions label-width="100px" :column="2" style="margin-top: 20px" border>
        <el-descriptions-item label="课程分类">
          <el-tag :type="getCategoryType(course.category)">{{ getCategoryLabel(course.category) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="教练">{{ course.coachName }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ formatTime(course.startTime) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ formatTime(course.endTime) }}</el-descriptions-item>
        <el-descriptions-item label="容量">{{ course.capacity }}人</el-descriptions-item>
        <el-descriptions-item label="剩余名额">{{ course.remainingCount }}人</el-descriptions-item>
      </el-descriptions>

      <el-divider />

      <!-- 课程介绍 -->
      <div class="course-intro">
        <h4>课程介绍</h4>
        <p class="description">{{ course.description || '暂无介绍' }}</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCourseDetail, bookCourse } from '@/api/course'

const route = useRoute()
const router = useRouter()
const message = ElMessage

const loading = ref(false)
const course = ref(null)

const categoryMap = {
  YOGA: { label: '瑜伽', type: 'success' },
  PILATES: { label: '普拉提', type: 'primary' },
  HIIT: { label: 'HIIT', type: 'danger' },
  STRENGTH: { label: '力量训练', type: 'warning' },
  SPINNING: { label: '动感单车', type: 'info' },
  CARDIO: { label: '有氧运动', type: 'success' },
  DANCE: { label: '舞蹈', type: 'primary' }
}

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
  return categoryMap[category]?.type || 'info'
}

function getCategoryLabel(category) {
  return categoryMap[category]?.label || category
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

.course-header {
  display: flex;
  align-items: center;
}

.course-cover {
  margin-bottom: 20px;
}

.course-intro h4 {
  margin-bottom: 12px;
  color: #333;
}

.description {
  color: #666;
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>
