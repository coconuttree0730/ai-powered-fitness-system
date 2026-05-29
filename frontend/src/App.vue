<template>
  <n-config-provider :z-index="9999">
    <n-message-provider>
      <n-notification-provider>
        <n-dialog-provider>
          <n-loading-bar-provider>
            <router-view />
          </n-loading-bar-provider>
        </n-dialog-provider>
      </n-notification-provider>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup>
import { NConfigProvider, NMessageProvider, NNotificationProvider, NDialogProvider, NLoadingBarProvider } from 'naive-ui'
</script>

<style>
#app {
  min-height: 100vh;
}
</style>
