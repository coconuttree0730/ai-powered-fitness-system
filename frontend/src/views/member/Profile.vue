<template>
  <div class="profile-page">
    <n-card title="个人信息">
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="left" label-width="100">
        <n-form-item label="身高" path="height">
          <n-input-number v-model:value="form.height" :min="50" :max="250" placeholder="请输入身高">
            <template #suffix>cm</template>
          </n-input-number>
        </n-form-item>
        <n-form-item label="体重" path="weight">
          <n-input-number v-model:value="form.weight" :min="20" :max="300" :precision="1" placeholder="请输入体重">
            <template #suffix>kg</template>
          </n-input-number>
        </n-form-item>
        <n-form-item label="年龄" path="age">
          <n-input-number v-model:value="form.age" :min="1" :max="150" placeholder="请输入年龄" />
        </n-form-item>
        <n-form-item label="健身经验" path="experience">
          <n-select v-model:value="form.experience" :options="experienceOptions" placeholder="请选择健身经验" />
        </n-form-item>
        <n-form-item label="健身目标" path="fitnessGoal">
          <n-select v-model:value="form.fitnessGoal" :options="goalOptions" placeholder="请选择健身目标" />
        </n-form-item>
        <n-form-item>
          <n-button type="primary" :loading="loading" @click="handleSubmit">保存</n-button>
        </n-form-item>
      </n-form>
    </n-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { getProfile, updateProfile } from '@/api/plan'

const message = useMessage()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  height: null,
  weight: null,
  age: null,
  experience: null,
  fitnessGoal: null
})

const rules = {
  height: [{ required: true, message: '请输入身高', trigger: 'blur' }],
  weight: [{ required: true, message: '请输入体重', trigger: 'blur' }],
  age: [{ required: true, message: '请输入年龄', trigger: 'blur' }]
}

const experienceOptions = [
  { label: '初学者', value: 'BEGINNER' },
  { label: '中级', value: 'INTERMEDIATE' },
  { label: '高级', value: 'ADVANCED' }
]

const goalOptions = [
  { label: '减脂', value: 'WEIGHT_LOSS' },
  { label: '增肌', value: 'MUSCLE_GAIN' },
  { label: '塑形', value: 'BODY_SHAPING' },
  { label: '增强体能', value: 'ENDURANCE' },
  { label: '保持健康', value: 'HEALTH' }
]

onMounted(() => {
  fetchProfile()
})

async function fetchProfile() {
  try {
    const res = await getProfile()
    if (res.data) {
      Object.assign(form, res.data)
    }
  } catch (error) {
    console.error('获取个人信息失败:', error)
  }
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    loading.value = true
    await updateProfile(form)
    message.success('保存成功')
  } catch (error) {
    message.error(error.message || '保存失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.profile-page {
  padding: 0;
}
</style>
