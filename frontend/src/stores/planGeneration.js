import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getGenerationTaskStatus } from '@/api/chat'

export const usePlanGenerationStore = defineStore('planGeneration', () => {
  // 进行中的任务
  const activeTaskId = ref(null)
  const isGenerating = ref(false)
  const taskResult = ref(null)
  const taskError = ref(null)
  const planGenStep = ref(0)
  const planGenProgress = ref(0)

  // 定时器引用（不持久化）
  let pollTimer = null
  let progressTimer = null
  let planGenTimer = null

  // 计算属性
  const hasActiveTask = computed(() => !!activeTaskId.value && isGenerating.value)
  const isCompleted = computed(() => !!taskResult.value && !isGenerating.value)

  // 步骤文本
  const planGenStepText = computed(() => {
    const texts = [
      '正在准备生成环境...',
      '正在分析您的健身档案...',
      '正在匹配适合您的课程...',
      '正在制定个性化训练方案...',
      '即将完成，正在整理数据...'
    ]
    return texts[planGenStep.value] || texts[0]
  })

  // 开始生成计划
  function startGeneration(taskId) {
    activeTaskId.value = taskId
    isGenerating.value = true
    taskResult.value = null
    taskError.value = null
    planGenStep.value = 0
    planGenProgress.value = 0

    // 保存到 sessionStorage，确保页面刷新后也能恢复
    sessionStorage.setItem('planGeneration_taskId', taskId)
    sessionStorage.setItem('planGeneration_isGenerating', 'true')

    // 启动动画
    startPlanGenAnimation()

    // 开始轮询
    startPolling(taskId)
  }

  // 启动轮询
  function startPolling(taskId) {
    const maxAttempts = 60 * 5 // 最多轮询5分钟
    const intervalMs = 2000
    let attempts = 0

    // 清除之前的定时器
    stopPolling()

    pollTimer = setInterval(async () => {
      attempts++
      try {
        const task = await getGenerationTaskStatus(taskId)
        if (!task) {
          handleError('无法获取任务状态')
          return
        }

        if (task.status === 'COMPLETED') {
          handleComplete(task.result)
          return
        }

        if (task.status === 'FAILED') {
          handleError(task.errorMessage || '生成计划失败')
          return
        }

        if (attempts >= maxAttempts) {
          handleError('生成超时，请稍后重试')
          return
        }
      } catch (error) {
        handleError(error.message || '轮询任务状态失败')
      }
    }, intervalMs)
  }

  // 处理完成
  function handleComplete(result) {
    stopPolling()
    stopPlanGenAnimation()
    isGenerating.value = false
    taskResult.value = result
    planGenStep.value = 4
    planGenProgress.value = 100

    // 清除 sessionStorage
    sessionStorage.removeItem('planGeneration_taskId')
    sessionStorage.removeItem('planGeneration_isGenerating')
  }

  // 处理错误
  function handleError(errorMessage) {
    stopPolling()
    stopPlanGenAnimation()
    isGenerating.value = false
    taskError.value = errorMessage
    activeTaskId.value = null

    // 清除 sessionStorage
    sessionStorage.removeItem('planGeneration_taskId')
    sessionStorage.removeItem('planGeneration_isGenerating')
  }

  // 停止轮询
  function stopPolling() {
    if (pollTimer) {
      clearInterval(pollTimer)
      pollTimer = null
    }
  }

  // 启动进度动画
  function startPlanGenAnimation() {
    planGenStep.value = 0
    planGenProgress.value = 0

    // 进度条动画
    progressTimer = setInterval(() => {
      if (planGenProgress.value < 90) {
        const increment = Math.max(1, Math.floor((100 - planGenProgress.value) / 10))
        planGenProgress.value = Math.min(90, planGenProgress.value + increment)
      }
    }, 200)

    // 模拟步骤进度
    planGenTimer = setInterval(() => {
      if (planGenStep.value < 4) {
        planGenStep.value++
      }
    }, 1200)
  }

  // 停止进度动画
  function stopPlanGenAnimation() {
    if (planGenTimer) {
      clearInterval(planGenTimer)
      planGenTimer = null
    }
    if (progressTimer) {
      clearInterval(progressTimer)
      progressTimer = null
    }
  }

  // 恢复任务（页面切换后）
  function restoreTask() {
    const savedTaskId = sessionStorage.getItem('planGeneration_taskId')
    const savedIsGenerating = sessionStorage.getItem('planGeneration_isGenerating')

    if (savedTaskId && savedIsGenerating === 'true' && !isGenerating.value) {
      activeTaskId.value = savedTaskId
      isGenerating.value = true
      taskResult.value = null
      taskError.value = null
      startPlanGenAnimation()
      startPolling(savedTaskId)
      return true
    }
    return false
  }

  // 清除状态
  function clearState() {
    stopPolling()
    stopPlanGenAnimation()
    activeTaskId.value = null
    isGenerating.value = false
    taskResult.value = null
    taskError.value = null
    planGenStep.value = 0
    planGenProgress.value = 0
    sessionStorage.removeItem('planGeneration_taskId')
    sessionStorage.removeItem('planGeneration_isGenerating')
  }

  return {
    // State
    activeTaskId,
    isGenerating,
    taskResult,
    taskError,
    planGenStep,
    planGenProgress,

    // Getters
    hasActiveTask,
    isCompleted,
    planGenStepText,

    // Actions
    startGeneration,
    startPolling,
    handleComplete,
    handleError,
    stopPolling,
    startPlanGenAnimation,
    stopPlanGenAnimation,
    restoreTask,
    clearState
  }
})
