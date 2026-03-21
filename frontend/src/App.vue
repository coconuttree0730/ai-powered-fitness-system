<template>
  <n-message-provider>
    <n-dialog-provider>
      <router-view />
    </n-dialog-provider>
  </n-message-provider>
</template>

<script setup>
import { NMessageProvider, NDialogProvider } from 'naive-ui'
</script>

<style>
#app {
  min-height: 100vh;
}

/* 确保消息提示在最上层 */
.n-message-container {
  z-index: 99999 !important;
}

.n-notification-container {
  z-index: 99999 !important;
}
</style>
