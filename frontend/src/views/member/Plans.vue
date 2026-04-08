<template>
  <div class="fitness-plans-page">
    <div class="page-header">
      <h2>我的健身计划</h2>
      <p class="page-desc">在「健小助」中生成的健身计划会保存在此处</p>
    </div>

    <n-spin :show="loading">
      <n-empty v-if="!loading && plans.length === 0" description="暂无健身计划" size="large">
        <template #extra>
          <n-button type="primary" @click="$router.push('/member/assistant')">
            去健小助生成计划
          </n-button>
        </template>
      </n-empty>

      <n-data-table
        v-else
        :columns="columns"
        :data="plans"
        :pagination="false"
        :bordered="false"
        :row-key="row => row.id"
        striped
        size="medium"
        class="plan-table"
      />
    </n-spin>

    <!-- 查看详情弹窗 - 独立内嵌视图 -->
    <n-modal
      v-model:show="showDetailModal"
      preset="card"
      :title="`健身训练计划 - ${formatDate(selectedPlan?.createTime)}`"
      :style="{ width: '90vw', maxWidth: '1000px' }"
      :bordered="false"
      class="plan-view-modal"
    >
      <div v-if="previewData" class="plan-view-content">
        <!-- 用户信息卡片 -->
        <div class="user-info-card">
          <div class="info-item">
            <span class="label">身高</span>
            <span class="value">{{ previewData.userInfo?.height || '--' }}</span>
          </div>
          <div class="info-item">
            <span class="label">体重</span>
            <span class="value">{{ previewData.userInfo?.weight || '--' }}</span>
          </div>
          <div class="info-item">
            <span class="label">BMI</span>
            <span class="value">{{ previewData.userInfo?.bmi || '--' }}</span>
          </div>
          <div class="info-item">
            <span class="label">目标</span>
            <n-tag size="small" type="info">{{ previewData.userInfo?.goal || '--' }}</n-tag>
          </div>
          <div class="info-item">
            <span class="label">强度</span>
            <n-tag size="small" :type="getIntensityType(previewData.userInfo?.intensity)">
              {{ previewData.userInfo?.intensity || '--' }}
            </n-tag>
          </div>
        </div>

        <!-- 星期标签页 -->
        <n-tabs v-model:value="activeDay" type="line" animated class="day-tabs">
          <n-tab-pane
            v-for="(day, index) in previewData.weeklyPlan"
            :key="index"
            :name="index"
            :tab="day.dayName"
          >
            <div class="day-content">
              <!-- 今日焦点 -->
              <div v-if="day.focus" class="focus-banner">
                <n-icon size="18" color="#f97316">
                  <InformationCircleOutline />
                </n-icon>
                <span>{{ day.focus }}</span>
              </div>

              <!-- 推荐课程 -->
              <div v-if="day.courses?.length" class="section">
                <div class="section-title">
                  <span>推荐课程</span>
                  <n-tag size="small" round>{{ day.courses.length }}个课程</n-tag>
                </div>
                <div class="courses-grid">
                  <div
                    v-for="course in day.courses"
                    :key="course.id"
                    class="course-card"
                    @click="handleCourseClick(course)"
                  >
                    <img :src="course.coverImage || getDefaultCourseImage()" :alt="course.name">
                    <div class="course-info">
                      <h4>{{ course.name }}</h4>
                      <p class="desc">{{ course.description }}</p>
                      <div class="meta">
                        <n-icon size="14"><TimeOutline /></n-icon>
                        <span>{{ course.duration }}分钟</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 训练动作 -->
              <div v-if="day.exercises?.length" class="section">
                <div class="section-title">
                  <span>训练动作</span>
                  <n-tag size="small" round type="warning">{{ day.exercises.length }}个动作</n-tag>
                </div>
                <div class="exercises-list">
                  <div
                    v-for="(exercise, idx) in day.exercises"
                    :key="idx"
                    class="exercise-item"
                  >
                    <div class="exercise-number">{{ String(idx + 1).padStart(2, '0') }}</div>
                    <div class="exercise-info">
                      <span class="name">{{ exercise.name }}</span>
                      <div class="tags">
                        <n-tag size="small" type="success">{{ exercise.sets }}组</n-tag>
                        <n-tag size="small" type="info">{{ exercise.reps }}次</n-tag>
                        <n-tag size="small" type="warning">休息{{ exercise.restSeconds }}s</n-tag>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 训练器械 -->
              <div v-if="day.equipment?.length" class="section">
                <div class="section-title">
                  <span>训练器械</span>
                </div>
                <div class="equipment-grid">
                  <div
                    v-for="(equip, idx) in day.equipment"
                    :key="idx"
                    class="equipment-item"
                  >
                    <img :src="equip.image || getDefaultEquipmentImage()" :alt="equip.name">
                    <span>{{ equip.name }}</span>
                  </div>
                </div>
              </div>
            </div>
          </n-tab-pane>
        </n-tabs>
      </div>
      <n-empty v-else description="计划数据加载失败" />
    </n-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, h } from 'vue'
import { NButton, NTag, useMessage, useDialog, NSpace, NTabs, NTabPane, NIcon } from 'naive-ui'
import { InformationCircleOutline, TimeOutline } from '@vicons/ionicons5'
import { getMyPlans, deletePlan, getPlanDetail } from '@/api/plan'

const message = useMessage()
const dialog = useDialog()

const loading = ref(false)
const plans = ref([])
const showDetailModal = ref(false)
const selectedPlan = ref(null)
const previewData = ref(null)
const activeDay = ref(0)

const columns = [
  {
    title: '序号',
    key: 'index',
    width: 70,
    render: (_, index) => index + 1
  },
  {
    title: '计划生成日期',
    key: 'createTime',
    width: 180,
    render: row => formatDateTime(row.createTime),
    sorter: (a, b) => new Date(a.createTime) - new Date(b.createTime)
  },
  {
    title: '身高',
    key: 'height',
    width: 80,
    render: row => row.height ? `${row.height}cm` : '-'
  },
  {
    title: '体重',
    key: 'weight',
    width: 80,
    render: row => row.weight ? `${row.weight}kg` : '-'
  },
  {
    title: '年龄',
    key: 'age',
    width: 70,
    render: row => row.age ? `${row.age}岁` : '-'
  },
  {
    title: '性别',
    key: 'gender',
    width: 80,
    render: row => {
      const map = { MALE: '男', FEMALE: '女', male: '男', female: '女' }
      return map[row.gender] || row.gender || '-'
    }
  },
  {
    title: '健身经验',
    key: 'experience',
    width: 100,
    render: row => {
      const exp = row.experience || ''
      if (!exp) return '-'
      if (exp.includes('初级') || exp === 'BEGINNER') return h(NTag, { size: 'small', type: 'success' }, () => '初级')
      if (exp.includes('中级') || exp === 'INTERMEDIATE') return h(NTag, { size: 'small', type: 'warning' }, () => '中级')
      if (exp.includes('高级') || exp === 'ADVANCED') return h(NTag, { size: 'small', type: 'error' }, () => '高级')
      return exp
    }
  },
  {
    title: '健身目标',
    key: 'fitnessGoal',
    width: 110,
    render: row => {
      const goal = row.fitnessGoal || row.goal || ''
      if (!goal) return '-'
      const colorMap = {
        '增肌': 'info', 'MUSCLE_GAIN': 'info',
        '减脂': 'success', 'FAT_LOSS': 'success',
        '塑形': 'warning', 'BODY_SHAPING': 'warning',
        '增强体能': 'error', 'ENDURANCE': 'error'
      }
      return h(NTag, { size: 'small', type: colorMap[goal] || 'default', bordered: false }, () => goal)
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    fixed: 'right',
    render: row => h(NSpace, {}, () => [
      h(NButton, {
        text: true,
        type: 'primary',
        size: 'small',
        onClick: () => handleView(row)
      }, () => '查看'),
      h(NButton, {
        text: true,
        type: 'error',
        size: 'small',
        onClick: () => handleDelete(row)
      }, () => '删除')
    ])
  }
]

onMounted(() => {
  fetchPlans()
})

async function fetchPlans() {
  loading.value = true
  try {
    const res = await getMyPlans()
    plans.value = Array.isArray(res) ? res : []
  } catch (error) {
    console.error('获取计划列表失败:', error)
    message.error('获取计划列表失败')
  } finally {
    loading.value = false
  }
}

async function handleView(plan) {
  try {
    previewData.value = null
    selectedPlan.value = null
    activeDay.value = 0

    const res = await getPlanDetail(plan.id)
    selectedPlan.value = res

    let data = res?.planDataJson
    if (data && typeof data === 'string') {
      try { data = JSON.parse(data) } catch (e) { data = null }
    }

    previewData.value = data
    showDetailModal.value = true
  } catch (error) {
    console.error('获取计划详情失败:', error)
    message.error('获取计划详情失败')
  }
}

function handleDelete(plan) {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除 ${formatDate(plan.createTime)} 生成的健身计划吗？删除后无法恢复。`,
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deletePlan(plan.id)
        message.success('删除成功')
        fetchPlans()
      } catch (error) {
        message.error('删除失败')
      }
    }
  })
}

function handleCourseClick(course) {
  message.info(`查看课程: ${course.name}`)
}

function getIntensityType(intensity) {
  if (!intensity) return 'default'
  if (intensity.includes('高')) return 'error'
  if (intensity.includes('中')) return 'warning'
  return 'success'
}

function getDefaultCourseImage() {
  return 'https://images.unsplash.com/photo-1571019614242-c5c5dee9f50b?w=400&h=250&fit=crop&q=80'
}

function getDefaultEquipmentImage() {
  return 'https://images.unsplash.com/photo-1597452485669-2c7bb5fef90d?w=100&h=100&fit=crop&q=80'
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

function formatDateTime(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}
</script>

<style scoped>
.fitness-plans-page {
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  font-size: 22px;
  font-weight: 700;
  color: #1a1a2e;
}

.page-desc {
  margin: 0;
  font-size: 14px;
  color: #9ca3af;
}

.plan-table {
  border-radius: 12px;
  overflow: hidden;
}

.plan-table :deep(.n-data-table-tr) {
  cursor: default;
}

.plan-table :deep(.n-data-table-th) {
  background: #f8fafc;
  font-weight: 600;
  color: #374151;
  font-size: 13px;
}

.plan-table :deep(.n-data-table-td) {
  font-size: 13px;
  color: #4b5563;
}

/* 弹窗样式 */
.plan-view-modal :deep(.n-card__content) {
  max-height: 75vh;
  overflow-y: auto;
  padding: 20px;
}

/* 用户信息卡片 */
.user-info-card {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  padding: 16px 20px;
  background: linear-gradient(135deg, #fff7ed 0%, #ffedd5 100%);
  border-radius: 12px;
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-item .label {
  font-size: 13px;
  color: #78716c;
}

.info-item .value {
  font-size: 14px;
  font-weight: 600;
  color: #ea580c;
}

/* 星期标签页 */
.day-tabs :deep(.n-tabs-nav) {
  margin-bottom: 16px;
}

.day-content {
  padding: 4px;
}

/* 焦点横幅 */
.focus-banner {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: #fff7ed;
  border-left: 4px solid #f97316;
  border-radius: 8px;
  margin-bottom: 20px;
  font-size: 14px;
  font-weight: 500;
  color: #c2410c;
}

/* 区块样式 */
.section {
  margin-bottom: 24px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 14px;
}

/* 课程网格 */
.courses-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.course-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
}

.course-card:hover {
  border-color: #f97316;
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.15);
  transform: translateY(-2px);
}

.course-card img {
  width: 100%;
  height: 140px;
  object-fit: cover;
}

.course-info {
  padding: 12px 14px;
}

.course-info h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 600;
  color: #1a1a2e;
}

.course-info .desc {
  margin: 0 0 10px 0;
  font-size: 12px;
  color: #6b7280;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-info .meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #f97316;
  font-weight: 500;
}

/* 动作列表 */
.exercises-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.exercise-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  transition: all 0.2s ease;
}

.exercise-item:hover {
  border-color: #f97316;
  background: #fff7ed;
}

.exercise-number {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f97316 0%, #fb923c 100%);
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  border-radius: 8px;
}

.exercise-info {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.exercise-info .name {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a2e;
}

.exercise-info .tags {
  display: flex;
  gap: 8px;
}

/* 器械网格 */
.equipment-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 12px;
}

.equipment-item {
  text-align: center;
  padding: 12px;
  background: #f9fafb;
  border-radius: 10px;
}

.equipment-item img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
  margin-bottom: 8px;
}

.equipment-item span {
  font-size: 12px;
  color: #4b5563;
  font-weight: 500;
}
</style>
