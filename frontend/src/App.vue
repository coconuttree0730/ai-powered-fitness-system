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
</style>
