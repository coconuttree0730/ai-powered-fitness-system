<template>
  <div class="coach-page">
    <!-- 我的专属教练 -->
    <div class="card-section">
      <div class="section-header">
        <h3 class="section-title">我的专属教练</h3>
        <n-button type="primary" size="small" class="book-btn" @click="showBookingModal = true">
          预约课程
        </n-button>
      </div>
      <div class="my-coach">
        <div class="coach-avatar-large">张</div>
        <div class="coach-details">
          <h3>张教练</h3>
          <p class="specialty">专长: 增肌训练、减脂塑形、体能提升</p>
          <div class="stats">
            <span class="rating">⭐⭐⭐⭐⭐ 4.9分</span>
            <span class="stat-item">授课: 1,280节</span>
            <span class="stat-item">学员: 156人</span>
          </div>
          <p class="next-class">下次课程: 2024年10月20日 14:00 - 增肌训练</p>
        </div>
      </div>
    </div>

    <!-- 推荐教练 -->
    <div class="card-section">
      <div class="section-header">
        <h3 class="section-title">推荐教练</h3>
      </div>
      <n-grid :cols="4" :x-gap="20" class="coach-grid">
        <n-grid-item v-for="coach in recommendedCoaches" :key="coach.id">
          <div class="coach-card">
            <div class="coach-avatar" :style="{ background: coach.gradient }">
              {{ coach.name[0] }}
            </div>
            <div class="coach-info">
              <div class="coach-name">{{ coach.name }}</div>
              <div class="coach-specialty">{{ coach.specialty }}</div>
              <div class="coach-rating">⭐ {{ coach.rating }}分</div>
            </div>
          </div>
        </n-grid-item>
      </n-grid>
    </div>

    <!-- 训练记录 -->
    <div class="card-section">
      <div class="section-header">
        <h3 class="section-title">训练记录</h3>
      </div>
      <n-data-table
        :columns="recordColumns"
        :data="trainingRecords"
        :pagination="false"
        :bordered="false"
        class="record-table"
      />
    </div>

    <!-- 预约课程弹窗 -->
    <n-modal v-model:show="showBookingModal" preset="card" style="width: 500px" title="预约课程">
      <n-form :model="bookingForm" label-placement="left" label-width="80">
        <n-form-item label="课程类型">
          <n-select v-model:value="bookingForm.courseType" :options="courseOptions" />
        </n-form-item>
        <n-form-item label="预约日期">
          <n-date-picker v-model:value="bookingForm.date" type="date" style="width: 100%" />
        </n-form-item>
        <n-form-item label="时间段">
          <n-select v-model:value="bookingForm.timeSlot" :options="timeOptions" />
        </n-form-item>
        <n-form-item label="备注">
          <n-input v-model:value="bookingForm.note" type="textarea" :rows="3" placeholder="请输入备注信息..." />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showBookingModal = false">取消</n-button>
          <n-button type="primary" :loading="bookingLoading" @click="confirmBooking">确认预约</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, h } from 'vue'
import { useMessage, NTag } from 'naive-ui'

const message = useMessage()

const showBookingModal = ref(false)
const bookingLoading = ref(false)

const bookingForm = ref({
  courseType: 'muscle',
  date: null,
  timeSlot: '14:00',
  note: ''
})

const courseOptions = [
  { label: '增肌训练', value: 'muscle' },
  { label: '减脂塑形', value: 'fatloss' },
  { label: '体能提升', value: 'fitness' },
  { label: '康复训练', value: 'rehab' }
]

const timeOptions = [
  { label: '09:00 - 10:00', value: '09:00' },
  { label: '10:00 - 11:00', value: '10:00' },
  { label: '14:00 - 15:00', value: '14:00' },
  { label: '15:00 - 16:00', value: '15:00' },
  { label: '18:00 - 19:00', value: '18:00' },
  { label: '19:00 - 20:00', value: '19:00' }
]

const recommendedCoaches = [
  { id: 1, name: '李教练', specialty: '瑜伽 / 普拉提', rating: 4.8, gradient: 'linear-gradient(135deg, #2EC4B6, #06D6A0)' },
  { id: 2, name: '王教练', specialty: '力量训练 / CrossFit', rating: 4.9, gradient: 'linear-gradient(135deg, #FF6B35, #FFD93D)' },
  { id: 3, name: '刘教练', specialty: '有氧 / 舞蹈', rating: 4.7, gradient: 'linear-gradient(135deg, #EF476F, #FFD166)' },
  { id: 4, name: '陈教练', specialty: '康复 / 体态矫正', rating: 4.9, gradient: 'linear-gradient(135deg, #667eea, #764ba2)' }
]

const recordColumns = [
  { title: '日期', key: 'date' },
  { title: '教练', key: 'coach' },
  { title: '课程类型', key: 'courseType' },
  { title: '时长', key: 'duration' },
  { title: '消耗卡路里', key: 'calories' },
  {
    title: '状态',
    key: 'status',
    render(row) {
      return h(NTag, { type: 'success', size: 'small' }, { default: () => '已完成' })
    }
  }
]

const trainingRecords = [
  { date: '2024-10-15', coach: '张教练', courseType: '增肌训练', duration: '60分钟', calories: '420 kcal', status: 'completed' },
  { date: '2024-10-12', coach: '张教练', courseType: '体能测试', duration: '45分钟', calories: '280 kcal', status: 'completed' },
  { date: '2024-10-08', coach: '张教练', courseType: '核心训练', duration: '50分钟', calories: '350 kcal', status: 'completed' }
]

function confirmBooking() {
  bookingLoading.value = true
  setTimeout(() => {
    bookingLoading.value = false
    showBookingModal.value = false
    message.success('预约成功！')
    bookingForm.value = { courseType: 'muscle', date: null, timeSlot: '14:00', note: '' }
  }, 1500)
}
</script>

<style scoped>
.coach-page {
  max-width: 1400px;
  margin: 0 auto;
}

.card-section {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.06);
  margin-bottom: 24px;
  transition: transform 0.3s, box-shadow 0.3s;
}

.card-section:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(0,0,0,0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #1A1A2E;
  margin: 0;
}

.book-btn {
  padding: 10px 20px;
  font-size: 13px;
  border-radius: 12px;
}

.my-coach {
  display: flex;
  gap: 24px;
  align-items: center;
}

.coach-avatar-large {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: linear-gradient(135deg, #2EC4B6, #06D6A0);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  color: white;
  flex-shrink: 0;
}

.coach-details {
  flex: 1;
}

.coach-details h3 {
  font-size: 20px;
  margin-bottom: 8px;
  font-weight: 600;
  color: #1A1A2E;
}

.specialty {
  color: #6B7280;
  margin-bottom: 8px;
  font-size: 14px;
}

.stats {
  display: flex;
  gap: 16px;
  margin-bottom: 8px;
  align-items: center;
}

.rating {
  color: #FFD93D;
  font-weight: 600;
}

.stat-item {
  color: #6B7280;
  font-size: 14px;
}

.next-class {
  color: #FF6B35;
  font-size: 14px;
  margin: 0;
}

.coach-grid {
  margin-bottom: 0;
}

.coach-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0,0,0,0.06);
  transition: all 0.3s;
  cursor: pointer;
}

.coach-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0,0,0,0.12);
}

.coach-avatar {
  width: 100%;
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  color: white;
}

.coach-info {
  padding: 16px;
}

.coach-name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 4px;
  color: #1A1A2E;
}

.coach-specialty {
  font-size: 13px;
  color: #6B7280;
  margin-bottom: 8px;
}

.coach-rating {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #FFD93D;
  font-size: 14px;
  font-weight: 600;
}

.record-table :deep(.n-data-table-th) {
  font-weight: 600;
  color: #6B7280;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding: 14px 16px;
}

.record-table :deep(.n-data-table-td) {
  padding: 14px 16px;
  font-size: 14px;
}
</style>
