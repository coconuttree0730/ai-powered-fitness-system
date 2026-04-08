import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useLoadingStore = defineStore('loading', () => {
  // 全局加载状态
  const globalLoading = ref(false)
  const loadingText = ref('加载中...')

  // 显示全局加载
  function showLoading(text = '加载中...') {
    loadingText.value = text
    globalLoading.value = true
  }

  // 隐藏全局加载
  function hideLoading() {
    globalLoading.value = false
  }

  return {
    globalLoading,
    loadingText,
    showLoading,
    hideLoading
  }
})
