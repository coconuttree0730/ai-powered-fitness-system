<template>
  <div class="fitness-plans">
    <n-space justify="space-between" style="margin-bottom: 20px">
      <h2>我的健身计划</h2>
      <n-button type="primary" @click="showGenerateModal = true">
        生成新计划
      </n-button>
    </n-space>

    <n-empty v-if="plans.length === 0" description="暂无健身计划">
      <template #extra>
        <n-button type="primary" @click="showGenerateModal = true">
          生成计划
        </n-button>
      </template>
    </n-empty>

    <n-grid :cols="2" :x-gap="20" :y-gap="20" v-else>
      <n-grid-item v-for="plan in plans" :key="plan.id">
        <n-card :title="plan.planName" hoverable>
          <template #header-extra>
            <n-button text type="error" @click="handleDelete(plan.id)">
              删除
            </n-button>
          </template>
          <p><strong>目标:</strong> {{ plan.goal }}</p>
          <p><strong>时长:</strong> {{ plan.duration }}天</p>
          <p><strong>创建时间:</strong> {{ formatTime(plan.createTime) }}</p>
          <n-button text type="primary" @click="viewDetail(plan)">
            查看详情
          </n-button>
        </n-card>
      </n-grid-item>
    </n-grid>

    <n-modal v-model:show="showGenerateModal" preset="card" title="生成健身计划" style="width: 500px">
      <n-form ref="formRef" :model="generateForm" :rules="rules">
        <n-form-item path="goal" label="健身目标">
          <n-select v-model:value="generateForm.goal" :options="goalOptions" placeholder="选择目标" />
        </n-form-item>
        <n-form-item path="bodyPart" label="训练部位">
          <n-select v-model:value="generateForm.bodyPart" :options="bodyPartOptions" placeholder="选择部位" />
        </n-form-item>
        <n-form-item path="experience" label="经验水平">
          <n-select v-model:value="generateForm.experience" :options="experienceOptions" placeholder="选择水平" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showGenerateModal = false">取消</n-button>
          <n-button type="primary" @click="handleGenerate" :loading="generating">
            生成
          </n-button>
        </n-space>
      </template>
    </n-modal>

    <n-modal v-model:show="showDetailModal" preset="card" title="计划详情" style="width: 700px">
      <n-timeline v-if="selectedPlan">
        <n-timeline-item
          v-for="detail in planDetails"
          :key="detail.id"
          :type="'info'"
          :title="`第${detail.dayOfWeek}天`"
        >
          <h4>{{ detail.exerciseName }}</h4>
          <p>组数: {{ detail.sets }} | 次数: {{ detail.reps }} | 时长: {{ detail.duration }}</p>
          <p v-if="detail.notes">备注: {{ detail.notes }}</p>
        </n-timeline-item>
      </n-timeline>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useMessage, useDialog } from 'naive-ui'
import { getMyPlans, generatePlan, deletePlan, getPlanDetail } from '@/api/plan'

const message = useMessage()
const dialog = useDialog()

const plans = ref([])
const showGenerateModal = ref(false)
const showDetailModal = ref(false)
const generating = ref(false)
const selectedPlan = ref(null)
const planDetails = ref([])
const formRef = ref(null)

const generateForm = reactive({
  goal: null,
  bodyPart: null,
  experience: null
})

const rules = {
  goal: [{ required: true, message: '请选择健身目标', trigger: 'change' }],
  bodyPart: [{ required: true, message: '请选择训练部位', trigger: 'change' }],
  experience: [{ required: true, message: '请选择经验水平', trigger: 'change' }]
}

const goalOptions = [
  { label: '增肌', value: 'MUSCLE_GAIN' },
  { label: '减脂', value: 'FAT_LOSS' },
  { label: '塑形', value: 'BODY_SHAPING' },
  { label: '耐力提升', value: 'ENDURANCE' }
]

const bodyPartOptions = [
  { label: '全身', value: 'FULL_BODY' },
  { label: '上肢', value: 'UPPER_BODY' },
  { label: '下肢', value: 'LOWER_BODY' },
  { label: '核心', value: 'CORE' },
  { label: '背部', value: 'BACK' },
  { label: '胸部', value: 'CHEST' }
]

const experienceOptions = [
  { label: '新手', value: 'BEGINNER' },
  { label: '中级', value: 'INTERMEDIATE' },
  { label: '高级', value: 'ADVANCED' }
]

onMounted(() => {
  fetchPlans()
})

async function fetchPlans() {
  try {
    const res = await getMyPlans()
    plans.value = res.data || []
  } catch (error) {
    console.error('获取计划失败:', error)
  }
}

async function handleGenerate() {
  try {
    await formRef.value?.validate()
    generating.value = true
    const res = await generatePlan(generateForm)
    if (res.data?.code === 200) {
      message.success('计划生成成功')
      showGenerateModal.value = false
      fetchPlans()
    } else {
      message.error(res.data?.message || '生成失败')
    }
  } catch (error) {
    console.error('生成计划失败:', error)
  } finally {
    generating.value = false
  }
}

async function viewDetail(plan) {
  selectedPlan.value = plan
  try {
    const res = await getPlanDetail(plan.id)
    planDetails.value = res.data?.details || []
    showDetailModal.value = true
  } catch (error) {
    console.error('获取详情失败:', error)
  }
}

function handleDelete(id) {
  dialog.warning({
    title: '确认删除',
    content: '确定要删除这个健身计划吗？',
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deletePlan(id)
        message.success('删除成功')
        fetchPlans()
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
.fitness-plans h2 {
  margin: 0;
}
</style>
