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

    <!-- 查看详情弹窗 - 使用 FitnessPlanPreview 组件渲染JSON数据 -->
    <n-modal
      v-model:show="showDetailModal"
      preset="card"
      title=""
      :style="{ width: '92vw', maxWidth: '1200px' }"
      :bordered="false"
      :segmented="{ content: true }"
      class="plan-detail-modal"
    >
      <template #header>
        <div class="modal-header">
          <span class="modal-title">健身训练计划</span>
          <span class="modal-date">{{ formatDateTime(selectedPlan?.createTime) }}</span>
        </div>
      </template>
      <FitnessPlanPreview
        v-if="selectedPlan && selectedPlan.planDataJson"
        :key="'preview-' + selectedPlan.id"
        :plan-data="selectedPlan.planDataJson"
        :is-embedded="true"
        @course-click="handleCourseClick"
      />
      <n-empty v-else description="计划数据加载失败" />
    </n-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, h, computed } from 'vue'
import { NButton, NTag, useMessage, useDialog, NSpace } from 'naive-ui'
import { getMyPlans, deletePlan, getPlanDetail } from '@/api/plan'
import FitnessPlanPreview from '@/components/FitnessPlanPreview.vue'

const message = useMessage()
const dialog = useDialog()

const loading = ref(false)
const plans = ref([])
const showDetailModal = ref(false)
const selectedPlan = ref(null)

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
    const res = await getPlanDetail(plan.id)
    selectedPlan.value = res
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

.plan-detail-modal :deep(.n-card-header) {
  padding: 16px 24px 12px;
  border-bottom: 1px solid #f0f2f5;
}

.modal-header {
  display: flex;
  align-items: center;
  gap: 16px;
}

.modal-title {
  font-size: 18px;
  font-weight: 700;
  color: #1a1a2e;
}

.modal-date {
  font-size: 13px;
  color: #9ca3af;
}

.plan-detail-modal :deep(.n-card__content) {
  padding: 0;
  max-height: 80vh;
  overflow-y: auto;
}
</style>
