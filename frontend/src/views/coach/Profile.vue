<template>
  <div class="coach-profile">
    <n-card title="个人信息">
      <n-descriptions label-placement="left" :column="2">
        <n-descriptions-item label="用户名">{{ user.username }}</n-descriptions-item>
        <n-descriptions-item label="姓名">{{ user.realName }}</n-descriptions-item>
        <n-descriptions-item label="手机号">{{ user.phone }}</n-descriptions-item>
        <n-descriptions-item label="邮箱">{{ user.email }}</n-descriptions-item>
      </n-descriptions>
    </n-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const user = computed(() => authStore.user || {})
</script>

<style scoped>
.coach-profile {
  padding: 0;
}
</style>
