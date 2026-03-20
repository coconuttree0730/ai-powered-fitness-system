<template>
  <n-message-provider :to="messageContainerRef" :z-index="9999">
    <n-dialog-provider :z-index="9998">
      <router-view />
    </n-dialog-provider>
  </n-message-provider>
  <!-- 全局消息容器 - 确保消息显示在最顶层 -->
  <div ref="messageContainerRef" class="global-message-container"></div>
</template>

<script setup>
import { ref } from 'vue'
import { NMessageProvider, NDialogProvider } from 'naive-ui'

const messageContainerRef = ref(null)
</script>

<style>
#app {
  min-height: 100vh;
}

/* 全局消息容器 - 确保消息显示在最顶层 */
.global-message-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 9999;
}

/* 确保消息内容可以点击 */
:deep(.n-message) {
  pointer-events: auto;
}
</style>
