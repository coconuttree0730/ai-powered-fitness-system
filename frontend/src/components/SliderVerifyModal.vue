<template>
  <Teleport to="body">
    <Transition name="slider-fade">
      <div v-if="visible" class="slider-verify-overlay" @click.self="handleClose">
        <Transition name="slider-scale">
          <div v-if="visible" class="slider-verify-container">
            <div class="slider-verify-header">
              <h3 class="slider-verify-title">安全验证</h3>
              <p class="slider-verify-subtitle">请拖动滑块完成验证</p>
              <button class="slider-close-btn" @click="handleClose">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M18 6L6 18M6 6l12 12"/>
                </svg>
              </button>
            </div>

            <div class="slider-verify-content">
              <!-- 自定义滑块验证 -->
              <div class="slider-box" :style="{ width: boxWidth + 'px' }">
                <div class="slider-bg" :class="{ success: isSuccess }"></div>
                <div class="slider-text" v-if="!isSuccess">{{ sliderText }}</div>
                <div class="slider-text success-text" v-else>{{ successText }}</div>

                <div
                  class="slider-btn"
                  :class="{ success: isSuccess, moving: isMoving }"
                  :style="{ left: sliderLeft + 'px' }"
                  @mousedown="startDrag"
                  @touchstart="startDrag"
                >
                  <svg v-if="!isSuccess" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M9 18l6-6-6-6"/>
                  </svg>
                  <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M20 6L9 17l-5-5"/>
                  </svg>
                </div>
              </div>
            </div>

            <div class="slider-verify-footer">
              <p class="slider-verify-tip">拖动滑块以获取验证码</p>
            </div>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  boxWidth: {
    type: Number,
    default: 320
  },
  sliderText: {
    type: String,
    default: '拖动滑块验证'
  },
  successText: {
    type: String,
    default: '验证成功'
  }
})

const emit = defineEmits(['update:visible', 'verify-success', 'verify-fail', 'close'])

// 滑块状态
const isMoving = ref(false)
const isSuccess = ref(false)
const sliderLeft = ref(0)
const startX = ref(0)
const currentX = ref(0)

// 滑块按钮宽度
const btnWidth = 40
// 成功阈值（滑动到95%以上算成功）
const successThreshold = 0.95

// 开始拖动
function startDrag(e) {
  if (isSuccess.value) return
  isMoving.value = true
  startX.value = e.type.includes('touch') ? e.touches[0].clientX : e.clientX
  currentX.value = sliderLeft.value

  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', endDrag)
  document.addEventListener('touchmove', onDrag)
  document.addEventListener('touchend', endDrag)
}

// 拖动中
function onDrag(e) {
  if (!isMoving.value) return
  e.preventDefault()

  const clientX = e.type.includes('touch') ? e.touches[0].clientX : e.clientX
  const diff = clientX - startX.value
  let newLeft = currentX.value + diff

  // 限制滑动范围
  const maxLeft = props.boxWidth - btnWidth
  newLeft = Math.max(0, Math.min(newLeft, maxLeft))

  sliderLeft.value = newLeft
}

// 结束拖动
function endDrag() {
  if (!isMoving.value) return
  isMoving.value = false

  const maxLeft = props.boxWidth - btnWidth
  const progress = sliderLeft.value / maxLeft

  if (progress >= successThreshold) {
    // 验证成功
    sliderLeft.value = maxLeft
    isSuccess.value = true

    // 触发成功事件
    emit('verify-success', {
      timestamp: Date.now(),
      sliderValue: Math.round(sliderLeft.value)
    })
  } else {
    // 验证失败，回弹
    resetSlider()
    emit('verify-fail')
  }

  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', endDrag)
  document.removeEventListener('touchmove', onDrag)
  document.removeEventListener('touchend', endDrag)
}

// 重置滑块
function resetSlider() {
  sliderLeft.value = 0
  isSuccess.value = false
  isMoving.value = false
}

// 关闭弹窗
function handleClose() {
  emit('update:visible', false)
  emit('close')
  resetSlider()
}

// 验证通过后的处理
function onVerifyComplete() {
  setTimeout(() => {
    handleClose()
  }, 800)
}

// 验证失败的处理
function onVerifyError() {
  resetSlider()
}

defineExpose({
  onVerifyComplete,
  onVerifyError,
  resetSlider
})

// 清理事件监听
onUnmounted(() => {
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', endDrag)
  document.removeEventListener('touchmove', onDrag)
  document.removeEventListener('touchend', endDrag)
})
</script>

<style scoped>
/* 遮罩层 */
.slider-verify-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(10, 10, 15, 0.75);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
  padding: 20px;
}

/* 弹窗容器 */
.slider-verify-container {
  width: 360px;
  background: #1A1A25;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.6), 0 0 0 1px rgba(255, 255, 255, 0.05);
  position: relative;
}

/* 头部 */
.slider-verify-header {
  padding: 24px 24px 16px;
  text-align: center;
  position: relative;
}

.slider-verify-title {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
  margin: 0 0 8px 0;
}

.slider-verify-subtitle {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.5);
  margin: 0;
}

.slider-close-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 32px;
  height: 32px;
  border: none;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.5);
  transition: all 0.3s ease;
}

.slider-close-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
}

/* 内容区 */
.slider-verify-content {
  padding: 20px;
  display: flex;
  justify-content: center;
}

/* 滑块容器 */
.slider-box {
  height: 40px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

/* 背景进度条 */
.slider-bg {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  width: 0;
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C61 100%);
  border-radius: 8px;
  transition: width 0.3s ease;
}

.slider-bg.success {
  width: 100% !important;
}

/* 文字 */
.slider-text {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.5);
  user-select: none;
  z-index: 1;
}

.slider-text.success-text {
  color: #fff;
}

/* 滑块按钮 */
.slider-btn {
  position: absolute;
  left: 0;
  top: 0;
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C61 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #fff;
  z-index: 2;
  transition: background 0.3s ease;
  box-shadow: 0 2px 8px rgba(255, 107, 53, 0.3);
}

.slider-btn:hover {
  background: linear-gradient(135deg, #E55A2B 0%, #FF6B35 100%);
}

.slider-btn.moving {
  cursor: grabbing;
}

.slider-btn.success {
  background: #52c41a;
  cursor: default;
}

/* 底部 */
.slider-verify-footer {
  padding: 0 24px 20px;
  text-align: center;
}

.slider-verify-tip {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
  margin: 0;
}

/* 动画 */
.slider-fade-enter-active,
.slider-fade-leave-active {
  transition: opacity 0.3s ease;
}

.slider-fade-enter-from,
.slider-fade-leave-to {
  opacity: 0;
}

.slider-scale-enter-active,
.slider-scale-leave-active {
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.slider-scale-enter-from,
.slider-scale-leave-to {
  opacity: 0;
  transform: scale(0.9) translateY(20px);
}
</style>
