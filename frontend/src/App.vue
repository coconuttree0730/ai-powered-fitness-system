<template>
  <n-message-provider>
    <router-view />
  </n-message-provider>
</template>

<script setup>
import { NMessageProvider } from 'naive-ui'
</script>

<style>
#app {
  min-height: 100vh;
}
</style>
