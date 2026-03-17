<template>
  <div class="member-home">
    <n-grid :cols="2" :x-gap="20">
      <n-grid-item>
        <n-card title="快速入口">
          <n-space>
            <n-button type="primary" @click="$router.push('/member/bookings')">
              我的预约
            </n-button>
            <n-button type="info" @click="$router.push('/member/plans')">
              健身计划
            </n-button>
            <n-button @click="$router.push('/member/equipment')">
              器材列表
            </n-button>
          </n-space>
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card title="个人信息完成度">
          <n-progress
            type="line"
            :percentage="profileCompletion"
            :indicator-placement="'inside'"
            :status="profileCompletion === 100 ? 'success' : 'default'"
          />
          <p style="margin-top: 12px; color: #666">
            {{ profileCompletion === 100 ? '信息已完善' : '请完善个人信息以获得更好的体验' }}
          </p>
        </n-card>
      </n-grid-item>
    </n-grid>

    <n-card title="最近预约" style="margin-top: 20px">
      <n-empty v-if="recentBookings.length === 0" description="暂无预约" />
      <n-list v-else bordered>
        <n-list-item v-for="booking in recentBookings" :key="booking.id">
          <n-thing :title="booking.courseName">
            <template #description>
              <n-space>
                <span>教练: {{ booking.coachName }}</span>
                <span>时间: {{ formatTime(booking.startTime) }}</span>
                <n-tag :type="booking.status === 1 ? 'success' : 'default'">
                  {{ booking.status === 1 ? '已预约' : '已取消' }}
                </n-tag>
              </n-space>
            </template>
          </n-thing>
        </n-list-item>
      </n-list>
    </n-card>

    <n-card title="我的健身计划" style="margin-top: 20px">
      <n-empty v-if="!latestPlan" description="暂无健身计划">
        <template #extra>
          <n-button type="primary" @click="$router.push('/member/plans')">
            生成计划
          </n-button>
        </template>
      </n-empty>
      <div v-else>
        <h4>{{ latestPlan.planName }}</h4>
        <p>目标: {{ latestPlan.goal }}</p>
        <n-button text type="primary" @click="$router.push('/member/plans')">
          查看详情
        </n-button>
      </div>
    </n-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getMyBookings } from '@/api/course'
import { getMyPlans } from '@/api/plan'
import { getProfile } from '@/api/plan'

const recentBookings = ref([])
const latestPlan = ref(null)
const profile = ref({})

const profileCompletion = computed(() => {
  if (!profile.value) return 0
  const fields = ['height', 'weight', 'age', 'experience']
  const filled = fields.filter(f => profile.value[f]).length
  return Math.round((filled / fields.length) * 100)
})

onMounted(async () => {
  await Promise.all([fetchBookings(), fetchPlans(), fetchProfile()])
})

async function fetchBookings() {
  try {
    const res = await getMyBookings()
    recentBookings.value = (res.data || []).slice(0, 5)
  } catch (error) {
    console.error('获取预约失败:', error)
  }
}

async function fetchPlans() {
  try {
    const res = await getMyPlans()
    latestPlan.value = (res.data || [])[0]
  } catch (error) {
    console.error('获取计划失败:', error)
  }
}

async function fetchProfile() {
  try {
    const res = await getProfile()
    profile.value = res.data || {}
  } catch (error) {
    console.error('获取个人信息失败:', error)
  }
}

function formatTime(time) {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.member-home {
  padding: 0;
}
</style>
